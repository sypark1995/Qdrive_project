package com.giosis.library.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.library.R


/**
 * 공통으로 적용하기 위한 BaseActivity
 */
open class CommonActivity : AppCompatActivity() {

    // Auto Logout
    override fun onResume() {
        super.onResume()

        if (Preferences.autoLogout) {

            Preferences.autoLogout = false;
            Toast.makeText(
                this,
                resources.getString(R.string.msg_qdrive_auto_logout),
                Toast.LENGTH_SHORT
            ).show()

            val intent = if (Preferences.userNation == "SG") {
                Intent(
                    this@CommonActivity,
                    Class.forName("com.giosis.util.qdrive.singapore.LoginActivity")
                )
            } else {
                Intent(
                    this@CommonActivity,
                    Class.forName("com.giosis.util.qdrive.international.LoginActivity")
                )
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()

        }
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            super.attachBaseContext(LocaleManager.getInstance(base).setLocale(base))
        }
    }
}