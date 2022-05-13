package com.giosis.util.qdrive.singapore.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class FusedProviderOnceListener(private val context: Context) {

    private val TAG = "FusedProviderOnceListener"

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var count = 0
    var latitude = 0.0
    var longitude = 0.0
    var accuracy = 0.0

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return

            for (location in locationResult.locations) {

                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    accuracy = location.accuracy.toDouble()

                    if (count < 3) {
                        Log.e(
                            "Location",
                            TAG + "  LocationCallback : " + location.latitude + "  /  " + location.longitude + "  - " + count
                        )
                        count++
                    }
                }
            }
        }
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {

                latitude = location.latitude
                longitude = location.longitude
                accuracy = location.accuracy.toDouble()
                Log.e(
                    "Location",
                    TAG + " startLocationUpdates  getLastLocation : " + location.latitude + "  /  " + location.longitude
                )
            }
        }


        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun removeLocationUpdates() {
        Log.e("Location", "$TAG  removeLocationUpdates")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}