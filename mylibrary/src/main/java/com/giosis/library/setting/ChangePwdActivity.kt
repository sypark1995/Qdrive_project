package com.giosis.library.setting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.ViewModelActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityChangePwdBinding
import com.giosis.library.server.APIModel
import com.giosis.library.util.DisplayUtil
import com.giosis.library.util.dialog.CustomDialog
import com.giosis.library.util.dialog.DialogUiConfig
import com.giosis.library.util.dialog.DialogViewModel
import kotlinx.android.synthetic.main.activity_change_pwd.*
import kotlinx.android.synthetic.main.top_title.*

class ChangePwdActivity : ViewModelActivity<ActivityChangePwdBinding, ChangePwdViewModel>() {

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
}