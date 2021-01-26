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
import com.giosis.util.qdrive.server.data.LoginInfo
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.PermissionActivity
import com.giosis.util.qdrive.util.PermissionChecker
import com.giosis.util.qdrive.util.ui.CommonActivity
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File

class LoginActivity : CommonActivity() {

    val tag = "LoginActivity"
    private val progressBar by lazy {
        ProgressBar(this@LoginActivity)
    }

    //    private var spinnerList = ArrayList<LoginNation>()
    private var spinnerPosition = 0
    private lateinit var appVersion: String

    // Location
    private val gpsTrackerManager: GPSTrackerManager by lazy {
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
        setContentView(R.layout.activity_login)
        DatabaseHelper.getInstance()            // DB 생성

        var showDeveloperModeClickCount = 0

        img_login_bottom_logo.setOnClickListener {

            if (showDeveloperModeClickCount == 10) {
                showDeveloperModeClickCount = 0

                val intent = Intent(this, DeveloperModeActivity::class.java)
                startActivity(intent)
            } else {
                showDeveloperModeClickCount++
            }
        }

        progressBar.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout_login.addView(progressBar)
        progressBar.visibility = View.GONE

        edit_login_id.setText(MyApplication.preferences.userId)
        edit_login_password.setText(MyApplication.preferences.userPw)
        Log.e(tag, "  init Data  -   ${MyApplication.preferences.userNation}  ${MyApplication.preferences.userId}  ${MyApplication.preferences.userPw}")
        appVersion = getVersion()

        // Login
        btn_login_sign.setOnClickListener {

            hideKeyboard()

            val userID = edit_login_id.text.toString().trim()
            val userPW = edit_login_password.text.toString().trim()
            val deviceUUID = getDeviceUUID()
            Log.e(tag, " Input Data  -  SG  / $userID  / $userPW  / $deviceUUID")

            // DB 파일 생성여부
            val dbFile = File(DatabaseHelper.getInstance().dbPath)

            // 위치 정보
            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager.let {
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
                            latitude.toString(), longitude.toString(), "QDRIVE", "SG")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({

                                progressBar.visibility = View.GONE
                                Log.e("krm0219", "result  ${it.resultCode}")

                                if (it.resultObject != null && it.resultCode == 0) {
                                    val loginData = Gson().fromJson(it.resultObject, LoginInfo::class.java)
                                    Log.e(RetrofitClient.TAG, "response : ${it.resultObject}")

                                    if (loginData != null) {

                                        if (!loginData.version.isNullOrEmpty()) {
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
                                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                                    startActivity(intent)
                                                    finish()

                                                } else {
                                                    if (loginData.deviceYn == "N") {
                                                        showDialog("You have attempted to login from unauthorized mobile phone.\n" +
                                                                "If your mobile phone was changed, you have to pass the SMS verification.")
                                                    }

                                                    val intent = Intent(this@LoginActivity, SMSVerificationActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }

                                            }
                                        }
                                    }

                                } else {
                                    if (it.resultCode == -10) {

                                        showDialog("You Qdrive account has been deactivated")
                                    } else if (it.resultMsg != "") {

                                        showDialog(it.resultMsg)
                                    } else {

                                        showDialog("Sorry, your sign-in information is not valid. Please try again with your correct ID and password.")
                                    }

                                    edit_login_password.setText("")
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


    override fun onResume() {
        super.onResume()

        var info = ""
        if (MyApplication.preferences.serverURL.contains("test")) {
            info = "test / "
        } else if (MyApplication.preferences.serverURL.contains("staging")) {
            info = "staging / "
        }
        text_login_version.text = "$info${resources.getString(R.string.text_app_version)} - $appVersion"


        if (isPermissionTrue) {

            val gpsEnable = gpsTrackerManager.enableGPSSetting()

            if (gpsEnable) {
                gpsTrackerManager.GPSTrackerStart()
            } else {
                DataUtil.enableLocationSettings(this, this@LoginActivity)
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
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)
        ) { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertBuilder.create()
        alertDialog.show()
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
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

    fun goGooglePlay(msg: String?) {

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
            Log.e("permission", "$tag   Permission granted")
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}