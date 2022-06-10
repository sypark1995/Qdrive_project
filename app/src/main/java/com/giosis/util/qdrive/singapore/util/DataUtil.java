package com.giosis.util.qdrive.singapore.util;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.data.CaptureData;
import com.giosis.util.qdrive.singapore.data.FailedCodeData;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.server.ImageUpload;
import com.giosis.util.qdrive.singapore.server.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DataUtil {

    public static String appID = "QDRIVE";

    public static String SERVER_LOCAL = "http://kwher-dev.qxpress.net";
    public static String SERVER_TEST = "https://test-api.qxpress.net";
    public static String SERVER_STAGING = "http://staging-qxapi.qxpress.net";
    public static String SERVER_REAL = "https://qxapi.qxpress.net";

    public static String API_ADDRESS = "/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi/";
    public static String API_ADDRESS_MOBILE_SERVICE = "/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi/";

    public static String XROUTE_SERVER_STAGING = "http://211.115.100.24/api/";
    public static String XROUTE_SERVER_REAL = "http://xrouter.qxpress.net/api/";

    public static String qrcode_url = "https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code=";
    public static String locker_pin_url = "https://www.lockeralliance.net/pin";


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

                RetrofitClient.INSTANCE.instanceMobileService()
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
                RetrofitClient.INSTANCE.instanceMobileService()
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

            RetrofitClient.INSTANCE.instanceMobileService()
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
            } catch (Exception ignore) {

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

    public static ArrayList<FailedCodeData> pickupFailedList = null;
    public static ArrayList<FailedCodeData> deliveryFailedList = null;

    public static ArrayList<FailedCodeData> getFailCode(String type) {

        if (type.equals("D")) {
            if (deliveryFailedList == null) {
                String json = Preferences.INSTANCE.getDFailedCode();
                deliveryFailedList = new Gson().fromJson(json,
                        new TypeToken<ArrayList<FailedCodeData>>() {
                        }.getType());
            }
            return deliveryFailedList;

        } else {
            if (pickupFailedList == null) {
                String json = Preferences.INSTANCE.getPFailedCode();
                pickupFailedList = new Gson().fromJson(json,
                        new TypeToken<ArrayList<FailedCodeData>>() {
                        }.getType());
            }

            return pickupFailedList;
        }

    }


    public static String getDeliveryFailedMsg(String code) {
        if (deliveryFailedList == null) {
            deliveryFailedList = getFailCode("D");
        }

        String reasonText = "";
        if (deliveryFailedList != null) {

            for (int i = 0; i < deliveryFailedList.size(); i++) {

                FailedCodeData failedCode = deliveryFailedList.get(i);

                if (failedCode.getFailedCode().equals(code)) {
                    reasonText = failedCode.getFailedString();
                }
            }
        }
        return reasonText;
    }


    public static String getPickupFailedMsg(String code) {
        if (pickupFailedList == null) {
            pickupFailedList = DataUtil.getFailCode("P");
        }

        String reasonText = "";
        if (pickupFailedList != null) {

            for (int i = 0; i < pickupFailedList.size(); i++) {

                FailedCodeData failedCode = pickupFailedList.get(i);

                if (failedCode.getFailedCode().equals(code)) {
                    reasonText = failedCode.getFailedString();
                }
            }
        }
        return reasonText;
    }


    public static int getContrNoCount(String contr_no) {

        String sql = "SELECT count(*) as contrno_cnt FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE contr_no='" + contr_no + "' COLLATE NOCASE";
        Cursor cursor = DatabaseHelper.getInstance().get(sql);

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("contrno_cnt"));
        }

        cursor.close();

        return count;
    }

    public static void deleteContrNo(String contr_no) {

        DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "contr_no='" + contr_no + "' COLLATE NOCASE");
    }

    public static boolean insertDriverAssignInfo(CaptureData data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        String contr_no = data.getContr_no();
        int cnt = getContrNoCount(contr_no);
        if (cnt > 0) {
            deleteContrNo(contr_no);
        }

        //성공 시 통합리스트 테이블 저장
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", data.getContr_no());
        contentVal.put("partner_ref_no", data.getPartner_ref_no());
        contentVal.put("invoice_no", data.getInvoice_no());
        contentVal.put("stat", data.getStat());
        contentVal.put("rcv_nm", data.getRcv_nm());
        contentVal.put("sender_nm", data.getSender_nm());
        contentVal.put("tel_no", data.getTel_no());
        contentVal.put("hp_no", data.getHp_no());
        contentVal.put("zip_code", data.getZip_code());
        contentVal.put("address", data.getAddress());
        contentVal.put("rcv_request", data.getDel_memo());
        contentVal.put("delivery_dt", data.getDelivery_first_date());
        contentVal.put("type", StatueType.TYPE_DELIVERY);
        contentVal.put("route", data.getRoute());
        contentVal.put("reg_id", Preferences.INSTANCE.getUserId());
        contentVal.put("reg_dt", regDataString);
        contentVal.put("punchOut_stat", "N");
        contentVal.put("driver_memo", data.getDriver_memo());
        contentVal.put("fail_reason", data.getFail_reason());
        contentVal.put("secret_no_type", data.getSecret_no_type());
        contentVal.put("secret_no", data.getSecret_no());
        contentVal.put("secure_delivery_yn", data.getSecure_delivery_yn());
        contentVal.put("parcel_amount", data.getParcel_amount());
        contentVal.put("currency", data.getCurrency());
        contentVal.put("order_type_etc", data.getOrder_type_etc());
        String[] latLng = GeoCodeUtil.getLatLng(data.getLat_lng());
        contentVal.put("lat", latLng[0]);
        contentVal.put("lng", latLng[1]);
        contentVal.put("high_amount_yn", data.getHigh_amount_yn());
        contentVal.put("order_type", data.getOrder_type());
        contentVal.put("state", data.getReceive_state());
        contentVal.put("city", data.getReceive_city());
        contentVal.put("street", data.getReceive_street());

        long insertCount = DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
        return insertCount >= 0;
    }
}