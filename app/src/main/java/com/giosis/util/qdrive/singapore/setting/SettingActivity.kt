package com.giosis.util.qdrive.singapore.setting

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.giosis.util.qdrive.singapore.BR
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.ViewModelActivity
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.databinding.ActivitySettingBinding
import com.giosis.util.qdrive.singapore.util.Preferences
import com.giosis.util.qdrive.singapore.util.dialog.CustomDialog
import com.giosis.util.qdrive.singapore.util.dialog.DialogUiConfig
import com.giosis.util.qdrive.singapore.util.dialog.DialogViewModel
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.top_title.*

class SettingActivity : ViewModelActivity<ActivitySettingBinding, SettingViewModel>() {

    val tag = "SettingActivity"

    override fun getLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): SettingViewModel {
        return ViewModelProvider(this).get(SettingViewModel::class.java)
    }

    private val deleteAlert by lazy { CustomDialog(this@SettingActivity) }

    var showDeveloperModeClickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.setText(R.string.navi_setting)

        layout_top_back.setOnClickListener {
            finish()
        }

        layout_top_signout.visibility = View.VISIBLE
        layout_top_signout.setOnClickListener {
            signOut()
        }

        text_top_title.setOnClickListener {
            if (showDeveloperModeClickCount == 10) {
                if (Preferences.developerMode) {
                    // true > false
                    Preferences.developerMode = false
                } else {
                    // false > true
                    Preferences.developerMode = true
                    Toast.makeText(
                        this@SettingActivity,
                        resources.getString(R.string.text_developer_mode),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showDeveloperModeClickCount = 0
                initDeveloperMode()
            } else {
                showDeveloperModeClickCount++
            }
        }

        initDeveloperMode()

        mViewModel.deleteAlert.observe(this) {

            if (it) {
                val text = DialogUiConfig(
                    title = R.string.button_confirm,
                    message = R.string.msg_want_to_delete_data
                )

                val listener = DialogViewModel(
                    positiveClick = {

                        deleteAlert.visibility = View.GONE

                        DatabaseHelper.getInstance()
                            .delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "")

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(resources.getString(R.string.text_alert))
                        builder.setMessage(resources.getString(R.string.msg_deleted_data))
                        builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialogInterface, _ ->
                            dialogInterface.cancel()
                        }
                        builder.show()

                    },
                    negativeClick = {
                        deleteAlert.visibility = View.GONE
                    }
                )
                deleteAlert.bindingData = Pair(text, listener)
                deleteAlert.visibility = View.VISIBLE

            } else {
                deleteAlert.visibility = View.GONE
            }
        }

        mViewModel.version.value = packageManager.getPackageInfo(packageName, 0).versionName
    }


    override fun onResume() {
        super.onResume()
        mViewModel.initData()
    }


    private fun initDeveloperMode() {
        if (Preferences.developerMode) {
            btn_setting_developer_mode.visibility = View.VISIBLE
        } else {
            btn_setting_developer_mode.visibility = View.GONE
        }
    }

    private fun signOut() {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(resources.getString(R.string.button_confirm))
        alertBuilder.setMessage(resources.getString(R.string.msg_want_sign_out))
        alertBuilder.setCancelable(true)

        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { _, _ ->

            val intent = Intent(this@SettingActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        alertBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
            dialogInterface.cancel()
        }

        alertBuilder.show()

    }
}
