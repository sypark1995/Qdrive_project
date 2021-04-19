package com.giosis.util.qdrive.singapore;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.LocaleManager;
import com.giosis.library.util.Preferences;
import com.giosis.util.qdrive.util.MySharedPreferences;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Calendar;

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

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);

        preferences = new MySharedPreferences(getApplicationContext());
        DatabaseHelper.getInstance(this);
        LocaleManager.Companion.getInstance(this);

        Preferences.INSTANCE.init(this);
        Preferences.INSTANCE.setAppInfo("SG");
        Preferences.INSTANCE.setUserNation("SG");


        context = getApplicationContext();
        badgeCnt = 0;


        String[] array = MyApplication.preferences.getAutoLogoutTime().split(":");
        setAutoLogout(Integer.parseInt(array[0]), Integer.parseInt(array[1]), false);

        PackageManager pm = context.getPackageManager();
        ComponentName receiver = new ComponentName(context, DeviceBootReceiver.class);

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void attachBaseContext(Context base) {
        Preferences.INSTANCE.init(base);
        super.attachBaseContext(LocaleManager.Companion.getInstance(base).setLocale(base));
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


    public static void setAutoLogout(int hour, int minute, boolean test) {

        // Auto LogOut
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 123, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.e("Alarm", "Auto Logout Setting? " + preferences.getAutoLogoutSetting());
        Log.e("Alarm", "Auto Logout Time? " + hour + ":" + minute);

        if (!preferences.getAutoLogoutSetting()) {

            Log.e("Alarm", "AlarmManager Repeating  -  " + hour + ":" + minute);
            // With setInexactRepeating(), you have to use one of the AlarmManager interval
            // constants--in this case, AlarmManager.INTERVAL_DAY.
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

            preferences.setAutoLogoutSetting(true);
        } else {
            if (test) {

                Log.e("Alarm", "test Time? " + hour + ":" + minute);
                alarmManager.cancel(pendingIntent);

                alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        }
    }
}
