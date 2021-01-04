package com.giosis.util.qdrive.list;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.google.gson.Gson;

import org.json.JSONObject;


public class TodayScannedPackingListDownloadHelper {
    String TAG = "TodayScannedPackingListDownloadHelper";

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

        public TodayScannedPackingListDownloadHelper build() {
            return new TodayScannedPackingListDownloadHelper(this);
        }

        Builder setOnScanPackingListDownloadEventListener(OnScanPackingListDownloadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private TodayScannedPackingListDownloadHelper(Builder builder) {
        this.context = builder.context;
        this.opID = builder.opID;
        this.pickupNo = builder.pickupNo;

        this.eventListener = builder.eventListener;
    }


    class DownloadTask extends AsyncTask<Void, Void, Integer> {

        int resultCode = -999;
        PickupPackingListResult pickupPackingResult = null;

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
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

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


    public TodayScannedPackingListDownloadHelper execute() {
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute();
        return this;
    }

    public interface OnScanPackingListDownloadEventListener {
        void onScanPackingListDownloadResult(PickupPackingListResult result);
    }
}