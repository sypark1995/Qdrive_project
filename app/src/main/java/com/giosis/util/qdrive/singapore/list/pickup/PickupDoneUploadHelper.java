package com.giosis.util.qdrive.singapore.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.gps.GpsUpdateDialog;
import com.giosis.util.qdrive.singapore.gps.LocationModel;
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
import java.util.Date;

// Pickup - Start to Scan
@Deprecated // TODO_sypark ver 3.9.0  부터 사용 안함 추후 삭제
public class PickupDoneUploadHelper {
    String TAG = "PickupDoneUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String pickup_no;
    private final String scanned_str;
    private final String scannedQty;
    private final SigningView signingView;
    private final SigningView collectorSigningView;
    private final String driverMemo;

    private final long disk_size;
    private final LocationModel locationModel;

    private final OnServerEventListener eventListener;
    private final String networkType;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;
    boolean gpsUpdate = false;

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_set_transfer));
        progressDialog.setCancelable(false);
        return progressDialog;
    }


    private PickupDoneUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.pickup_no = builder.pickup_no;
        this.scanned_str = builder.scanned_str;
        this.scannedQty = builder.scannedQty;
        this.signingView = builder.signingView;
        this.collectorSigningView = builder.collectorSigningView;
        this.driverMemo = builder.driverMemo;

        this.disk_size = builder.disk_size;
        this.locationModel = builder.locationModel;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context)
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

                    if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {   // MY,ID
                        if (locationModel.getDriverLat() != 0 && locationModel.getDriverLng() != 0
                                && locationModel.getParcelLat() != 0 && locationModel.getParcelLng() != 0) {
                            // Parcel & Driver 위치정보 수집 했을 때      (0일 경우 제외)
                            //   Log.e("GpsUpdate", "DATA : " + locationModel.getDifferenceLat() + " / " + locationModel.getDifferenceLng());
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

        return dialog;
    }

    private void showResultDialog(String message) {

        resultDialog.setMessage(message);
        resultDialog.show();
    }

    private void AlertShow(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(context);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_upload_failed));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                (dialog, which) -> {
                    dialog.dismiss(); // 닫기
                });
        alert_internet_status.show();
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final String pickup_no;
        private final String scanned_str;
        private final String scannedQty;
        private final SigningView signingView;
        private final SigningView collectorSigningView;
        private final String driverMemo;

        private final long disk_size;
        private final LocationModel locationModel;

        private String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickup_no, String scanned_str, String scannedQty, SigningView signingView, SigningView collectorSigningView, String driverMemo,
                       long disk_size, LocationModel locationModel) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.pickup_no = pickup_no;
            this.scanned_str = scanned_str;
            this.scannedQty = scannedQty;
            this.signingView = signingView;
            this.collectorSigningView = collectorSigningView;
            this.driverMemo = driverMemo;

            this.disk_size = disk_size;
            this.locationModel = locationModel;
        }

        public PickupDoneUploadHelper build() {
            return new PickupDoneUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    class PickupUploadTask extends AsyncTask<Void, Integer, StdResult> {

        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {

                progressDialog.setMax(1);
                progressDialog.show();
            }
        }

        @Override
        protected StdResult doInBackground(Void... params) {

            StdResult stdResult = requestPickupUpload(pickup_no);
            publishProgress(1);
            return stdResult;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress += values[0];
            progressDialog.setProgress(progress);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                int resultCode = result.getResultCode();

                if (resultCode < 0) {
                    if (resultCode == -16) {

                        showResultDialog(result.getResultMsg());
                    } else {

                        AlertShow(result.getResultMsg());
                    }
                } else {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), 1);
                    showResultDialog(msg);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestPickupUpload(String pickup_no) {

            DataUtil.capture("/QdrivePickup", pickup_no, signingView);
            DataUtil.capture("/QdriveCollector", pickup_no, collectorSigningView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String changeDataString = dateFormat.format(date);

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", StatueType.PICKUP_DONE);
            contentVal.put("real_qty", scannedQty);
            contentVal.put("fail_reason", "");
            contentVal.put("driver_memo", driverMemo);
            contentVal.put("retry_dt", "");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{pickup_no, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

//              // TEST_Upload Failed 시 주석 풀고 실행
//            if (true) {
//
//                result.setResultCode(-15);
//                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
//                return result;
//            }

            try {

                signingView.buildDrawingCache();
                collectorSigningView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                Bitmap captureView2 = collectorSigningView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(context, captureView, ImageUpload.QXPOP, "qdriver/sign", pickup_no);
                String bitmapString2 = DataUtil.bitmapToString(context, captureView2, ImageUpload.QXPOP, "qdriver/sign", pickup_no);

                if (bitmapString.equals("") || bitmapString2.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "SC");
                job.accumulate("stat", StatueType.PICKUP_DONE);
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", pickup_no);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString2);
                job.accumulate("remark", driverMemo);           // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", locationModel.getDriverLat());
                job.accumulate("lon", locationModel.getDriverLng());
                job.accumulate("real_qty", scannedQty);         // 실제픽업수량
                job.accumulate("fail_reason", "");
                job.accumulate("retry_day", "");
                job.accumulate("scanned_str", scanned_str);     // scanned list  Q1234, Q5678, Q1111
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "SetPickupUploadData_ScanAll";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"Success"}
                // {"ResultCode":-11,"ResultMsg":"Failed"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int resultCode = jsonObject.getInt("ResultCode");
                result.setResultCode(resultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (resultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");
                    contentVal2.put("chg_dt", changeDataString);

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{pickup_no, opID});


                    // CnR, Lazada Data Scan시 함께 Done 처리
                    String[] scannedList = scanned_str.split(",");
                    for (String s : scannedList) {

                        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + s + "' COLLATE NOCASE");

                        if (cursor != null && cursor.moveToFirst()) {

                            ContentValues contentVal3 = new ContentValues();
                            contentVal3.put("stat", StatueType.PICKUP_DONE);
                            contentVal3.put("real_qty", "1");
                            contentVal3.put("chg_dt", dateFormat.format(date));
                            contentVal3.put("fail_reason", "");
                            contentVal3.put("retry_dt", "");
                            contentVal3.put("driver_memo", driverMemo);
                            contentVal3.put("reg_id", opID);
                            contentVal3.put("punchOut_stat", "S");

                            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal3,
                                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{s, opID});
                        }

                        cursor.close();
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetPickupUploadData_ScanAll Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public PickupDoneUploadHelper execute() {
        PickupUploadTask serverUploadTask = new PickupUploadTask();
        serverUploadTask.execute();
        return this;
    }
}