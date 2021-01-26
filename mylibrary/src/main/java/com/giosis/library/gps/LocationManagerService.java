package com.giosis.library.gps;


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

import com.giosis.library.R;
import com.giosis.library.main.MainActivity;
import com.giosis.library.util.Preferences;

public class LocationManagerService extends Service {
    String TAG = "LocationManagerService";

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

        Log.e("Location", TAG + " Library   onStartCommand");
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


            int resourceId = getResources().getIdentifier("qdrive_icon", "drawable", getPackageName());
            if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {

                resourceId = getResources().getIdentifier("icon_qdrive_my", "drawable", getPackageName());
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_ID)
                    .setContentTitle(getResources().getString(R.string.text_gps_location_service))
                    .setSmallIcon(resourceId)
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

        Log.e("Location", TAG + "   onDestroy");

        mLocationManager1.removeUpdates(locationMngTimeListener);
        mLocationManager2.removeUpdates(locationMngDistanceListener);
    }
}