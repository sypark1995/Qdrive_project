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
import android.widget.ImageView;

import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

// 일반 픽업 - Visit Log
// CnR건 - Visit Log
public class PickupFailedUploadHelper {
    String TAG = "PickupFailedUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String rcvType;
    private final String pickupNo;
    private final String failedCode;
    private final String retryDay;
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

        private final String rcvType;
        private final String pickupNo;
        private final String failedCode;
        private final String retryDay;
        private final String driverMemo;
        private final ImageView imageView;

        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String rcvType, String pickupNo, String failedCode, String retryDay, String driverMemo, ImageView imageView,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.rcvType = rcvType;
            this.pickupNo = pickupNo;
            this.failedCode = failedCode;
            this.retryDay = retryDay;
            this.driverMemo = driverMemo;
            this.imageView = imageView;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
        }

        public PickupFailedUploadHelper build() {
            return new PickupFailedUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private PickupFailedUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.rcvType = builder.rcvType;
        this.pickupNo = builder.pickupNo;
        this.failedCode = builder.failedCode;
        this.retryDay = builder.retryDay;
        this.driverMemo = builder.driverMemo;
        this.imageView = builder.imageView;

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

            StdResult result = requestPickupUpload(pickupNo);
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
                    if (resultCode == -14) {
                        fail_reason += context.getResources().getString(R.string.msg_upload_fail_14);
                    } else if (resultCode == -15) {
                        fail_reason += context.getResources().getString(R.string.msg_upload_fail_15);
                    } else if (resultCode == -16) {
                        fail_reason += context.getResources().getString(R.string.msg_upload_fail_16);
                    } else {
                        fail_reason += resultMsg;
                    }

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
                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), successCount, failCount, fail_reason);
                        showResultDialog(msg);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestPickupUpload(String assignNo) {

            DataUtil.captureSign("/QdrivePickup", assignNo, imageView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("rcv_type", rcvType);
            contentVal.put("stat", "PF");
            contentVal.put("real_qty", "0");
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", failedCode);
            contentVal.put("retry_dt", retryDay);
            contentVal.put("driver_memo", driverMemo);

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {
                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }


//            // TEST.  Upload Failed
//            if (true) {
//
//                result.setResultCode(-15);
//                result.setResultMsg("Exception : error - test");
//                return result;
//            }


            try {

                imageView.buildDrawingCache();
                Bitmap captureView = imageView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView, ImageUpload.QXPOP, "qdriver/sign", assignNo);

                if (bitmapString.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", rcvType);        // VL, RC
                job.accumulate("stat", "PF");
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)"); // 내부관리자용 메세지
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", "");
                job.accumulate("remark", driverMemo);                      // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("real_qty", "0");
                job.accumulate("fail_reason", failedCode);
                job.accumulate("retry_day", retryDay);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);
                Log.e("Server", "  SetPickupUploadData DATA : " + rcvType + " / " + assignNo + " / " + lat + ", " + lon);

                String methodName = "SetPickupUploadData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"SUCCESS"}
                // {"ResultCode":-11,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int resultCode = jsonObject.getInt("ResultCode");

                result.setResultCode(resultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (resultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});
                }
            } catch (Exception e) {

                result.setResultCode(-15);
                result.setResultMsg("Exception : " + e.toString());
            }

            return result;
        }
    }


    public PickupFailedUploadHelper execute() {
        PickupUploadTask pickupUploadTask = new PickupUploadTask();
        pickupUploadTask.execute();
        return this;
    }
}