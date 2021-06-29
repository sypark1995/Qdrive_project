package com.giosis.library.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;


public class PickupAssignCheckHelper {
    String TAG = "PickupAssignCheckHelper";

    private final Context context;
    private final String opID;

    private final OnPickupAssignCheckListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;


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
            job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());


            String methodName = "GetManualAssignCount";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultCode":0,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            result = jsonObject.getInt("ResultCode");
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetManualAssignCount Exception : " + e.toString());
        }

        return result;
    }

    private PickupAssignCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;

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


                if (0 < result) {

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

    private AlertDialog getResultAlertDialog(final Context context) {

        String dialogTitle = context.getResources().getString(R.string.text_download_result);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(dialogTitle)
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {
                    if (dialog1 != null)
                        dialog1.dismiss();
                    if (eventListener != null) {
                        eventListener.onDownloadResult(0);
                    }
                })
                .create();
        return dialog;
    }

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static class Builder {

        private final Context context;
        private final String opID;

        private OnPickupAssignCheckListener eventListener;

        public Builder(Context context, String opID) {

            this.context = context;
            this.opID = opID;
        }

        public PickupAssignCheckHelper build() {
            return new PickupAssignCheckHelper(this);
        }

        public Builder setOnPickupAssignCheckListener(OnPickupAssignCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
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