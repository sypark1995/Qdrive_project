package com.giosis.library.barcodescanner.helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.StdResult;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

public class PickupTakeBackValidationCheckHelper {
    String TAG = "PickupTakeBackValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String pickup_no;
    private final String scanNo;

    private final OnPickupTakeBackValidationCheckListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String pickup_no;
        private final String scanNo;

        private OnPickupTakeBackValidationCheckListener eventListener;

        public Builder(Context context, String opID, String pickup_no, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.pickup_no = pickup_no;
            this.scanNo = scanNo;
        }

        public PickupTakeBackValidationCheckHelper build() {
            return new PickupTakeBackValidationCheckHelper(this);
        }

        public Builder setOnPickupTakeBackValidationCheckListener(OnPickupTakeBackValidationCheckListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }
    }

    private PickupTakeBackValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.pickup_no = builder.pickup_no;
        this.scanNo = builder.scanNo;

        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_driver_assign));
        progressDialog.setCancelable(false);

        return progressDialog;
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        return new AlertDialog.Builder(context)
                .setTitle("[" + context.getResources().getString(R.string.text_scanned_failed) + "]").setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {
                    if (dialog1 != null)
                        dialog1.dismiss();
                }).create();
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }


    class TakeBackValidationCheckAsyncTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            StdResult result = new StdResult();

            if (scanNo != null && !scanNo.equals("")) {

                result = validationCheck(scanNo);
            }

            return result;
        }

        @Override
        protected void onPostExecute(StdResult result) {
            try {

                DisplayUtil.dismissProgressDialog(progressDialog);

                if (result.getResultCode() < 0) {
                    showResultDialog(result.getResultMsg());
                }

                if (eventListener != null) {
                    eventListener.onPickupTakeBackValidationCheckResult(result);
                }

                super.onPostExecute(result);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();
            }
        }


        private StdResult validationCheck(String scan_no) {

            StdResult resultObj = new StdResult();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("op_id", opID);
                job.accumulate("pickup_no", pickup_no);
                job.accumulate("scan_no", scan_no);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "SetAddScanNo_TakeBack";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":10,"ResultMsg":"Success"}
                // {"ResultCode":-2,"ResultMsg":"Scanned number is not take back"}

                JSONObject jsonObject = new JSONObject(jsonString);
                resultObj.setResultCode(jsonObject.getInt("ResultCode"));
                resultObj.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  changePassword Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                resultObj.setResultCode(-15);
                resultObj.setResultMsg(msg);
            }

            return resultObj;
        }
    }


    public PickupTakeBackValidationCheckHelper execute() {
        TakeBackValidationCheckAsyncTask TakeBackValidationCheckAsyncTask = new TakeBackValidationCheckAsyncTask();
        TakeBackValidationCheckAsyncTask.execute();
        return this;
    }

    public interface OnPickupTakeBackValidationCheckListener {

        void onPickupTakeBackValidationCheckResult(StdResult result);
    }
}