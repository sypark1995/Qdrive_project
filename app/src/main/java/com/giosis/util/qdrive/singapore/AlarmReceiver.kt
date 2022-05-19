package com.giosis.util.qdrive.singapore

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.giosis.util.qdrive.singapore.push.AlertDialogActivity
import com.giosis.util.qdrive.singapore.push.PushData
import com.giosis.util.qdrive.singapore.util.Preferences


class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

        Log.e("Alarm", "AlarmReceiver    App Running : ${isRunning(context)}")

        if (isRunning(context)) {

            val bun = Bundle()
            bun.putString(PushData.ACTION_KEY, PushData.LOGOUT)

            // Alert Dialog
            val popupIntent = Intent(context, AlertDialogActivity::class.java)
            popupIntent.putExtras(bun)
            val pendingIntent =
                PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT)
            pendingIntent.send()
        } else {
            Preferences.userId = ""
            Preferences.userPw = ""
        }
    }


    private fun isRunning(ctx: Context): Boolean {

        val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(1)

        for (task in tasks) {

            if (ctx.packageName == task.baseActivity!!.packageName)
                return true
        }
        return false
    }
}