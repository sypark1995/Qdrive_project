package com.giosis.util.qdrive.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.gps.OnFusedProviderListenerUploadEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

public class DriverPerformanceLogUploadHelper extends ManualHelper {
    String TAG = "DriverPerformanceLogUploadHelper";

    private final Context context;
    private final String opID;

    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private String action;

    private final OnFusedProviderListenerUploadEventListener eventListener;
    private final AlertDialog resultDialog;


    public static class Builder {

        private final Context context;
        private final String opID;

        private final double latitude;
        private final double longitude;
        private final double accuracy;
        private final String action;

        private OnFusedProviderListenerUploadEventListener eventListener;


        public Builder(Context context, String opID, double latitude, double longitude, double accuracy, String action) {

            this.context = context;
            this.opID = opID;

            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.action = action;
        }

        public DriverPerformanceLogUploadHelper build() {
            return new DriverPerformanceLogUploadHelper(this);
        }

        Builder setOnFusedProviderListenerUploadEventListener(OnFusedProviderListenerUploadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private DriverPerformanceLogUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;

        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.accuracy = builder.accuracy;
        this.action = builder.action;

        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
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
                }).create();

        return dialog;
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }


    class DriverPerformanceLogUploadTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            return requestServerUpload();
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            try {

                int resultCode = result.getResultCode();
                String resultMsg = result.getResultMsg();

                if (resultCode < 0) {

                    if (resultCode == -16) {

                        showResultDialog(resultMsg);
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_failed_msg), resultMsg);
                        showResultDialog(msg);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestServerUpload() {

            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }


            try {

                JSONObject job = new JSONObject();
                job.accumulate("channel", "QDRIVE_V2");
                job.accumulate("op_id", opID);
                job.accumulate("action", action);
                job.accumulate("pickup_no", "");
                job.accumulate("seller_id", "");
                job.accumulate("zipcode", "");
                job.accumulate("latitude", latitude);
                job.accumulate("longitude", longitude);
                job.accumulate("accuracy", accuracy);
                job.accumulate("memo", "");
                job.accumulate("reg_id", opID);
                job.accumulate("referece", "");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "setDriverPerformanceLog";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"Success"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  setDriverPerformanceLog Exception : " + e.toString());
                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }
    }


    public DriverPerformanceLogUploadHelper execute() {
        DriverPerformanceLogUploadTask DriverPerformanceLogUploadTask = new DriverPerformanceLogUploadTask();
        DriverPerformanceLogUploadTask.execute();
        return this;
    }
}