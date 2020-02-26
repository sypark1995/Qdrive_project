package com.giosis.util.qdrive.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.gc.android.market.api.Base64;
import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.message.AdminMessageListDetailActivity;
import com.giosis.util.qdrive.message.CustomerMessageListDetailActivity;
import com.giosis.util.qdrive.message.MessageListActivity;
import com.giosis.util.qdrive.singapore.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class DataUtil {

    public static String appID = "QDRIVE";
    public static String nationCode = "SG";
    public static String qDeliveryData = "QDATA";

    public static String qrcode_url = "https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code=";
    public static String barcode_url = "http://image.qxpress.asia/code128/code128.php?no=";
    public static String locker_pin_url = "https://www.lockeralliance.net/pin";
    public static String smart_route_url = "http://xrouter.qxpress.asia/api";

    // Main Service
    public static Intent fusedProviderService = null;
    public static Intent locationManagerService = null;

    // push Message 이동
    public static MessageListActivity messageListActivity = null;
    public static CustomerMessageListDetailActivity customerMessageListDetailActivity = null;
    public static AdminMessageListDetailActivity adminMessageListDetailActivity = null;


    //
    public static int inProgressListPosition = 0;
    public static int uploadFailedListPosition = 0;

    // 2019.04 FA(Firebase Analytics)
    public static FirebaseAnalytics mFirebaseAnalytics;

    // 2019.07  -  Smart Route
    public static String SHARED_PREFERENCE_FILE = "com.giosis.qdrive.sharedpreferences";
    public static String QDRIVE_API = "qdriveApiSG";
    public static String QDRIVE_NATION_CODE = "SG";


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


    public static MessageListActivity getMessageListActivity() {
        return messageListActivity;
    }

    public static void setMessageListActivity(MessageListActivity messageListActivity) {
        DataUtil.messageListActivity = messageListActivity;
    }

    public static CustomerMessageListDetailActivity getCustomerMessageListDetailActivity() {
        return customerMessageListDetailActivity;
    }

    public static void setCustomerMessageListDetailActivity(CustomerMessageListDetailActivity customerMessageListDetailActivity) {
        DataUtil.customerMessageListDetailActivity = customerMessageListDetailActivity;
    }

    public static AdminMessageListDetailActivity getAdminMessageListDetailActivity() {
        return adminMessageListDetailActivity;
    }

    public static void setAdminMessageListDetailActivity(AdminMessageListDetailActivity adminMessageListDetailActivity) {
        DataUtil.adminMessageListDetailActivity = adminMessageListDetailActivity;
    }

    public static void copyClipBoard(Context context, String data) {

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", data);
        clipboardManager.setPrimaryClip(clipData);
    }


    public static void enableLocationSettings(final Activity activity, Context context) {

        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(context.getResources().getString(R.string.text_location_setting))
                .setMessage(context.getResources().getString(R.string.msg_location_off))
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        activity.startActivity(intent);
                    }
                }).show();
    }

    public static void stopGPSManager(GPSTrackerManager gpsTrackerManager) {

        if (gpsTrackerManager != null) {

            Log.e("DataUtil", "Stop GPS");
            gpsTrackerManager.stopFusedProviderService();
        }
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

    public static String bitmapToString(Bitmap bitmap) {

        String pngImage = "";

        try {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String imgToString = Base64.encodeBytes(byteArray);

            StringBuilder sb = new StringBuilder();
            sb.append("data:image/png;base64,");
            sb.append(imgToString);
            pngImage = sb.toString();
        } catch (Exception e) {

            e.printStackTrace();
        }

        return pngImage;
    }
}