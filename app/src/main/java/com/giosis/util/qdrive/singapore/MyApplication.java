package com.giosis.util.qdrive.singapore;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.giosis.util.qdrive.util.MySharedPreferences;

import java.util.Calendar;

import androidx.multidex.MultiDexApplication;

/*********
 *
 * @author jtpark_eurasia
 * 전역 변수 관리
 * @editor krm0219
 */
public class MyApplication extends MultiDexApplication {
    String TAG = "MyApplication";

    public static MySharedPreferences preferences;
    private static Context context;

    private int badgeCnt;


    static AlarmReceiver alarmReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = new MySharedPreferences(getApplicationContext());
        context = getApplicationContext();
        badgeCnt = 0;

/*
        if (MyApplication.alarmReceiver == null) {

            alarmReceiver = new AlarmReceiver(context);

            IntentFilter filter = new IntentFilter();
            //    filter.addAction(BluetoothDeviceData.ACTION_CONNECT_STATE);        // "action_connect_state"
            registerReceiver(alarmReceiver, filter);
        }
*/

        String[] array = MyApplication.preferences.getAutoLogoutTime().split(":");
        Log.e("Alarm", "AUTO LogOut Time : " + MyApplication.preferences.getAutoLogoutTime());
        Log.e("Alarm", "AUTO LogOut Time 11 : " + array[0] + " / " + array[1]);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(array[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(array[1]));


        PackageManager pm = context.getPackageManager();
        ComponentName receiver = new ComponentName(context, DeviceBootReceiver.class);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (pendingIntent != null && alarmManager != null) {

            Log.e("Alarm", "before cancel");
            alarmManager.cancel(pendingIntent);
        }

        if (alarmManager != null) {

            Log.e("Alarm", "set Repeating");
            // With setInexactRepeating(), you have to use one of the AlarmManager interval
            // constants--in this case, AlarmManager.INTERVAL_DAY.
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            // TEST   60 * 1000 * 5

           /*
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                   AlarmManager.INTERVAL_DAY, pendingIntent);

           *  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
           * */
        }


        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static Context getContext() {
        return context;
    }

    public void setBadgeCnt(int badgeCnt) {
        this.badgeCnt = badgeCnt;
    }

    public int getBadgeCnt() {
        return badgeCnt;
    }

}