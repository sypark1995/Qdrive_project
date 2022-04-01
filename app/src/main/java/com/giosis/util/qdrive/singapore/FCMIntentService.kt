package com.giosis.util.qdrive.singapore

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.giosis.library.list.ListActivity
import com.giosis.library.main.MainActivity
import com.giosis.library.message.MessageListActivity
import com.giosis.library.util.DatabaseHelper
import com.giosis.util.qdrive.util.DataUtil
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FCMIntentService : FirebaseMessagingService() {
    var TAG = "FCM"

    companion object {
        ////////////////////////////////////
        const val TITLE = "title"
        const val MESSAGE = "message"
        const val ACTION_KEY = "action_key"
        const val ACTION_VALUE = "action_value"


        ////////////////////////////////////
        const val PICKUP_CANCEL = "PX"
        const val SevenEle_TAKEBACK = "7ETB" // 7E TakeBack
        const val FL_TAKEBACK = "FLTB" // FL TakeBack
        const val Locker_EXPIRED = "LAE" // Locker Alliance Expired

        const val LZD_PICK = "LZD_PICK" // lazada pickup 건 처리

        ////////////////////////////////////

        const val DOWNLOAD = "download"


        const val QX_MSG = "QXMSG"
        const val QST = "QST"

        const val LAE = "LAE"
        const val SRL = "SRL"
    }

    ////////////////////////////////////
    // 앱이 처음 설치(재설치)되거나 유효기간이 만료되면 자동으로 토큰을 새로 생성해 준다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG, "$TAG  new Token : $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(TAG, "onMessageReceived " + remoteMessage.data.size)

        /*
         * 앱이 Background 일 때, onMessageReceived 메소드를 타지 않아서 notification 관리를 할 수 없는 현상이 발생!
         * > 서버에서 메세지를 보낼 때, notification 키 없이 data 에 값을 보내면 된다.
         * 그러면 foreground, background 상관없이 onMessageReceived 함수 호출 !!
         * */

        if (remoteMessage.data.isNotEmpty()) {
            var title = remoteMessage.data[TITLE]
            var message = remoteMessage.data[MESSAGE]
            var actionKey = remoteMessage.data[ACTION_KEY]
            var actionValue = remoteMessage.data[ACTION_VALUE]

            // TEST_ 값 필수 아니어도 push 받을 수 있도록.. 테스트 용도
            if (title == null) {
                title = "QDRIVE title"
            }
            if (message == null) {
                message = "QDRIVE message"
            }
            if (actionKey == null) {
                actionKey = "QDRIVE ACTION_KEY"
            }
            if (actionValue == null) {
                actionValue = "QDRIVE ACTION_VALUE"
            }

            val myApp = applicationContext as MyApplication
            val badgeCnt = myApp.badgeCnt + 1
            myApp.badgeCnt = badgeCnt
            setBadge(this, badgeCnt)

            sendNotification(this, title, message, actionKey, actionValue)

            when (actionKey) {
                PICKUP_CANCEL,
                SevenEle_TAKEBACK,
                FL_TAKEBACK -> {
                    // 7E TakeBack : 48시간 초과 후, Pickup Driver가 수거하러 가기 전 고객이 물건을 찾아 갔을 때...
                    // FL TakeBack : 48시간 초과 후, Pickup Driver가 수거하러 가기 전 고객이 물건을 찾아 갔을 때...
                    // AlertDialog에서 'OK' 버튼을 누르지 않을 수 있어서(Dialog 바깥부분. 막기는 했지만...) 추가!
                    try {
                        //DB 삭제
                        DatabaseHelper.getInstance().delete(
                            DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                            "invoice_no='$actionValue' COLLATE NOCASE"
                        )
                    } catch (ignore: Exception) {

                    }

                }

                Locker_EXPIRED -> {
                    // Locker Alliance Expired - User key(12자리) 값 클립보드에 복사
                    DataUtil.copyClipBoard(this, actionValue)
                }
            }

            // 진동
            val tVibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            tVibrator.vibrate(1000)

            if (!actionKey.equals(QX_MSG, ignoreCase = true)) {
                if (!actionKey.equals(QST, ignoreCase = true)) {
                    val bun = Bundle()
                    bun.putString(TITLE, title)
                    bun.putString(MESSAGE, message)
                    bun.putString(ACTION_KEY, actionKey)
                    bun.putString(ACTION_VALUE, actionValue)

                    // Alert 메세지 기능
                    val popupIntent = Intent(applicationContext, AlertDialogActivity::class.java)
                    popupIntent.putExtras(bun)
                    val pie = PendingIntent.getActivity(
                        applicationContext,
                        0,
                        popupIntent,
                        PendingIntent.FLAG_ONE_SHOT
                    )
                    try {
                        pie.send() //호출
                    } catch (e: CanceledException) {
                        Log.e("getmessage", "getmessage:$message")
                    }
                }
            }
        }
    }

    private fun sendNotification(
        context: Context,
        title: String,
        message: String,
        actionKey: String,
        actionValue: String
    ) {
        var idNum = Random().nextInt(100) + 1

        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = am.getRunningTasks(1)
        val topActivity = info[0].topActivity
        val topClassname = topActivity!!.className

        var intent = Intent(context, MainActivity::class.java)

        if (topClassname.contains("singapore.LoginActivity")) {
            intent = Intent(context, LoginActivity::class.java)

        } else if (actionKey == QX_MSG) {
            //  Admin Message
            intent = Intent(context, MessageListActivity::class.java)
            intent.putExtra("position", 1)

        } else if (actionKey == QST) {
            //   Customer Message
            intent = Intent(context, MessageListActivity::class.java)
            intent.putExtra("position", 0)

        } else if (actionKey == LAE) {
            try {
                idNum = actionValue.substring(0, 9).toInt()
            } catch (e: java.lang.Exception) {

            }
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(DataUtil.locker_pin_url))

        } else if (actionKey == SRL) {

            intent = Intent(context, ListActivity::class.java)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // Oreo 버전(API 26) 이상부터는 Notification Channel 설정 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = "FCM"
            val channelNm = "Qdrive PUSH"

            val notiChannel =
                NotificationChannel(channel, channelNm, NotificationManager.IMPORTANCE_LOW)
            notiChannel.setShowBadge(true)
            notiChannel.vibrationPattern = longArrayOf(0)
            notiChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notiChannel)

            val pendingIntent =
                PendingIntent.getActivity(this, idNum, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.drawable.qdrive_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(idNum, builder.build())

        } else {

            val pendingIntent =
                PendingIntent.getActivity(this, idNum, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.qdrive_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(idNum, builder.build())
        }
    }


    private fun setBadge(context: Context, count: Int) {
        val launcherClassName = getLauncherClassName(context)
        val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
        intent.putExtra("badge_count", count)
        intent.putExtra("badge_count_package_name", context.packageName)
        intent.putExtra("badge_count_class_name", launcherClassName)
        context.sendBroadcast(intent)
    }

    private fun getLauncherClassName(context: Context): String {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in resolveInfoList) {
            val pkgName = resolveInfo.activityInfo.applicationInfo.packageName
            if (pkgName.equals(context.packageName, ignoreCase = true)) {
                return resolveInfo.activityInfo.name
            }
        }
        return ""
    }
}