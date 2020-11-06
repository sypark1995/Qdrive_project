package com.giosis.library.setting

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giosis.library.R
import com.giosis.library.server.APIModel
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.DisplayUtil
import kotlinx.android.synthetic.main.activity_change_pwd.*
import kotlinx.android.synthetic.main.top_title.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


class ChangePasswordActivity : AppCompatActivity() {

    val tag = "ChangePasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pwd)

        text_top_title.text = resources.getString(R.string.text_title_change_password)

        layout_top_back.setOnClickListener {
            finish()
        }

        btn_setting_change_confirm.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        DisplayUtil.hideKeyboard(this)

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_title_change_password))
        alertBuilder.setMessage(resources.getString(R.string.msg_want_change_password))
        alertBuilder.setCancelable(true)

        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

            dialogInterface.cancel()

            val oldPassword = edit_setting_change_old_password.text.toString().trim()
            val newPassword = edit_setting_change_new_password.text.toString().trim()
            val confirmPassword = edit_setting_change_confirm_password.text.toString().trim()

            val isValid = isValidPassword(oldPassword, newPassword, confirmPassword)

            if (isValid) {

                val userAgent = ""
                val id = ""
                val appID = ""
                val nationCode = ""

                RetrofitClient.instanceDynamic(userAgent).requestChangePwd(
                        id, oldPassword, newPassword, appID, nationCode
                ).enqueue(object : Callback<APIModel> {

                    override fun onFailure(call: Call<APIModel>, t: Throwable) {
//                        progressBar.visibility = View.GONE

                    }

                    override fun onResponse(call: Call<APIModel>, response: Response<APIModel>) {

                        if (response.isSuccessful) {
                            if (response.body() != null && response.body()!!.resultCode == 0) {
//                                val loginData = Gson().fromJson(response.body()!!.resultObject, LoginInfo::class.java)
// TODO kjyoo
                            }
                        }

//                        progressBar.visibility = View.GONE
                    }
                })
            }
        }

        alertBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
            dialogInterface.cancel()
        }

        alertBuilder.show()
    }

    private fun isValidPassword(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        var isValid = false

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.text_invalidation))

        if (oldPassword.isNotEmpty()) {      // 현재 패스워드 입력

            if (11 <= newPassword.length) {     // 새로운 패스워드 11자리 이상 입력

                val passwordPattern = "((?=.*\\d)(?=.*[A-Za-z])(?=.*[!@#$%]).{11,20})"
                val pattern = Pattern.compile(passwordPattern)
                val matcher = pattern.matcher(newPassword)
                val patternValid = matcher.matches()

                if (patternValid) {  // 비밀번호 유효성

                    if (newPassword == confirmPassword) {    // 확인 비밀번호 일치
                        isValid = true

                    } else {        // 확인 비밀번호 불일치

                        alertBuilder.setMessage(resources.getString(R.string.msg_same_password_error))
                        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                            edit_setting_change_confirm_password.requestFocus()
                            dialogInterface.cancel()
                        }

                        alertBuilder.show()
                    }
                } else {             // 비밀번호 유효성 틀림

                    alertBuilder.setMessage(resources.getString(R.string.msg_password_symbols_error))
                    alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                        edit_setting_change_new_password.requestFocus()
                        dialogInterface.cancel()
                    }

                    alertBuilder.show()
                }
            } else {        // 새로운 패스워드 11자리 이상 입력하지 않음

                alertBuilder.setMessage(resources.getString(R.string.msg_password_length_error))
                alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                    edit_setting_change_new_password.requestFocus()
                    dialogInterface.cancel()
                }

                alertBuilder.show()
            }
        } else {    // 현재 패스워드 입력하지 않음

            alertBuilder.setMessage(resources.getString(R.string.msg_empty_password_error))
            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                edit_setting_change_old_password.requestFocus()
                dialogInterface.cancel()
            }

            alertBuilder.show()
        }

        return isValid
    }

//    @SuppressLint("StaticFieldLeak")
//    inner class ChangePasswordAsyncTask(private val oldPassword: String, private val newPassword: String) : AsyncTask<Void, Void, StdResult>() {
//
//        override fun doInBackground(vararg p0: Void?): StdResult {
//
//            val result = StdResult()
//
//            if (!NetworkUtil.isNetworkAvailable(context)) {
//
//                result.resultCode = -16
//                result.resultMsg = context.resources.getString(R.string.msg_network_connect_error_saved)
//
//                return result
//            }
//
//            try {
//
//                val job = JSONObject()
//                job.accumulate("op_id", userId)
//                job.accumulate("old_pwd", oldPassword)
//                job.accumulate("new_pwd", newPassword)
//                job.accumulate("app_id", DataUtil.appID)
//                job.accumulate("nation_cd", DataUtil.nationCode)
//
//                Log.e("Server", "$tag  DATA : $userId / $oldPassword / $newPassword")
//
//
//                val methodName = "changePassword"
//                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(ManualHelper.MOBILE_SERVER_URL, methodName, job)
//                // {"ResultCode":0,"ResultMsg":"Change Password job is completed successfully"}
//                // {"ResultCode":-31,"ResultMsg":"Change Password job is failed."}
//
//                val jsonObject = JSONObject(jsonString)
//                val resultCode = jsonObject.getInt("ResultCode")
//                val resultMsg = jsonObject.getString("ResultMsg")
//                result.resultCode = resultCode
//                result.resultMsg = resultMsg
//            } catch (e: Exception) {
//
//                Log.e("krm0219", "Exception : $e")
//                result.resultCode = -15
//                result.resultMsg = java.lang.String.format(context.resources.getString(R.string.text_exception), e.toString())
//            }
//
//            return result
//        }
//
//        override fun onPostExecute(result: StdResult) {
//            super.onPostExecute(result)
//
//            val resultCode = result.resultCode
//            val resultMsg = result.resultMsg
//
//
//            val alertBuilder = AlertDialog.Builder(this@ChangePasswordActivity)
//            alertBuilder.setTitle(context.resources.getString(R.string.text_alert))
//            alertBuilder.setMessage(resultMsg)
//            alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialogInterface, _ ->
//
//                if (resultCode == 0) {   // 성공
//
//                    dialogInterface.cancel()
//                    finish()
//                } else {                // 실패
//
//                    dialogInterface.cancel()
//                }
//            }
//
//            alertBuilder.show()
//        }
//    }
}