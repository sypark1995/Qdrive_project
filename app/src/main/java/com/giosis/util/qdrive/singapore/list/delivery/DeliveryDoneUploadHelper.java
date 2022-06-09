package com.giosis.util.qdrive.singapore.list.delivery;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.gps.GpsUpdateDialog;
import com.giosis.util.qdrive.singapore.gps.LocationModel;
import com.giosis.util.qdrive.singapore.list.BarcodeData;
import com.giosis.util.qdrive.singapore.list.SigningView;
import com.giosis.util.qdrive.singapore.server.Custom_JsonParser;
import com.giosis.util.qdrive.singapore.server.ImageUpload;
import com.giosis.util.qdrive.singapore.util.StatueType;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.util.DisplayUtil;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.OnServerEventListener;
import com.giosis.util.qdrive.singapore.util.Preferences;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Deprecated
public class DeliveryDoneUploadHelper {
    String TAG = "DeliveryDoneUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final ArrayList<BarcodeData> assignBarcodeList;
    private final String receiveType;
    private final String driverMemo;

    private final SigningView signingView;
    private final boolean hasSignImage;
    private final ImageView imageView;
    private final boolean hasVisitImage;

    private final long disk_size;
    private final OnServerEventListener eventListener;

    LocationModel locationModel;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;
    int count = 0;
    boolean gpsUpdate = false;

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_set_transfer));
        progressDialog.setCancelable(false);
        return progressDialog;
    }


    private DeliveryDoneUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.assignBarcodeList = builder.assignBarcodeList;
        this.receiveType = builder.receiveType;
        this.driverMemo = builder.driverMemo;

        this.signingView = builder.signingView;
        this.hasSignImage = builder.hasSignImage;
        this.imageView = builder.imageView;
        this.hasVisitImage = builder.hasVisitImage;

        this.disk_size = builder.disk_size;
        this.locationModel = builder.locationModel;

        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        return new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {

                    try {
                        if (dialog1 != null)
                            dialog1.dismiss();
                    } catch (Exception e) {
                        Log.e("Exception", TAG + "getResultAlertDialog Exception : " + e.toString());
                    }

//                    if (eventListener != null) {
//                        eventListener.onPostResult();
//                    }

                    Log.e("GpsUpdate", "Count : " + count);
                    if (!Preferences.INSTANCE.getUserNation().equals("SG") && count == 1) {   // MY,ID && 단건

                        if (locationModel.getDriverLat() != 0 && locationModel.getDriverLng() != 0
                                && locationModel.getParcelLat() != 0 && locationModel.getParcelLng() != 0) {
                            // Parcel & Driver 위치정보 수집 했을 때      (0일 경우 제외)
                            //    Log.e("GpsUpdate", "DATA : " + locationModel.getDifferenceLat() + " / " + locationModel.getDifferenceLng());
                            if (locationModel.getDifferenceLat() < 0.05 && locationModel.getDifferenceLng() < 0.05) {
                                // 두 값의 차이가 0.05 이내의 범위일 경우     (0.05 이상이면 부정확)
                                // 소수점 이하 3까지만 비교       (값이 너무 작으면 빈번하게 호출됨)
                                gpsUpdate = 0.001 <= locationModel.getDifferenceLat() || 0.001 <= locationModel.getDifferenceLng();
                            }
                        }
                    }

                    if (gpsUpdate) {

                        GpsUpdateDialog gpsDialog = new GpsUpdateDialog(context, locationModel, eventListener);
                        gpsDialog.show();
                        gpsDialog.setCanceledOnTouchOutside(false);
                        Window window = gpsDialog.getWindow();
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    } else {

                        if (eventListener != null) {
                            eventListener.onPostResult();
                        }
                    }
                })
                .create();
    }

    private void showResultDialog(String message, int count) {

        this.count = count;
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final ArrayList<BarcodeData> assignBarcodeList;
        private final String receiveType;
        private final String driverMemo;

        private final SigningView signingView;
        private final boolean hasSignImage;
        private final ImageView imageView;
        private final boolean hasVisitImage;


        private final long disk_size;
        LocationModel locationModel;

        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<BarcodeData> assignBarcodeList, String receiveType, String driverMemo,
                       SigningView signingView, boolean hasSignImage, ImageView imageView, boolean hasVisitImage,
                       long disk_size, LocationModel locationModel) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.assignBarcodeList = assignBarcodeList;
            this.receiveType = receiveType;
            this.driverMemo = driverMemo;

            this.signingView = signingView;
            this.hasSignImage = hasSignImage;
            this.imageView = imageView;
            this.hasVisitImage = hasVisitImage;

            this.disk_size = disk_size;
            this.locationModel = locationModel;
        }

        public DeliveryDoneUploadHelper build() {
            return new DeliveryDoneUploadHelper(this);
        }

        public Builder setOnServerUploadEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    class DeliveryUploadTask extends AsyncTask<Void, Integer, ArrayList<StdResult>> {
        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {

                int maxCount = assignBarcodeList.size();
                progressDialog.setMax(maxCount);
                progressDialog.show();
            }
        }

        @Override
        protected ArrayList<StdResult> doInBackground(Void... params) {

            ArrayList<StdResult> resultList = new ArrayList<>();

            if (assignBarcodeList != null && assignBarcodeList.size() > 0) {
                for (BarcodeData assignData : assignBarcodeList) {

                    StdResult result = null;

                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        result = requestServerUpload(assignData.getBarcode());
                    }

                    resultList.add(result);
                    publishProgress(1);
                }
            }

            return resultList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progress += values[0];
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(ArrayList<StdResult> resultList) {
            super.onPostExecute(resultList);
            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                int successCount = 0;
                int failCount = 0;
                StringBuilder fail_reason = new StringBuilder();

                for (int i = 0; i < resultList.size(); i++) {

                    StdResult result = resultList.get(i);

                    if (result.getResultCode() < 0) {

                        fail_reason.append(result.getResultMsg());
                        failCount++;
                    } else {

                        successCount++;
                    }
                }

                if (0 < successCount && failCount == 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showResultDialog(msg, successCount);
                } else {

                    String msg;
                    if (0 < successCount) {
                        msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), successCount, failCount, fail_reason.toString());
                    } else {
                        msg = String.format(context.getResources().getString(R.string.text_upload_fail_count1), failCount, fail_reason.toString());
                    }
                    showResultDialog(msg, 0);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestServerUpload(String assignNo) {

            StdResult result = new StdResult();

            String bitmapString = "";
            String bitmapString1 = "";

            if (hasSignImage) {

                DataUtil.captureSign("/Qdrive", assignNo, signingView);

                signingView.buildDrawingCache();
                Bitmap signBitmap = signingView.getDrawingCache();
                bitmapString = DataUtil.bitmapToString(context, signBitmap, ImageUpload.QXPOD, "qdriver/sign", assignNo);

                if (bitmapString.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }
            }

            if (hasVisitImage) {

                DataUtil.captureSign("/Qdrive", assignNo + "_1", imageView);

                imageView.buildDrawingCache();
                Bitmap visitBitmap = imageView.getDrawingCache();
                bitmapString1 = DataUtil.bitmapToString(context, visitBitmap, ImageUpload.QXPOD, "qdriver/delivery", assignNo);

                if (bitmapString1.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }
            }

            Log.e(TAG, "sign DATA  :  " + bitmapString + " / " + bitmapString1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", StatueType.DELIVERY_DONE);
            contentVal.put("rcv_type", receiveType);
            contentVal.put("driver_memo", driverMemo);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", "");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});


            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

//            // TEST_Upload Failed 시 주석 풀고 실행
//            if (true) {
//
//                result.setResultCode(-15);
//                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
//
//                return result;
//            }

            try {

                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", receiveType);
                job.accumulate("stat", StatueType.DELIVERY_DONE);
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", NetworkUtil.getNetworkType(context));
                job.accumulate("no_songjang", assignNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("delivery_photo_url", bitmapString1);
                job.accumulate("remark", driverMemo);            // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", locationModel.getDriverLat());
                job.accumulate("lon", locationModel.getDriverLng());
                job.accumulate("stat_reason", "");
                job.accumulate("del_channel", "QR");        // 업로드 채널: Qsign Realtime
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());


                String jsonString = Custom_JsonParser.requestServerDataReturnJSON("SetDeliveryUploadData", job);
                // {"ResultCode":0,"ResultMsg":"SUCCESS"}
                // {"ResultCode":-11,"ResultMsg":"Upload Failed."}

                JSONObject jsonObject = new JSONObject(jsonString);
                int resultCode = jsonObject.getInt("ResultCode");
                result.setResultCode(resultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (resultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});
                } else if (resultCode == -25) {

                    dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "invoice_no= '" + assignNo + "' COLLATE NOCASE");
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  upload Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public DeliveryDoneUploadHelper execute() {
        DeliveryUploadTask deliveryUploadTask = new DeliveryUploadTask();
        deliveryUploadTask.execute();
        return this;
    }
}