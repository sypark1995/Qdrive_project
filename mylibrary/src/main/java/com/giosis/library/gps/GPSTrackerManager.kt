package com.giosis.library.gps

import android.content.Context
import android.location.LocationManager
import android.os.Build
import com.giosis.library.util.Preferences
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

// 일회성으로 위/경도 필요
class GPSTrackerManager(private val context: Context) {
    private val TAG = "GPSTrackerManager"

    private var locationManager: LocationManager?
    private var isGooglePlayService = false

    // Google Play Service - Y
    private var fusedProviderListener: FusedProviderOnceListener? = null

    // Google Play Service - N
    private var locationMngListener: LocationManagerOnceListener? = null

    init {

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun enableGPSSetting(): Boolean {

        var gpsEnable = false

        if (locationManager != null) {
            gpsEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        return gpsEnable
    }

    fun gpsTrackerStart() {

        stopFusedProviderService()

        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        isGooglePlayService = ConnectionResult.SUCCESS == status

        // TEST_
        if (Build.MANUFACTURER.equals("HUAWEI") && Preferences.serverURL.contains("staging")) {
            // KR 화웨이폰 - google 위치정보 못가져옴
            isGooglePlayService = false
        }
        // isGooglePlayService = true
        // isGooglePlayService = false

        if (isGooglePlayService) {
            createFusedProvider()
        } else {
            startLocationService()
        }
    }

    private fun createFusedProvider() {

        fusedProviderListener = FusedProviderOnceListener(context)
        fusedProviderListener?.startLocationUpdates()
    }

    private fun startLocationService() {

        locationMngListener = LocationManagerOnceListener(context)
        locationManager = locationMngListener?.locationManager
        locationMngListener?.getLastLocation()
    }

    fun stopFusedProviderService() {
        if (fusedProviderListener != null) fusedProviderListener!!.removeLocationUpdates()

        if (locationManager != null) {
            if (locationMngListener != null) {
                locationManager!!.removeUpdates(locationMngListener!!)
            }
        }
    }

    val latitude: Double
        get() {
            var latitude = 0.0

            if (isGooglePlayService) {
                fusedProviderListener?.let {
                    latitude = it.latitude
                }
            } else {
                locationMngListener?.let {
                    latitude = it.latitude
                }
            }

            return latitude
        }
    val longitude: Double
        get() {
            var longitude = 0.0

            if (isGooglePlayService) {
                fusedProviderListener?.let {
                    longitude = it.longitude
                }
            } else {
                locationMngListener?.let {
                    longitude = it.longitude
                }
            }

            return longitude
        }

    val accuracy: Double
        get() {
            var accuracy = 0.0

            if (isGooglePlayService) {
                fusedProviderListener?.let {
                    accuracy = it.accuracy
                }
            }
            return accuracy
        }
}