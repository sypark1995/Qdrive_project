package com.giosis.util.qdrive.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.singapore.MyApplication.setAutoLogout
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.DataUtil
import kotlinx.android.synthetic.main.activity_developer_mode.*
import kotlinx.android.synthetic.main.top_title.*


class DeveloperModeActivity : AppCompatActivity() {
    val tag = "DeveloperModeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_mode)

        val method = intent.getStringExtra("called")

        layout_top_back.setOnClickListener {

            if (method == "login") {

                val intent = Intent(this@DeveloperModeActivity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }

            finish()
        }


        text_top_title.text = resources.getString(R.string.text_developer_mode)

        rb_developer_server_url_test.text = DataUtil.SERVER_TEST
        rb_developer_server_url_staging.text = DataUtil.SERVER_STAGING
        rb_developer_server_url_real.text = DataUtil.SERVER_REAL
        initServerUrl()


        // Auto Logout
        text_developer_logout_time.text = MyApplication.preferences.autoLogoutTime

        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

            val preTime = MyApplication.preferences.autoLogoutTime
            val changeTime = "$hourOfDay:$minute"

            if (preTime != changeTime) {

                MyApplication.preferences.autoLogoutTime = changeTime
                text_developer_logout_time.text = changeTime

                setAutoLogout(hourOfDay, minute, true)
            }
        }

        val array = MyApplication.preferences.autoLogoutTime.split(":".toRegex()).toTypedArray()
        val timePickerDialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeListener, array[0].toInt(), array[1].toInt(), true)
        timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btn_developer_logout_time.setOnClickListener {

            timePickerDialog.show()
        }
    }


    private fun initServerUrl() {

        Log.e("krm0219", "init " + MyApplication.preferences.serverURL)

        val serverURL = MyApplication.preferences.serverURL


        when {
            serverURL.contains(DataUtil.SERVER_TEST) -> {

                rb_developer_server_url_test.isChecked = true
            }
            serverURL.contains(DataUtil.SERVER_STAGING) -> {

                rb_developer_server_url_staging.isChecked = true
            }
            serverURL.contentEquals(DataUtil.SERVER_REAL) -> {

                rb_developer_server_url_real.isChecked = true
            }
        }
        edit_developer_server_url.setText(serverURL.replace(DataUtil.API_ADDRESS, ""))



        rg_developer_server_url.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_developer_server_url_test -> {

                    edit_developer_server_url.setText(DataUtil.SERVER_TEST)
                    changeServerUrl()
                }
                R.id.rb_developer_server_url_staging -> {

                    edit_developer_server_url.setText(DataUtil.SERVER_STAGING)
                    changeServerUrl()
                }
                R.id.rb_developer_server_url_real -> {

                    edit_developer_server_url.setText(DataUtil.SERVER_REAL)
                    changeServerUrl()
                }
            }
        }
    }

    private fun changeServerUrl() {

        Toast.makeText(this, edit_developer_server_url.text.toString(), Toast.LENGTH_SHORT).show()
        MyApplication.preferences.serverURL = edit_developer_server_url.text.toString()
    }
}