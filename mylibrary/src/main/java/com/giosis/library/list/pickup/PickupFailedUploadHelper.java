package com.giosis.library.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.StdResult;
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


/**
 * 일반 픽업, CNR 픽업 실패
 */
@Deprecated
public class PickupFailedUploadHelper {
    String TAG = "PickupVisitLogUploadHelper";

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

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {
                    if (dialog1 != null)
                        dialog1.dismiss();

                    if (eventListener != null) {
                        eventListener.onPostResult();
                    }
                })
                .create();

        return dialog;
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

    public PickupFailedUploadHelper execute() {

        PickupVisitLogUploadTask pickupVisitLogUploadTask = new PickupVisitLogUploadTask();
        pickupVisitLogUploadTask.execute();
        return this;
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }

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

        private final String networkType;
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

    class PickupVisitLogUploadTask extends AsyncTask<Void, Integer, StdResult> {
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


        private StdResult requestPickupUpload(String assignNo) {

            DataUtil.captureSign("/QdrivePickup", assignNo, imageView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", "PF");
            contentVal.put("rcv_type", rcvType);
            contentVal.put("real_qty", "0");
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", failedCode);
            contentVal.put("retry_dt", retryDay);
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


//             // TEST_Upload Failed 시 주석 풀고 실행
//            if (true) {
//
//                result.setResultCode(-15);
//                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
//                return result;
//            }


            try {

                imageView.buildDrawingCache();
                Bitmap captureView = imageView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(context, captureView, ImageUpload.QXPOP, "qdriver/sign", assignNo);

                if (bitmapString.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", rcvType);        // VL, RC
                job.accumulate("stat", "PF");
                job.accumulate("opId", opID);
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)"); // 내부관리자용 메세지
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("real_qty", "0");
                job.accumulate("fail_reason", failedCode);
                job.accumulate("retry_day", retryDay);
                job.accumulate("remark", driverMemo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", "");
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", lat);
                job.accumulate("lon", lon);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());
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
                            "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{assignNo, opID});
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
}