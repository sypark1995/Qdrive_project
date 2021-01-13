package com.giosis.util.qdrive.util.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.singapore.R

open class CommonActivity : AppCompatActivity() {

    var progressDialog: QProgressDialog = QProgressDialog(this)
    private var progressCount = 0


    // QProgressDialog
    fun showProgress() {

        ++progressCount
        //    Log.e("krm0219", "showProgress $progressCount")
        progressDialog.showProgressDialog()
    }

    fun hideProgress() {

        try {

            if (progressCount == 0 || --progressCount <= 0) {

                progressDialog.hideProgressDialog()
            }

            //    Log.e("krm0219", "hideProgress $progressCount")
        } catch (e: Exception) {

            Log.e("Exception", "CommonActivity hideProgress() Exception $e")
        }
    }


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


    // Multi Language
    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(MyApplication.localeManager.setLocale(newBase))
        }
    }
}