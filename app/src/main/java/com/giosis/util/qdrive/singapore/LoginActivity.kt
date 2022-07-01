package com.giosis.util.qdrive.singapore


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.giosis.util.qdrive.singapore.data.LoginInfo
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.databinding.ActivityLoginBinding
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.main.MainActivity
import com.giosis.util.qdrive.singapore.main.SMSVerificationActivity
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.setting.DeveloperModeActivity
import com.giosis.util.qdrive.singapore.util.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class LoginActivity : CommonActivity() {

    val tag = "LoginActivity"

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val progressBar by lazy {
        ProgressBar(this@LoginActivity)
    }

    private var nationList = ArrayList<LoginNation>()
    private var spinnerPosition = 0

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

        QDataUtil.setCustomUserAgent(this@LoginActivity)
        // Image Shake Animation
        binding.imgLoginTopBg.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.shake_animation
            )
        )
        ///
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

        lifecycleScope.launch {
            getNationList()

            if (nationList.size > 0) {
                binding.spinnerSelectNation.adapter =
                    LoginSpinnerAdapter(this@LoginActivity, nationList)
            }

            // Nation
            val nationCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales[0].country
            } else {
                Resources.getSystem().configuration.locale.country
            }

            for ((index, item) in nationList.withIndex()) {
                if (nationCode == item.nation_cd) {
                    binding.textLoginNation.text = item.nation_nm

                    Glide.with(this@LoginActivity)
                        .load(item.nation_img_url)
                        .into(binding.imgLoginNation)

                    binding.spinnerSelectNation.setSelection(index)

                    spinnerPosition = index
                    break
                }
            }
        }

        binding.layoutLoginSelectNation.setOnClickListener {
            if (nationList.size == 0) {
                lifecycleScope.launch {
                    getNationList()
                }
            }
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

                        spinnerPosition = position
                        Glide.with(this@LoginActivity)
                            .load(nationList[position].nation_img_url)
                            .into(binding.imgLoginNation)

                        binding.textLoginNation.text = nationList[position].nation_nm

                    }

                    hideKeyboard()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        binding.editLoginId.setText(Preferences.userId)
        binding.editLoginPassword.setText(Preferences.userPw)

        // Login
        binding.btnLoginSign.setOnClickListener {
            loginBtnClick()
        }

        // permission
        val checker = PermissionChecker(this)

        if (checker.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
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
            "$info${resources.getString(R.string.text_app_version)} - ${getVersion()}"

        if (isPermissionTrue) {

            val gpsEnable = gpsTrackerManager?.enableGPSSetting()

            if (gpsEnable == true) {
                gpsTrackerManager?.gpsTrackerStart()

            } else {
                DataUtil.enableLocationSettings(this@LoginActivity)
            }
        }
    }

    private fun loginBtnClick() {
        hideKeyboard()

        lifecycleScope.launch {

            progressBar.visibility = View.VISIBLE

            if (nationList.size == 0) {
                getNationList()
            }

            val userNationCode = try {
                nationList[spinnerPosition].nation_cd
            } catch (e: Exception) {
                "SG"
            }

            val userID = binding.editLoginId.text.toString().trim()
            val userPW = binding.editLoginPassword.text.toString().trim()
            val deviceUUID = getDeviceUUID()

            // DB 파일 생성여부
            // todo_sypark  없으면 만들도록 . ...
            val dbFile = File(DatabaseHelper.getInstance().dbPath)
            if (!dbFile.exists()) {
                showDialog(resources.getString(R.string.msg_db_problem))
            }

            // 위치 정보
            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }

            if (userID.isEmpty()) {
                showDialog(resources.getString(R.string.msg_please_input_id))
                return@launch
            }

            if (userPW.isEmpty()) {
                showDialog(resources.getString(R.string.msg_please_input_password))
                return@launch
            }

            Preferences.userNation = userNationCode
            Preferences.userId = userID
            Preferences.userPw = userPW
            Preferences.deviceUUID = deviceUUID

            val chanel = if (userNationCode == Common.SG) {
                Common.QDRIVE
            } else {
                Common.QDRIVE_V2
            }

            try {
                pingCheck()

                val result = RetrofitClient.instanceDynamic().requestServerLogin(
                    userID, userPW, chanel, deviceUUID,
                    latitude.toString(), longitude.toString(), userNationCode
                )

                progressBar.visibility = View.GONE

                if (result.resultCode != 0) {
                    // Login Failed
                    Preferences.userPw = ""
                    binding.editLoginPassword.setText("")

                    when {
                        result.resultCode == -10 && !BuildConfig.DEBUG -> {
                            showDialog(resources.getString(R.string.msg_account_deactivated))
                        }
                        result.resultMsg != "" -> {
                            showDialog(result.resultMsg)
                        }
                        else -> {
                            showDialog(resources.getString(R.string.msg_not_valid_info))
                        }
                    }
                } else {

                    val loginData =
                        Gson().fromJson(result.resultObject, LoginInfo::class.java)

                    val response = RetrofitClient.instanceDynamic().requestAppVersionCheck()

                    if (response.resultCode == -10 && !BuildConfig.DEBUG) {
                        val msg = java.lang.String.format(
                            resources.getString(R.string.msg_update_version),
                            getVersion(),
                            loginData.version
                        )
                        goGooglePlay(msg)

                    } else {
                        Preferences.userId = loginData.opId!!
                        Preferences.userPw = userPW
                        Preferences.deviceUUID = deviceUUID

                        if (!loginData.opNm.isNullOrEmpty()) {
                            Preferences.userName = loginData.opNm!!
                        }

                        if (!loginData.epEmail.isNullOrEmpty()) {
                            Preferences.userEmail = loginData.epEmail!!
                        }

                        if (!loginData.officeCode.isNullOrEmpty()) {
                            Preferences.officeCode = loginData.officeCode!!
                        }

                        if (!loginData.officeName.isNullOrEmpty()) {
                            Preferences.officeName = loginData.officeName!!
                        }

                        if (!loginData.pickupDriverYN.isNullOrEmpty()) {
                            Preferences.pickupDriver = loginData.pickupDriverYN!!
                        }

                        if (!loginData.shuttle_driver_yn.isNullOrEmpty()) {
                            Preferences.outletDriver = loginData.shuttle_driver_yn!!
                        }

                        if (!loginData.locker_driver_status.isNullOrEmpty()) {
                            Preferences.lockerStatus = loginData.locker_driver_status!!
                        }

                        if (!loginData.defaultYn.isNullOrEmpty()) {
                            Preferences.default = loginData.defaultYn!!
                        }

                        if (!loginData.authNo.isNullOrEmpty()) {
                            Preferences.authNo = loginData.authNo!!
                        }

                        if (loginData.smsYn == "Y" && loginData.deviceYn == "Y") {

                            FirebaseCrashlytics.getInstance().setCustomKey("ID", Preferences.userId)

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)

                            startActivity(intent)
                            finish()
                        } else {
                            goSMSVerification(resources.getString(R.string.msg_go_sms_verification))
                        }
                    }
                }

            } catch (e: Exception) {
                delay(1000)
                RetrofitClient.instanceDynamic().requestWriteLog(
                    "1", "LOGIN", "DNS error in RetrofitClient",
                    qoo10Result + daumResult + nowTime + teleInfo
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ }) {}
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUUID(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getVersion(): String {
        return try {
            applicationContext.packageManager
                .getPackageInfo(applicationContext.packageName, 0)
                .versionName
        } catch (e: Exception) {
            ""
        }
    }

    private fun showDialog(msg: String?) {
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

    private fun goGooglePlay(msg: String?) {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(
            resources.getString(R.string.button_ok)
        ) { dialog, _ ->
            val uri: Uri = Uri.parse("market://details?id=com.giosis.util.qdrive.singapore")
            val itt = Intent(Intent.ACTION_VIEW, uri)
            startActivity(itt)
            dialog.dismiss()
        }
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }


    private fun goSMSVerification(msg: String?) {
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

    private suspend fun getNationList() {
        try {
            val response = RetrofitClient.instanceDynamic().requestNationList()

            if (response.resultCode == 0) {
                nationList = Gson().fromJson(
                    response.resultObject,
                    object : TypeToken<ArrayList<LoginNation>>() {}.type
                )
            }

        } catch (e: java.lang.Exception) {
            Toast.makeText(
                this,
                resources.getText(R.string.msg_network_connect_error),
                Toast.LENGTH_SHORT
            ).show()
            Log.e(tag, e.toString())
        }
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

    private fun pingCheck() {

        CoroutineScope(Dispatchers.IO).launch {
            val result: String = try {
                val runTime = Runtime.getRuntime()
                val cmd = "ping -c 1 -W 10 qxapi.qxpress.net"

                val proc = runTime.exec(cmd)
                proc.waitFor()

                proc.exitValue().toString()
            } catch (e: Exception) {
                e.toString()
            }
            val returnString = when (result) {
                "0" -> "Ping Success"
                "1" -> "Ping Fail"
                "2" -> "Ping Error"
                else -> result
            }

            FirebaseCrashlytics.getInstance().setCustomKey(
                "PING qxapi",
                returnString
            )

            if (result != "0") {
                urlConnectionCheck()
                nowTimeCheck()
                telephonyInfo()

                FirebaseCrashlytics.getInstance().setCustomKey(
                    "ERROR INFO",
                    qoo10Result + daumResult + nowTime + teleInfo
                )
            }
        }
    }

    var qoo10Result = ""
    var daumResult = ""
    private fun urlConnectionCheck() {
        lifecycleScope.launch(Dispatchers.IO) {
            val url = URL("https://www.qoo10.com")
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            qoo10Result = try {
                "qoo10 url connection / ${urlConnection.responseCode}"
            } catch (e: java.lang.Exception) {
                "qoo10 url connection $e"
            }

            val url1 = URL("https://www.daum.net")
            val urlConnection1: HttpURLConnection = url1.openConnection() as HttpURLConnection
            daumResult = try {
                "daum url connection / ${urlConnection1.responseCode}"
            } catch (e: java.lang.Exception) {
                "daum url connection $e"
            }
        }
    }

    var nowTime = ""
    private fun nowTimeCheck() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val regDataString = dateFormat.format(Date())

        nowTime = " / now Time : $regDataString"
    }

    var teleInfo = ""
    private fun telephonyInfo() {
        val tm =
            MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        teleInfo = "TelephonyManager" + tm.simOperatorName
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}