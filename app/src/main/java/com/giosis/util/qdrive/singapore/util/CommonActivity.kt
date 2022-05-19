package com.giosis.util.qdrive.singapore.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.R


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