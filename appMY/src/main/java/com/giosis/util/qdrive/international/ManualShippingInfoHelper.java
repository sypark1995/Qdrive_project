package com.giosis.util.qdrive.international;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.library.list.BarcodeData;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

public class ManualShippingInfoHelper {
    String TAG = "ManualShippingInfoHelper";

    private final Context context;
    private final ArrayList<BarcodeData> assignBarcodeList;

    private final OnShippingInfoEventListener eventListener;

    public static class Builder {

        private final Context context;
        private final ArrayList<BarcodeData> assignBarcodeList;

        private OnShippingInfoEventListener eventListener;


        public Builder(Context context, ArrayList<BarcodeData> assignBarcodeList) {

            this.context = context;
            this.assignBarcodeList = assignBarcodeList;
        }

        public ManualShippingInfoHelper build() {
            return new ManualShippingInfoHelper(this);
        }

        Builder setOnShippingInfoEventListener(OnShippingInfoEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualShippingInfoHelper(Builder builder) {
        this.context = builder.context;
        this.assignBarcodeList = builder.assignBarcodeList;

        this.eventListener = builder.eventListener;
    }


    class ShippingInfoTask extends AsyncTask<Void, Void, ArrayList<ShippingInfoResult>> {

        @Override
        protected ArrayList<ShippingInfoResult> doInBackground(Void... params) {

            ArrayList<ShippingInfoResult> resultList = new ArrayList<>();

            if (assignBarcodeList != null && assignBarcodeList.size() > 0) {

                ShippingInfoResult result = null;
                for (BarcodeData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        result = requestShippingInfo(assignData.getBarcode());
                    }

                    resultList.add(result);
                }
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<ShippingInfoResult> resultList) {
            super.onPostExecute(resultList);

            if (eventListener != null) {
                eventListener.onPostResult(resultList);
            }
        }


        // 배송정보 습득 (받는사람, 보내는셀러)
        private ShippingInfoResult requestShippingInfo(String assignNo) {

            Gson gson = new Gson();
            ShippingInfoResult resultObj;

            try {

                JSONObject job = new JSONObject();
                job.accumulate("id_type", "PARTNERNO");
                job.accumulate("id_value", assignNo);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "GetContrInfo";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                resultObj = gson.fromJson(jsonString, ShippingInfoResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetContrInfo Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }


    public ManualShippingInfoHelper execute() {
        ShippingInfoTask shippingInfoTask = new ShippingInfoTask();
        shippingInfoTask.execute();
        return this;
    }

    public interface OnShippingInfoEventListener {
        void onPostResult(ArrayList<ShippingInfoResult> resultList);
    }
}