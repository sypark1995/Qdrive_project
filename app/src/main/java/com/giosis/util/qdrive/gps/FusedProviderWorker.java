package com.giosis.util.qdrive.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class FusedProviderWorker implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "FusedProviderWorker";

    private Context context;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private int count = 0;      // TEST.

    private String opID;
    private String deviceID;
    private String reference;

    private long MIN_TIME_BW_UPDATES;
    private long MIN_FAST_INTERVAL_UPDATES;
    private long MIN_DISTANCE_CHANGE_FOR_UPDATES;


    private String api_level;
    private String device_info;
    private String device_model;
    private String device_product;
    private String device_os_version;

    private double latitude = 0;
    private double longitude = 0;
    private double accuracy = 0;


    FusedProviderWorker(Context context, String reference) {

        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        this.reference = reference;

        opID = MyApplication.preferences.getUserId();
        deviceID = MyApplication.preferences.getDeviceUUID();

        if (reference.equals("time_fused")) {

           /* // TEST.
            MIN_TIME_BW_UPDATES = 1000 * 60;
            MIN_FAST_INTERVAL_UPDATES = 1000 * 60;*/

            MIN_TIME_BW_UPDATES = 1000 * 60 * 5;
            MIN_FAST_INTERVAL_UPDATES = 1000 * 60 * 5;
            MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        } else if (reference.equals("distance_fused")) {

            MIN_TIME_BW_UPDATES = 1000 * 60;
            MIN_FAST_INTERVAL_UPDATES = 1000 * 60;
            MIN_DISTANCE_CHANGE_FOR_UPDATES = 500;
        }

        api_level = Integer.toString(Build.VERSION.SDK_INT);   // API Level
        device_info = android.os.Build.DEVICE;           // Device
        device_model = android.os.Build.MODEL;            // Model
        device_product = android.os.Build.PRODUCT;          // Product
        device_os_version = System.getProperty("os.version"); // OS version
    }


    GoogleApiClient getGoogleApiClient() {

        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }


    @Override
    public void onConnected(Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();

                    Log.e("Location", TAG + " onConnected  getLastLocation : " + location.getLatitude() + "  /  " + location.getLongitude());
                }
            }
        });

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(MIN_TIME_BW_UPDATES);
        locationRequest.setFastestInterval(MIN_FAST_INTERVAL_UPDATES);
        locationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);


        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {

                if (location != null) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();
                    String provider = location.getProvider();

                    if (count < 5) {
                        Log.e("Location", TAG + "  LocationCallback : " + latitude + " /  " + longitude + " / " + provider + " - " + count);
                        count++;
                    }

                    uploadGPSData(latitude, longitude, accuracy, provider);
                } else {

                    uploadGPSFailedLogData();
                }
            }
        }
    };


    private void uploadGPSData(double latitude, double longitude, double accuracy, String provider) {

        new FusedProviderListenerUploadHelper.Builder(context, opID, deviceID, latitude, longitude, accuracy, reference, provider).build().execute();
    }


    private void uploadGPSFailedLogData() {

        new QuickAppUserInfoUploadHelper.Builder(context, opID, "FusedProvider Location is null", api_level, device_info,
                device_model, device_product, device_os_version, "")
                .setOnQuickQppUserInfoUploadEventListener(new QuickAppUserInfoUploadHelper.OnQuickAppUserInfoUploadEventListener() {

                    @Override
                    public void onServerResult() {

                    }
                }).build().execute();
    }


    @Override
    public void onConnectionSuspended(int arg0) {
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("Location", TAG + "  onConnectionFailed");
    }

    void removeLocationUpdates() {

        Log.e("Location", TAG + "  removeLocationUpdates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}