package com.giosis.util.qdrive.list.delivery;

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
import android.widget.ImageView;

import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.list.BarcodeData;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Delivery Done
 */
public class DeliveryDoneUploadHelper {
    String TAG = "DeliveryDoneUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final ArrayList<BarcodeData> assignBarcodeList;
    private final String receiveType;
    private final String driverMemo;

    private final SigningView signingView;
    private final boolean hasSignImage;
    private final ImageView imageView;
    private final boolean hasVisitImage;

    private final long disk_size;
    private final double lat;
    private final double lon;

    private final String networkType;
    private final OnServerUploadEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final ArrayList<BarcodeData> assignBarcodeList;
        private final String receiveType;
        private final String driverMemo;

        private final SigningView signingView;
        private final boolean hasSignImage;
        private final ImageView imageView;
        private final boolean hasVisitImage;


        private final long disk_size;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnServerUploadEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<BarcodeData> assignBarcodeList, String receiveType, String driverMemo,
                       SigningView signingView, boolean hasSignImage, ImageView imageView, boolean hasVisitImage,
                       long disk_size, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.assignBarcodeList = assignBarcodeList;
            this.receiveType = receiveType;
            this.driverMemo = driverMemo;

            this.signingView = signingView;
            this.hasSignImage = hasSignImage;
            this.imageView = imageView;
            this.hasVisitImage = hasVisitImage;

            this.disk_size = disk_size;
            this.lat = lat;
            this.lon = lon;
        }

        public DeliveryDoneUploadHelper build() {
            return new DeliveryDoneUploadHelper(this);
        }

        public Builder setOnServerUploadEventListener(OnServerUploadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private DeliveryDoneUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.assignBarcodeList = builder.assignBarcodeList;
        this.receiveType = builder.receiveType;
        this.driverMemo = builder.driverMemo;

        this.signingView = builder.signingView;
        this.hasSignImage = builder.hasSignImage;
        this.imageView = builder.imageView;
        this.hasVisitImage = builder.hasVisitImage;

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
                        try {
                            if (dialog != null)
                                dialog.dismiss();
                        } catch (Exception e) {
                            Log.e("Exception", TAG + "getResultAlertDialog Exception : " + e.toString());
                        }

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


    class DeliveryUploadTask extends AsyncTask<Void, Integer, ArrayList<StdResult>> {
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
                for (BarcodeData assignData : assignBarcodeList) {

                    StdResult result = null;

                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        result = requestServerUpload(assignData.getBarcode());
                    }

                    resultList.add(result);
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

            StdResult result = null;
            int successCount = 0;
            int failCount = 0;
            String fail_reason = "";

            try {

                for (int i = 0; i < resultList.size(); i++) {

                    result = resultList.get(i);

                    if (result.getResultCode() < 0) {

                        fail_reason += result.getResultMsg();
                        failCount++;
                    } else {

                        successCount++;
                    }
                }

                if (0 < successCount && failCount == 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showResultDialog(msg);
                } else {

                    String msg;
                    if (0 < successCount) {
                        msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), successCount, failCount, fail_reason);
                    } else {
                        msg = String.format(context.getResources().getString(R.string.text_upload_fail_count1), failCount, fail_reason);
                    }

                    showResultDialog(msg);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestServerUpload(String assignNo) {

            StdResult result = new StdResult();

            String bitmapString = "";
            String bitmapString1 = "";

            if (hasSignImage) {
                Log.e("krm0219", " save  sign");
                DataUtil.captureSign("/Qdrive", assignNo, signingView);

                signingView.buildDrawingCache();
                Bitmap signBitmap = signingView.getDrawingCache();
                bitmapString = DataUtil.bitmapToString(signBitmap, ImageUpload.QXPOD, "qdriver/sign", assignNo);

                if (bitmapString.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }
            }

            if (hasVisitImage) {
                Log.e("krm0219", " save  visit log");
                DataUtil.captureSign("/Qdrive", assignNo + "_1", imageView);

                imageView.buildDrawingCache();
                Bitmap visitBitmap = imageView.getDrawingCache();
                bitmapString1 = DataUtil.bitmapToString(visitBitmap, ImageUpload.QXPOD, "qdriver/delivery", assignNo);

                if (bitmapString1.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }
            }

            Log.e("krm0219", TAG + " DATA  :  " + bitmapString + " / " + bitmapString1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", "D4");
            contentVal.put("rcv_type", receiveType);
            contentVal.put("driver_memo", driverMemo);
            contentVal.put("chg_id", opID);
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", "");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});


            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

            
//            // TEST.  Upload Failed
//            if (true) {
//
//                result.setResultCode(-15);
//                result.setResultMsg(context.getResources().getString(R.string.msg_time_out));
//
//                return result;
//            }


            try {

                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", receiveType);
                job.accumulate("stat", "D4");
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("delivery_photo_url", bitmapString1);
                job.accumulate("remark", driverMemo);            // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("stat_reason", "");
                job.accumulate("del_channel", "QR");        // 업로드 채널: Qsign Realtime
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
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  upload Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
            }

            return result;
        }
    }


    public DeliveryDoneUploadHelper execute() {
        DeliveryUploadTask deliveryUploadTask = new DeliveryUploadTask();
        deliveryUploadTask.execute();
        return this;
    }

    public interface OnServerUploadEventListener {
        void onPostResult();
    }
}