package com.giosis.library.setting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityChangePwdBinding
import com.giosis.library.server.APIModel
import com.giosis.library.util.DisplayUtil
import com.giosis.library.util.dialog.CustomDialog
import com.giosis.library.util.dialog.DialogUiConfig
import com.giosis.library.util.dialog.DialogViewModel
import kotlinx.android.synthetic.main.activity_change_pwd.*
import kotlinx.android.synthetic.main.top_title.*

class ChangePwdActivity : BaseActivity<ActivityChangePwdBinding, ChangePwdViewModel>() {

    val tag = "ChangePwdActivity"

    override fun getLayoutId(): Int {
        return R.layout.activity_change_pwd
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): ChangePwdViewModel {
        return ViewModelProvider(this).get(ChangePwdViewModel::class.java)
    }

    private val dialog by lazy { CustomDialog(this@ChangePwdActivity) }
    private val errDialog by lazy { CustomDialog(this@ChangePwdActivity) }
    private val resultDialog by lazy { CustomDialog(this@ChangePwdActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_title_change_password)

        layout_top_back.setOnClickListener {
            finish()
        }

        mViewModel.checkAlert.observe(this, Observer {
            if (it != null) {

                DisplayUtil.hideKeyboard(this)

                dialog.bindingData = it
                dialog.visibility = View.VISIBLE
            } else {

                dialog.visibility = View.GONE
            }
        })


        mViewModel.errorAlert.observe(this, Observer {

            DisplayUtil.hideKeyboard(this)

            val text = DialogUiConfig(
                    title = R.string.text_invalidation,
                    message = it,
                    cancelVisible = false
            )

            val listener = DialogViewModel(
                    positiveClick = {
                        when (it) {
                            R.string.msg_same_password_error -> {
                                edit_setting_change_confirm_password.requestFocus()
                            }
                            R.string.msg_password_symbols_error -> {
                                edit_setting_change_new_password.requestFocus()
                            }
                            R.string.msg_empty_password_error -> {
                                edit_setting_change_old_password.requestFocus()
                            }
                            R.string.msg_password_length_error -> {
                                edit_setting_change_new_password.requestFocus()
                            }
                        }

                        errDialog.visibility = View.GONE
                    }
            )

            errDialog.bindingData = Pair(text, listener)
            errDialog.visibility = View.VISIBLE
        })


        getViewModel().resultAlert.observe(this, Observer {

            val text = DialogUiConfig(
                    title = R.string.text_alert,
                    messageString = (it as APIModel).resultMsg.toString(),
                    cancelVisible = false
            )

            val listener = DialogViewModel(
                    positiveClick = {

                        resultDialog.visibility = View.GONE

                        if (it.resultCode == 0) {
                            finish()
                        }
                    }
            )

            resultDialog.bindingData = Pair(text, listener)
            resultDialog.visibility = View.VISIBLE
        })
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