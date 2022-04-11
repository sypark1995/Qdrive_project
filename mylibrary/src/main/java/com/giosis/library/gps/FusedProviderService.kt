package com.giosis.library.gps

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.giosis.library.R
import com.giosis.library.main.MainActivity
import com.giosis.library.util.Preferences

// Main 에서 호출 / 5분 또는 500m 거리 이동 시 마다 호출
class FusedProviderService : Service() {
    var TAG = "FusedProviderService"

    private var fusedProviderTimeWorker: FusedProviderWorker? = null
    private var fusedProviderDistanceWorker: FusedProviderWorker? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("Location", "$TAG   onStartCommand")

        createFusedProvider()

        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {

            val channelId = "GPS_Fused_Provider"

            val serviceChannel = NotificationChannel(
                channelId,
                "Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            serviceChannel.setShowBadge(false)
            serviceChannel.vibrationPattern = longArrayOf(0) // 진동 없애기
            serviceChannel.enableVibration(true) // 진동 없애기


            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)

            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            var resourceId = resources.getIdentifier("qdrive_icon", "drawable", packageName)

            if (!Preferences.userNation.equals("SG", ignoreCase = true)) {
                resourceId = resources.getIdentifier("icon_qdrive_my", "drawable", packageName)
            }

            val builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(resources.getString(R.string.text_gps_service))
                .setSmallIcon(resourceId)
                .setContentIntent(pendingIntent)
            val notification = builder.build()
            startForeground(1, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    fun createFusedProvider() {

        fusedProviderTimeWorker = FusedProviderWorker(this, "time_fused")
        fusedProviderDistanceWorker = FusedProviderWorker(this, "distance_fused")

        fusedProviderTimeWorker?.startLocationUpdates()
        fusedProviderDistanceWorker?.startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Location", "$TAG   onDestroy")

        try {

            fusedProviderTimeWorker?.removeLocationUpdates()
            fusedProviderDistanceWorker?.removeLocationUpdates()
        } catch (e: Exception) {

            Log.e("Exception", "$TAG  onDestroy Exception : $e")
        }
    }
}