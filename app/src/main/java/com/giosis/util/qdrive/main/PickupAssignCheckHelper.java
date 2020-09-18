package com.giosis.util.qdrive.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;


public class PickupAssignCheckHelper extends ManualHelper {
    String TAG = "PickupAssignCheckHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String networkType;
    private final OnPickupAssignCheckListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private String networkType;
        private OnPickupAssignCheckListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public PickupAssignCheckHelper build() {
            return new PickupAssignCheckHelper(this);
        }

        Builder setOnPickupAssignCheckListener(OnPickupAssignCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private PickupAssignCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }


    class PickupCheckTask extends AsyncTask<Void, Integer, Integer> {
        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {

                progressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            return getManualAssignCount();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progress += values[0];
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            try {

                DisplayUtil.dismissProgressDialog(progressDialog);

                if (0 < result) {    // RPC 다운로드 성공

                    if (eventListener != null) {
                        eventListener.onDownloadResult(0);
                    }
                } else if (result == -1) {    //Deactivated 사용자

                    if (eventListener != null) {
                        eventListener.onDownloadFailList(-1);
                    }
                } else if (result == -16) {

                    showDisconnectedDialog();
                } else if (result < 0) {

                    showResultDialog(context.getResources().getString(R.string.msg_manual_assign_failed) + " : " + result);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }
    }


    private int getManualAssignCount() {

        if (!NetworkUtil.isNetworkAvailable(context)) {
            return -16;
        }

        int result = -15;

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opid", opID);
            job.accumulate("datatype", "MA");
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetManualAssignCount";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultCode":0,"ResultMsg":"OK"}

            JSONObject jsonObject = new JSONObject(jsonString);
            result = jsonObject.getInt("ResultCode");
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetManualAssignCount Exception : " + e.toString());
        }

        return result;
    }


    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        String dialogTitle = context.getResources().getString(R.string.text_download_result);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(dialogTitle)
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                        if (eventListener != null) {
                            eventListener.onDownloadResult(0);
                        }
                    }
                })
                .create();
        return dialog;
    }

    private void showResultDialog(String message) {
        resultDialog.setTitle(context.getResources().getString(R.string.text_download_result));
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    private void showDisconnectedDialog() {
        resultDialog.setTitle("");
        resultDialog.setMessage(context.getResources().getString(R.string.msg_network_connect_error));
        resultDialog.show();
    }

    public PickupAssignCheckHelper execute() {

        PickupCheckTask pickupCheckTask = new PickupCheckTask();
        pickupCheckTask.execute();
        return this;
    }

    public interface OnPickupAssignCheckListener {
        void onDownloadResult(Integer resultList);

        void onDownloadFailList(Integer resultList);
    }
}