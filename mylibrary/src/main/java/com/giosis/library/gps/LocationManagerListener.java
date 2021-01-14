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

import com.giosis.library.util.Preferences;

public class LocationManagerListener implements LocationListener {
    private final String TAG = "LocationManagerListener";

    private final Context context;
    private LocationManager locationManager;

    private final String opID;
    private final String deviceID;
    private final String reference;

    private long minTime;
    private float minDistance;
    private String provider = "";


    public LocationManagerListener(Context context, String reference) {

        this.context = context;
        opID = Preferences.INSTANCE.getUserId();
        deviceID = Preferences.INSTANCE.getDeviceUUID();

        this.reference = reference;


        if (reference.equals("time_location")) {

            // TEST.
            // minTime = 1000 * 60;

            minTime = 1000 * 60 * 10;
            minDistance = 0;
        } else if (reference.equals("distance_location")) {

            minTime = 1000 * 60;
            minDistance = 500;
        }
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

                    provider = "NETWORK_PROVIDER";
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
                }

                if (gpsEnable) {

                    provider = "GPS_PROVIDER";
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
                }
            } catch (Exception e) {
                Log.e("Location", "fail to request location update, ignore " + e.toString());
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.e("Location", TAG + "  onLocationChanged : " + latitude + " / " + longitude);

        new FusedProviderListenerUploadHelper.Builder(context, opID, deviceID, latitude, longitude, 0, reference, provider).build().execute();
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