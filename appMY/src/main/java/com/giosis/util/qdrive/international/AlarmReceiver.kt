package com.giosis.util.qdrive.international

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.giosis.library.AlertDialogActivity
import com.giosis.library.util.Preferences


class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

        Log.e("Alarm", "AlarmReceiver    App Running : ${isRunning(context)}")

        if (isRunning(context)) {

            val bun = Bundle()
            bun.putString("notiTitle", "")
            bun.putString("notiMessage", context.resources.getString(R.string.msg_qdrive_auto_logout))
            bun.putString("actionKey", "LOGOUT")
            bun.putString("actionValue", null)

            // Alert Dialog
            val popupIntent = Intent(context, AlertDialogActivity::class.java)
            popupIntent.putExtras(bun)
            val pendingIntent = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT)
            pendingIntent.send()

        } else {

            Preferences.userId = ""
            Preferences.autoLogout = true
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