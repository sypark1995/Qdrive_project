package com.giosis.util.qdrive.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.singapore.MyApplication.setAutoLogout
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.activity_developer_mode.*
import kotlinx.android.synthetic.main.top_title.*


class DeveloperModeActivity : AppCompatActivity() {
    val tag = "DeveloperModeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_mode)


        layout_top_back.setOnClickListener {

            finish()
        }


        text_top_title.text = resources.getString(R.string.text_developer_mode)
        text_developer_logout_time.text = MyApplication.preferences.autoLogoutTime

        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

            val preTime = MyApplication.preferences.autoLogoutTime
            val changeTime = "$hourOfDay:$minute"

            if (preTime != changeTime) {

                MyApplication.preferences.autoLogoutTime = changeTime
                text_developer_logout_time.text = changeTime

                setAutoLogout(hourOfDay, minute, true)
            } else {

                Log.e("Alarm", "Same Time $preTime - $changeTime")
            }
        }


        val array = MyApplication.preferences.autoLogoutTime.split(":".toRegex()).toTypedArray()
        val timePickerDialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeListener, array[0].toInt(), array[1].toInt(), true)
        timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btn_developer_logout_time.setOnClickListener {

            timePickerDialog.show()
        }
    }
}