package com.giosis.library.setting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityModifyUserInfoBinding
import com.giosis.library.server.APIModel
import com.giosis.library.util.DisplayUtil
import com.giosis.library.util.dialog.CustomDialog
import com.giosis.library.util.dialog.DialogUiConfig
import com.giosis.library.util.dialog.DialogViewModel
import kotlinx.android.synthetic.main.activity_modify_user_info.*
import kotlinx.android.synthetic.main.top_title.*


class ModifyUserInfoActivity :
    BaseActivity<ActivityModifyUserInfoBinding, ModifyUserInfoViewModel>() {

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

    private val dialog by lazy { CustomDialog(this) }
    private val errDialog by lazy { CustomDialog(this) }
    private val resultDialog by lazy { CustomDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_title_my_info)

        layout_top_back.setOnClickListener {
            finish()
        }

        getViewModel().checkAlert.observe(this, Observer {
            if (it != null) {
                DisplayUtil.hideKeyboard(this)

                dialog.bindingData = it
                dialog.visibility = View.VISIBLE

            } else {
                dialog.visibility = View.GONE
            }
        })

        getViewModel().errorAlert.observe(this, Observer {

            DisplayUtil.hideKeyboard(this)

            val text = DialogUiConfig(
                title = R.string.text_invalidation,
                message = it,
                cancelVisible = false
            )

            val listener = DialogViewModel(
                positiveClick = {
                    when (it) {
                        R.string.msg_full_name_info -> {
                            edit_setting_change_name.requestFocus()
                        }
                        R.string.msg_email_format_error -> {
                            edit_setting_change_email.requestFocus()
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
}