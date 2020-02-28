package com.giosis.util.qdrive.list.pickup;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.list.PrintDataResult;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

public class CnRPickupInfoGetHelper extends ManualHelper {
    String TAG = "CnRPickupInfoGetHelper";

    private final Context context;
    private final String opID;
    private final String tracking_no;

    private final OnCnRPrintDataEventListener eventListener;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String tracking_no;

        private OnCnRPrintDataEventListener eventListener;

        public Builder(Context context, String opID, String tracking_no) {

            this.context = context;
            this.opID = opID;
            this.tracking_no = tracking_no;
        }

        public CnRPickupInfoGetHelper build() {
            return new CnRPickupInfoGetHelper(this);
        }

        public Builder setOnCnRPrintDataEventListener(OnCnRPrintDataEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private CnRPickupInfoGetHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.tracking_no = builder.tracking_no;

        this.eventListener = builder.eventListener;
    }


    class CNRPrintData extends AsyncTask<Void, Integer, PrintDataResult> {

        @Override
        protected PrintDataResult doInBackground(Void... params) {

            PrintDataResult result = null;

            if (tracking_no != null && !tracking_no.equals("")) {

                result = requestDriverAssign(tracking_no);
            }

            return result;
        }

        @Override
        protected void onPostExecute(PrintDataResult resultList) {
            super.onPostExecute(resultList);

            if (eventListener != null) {
                eventListener.onPostAssignResult(resultList);
            }
        }


        private PrintDataResult requestDriverAssign(String tracking_no) {

            PrintDataResult resultObj;

            if (!NetworkUtil.isNetworkAvailable(context)) {

                resultObj = new PrintDataResult();
                resultObj.setResultCode(-16);
                resultObj.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error));
                return resultObj;
            }


            try {

                GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
                HashMap<String, String> hmActionParam = new HashMap<>();

                // TODO.  TEST
                //   tracking_no = "C2859SGSG";

                hmActionParam.put("pickup_no", tracking_no);
                hmActionParam.put("driver_id", opID);
                hmActionParam.put("app_id", DataUtil.appID);
                hmActionParam.put("nation_cd", DataUtil.nationCode);

                String methodName = "GetCnRPrintData";
                Serializer serializer = new Persister();

                GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
                String resultString = response.getResultString();
                Log.e("Server", methodName + "  Result : " + resultString);
                // {"ResultObject":{"contr_no":"55003355","pickup_no":null,"partner_ref_no":"C2859SGSG","invoice_no":"C2859SGSG","rcv_nm":"hyemi6666","tel_no":"+65--","hp_no":"+65-1424-2354","zip_code":"048741","front_address":"11 PEKIN STREET","back_address":"hyemi6666","seller_shop_nm":"test191919","delivery_course_code":"EAE042"},"ResultCode":0,"ResultMsg":"SUCCESS"}

                resultObj = serializer.read(PrintDataResult.class, resultString);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetCnRPrintData Exception : " + e.toString());
                resultObj = new PrintDataResult();
                resultObj.setResultCode(-1);
                resultObj.setResultMsg("FAIL Update");
            }

            return resultObj;
        }
    }


    public CnRPickupInfoGetHelper execute() {
        CNRPrintData printData = new CNRPrintData();
        printData.execute();
        return this;
    }

    public interface OnCnRPrintDataEventListener {
        void onPostAssignResult(PrintDataResult stdResult);
    }
}