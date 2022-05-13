package com.giosis.util.qdrive.singapore.gps

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.NetworkUtil
import com.giosis.util.qdrive.singapore.util.Preferences
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class LocationManagerWorker(private val context: Context, private val reference: String) :
    LocationListener {
    private val TAG = "LocationManagerListener"

    val locationManager: LocationManager? by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var minTime: Long = 0
    private var minDistance = 0f
    private var provider = ""

    init {

        if (reference == "time_location") {

            // TEST_ GPS Time
            //   minTime = 1000 * 60
            minTime = (1000 * 60 * 10).toLong()
            minDistance = 0f
        } else if (reference == "distance_location") {

            minTime = (1000 * 60).toLong()
            minDistance = 500f
        }
    }

    val lastLocation: Unit
        get() {
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

                        provider = "NETWORK_PROVIDER"
                        locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            minTime,
                            minDistance,
                            this
                        )
                    }

                    if (gpsEnable) {

                        provider = "GPS_PROVIDER"
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            minTime,
                            minDistance,
                            this
                        )
                    }
                } catch (e: Exception) {
                    Log.e("Location", "fail to request location update, ignore $e")
                }
            }
        }


    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        Log.e("Location", "$TAG  onLocationChanged : $latitude / $longitude")

        val channel = if (Preferences.userNation == "SG") {
            "QDRIVE"
        } else {
            "QDRIVE_V2"
        }

        RetrofitClient.instanceDynamic().requestSetGPSLocation(
            channel,
            latitude,
            longitude,
            0.0,
            reference,
            provider,
            NetworkUtil.getNetworkType(context)
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    Log.e("Server", "Location requestSetGPSLocation  result  " + it.resultCode)

                    if (it.resultCode == -16) {

                        val builder = AlertDialog.Builder(context)
                        builder.setCancelable(false)
                        builder.setTitle(context.resources.getString(R.string.text_upload_result))
                        builder.setMessage(context.resources.getString(R.string.msg_network_connect_error_saved))
                        builder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialog1: DialogInterface, _ -> dialog1.dismiss() }
                        builder.show()
                    } else if (it.resultCode < 0) {

                        val builder = AlertDialog.Builder(context)
                        builder.setCancelable(false)
                        builder.setTitle(context.resources.getString(R.string.text_fail))
                        builder.setMessage(context.resources.getString(R.string.msg_network_connect_error_saved))
                        builder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialog1: DialogInterface, _ -> dialog1.dismiss() }
                        builder.show()
                    }
                } catch (e: Exception) {
                    Log.e("Exception", " Location  onLocationChanged  Exception $e")
                }
            }) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.msg_error_check_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}