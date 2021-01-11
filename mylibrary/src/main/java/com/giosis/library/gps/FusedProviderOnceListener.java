package com.giosis.library.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


public class FusedProviderOnceListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "FusedProviderOnceListener";

    private Context context;
    private int count;          // TEST.

    private FusedLocationProviderClient fusedLocationProviderClient;

    private double latitude = 0;
    private double longitude = 0;
    private double accuracy = 0;


    public FusedProviderOnceListener(Context context) {

        count = 0;
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }


    public GoogleApiClient getGoogleApiClient() {

        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // PRIORITY_HIGH_ACCURACY
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);

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

                    if (count < 3) {
                        Log.e("Location", TAG + "  LocationCallback : " + location.getLatitude() + "  /  " + location.getLongitude() + "  - " + count);
                        count++;
                    }
                }
            }
        }
    };


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


    @Override
    public void onConnectionSuspended(int arg0) {

        Log.e("Location", TAG + "  onConnectionSuspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("Location", TAG + "  onConnectionFailed");
    }

    public void removeLocationUpdates() {

        Log.e("Location", TAG + "  removeLocationUpdates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

}