package com.giosis.library.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.StdResult;
import com.giosis.library.list.SigningView;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.server.ImageUpload;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.OnServerEventListener;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


// Pickup - Zero Qty
public class PickupZeroQtyUploadHelper {
    String TAG = "PickupZeroQtyUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String pickupNo;
    private final SigningView signingView;
    private final SigningView collectorSigningView;
    private final String driverMemo;

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

        private final String pickupNo;
        private final SigningView signingView;
        private final SigningView collectorSigningView;
        private final String driverMemo;

        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickupNo, SigningView signingView, SigningView collectorSigningView, String driverMemo,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.pickupNo = pickupNo;
            this.signingView = signingView;
            this.collectorSigningView = collectorSigningView;
            this.driverMemo = driverMemo;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public PickupZeroQtyUploadHelper build() {
            return new PickupZeroQtyUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private PickupZeroQtyUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.pickupNo = builder.pickupNo;
        this.signingView = builder.signingView;
        this.collectorSigningView = builder.collectorSigningView;
        this.driverMemo = builder.driverMemo;

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

            StdResult stdResult = requestPickupUpload(pickupNo);
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

            DisplayUtil.dismissProgressDialog(progressDialog);

            StringBuilder fail_reason = new StringBuilder();

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            try {

                if (resultCode < 0) {

                    if (resultCode == -14) {
                        fail_reason.append(context.getResources().getString(R.string.msg_upload_fail_14));
                    } else if (resultCode == -15) {
                        fail_reason.append(context.getResources().getString(R.string.msg_upload_fail_15));
                    } else if (resultCode == -16) {
                        fail_reason.append(context.getResources().getString(R.string.msg_upload_fail_16));
                    } else {
                        //   fail_reason += String.format(context.getResources().getString(R.string.msg_upload_fail_etc), resultCode);
                        fail_reason.append(resultMsg);
                    }

                    if (resultCode == -16) {
                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved));
                    } else {
                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), 0, 1, fail_reason.toString());
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


        private StdResult requestPickupUpload(String pickupNo) {

            DataUtil.captureSign("/QdrivePickup", pickupNo, signingView);
            DataUtil.captureSign("/QdriveCollector", pickupNo, collectorSigningView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String changeDataString = dateFormat.format(date);

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", "P3");
            contentVal.put("real_qty", "0");
            contentVal.put("rcv_type", "ZQ");
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", changeDataString);
            contentVal.put("driver_memo", driverMemo);
            contentVal.put("fail_reason", "");
            contentVal.put("retry_dt", "");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{pickupNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg("");
                return result;
            }

           /* // TEST.   Upload Failed
            if (true) {

                result.setResultCode(-15);
                result.setResultMsg("");
                return result;
            }*/

            try {

                signingView.buildDrawingCache();
                collectorSigningView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                Bitmap captureView2 = collectorSigningView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(context, captureView, ImageUpload.QXPOP, "qdriver/sign", pickupNo);
                String bitmapString2 = DataUtil.bitmapToString(context, captureView2, ImageUpload.QXPOP, "qdriver/sign", pickupNo);

                if (bitmapString.equals("") || bitmapString2.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "ZQ");        // ZQ : Zero Qty
                job.accumulate("stat", "P3");            // P3 : Pickup Done
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", pickupNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString2);
                job.accumulate("remark", driverMemo);
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("real_qty", "0");        // 실제픽업수량
                job.accumulate("fail_reason", "");
                job.accumulate("retry_day", "");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

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
                            "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{pickupNo, opID});
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetPickupUploadData Exception : " + e.toString());
                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }
    }


    public PickupZeroQtyUploadHelper execute() {
        PickupUploadTask pickupUploadTask = new PickupUploadTask();
        pickupUploadTask.execute();
        return this;
    }
}