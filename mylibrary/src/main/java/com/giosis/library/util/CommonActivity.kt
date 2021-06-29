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

//    var progressDialog: QProgressDialog = QProgressDialog(this)
//    private var progressCount = 0
//
//
//    // QProgressDialog
//    fun showProgress() {
//
//        ++progressCount
//        //    Log.e("krm0219", "showProgress $progressCount")
//        progressDialog.showProgressDialog()
//    }
//
//    fun hideProgress() {
//
//        try {
//
//            if (progressCount == 0 || --progressCount <= 0) {
//
//                progressDialog.hideProgressDialog()
//            }
//
//            //    Log.e("krm0219", "hideProgress $progressCount")
//        } catch (e: Exception) {
//
//            Log.e("Exception", "CommonActivity hideProgress() Exception $e")
//        }
//    }


    // Auto Logout
    override fun onResume() {
        super.onResume()

        if (Preferences.autoLogout) {

            Preferences.autoLogout = false;
            Toast.makeText(this, resources.getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show()


            if (Preferences.userNation == "SG") {

                val intent = Intent(this@CommonActivity, Class.forName("com.giosis.util.qdrive.singapore.LoginActivity"))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            } else {

                val intent = Intent(this@CommonActivity, Class.forName("com.giosis.util.qdrive.international.LoginActivity"))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            super.attachBaseContext(LocaleManager.getInstance(base).setLocale(base))
        }
    }
}