package com.giosis.util.qdrive.main;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.list.BarcodeData;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

public class ManualShippingInfoHelper extends ManualHelper {
    String TAG = "ManualShippingInfoHelper";

    private final Context context;
    private final ArrayList<BarcodeData> assignBarcodeList;

    private final String networkType;
    private final OnShippingInfoEventListener eventListener;

    public static class Builder {

        private final Context context;
        private final ArrayList<BarcodeData> assignBarcodeList;

        private String networkType;
        private OnShippingInfoEventListener eventListener;


        public Builder(Context context, ArrayList<BarcodeData> assignBarcodeList) {

            this.context = context;
            this.assignBarcodeList = assignBarcodeList;

            this.networkType = NetworkUtil.getNetworkType(context);
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

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
    }


    class ShippingInfoAsyncTask extends AsyncTask<Void, Integer, ArrayList<ShippingInfoResult>> {
        int progress = 0;

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


        // 배송정보 습득 (받는사람, 보내는셀러)
        private ShippingInfoResult requestShippingInfo(String assignNo) {

            ShippingInfoResult resultObj = null;

            try {

                GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
                HashMap<String, String> hmActionParam = new HashMap<>();
                hmActionParam.put("id_type", "PARTNERNO");
                hmActionParam.put("id_value", assignNo);
                hmActionParam.put("app_id", DataUtil.appID);
                hmActionParam.put("nation_cd", DataUtil.nationCode);

                String methodName = "GetContrInfo";
                Serializer serializer = new Persister();

                GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
                String resultString = response.getResultString();
                Log.e("Server", methodName + "  Result : " + resultString);
                // <ResultCode>-3</ResultCode><ResultMsg>No result</ResultMsg><ResultObject />

                resultObj = serializer.read(ShippingInfoResult.class, resultString);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetContrInfo Exception : " + e.toString());
            }

            return resultObj;
        }
    }


    public ManualShippingInfoHelper execute() {

        ShippingInfoAsyncTask shippingInfoAsyncTask = new ShippingInfoAsyncTask();
        shippingInfoAsyncTask.execute();
        return this;
    }

    public interface OnShippingInfoEventListener {
        void onPostResult(ArrayList<ShippingInfoResult> resultList);
    }
}