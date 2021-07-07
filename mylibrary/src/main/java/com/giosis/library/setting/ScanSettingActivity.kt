package com.giosis.library.setting

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityScanSettingBinding
import com.giosis.library.util.Preferences
import kotlinx.android.synthetic.main.activity_scan_setting.*
import kotlinx.android.synthetic.main.top_title.*

class ScanSettingActivity : BaseActivity<ActivityScanSettingBinding, ScanSettingViewModel>() {

    val tag = "ScanSettingActivity"

    override fun getLayoutId(): Int {
        return R.layout.activity_scan_setting
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): ScanSettingViewModel {
        return ViewModelProvider(this).get(ScanSettingViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        text_top_title.text = resources.getString(R.string.text_title_scan_setting)

        layout_top_back.setOnClickListener {
            finish()
        }


        getViewModel().vibration.observe(this, Observer {

            Preferences.scanVibration = it

            if (it == "OFF") {

                btn_scan_setting_vibration_on.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.color_4fb648
                        )
                )
                btn_scan_setting_vibration_off.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.white
                        )
                )

                btn_scan_setting_vibration_on.isSelected = false
                btn_scan_setting_vibration_off.isSelected = true
            } else {

                btn_scan_setting_vibration_on.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.white
                        )
                )
                btn_scan_setting_vibration_off.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.color_4fb648
                        )
                )

                btn_scan_setting_vibration_on.isSelected = true
                btn_scan_setting_vibration_off.isSelected = false
            }
        })
    }
}