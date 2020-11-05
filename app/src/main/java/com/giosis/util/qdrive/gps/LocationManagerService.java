package com.giosis.util.qdrive.gps;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;

public class LocationManagerService extends Service {
    String TAG = "LocationManagerService";

    String opID = "";
    String deviceID = "";

    LocationManager mLocationManager1 = null;
    LocationManager mLocationManager2 = null;

    LocationManagerListener locationMngTimeListener = null;
    LocationManagerListener locationMngDistanceListener = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        opID = SharedPreferencesHelper.getSigninOpID(this);
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(this);
        opID = MyApplication.preferences.getUserId();
        deviceID = MyApplication.preferences.getDeviceUUID();

        startLocationService();


        // eylee
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {

            String Channel_ID = "GPS_Location_Manager";

            NotificationChannel serviceChannel = new NotificationChannel(
                    Channel_ID,
                    "Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setShowBadge(false);
            serviceChannel.setVibrationPattern(new long[]{0});         // 진동 없애기
            serviceChannel.enableVibration(true);                       // 진동 없애기

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);


            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_ID)
                    .setContentTitle(getResources().getString(R.string.text_gps_location_service))
                    .setSmallIcon(R.drawable.qdrive_icon)
                    .setContentIntent(pendingIntent);

            Notification notification = builder.build();
            startForeground(1, notification);
        }

        return super.onStartCommand(intent, flags, startId);
    }


    private void startLocationService() {

        locationMngTimeListener = new LocationManagerListener(this, "time_location");
        locationMngDistanceListener = new LocationManagerListener(this, "distance_location");

        mLocationManager1 = locationMngTimeListener.getLocationManager();
        mLocationManager2 = locationMngDistanceListener.getLocationManager();

        locationMngTimeListener.getLastLocation();
        locationMngDistanceListener.getLastLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("krm0219", TAG + "   onDestroy");

        mLocationManager1.removeUpdates(locationMngTimeListener);
        mLocationManager2.removeUpdates(locationMngDistanceListener);
    }
}