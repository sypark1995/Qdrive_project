package com.giosis.util.qdrive.list.delivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.international.OnServerEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DeliveryReturnFailedUploadHelper extends ManualHelper {
    private String TAG = "DeliveryReturnedVisitLogUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String shippingNo;
    private final ImageView imageView;
    private final String driverMemo;
    private final String receiveType;

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

        private final String shippingNo;
        private final ImageView imageView;
        private final String driverMemo;
        private final String receiveType;

        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String shippingNo, ImageView imageView, String driverMemo, String receiveType,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.shippingNo = shippingNo;
            this.imageView = imageView;
            this.driverMemo = driverMemo;
            this.receiveType = receiveType;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
        }

        public DeliveryReturnFailedUploadHelper build() {
            return new DeliveryReturnFailedUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private DeliveryReturnFailedUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.shippingNo = builder.shippingNo;
        this.imageView = builder.imageView;
        this.driverMemo = builder.driverMemo;
        this.receiveType = builder.receiveType;

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
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

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


    class ReturnFailUploadTask extends AsyncTask<Void, Integer, StdResult> {
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

            StdResult result = requestReturnedUpload(shippingNo);
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


            try {

                int resultCode = result.getResultCode();
                String resultMsg = result.getResultMsg();
                String fail_reason = "";

                if (resultCode < 0) {

                    if (resultCode == -14) {
                        fail_reason += context.getResources().getString(R.string.msg_upload_fail_14);
                    } else if (resultCode == -15) {
                        fail_reason += context.getResources().getString(R.string.msg_upload_fail_15);
                    } else {
                        //   fail_reason += String.format(context.getResources().getString(R.string.msg_upload_fail_etc), resultCode);
                        fail_reason += resultMsg;
                    }

                    if (resultCode == -16) {

                        showResultDialog(fail_reason);
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), 0, 1, fail_reason);
                        showResultDialog(msg);
                    }
                } else {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), 1);
                    showResultDialog(msg);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestReturnedUpload(String assignNo) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", "");
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("driver_memo", driverMemo);

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{assignNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

            try {

                imageView.buildDrawingCache();
                Bitmap captureView = imageView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView, ImageUpload.QXPOD, "qdriver/sign", assignNo);

                JSONObject job = new JSONObject();

                job.accumulate("rcv_type", receiveType);
                job.accumulate("stat", "RF");
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("fileData", bitmapString);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("remark", driverMemo);       // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("del_channel", "QDRIVE");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "setDeliveryRTNDPTypeUploadData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultCode":-32,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int resultCode = jsonObject.getInt("ResultCode");
                result.setResultCode(resultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (resultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{assignNo, opID});
                } else {

                    updateReceiverSign(assignNo, driverMemo);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  setDeliveryRTNDPTypeUploadData Exception : " + e.toString());

                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public DeliveryReturnFailedUploadHelper execute() {
        ReturnFailUploadTask returnFailUploadTask = new ReturnFailUploadTask();
        returnFailUploadTask.execute();
        return this;
    }


    private void updateReceiverSign(String invoiceNo, String driverMemo) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();


        ContentValues contentVal = new ContentValues();
        contentVal.put("stat", "RF");
        contentVal.put("chg_id", opID);
        contentVal.put("chg_dt", dateFormat.format(date));
        contentVal.put("real_qty", "0");        // 업로드시 값 Parse 시 에러나서 0 넘김
        contentVal.put("driver_memo", driverMemo);
        contentVal.put("retry_dt", "");
        contentVal.put("rev_type", "VL");
        contentVal.put("punchOut_stat", "S");

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                "invoice_no=? COLLATE NOCASE and punchOut_stat <> 'S' " + "and reg_id = ?", new String[]{invoiceNo, opID});
    }
}