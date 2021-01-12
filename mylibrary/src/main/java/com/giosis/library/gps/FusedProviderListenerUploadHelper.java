package com.giosis.library.gps;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

public class FusedProviderListenerUploadHelper {
    private final String TAG = "FusedProviderListenerUploadHelper";

    private final Context context;
    private final String opID;
    private final String deviceID;

    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private final String reference;
    private final String provider;

    private final String networkType;
    private final AlertDialog resultDialog;

    private AlertDialog getResultAlertDialog(final Context context) {

        return new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {
                    if (dialog1 != null)
                        dialog1.dismiss();
                }).create();
    }

    private FusedProviderListenerUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.deviceID = builder.deviceID;

        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.accuracy = builder.accuracy;
        this.reference = builder.reference;
        this.provider = builder.provider;

        this.networkType = builder.networkType;
        this.resultDialog = getResultAlertDialog(this.context);
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String deviceID;

        private final double latitude;
        private final double longitude;
        private final double accuracy;
        private final String reference;
        private final String provider;

        private final String networkType;


        public Builder(Context context, String opID, String deviceID, double latitude, double longitude, double accuracy, String reference, String provider) {

            this.context = context;
            this.opID = opID;
            this.deviceID = deviceID;

            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.reference = reference;
            this.provider = provider;
            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public FusedProviderListenerUploadHelper build() {
            return new FusedProviderListenerUploadHelper(this);
        }
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }


    class LocationUploadTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            return locationUpload();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            try {

                if (result == -16) {

                    showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved));
                } else if (result < 0) {

                    showResultDialog(context.getResources().getString(R.string.text_fail));
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private int locationUpload() {

            if (!NetworkUtil.isNetworkAvailable(context)) {
                return -16;
            }

            int result = -15;

            try {

                JSONObject job = new JSONObject();
                job.accumulate("channel", "QDRIVE");  // qdrive service
                job.accumulate("op_id", opID);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("latitude", latitude);       // 위도
                job.accumulate("longitude", longitude);     // 경도
                job.accumulate("accuracy", accuracy);       // 센서의 정확도
                job.accumulate("reference", reference);     // 시간 object or 거리 object
                job.accumulate("reg_id", opID);
                job.accumulate("chg_id", opID);
                job.accumulate("log_desc", provider);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                // ship.dbo.gps_location_history 위/경도 저장
                String methodName = "setGPSLocationVersion2";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"OK"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result = jsonObject.getInt("ResultCode");
            } catch (Exception e) {

                Log.e("Exception", TAG + "  setGPSLocationVersion2 Exception : " + e.toString());
            }

            return result;
        }
    }


    public FusedProviderListenerUploadHelper execute() {
        LocationUploadTask LocationUploadTask = new LocationUploadTask();
        LocationUploadTask.execute();
        return this;
    }
}