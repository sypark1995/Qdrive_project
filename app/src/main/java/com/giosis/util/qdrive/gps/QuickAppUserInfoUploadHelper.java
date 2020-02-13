package com.giosis.util.qdrive.gps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QuickAppUserInfoUploadHelper extends ManualHelper {
    private String TAG = "QuickAppUserInfoUploadHelper";

    private final Context context;
    private final String opID;
    private final String failed_reason;
    private final String type;

    private final String api_level;
    private final String device_info;
    private final String device_model;
    private final String device_product;
    private final String device_os_version;

    private final String networkType;
    private final AlertDialog resultDialog;
    private final OnQuickAppUserInfoUploadEventListener eventListener;


    public static class Builder {

        private final Context context;
        private final String opID;
        private final String failed_reason;
        private final String type;

        private final String api_level;
        private final String device_info;
        private final String device_model;
        private final String device_product;
        private final String device_os_version;

        private String networkType;
        private OnQuickAppUserInfoUploadEventListener eventListener;


        public Builder(Context context, String opID, String failed_reason, String api_level, String device_info,
                       String device_model, String device_product, String device_os_version, String type) {

            this.context = context;
            this.opID = opID;
            this.failed_reason = failed_reason;
            this.type = type;

            this.api_level = api_level;
            this.device_info = device_info;
            this.device_model = device_model;
            this.device_product = device_product;
            this.device_os_version = device_os_version;
            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public QuickAppUserInfoUploadHelper build() {
            return new QuickAppUserInfoUploadHelper(this);
        }

        public Builder setOnQuickQppUserInfoUploadEventListener(OnQuickAppUserInfoUploadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private QuickAppUserInfoUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.failed_reason = builder.failed_reason;
        this.type = builder.type;

        this.api_level = builder.api_level;
        this.device_info = builder.device_info;
        this.device_model = builder.device_model;
        this.device_product = builder.device_product;
        this.device_os_version = builder.device_os_version;

        this.networkType = builder.networkType;
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
                            eventListener.onServerResult();
                        }
                    }
                })
                .create();

        return dialog;
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }


    class UserInfoUploadTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            return requestServerUpload();
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            try {

                int resultCode = result.getResultCode();

                if (resultCode < 0) {

                    showResultDialog(result.getResultMsg());
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestServerUpload() {

            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error));

                return result;
            }


            try {

                JSONObject job = new JSONObject();
                job.accumulate("channel", "QDRIVE");
                job.accumulate("type", type);
                job.accumulate("op_id", opID);
                job.accumulate("vehicle_code", "");
                job.accumulate("device_id", "");
                job.accumulate("api_level", api_level);
                job.accumulate("device_info", device_info);
                job.accumulate("device_model", device_model);
                job.accumulate("device_product", device_product);
                job.accumulate("device_os_version", device_os_version);
                job.accumulate("network_type", networkType);
                job.accumulate("location_mng_stat", "");

                if (type.equals("exception") || type.equals("killapp")) {
                    job.accumulate("fused_provider_stat", "");
                } else {
                    job.accumulate("fused_provider_stat", failed_reason);
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String regDataString = dateFormat.format(new Date());
                job.accumulate("logout_dt", regDataString);

                if (type.equals("exception")) {
                    job.accumulate("desc1", failed_reason);
                } else {
                    job.accumulate("desc1", "");
                }

                job.accumulate("desc2", "");
                job.accumulate("desc3", "");
                job.accumulate("desc4", "");
                job.accumulate("desc5", "");
                job.accumulate("reg_id", opID);
                job.accumulate("chg_id", opID);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "setQuickAppUserInfo";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultCode":0,"ResultMsg":"OK"}

                JSONObject jsonObject = new JSONObject(jsonString);

                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  setQuickAppUserInfo Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg("Exception Error.\n" + e.toString());
            }

            return result;
        }
    }

    public QuickAppUserInfoUploadHelper execute() {
        UserInfoUploadTask UserInfoUploadTask = new UserInfoUploadTask();
        UserInfoUploadTask.execute();
        return this;
    }

    public interface OnQuickAppUserInfoUploadEventListener {
        void onServerResult();
    }
}