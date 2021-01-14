package com.giosis.library.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class LocationManagerOnceListener implements LocationListener {
    private final String TAG = "LocationManagerOnceListener";

    private final Context context;
    private LocationManager locationManager;

    private double latitude = 0;
    private double longitude = 0;
    private double accuracy = 0;
    private int count;


    public LocationManagerOnceListener(Context context) {

        count = 0;
        this.context = context;
    }

    public LocationManager getLocationManager() {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager;
    }

    public void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        if (locationManager != null) {

            boolean networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            try {

                if (networkEnable) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }

                if (gpsEnable) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            } catch (Exception e) {

                Log.e("Location", "fail to request location update, ignore " + e.toString());
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {

            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.accuracy = location.getAccuracy();

            if (count < 5) {

                Log.e("Location", TAG + "  onLocationChanged : " + latitude + " / " + longitude + " - " + count);
                count++;
            }
        }
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
        return this.accuracy;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}