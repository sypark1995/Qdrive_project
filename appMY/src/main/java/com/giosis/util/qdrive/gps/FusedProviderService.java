package com.giosis.util.qdrive.gps;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.main.MainActivity;
import com.google.android.gms.common.api.GoogleApiClient;

// Main 에서 호출 / 5분 또는 500m 거리 이동 시 마다 호출
public class FusedProviderService extends Service {
    String TAG = "FusedProviderService";

    Context context;

    FusedProviderWorker fusedProviderTimeWorker;
    FusedProviderWorker fusedProviderDistanceWorker;

    GoogleApiClient mGoogleApiClient1;
    GoogleApiClient mGoogleApiClient2;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Location", TAG + "   onStartCommand");

        context = getApplicationContext();

        createFusedProvider();


        // eylee
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            String Channel_ID = "GPS_Fused_Provider";

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
                    .setContentTitle(getResources().getString(R.string.text_gps_service))
                    .setSmallIcon(R.drawable.icon_qdrive_my)
                    .setContentIntent(pendingIntent);

            Notification notification = builder.build();
            startForeground(1, notification);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("RestrictedApi")
    public void createFusedProvider() {

        fusedProviderTimeWorker = new FusedProviderWorker(context, "time_fused");
        fusedProviderDistanceWorker = new FusedProviderWorker(context, "distance_fused");

        mGoogleApiClient1 = fusedProviderTimeWorker.getGoogleApiClient();
        mGoogleApiClient2 = fusedProviderDistanceWorker.getGoogleApiClient();

        if (mGoogleApiClient1 != null) {
            mGoogleApiClient1.connect();
        }
        if (mGoogleApiClient2 != null) {
            mGoogleApiClient2.connect();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("Location", TAG + "   onDestroy");

        try {

            fusedProviderTimeWorker.removeLocationUpdates();
            fusedProviderDistanceWorker.removeLocationUpdates();
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onDestroy Exception : " + e.toString());
        }

        if (mGoogleApiClient1 != null) {
            mGoogleApiClient1.disconnect();
        }
        if (mGoogleApiClient2 != null) {
            mGoogleApiClient2.disconnect();
        }
    }
}