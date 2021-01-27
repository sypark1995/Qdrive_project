package com.giosis.util.qdrive.barcodescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.server.Custom_JsonParser;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

public class ChangeDriverValidationCheckHelper {
    String TAG = "ChangeDriverValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String scanNo;

    private final String networkType;
    private final OnChangeDelDriverValidCheckListener eventListener;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String scanNo;

        private String networkType;
        private OnChangeDelDriverValidCheckListener eventListener;

        public Builder(Context context, String opID, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.scanNo = scanNo;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public ChangeDriverValidationCheckHelper build() {
            return new ChangeDriverValidationCheckHelper(this);
        }

        public Builder setOnChangeDelDriverValidCheckListener(OnChangeDelDriverValidCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ChangeDriverValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.scanNo = builder.scanNo;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private AlertDialog getResultAlertDialog(final Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("[ " + context.getResources().getString(R.string.text_scanned_failed) + "]")
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (dialog != null)
                            dialog.dismiss();
                    }
                }).create();

        return dialog;
    }

    private void showResultDialog(String message) {

        resultDialog.setMessage(message);
        resultDialog.show();
    }


    class ChangeDriverValidationTask extends AsyncTask<Void, Void, ChangeDriverResult> {

        @Override
        protected ChangeDriverResult doInBackground(Void... params) {

            ChangeDriverResult result = new ChangeDriverResult();

            if (scanNo != null && !scanNo.equals("")) {

                result = validateScanNo(scanNo);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ChangeDriverResult result) {
            super.onPostExecute(result);

            if (result != null) {
                if (result.getResultCode() < 0) {

                    showResultDialog(result.getResultMsg());
                }
            }

            if (result == null) {

                eventListener.OnChangeDelDriverValidCheckFailList(null);
            } else {
                if (eventListener != null) {
                    eventListener.OnChangeDelDriverValidCheckResult(result);
                }
            }
        }


        private ChangeDriverResult validateScanNo(String scan_no) {

            Gson gson = new Gson();
            ChangeDriverResult resultObj;

            // JSON Parser
            try {

                JSONObject job = new JSONObject();
                job.accumulate("scanData", scan_no);
                job.accumulate("driverId", opID);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetChangeDriverValidationCheck";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultObject":{"contr_no":"89581332","tracking_no":"SGP163206810","status":"DPC3-OUT","del_driver_id":"Farhan_STD"},"ResultCode":0,"ResultMsg":"Success"}
                // {"ResultObject":{"contr_no":null,"tracking_no":null,"status":null,"del_driver_id":null},"ResultCode":-1,"ResultMsg":"No data."}
                // {"ResultObject":{"contr_no":null,"tracking_no":null,"status":null,"del_driver_id":null},"ResultCode":-8,"ResultMsg":"[SGP163353912] has been on delivery by yourself"}
                resultObj = gson.fromJson(jsonString, ChangeDriverResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetChangeDriverValidationCheck Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }


    public ChangeDriverValidationCheckHelper execute() {
        ChangeDriverValidationTask changeDriverValidationTask = new ChangeDriverValidationTask();
        changeDriverValidationTask.execute();
        return this;
    }

    public interface OnChangeDelDriverValidCheckListener {
        void OnChangeDelDriverValidCheckResult(ChangeDriverResult result);

        void OnChangeDelDriverValidCheckFailList(ChangeDriverResult result);
    }
}