package com.giosis.util.qdrive.main;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.DriverAssignResult;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.ChildItem;
import com.giosis.util.qdrive.list.RowItem;
import com.giosis.util.qdrive.list.PickupAssignResult;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

// Outlet Order Status
public class OutletOrderStatusDownloadHelper {
    String TAG = "OutletStatusDownloadHelper";

    Gson gson = new Gson();
    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final int outletStatusPosition;

    private final String networkType;
    private final OnOutletStatusDownloadListener eventListener;
    private final ProgressDialog progressDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private final int outletStatusPosition;

        private String networkType;
        private OnOutletStatusDownloadListener eventListener;


        public Builder(Context context, String opID, String officeCode, String deviceID, int outletStatusPosition) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.outletStatusPosition = outletStatusPosition;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public OutletOrderStatusDownloadHelper build() {
            return new OutletOrderStatusDownloadHelper(this);
        }

        Builder setOnOutletStatusDownloadListener(OnOutletStatusDownloadListener eventListener) {

            this.eventListener = eventListener;
            return this;
        }
    }

    private OutletOrderStatusDownloadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.outletStatusPosition = builder.outletStatusPosition;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
    }


    // 리스트 만들 RowItem array (delivery + pickup)
    private ArrayList<RowItem> outletDataArrayList;

    class DownloadTask extends AsyncTask<Void, Integer, Long> {

        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {
                progressDialog.show();
            }
        }

        @Override
        protected Long doInBackground(Void... params) {

            outletDataArrayList = new ArrayList<>();
            DriverAssignResult OutletDeliveryServerList = null;
            PickupAssignResult PickupServerList = null;

            long maxCount = 0;
            long successCount = 0;

            if (outletStatusPosition == 1) {

                OutletDeliveryServerList = getOutletRealTimeData("tobe_scanned");
            } else if (outletStatusPosition == 2) {

                OutletDeliveryServerList = getOutletDeliveryServerData();
                PickupServerList = getPickupServerData();
            }
            if (outletStatusPosition == 3) {

                OutletDeliveryServerList = getOutletRealTimeData("done_all");
            }


            if (OutletDeliveryServerList != null && OutletDeliveryServerList.getResultObject() != null) {
                maxCount += OutletDeliveryServerList.getResultObject().size();
            }

            if (PickupServerList != null) {
                maxCount += PickupServerList.getResultObject().size();
            }

            progressDialog.setMax((int) maxCount);

            if (maxCount < 1) {
                return maxCount;
            }


            if (OutletDeliveryServerList != null && OutletDeliveryServerList.getResultObject() != null) {
                for (DriverAssignResult.QSignDeliveryList outlet_shippingInfo : OutletDeliveryServerList.getResultObject()) {

                    successCount = setOutletDeliveryData(outlet_shippingInfo);
                    publishProgress(1);
                }
          /*     //   TODO  7E TEST
                //   가상의 값 DB에 밀어넣기~
                testInsertOutletDeliveryData("55001497", "TSGP177621", "7E 001 CR20181107001");
                testInsertOutletDeliveryData("55001498", "TSGP177622", "7E 001 CR20181107002");
                testInsertOutletDeliveryData("55001499", "TSGP177623", "7E 002 CR20181107001");*/
            }

            if (PickupServerList != null) {
                for (PickupAssignResult.QSignPickupList pickupInfo : PickupServerList.getResultObject()) {
                    successCount = setOutletPickupData(pickupInfo);
                    publishProgress(1);
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
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    DisplayUtil.dismissProgressDialog(progressDialog);
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                // !((Activity)context).isFinishing()
            }

            if (eventListener != null) {
                eventListener.onDownloadResult(outletDataArrayList);
            }
        }
    }


    private DriverAssignResult getOutletRealTimeData(String type) {

        DriverAssignResult resultObj;

        try {

            JSONObject job = new JSONObject();
            job.accumulate("type", type);
            job.accumulate("opId", opID);
            job.accumulate("officeCd", officeCode);
            job.accumulate("exceptList", "");
            job.accumulate("assignList", "");
            job.accumulate("device_id", deviceID);
            job.accumulate("network_type", networkType);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);


            String methodName = "GetDeliveryList_OutletRealTime";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

            resultObj = gson.fromJson(jsonString, DriverAssignResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList_OutletRealTime Json Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }


    private DriverAssignResult getOutletDeliveryServerData() {

        DriverAssignResult resultObj;

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("officeCd", officeCode);
            job.accumulate("exceptList", "");
            job.accumulate("assignList", "");
            job.accumulate("device_id", deviceID);
            job.accumulate("network_type", networkType);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);


            String methodName = "GetDeliveryList_Outlet";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

            resultObj = gson.fromJson(jsonString, DriverAssignResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList_Outlet Json Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }

    private PickupAssignResult getPickupServerData() {

        PickupAssignResult resultObj;

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("officeCd", officeCode);
            job.accumulate("exceptList", "");
            job.accumulate("assignList", "");
            job.accumulate("device_id", deviceID);
            job.accumulate("network_type", networkType);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);


            String methodName = "GetPickupList";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

            resultObj = gson.fromJson(jsonString, PickupAssignResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetPickupList Json Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }


    // Outlet Delivery Data setting
    private long setOutletDeliveryData(DriverAssignResult.QSignDeliveryList data) {

        if (outletStatusPosition == 2) {

            insertDeviceOutletDeliveryData(data);
        }

        try {
            ArrayList<ChildItem> childItemArrayList = new ArrayList<>();
            ChildItem childItem = new ChildItem();

            childItem.setHp(data.getHpNo());
            childItem.setTel(data.getTelNo());
            childItem.setStat(data.getStat());
            childItem.setStatMsg(data.getDriverMemo());
            childItem.setStatReason(data.getFailReason());
            childItem.setSecretNoType(data.getSecretNoType());
            childItem.setSecretNo(data.getSecretNo());
            childItemArrayList.add(childItem);

            //
            long delay = 0;
            if (data.getDeliveryFirstDate() != null && !data.getDeliveryFirstDate().equals("")) {
                try {

                    delay = diffOfDate(data.getDeliveryFirstDate());
                } catch (Exception e) {

                    Log.e("Exception", TAG + "  diffOfDate Exception : " + e.toString());
                }
            }

            RowItem rowItem = new RowItem(data.getContrNo(), "D+" + delay, data.getInvoiceNo(), data.getRcvName(),
                    "(" + data.getZipCode() + ")" + data.getAddress(), data.getDelMemo(), "D", data.getRoute(),
                    data.getSenderName(), "", "", "", 0, 0, data.getStat(), "",
                    "", data.getSecureDeliveryYN(), data.getParcelAmount(), data.getCurrency());

            rowItem.setOrder_type_etc(data.getOrder_type_etc());
            rowItem.setOutlet_company(data.getRoute());
            rowItem.setZip_code(data.getZipCode());

            String[] routeSplit = data.getRoute().split(" ");

            if (1 < routeSplit.length) {

                StringBuilder sb = new StringBuilder();

                for (int j = 2; j < routeSplit.length; j++) {

                    sb.append(routeSplit[j]);
                    sb.append(" ");
                }

                rowItem.setOutlet_company(routeSplit[0]);
                rowItem.setOutlet_store_code(routeSplit[1]);
                rowItem.setOutlet_store_name(sb.toString().trim());
                rowItem.setOutlet_qty(1);
            }

            /*  R3, R4의 경우 */
            if (data.getStat().equals("DX")) {

                rowItem.setType("P");
            }

            rowItem.setItems(childItemArrayList);
            outletDataArrayList.add(rowItem);

            return 100;
        } catch (Exception e) {

            return 0;
        }
    }

    // Outlet Retrieve  Data setting
    private long setOutletPickupData(PickupAssignResult.QSignPickupList data) {

        if (outletStatusPosition == 2) {

            insertDevicePickupData(data);
        }

        try {

            ArrayList<ChildItem> childItemArrayList = new ArrayList<>();
            ChildItem childItem = new ChildItem();

            childItem.setHp(data.getHpNo());
            childItem.setTel(data.getTelNo());
            childItem.setStat(data.getStat());
            childItem.setStatMsg(data.getDriverMemo());
            childItem.setStatReason(data.getFailReason());
            childItem.setSecretNoType(data.getSecretNoType());
            childItem.setSecretNo(data.getSecretNo());
            childItemArrayList.add(childItem);

            //
            RowItem rowItem = new RowItem(data.getContrNo(), "D+0", data.getInvoiceNo(), data.getReqName(),
                    "(" + data.getZipCode() + ")" + data.getAddress(), data.getDelMemo(), "P", data.getRoute(),
                    "", data.getPickupHopeDay(), data.getQty(), "", 0, 0, data.getStat(), data.getCustNo(),
                    data.getPartnerID(), "", "", "");

            if (data.getRoute().equals("RPC")) {
                rowItem.setDesired_time(data.getPickupHopeTime());
            }

            rowItem.setOutlet_company(data.getRoute());
            rowItem.setZip_code(data.getZipCode());

            if (data.getRoute().contains("7E") || data.getRoute().contains("FL")) {

                String[] routeSplit = data.getRoute().split(" ");

                if (1 < routeSplit.length) {

                    StringBuilder sb = new StringBuilder();

                    for (int j = 2; j < routeSplit.length; j++) {

                        sb.append(routeSplit[j]);
                        sb.append(" ");
                    }

                    rowItem.setOutlet_company(routeSplit[0]);
                    rowItem.setOutlet_store_code(routeSplit[1]);
                    rowItem.setOutlet_store_name(sb.toString().trim());
                }
            }

            rowItem.setItems(childItemArrayList);
            outletDataArrayList.add(rowItem);

            return 100;
        } catch (Exception e) {

            return 0;
        }
    }


    private static long diffOfDate(String begin) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date beginDate = formatter.parse(begin);
        Date endDate = new Date();

        long diff = endDate.getTime() - beginDate.getTime();

        return diff / (24 * 60 * 60 * 1000);
    }


    private void insertDeviceOutletDeliveryData(DriverAssignResult.QSignDeliveryList data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", data.getContrNo());
        contentVal.put("partner_ref_no", data.getPartnerRefNo());
        contentVal.put("invoice_no", data.getInvoiceNo());
        contentVal.put("stat", data.getStat());
        contentVal.put("rcv_nm", data.getRcvName());
        contentVal.put("sender_nm", data.getSenderName());
        contentVal.put("tel_no", data.getTelNo());
        contentVal.put("hp_no", data.getHpNo());
        contentVal.put("zip_code", data.getZipCode());
        contentVal.put("address", data.getAddress());
        contentVal.put("rcv_request", data.getDelMemo());
        contentVal.put("delivery_dt", data.getDeliveryFirstDate());
        contentVal.put("delivery_cnt", data.getDeliveryCount());
        contentVal.put("type", "D");
        contentVal.put("route", data.getRoute());
        contentVal.put("reg_id", opID);
        contentVal.put("reg_dt", regDataString);
        contentVal.put("punchOut_stat", "N");
        contentVal.put("driver_memo", data.getDriverMemo());
        contentVal.put("fail_reason", data.getFailReason());
        contentVal.put("secret_no_type", data.getSecretNoType());
        contentVal.put("secret_no", data.getSecretNo());
        contentVal.put("secure_delivery_yn", data.getSecureDeliveryYN());
        contentVal.put("parcel_amount", data.getParcelAmount());
        contentVal.put("currency", data.getCurrency());
        contentVal.put("order_type_etc", data.getOrder_type_etc());

        dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
    }


    private void insertDevicePickupData(PickupAssignResult.QSignPickupList data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", data.getContrNo());
        contentVal.put("partner_ref_no", data.getPartnerRefNo());
        contentVal.put("invoice_no", data.getPartnerRefNo());
        contentVal.put("stat", data.getStat());
        contentVal.put("tel_no", data.getTelNo());
        contentVal.put("hp_no", data.getHpNo());
        contentVal.put("zip_code", data.getZipCode());
        contentVal.put("address", data.getAddress());
        contentVal.put("route", data.getRoute());
        contentVal.put("type", "P");
        contentVal.put("desired_date", data.getPickupHopeDay());
        contentVal.put("req_qty", data.getQty());
        contentVal.put("req_nm", data.getReqName());
        contentVal.put("failed_count", data.getFailedCount());
        contentVal.put("rcv_request", data.getDelMemo());
        contentVal.put("sender_nm", "");
        contentVal.put("punchOut_stat", "N");
        contentVal.put("reg_id", opID);
        contentVal.put("reg_dt", regDataString);
        contentVal.put("fail_reason", data.getFailReason());
        contentVal.put("secret_no_type", data.getSecretNoType());
        contentVal.put("secret_no", data.getSecretNo());
        contentVal.put("cust_no", data.getCustNo()); //QLPS cust_no
        contentVal.put("partner_id", data.getPartnerID()); //QLPS partner_cust_id

        dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
    }


    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_downloading));
        progressDialog.setCancelable(false);
        return progressDialog;
    }


    public OutletOrderStatusDownloadHelper execute() {
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute();
        return this;
    }

    public interface OnOutletStatusDownloadListener {
        void onDownloadResult(ArrayList<RowItem> resultList);
    }
}