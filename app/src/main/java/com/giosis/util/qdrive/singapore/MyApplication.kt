package com.giosis.util.qdrive.singapore

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.util.LocaleManager
import com.giosis.util.qdrive.singapore.util.Preferences
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MyApplication : MultiDexApplication() {

    private var badgeCnt = 0

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

    }

    @Override
    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        DatabaseHelper.getInstance(this)
        LocaleManager.getInstance(this)

        Preferences.init(this)

//        val nationCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Resources.getSystem().configuration.locales[0].country
//        } else {
//            Resources.getSystem().configuration.locale.country
//        }
//        Preferences.userNation = nationCode

        context = applicationContext
        badgeCnt = 0
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
