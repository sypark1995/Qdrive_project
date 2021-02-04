package com.giosis.library.barcodescanner.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.StdResult;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

public class ConfirmMyOrderValidationCheckHelper {
    String TAG = "ConfirmMyOrderValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String outletDriverYN;
    private final String scanNo;

    private final OnDpc3OutValidationCheckListener eventListener;
    private final AlertDialog resultDialog;

    private AlertDialog getResultAlertDialog(final Context context) {

        return new AlertDialog.Builder(context)
                .setTitle("[" + context.getResources().getString(R.string.text_scanned_failed) + "]")
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {

                    if (dialog1 != null)
                        dialog1.dismiss();
                }).create();
    }

    private ConfirmMyOrderValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.outletDriverYN = builder.outletDriverYN;
        this.scanNo = builder.scanNo;

        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
    }

    public interface OnDpc3OutValidationCheckListener {
        void OnDpc3OutValidationCheckResult(StdResult result);

        void OnDpc3OutValidationCheckFailList(StdResult result);

    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String outletDriverYN;
        private final String scanNo;

        private OnDpc3OutValidationCheckListener eventListener;

        public Builder(Context context, String opID, String outletDriverYN, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.outletDriverYN = outletDriverYN;
            this.scanNo = scanNo;
        }

        public ConfirmMyOrderValidationCheckHelper build() {
            return new ConfirmMyOrderValidationCheckHelper(this);
        }

        public Builder setOnDpc3OutValidationCheckListener(OnDpc3OutValidationCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    public ConfirmMyOrderValidationCheckHelper execute() {
        Dpc3OutValidationTask dpc3OutValidationTask = new Dpc3OutValidationTask();
        dpc3OutValidationTask.execute();
        return this;
    }

    class Dpc3OutValidationTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            StdResult result = new StdResult();

            if (scanNo != null && !scanNo.equals("")) {
                result = validateScanNo(scanNo);
            }

            return result;
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            try {

                if (result.getResultCode() < 0) {

                    if (!((Activity) context).isFinishing()) {
                        showResultDialog(result.getResultMsg());
                    }
                }

                if (eventListener != null) {
                    eventListener.OnDpc3OutValidationCheckResult(result);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();

                if (eventListener != null) {
                    eventListener.OnDpc3OutValidationCheckFailList(result);
                }
            }
        }


        private StdResult validateScanNo(String scan_no) {

            StdResult resultObj = new StdResult();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("scanData", scan_no);
                job.accumulate("driverId", opID);
                if (outletDriverYN.equals("Y")) {

                    job.accumulate("type", "OL");    // Outlet
                } else {

                    job.accumulate("type", "STD");    // Standard
                }
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "GetValidationCheckDpc3Out";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"Success"}
                // {"ResultCode":-25,"ResultMsg":"[SG19611818]  booking failed order. Pls return to DPC."}

                JSONObject jsonObject = new JSONObject(jsonString);
                resultObj.setResultCode(jsonObject.getInt("ResultCode"));
                resultObj.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetValidationCheckDpc3Out Exception : " + e.toString());
                resultObj.setResultCode(-15);
                resultObj.setResultMsg("Exception Error.\n" + e.toString());
            }

            return resultObj;
        }
    }
}