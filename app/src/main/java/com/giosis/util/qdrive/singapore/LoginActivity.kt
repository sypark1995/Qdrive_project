package com.giosis.util.qdrive.singapore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.giosis.library.util.Preferences
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.giosis.library.data.LoginInfo
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.main.MainActivity
import com.giosis.library.main.SMSVerificationActivity
import com.giosis.library.server.RetrofitClient
import com.giosis.library.setting.DeveloperModeActivity
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.PermissionActivity
import com.giosis.library.util.PermissionChecker
import com.giosis.util.qdrive.singapore.databinding.ActivityLoginBinding
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File


class LoginActivity : CommonActivity() {

    val tag = "LoginActivity"

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val progressBar by lazy {
        ProgressBar(this@LoginActivity)
    }

    private lateinit var appVersion: String

    // Location
    private val gpsTrackerManager: GPSTrackerManager? by lazy {
        GPSTrackerManager(this@LoginActivity)
    }
    var gpsEnable = false

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(
        PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
        PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
    )

    var showDeveloperModeClickCount = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DatabaseHelper.getInstance()

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

        binding.editLoginId.setText(Preferences.userId)
        binding.editLoginPassword.setText(Preferences.userPw)

        appVersion = getVersion()

        // Login

        binding.btnLoginSign.setOnClickListener {

            hideKeyboard()

            val userID = binding.editLoginId.text.toString().trim()
            val userPW = binding.editLoginPassword.text.toString().trim()
            val deviceUUID = getDeviceUUID()
            Log.e(tag, " Input Data  -  SG  / $userID  / $userPW  / $deviceUUID")

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

                    Preferences.userNation = "SG"
                    Preferences.userId = userID
                    Preferences.userPw = userPW
                    Preferences.deviceUUID = deviceUUID
                    Preferences.appVersion = appVersion

                    progressBar.visibility = View.VISIBLE

                    RetrofitClient.instanceDynamic().requestServerLogin(
                        userID, userPW, "QDRIVE", "", deviceUUID, "",
                        latitude.toString(), longitude.toString(), "QDRIVE", "SG"
                    ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                            progressBar.visibility = View.GONE

                            if (it.resultObject != null && it.resultCode == 0) {
                                val loginData =
                                    Gson().fromJson(it.resultObject, LoginInfo::class.java)

                                if (loginData != null) {

                                    if (!loginData.version.isNullOrEmpty()) {

                                        if (Preferences.appVersion < loginData.version!!) {

                                            val msg = java.lang.String.format(
                                                resources.getString(R.string.msg_update_version),
                                                loginData.version!!,
                                                Preferences.appVersion
                                            )
                                            goGooglePlay(msg)

                                        } else {

                                            Preferences.userId = loginData.opId!!
                                            Preferences.userPw = userPW
                                            Preferences.deviceUUID = deviceUUID

                                            if (!loginData.version.isNullOrEmpty()) {
                                                Preferences.appVersion = loginData.version!!
                                            }

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

                                                FirebaseCrashlytics.getInstance().setCustomKey(
                                                    "ID",
                                                    Preferences.userId
                                                )

                                                val intent = Intent(
                                                    this@LoginActivity,
                                                    MainActivity::class.java
                                                )
                                                startActivity(intent)
                                                finish()

                                            } else {
                                                if (loginData.deviceYn == "N") {
                                                    showDialog(resources.getString(R.string.msg_go_sms_verification))
                                                }

                                                val intent = Intent(
                                                    this@LoginActivity,
                                                    SMSVerificationActivity::class.java
                                                )
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                    }
                                }
                            } else {
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

                                binding.editLoginPassword.setText("")
                            }
                        }, {
                            Log.e(RetrofitClient.errorTag, it.toString())
                            progressBar.visibility = View.GONE
                            showDialog(it.message.toString())
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

    @SuppressLint("SetTextI18n")
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

            gpsTrackerManager?.let {
                gpsEnable = it.enableGPSSetting()
            }

            if (gpsEnable && gpsTrackerManager != null) {
                gpsTrackerManager!!.gpsTrackerStart()

            } else {
                if (!this@LoginActivity.isFinishing) {
                    AlertDialog.Builder(this@LoginActivity)
                        .setCancelable(false)
                        .setTitle(resources.getString(R.string.text_location_setting))
                        .setMessage(resources.getString(R.string.msg_location_off))
                        .setPositiveButton(resources.getString(R.string.button_ok)) { _, _ ->
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


    private fun showDialog(msg: String?) {

        if (!this@LoginActivity.isFinishing) {

            val alertBuilder = AlertDialog.Builder(this@LoginActivity)
            alertBuilder.setTitle(resources.getString(R.string.text_alert))
            alertBuilder.setMessage(msg)
            alertBuilder.setPositiveButton(
                resources.getString(R.string.button_ok)
            ) { dialog, _ -> dialog.dismiss() }

            val alertDialog = alertBuilder.create()
            alertDialog.show()
        }
    }

    private fun hideKeyboard() {

        val view = this.currentFocus

        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        gpsTrackerManager?.stopFusedProviderService()
    }

    private fun goGooglePlay(msg: String?) {

        val alertBuilder = android.app.AlertDialog.Builder(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

            isPermissionTrue = true
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}