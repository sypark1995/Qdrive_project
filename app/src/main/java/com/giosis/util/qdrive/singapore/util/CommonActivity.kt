package com.giosis.util.qdrive.singapore.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * 공통으로 적용하기 위한 BaseActivity
 */
open class CommonActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            super.attachBaseContext(LocaleManager.getInstance(base).setLocale(base))
        }
    }
}