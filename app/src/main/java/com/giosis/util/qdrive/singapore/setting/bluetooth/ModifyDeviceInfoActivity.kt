package com.giosis.util.qdrive.singapore.setting.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.Toast
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.util.CommonActivity
import com.giosis.util.qdrive.singapore.util.FirebaseEvent
import kotlinx.android.synthetic.main.activity_modify_device_info.*
import kotlinx.android.synthetic.main.top_title.*

class ModifyDeviceInfoActivity : CommonActivity() {
    val TAG = "ModifyDeviceInfoActivity"

    var device: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_device_info)

        FirebaseEvent.createEvent(this, TAG)

        text_top_title.setText(R.string.text_title_device_info)

        device = intent.getParcelableExtra("device")

        if (device != null) {
            var deviceName = device!!.name
            try {
                val method = device!!.javaClass.getMethod("getAliasName")
                if (method != null) {
                    deviceName = method.invoke(device) as String
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            text_setting_printer_device_name.text = deviceName
            edit_setting_printer_device_rename.setText(deviceName)
        }

        layout_top_back.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btn_setting_printer_device_rename_confirm.setOnClickListener {
            modifyConfirmClick()
        }
    }

    private fun modifyConfirmClick() {
        val rename = edit_setting_printer_device_rename.text.toString().trim { it <= ' ' }
        if (rename == "") {
            Toast.makeText(
                this,
                resources.getString(R.string.text_device_name_info),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            if (device != null) {
                val method = device!!.javaClass.getMethod("setAlias", String::class.java)
                method.invoke(device, rename)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setResult(RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }
}
