package com.giosis.util.qdrive.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class GeocoderUtil {
    private static String TAG = "GeocoderUtil";

    private static double latitude = 0;
    private static double longitude = 0;

    public GeocoderUtil(Activity activity, String address) {

        Geocoder geocoder = new Geocoder(activity);

        try {

            List<Address> addressList = geocoder.getFromLocationName(address, 5);

            if (addressList != null) {

                Address location = addressList.get(0);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (Exception e) {

            Log.e("Exception", "GeocoderUtil Exception : " + e.toString());
        }
    }

    @SuppressLint("DefaultLocale")
    public static double getLatitude() {

        return latitude;
    }

    @SuppressLint("DefaultLocale")
    public static double getLongitude() {

        return longitude;

    }


    public static String[] getLatLng(String lat_lng) {

        String[] latLngArray = {"0", "0"};

        if (!TextUtils.isEmpty(lat_lng)) {

            String[] latLng = lat_lng.split(",");

            if (!TextUtils.isEmpty(latLng[0]) && !TextUtils.isEmpty(latLng[1])) {
                // ' lat, lng '
                latLngArray[0] = latLng[0];
                latLngArray[1] = latLng[1];
            }
        }

//        Log.e(TAG, " Geocode DATA > " + latLngArray[0] + ", " + latLngArray[1]);
        return latLngArray;
    }
}