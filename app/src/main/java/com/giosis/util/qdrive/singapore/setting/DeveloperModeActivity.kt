package com.giosis.util.qdrive.singapore.setting

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.giosis.util.qdrive.singapore.BR
import com.giosis.util.qdrive.singapore.ViewModelActivity
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ActivityDeveloperModeBinding
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.QDataUtil
import kotlinx.android.synthetic.main.activity_developer_mode.*
import kotlinx.android.synthetic.main.top_title.*


class DeveloperModeActivity : ViewModelActivity<ActivityDeveloperModeBinding, DeveloperModeViewModel>() {
    val tag = "DeveloperModeActivity"


    override fun getLayoutId(): Int {
        return R.layout.activity_developer_mode
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): DeveloperModeViewModel {
        return ViewModelProvider(this).get(DeveloperModeViewModel::class.java)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_developer_mode)

        layout_top_back.setOnClickListener {
            finish()
        }

        rg_developer_server_url.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_developer_server_url_test -> {
                    getViewModel().changeServer(DataUtil.SERVER_TEST)
                }
                R.id.rb_developer_server_url_staging -> {
                    getViewModel().changeServer(DataUtil.SERVER_STAGING)
                }
                R.id.rb_developer_server_url_real -> {
                    getViewModel().changeServer(DataUtil.SERVER_REAL)
                }
                R.id.rb_developer_server_url_local -> {
                    getViewModel().changeServer(DataUtil.SERVER_LOCAL)
                }
            }
        }

        rg_developer_xroute_url.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_developer_xroute_url_real -> {
                    getViewModel().changeXRouteServer(DataUtil.XROUTE_SERVER_REAL)
                }
                R.id.rb_developer_xroute_url_staging -> {
                    getViewModel().changeXRouteServer(DataUtil.XROUTE_SERVER_STAGING)
                }
            }
        }

        rg_developer_gps.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_developer_gps_real -> {
                    getViewModel().changeGPS("REAL")
                }
                R.id.rb_developer_gps_test -> {
                    getViewModel().changeGPS("TEST")
                }
            }
        }
        rssi_btn.setOnClickListener {
            rssi_text.text = QDataUtil.getMobileNetworkSignal(it.context)
        }
    }

//
//        // Auto Logout
//        text_developer_logout_time.text = MyApplication.preferences.autoLogoutTime
//
//        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
//
//            val preTime = MyApplication.preferences.autoLogoutTime
//            val changeTime = "$hourOfDay:$minute"
//
//            if (preTime != changeTime) {
//
//                MyApplication.preferences.autoLogoutTime = changeTime
//                text_developer_logout_time.text = changeTime
//
//                setAutoLogout(hourOfDay, minute, true)
//            }
//        }
//
//        val array = MyApplication.preferences.autoLogoutTime.split(":".toRegex()).toTypedArray()
//        val timePickerDialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeListener, array[0].toInt(), array[1].toInt(), true)
//        timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        btn_developer_logout_time.setOnClickListener {
//            timePickerDialog.show()
//        }
}