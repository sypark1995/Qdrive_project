package com.giosis.library.gps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.giosis.library.R
import com.giosis.library.main.MainActivity
import com.giosis.library.util.Preferences

class LocationManagerService : Service() {
    var TAG = "LocationManagerService"

    private var mLocationManager1: LocationManager? = null
    private var mLocationManager2: LocationManager? = null

    private var timeListener: LocationManagerWorker? = null
    private var distanceListener: LocationManagerWorker? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e("Location", "$TAG    onStartCommand")
        startLocationService()

        // eylee
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {

            val channelId = "GPS_Location_Manager"
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

            if (Preferences.userNation != "SG") {
                resourceId = resources.getIdentifier("icon_qdrive_my", "drawable", packageName)
            }

            val builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(resources.getString(R.string.text_gps_location_service))
                .setSmallIcon(resourceId)
                .setContentIntent(pendingIntent)

            val notification = builder.build()
            startForeground(1, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startLocationService() {
        timeListener = LocationManagerWorker(this, "time_location")
        distanceListener = LocationManagerWorker(this, "distance_location")

        mLocationManager1 = timeListener!!.locationManager
        mLocationManager2 = distanceListener!!.locationManager

        timeListener!!.lastLocation
        distanceListener!!.lastLocation
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("Location", "$TAG   onDestroy")
        mLocationManager1!!.removeUpdates(timeListener!!)
        mLocationManager2!!.removeUpdates(distanceListener!!)
    }
}