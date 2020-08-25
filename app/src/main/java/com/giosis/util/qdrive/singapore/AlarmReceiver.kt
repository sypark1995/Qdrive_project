package com.giosis.util.qdrive.singapore

import android.app.ActivityManager
import android.app.ActivityManager.RunningTaskInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

        Log.e("Alarm", "AlarmReceiver")
        Log.e("Alarm", "Running : ${isAppRunning(context)}")


        /* val bun = Bundle()
         bun.putString("notiTitle", "[Qdrive] Auto Logout")
         bun.putString("notiMessage", "Auto Logout")
         bun.putString("actionKey", "LOGOUT")
         bun.putString("actionValue", null)

         // Alert Dialog
         val popupIntent = Intent(context, AlertDialogActivity::class.java)
         popupIntent.putExtras(bun)
         val pendingIntent = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT)
         pendingIntent.send()*/
    }


    private fun isAppRunning(context: Context): Boolean {

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        /* val procInfos = activityManager.runningAppProcesses

         for (i in procInfos.indices) {

             if (procInfos[i].processName == context.packageName) {

                 return true
             }
         }
         */


        return false
    }
}