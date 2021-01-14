package com.giosis.util.qdrive.international

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
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
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.setting.DeveloperModeActivity
import com.giosis.library.util.DatabaseHelper
import com.giosis.util.qdrive.main.GetRestDaysAsyncTask
import com.giosis.util.qdrive.main.MainActivity
import com.giosis.util.qdrive.util.*
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * @author krm0219
 * Login (Kotlin)
 * */
class LoginActivity : CommonActivity() {

    val tag = "LoginActivity"
    val context: Context = MyApplication.getContext()
    private val progressBar = ProgressBar(context)

    private var spinnerList = ArrayList<LoginNation>()
    private var spinnerPosition = 0
    private lateinit var appVersion: String

    // Location
    private val gpsTrackerManager: GPSTrackerManager? = GPSTrackerManager(context)

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Image Shake Animation
        img_login_top_bg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation))

        img_login_top_logo.setOnClickListener {
            img_login_top_bg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation))
        }


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


        // Nation
        spinnerList.add(LoginNation(context.resources.getString(R.string.text_malaysia), "MY", "login_icon_my"))
        spinnerList.add(LoginNation(context.resources.getString(R.string.text_Indonesia), "ID", "login_icon_id"))
        spinner_select_nation.adapter = LoginSpinnerAdapter(this, spinnerList)

        when (MyApplication.preferences.userNation) {
            "MY" -> {

                spinner_select_nation.setSelection(0)
            }
            "ID" -> {

                spinner_select_nation.setSelection(1)
            }
            else -> {
                // TEST
                spinner_select_nation.setSelection(0)
            }
        }


        layout_login_select_nation.setOnClickListener {

            spinner_select_nation.performClick()
        }

        spinner_select_nation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                parent?.let {

                    hideKeyboard()

                    spinnerPosition = position

                    val resourceId = context.resources.getIdentifier(spinnerList[position].nationImg, "drawable", context.packageName)
                    img_login_nation.setBackgroundResource(resourceId)
                    text_login_nation.text = spinnerList[position].nation
                    Log.e(tag, " Select Nation : ${text_login_nation.text}")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }


        //
        edit_login_id.setText(MyApplication.preferences.userId)
        edit_login_password.setText(MyApplication.preferences.userPw)
        Log.e(tag, "  init Data  -   ${MyApplication.preferences.userNation}  ${MyApplication.preferences.userId}  ${MyApplication.preferences.userPw}")
        appVersion = getVersion()


        // Login
        btn_login_sign.setOnClickListener {

            hideKeyboard()

            var userNationCode = spinnerList[spinnerPosition].nationCode
            /* // TEST
             userNationCode = "SG"*/
            val userID = edit_login_id.text.toString().trim()
            val userPW = edit_login_password.text.toString().trim()
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

                    showDialog(context.resources.getString(R.string.msg_please_input_id))
                    return@setOnClickListener
                }
                userPW.isEmpty() -> {

                    showDialog(context.resources.getString(R.string.msg_please_input_password))
                    return@setOnClickListener
                }
                !dbFile.exists() -> {

                    showDialog(context.resources.getString(R.string.msg_db_problem))
                }
                else -> {

                    MyApplication.preferences.userNation = userNationCode
                    MyApplication.preferences.userId = userID
                    MyApplication.preferences.userPw = userPW
                    MyApplication.preferences.deviceUUID = deviceUUID
                    MyApplication.preferences.appVersion = appVersion

                    progressBar.visibility = View.VISIBLE
                    val loginAsyncTask = LoginAsyncTask(latitude.toString(), longitude.toString())
                    loginAsyncTask.execute()
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
        text_login_version.text = "$info${context.resources.getString(R.string.text_app_version)} - $appVersion"


        if (isPermissionTrue) {

            val gpsEnable = gpsTrackerManager?.enableGPSSetting()

            if (gpsEnable == true) {

                gpsTrackerManager?.GPSTrackerStart()
                Log.e(tag, " onResume  Location  :  ${gpsTrackerManager?.latitude} / ${gpsTrackerManager?.longitude}")
            } else {

                DataUtil.enableLocationSettings(this, context)
            }
        }
    }


    @SuppressLint("HardwareIds")
    private fun getDeviceUUID(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getVersion(): String {

        return try {

            val packageInfo = context.applicationContext.packageManager.getPackageInfo(context.applicationContext.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {

            ""
        }
    }


    fun showDialog(msg: String?) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(context.resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)
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


    // NOTIFICATION.  Login API
    @SuppressLint("StaticFieldLeak")
    inner class LoginAsyncTask(val latitude: String, val longitude: String) : AsyncTask<Void, Void, LoginResult>() {

        override fun doInBackground(vararg p0: Void?): LoginResult {

            val result = LoginResult()

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.resultCode = "-16"
                result.resultMsg = context.resources.getString(R.string.msg_network_connect_error_saved)
                return result
            }

            try {

                val job = JSONObject()
                job.accumulate("login_id", MyApplication.preferences.userId)
                job.accumulate("password", MyApplication.preferences.userPw)
                job.accumulate("chanel", "QDRIVE_V2")
                job.accumulate("referer", MyApplication.preferences.deviceUUID)
                job.accumulate("ip", "")
                job.accumulate("vehicle", "")
                job.accumulate("latitude", latitude)
                job.accumulate("longitude", longitude)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", MyApplication.preferences.userNation)

                Log.e(tag, "SERVER  UPLOAD  DATA : ${MyApplication.preferences.userId} / ${MyApplication.preferences.userPw} / " +
                        "$latitude / $longitude / ${MyApplication.preferences.deviceUUID} / ${DataUtil.appID} / ${MyApplication.preferences.userNation}")


                val methodName = "LoginQDRIVE"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                // {"ResultObject":{"OpNo":"5044","OpId":"karam.kim","OpNm":"k driver name 1","OpType":"DR","OfficeCode":"0000","OfficeName":"Qxpress SG West","AgentCode":"karam.kim","AdminAuthYn":"Y","DsmAuthYn":"Y","CustNo":"100000194","Email":"qfs@qxpress.sg","DefaultYn":"N","QLPSCustNo":null,"GroupNo":"42","AuthNo":"68 , 69 , 70 , 71 , 74 , 80 , 85 , 93 , 96 , 97 , 98 , 99 , 101 , 102 , 104 , 105 , 106 , 107 , 108 , 111 , 112 , 114 , 115 , 116 , 117 , 118 , 119 , 121 , 122 , 123 , 133 , 134 , 135 , 136 , 137 , 138 , 139 , 140 , 141 , 142 , 143 , 148 , 153 , 154 , 156 , 157 , 158 , 159 , 161 , 164 , 165 , 166 , 171 , 172 , 173 , 174 , 176 , 177 , 181 , 186 , 190 , 193 , 204 , 219 , 221 , 222","Version":"1.0.0","SmsYn":"Y","DeviceYn":"Y","EpEmail":"kim@abc.com","PickupDriverYN":"Y","shuttle_driver_yn":"N","locker_driver_status":"","country_code":"SG"},"ResultCode":0,"ResultMsg":""}

                val jsonObject = JSONObject(jsonString)
                result.resultCode = jsonObject.getString("ResultCode")
                result.resultMsg = jsonObject.getString("ResultMsg")

                if (result.resultCode == "0") {

                    val resultObject = jsonObject.getJSONObject("ResultObject")
                    val data = LoginResult.LoginData()

                    data.userId = resultObject.getString("OpId")
                    data.userName = resultObject.getString("OpNm")
                    data.officeCode = resultObject.getString("OfficeCode")
                    data.officeName = resultObject.getString("OfficeName")
                    data.userEmail = resultObject.getString("EpEmail")
                    data.serverVersion = resultObject.getString("Version")
                    data.pickupDriver = resultObject.getString("PickupDriverYN")
                    data.outletDriver = resultObject.getString("shuttle_driver_yn")
                    data.lockerStatus = resultObject.getString("locker_driver_status")
                    data.smsYn = resultObject.getString("SmsYn")
                    data.deviceYn = resultObject.getString("DeviceYn")
                    data.defaultYn = resultObject.getString("DefaultYn")
                    data.authNo = resultObject.getString("AuthNo")
                    data.nationCode = resultObject.getString("country_code")

                    result.resultObject = data
                }
            } catch (e: Exception) {

                Log.e("krm0219", "Exception : $e")
                result.resultCode = "-15"
                result.resultMsg = java.lang.String.format(context.resources.getString(R.string.text_exception), e.toString())
            }

            return result
        }

        override fun onPostExecute(result: LoginResult) {
            super.onPostExecute(result)

            if (result.resultCode != "0") {
                // NOTIFICATION.  Login Fail

                progressBar.visibility = View.GONE
                MyApplication.preferences.userPw = ""
                edit_login_password.setText("")

                when {
                    result.resultCode == "-10" -> {

                        showDialog(context.resources.getString(R.string.msg_account_deactivated))
                    }
                    result.resultMsg != "" -> {

                        showDialog(result.resultMsg)
                    }
                    else -> {

                        showDialog(context.resources.getString(R.string.msg_not_valid_info))
                    }
                }
            } else {
                // NOTIFICATION.  Login Success

                Log.e(tag, " Login Version -  ${MyApplication.preferences.appVersion} < ${result.resultObject.serverVersion}")

                if (MyApplication.preferences.appVersion < result.resultObject.serverVersion) {

                    progressBar.visibility = View.GONE
                    val msg = java.lang.String.format(context.resources.getString(R.string.msg_update_version),
                            result.resultObject.serverVersion, MyApplication.preferences.appVersion)
                    goGooglePlay(msg)
                } else {

                    DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_REST_DAYS, "")

                    GetRestDaysAsyncTask(MyApplication.preferences.userNation, Calendar.getInstance().get(Calendar.YEAR), object : onEventListner {
                        override fun onSuccess() {

                            GetRestDaysAsyncTask(MyApplication.preferences.userNation, Calendar.getInstance().get(Calendar.YEAR) + 1, object : onEventListner {
                                override fun onSuccess() {
                                    Log.e(tag, "Rest Day   DB insert finish")

                                    progressBar.visibility = View.GONE
                                    DataUtil.nationCode = MyApplication.preferences.userNation

                                    MyApplication.preferences.userId = result.resultObject.userId
                                    MyApplication.preferences.userName = result.resultObject.userName
                                    MyApplication.preferences.userEmail = result.resultObject.userEmail
                                    MyApplication.preferences.officeCode = result.resultObject.officeCode
                                    MyApplication.preferences.officeName = result.resultObject.officeName
                                    MyApplication.preferences.pickupDriver = result.resultObject.pickupDriver
                                    MyApplication.preferences.outletDriver = result.resultObject.outletDriver
                                    MyApplication.preferences.lockerStatus = result.resultObject.lockerStatus
                                    MyApplication.preferences.default = result.resultObject.defaultYn
                                    MyApplication.preferences.authNo = result.resultObject.authNo

                                    Log.e(tag, "SERVER  DOWNLOAD  DATA : ${result.resultObject.officeCode} / ${result.resultObject.officeName} / " +
                                            "${result.resultObject.pickupDriver} / ${result.resultObject.outletDriver} / ${result.resultObject.lockerStatus} / " +
                                            "${result.resultObject.defaultYn} / ${result.resultObject.authNo}")


                                    Log.e(tag, "  SMS / Device Auth - ${result.resultObject.smsYn}, ${result.resultObject.deviceYn}")
                                    // Notification.  GO Main
                                    if (result.resultObject.smsYn == "Y" && result.resultObject.deviceYn == "Y") {

                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {

                                        goSMSVerification(context.resources.getString(R.string.msg_go_sms_verification))
                                    }
                                }

                                override fun onFailure() {

                                    showDialog("Rest Day API Error. ${context.resources.getString(R.string.msg_please_try_again)}")
                                }
                            }).execute()
                        }

                        override fun onFailure() {

                            showDialog("Rest Day API Error. ${context.resources.getString(R.string.msg_please_try_again)}")
                        }
                    }).execute()
                }
            }
        }
    }


    fun goGooglePlay(msg: String?) {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(context.resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)
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
        alertBuilder.setTitle(context.resources.getString(R.string.text_alert))
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)
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
        DataUtil.stopGPSManager(gpsTrackerManager)
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