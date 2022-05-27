package com.giosis.util.qdrive.singapore.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseEvent {

    fun createEvent(context: Context, activityName: String) {
        try {
            val params = Bundle()
            params.putString("Activity", activityName)
            FirebaseAnalytics.getInstance(context).logEvent("ACTIVITY_CREATE", params)
        } catch (ignored: Exception) {
        }
    }


    fun clickEvent(context: Context, activityName: String, method: String) {
        try {
            val params = Bundle()
            params.putString("Activity", activityName)
            params.putString("method", method)
            FirebaseAnalytics.getInstance(context).logEvent("btnClick", params)
        } catch (ignored: Exception) {
        }
    }


}