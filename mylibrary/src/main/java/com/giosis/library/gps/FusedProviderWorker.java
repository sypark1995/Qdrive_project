package com.giosis.library.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.giosis.library.R;
import com.giosis.library.server.RetrofitClient;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.Preferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FusedProviderWorker {
    private final String TAG = "FusedProviderWorker";

    private final Context context;

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final String reference;

    private long MIN_TIME_BW_UPDATES;
    private long MIN_FAST_INTERVAL_UPDATES;
    private long MIN_DISTANCE_CHANGE_FOR_UPDATES;

    private final String api_level;
    private final String device_info;
    private final String device_model;
    private final String device_product;
    private final String device_os_version;
    private int count = 0;

    private double latitude = 0;
    private double longitude = 0;
    private double accuracy = 0;


    public FusedProviderWorker(Context context, String reference) {

        this.context = context;
        this.reference = reference;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        if (reference.equals("time_fused")) {

//             // TEST_ GPS Time
//            MIN_TIME_BW_UPDATES = 1000 * 60;
//            MIN_FAST_INTERVAL_UPDATES = 1000 * 60;

            MIN_TIME_BW_UPDATES = 1000 * 60 * 5;
            MIN_FAST_INTERVAL_UPDATES = 1000 * 60 * 5;
            MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        } else if (reference.equals("distance_fused")) {

            MIN_TIME_BW_UPDATES = 1000 * 60;
            MIN_FAST_INTERVAL_UPDATES = 1000 * 60;
            MIN_DISTANCE_CHANGE_FOR_UPDATES = 500;
        }

        api_level = Integer.toString(Build.VERSION.SDK_INT);   // API Level
        device_info = Build.DEVICE;           // Device
        device_model = Build.MODEL;            // Model
        device_product = Build.PRODUCT;          // Product
        device_os_version = System.getProperty("os.version"); // OS version
    }


    public void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(MIN_TIME_BW_UPDATES);
        locationRequest.setFastestInterval(MIN_FAST_INTERVAL_UPDATES);
        locationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);


        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

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

        String channel = "QDRIVE";

        if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {
            channel = "QDRIVE_V2";
        }

        RetrofitClient.INSTANCE.instanceDynamic().requestSetGPSLocation(channel, latitude, longitude, accuracy, reference, provider,
                NetworkUtil.getNetworkType(context), Preferences.INSTANCE.getUserId(), Preferences.INSTANCE.getUserId(),
                Preferences.INSTANCE.getDeviceUUID(), Preferences.INSTANCE.getUserId(), DataUtil.appID, Preferences.INSTANCE.getUserNation())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {

                    try {
                        Log.e("Server", "Fused requestSetGPSLocation  result  " + it.getResultCode());

                        if (it.getResultCode() == -16) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(false);
                            builder.setTitle(context.getResources().getString(R.string.text_upload_result));
                            builder.setMessage(context.getResources().getString(R.string.msg_network_connect_error_saved));
                            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> dialog1.dismiss());
                            builder.show();
                        } else if (it.getResultCode() < 0) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(false);
                            builder.setTitle(context.getResources().getString(R.string.text_fail));
                            builder.setMessage(context.getResources().getString(R.string.msg_network_connect_error_saved));
                            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> dialog1.dismiss());
                            builder.show();
                        }
                    } catch (Exception e) {
                        Log.e("Exception", " Fused requestSetGPSLocation  Exception " + e.toString());
                    }
                }, it -> Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SimpleDateFormat")
    private void uploadGPSFailedLogData() {

        String opId = Preferences.INSTANCE.getUserId();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String regDataString = dateFormat.format(new Date());

        RetrofitClient.INSTANCE.instanceDynamic().requestSetAppUserInfo("", api_level, device_info, device_model, device_product, device_os_version,
                NetworkUtil.getNetworkType(context), "FusedProvider Location is null", regDataString, "QDRIVE", "", "",
                "", "", "", "", "", "", opId, opId, opId, DataUtil.appID, Preferences.INSTANCE.getUserNation())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {

                    try {

                        Log.e("Server", " requestSetAppUserInfo  result  " + it.getResultCode());

                        if (it.getResultCode() < 0) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(false);
                            builder.setTitle(context.getResources().getString(R.string.text_upload_result));
                            builder.setMessage(it.getResultMsg());
                            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> dialog1.dismiss());
                            builder.show();
                        }
                    } catch (Exception e) {
                        Log.e("Exception", "  requestSetAppUserInfo  Exception " + e.toString());
                    }
                }, it -> Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
    }

    public void removeLocationUpdates() {

        Log.e("Location", TAG + "  removeLocationUpdates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}