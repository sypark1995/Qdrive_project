package com.giosis.util.qdrive.singapore.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

public class NetworkUtil {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            @SuppressLint("MissingPermission") NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            /// if no network is available networkInfo will be null
            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }


    public static String getNetworkType(Context context) {
        String networkType = "";

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    networkType = getMobileTypeName(activeNetwork);
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    networkType = "WiFi";
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    networkType = "4G";
                    break;
                default:
                    networkType = "";
                    break;
            }
        }

        return networkType;
    }

    private static String getMobileTypeName(NetworkInfo networkInfo) {
        String mobileTypeName = "";
        if (networkInfo != null) {
            String subtypeName = networkInfo.getSubtypeName();
            if (!TextUtils.isEmpty(subtypeName)) {
                if (subtypeName.equals("LTE")) {
                    mobileTypeName = subtypeName;
                } else {
                    mobileTypeName = "3G";
                }
            }
        }

        return mobileTypeName;
    }
}
