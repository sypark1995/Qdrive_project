package com.giosis.library.gps;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import com.giosis.library.util.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

// 일회성으로 위/경도 필요
public class GPSTrackerManager {
    private final String TAG = "GPSTrackerManager";


    public GPSTrackerManager(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }


    private final Context context;

    private LocationManager locationManager;
    private boolean isGooglePlayService = false;


    // Google Play Service - Y
//    private FusedProviderOnceListener fusedProviderListener = null;
//    private GoogleApiClient mGoogleApiClient;

    private FusedProviderOnceListener1 fusedProviderListener1 = null;

    // Google Play Service - N
    private LocationManagerOnceListener locationMngListener = null;

    public boolean enableGPSSetting() {

        boolean gpsEnable = false;

        if (locationManager != null) {

            gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return gpsEnable;
    }


    public void GPSTrackerStart() {

        stopFusedProviderService();

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        isGooglePlayService = ConnectionResult.SUCCESS == status;

        if (Build.MANUFACTURER.equals("HUAWEI") && Preferences.INSTANCE.getServerURL().contains("staging")) {  // KR 화웨이폰 - google 위치정보 못가져옴

            Log.e("krm0219", TAG + "   MANUFACTURER = " + Build.MANUFACTURER); //제조사
            isGooglePlayService = false;
        }

        // TEST
        // isGooglePlayService = true;
        // isGooglePlayService = false;

        if (isGooglePlayService) {

            createFusedProvider();
        } else {

            startLocationService();
        }
    }


    private void createFusedProvider() {

//        fusedProviderListener = new FusedProviderOnceListener(context);
//        mGoogleApiClient = fusedProviderListener.getGoogleApiClient();
//
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.connect();
//        }

        fusedProviderListener1 = new FusedProviderOnceListener1(context);
        fusedProviderListener1.startLocationUpdates();
    }


    private void startLocationService() {

        locationMngListener = new LocationManagerOnceListener(context);

        locationManager = locationMngListener.getLocationManager();
        locationMngListener.getLastLocation();
    }


    public void stopFusedProviderService() {

//        if (fusedProviderListener != null)
//            fusedProviderListener.removeLocationUpdates();
//
//        if (mGoogleApiClient != null) {
//
//            mGoogleApiClient.disconnect();
//        }

        if (fusedProviderListener1 != null)
            fusedProviderListener1.removeLocationUpdates();


        if (locationManager != null) {
            if (locationMngListener != null) {

                locationManager.removeUpdates(locationMngListener);
            }
        }
    }


    public double getLatitude() {

        double latitude;

        if (isGooglePlayService) {

            //    latitude = fusedProviderListener.getLatitude();
            latitude = fusedProviderListener1.getLatitude();
        } else {

            latitude = locationMngListener.getLatitude();
        }

        return latitude;
    }


    public double getLongitude() {

        double longitude;

        if (isGooglePlayService) {

            //  longitude = fusedProviderListener.getLongitude();
            longitude = fusedProviderListener1.getLongitude();
        } else {

            longitude = locationMngListener.getLongitude();
        }

        return longitude;
    }

    public double getAccuracy() {

        double accuracy = 0;

        if (isGooglePlayService) {
            //   accuracy = fusedProviderListener.getAccuracy();
            accuracy = fusedProviderListener1.getAccuracy();
        }

        return accuracy;
    }
}