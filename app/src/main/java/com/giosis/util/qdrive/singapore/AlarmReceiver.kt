package com.giosis.util.qdrive.singapore

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

        Log.e("Alarm", "AlarmReceiver")


        // Receive 에서 Dialog 불가능능
/*
       val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.button_confirm))
        builder.setMessage(context.resources.getString(R.string.msg_want_sign_out))
        builder.setCancelable(false)
        builder.setPositiveButton(context.resources.getString(R.string.button_ok)) { _, _ ->

            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }

        val alertDialog = builder.create()

        if (isAppRunning(context)) {

            alertDialog.show()
        } else {

            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }*/
    }

    private fun isAppRunning(context: Context): Boolean {

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        for (i in procInfos.indices) {
            if (procInfos[i].processName == context.packageName) {
                return true
            }
        }
        return false
    }
}