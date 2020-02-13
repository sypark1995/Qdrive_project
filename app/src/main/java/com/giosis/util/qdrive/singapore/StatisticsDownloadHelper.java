package com.giosis.util.qdrive.singapore;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class StatisticsDownloadHelper extends ManualHelper {
    private String TAG = "StatisticsDownloadHelper";

    private final Context context;
    private final String opID;
    private final String searchType;
    private final String startDate;
    private final String endDate;
    private final String status;

    private final OnStatisticsDownloadListener eventListener;
    private final ProgressDialog progressDialog;

    private StatisticsResult statisticsResult;


    public static class Builder {

        private final Context context;
        private final String opID;
        private final String searchType;
        private final String startDate;
        private final String endDate;
        private final String status;

        private OnStatisticsDownloadListener eventListener;


        public Builder(Context context, String opID, String searchType, String startDate, String endDate, String status) {

            this.context = context;
            this.opID = opID;
            this.searchType = searchType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }

        public StatisticsDownloadHelper build() {
            return new StatisticsDownloadHelper(this);
        }

        Builder setOnStatisticsDownloadListener(OnStatisticsDownloadListener eventListener) {

            this.eventListener = eventListener;
            return this;
        }
    }

    private StatisticsDownloadHelper(Builder builder) {
        this.context = builder.context;
        this.opID = builder.opID;
        this.searchType = builder.searchType;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.status = builder.status;

        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
    }


    class StatisticsTask extends AsyncTask<Void, Integer, Integer> {

        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {
                progressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            int maxCount = 0;
            int successCount = 0;


            if (searchType.contains("Summary")) {
                // D_Summary    P_Summary

                if (searchType.contains("D_S")) {

                    statisticsResult = getDeliverySummary();
                } else if (searchType.contains("P_S")) {

                    statisticsResult = getPickupSummary();
                }

                if (statisticsResult != null && statisticsResult.getSummaryDataArrayList() != null) {
                    maxCount += statisticsResult.getSummaryDataArrayList().size();
                }
                progressDialog.setMax(maxCount);

                if (maxCount < 1) {
                    return maxCount;
                }

                if (statisticsResult != null && statisticsResult.getSummaryDataArrayList() != null) {
                    for (int i = 0; i < statisticsResult.getSummaryDataArrayList().size(); i++) {

                        successCount = i;
                        publishProgress(1);
                    }
                }
            } else if (searchType.contains("Detail")) {
                // D_Detail    P_Detail

                if (searchType.contains("D_D")) {

                    statisticsResult = getDeliveryDetail();
                } else if (searchType.contains("P_D")) {

                    statisticsResult = getPickupDetail();
                }

                if (statisticsResult != null && statisticsResult.getDetailDataArrayList() != null) {
                    maxCount += statisticsResult.getDetailDataArrayList().size();
                }
                progressDialog.setMax(maxCount);

                if (maxCount < 1) {
                    return maxCount;
                }

                if (statisticsResult != null && statisticsResult.getDetailDataArrayList() != null) {
                    for (int i = 0; i < statisticsResult.getDetailDataArrayList().size(); i++) {

                        successCount = i;
                        publishProgress(1);
                    }
                }
            }

            return successCount;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progress += values[0];
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    DisplayUtil.dismissProgressDialog(progressDialog);
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }

            if (eventListener != null) {
                eventListener.onDownloadResult(searchType, statisticsResult);
            }
        }
    }


    private StatisticsResult getDeliverySummary() {

        StatisticsResult result = new StatisticsResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("search_type", searchType);
            job.accumulate("date_from", startDate);
            job.accumulate("date_to", endDate);
            job.accumulate("del_driver_id", opID);
            job.accumulate("status", "");
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetStaticDeliverySummary";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            //  {"ResultObject":[],"ResultCode":0,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray resultArray = jsonObject.getJSONArray("ResultObject");

          /*  // TEST.
            if (MOBILE_SERVER_URL.contains("test")) {
                String abc = "[{\"dpc3out_dt\":\"2019-12-04\",\"total_cnt\":\"10\",\"delivered_ctn\":\"8\",\"delivered_prcntg\":\"80\", \"avg_delivery_day\":\"5\"}," +
                        "{\"dpc3out_dt\":\"2019-12-05\",\"total_cnt\":\"10\",\"delivered_ctn\":\"8\",\"delivered_prcntg\":\"80\", \"avg_delivery_day\":\"4\"}," +
                        "{\"dpc3out_dt\":\"2019-12-06\",\"total_cnt\":\"10\",\"delivered_ctn\":\"8\",\"delivered_prcntg\":\"80\", \"avg_delivery_day\":\"6\"}]";

                resultArray = new JSONArray(abc);
            }*/

            int resultCode = jsonObject.getInt("ResultCode");
            String resultMsg = jsonObject.getString("ResultMsg");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

            ArrayList<StatisticsResult.SummaryData> deliverySummaryArrayList = new ArrayList<>();

            for (int i = 0; i < resultArray.length(); i++) {

                JSONObject resultObject = resultArray.getJSONObject(i);

                StatisticsResult.SummaryData deliverySummary = new StatisticsResult.SummaryData();
                deliverySummary.setDate(resultObject.getString("dpc3out_dt"));
                deliverySummary.setTotalCount(resultObject.getInt("total_cnt"));
                deliverySummary.setDeliveredCount(resultObject.getInt("delivered_ctn"));
                deliverySummary.setPercent(resultObject.getString("delivered_prcntg"));
                deliverySummary.setAvgDate(resultObject.getString("avg_delivery_day"));
                deliverySummaryArrayList.add(deliverySummary);
            }

            result.setSummaryDataArrayList(deliverySummaryArrayList);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetStaticDeliverySummary Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }

    private StatisticsResult getDeliveryDetail() {

        StatisticsResult result = new StatisticsResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("search_type", searchType);
            job.accumulate("date_from", startDate);
            job.accumulate("date_to", endDate);
            job.accumulate("del_driver_id", opID);
            job.accumulate("status", status);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetStaticDeliveryDetail";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            //  {"ResultObject":[],"ResultCode":0,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray resultArray = jsonObject.getJSONArray("ResultObject");

         /*   // TEST.
            if (MOBILE_SERVER_URL.contains("test")) {

                String abc = "[{\"shipping_no\":\"S1234\",\"tracking_no\":\"T1234\",\"stat\":\"D3\",\"delivered_dt\":\"2019-12-04\"}," +
                        "{\"shipping_no\":\"S5678\",\"tracking_no\":\"T5678\",\"stat\":\"D4\",\"delivered_dt\":\"2019-12-05\"}," +
                        "{\"shipping_no\":\"S9012\",\"tracking_no\":\"T9012\",\"stat\":\"D4\",\"delivered_dt\":\"2019-12-06\"}]";

                resultArray = new JSONArray(abc);
            }*/

            int resultCode = jsonObject.getInt("ResultCode");
            String resultMsg = jsonObject.getString("ResultMsg");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

            ArrayList<StatisticsResult.DetailData> deliveryDetailArrayList = new ArrayList<>();

            for (int i = 0; i < resultArray.length(); i++) {

                JSONObject resultObject = resultArray.getJSONObject(i);

                StatisticsResult.DetailData deliveryDetail = new StatisticsResult.DetailData();
                deliveryDetail.setShippingNo(resultObject.getString("shipping_no"));
                deliveryDetail.setTrackingNo(resultObject.getString("tracking_no"));
                deliveryDetail.setStat(resultObject.getString("stat"));
                deliveryDetail.setDate(resultObject.getString("delivered_dt"));
                deliveryDetailArrayList.add(deliveryDetail);
            }

            result.setDetailDataArrayList(deliveryDetailArrayList);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetStaticDeliverySummary Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }

    private StatisticsResult getPickupSummary() {

        StatisticsResult result = new StatisticsResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("search_type", searchType);
            job.accumulate("date_from", startDate);
            job.accumulate("date_to", endDate);
            job.accumulate("del_driver_id", opID);
            job.accumulate("status", "");
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetStaticPickupSummary";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            //  {"ResultObject":[],"ResultCode":0,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray resultArray = jsonObject.getJSONArray("ResultObject");

           /*// TEST.
            if (MOBILE_SERVER_URL.contains("test")) {
                String abc = "[{\"desired_dt\":\"2019-12-04\",\"total_cnt\":\"10\",\"done\":\"8\",\"failed\":\"1\",\"confirmed\":\"1\",\"done_prcntg\":\"80\", \"avg_done_day\":\"5\"}," +
                        "{\"desired_dt\":\"2019-12-05\",\"total_cnt\":\"10\",\"done\":\"8\",\"failed\":\"1\",\"confirmed\":\"1\",\"done_prcntg\":\"80\", \"avg_done_day\":\"4\"}," +
                        "{\"desired_dt\":\"2019-12-06\",\"total_cnt\":\"10\",\"done\":\"8\",\"failed\":\"1\",\"confirmed\":\"1\",\"done_prcntg\":\"80\", \"avg_done_day\":\"6\"}]";

                resultArray = new JSONArray(abc);
            }*/

            int resultCode = jsonObject.getInt("ResultCode");
            String resultMsg = jsonObject.getString("ResultMsg");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

            ArrayList<StatisticsResult.SummaryData> pickupSummaryArrayList = new ArrayList<>();

            for (int i = 0; i < resultArray.length(); i++) {

                JSONObject resultObject = resultArray.getJSONObject(i);

                StatisticsResult.SummaryData pickupSummary = new StatisticsResult.SummaryData();
                pickupSummary.setDate(resultObject.getString("desired_dt"));
                pickupSummary.setTotalCount(resultObject.getInt("total_cnt"));
                pickupSummary.setDoneCount(resultObject.getInt("done"));
                pickupSummary.setFailedCount(resultObject.getInt("failed"));
                pickupSummary.setConfirmedCount(resultObject.getInt("confirmed"));
                pickupSummary.setPercent(resultObject.getString("done_prcntg"));
                pickupSummary.setAvgDate(resultObject.getString("avg_done_day"));
                pickupSummaryArrayList.add(pickupSummary);
            }

            result.setSummaryDataArrayList(pickupSummaryArrayList);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetStaticDeliverySummary Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }

    private StatisticsResult getPickupDetail() {

        StatisticsResult result = new StatisticsResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("search_type", searchType);
            job.accumulate("date_from", startDate);
            job.accumulate("date_to", endDate);
            job.accumulate("del_driver_id", opID);
            job.accumulate("status", status);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetStaticPickupDetail";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            //  {"ResultObject":[],"ResultCode":0,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray resultArray = jsonObject.getJSONArray("ResultObject");

          /*// TEST.
            if (MOBILE_SERVER_URL.contains("test")) {
                String abc = "[{\"pickup_no\":\"P1234\",\"qty\":\"11\",\"stat\":\"P2\",\"desired_dt\":\"2019-12-04\"}," +
                        "{\"pickup_no\":\"P5678\",\"qty\":\"22\",\"stat\":\"P3\",\"desired_dt\":\"2019-12-05\"}," +
                        "{\"pickup_no\":\"P9012\",\"qty\":\"33\",\"stat\":\"P3\",\"desired_dt\":\"2019-12-06\"}]";

                resultArray = new JSONArray(abc);
            }*/

            int resultCode = jsonObject.getInt("ResultCode");
            String resultMsg = jsonObject.getString("ResultMsg");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

            ArrayList<StatisticsResult.DetailData> pickupDetailArrayList = new ArrayList<>();

            for (int i = 0; i < resultArray.length(); i++) {

                JSONObject resultObject = resultArray.getJSONObject(i);

                StatisticsResult.DetailData pickupDetail = new StatisticsResult.DetailData();
                pickupDetail.setPickupNo(resultObject.getString("pickup_no"));
                pickupDetail.setStat(resultObject.getString("stat"));
                pickupDetail.setPickupQty(resultObject.getString("qty"));
                pickupDetail.setDate(resultObject.getString("desired_dt"));
                pickupDetailArrayList.add(pickupDetail);
            }

            result.setDetailDataArrayList(pickupDetailArrayList);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetStaticDeliverySummary Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    //
    public interface OnStatisticsDownloadListener {
        void onDownloadResult(String searchType, StatisticsResult result);
    }

    public StatisticsDownloadHelper execute() {
        StatisticsTask statisticsTask = new StatisticsTask();
        statisticsTask.execute();
        return this;
    }
}