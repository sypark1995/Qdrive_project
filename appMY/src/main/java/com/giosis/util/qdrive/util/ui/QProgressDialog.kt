package com.giosis.util.qdrive.util.ui

import android.app.Activity
import android.util.Log


class QProgressDialog(val activity: Activity) {

    private var isShowing = false
    private var commonProgressDialog: CommonProgressDialog? = null


    fun showProgressDialog() {

        if (commonProgressDialog == null || !commonProgressDialog!!.isShowing) {

            try {

                if (!isShowing) {

                    isShowing = true
                    commonProgressDialog = CommonProgressDialog(activity).showProgress()
                }
            } catch (e: Exception) {

                Log.e("Exception", "QProgressDialog showProgressDialog() Exception $e")
            }
        }
    }


    fun hideProgressDialog() {

        try {

            if (commonProgressDialog != null && commonProgressDialog!!.isShowing) {

                isShowing = false
                commonProgressDialog!!.dismiss()
                commonProgressDialog = null
            }
        } catch (e: Exception) {

            Log.e("Exception", "QProgressDialog hideProgressDialog() Exception $e")
        }


        isShowing = false
        if (commonProgressDialog != null) {

            commonProgressDialog!!.dismiss()
            commonProgressDialog = null
        }
    }
}
