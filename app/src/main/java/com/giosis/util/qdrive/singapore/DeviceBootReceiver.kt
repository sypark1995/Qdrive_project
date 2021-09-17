package com.giosis.util.qdrive.singapore

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
       
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {

            Log.e("Alarm", "DeviceBootReceiver    BOOT_COMPLETED")

            MyApplication.preferences.autoLogoutSetting = false

            val array = MyApplication.preferences.autoLogoutTime.split(":".toRegex()).toTypedArray()
            MyApplication.setAutoLogout(array[0].toInt(), array[1].toInt(), false)
        }
    }
}