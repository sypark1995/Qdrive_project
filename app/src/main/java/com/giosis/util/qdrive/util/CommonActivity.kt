package com.giosis.util.qdrive.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.library.util.LocaleManager
import com.giosis.library.util.Preferences
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.R

open class CommonActivity : AppCompatActivity() {

    // Auto Logout
    override fun onResume() {
        super.onResume()

        if (Preferences.autoLogout) {

            Preferences.autoLogout = false
            Toast.makeText(
                this,
                resources.getString(R.string.msg_qdrive_auto_logout),
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }


    // Multi Language
    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(LocaleManager.getInstance(newBase).setLocale(newBase))
        }
    }
}