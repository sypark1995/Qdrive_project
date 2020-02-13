package com.giosis.util.qdrive.gps;

import android.content.Context;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

// 일회성으로 위/경도 필요
public class GPSTrackerManager {
    private String TAG = "GPSTrackerManager";


    public GPSTrackerManager(Context context) {

        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }


    private Context context;

    private LocationManager locationManager;
    private boolean isGooglePlayService = false;


    // Google Play Service - Y
    private FusedProviderOnceListener fusedProviderListener = null;
    private GoogleApiClient mGoogleApiClient;

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

        fusedProviderListener = new FusedProviderOnceListener(context);
        mGoogleApiClient = fusedProviderListener.getGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    private void startLocationService() {

        locationMngListener = new LocationManagerOnceListener(context);

        locationManager = locationMngListener.getLocationManager();
        locationMngListener.getLastLocation();
    }


    public void stopFusedProviderService() {

        if (fusedProviderListener != null)
            fusedProviderListener.removeLocationUpdates();

        if (mGoogleApiClient != null) {

            mGoogleApiClient.disconnect();
        }

        if (locationManager != null) {
            if (locationMngListener != null) {

                locationManager.removeUpdates(locationMngListener);
            }
        }
    }


    public double getLatitude() {

        double latitude;

        if (isGooglePlayService) {

            latitude = fusedProviderListener.getLatitude();
        } else {

            latitude = locationMngListener.getLatitude();
        }

        return latitude;
    }


    public double getLongitude() {

        double longitude;

        if (isGooglePlayService) {

            longitude = fusedProviderListener.getLongitude();
        } else {

            longitude = locationMngListener.getLongitude();
        }

        return longitude;
    }

    public double getAccuracy() {

        double accuracy = 0;

        if (isGooglePlayService) {
            accuracy = fusedProviderListener.getAccuracy();
        }

        return accuracy;
    }
}