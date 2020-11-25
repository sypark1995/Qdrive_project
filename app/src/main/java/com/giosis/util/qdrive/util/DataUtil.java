package com.giosis.util.qdrive.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.list.RowItem;
import com.giosis.util.qdrive.message.AdminMessageListDetailActivity;
import com.giosis.util.qdrive.message.CustomerMessageListDetailActivity;
import com.giosis.util.qdrive.message.MessageListActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Hashtable;

public class DataUtil {

    public static String appID = "QDRIVE";
    public static String nationCode = "SG";
    public static String qDeliveryData = "QDATA";

    public static String SERVER_TEST = "https://test-api.qxpress.net";
    public static String SERVER_STAGING = "http://staging-qxapi.qxpress.net";
    public static String SERVER_REAL = "https://qxapi.qxpress.net";
    public static String API_ADDRESS = "/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi";


    public static String qrcode_url = "https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code=";
    public static String barcode_url = "http://image.qxpress.net/code128/code128.php?no=";
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

    public static void logEvent(String event, String activity, String method) {

        try {

            Bundle params = new Bundle();
            params.putString("Activity", activity);
            params.putString("method", "SetDeliveryUploadData/SetPickupUploadData");
            mFirebaseAnalytics.logEvent("button_click", params);
        } catch (Exception ignored) {
        }
    }


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

            Log.e("Location", "Stop GPS");
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

        String imagePath = "";

        try {
            File outputDir = MyApplication.getContext().getCacheDir();
            File tempFile = File.createTempFile("temp", ".jpg", outputDir);

            if (tempFile != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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

                imagePath = ImageUpload.INSTANCE.upload(tempFile);
            }

        } catch (Exception e) {

        }

        return imagePath;
    }


    public static Bitmap stringToDataMatrix(String scan_no) {

        Bitmap bitmap = null;
        MultiFormatWriter gen = new MultiFormatWriter();

        try {

            final int WIDTH = 200;
            final int HEIGHT = 200;

            Hashtable<EncodeHintType, Object> hints = new Hashtable<>(1);
            hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);

            BitMatrix bytemap = gen.encode(scan_no, BarcodeFormat.DATA_MATRIX, WIDTH, HEIGHT, hints);
            bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < WIDTH; ++i) {
                for (int j = 0; j < HEIGHT; ++j) {

                    bitmap.setPixel(i, j, bytemap.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (Exception e) {

            Log.e("print", "stringToDataMatrix  MultiFormatWriter Exception  : " + e.toString());
        }


        return bitmap;
    }


    // Sort
    class CompareZipCodeAsc implements Comparator<RowItem> {

        @Override
        public int compare(RowItem o1, RowItem o2) {

            return o1.getZip_code().compareTo(o2.getZip_code());
        }
    }

    class CompareZipCodeDesc implements Comparator<RowItem> {

        @Override
        public int compare(RowItem o1, RowItem o2) {

            return o2.getZip_code().compareTo(o1.getZip_code());
        }
    }

    private String[] orderbyQuery = {
            "zip_code asc",
            "zip_code desc",
            "invoice_no asc",
            "invoice_no desc",
            "rcv_nm asc",
            "rcv_nm desc"
            , "Smart Route"
    };

    class CompareRowItem implements Comparator<RowItem> {

        String orderBy;

        public CompareRowItem(String orderBy) {

            this.orderBy = orderBy;
        }

        @Override
        public int compare(RowItem o1, RowItem o2) {

            if (orderBy.equals(orderbyQuery[0])) {
                return o1.getZip_code().compareTo(o2.getZip_code());
            } else if (orderBy.equals(orderbyQuery[1])) {
                return o2.getZip_code().compareTo(o1.getZip_code());
            } else if (orderBy.equals(orderbyQuery[2])) {
                return o1.getShipping().compareTo(o2.getShipping());
            } else if (orderBy.equals(orderbyQuery[3])) {
                return o2.getShipping().compareTo(o1.getShipping());
            } else if (orderBy.equals(orderbyQuery[4])) {
                return o1.getName().compareTo(o2.getName());
            } else if (orderBy.equals(orderbyQuery[5])) {
                return o2.getName().compareTo(o1.getName());
            }

            return o1.getZip_code().compareTo(o2.getZip_code());
        }
    }
}