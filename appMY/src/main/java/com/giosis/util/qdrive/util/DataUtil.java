package com.giosis.util.qdrive.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.util.qdrive.international.R;

public class DataUtil {

    public static String appID = "QDRIVE";

    public static String SERVER_TEST = "https://test-api.qxpress.net";
    public static String SERVER_STAGING = "http://staging-qxapi.qxpress.net";
    public static String SERVER_REAL = "https://qxapi.qxpress.net";
    public static String API_ADDRESS = "/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi";

    public static String qrcode_url = "https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code=";
    public static String barcode_url = "http://image.qxpress.net/code128/code128.php?no=";
    public static String locker_pin_url = "https://www.lockeralliance.net/pin";


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

        if (gpsTrackerManager != null)
            gpsTrackerManager.stopFusedProviderService();
    }


}