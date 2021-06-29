package com.giosis.library.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class FusedProviderOnceListener1 {
    private final String TAG = "FusedProviderOnceListener";

    private final Context context;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private int count;

    private double latitude = 0;
    private double longitude = 0;
    private double accuracy = 0;
    private final LocationCallback locationCallback = new LocationCallback() {
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

                    if (count < 3) {
                        Log.e("Location", TAG + "  LocationCallback11 : " + location.getLatitude() + "  /  " + location.getLongitude() + "  - " + count);
                        count++;
                    }
                }
            }
        }
    };


    public FusedProviderOnceListener1(Context context) {

        count = 0;
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();

                Log.e("Location", TAG + " startLocationUpdates  getLastLocation : " + location.getLatitude() + "  /  " + location.getLongitude());
            }
        });


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public double getLatitude() {

        Log.e("Location", TAG + "  getLatitude : " + latitude);
        return latitude;
    }

    public double getLongitude() {

        Log.e("Location", TAG + "  getLongitude : " + longitude);
        return longitude;
    }

    public double getAccuracy() {

        return accuracy;
    }

    public void removeLocationUpdates() {

        Log.e("Location", TAG + "  removeLocationUpdates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}