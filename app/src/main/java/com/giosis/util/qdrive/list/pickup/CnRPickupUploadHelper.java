package com.giosis.util.qdrive.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.list.BarcodeData;
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
import java.util.ArrayList;
import java.util.Date;

/**
 * CnR Done
 * CnR Failed
 */

public class CnRPickupUploadHelper extends ManualHelper {
    String TAG = "CnRlPickupUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String pickupStat;
    private final ArrayList<BarcodeData> assignBarcodeList;
    private final SigningView signingView;
    private final SigningView collectorSigningView;

    private final String cancelCode;
    private final String retryDay;
    private final String driverMemo;
    private final String realQty;

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

        private final String pickupStat;
        private final ArrayList<BarcodeData> assignBarcodeList;
        private final SigningView signingView;
        private final SigningView collectorSigningView;

        private final String cancelCode;
        private final String retryDay;
        private final String driverMemo;
        private final String realQty;

        private final long disk_size;
        private final double lat;
        private final double lon;

        private OnServerEventListener eventListener;
        private String networkType;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       String pickupStat, ArrayList<BarcodeData> assignBarcodeList, SigningView signingView, SigningView collectorSigningView,
                       String cancelCode, String retryDay, String driverMemo, String realQty,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.pickupStat = pickupStat;
            this.assignBarcodeList = assignBarcodeList;
            this.signingView = signingView;
            this.collectorSigningView = collectorSigningView;

            this.cancelCode = cancelCode;
            this.retryDay = retryDay;
            this.driverMemo = driverMemo;
            this.realQty = realQty;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public CnRPickupUploadHelper build() {
            return new CnRPickupUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private CnRPickupUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.pickupStat = builder.pickupStat;
        this.assignBarcodeList = builder.assignBarcodeList;
        this.signingView = builder.signingView;
        this.collectorSigningView = builder.collectorSigningView;

        this.cancelCode = builder.cancelCode;
        this.retryDay = builder.retryDay;
        this.driverMemo = builder.driverMemo;
        this.realQty = builder.realQty;

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


    class CnRPickupUploadTask extends AsyncTask<Void, Integer, ArrayList<StdResult>> {
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
                StdResult stdResult = new StdResult();
                stdResult.setResultCode(-999);
                stdResult.setResultMsg("Error.");

                for (BarcodeData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        stdResult = requestPickupUpload(assignData.getBarcode());
                    }
                    resultList.add(stdResult);
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

            int successCount = 0;
            int failCount = 0;
            int resultCode = -999;
            String resultMsg;
            String fail_reason = "";

            try {

                for (int i = 0; i < resultList.size(); i++) {

                    StdResult result = resultList.get(i);
                    resultCode = result.getResultCode();
                    resultMsg = result.getResultMsg();
                    Log.e("krm0219", "DeviceData Result : " + resultCode + " / " + resultMsg);

                    if (resultCode < 0) {

                        if (resultCode == -14) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_14);
                        } else if (resultCode == -15) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_15);
                        } else if (resultCode == -16) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_16);
                        } else {
                            //   fail_reason += String.format(context.getResources().getString(R.string.msg_upload_fail_etc), resultCode);
                            fail_reason += resultMsg;
                        }

                        failCount++;
                    } else {
                        successCount++;
                    }
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

            if (pickupStat.equals("P3")) {

                DataUtil.captureSign("/QdrivePickup", assignNo, signingView);
                DataUtil.captureSign("/QdriveCollector", assignNo, collectorSigningView);
            }/* else if (pickupStat.equals("PF")) {   // cnr failed 시 이미지 선택x

                DataUtil.captureSign("/QdrivePickup", assignNo, signingView);
            }*/


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", pickupStat);
            contentVal.put("real_qty", realQty);
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", cancelCode);
            contentVal.put("retry_dt", retryDay);
            contentVal.put("driver_memo", driverMemo);

            if (pickupStat.equals("P3")) {
                contentVal.put("reg_id", opID);
            }

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

           /* // TEST   Upload Failed
            if (true) {

                result.setResultCode(-15);
                result.setResultMsg("");
                Log.e("Location", "TEST ManualPickupUploadHelper location : " + lat + " / " + lon);
                return result;
            }*/


            try {

                String bitmapString = "";
                String bitmapString2 = "";

                if (pickupStat.equals("P3")) {

                    signingView.buildDrawingCache();
                    collectorSigningView.buildDrawingCache();
                    Bitmap captureView = signingView.getDrawingCache();
                    Bitmap captureView2 = collectorSigningView.getDrawingCache();
                    bitmapString = DataUtil.bitmapToString(captureView);
                    bitmapString2 = DataUtil.bitmapToString(captureView2);
                }

                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "RC");
                job.accumulate("stat", pickupStat);
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)"); // 내부관리자용 메세지
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString2);
                job.accumulate("remark", driverMemo);                   // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("real_qty", realQty);
                job.accumulate("fail_reason", cancelCode);
                job.accumulate("retry_day", retryDay);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);
                Log.e("Server", "DATA : " + pickupStat + " / " + assignNo + " / " + realQty);


                String methodName = "SetPickupUploadData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
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

                Log.e("Exception", TAG + "  SetPickupUploadData Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg("");
            }

            return result;
        }
    }


    public CnRPickupUploadHelper execute() {
        CnRPickupUploadTask serverUploadTask = new CnRPickupUploadTask();
        serverUploadTask.execute();
        return this;
    }
}