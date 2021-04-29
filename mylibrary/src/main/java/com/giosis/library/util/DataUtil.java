package com.giosis.library.util;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.giosis.library.R;
import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.library.server.CallServer;
import com.giosis.library.server.ImageUpload;
import com.giosis.library.server.RetrofitClient;
import com.giosis.library.server.data.FailedCodeResult;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DataUtil {

    public static String appID = "QDRIVE";

    public static String SERVER_LOCAL = "http://172.30.88.73";
    public static String SERVER_TEST = "https://test-api.qxpress.net";
    public static String SERVER_STAGING = "http://staging-qxapi.qxpress.net";
    public static String SERVER_REAL = "https://qxapi.qxpress.net";

    public static String API_ADDRESS = "/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi/";
    public static String API_ADDRESS_QX_APP_COMMON = "/GMKT.INC.GLPS.MobileApiService/QxAppCommonService.qapi/";

    public static String XROUTE_SERVER_STAGING = "http://211.115.100.24/api/";
    public static String XROUTE_SERVER_REAL = "http://xrouter.qxpress.net/api/";

    public static String qrcode_url = "https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code=";
    public static String barcode_url = "http://image.qxpress.net/code128/code128.php?no=";
    public static String locker_pin_url = "https://www.lockeralliance.net/pin";
    public static String smart_route_url = "http://xrouter.qxpress.asia/api";


    public static String requestSetUploadDeliveryData = "SetDeliveryUploadData";
    public static String requestSetUploadPickupData = "SetPickupUploadData";


    // Main Service
    public static Intent fusedProviderService = null;
    public static Intent locationManagerService = null;


    public static int inProgressListPosition = 0;
    public static int uploadFailedListPosition = 0;


    public static Intent getFusedProviderService() {
        return fusedProviderService;
    }

    public static void setFusedProviderService(Intent fusedProviderService) {
        DataUtil.fusedProviderService = fusedProviderService;
    }

    public static Intent getLocationManagerService() {
        return locationManagerService;
    }

    public static void setLocationManagerService(Intent locationManagerService) {
        DataUtil.locationManagerService = locationManagerService;
    }

    public static void copyClipBoard(Context context, String data) {

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", data);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void captureSign(String dirName, String fileName, View targetView) {

        targetView.buildDrawingCache();
        Bitmap captureView = targetView.getDrawingCache();

        String dirPath = Environment.getExternalStorageDirectory().toString() + dirName;
        File saveDir = new File(dirPath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }

        String filePath = dirPath + "/" + fileName + ".png";

        try {

            FileOutputStream fos = new FileOutputStream(filePath);
            captureView.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // 2019.04 FA(Firebase Analytics)
    public static FirebaseAnalytics mFirebaseAnalytics;

    public static void logEvent(String event, String activity, String method) {

        try {

            Bundle params = new Bundle();
            params.putString("Activity", activity);
            params.putString("method", method);
            mFirebaseAnalytics.logEvent(event, params);
        } catch (Exception ignored) {
        }
    }

    public static void FirebaseSelectEvents(String type, String id) {

        try {

            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
            params.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            DataUtil.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
        } catch (Exception e) {

            Log.e("Firebase", "FirebaseSelectEvents error : " + e.toString());
        }
    }


    public static void enableLocationSettings(Context context) {

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(context.getResources().getString(R.string.text_location_setting))
                .setMessage(context.getResources().getString(R.string.msg_location_off))
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog, which) -> {

                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    context.startActivity(intent);

                }).show();
    }


    private static String imageUpload(Context context, Bitmap bitmap, String basePath, String path, String trackNo) {

        String imagePath = "";
        try {
            File outputDir = context.getCacheDir();

            File tempFile = null;

            try {
                tempFile = File.createTempFile("temp", ".jpg", outputDir);

            } catch (IOException ioException) {

                RetrofitClient.INSTANCE.instanceCommonService()
                        .requestWriteLog("1", "IMAGEUPLOAD", "image upload error", "image file ioException " + ioException.getLocalizedMessage())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            Log.e("imageUpload", "result  ${it.resultCode}");
                        }, it -> {
                            Log.e("imageUpload", it.getMessage());
                        });

            }


            if (tempFile != null) {

                int quality = 90;

                if (bitmap.getByteCount() > 4 * 1024 * 1024) {
                    quality = 55;
                } else if (bitmap.getByteCount() > 2 * 1024 * 1024) {
                    quality = 75;
                }

                Log.e("imageUpload", " quality = " + quality + " / " + bitmap.getByteCount());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(tempFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long size2 = tempFile.length();

                Log.e("imageUpload", "size2 11111 " + size2);

                imagePath = ImageUpload.INSTANCE.upload(tempFile, basePath, path, trackNo);

            } else {

                Log.e("ImageUpload", "tempFile is NULL why?");
                RetrofitClient.INSTANCE.instanceCommonService()
                        .requestWriteLog("1", "IMAGEUPLOAD", "image upload error", " tempFile is NULL why? ")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            Log.e("imageUpload", "result  ${it.resultCode}");
                        }, it -> {
                            Log.e("imageUpload", it.getMessage());
                        });

            }

        } catch (Exception e) {
            Log.e("ImageUpload", e.getLocalizedMessage());

            RetrofitClient.INSTANCE.instanceCommonService()
                    .requestWriteLog("1", "IMAGEUPLOAD", "image upload error", " Exception " + e.getLocalizedMessage())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(it -> {
                        Log.e("imageUpload", "result  ${it.resultCode}");
                    }, it -> {
                        Log.e("imageUpload", it.getMessage());
                    });
        }
        return imagePath;
    }


    public static String bitmapToString(Context context, Bitmap bitmap, String basePath, String path, String trackNo) {

        String imagePath = imageUpload(context, bitmap, basePath, path, trackNo);

        if (TextUtils.isEmpty(imagePath)) {

            try {
                Thread.sleep(2000);
            } catch (Exception e) {

            }

            imagePath = imageUpload(context, bitmap, basePath, path, trackNo);
        }

        return imagePath;
    }

    public static void stopGPSManager(GPSTrackerManager gpsTrackerManager) {

        if (gpsTrackerManager != null) {

            Log.e("Location", "Stop GPS");
            gpsTrackerManager.stopFusedProviderService();
        }
    }


    // NOTIFICATION. 202012  Failed Reason
    public static void requestServerPickupFailedCode() {
        CallServer.INSTANCE.getFailedCode(CallServer.PFC, "", new CallServer.GetFailedCodeCallback() {

            @Override
            public void onServerError(int value) {
            }

            @Override
            public void onServerResult(@NotNull FailedCodeResult value) {

                if (value.getResultCode() == 10) {

                    Gson gson = new Gson();
                    String json = gson.toJson(value);
                    Log.e("krm0219", "P  getFailedCode  " + json);
                    Preferences.INSTANCE.setPFailedCode(json);
                }
            }
        });
    }

    public static void requestServerDeliveryFailedCode() {

        CallServer.INSTANCE.getFailedCode(CallServer.DFC, "", new CallServer.GetFailedCodeCallback() {

            @Override
            public void onServerError(int value) {
            }

            @Override
            public void onServerResult(@NotNull FailedCodeResult value) {

                if (value.getResultCode() == 10) {

                    Gson gson = new Gson();
                    String json = gson.toJson(value);
                    Log.e("krm0219", "D  getFailedCode  " + json);
                    Preferences.INSTANCE.setDFailedCode(json);
                }
            }
        });
    }


    public static ArrayList<FailedCodeResult.FailedCode> getFailCode(String type) {

        ArrayList<FailedCodeResult.FailedCode> arrayList;
        String json = "";

        if (type.equals("D")) {

            json = Preferences.INSTANCE.getDFailedCode();
        } else if (type.equals("P")) {

            json = Preferences.INSTANCE.getPFailedCode();
        }


        if (json.equals("")) {

            return null;
        } else {

            Gson gson = new Gson();
            FailedCodeResult result = gson.fromJson(json, FailedCodeResult.class);
            arrayList = new ArrayList<>(result.getResultObject());
        }

        return arrayList;
    }
}