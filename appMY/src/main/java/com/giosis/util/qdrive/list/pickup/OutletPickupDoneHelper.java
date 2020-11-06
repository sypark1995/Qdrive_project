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

public class OutletPickupDoneHelper extends ManualHelper {
    String TAG = "OutletPickupDoneHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String pickup_no;
    private final SigningView signingView;
    private final String driverMemo;
    private final String receiveType;
    private final String pickupStat;

    private final long disk_size;
    private final double lat;
    private final double lon;
    private final String scannedQty;
    private final String scanned_str;
    private final String outlet_type;

    private final String networkType;
    private final OnOutletDataUploadEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final String pickup_no;
        private final SigningView signingView;
        private final String driverMemo;
        private final String receiveType;
        private final String pickupStat;

        private final long disk_size;
        private final double lat;
        private final double lon;
        private final String scannedQty;
        private final String scanned_str;
        private final String outlet_type;

        private String networkType;
        private OnOutletDataUploadEventListener eventListener;


        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickup_no, SigningView signingView, String driverMemo, String receiveType, String pickupStat,
                       long disk_size, double lat, double lon, String scannedQty, String scanned_str, String outletType) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.pickup_no = pickup_no;
            this.signingView = signingView;
            this.driverMemo = driverMemo;
            this.receiveType = receiveType;
            this.pickupStat = pickupStat;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
            this.scannedQty = scannedQty;
            this.scanned_str = scanned_str;
            this.outlet_type = outletType;
        }

        public OutletPickupDoneHelper build() {
            return new OutletPickupDoneHelper(this);
        }

        Builder setOnOutletDataUploadEventListener(OnOutletDataUploadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private OutletPickupDoneHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.pickup_no = builder.pickup_no;
        this.signingView = builder.signingView;
        this.driverMemo = builder.driverMemo;
        this.receiveType = builder.receiveType;
        this.pickupStat = builder.pickupStat;

        this.disk_size = builder.disk_size;
        this.lat = builder.lat;
        this.lon = builder.lon;
        this.scannedQty = builder.scannedQty;
        this.scanned_str = builder.scanned_str;
        this.outlet_type = builder.outlet_type;

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


    class OutletPickupTask extends AsyncTask<Void, Integer, StdResult> {

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

            int resultCode = result.getResultCode();
            int successCount = 0;
            String fail_reason = "";

            try {
                if (resultCode < 0) {

                    fail_reason += result.getResultMsg();
                } else {

                    successCount++;
                }

                if (resultCode < 0) {
                    if (resultCode == -16) {

                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved));
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count2), fail_reason);
                        showResultDialog(msg);
                    }
                } else {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showResultDialog(msg);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestPickupUpload(String pickup_no) {

            if (!outlet_type.equals("FL")) {

                DataUtil.captureSign("/QdrivePickup", pickup_no, signingView);
                DataUtil.captureSign("/QdriveCollector", pickup_no, signingView);
            }


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String changeDataString = dateFormat.format(date);

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", pickupStat);
            contentVal.put("real_qty", scannedQty);
            contentVal.put("chg_id", opID);
            contentVal.put("fail_reason", "");
            contentVal.put("driver_memo", driverMemo);
            contentVal.put("retry_dt", "");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal, "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{pickup_no, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg("");
                return result;
            }

            try {

                String bitmapString = "";

                try {
                    if (!outlet_type.equals("FL")) {

                        signingView.buildDrawingCache();
                        Bitmap captureView = signingView.getDrawingCache();
                        bitmapString = DataUtil.bitmapToString(captureView);
                    }
                } catch (Exception e) {

                    bitmapString = "";
                }
                Log.e("krm0219", "Outlet Pickup  DATA > " + receiveType + " " + pickupStat + " " + opID + " " + bitmapString + " " + pickup_no + " " + outlet_type);

                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", receiveType);
                job.accumulate("stat", pickupStat);
                job.accumulate("chg_id", opID);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)"); // 내부관리자용 메세지
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", pickup_no);
                job.accumulate("remark", driverMemo);           // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("real_qty", scannedQty);
                job.accumulate("fail_reason", "");
                job.accumulate("retry_day", "");
                job.accumulate("scanned_str", scanned_str);
                job.accumulate("outlet_type", outlet_type);     //  Outlet 종류
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "SetOutletPickupUploadData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultCode":-11,"ResultMsg":"Failed"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int ResultCode = jsonObject.getInt("ResultCode");
                result.setResultCode(ResultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (ResultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");
                    contentVal2.put("chg_dt", changeDataString);

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2, "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{pickup_no, opID});
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetOutletPickupUploadData Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public OutletPickupDoneHelper execute() {
        OutletPickupTask outletPickupTask = new OutletPickupTask();
        outletPickupTask.execute();
        return this;
    }
}