package com.giosis.util.qdrive.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

public class PickupAddScanUploadHelper extends ManualHelper {
    String TAG = "PickupAddScanUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String pickup_no;
    private final String scanned_str;
    private final String scannedQty;
    private final SigningView signingView;
    private final SigningView collectorSigningView;

    private final long disk_size;
    private final double lat;
    private final double lon;

    private final String networkType;
    private final OnServerEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

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

        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerEventListener eventListener;


        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickup_no, String scanned_str, String scannedQty, SigningView signingView, SigningView collectorSigningView,
                       long disk_size, double lat, double lon) {

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

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
        }

        public PickupAddScanUploadHelper build() {
            return new PickupAddScanUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private PickupAddScanUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.pickup_no = builder.pickup_no;
        this.scanned_str = builder.scanned_str;
        this.scannedQty = builder.scannedQty;
        this.signingView = builder.signingView;
        this.collectorSigningView = builder.collectorSigningView;

        this.disk_size = builder.disk_size;
        this.lat = builder.lat;
        this.lon = builder.lon;

        this.networkType = builder.networkType;
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


    class PickupAddScanTask extends AsyncTask<Void, Integer, StdResult> {

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

            StdResult result = requestPickupUpload(pickup_no);
            publishProgress(1);

            return result;
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

            DisplayUtil.dismissProgressDialog(progressDialog);

            int successCount = 0;
            int failCount = 0;
            String fail_reason = "";

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            try {

                if (resultCode < 0) {

                    fail_reason += resultMsg;
                    failCount++;
                } else {

                    successCount++;
                }

                if (successCount > 0 && failCount == 0) {
                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showResultDialog(msg);
                } else {

                    if (resultCode == -16) {
                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved));
                    } else {
                        AlertShow(fail_reason);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestPickupUpload(String pickup_no) {

            String[] scannedItems = scanned_str.split(",");

            for (String scannedItem : scannedItems) {

                DataUtil.captureSign("/QdrivePickup", scannedItem, signingView);
                DataUtil.captureSign("/QdriveCollector", scannedItem, collectorSigningView);
            }


            StdResult result = new StdResult();

            try {

                signingView.buildDrawingCache();
                collectorSigningView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                Bitmap captureView2 = collectorSigningView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView);
                String bitmapString2 = DataUtil.bitmapToString(captureView2);

                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "RC");
                job.accumulate("stat", "P3");
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", pickup_no);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString2);
                job.accumulate("remark", "");
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("real_qty", scannedQty);
                job.accumulate("scanned_str", scanned_str);
                job.accumulate("fail_reason", "");
                job.accumulate("retry_day", "");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "SetPickupUploadData_AddScan";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultCode":0,"ResultMsg":"Success"}
                // {"ResultCode":-11,"ResultMsg":"Failed"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetPickupUploadData_AddScan Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public PickupAddScanUploadHelper execute() {
        PickupAddScanTask pickupAddScanTask = new PickupAddScanTask();
        pickupAddScanTask.execute();
        return this;
    }
}