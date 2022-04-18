package com.giosis.util.qdrive.international

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.giosis.library.data.LoginInfo
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.main.MainActivity
import com.giosis.library.main.SMSVerificationActivity
import com.giosis.library.server.RetrofitClient
import com.giosis.library.setting.DeveloperModeActivity
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.PermissionActivity
import com.giosis.library.util.PermissionChecker
import com.giosis.library.util.Preferences
import com.giosis.util.qdrive.international.databinding.ActivityLoginBinding
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class LoginActivity : CommonActivity() {

    val tag = "LoginActivity"
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val progressBar by lazy {
        ProgressBar(this)
    }

    private var spinnerList = ArrayList<LoginNation>()
    private var spinnerPosition = 0
    private lateinit var appVersion: String

    // Location
    private val gpsTrackerManager: GPSTrackerManager? by lazy {
        GPSTrackerManager(this)
    }

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(
        PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
        PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
    )


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Image Shake Animation
        binding.imgLoginTopBg.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.shake_animation
            )
        )

        binding.imgLoginTopLogo.setOnClickListener {
            binding.imgLoginTopBg.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.shake_animation
                )
            )
        }


        var showDeveloperModeClickCount = 0

        binding.imgLoginBottomLogo.setOnClickListener {

            if (showDeveloperModeClickCount == 10) {

                showDeveloperModeClickCount = 0

                val intent = Intent(this, DeveloperModeActivity::class.java)
                startActivity(intent)
            } else {

                showDeveloperModeClickCount++
            }
        }


        progressBar.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.layoutLogin.addView(progressBar)
        progressBar.visibility = View.GONE


        // Nation
        spinnerList.add(
            LoginNation(
                resources.getString(R.string.text_malaysia),
                "MY",
                "login_icon_my"
            )
        )
        spinnerList.add(
            LoginNation(
                resources.getString(R.string.text_Indonesia),
                "ID",
                "login_icon_id"
            )
        )

        binding.spinnerSelectNation.adapter = LoginSpinnerAdapter(this, spinnerList)

        when (Preferences.userNation) {
            "MY" -> {
                binding.spinnerSelectNation.setSelection(0)
            }
            "ID" -> {
                binding.spinnerSelectNation.setSelection(1)
            }
            else -> {
                // TEST
                binding.spinnerSelectNation.setSelection(0)
            }
        }

        binding.layoutLoginSelectNation.setOnClickListener {

            binding.spinnerSelectNation.performClick()
        }

        binding.spinnerSelectNation.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    parent?.let {

                        hideKeyboard()

                        spinnerPosition = position

                        val resourceId = resources.getIdentifier(
                            spinnerList[position].nationImg,
                            "drawable",
                            packageName
                        )
                        binding.imgLoginNation.setBackgroundResource(resourceId)
                        binding.textLoginNation.text = spinnerList[position].nation
                        Log.e(tag, " Select Nation : ${binding.textLoginNation.text}")
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }


        //
        binding.editLoginId.setText(Preferences.userId)
        binding.editLoginPassword.setText(Preferences.userPw)
        appVersion = getVersion()

        // Login
        binding.btnLoginSign.setOnClickListener {

            hideKeyboard()

            val userNationCode = spinnerList[spinnerPosition].nationCode

            val userID = binding.editLoginId.text.toString().trim()
            val userPW = binding.editLoginPassword.text.toString().trim()
            val deviceUUID = getDeviceUUID()
            Log.e(tag, " Input Data  -  $userNationCode  / $userID  / $userPW  / $deviceUUID")

            // DB 파일 생성여부
            val dbFile = File(DatabaseHelper.getInstance().dbPath)

            // 위치 정보
            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
            Log.e(tag, "  Location  -  $latitude / $longitude")

            when {
                userID.isEmpty() -> {
                    showDialog(resources.getString(R.string.msg_please_input_id))
                    return@setOnClickListener
                }
                userPW.isEmpty() -> {
                    showDialog(resources.getString(R.string.msg_please_input_password))
                    return@setOnClickListener
                }
                !dbFile.exists() -> {
                    showDialog(resources.getString(R.string.msg_db_problem))
                }

                else -> {
                    Preferences.userNation = userNationCode
                    Preferences.userId = userID
                    Preferences.userPw = userPW
                    Preferences.deviceUUID = deviceUUID

                    progressBar.visibility = View.VISIBLE

                    RetrofitClient.instanceDynamic().requestServerLogin(
                        userID, userPW, "QDRIVE_V2", "", deviceUUID, "",
                        latitude.toString(), longitude.toString(), "QDRIVE", userNationCode
                    ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                            Log.e("Server", "result  ${it.resultCode}  ${it.resultMsg}")

                            if (it.resultCode != 0) {        // Login Failed

                                progressBar.visibility = View.GONE
                                Preferences.userPw = ""
                                binding.editLoginPassword.setText("")

                                when {
                                    it.resultCode == -10 -> {
                                        showDialog(resources.getString(R.string.msg_account_deactivated))
                                    }
                                    it.resultMsg != "" -> {
                                        showDialog(it.resultMsg)
                                    }
                                    else -> {
                                        showDialog(resources.getString(R.string.msg_not_valid_info))
                                    }
                                }
                            } else {        // Login Success

                                progressBar.visibility = View.GONE

                                val loginData =
                                    Gson().fromJson(it.resultObject, LoginInfo::class.java)

                                Log.e(RetrofitClient.TAG, "response : ${it.resultObject}")

                                lifecycleScope.launch {
                                    val response = RetrofitClient.instanceDynamic()
                                        .requestAppVersionCheck()

                                    if (response.resultCode == -10) {
                                        val msg = java.lang.String.format(
                                            resources.getString(R.string.msg_update_version),
                                            loginData.version,
                                            loginData.version
                                        )
                                        goGooglePlay(msg)
                                    } else {
                                        Preferences.userId = loginData.opId!!
                                        Preferences.userPw = userPW
                                        Preferences.deviceUUID = deviceUUID

                                        if (!loginData.opNm.isNullOrEmpty()) {
                                            Preferences.userName = loginData.opNm!!
                                        } else {
                                            Preferences.userName = ""
                                        }

                                        if (!loginData.epEmail.isNullOrEmpty()) {
                                            Preferences.userEmail = loginData.epEmail!!
                                        } else {
                                            Preferences.userEmail = ""
                                        }

                                        if (!loginData.officeCode.isNullOrEmpty()) {
                                            Preferences.officeCode = loginData.officeCode!!
                                        } else {
                                            Preferences.officeCode = ""
                                        }

                                        if (!loginData.officeName.isNullOrEmpty()) {
                                            Preferences.officeName = loginData.officeName!!
                                        } else {
                                            Preferences.officeName = ""
                                        }

                                        if (!loginData.pickupDriverYN.isNullOrEmpty()) {
                                            Preferences.pickupDriver =
                                                loginData.pickupDriverYN!!
                                        } else {
                                            Preferences.pickupDriver = "N"
                                        }

                                        if (!loginData.shuttle_driver_yn.isNullOrEmpty()) {
                                            Preferences.outletDriver =
                                                loginData.shuttle_driver_yn!!
                                        } else {
                                            Preferences.outletDriver = ""
                                        }

                                        if (!loginData.locker_driver_status.isNullOrEmpty()) {
                                            Preferences.lockerStatus =
                                                loginData.locker_driver_status!!
                                        } else {
                                            Preferences.lockerStatus = ""
                                        }

                                        if (!loginData.defaultYn.isNullOrEmpty()) {
                                            Preferences.default =
                                                loginData.defaultYn!!
                                        } else {
                                            Preferences.default = ""
                                        }

                                        if (!loginData.authNo.isNullOrEmpty()) {
                                            Preferences.authNo =
                                                loginData.authNo!!
                                        } else {
                                            Preferences.authNo = ""
                                        }

                                        if (loginData.smsYn == "Y" && loginData.deviceYn == "Y") {

                                            val intent =
                                                Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            goSMSVerification(resources.getString(R.string.msg_go_sms_verification))
                                        }
                                    }
                                }
                            }
                        }, {

                            Log.e(RetrofitClient.errorTag, it.toString())
                            progressBar.visibility = View.GONE
                            showDialog(it.toString())
                        })
                }
            }
        }


        // permission
        val checker = PermissionChecker(this)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {

            isPermissionTrue = true
        }
    }


    override fun onResume() {
        super.onResume()

        var info = ""
        if (Preferences.serverURL.contains("test")) {
            info = "test / "
        } else if (Preferences.serverURL.contains("staging")) {
            info = "staging / "
        }

        binding.textLoginVersion.text =
            "$info${resources.getString(R.string.text_app_version)} - $appVersion"

        if (isPermissionTrue) {

            val gpsEnable = gpsTrackerManager?.enableGPSSetting()

            if (gpsEnable == true) {

                gpsTrackerManager?.gpsTrackerStart()
                Log.e(
                    tag,
                    " onResume  Location  :  ${gpsTrackerManager?.latitude} / ${gpsTrackerManager?.longitude}"
                )
            } else {
                if (!this@LoginActivity.isFinishing) {
                    AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(resources.getString(R.string.text_location_setting))
                        .setMessage(resources.getString(R.string.msg_location_off))
                        .setPositiveButton(
                            resources.getString(R.string.button_ok)
                        ) { dialog, which ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            startActivity(intent)
                        }.show()
                }
            }
        }
    }


    @SuppressLint("HardwareIds")
    private fun getDeviceUUID(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getVersion(): String {

        return try {

            val packageInfo =
                applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {

            ""
        }
    }


    fun showDialog(msg: String?) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(
            resources.getString(R.string.button_ok)
        ) { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

    fun hideKeyboard() {

        val view = this.currentFocus

        if (view != null) {

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun goGooglePlay(msg: String?) {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(
            resources.getString(R.string.button_ok)
        ) { dialog, _ ->
            val uri: Uri = Uri.parse("market://details?id=com.giosis.util.qdrive.international")
            val itt = Intent(Intent.ACTION_VIEW, uri)
            startActivity(itt)
            dialog.dismiss()
        }
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }


    fun goSMSVerification(msg: String?) {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(
            resources.getString(R.string.button_ok)
        ) { _, _ ->
            val intent = Intent(this, SMSVerificationActivity::class.java)
            startActivity(intent)
            finish()
        }
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsTrackerManager?.stopFusedProviderService()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

            isPermissionTrue = true
            Log.e("permission", "$tag   Permission granted")
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}