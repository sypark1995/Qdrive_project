package com.giosis.library.main;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.StdResult;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

public class DriverPerformanceLogUploadHelper {
    String TAG = "DriverPerformanceLogUploadHelper";

    private final Context context;
    private final String opID;

    private final double latitude;
    private final double longitude;
    private final double accuracy;

    private final AlertDialog resultDialog;
    private final OnDriverPerformanceLogUploadEventListener eventListener;

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {

                    if (dialog1 != null)
                        dialog1.dismiss();

                    if (eventListener != null) {
                        eventListener.onServerResult();
                    }
                }).create();

        return dialog;
    }

    private DriverPerformanceLogUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;

        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.accuracy = builder.accuracy;

        this.resultDialog = getResultAlertDialog(this.context);
        this.eventListener = builder.eventListener;
    }

    public static class Builder {

        private final Context context;
        private final String opID;

        private final double latitude;
        private final double longitude;
        private final double accuracy;

        private OnDriverPerformanceLogUploadEventListener eventListener;


        public Builder(Context context, String opID, double latitude, double longitude, double accuracy) {

            this.context = context;
            this.opID = opID;

            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
        }

        public DriverPerformanceLogUploadHelper build() {
            return new DriverPerformanceLogUploadHelper(this);
        }

        public Builder setOnDriverPerformanceLogUploadEventListener(OnDriverPerformanceLogUploadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
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


            String channel = "QDRIVE_V2";
            if (Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG"))
                channel = "QDRIVE";


            try {

                JSONObject job = new JSONObject();
                job.accumulate("channel", channel);
                job.accumulate("op_id", opID);
                job.accumulate("action", "killapp");
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
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "setDriverPerformanceLog";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"Success"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

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

    public interface OnDriverPerformanceLogUploadEventListener {
        void onServerResult();
    }
}