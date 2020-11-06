package com.giosis.util.qdrive.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.giosis.util.qdrive.barcodescanner.ManualHelper
import com.giosis.util.qdrive.barcodescanner.StdResult
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.util.*
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_modify_user_info.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONObject

class ModifyUserInfoActivity : CommonActivity() {

    val tag = "ModifyUserInfoActivity"
    val context: Context = MyApplication.getContext()
    private val userId = MyApplication.preferences.userId


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_user_info)


        text_top_title.text = context.resources.getString(R.string.text_title_my_info)

        layout_top_back.setOnClickListener {

            finish()
        }


        btn_setting_change_confirm.setOnClickListener {

            modifyUserInfo()
        }
    }

    override fun onResume() {
        super.onResume()

        text_setting_change_id.text = userId
        edit_setting_change_name.setText(MyApplication.preferences.userName)
        edit_setting_change_email.setText(MyApplication.preferences.userEmail)
    }


    private fun modifyUserInfo() {

        DisplayUtil.hideKeyboard(this)

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(context.resources.getString(R.string.text_modify_my_info))
        alertBuilder.setMessage(context.resources.getString(R.string.msg_want_change_info))
        alertBuilder.setCancelable(true)

        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialogInterface, _ ->

            dialogInterface.cancel()

            val name = edit_setting_change_name.text.toString()
            val email = edit_setting_change_email.text.toString()

            // TODO  email 형식 체크
            //    Log.e(tag, "Success  / $name / $email")

            val modifyInfoAsyncTask = ModifyInfoAsyncTask(userId, name, email)
            modifyInfoAsyncTask.execute()
        }

        alertBuilder.setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialogInterface, _ ->

            Log.e(tag, "Cancel")

            dialogInterface.cancel()
        }

        alertBuilder.show()
    }

    @SuppressLint("StaticFieldLeak")
    inner class ModifyInfoAsyncTask(private val userId: String, private val userName: String, private val userEmail: String) : AsyncTask<Void, Void, StdResult>() {

        override fun doInBackground(vararg p0: Void?): StdResult {

            val result = StdResult()

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.resultCode = -16
                result.resultMsg = context.resources.getString(R.string.msg_network_connect_error_saved)

                return result
            }

            try {

                val job = JSONObject()
                job.accumulate("op_id", userId)
                job.accumulate("name", userName)
                job.accumulate("email", userEmail)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", DataUtil.nationCode)

                Log.e("Server", "$tag  DATA : $userId / $userName / $userEmail")


                val methodName = "changeMyInfo"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(ManualHelper.MOBILE_SERVER_URL, methodName, job)
                // {"ResultCode":0,"ResultMsg":"Modification job is completed successfully."}
                // {"ResultCode":-5,"ResultMsg":"This email is already registered by another Qsign ID."}

                val jsonObject = JSONObject(jsonString)
                val resultCode = jsonObject.getInt("ResultCode")
                val resultMsg = jsonObject.getString("ResultMsg")
                result.resultCode = resultCode
                result.resultMsg = resultMsg
            } catch (e: Exception) {

                Log.e("krm0219", "Exception : $e")
                result.resultCode = -15
                result.resultMsg = java.lang.String.format(context.resources.getString(R.string.text_exception), e.toString())
            }

            return result
        }

        @SuppressLint("CommitPrefEdits")
        override fun onPostExecute(result: StdResult) {
            super.onPostExecute(result)


            val resultCode = result.resultCode
            val resultMsg = result.resultMsg


            val alertBuilder = AlertDialog.Builder(this@ModifyUserInfoActivity)
            alertBuilder.setTitle(context.resources.getString(R.string.text_alert))
            alertBuilder.setMessage(resultMsg)
            alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                if (resultCode == 0) {   // 성공

                    MyApplication.preferences.userName = userName
                    MyApplication.preferences.userEmail = userEmail


                    dialogInterface.cancel()
                    finish()
                } else {                // 실패

                    dialogInterface.cancel()
                }
            }

            alertBuilder.show()
        }
    }
}