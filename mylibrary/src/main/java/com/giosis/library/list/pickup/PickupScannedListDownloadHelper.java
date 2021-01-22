package com.giosis.library.list.pickup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.server.data.PickupScannedListResult;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;


public class PickupScannedListDownloadHelper {
    private final OnServerEventListener eventListener;

    private final Context context;
    private final String pickupNo;
    private final String opID;
    String TAG = "PickupScannedListDownloadHelper";

    private PickupScannedListDownloadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.pickupNo = builder.pickupNo;

        this.eventListener = builder.eventListener;
    }

    private PickupScannedListResult getPickupPackingListData() {

        Gson gson = new Gson();
        PickupScannedListResult resultObj;

        // JSON Parser
        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("pickup_no", pickupNo);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());
            Log.e("Server", "DATA  " + opID + " / " + pickupNo);

            String methodName = "getScanPackingList";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultObject":[{"packing_no":"SGP163596005","reg_dt":"Aug 24 2020  1:08PM","op_id":"YuMin.Dwl","pickup_no":"P1631998"},{"packing_no":"SGP163579875","reg_dt":"Aug 24 2020  1:08PM","op_id":"YuMin.Dwl","pickup_no":"P1631998"},{"packing_no":"SGP163612649","reg_dt":"Aug 24 2020  1:08PM","op_id":"YuMin.Dwl","pickup_no":"P1631998"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

            resultObj = gson.fromJson(jsonString, PickupScannedListResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getScanPackingList Json Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }

    public PickupScannedListDownloadHelper execute() {

        ScannedPackingListAsyncTask scannedPackingListAsyncTask = new ScannedPackingListAsyncTask();
        scannedPackingListAsyncTask.execute();
        return this;
    }


    public interface OnServerEventListener {
        void onScannedListDownloadResult(PickupScannedListResult result);
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String pickupNo;

        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String pickupNo) {

            this.context = context;
            this.opID = opID;
            this.pickupNo = pickupNo;
        }

        public PickupScannedListDownloadHelper build() {
            return new PickupScannedListDownloadHelper(this);
        }

        Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    class ScannedPackingListAsyncTask extends AsyncTask<Void, Void, Integer> {

        int resultCode = -999;
        PickupScannedListResult result = null;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            result = getPickupPackingListData();

            if (result != null && result.getResultObject() != null) {

                resultCode = result.getResultCode();
            }

            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                if (resultCode == 0) {

                    if (eventListener != null) {

                        eventListener.onScannedListDownloadResult(result);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }
    }
}