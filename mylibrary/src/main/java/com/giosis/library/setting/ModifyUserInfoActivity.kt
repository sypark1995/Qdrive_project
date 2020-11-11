package com.giosis.library.setting

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityChangePwdBinding
import com.giosis.library.databinding.ActivityModifyUserInfoBinding
import com.giosis.library.util.DisplayUtil
import kotlinx.android.synthetic.main.activity_modify_user_info.*
import kotlinx.android.synthetic.main.top_title.*

class ModifyUserInfoActivity : BaseActivity<ActivityModifyUserInfoBinding, ModifyUserInfoViewModel>() {

    val tag = "ModifyUserInfoActivity"
//    val context: Context = MyApplication.getContext()
//    private val userId = MyApplication.preferences.userId



    override fun getLayoutId(): Int {
        return R.layout.activity_modify_user_info
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): ModifyUserInfoViewModel {
        return ViewModelProvider(this).get(ModifyUserInfoViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_user_info)

//
//        val mViewModel : ModifyUserInfoViewModel by viewModels()
//        val binding: ActivityModifyUserInfoBinding = DataBindingUtil.setContentView(this, R.layout.activity_modify_user_info)
//        binding.lifecycleOwner = this
//        binding.viewModel = mViewModel


        text_top_title.text = resources.getString(R.string.text_title_my_info)

        layout_top_back.setOnClickListener {
            finish()
        }




//
//        btn_setting_change_confirm.setOnClickListener {
//
//            modifyUserInfo()
//        }
    }

//
//
//    private fun modifyUserInfo() {
//
//        DisplayUtil.hideKeyboard(this)
//
//        val alertBuilder = AlertDialog.Builder(this)
//        alertBuilder.setTitle(context.resources.getString(R.string.text_modify_my_info))
//        alertBuilder.setMessage(context.resources.getString(R.string.msg_want_change_info))
//        alertBuilder.setCancelable(true)
//
//        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialogInterface, _ ->
//
//            dialogInterface.cancel()
//
//            val name = edit_setting_change_name.text.toString()
//            val email = edit_setting_change_email.text.toString()
//
//            // TODO  email 형식 체크
//            //    Log.e(tag, "Success  / $name / $email")
//
//            val modifyInfoAsyncTask = ModifyInfoAsyncTask(userId, name, email)
//            modifyInfoAsyncTask.execute()
//        }
//
//        alertBuilder.setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
//
//            Log.e(tag, "Cancel")
//
//            dialogInterface.cancel()
//        }
//
//        alertBuilder.show()
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    inner class ModifyInfoAsyncTask(private val userId: String, private val userName: String, private val userEmail: String) : AsyncTask<Void, Void, StdResult>() {
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
//                job.accumulate("name", userName)
//                job.accumulate("email", userEmail)
//                job.accumulate("app_id", DataUtil.appID)
//                job.accumulate("nation_cd", DataUtil.nationCode)
//
//                Log.e("Server", "$tag  DATA : $userId / $userName / $userEmail")
//
//
//                val methodName = "changeMyInfo"
//                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(ManualHelper.MOBILE_SERVER_URL, methodName, job)
//                // {"ResultCode":0,"ResultMsg":"Modification job is completed successfully."}
//                // {"ResultCode":-5,"ResultMsg":"This email is already registered by another Qsign ID."}
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
//        @SuppressLint("CommitPrefEdits")
//        override fun onPostExecute(result: StdResult) {
//            super.onPostExecute(result)
//
//
//            val resultCode = result.resultCode
//            val resultMsg = result.resultMsg
//
//
//            val alertBuilder = AlertDialog.Builder(this@ModifyUserInfoActivity)
//            alertBuilder.setTitle(context.resources.getString(R.string.text_alert))
//            alertBuilder.setMessage(resultMsg)
//            alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialogInterface, _ ->
//
//                if (resultCode == 0) {   // 성공
//
//                    MyApplication.preferences.userName = userName
//                    MyApplication.preferences.userEmail = userEmail
//
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