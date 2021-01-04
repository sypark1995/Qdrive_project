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

import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.international.OnServerEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

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

    private final String pickupStat;
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

        private final String pickupNo;
        private final SigningView signingView;
        private final SigningView collectorSigningView;
        private final String driverMemo;

        private final String pickupStat;
        private final String receiveType;
        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickupNo, SigningView signingView, SigningView collectorSigningView, String driverMemo,
                       String pickupStat, String receiveType, long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.pickupNo = pickupNo;
            this.signingView = signingView;
            this.collectorSigningView = collectorSigningView;
            this.driverMemo = driverMemo;

            this.pickupStat = pickupStat;
            this.receiveType = receiveType;
            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
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

        this.pickupStat = builder.pickupStat;
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

            try {

                int resultCode = result.getResultCode();

                if (resultCode < 0) {

                    if (resultCode == -14) {

                        result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_14));
                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), 0, 1, result.getResultMsg());
                        showResultDialog(msg);
                    } else if (resultCode == -15) {

                        result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), 0, 1, result.getResultMsg());
                        showResultDialog(msg);
                    } else if (resultCode == -16) {

                        showResultDialog(result.getResultMsg());
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count1), 1, result.getResultMsg());
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

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", pickupStat);
            contentVal.put("real_qty", "0");
            contentVal.put("rcv_type", receiveType);
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("driver_memo", driverMemo);
            contentVal.put("fail_reason", "");
            contentVal.put("retry_dt", "");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{pickupNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

           /*// TEST   Upload Failed
            if (true) {

                result.setResultCode(-15);
                result.setResultMsg("");
                return result;
            }*/

            try {

                signingView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                Bitmap captureView2 = collectorSigningView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView, ImageUpload.QXPOP, "qdriver/sign", pickupNo);
                String bitmapString2 = DataUtil.bitmapToString(captureView2, ImageUpload.QXPOP, "qdriver/sign", pickupNo);

                if (bitmapString.equals("") || bitmapString2.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", receiveType);        // ZQ : Zero Qty
                job.accumulate("stat", pickupStat);             // P3 : Pickup Done
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
                job.accumulate("real_qty", "0");
                job.accumulate("fail_reason", "");
                job.accumulate("retry_day", "");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


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