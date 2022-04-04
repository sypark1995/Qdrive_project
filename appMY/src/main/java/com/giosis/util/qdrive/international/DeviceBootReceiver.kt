package com.giosis.util.qdrive.international


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.giosis.library.util.Preferences


class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            Log.e("Alarm", "DeviceBootReceiver    BOOT_COMPLETED")

            Preferences.autoLogoutSetting = false

            val array = Preferences.autoLogoutTime.split(":".toRegex()).toTypedArray()
            MyApplication.setAutoLogout(array[0].toInt(), array[1].toInt(), false)
        }
    }
}