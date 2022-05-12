package com.giosis.library.gps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.NetworkUtil
import com.giosis.library.util.Preferences
import com.google.android.gms.location.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class FusedProviderWorker(private val context: Context, private val reference: String) {
    private val TAG = "FusedProviderWorker"

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var MIN_TIME_BW_UPDATES: Long = 0
    private var MIN_FAST_INTERVAL_UPDATES: Long = 0
    private var MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 0

    private var count = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var accuracy = 0.0

    init {

        if (reference == "time_fused") {
//             // TEST_ GPS Time
//            MIN_TIME_BW_UPDATES = 1000 * 60;
//            MIN_FAST_INTERVAL_UPDATES = 1000 * 60;
            MIN_TIME_BW_UPDATES = (1000 * 60 * 5).toLong()
            MIN_FAST_INTERVAL_UPDATES = (1000 * 60 * 5).toLong()
            MIN_DISTANCE_CHANGE_FOR_UPDATES = 0
        } else if (reference == "distance_fused") {

            MIN_TIME_BW_UPDATES = (1000 * 60).toLong()
            MIN_FAST_INTERVAL_UPDATES = (1000 * 60).toLong()
            MIN_DISTANCE_CHANGE_FOR_UPDATES = 500
        }
    }

    fun startLocationUpdates() {

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
            interval = MIN_TIME_BW_UPDATES
            fastestInterval = MIN_FAST_INTERVAL_UPDATES
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            smallestDisplacement = MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat()
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            for (location in locationResult.locations) {
                if (location != null) {

                    latitude = location.latitude
                    longitude = location.longitude
                    accuracy = location.accuracy.toDouble()
                    val provider = location.provider

                    if (count < 5) {
                        Log.e(
                            "Location",
                            "$TAG  LocationCallback : $latitude /  $longitude / $provider - $count"
                        )
                        count++
                    }

                    uploadGPSData(latitude, longitude, accuracy, provider)
                } else {

                    uploadGPSFailedLogData()
                }
            }
        }
    }

    private fun uploadGPSData(
        latitude: Double,
        longitude: Double,
        accuracy: Double,
        provider: String
    ) {

        val channel = if (Preferences.userNation == "SG") {
            "QDRIVE"
        } else {
            "QDRIVE_V2"
        }

        RetrofitClient.instanceDynamic().requestSetGPSLocation(
            channel,
            latitude,
            longitude,
            accuracy,
            reference,
            provider,
            NetworkUtil.getNetworkType(context)
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    Log.e("Server", "Fused requestSetGPSLocation  result  " + it.resultCode)

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
                    Log.e("Exception", " Fused requestSetGPSLocation  Exception $e")
                }
            }) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.msg_error_check_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadGPSFailedLogData() {
        val channel = if (Preferences.userNation == "SG") {
            "QDRIVE"
        } else {
            "QDRIVE_V2"
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val regDataString = dateFormat.format(Date())

        RetrofitClient.instanceDynamic().requestSetAppUserInfo(
            "",
            NetworkUtil.getNetworkType(context),
            "FusedProvider Location is null",
            regDataString,
            channel
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    Log.e("Server", " requestSetAppUserInfo  result  " + it.resultCode)

                    if (it.resultCode < 0) {

                        val builder = AlertDialog.Builder(context)
                        builder.setCancelable(false)
                        builder.setTitle(context.resources.getString(R.string.text_upload_result))
                        builder.setMessage(it.resultMsg)
                        builder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialog1: DialogInterface, _ -> dialog1.dismiss() }
                        builder.show()
                    }
                } catch (e: Exception) {

                    Log.e("Exception", "  requestSetAppUserInfo  Exception $e")
                }
            }) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.msg_error_check_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun removeLocationUpdates() {

        Log.e("Location", "$TAG  removeLocationUpdates")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}