package com.giosis.library.setting

import android.app.AlertDialog
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityModifyUserInfoBinding
import com.giosis.library.server.APIModel
import com.giosis.library.util.DisplayUtil
import kotlinx.android.synthetic.main.activity_modify_user_info.*
import kotlinx.android.synthetic.main.top_title.*


class ModifyUserInfoActivity : BaseActivity<ActivityModifyUserInfoBinding, ModifyUserInfoViewModel>() {

    val tag = "ModifyUserInfoActivity"


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


        text_top_title.text = resources.getString(R.string.text_title_my_info)

        layout_top_back.setOnClickListener {
            finish()
        }


        getViewModel().checkAlert.observe(this) {

            DisplayUtil.hideKeyboard(this)

            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle(resources.getString(R.string.text_modify_my_info))
            alertBuilder.setMessage(resources.getString(R.string.msg_want_change_info))
            alertBuilder.setCancelable(true)

            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { _, _ ->
                getViewModel().modifyUserInfo()
            }

            alertBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

            alertBuilder.show()
        }


        getViewModel().errorAlert.observe(this) {

            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle(resources.getString(R.string.text_invalidation))
            alertBuilder.setMessage(resources.getString(it))
            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                when (it) {
                    R.string.msg_full_name_info -> {
                        edit_setting_change_name.requestFocus()
                    }
                    R.string.msg_email_format_error -> {
                        edit_setting_change_email.requestFocus()
                    }
                }

                dialogInterface.cancel()
            }

            alertBuilder.show()
        }


        getViewModel().successAlert.observe(this) {

            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle(resources.getString(R.string.text_alert))
            alertBuilder.setMessage((it as APIModel).resultMsg)
            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->

                dialogInterface.cancel()

                if (it.resultCode == 0) {

                    finish()
                }
            }

            alertBuilder.show()
        }
    }


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