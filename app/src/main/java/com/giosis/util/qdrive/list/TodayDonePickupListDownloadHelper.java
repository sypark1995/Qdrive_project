package com.giosis.util.qdrive.list;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;
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

        // JSON Parser
        Gson gson = new Gson();
        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("done_date", "");
            job.accumulate("pickup_type", "");
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "getTodayPickupDone";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultObject":[{"contr_no":"90547198","partner_ref_no":"C3036968SGMY","invoice_no":"C3036968SGMY","stat":"P3","req_nm":"Fei sien LIM","req_dt":"2020-08-2710:00-19:00","tel_no":"+65--","hp_no":"+65-9672-6662","zip_code":"752467","address":"467B ADMIRALTY DRIVE#03-133","pickup_hopeday":"2020-08-27","pickup_hopetime":"10:00-19:00","sender_nm":"Fei sien LIM","del_memo":"","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"CLEE16","partner_id":"CLEE16","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"C2C","del_driver_id":null,"cust_no":null,"ref_pickup_no":null,"lat_lng":null},{"contr_no":"90624111","partner_ref_no":"C3041458SGSG","invoice_no":"C3041458SGSG","stat":"P3","req_nm":"CLEE16","req_dt":"2020-08-2710:00-19:00","tel_no":"+65--","hp_no":"+65-9672-6662","zip_code":"752467","address":"467B ADMIRALTY DRIVE#03-133","pickup_hopeday":"2020-08-27","pickup_hopetime":"10:00-19:00","sender_nm":"CLEE16","del_memo":"","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"CLEE16","partner_id":"CLEE16","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"C2C","del_driver_id":null,"cust_no":null,"ref_pickup_no":null,"lat_lng":null},{"contr_no":"90425325","partner_ref_no":"C3028693SGSG","invoice_no":"C3028693SGSG","stat":"P3","req_nm":"Koh Wee Kuan","req_dt":"2020-08-2710:00-19:00","tel_no":"+65-9233-6413","hp_no":"+65-9233-6413","zip_code":"752469","address":"469B ADMIRALTY DRIVE#10-87 SINGAPORE 752469","pickup_hopeday":"2020-08-27","pickup_hopetime":"10:00-19:00","sender_nm":"Koh Wee Kuan","del_memo":"","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Koh Wee Kuan","partner_id":"wayne.kwk","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"C2C","del_driver_id":null,"cust_no":null,"ref_pickup_no":null,"lat_lng":null},{"contr_no":"90543559","partner_ref_no":"C3036859SGUS","invoice_no":"C3036859SGUS","stat":"P3","req_nm":"Cody Tong","req_dt":"2020-08-2710:00-19:00","tel_no":"+65-9137-7167","hp_no":"+65-9137-7167","zip_code":"750473","address":"473 SEMBAWANG DRIVE#03-363","pickup_hopeday":"2020-08-27","pickup_hopetime":"10:00-19:00","sender_nm":"Cody Tong","del_memo":"","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Cody Tong","partner_id":"263150561","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"C2C","del_driver_id":null,"cust_no":null,"ref_pickup_no":null,"lat_lng":null},{"contr_no":"90428484","partner_ref_no":"C3029324SGSG","invoice_no":"C3029324SGSG","stat":"P3","req_nm":"Sharlyn Bay","req_dt":"2020-08-2710:00-19:00","tel_no":"+65--","hp_no":"+65-9838-1136","zip_code":"750481","address":"481 SEMBAWANG DRIVE#10-467","pickup_hopeday":"2020-08-27","pickup_hopetime":"10:00-19:00","sender_nm":"Sharlyn Bay","del_memo":"","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Sharlyn Bay","partner_id":"Fruitarts","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"C2C","del_driver_id":null,"cust_no":null,"ref_pickup_no":null,"lat_lng":null},{"contr_no":"90600289","partner_ref_no":"C3039073SGSG","invoice_no":"C3039073SGSG","stat":"P3","req_nm":"Hazel Sim","req_dt":"2020-08-2710:00-19:00","tel_no":"+65-9113-3949","hp_no":"+65-9113-3949","zip_code":"750481","address":"481 SEMBAWANG DRIVE#11-467","pickup_hopeday":"2020-08-27","pickup_hopetime":"10:00-19:00","sender_nm":"Hazel Sim","del_memo":"Pls pick up before 4pm on 27/8","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Hazel Sim","partner_id":"hazelsim41","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":"C2C","del_driver_i
            resultObj = gson.fromJson(jsonString, PickupAssignResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getTodayPickupDone Json Exception : " + e.toString());
            resultObj = null;
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