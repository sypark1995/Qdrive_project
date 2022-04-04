package com.giosis.library.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

class LocationManagerOnceListener(private val context: Context) : LocationListener {
    private val TAG = "LocationManagerOnceListener"

    val locationManager: LocationManager? by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var count = 0
    var latitude = 0.0
    var longitude = 0.0


    fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (locationManager != null) {

            val networkEnable =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val gpsEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            try {
                if (networkEnable) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0f,
                        this
                    )
                }
                if (gpsEnable) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        this
                    )
                }
            } catch (e: Exception) {
                Log.e("Location", "fail to request location update, ignore $e")
            }
        }
    }


    override fun onLocationChanged(location: Location) {

        latitude = location.latitude
        longitude = location.longitude

        if (count < 5) {
            Log.e("Location", "$TAG  onLocationChanged : $latitude / $longitude - $count")
            count++
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}