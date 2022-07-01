package com.giosis.util.qdrive.singapore

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.util.LocaleManager
import com.giosis.util.qdrive.singapore.util.Preferences
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MyApplication : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        // TODO_kjyoo 머하는 뱃지 인지 모르겠음...
        var badgeCnt = 0
    }

    @Override
    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        DatabaseHelper.getInstance(this)
        LocaleManager.getInstance(this)
        Preferences.init(this)

        context = applicationContext
        badgeCnt = 0
    }

    @Override
    override fun attachBaseContext(base: Context) {
        Preferences.init(base)
        super.attachBaseContext(LocaleManager.getInstance(base).setLocale(base))
    }

}
