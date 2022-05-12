package com.giosis.util.qdrive.singapore

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.util.LocaleManager
import com.giosis.library.util.Preferences
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*

class MyApplication : MultiDexApplication() {

    private var badgeCnt = 0

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        fun setAutoLogout(hour: Int, minute: Int, test: Boolean) {

            // Auto LogOut
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            calendar[Calendar.SECOND] = 0

            val intent = Intent(
                context,
                AlarmReceiver::class.java
            )
            val pendingIntent = PendingIntent.getBroadcast(context, 123, intent, 0)
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            Log.e("Alarm", "Auto Logout Setting? " + Preferences.autoLogoutSetting)
            Log.e("Alarm", "Auto Logout Time? $hour:$minute")

            if (!Preferences.autoLogoutSetting) {
                Log.e("Alarm", "AlarmManager Repeating  -  $hour:$minute")
                // With setInexactRepeating(), you have to use one of the AlarmManager interval
                // constants--in this case, AlarmManager.INTERVAL_DAY.
                // With setInexactRepeating(), you have to use one of the AlarmManager interval
                // constants--in this case, AlarmManager.INTERVAL_DAY.
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent
                )

                Preferences.autoLogoutSetting = true
            } else {
                if (test) {
                    Log.e("Alarm", "test Time? $hour:$minute")
                    alarmManager.cancel(pendingIntent)

                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC, calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY, pendingIntent
                    )
                }
            }
        }
    }

    @Override
    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        DatabaseHelper.getInstance(this)
        LocaleManager.getInstance(this)
        Preferences.init(this)
        Preferences.userNation = "SG"

        context = applicationContext
        badgeCnt = 0

        val array = Preferences.autoLogoutTime.split(":").toTypedArray()
        setAutoLogout(array[0].toInt(), array[1].toInt(), false)
    }

    @Override
    override fun attachBaseContext(base: Context) {
        Preferences.init(base)
        super.attachBaseContext(LocaleManager.Companion.getInstance(base).setLocale(base))
    }

    fun setBadgeCnt(badgeCnt: Int) {
        this.badgeCnt = badgeCnt
    }

    fun getBadgeCnt(): Int {
        return badgeCnt
    }

}
