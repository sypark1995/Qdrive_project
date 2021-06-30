package com.giosis.library.barcodescanner.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.ChangeDriverResult;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;

@Deprecated
public class ChangeDriverValidationCheckHelper {
    String TAG = "ChangeDriverValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String scanNo;

    private final OnChangeDelDriverValidCheckListener eventListener;
    private final AlertDialog resultDialog;

    private AlertDialog getResultAlertDialog(final Context context) {

        return new AlertDialog.Builder(context)
                .setTitle("[ " + context.getResources().getString(R.string.text_scanned_failed) + "]")
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {

                    if (dialog1 != null)
                        dialog1.dismiss();
                }).create();
    }

    private ChangeDriverValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.scanNo = builder.scanNo;

        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String scanNo;

        private OnChangeDelDriverValidCheckListener eventListener;

        public Builder(Context context, String opID, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.scanNo = scanNo;
        }

        public ChangeDriverValidationCheckHelper build() {
            return new ChangeDriverValidationCheckHelper(this);
        }

        public Builder setOnChangeDelDriverValidCheckListener(OnChangeDelDriverValidCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
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
                    try {
                        if (!((Activity) context).isFinishing()) {
                            showResultDialog(result.getResultMsg());
                        }
                    } catch (Exception e) {

                        Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                    }
                }
            }

            if (result != null && eventListener != null) {
                eventListener.OnChangeDelDriverValidCheckResult(result);
            }
        }


        private ChangeDriverResult validateScanNo(String scan_no) {

            Gson gson = new Gson();
            ChangeDriverResult resultObj;

            try {

                JSONObject job = new JSONObject();
                job.accumulate("scanData", scan_no);
                job.accumulate("driverId", opID);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

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
    }
}