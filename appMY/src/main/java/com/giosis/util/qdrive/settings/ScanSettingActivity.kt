package com.giosis.util.qdrive.settings

import android.os.Bundle
import android.util.Log
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_scan_setting.*
import kotlinx.android.synthetic.main.top_title.*

class ScanSettingActivity : CommonActivity() {

    val tag = "ScanSettingActivity"
    private val context = MyApplication.getContext()

    lateinit var vibration: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_setting)


        text_top_title.text = context.resources.getString(R.string.text_title_scan_setting)


        vibration = MyApplication.preferences.scanVibration
        Log.e(tag, "Vibration : $vibration")


        if (vibration == "OFF") {

            btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.border_radius_30_4fb648_ffffff)
            btn_scan_setting_vibration_on.setTextColor(context.resources.getColor(R.color.color_4fb648))

            btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.bg_radius_30_4fb648)
            btn_scan_setting_vibration_off.setTextColor(context.resources.getColor(R.color.white))
        } else if (vibration == "ON") {

            btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.bg_radius_30_4fb648)
            btn_scan_setting_vibration_on.setTextColor(context.resources.getColor(R.color.white))

            btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.border_radius_30_4fb648_ffffff)
            btn_scan_setting_vibration_off.setTextColor(context.resources.getColor(R.color.color_4fb648))
        }


        layout_top_back.setOnClickListener {

            finish()
        }

        btn_scan_setting_vibration_on.setOnClickListener {

            if (vibration == "OFF") {        // OFF > ON

                vibration = "ON"

                btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.bg_radius_30_4fb648)
                btn_scan_setting_vibration_on.setTextColor(context.resources.getColor(R.color.white))

                btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.border_radius_30_4fb648_ffffff)
                btn_scan_setting_vibration_off.setTextColor(context.resources.getColor(R.color.color_4fb648))

                MyApplication.preferences.scanVibration = vibration
            }
        }

        btn_scan_setting_vibration_off.setOnClickListener {

            if (vibration == "ON") {        // ON > OFF

                vibration = "OFF"

                btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.border_radius_30_4fb648_ffffff)
                btn_scan_setting_vibration_on.setTextColor(context.resources.getColor(R.color.color_4fb648))

                btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.bg_radius_30_4fb648)
                btn_scan_setting_vibration_off.setTextColor(context.resources.getColor(R.color.white))

                MyApplication.preferences.scanVibration = vibration
            }
        }
    }
}