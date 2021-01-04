package com.giosis.util.qdrive.list.pickup;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.PrintDataResult;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

public class ManualCnRPrintDataHelper {
    String TAG = "ManualCnRPrintDataHelper";

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

        public ManualCnRPrintDataHelper build() {
            return new ManualCnRPrintDataHelper(this);
        }

        public Builder setOnCnRPrintDataEventListener(OnCnRPrintDataEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualCnRPrintDataHelper(Builder builder) {

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

            Gson gson = new Gson();
            PrintDataResult resultObj;

            if (!NetworkUtil.isNetworkAvailable(context)) {

                resultObj = new PrintDataResult();
                resultObj.setResultCode(-16);
                resultObj.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error));
                return resultObj;
            }

            try {

                //  TEST
                //  tracking_no = "C2859SGSG";
                JSONObject job = new JSONObject();
                job.accumulate("pickup_no", tracking_no);
                job.accumulate("driver_id", opID);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetCnRPrintData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                resultObj = gson.fromJson(jsonString, PrintDataResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetCnRPrintData Json Exception : " + e.toString());
                resultObj = new PrintDataResult();
                resultObj.setResultCode(-1);
                resultObj.setResultMsg("FAIL Update");
            }

            return resultObj;
        }
    }


    public ManualCnRPrintDataHelper execute() {
        CNRPrintData printData = new CNRPrintData();
        printData.execute();
        return this;
    }

    public interface OnCnRPrintDataEventListener {
        void onPostAssignResult(PrintDataResult stdResult);
    }
}