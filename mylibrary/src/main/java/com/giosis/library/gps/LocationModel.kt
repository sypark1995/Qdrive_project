package com.giosis.library.gps

import android.util.Log
import com.giosis.library.util.Preferences
import kotlin.math.abs

class LocationModel {

    var parcelLat: Double = 0.0
    var parcelLng: Double = 0.0
    var zipCode: String = ""
    var state: String = ""
    var city: String = ""
    var street: String = ""

    var driverLat: Double = 0.0
    var driverLng: Double = 0.0

    var differenceLat: Double = 0.0
    var differenceLng: Double = 0.0


    fun setParcelLocation(parcelLat: Double, parcelLng: Double, zipCode: String, state: String, city: String, street: String) {

        this.parcelLat = parcelLat
        this.parcelLng = parcelLng
        this.zipCode = zipCode
        this.state = state
        this.city = city
        this.street = street
    }

    fun setDriverLocation(lat: Double, lng: Double) {

        driverLat = lat
        driverLng = lng

//        if (Preferences.serverURL.contains("staging") && Preferences.gpsMode == "TEST") {
//
//            val gpsValue = Preferences.gpsTestValue.split(",")
//
//            driverLat = parcelLat + gpsValue[0].toDouble()
//            driverLng = parcelLng + gpsValue[1].toDouble()
//            Log.e("GPSUpdate", " GPS TEST Mode  >>")
//        }

        Log.e("GPSUpdate", "GPS : $parcelLat, $parcelLng  >> $driverLat, $driverLng")

        differenceLat = abs(parcelLat - driverLat)
        differenceLng = abs(parcelLng - driverLng)
    }
}