package com.giosis.util.qdrive.international;


import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.giosis.library.main.MainActivity;
import com.giosis.library.message.MessageListActivity;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.database.DatabaseHelper;
import com.giosis.library.util.DataUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Random;


public class FCMIntentService extends FirebaseMessagingService {
    String TAG = "FCMIntentService";

    Context context;
    DatabaseHelper dbHelper;

    // 앱이 처음 설치(재설치)되거나 유효기간이 만료되면 자동으로 토큰을 새로 생성해 준다.
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("FCM", TAG + "  new Token : " + token);
    }


    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }
        return null;
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        context = this;

        Log.e("FCM", "onMessageReceived " + remoteMessage.getData().size());

        /*
          앱이 Background 일 때, onMessageReceived 메소드를 타지 않아서 notification 관리를 할 수 없는 현상이 발생!
          > 서버에서 메세지를 보낼 때, notification 키 없이 data 에 값을 보내면 된다.
          그러면 foreground, background 상관없이 onMessageReceived 함수 호출 !!
          */

        if (0 < remoteMessage.getData().size()) {

            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String action_key = remoteMessage.getData().get("action_key");
            String action_value = remoteMessage.getData().get("action_value");

            // TEST - 값 필수 아니어도 push 받을 수 있도록.. 테스트 용도
            if (title == null) {
                title = "QDRIVE title";
            }

            if (message == null) {
                message = "QDRIVE message";
            }

            if (action_key == null) {
                action_key = "QDRIVE ACTION_KEY";
            }

            if (action_value == null) {
                action_value = "QDRIVE ACTION_VALUE";
            }


            ActivityManager am = (ActivityManager) getSystemService(context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
            ComponentName topActivity = Info.get(0).topActivity;
            String topClassname = topActivity.getClassName();


            int badgeCnt = 0;
            MyApplication myApp = (MyApplication) getApplicationContext();
            badgeCnt = myApp.getBadgeCnt() + 1;
            myApp.setBadgeCnt(badgeCnt);
            setBadge(context, badgeCnt);

            if (title.equals("")) {
                title = "QSign SG";
            }

            sendNotification(context, topClassname, title, message, action_key, action_value); // 안드로이드폰에 Noti기능

            if (action_key.equals(BarcodeType.PICKUP_CANCEL)) {

                try {
                    dbHelper = DatabaseHelper.getInstance();
                    //DB 삭제
                    dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "invoice_no='" + action_value + "' COLLATE NOCASE");
                } catch (Exception e) {

                }
            } else if (action_key.equals("7ETB")) {
                // 7E TakeBack : 48시간 초과 후, Pickup Driver가 수거하러 가기 전 고객이 물건을 찾아 갔을 때...
                // AlertDialog에서 'OK' 버튼을 누르지 않을 수 있어서(Dialog 바깥부분. 막기는 했지만...) 추가!
                try {

                    dbHelper = DatabaseHelper.getInstance();
                    dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "invoice_no='" + action_value + "' COLLATE NOCASE");
                } catch (Exception e) {

                }
            } else if (action_key.equals("FLTB")) {
                // FL TakeBack : 48시간 초과 후, Pickup Driver가 수거하러 가기 전 고객이 물건을 찾아 갔을 때...
                // AlertDialog에서 'OK' 버튼을 누르지 않을 수 있어서(Dialog 바깥부분. 막기는 했지만...) 추가!
                try {

                    dbHelper = DatabaseHelper.getInstance();
                    dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "invoice_no='" + action_value + "' COLLATE NOCASE");
                } catch (Exception e) {

                }
            } else if (action_key.equals("LAE")) {
                // Locker Alliance Expired - User key(12자리) 값 클립보드에 복사
                DataUtil.copyClipBoard(context, action_value);
            }

            // 진동
            Vibrator tVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            tVibrator.vibrate(1000);

            if (!action_key.equalsIgnoreCase("QXMSG")) {
                if (!action_key.equalsIgnoreCase("QST")) {

                    Bundle bun = new Bundle();
                    bun.putString("notiTitle", title);
                    bun.putString("notiMessage", message);
                    bun.putString("actionKey", action_key);
                    bun.putString("actionValue", action_value);

                    if (action_key.equals("7ETB") || action_key.equals("FLTB")) {

                        bun.putString("outletPush", "Y");
                    }

                    // Alert메세지 기능
                    Intent popupIntent = new Intent(getApplicationContext(), AlertDialogActivity.class);
                    popupIntent.putExtras(bun);
                    PendingIntent pie = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);

                    try {
                        pie.send(); //호출
                    } catch (CanceledException e) {

                    }
                }
            }
        }
    }

    private void sendNotification(Context context, String topClassname, String title, String message, String action_key, String action_value) {

        Intent notificationIntent;

        Random generator = new Random();
        int idNum = generator.nextInt(100) + 1;
        Log.e("FCM", "sendNotification DATA > " + title + " / " + message + " / " + idNum + " / " + topClassname);

        if (topClassname.contains("international.LoginActivity")) {

            notificationIntent = new Intent(context, LoginActivity.class);
        } else {
            notificationIntent = new Intent(context, MainActivity.class);
        }


        if (action_key.equalsIgnoreCase("QXMSG")) {
            //  Admin Message
            notificationIntent = new Intent(context, MessageListActivity.class);
            notificationIntent.putExtra("position", 1);
        } else if (action_key.equalsIgnoreCase("QST")) {
            //   Customer Message
            notificationIntent = new Intent(context, MessageListActivity.class);
            notificationIntent.putExtra("position", 0);
        } else if (action_key.equals("LAE")) {

            if (action_value != null) {

                idNum = Integer.parseInt(action_value.substring(0, 9));
            }
            notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DataUtil.locker_pin_url));
        }


        // Oreo 버전(API 26) 이상부터는 Notification Channel 설정 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channel = "FCM";
            String channel_nm = "Qdrive PUSH";

            NotificationChannel notificationChannel = new NotificationChannel(channel, channel_nm, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setShowBadge(true);
            notificationChannel.setVibrationPattern(new long[]{0});
            notificationChannel.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, idNum, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
                    .setSmallIcon(R.drawable.icon_qdrive_my)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager1.notify(idNum, builder.build());
        } else {

            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, idNum, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                    .setSmallIcon(R.drawable.icon_qdrive_my)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager1.notify(idNum, builder.build());
        }
    }
}
