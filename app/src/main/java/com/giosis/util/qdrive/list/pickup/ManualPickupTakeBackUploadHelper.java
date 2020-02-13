package com.giosis.util.qdrive.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ManualPickupTakeBackUploadHelper extends ManualHelper {
    String TAG = "ManualPickupTakeBackUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final String networkType;

    private final String pickup_no;
    private final String scannedBarcode;
    private final int realQty;
    private final SigningView signingView;
    private final SigningView collectorSigningView;

    private final long disk_size;
    private final double lat;
    private final double lon;

    private final OnServerEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;


    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private String networkType;

        private final String pickup_no;
        private final String scannedBarcode;
        private final int realQty;
        private final SigningView signingView;
        private final SigningView collectorSigningView;

        private final long disk_size;
        private final double lat;
        private final double lon;

        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickup_no, String scannedBarcode, int realQty, SigningView signingView, SigningView collectorSigningView,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.pickup_no = pickup_no;
            this.scannedBarcode = scannedBarcode;
            this.realQty = realQty;
            this.signingView = signingView;
            this.collectorSigningView = collectorSigningView;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
        }

        public ManualPickupTakeBackUploadHelper build() {
            return new ManualPickupTakeBackUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualPickupTakeBackUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.networkType = builder.networkType;

        this.pickup_no = builder.pickup_no;
        this.scannedBarcode = builder.scannedBarcode;
        this.realQty = builder.realQty;
        this.signingView = builder.signingView;
        this.collectorSigningView = builder.collectorSigningView;

        this.disk_size = builder.disk_size;
        this.lat = builder.lat;
        this.lon = builder.lon;

        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_set_transfer));
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();

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
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 닫기
                    }
                });
        alert_internet_status.show();
    }


    class TakeBackUploadTask extends AsyncTask<Void, Integer, StdResult> {

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
            super.onProgressUpdate(values);
            progress += values[0];
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    DisplayUtil.dismissProgressDialog(progressDialog);
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }

            int resultCode = result.getResultCode();

            try {

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
                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                showResultDialog(msg);
            }
        }


        private StdResult requestPickupUpload(String pickup_no) {

            DataUtil.captureSign("/QdrivePickup", pickup_no, signingView);
            DataUtil.captureSign("/QdriveCollector", pickup_no, collectorSigningView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String changeDataString = dateFormat.format(date);

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", "P3");
            contentVal.put("real_qty", realQty);
            contentVal.put("chg_id", opID);
            contentVal.put("fail_reason", "");
            contentVal.put("driver_memo", "");
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

            try {

                signingView.buildDrawingCache();
                collectorSigningView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                Bitmap captureView2 = collectorSigningView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView);
                String bitmapString2 = DataUtil.bitmapToString(captureView2);

                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "SC_TAKEBACK");
                job.accumulate("pickup_no", pickup_no);
                job.accumulate("scan_nos", scannedBarcode);                // scanned list  Q1234, Q5678, Q1111
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString2);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-TakeBack)"); // 내부관리자용 메세지
                job.accumulate("remark", "");
                job.accumulate("op_id", opID);
                job.accumulate("office_code", officeCode);
                job.accumulate("chg_id", opID);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "SetPickupUploadData_TakeBack";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultCode":10,"ResultMsg":"Success"}
                // {"ResultCode":-1,"ResultMsg":"Scanned number is too short"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int ResultCode = jsonObject.getInt("ResultCode");
                result.setResultCode(ResultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (ResultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");
                    contentVal2.put("chg_dt", changeDataString);

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{pickup_no, opID});
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetPickupUploadData_TakeBack Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }

    public ManualPickupTakeBackUploadHelper execute() {
        TakeBackUploadTask serverUploadTask = new TakeBackUploadTask();
        serverUploadTask.execute();
        return this;
    }
}