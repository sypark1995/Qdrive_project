package com.giosis.library.list;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;


public class TodayScanPackingListDownloadHelper {
    String TAG = "ScanPackingListDownloadHelper";

    private final Context context;
    private final String pickupNo;
    private final String opID;

    private OnScanPackingListDownloadEventListener eventListener;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String pickupNo;

        private OnScanPackingListDownloadEventListener eventListener;

        public Builder(Context context, String opID, String pickupNo) {

            this.context = context;
            this.opID = opID;
            this.pickupNo = pickupNo;
        }

        public TodayScanPackingListDownloadHelper build() {
            return new TodayScanPackingListDownloadHelper(this);
        }

        Builder setOnScanPackingListDownloadEventListener(OnScanPackingListDownloadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private TodayScanPackingListDownloadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.pickupNo = builder.pickupNo;

        this.eventListener = builder.eventListener;
    }


    class ScannedPackingListAsyncTask extends AsyncTask<Void, Void, Integer> {

        int resultCode = -999;
        PickupPackingListResult pickupPackingResult = null;
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

            pickupPackingResult = getPickupPackingListData();

            if (pickupPackingResult != null && pickupPackingResult.getResultObject() != null) {

                resultCode = pickupPackingResult.getResultCode();
            }

            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                if (result == 0) {

                    if (eventListener != null) {
                        eventListener.onScanPackingListDownloadResult(pickupPackingResult);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }
    }


    private PickupPackingListResult getPickupPackingListData() {

        Gson gson = new Gson();
        PickupPackingListResult resultObj;

        // JSON Parser
        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("pickup_no", pickupNo);
            job.accumulate("app_id", Preferences.INSTANCE.getUserId());
            job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

            String methodName = "getScanPackingList";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultObject":[{"packing_no":"SGP163596005","reg_dt":"Aug 24 2020  1:08PM","op_id":"YuMin.Dwl","pickup_no":"P1631998"},{"packing_no":"SGP163579875","reg_dt":"Aug 24 2020  1:08PM","op_id":"YuMin.Dwl","pickup_no":"P1631998"},{"packing_no":"SGP163612649","reg_dt":"Aug 24 2020  1:08PM","op_id":"YuMin.Dwl","pickup_no":"P1631998"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

            resultObj = gson.fromJson(jsonString, PickupPackingListResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getScanPackingList Json Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }


    public TodayScanPackingListDownloadHelper execute() {
        ScannedPackingListAsyncTask scannedPackingListAsyncTask = new ScannedPackingListAsyncTask();
        scannedPackingListAsyncTask.execute();
        return this;
    }

    public interface OnScanPackingListDownloadEventListener {
        void onScanPackingListDownloadResult(PickupPackingListResult result);
    }
}