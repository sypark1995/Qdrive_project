package com.giosis.util.qdrive.singapore.list;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.server.Custom_JsonParser;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;

// TODO_kjyoo 프린트로 테스트 해보기
public class CnRPickupInfoGetHelper {
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

                JSONObject job = new JSONObject();
                job.accumulate("pickup_no", tracking_no);
                job.accumulate("driver_id", opID);
                job.accumulate("app_id", Preferences.INSTANCE.getUserId());
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "GetCnRPrintData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                resultObj = new Gson().fromJson(jsonString, PrintDataResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetCnRPrintData Json Exception : " + e.toString());
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