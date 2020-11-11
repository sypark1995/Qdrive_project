package com.giosis.library.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

public class DataUtil {

    public static String appID = "QDRIVE";

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


    //
    public static int inProgressListPosition = 0;
    public static int uploadFailedListPosition = 0;


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

}