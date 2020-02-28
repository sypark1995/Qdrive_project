package com.giosis.util.qdrive.list;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;


public class TodayDonePickupListDownloadHelper extends ManualHelper {
    String TAG = "TodayDonePickupListDownloadHelper";

    private final Context context;
    private final String opID;

    private final String networkType;
    private OnTodayDonePickupOrderDownloadEventListener eventListener;


    public static class Builder {

        private final Context context;
        private final String opID;

        private String networkType;
        private OnTodayDonePickupOrderDownloadEventListener eventListener;

        public Builder(Context context, String opID) {

            this.context = context;
            this.opID = opID;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public TodayDonePickupListDownloadHelper build() {
            return new TodayDonePickupListDownloadHelper(this);
        }

        Builder setOnTodayDonePickupOrderDownloadEventListener(OnTodayDonePickupOrderDownloadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private TodayDonePickupListDownloadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
    }


    class TodayPickupDoneDownloadAsyncTask extends AsyncTask<Void, Void, String> {

        int resultCode = -999;
        int pickupSize = 0;
        PickupAssignResult pickupAssignResult = null;

        @Override
        protected String doInBackground(Void... params) {

            String successString = null;

            try {

                pickupAssignResult = getPickupServerData();

                if (pickupAssignResult != null) {

                    if (pickupAssignResult.getResultObject() != null) {

                        pickupSize = pickupAssignResult.getResultObject().size();
                    }

                    resultCode = pickupAssignResult.getResultCode();
                    successString = pickupAssignResult.getResultMsg();
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  doInBackground Exception : " + e.toString());
                successString = null;
            }

            return successString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                if (result != null) {

                    ArrayList<Object> eventList = new ArrayList<>();
                    eventList.add(Integer.toString(resultCode));
                    eventList.add(Integer.toString(pickupSize));
                    eventList.add(pickupAssignResult);

                    if (eventListener != null) {
                        eventListener.onTodayDonePickupOrderDownloadResult(eventList);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }
    }


    private PickupAssignResult getPickupServerData() {

        PickupAssignResult resultObj = null;

        try {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", opID);
            hmActionParam.put("done_date", "");
            hmActionParam.put("pickup_type", "");
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "getTodayPickupDone";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":[{"contr_no":"55003835","partner_ref_no":"P42147N","invoice_no":"P42147N","stat":"P3","req_nm":"KARAM","req_dt":"2019-08-2109:00-17:00","tel_no":"01012345678","hp_no":"01012345678","zip_code":"99785","address":"13 BUKIT TERESA CLOSE#10-16","pickup_hopeday":"2019-08-21","pickup_hopetime":"09:00-17:00","sender_nm":"","del_memo":"","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Qxpress","partner_id":"qxpress.sg","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"QSM","del_driver_id":null,"cust_no":null}],"ResultCode":0,"ResultMsg":"SUCCESS"}

            resultObj = serializer.read(PickupAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getTodayPickupDone Exception : " + e.toString());
        }

        return resultObj;
    }


    public TodayDonePickupListDownloadHelper execute() {
        TodayPickupDoneDownloadAsyncTask todayPickupDoneDownloadAsyncTask = new TodayPickupDoneDownloadAsyncTask();
        todayPickupDoneDownloadAsyncTask.execute();
        return this;
    }

    public interface OnTodayDonePickupOrderDownloadEventListener {
        void onTodayDonePickupOrderDownloadResult(ArrayList<Object> resultList);
    }
}