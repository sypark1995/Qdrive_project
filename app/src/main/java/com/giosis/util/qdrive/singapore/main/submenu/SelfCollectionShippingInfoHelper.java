package com.giosis.util.qdrive.singapore.main.submenu;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.util.qdrive.singapore.list.BarcodeData;
import com.giosis.util.qdrive.singapore.server.Custom_JsonParser;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

public class SelfCollectionShippingInfoHelper {
    String TAG = "ManualShippingInfoHelper";

    private final ArrayList<BarcodeData> assignBarcodeList;
    private final OnShippingInfoEventListener eventListener;

    private SelfCollectionShippingInfoHelper(Builder builder) {

        this.assignBarcodeList = builder.assignBarcodeList;
        this.eventListener = builder.eventListener;
    }

    public SelfCollectionShippingInfoHelper execute() {

        ShippingInfoAsyncTask shippingInfoAsyncTask = new ShippingInfoAsyncTask();
        shippingInfoAsyncTask.execute();
        return this;
    }

    public static class Builder {

        private final ArrayList<BarcodeData> assignBarcodeList;
        private OnShippingInfoEventListener eventListener;


        public Builder(ArrayList<BarcodeData> assignBarcodeList) {

            this.assignBarcodeList = assignBarcodeList;
        }

        public SelfCollectionShippingInfoHelper build() {
            return new SelfCollectionShippingInfoHelper(this);
        }

        Builder setOnShippingInfoEventListener(OnShippingInfoEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    class ShippingInfoAsyncTask extends AsyncTask<Void, Integer, ArrayList<ShippingInfoResult>> {

        @Override
        protected ArrayList<ShippingInfoResult> doInBackground(Void... params) {

            ArrayList<ShippingInfoResult> resultList = new ArrayList<>();

            if (assignBarcodeList != null && 0 < assignBarcodeList.size()) {

                ShippingInfoResult result = null;

                for (BarcodeData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getBarcode())) {

                        result = requestShippingInfo(assignData.getBarcode());
                    }

                    resultList.add(result);
                    publishProgress(1);
                }
            }

            return resultList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<ShippingInfoResult> resultList) {
            super.onPostExecute(resultList);

            if (eventListener != null) {
                eventListener.onPostResult(resultList);
            }
        }


        // ???????????? ?????? (????????????, ???????????????)
        private ShippingInfoResult requestShippingInfo(String assignNo) {

            ShippingInfoResult resultObj;

            try {

                JSONObject job = new JSONObject();
                job.accumulate("id_type", "PARTNERNO");
                job.accumulate("id_value", assignNo);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());


                String methodName = "GetContrInfo";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                resultObj = new Gson().fromJson(jsonString, ShippingInfoResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetContrInfo Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }

    public interface OnShippingInfoEventListener {
        void onPostResult(ArrayList<ShippingInfoResult> resultList);
    }
}