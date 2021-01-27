package com.giosis.util.qdrive.barcodescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.server.Custom_JsonParser;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

public class OutletPickupScanValidationCheckHelper {
    String TAG = "OutletPickupScanValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String pickup_no;
    private final String scanNo;
    private final String route;

    private final String networkType;
    private final OnPickupAddScanNoOneByOneUploadListener eventListener;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String pickup_no;
        private final String scanNo;
        private final String route;

        private String networkType;
        private OnPickupAddScanNoOneByOneUploadListener eventListener;

        public Builder(Context context, String opID, String pickup_no, String scanNo, String route) {

            this.context = context;
            this.opID = opID;
            this.pickup_no = pickup_no;
            this.scanNo = scanNo;
            this.route = route;
            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public OutletPickupScanValidationCheckHelper build() {
            return new OutletPickupScanValidationCheckHelper(this);
        }

        public Builder setOnPickupAddScanNoOneByOneUploadListener(OnPickupAddScanNoOneByOneUploadListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }
    }

    private OutletPickupScanValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.pickup_no = builder.pickup_no;
        this.scanNo = builder.scanNo;
        this.route = builder.route;

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

    class OutletPickupScanValidationTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            StdResult result = new StdResult();

            if (scanNo != null && !scanNo.equals("")) {
                result = updateAddScanNo(scanNo);
            }

            return result;
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            if (result.getResultCode() < 0) {
                showResultDialog(result.getResultMsg());
            }

            if (eventListener != null) {
                eventListener.onPickupAddScanNoOneByOneUploadResult(result);
            }
        }


        private StdResult updateAddScanNo(String scan_no) {

            StdResult resultObj = new StdResult();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("opId", opID);
                job.accumulate("type", route);
                job.accumulate("pickup_no", pickup_no);
                job.accumulate("scan_no", scan_no);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "SetPickupScanNo";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"STD"}

                JSONObject jsonObject = new JSONObject(jsonString);
                resultObj.setResultCode(jsonObject.getInt("ResultCode"));
                resultObj.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetPickupScanNo Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                resultObj.setResultCode(-15);
                resultObj.setResultMsg(msg);
            }

            return resultObj;
        }
    }


    public OutletPickupScanValidationCheckHelper execute() {
        OutletPickupScanValidationTask outletPickupScanValidationTask = new OutletPickupScanValidationTask();
        outletPickupScanValidationTask.execute();
        return this;
    }

    public interface OnPickupAddScanNoOneByOneUploadListener {
        void onPickupAddScanNoOneByOneUploadResult(StdResult result);
    }
}