package com.giosis.library.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity


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


    // TODO.  Alarm Receiver 옮기면 주석 풀기
    // Auto Logout
    override fun onResume() {
        super.onResume()

//        if (MyApplication.preferences.autoLogout) {
//
//            MyApplication.preferences.autoLogout = false;
//            Toast.makeText(this, resources.getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show()
//
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            startActivity(intent)
//        }
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            super.attachBaseContext(LocaleManager.getInstance(base).setLocale(base))
        }
    }
}