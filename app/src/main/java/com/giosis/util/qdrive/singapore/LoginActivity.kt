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
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.main.MainActivity
import com.giosis.library.main.SMSVerificationActivity
import com.giosis.library.server.RetrofitClient
import com.giosis.library.setting.DeveloperModeActivity
import com.giosis.library.util.DatabaseHelper
import com.giosis.library.util.PermissionActivity
import com.giosis.library.util.PermissionChecker
import com.giosis.util.qdrive.singapore.databinding.ActivityLoginBinding
import com.giosis.util.qdrive.util.CommonActivity
import com.giosis.util.qdrive.util.DataUtil
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

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DatabaseHelper.getInstance()            // DB 생성

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

        progressBar.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.layoutLogin.addView(progressBar)
        progressBar.visibility = View.GONE

        binding.editLoginId.setText(MyApplication.preferences.userId)
        binding.editLoginPassword.setText(MyApplication.preferences.userPw)
        Log.e(tag, "  init Data  -   ${MyApplication.preferences.userNation}  ${MyApplication.preferences.userId}  ${MyApplication.preferences.userPw}")
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

                    MyApplication.preferences.userNation = "SG"
                    MyApplication.preferences.userId = userID
                    MyApplication.preferences.userPw = userPW
                    MyApplication.preferences.deviceUUID = deviceUUID
                    MyApplication.preferences.appVersion = appVersion

                    progressBar.visibility = View.VISIBLE

                    RetrofitClient.instanceDynamic().requestServerLogin(
                            userID, userPW, "QDRIVE", "", deviceUUID, "",
                            latitude.toString(), longitude.toString(), DataUtil.appID, "SG")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({

                                progressBar.visibility = View.GONE

                                if (it.resultObject != null && it.resultCode == 0) {
                                    val loginData = Gson().fromJson(it.resultObject, LoginInfo::class.java)
                                    Log.e(RetrofitClient.TAG, "response : ${it.resultObject}")

                                    if (loginData != null) {

                                        if (!loginData.version.isNullOrEmpty()) {
//                                            //TODO_
//                                            val compare =  Version("1.10.0") < Version("1.9.0")
//                                            val compare1 = Version("1.10.9") < Version("1.10.10")
//                                            Log.e("krm0219", "Compare $compare  $compare1")

                                            if (MyApplication.preferences.appVersion < loginData.version!!) {

                                                val msg = java.lang.String.format(resources.getString(R.string.msg_update_version),
                                                        loginData.version!!, MyApplication.preferences.appVersion)
                                                goGooglePlay(msg)
                                            } else {

                                                MyApplication.preferences.userId = loginData.opId!!
                                                MyApplication.preferences.userPw = userPW
                                                MyApplication.preferences.deviceUUID = deviceUUID

                                                if (!loginData.version.isNullOrEmpty()) {
                                                    MyApplication.preferences.appVersion = loginData.version!!
                                                }

                                                if (!loginData.opNm.isNullOrEmpty()) {
                                                    MyApplication.preferences.userName = loginData.opNm!!
                                                } else {
                                                    MyApplication.preferences.userName = ""
                                                }

                                                if (!loginData.epEmail.isNullOrEmpty()) {
                                                    MyApplication.preferences.userEmail = loginData.epEmail!!
                                                } else {
                                                    MyApplication.preferences.userEmail = ""
                                                }

                                                if (!loginData.officeCode.isNullOrEmpty()) {
                                                    MyApplication.preferences.officeCode = loginData.officeCode!!
                                                } else {
                                                    MyApplication.preferences.officeCode = ""
                                                }

                                                if (!loginData.officeName.isNullOrEmpty()) {
                                                    MyApplication.preferences.officeName = loginData.officeName!!
                                                } else {
                                                    MyApplication.preferences.officeName = ""
                                                }

                                                if (!loginData.pickupDriverYN.isNullOrEmpty()) {
                                                    MyApplication.preferences.pickupDriver = loginData.pickupDriverYN!!
                                                } else {
                                                    MyApplication.preferences.pickupDriver = "N"
                                                }

                                                if (!loginData.shuttle_driver_yn.isNullOrEmpty()) {
                                                    MyApplication.preferences.outletDriver = loginData.shuttle_driver_yn!!
                                                } else {
                                                    MyApplication.preferences.outletDriver = ""
                                                }

                                                if (!loginData.locker_driver_status.isNullOrEmpty()) {
                                                    MyApplication.preferences.lockerStatus = loginData.locker_driver_status!!
                                                } else {
                                                    MyApplication.preferences.lockerStatus = ""
                                                }

                                                if (!loginData.defaultYn.isNullOrEmpty()) {
                                                    MyApplication.preferences.default = loginData.defaultYn!!
                                                } else {
                                                    MyApplication.preferences.default = ""
                                                }

                                                if (!loginData.authNo.isNullOrEmpty()) {
                                                    MyApplication.preferences.authNo = loginData.authNo!!
                                                } else {
                                                    MyApplication.preferences.authNo = ""
                                                }

                                                if (loginData.smsYn == "Y" && loginData.deviceYn == "Y") {

                                                    FirebaseCrashlytics.getInstance().setCustomKey("ID", MyApplication.preferences.userId)

                                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                                    startActivity(intent)
                                                    finish()

                                                } else {
                                                    if (loginData.deviceYn == "N") {
                                                        showDialog(resources.getString(R.string.msg_go_sms_verification))
                                                    }

                                                    val intent = Intent(this@LoginActivity, SMSVerificationActivity::class.java)
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
                                Log.e(RetrofitClient.TAG, it.message.toString())
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

    inner class Version(private val value: String) : Comparable<Version> {
        private val splitted by lazy { value.split("-").first().split(".").map { it.toIntOrNull() ?: 0 } }

        override fun compareTo(other: Version): Int {
            for (i in 0 until maxOf(splitted.size, other.splitted.size)) {
                val compare = splitted.getOrElse(i) { 0 }.compareTo(other.splitted.getOrElse(i) { 0 })
                if (compare != 0)
                    return compare
            }
            return 0
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        var info = ""
        if (MyApplication.preferences.serverURL.contains("test")) {
            info = "test / "
        } else if (MyApplication.preferences.serverURL.contains("staging")) {
            info = "staging / "
        }

        binding.textLoginVersion.text = "$info${resources.getString(R.string.text_app_version)} - $appVersion"


        if (isPermissionTrue) {

            val gpsEnable = gpsTrackerManager?.enableGPSSetting()

            if (gpsEnable == true && gpsTrackerManager != null) {

                gpsTrackerManager!!.GPSTrackerStart()
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
            val packageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
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
            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)
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

        if (gpsTrackerManager != null) {

            gpsTrackerManager!!.stopFusedProviderService()
        }
    }

    private fun goGooglePlay(msg: String?) {

        val alertBuilder = android.app.AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)
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