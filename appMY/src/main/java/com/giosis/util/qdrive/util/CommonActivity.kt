package com.giosis.util.qdrive.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.library.util.LocaleManager
import com.giosis.util.qdrive.international.LoginActivity
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.R


/**
 * 공통으로 적용하기 위한 BaseActivity
 */

open class CommonActivity : AppCompatActivity() {

    // Auto Logout
    override fun onResume() {
        super.onResume()

        if (MyApplication.preferences.autoLogout) {

            MyApplication.preferences.autoLogout = false;
            Toast.makeText(this, resources.getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            super.attachBaseContext(LocaleManager.getInstance(base).setLocale(base))
        }
    }
}
