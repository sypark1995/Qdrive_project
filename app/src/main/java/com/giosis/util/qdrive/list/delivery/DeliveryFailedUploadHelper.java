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
import android.widget.Toast;

import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.MyApplication;
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


/**
 * Delivery Visit Log
 */
public class DeliveryFailedUploadHelper {
    String TAG = "DeliveryFailedUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String trackingNo;
    private final String driverMemo;
    private final ImageView imageView;

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

        private final String trackingNo;
        private final String driverMemo;
        private final ImageView imageView;

        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String trackingNo, String driverMemo, ImageView imageView,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.trackingNo = trackingNo;
            this.driverMemo = driverMemo;
            this.imageView = imageView;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
        }

        public DeliveryFailedUploadHelper build() {
            return new DeliveryFailedUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }


    private DeliveryFailedUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.trackingNo = builder.trackingNo;
        this.driverMemo = builder.driverMemo;
        this.imageView = builder.imageView;

        this.disk_size = builder.disk_size;
        this.lat = builder.lat;
        this.lon = builder.lon;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(context);
        this.resultDialog = getResultAlertDialog(context);
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
                .setCancelable(false).setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

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

    class DeliveryFailUploadTask extends AsyncTask<Void, Integer, StdResult> {
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

            StdResult result = requestDeliveryUpload(trackingNo);
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

                if (resultCode < 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count1), 1, result.getResultMsg());
                    showResultDialog(msg);
                } else {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), 1);
                    showResultDialog(msg);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                Toast.makeText(context, "Error.\n" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }


        private StdResult requestDeliveryUpload(String assignNo) {

            DataUtil.captureSign("/QdriveFailed", trackingNo, imageView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", "DX");
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", "");
            contentVal.put("driver_memo", driverMemo);

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {  // Wifi, 3G,4G,LTE(Network) 연결이 모두 안될경우
                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

          /*  // TEST.  Upload Failed
            if (true) {

                result.setResultCode(-15);
                result.setResultMsg("Exception : error - test");
                return result;
            }*/


            try {

                imageView.buildDrawingCache();
                Bitmap captureView = imageView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView, ImageUpload.QXPOD, "qdriver/sign", assignNo);

                if (bitmapString.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "RC");
                job.accumulate("stat", "DX");
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("fileData", bitmapString);
                job.accumulate("delivery_photo_url", "");
                job.accumulate("no_songjang", assignNo);
                job.accumulate("remark", driverMemo);               // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("stat_reason", "");
                job.accumulate("del_channel", "QDRIVE");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(com.giosis.library.util.DataUtil.requestSetUploadDeliveryData, job);
                // {"ResultCode":0,"ResultMsg":"SUCCESS"}
                // {"ResultCode":-11,"ResultMsg":"Upload Failed."}

                JSONObject jsonObject = new JSONObject(jsonString);
                int ResultCode = jsonObject.getInt("ResultCode");
                result.setResultCode(ResultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (ResultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});
                } else if (ResultCode == -25) {

                    dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "invoice_no= '" + assignNo + "' COLLATE NOCASE");
                } else {

                    updateReceiverSign(assignNo, driverMemo);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  Upload Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public DeliveryFailedUploadHelper execute() {
        DeliveryFailUploadTask deliveryFailUploadTask = new DeliveryFailUploadTask();
        deliveryFailUploadTask.execute();
        return this;
    }

    // SQLite UPDATE
    private void updateReceiverSign(String invoiceNo, String driverMemo) {

//        String opId = SharedPreferencesHelper.getSigninOpID(context);
        String opId = MyApplication.preferences.getUserId();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        ContentValues contentVal = new ContentValues();
        contentVal.put("stat", "DX");
        contentVal.put("chg_id", opId);
        contentVal.put("chg_dt", dateFormat.format(date));
        contentVal.put("real_qty", "0");            // 업로드시 값 Parse 시 에러나서 0 넘김
        contentVal.put("fail_reason", "");
        contentVal.put("driver_memo", driverMemo);
        contentVal.put("retry_dt", "");
        contentVal.put("rev_type", "VL");
        contentVal.put("punchOut_stat", "S");

        DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                "partner_ref_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?", new String[]{invoiceNo, opId});
    }
}