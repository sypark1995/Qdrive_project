package com.giosis.util.qdrive.barcodescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

public class Dpc3OutValidationCheckHelper {
    String TAG = "Dpc3OutValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String outletDriverYN;
    private final String scanNo;

    private final String networkType;
    private final OnDpc3OutValidationCheckListener eventListener;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String outletDriverYN;
        private final String scanNo;

        private String networkType;
        private OnDpc3OutValidationCheckListener eventListener;

        public Builder(Context context, String opID, String outletDriverYN, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.outletDriverYN = outletDriverYN;
            this.scanNo = scanNo;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public Dpc3OutValidationCheckHelper build() {
            return new Dpc3OutValidationCheckHelper(this);
        }

        public Builder setOnDpc3OutValidationCheckListener(OnDpc3OutValidationCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private Dpc3OutValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.outletDriverYN = builder.outletDriverYN;
        this.scanNo = builder.scanNo;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private AlertDialog getResultAlertDialog(final Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("[" + context.getResources().getString(R.string.text_scanned_failed) + "]")
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


    class Dpc3OutValidationTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            StdResult result = new StdResult();
            if (scanNo != null && !scanNo.equals("")) {

                result = validateScanNo();
            }
            return result;
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            try {

                if (result.getResultCode() < 0) {

                    showResultDialog(result.getResultMsg());
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


        private StdResult validateScanNo() {

            StdResult resultObj = new StdResult();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("scanData", scanNo);
                job.accumulate("driverId", opID);

                if (outletDriverYN.equals("Y")) {

                    job.accumulate("type", "OL");    // Outlet
                } else {

                    job.accumulate("type", "STD");    // Standard
                }
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "GetValidationCheckDpc3Out";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"Success"}
                // {"ResultCode":-25,"ResultMsg":"[SG19611818]  booking failed order. Pls return to DPC."}

                JSONObject jsonObject = new JSONObject(jsonString);
                resultObj.setResultCode(jsonObject.getInt("ResultCode"));
                resultObj.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetValidationCheckDpc3Out Exception : " + e.toString());
                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                resultObj.setResultCode(-15);
                resultObj.setResultMsg(msg);
            }

            return resultObj;
        }
    }


    public Dpc3OutValidationCheckHelper execute() {
        Dpc3OutValidationTask dpc3OutValidationTask = new Dpc3OutValidationTask();
        dpc3OutValidationTask.execute();
        return this;
    }

    public interface OnDpc3OutValidationCheckListener {
        void OnDpc3OutValidationCheckResult(StdResult result);

        void OnDpc3OutValidationCheckFailList(StdResult result);

    }
}