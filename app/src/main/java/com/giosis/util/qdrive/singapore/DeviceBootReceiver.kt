package com.giosis.util.qdrive.singapore

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*


class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            Log.e("Alarm", "BOOT_COMPLETED")

            // on device boot complete, reset the alarm
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val array = MyApplication.preferences.autoLogoutTime.split(":".toRegex()).toTypedArray()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar[Calendar.HOUR_OF_DAY] = array[0].toInt()
            calendar[Calendar.MINUTE] = array[1].toInt()

            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }
}