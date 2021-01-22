package com.giosis.util.qdrive.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.barcodescanner.DriverAssignResult;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.PickupAssignResult;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ServerDownloadHelper {
    String TAG = "ServerDownloadHelper";

    Gson gson = new Gson();
    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String networkType;
    private final OnServerDownloadEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private String networkType;
        private OnServerDownloadEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public ServerDownloadHelper build() {
            return new ServerDownloadHelper(this);
        }

        Builder setOnServerDownloadEventListener(OnServerDownloadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }


    private ServerDownloadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }


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

            // NOTIFICATION.  2019.05 - Outlet 제외, 기본 Delivery, Pickup
            DriverAssignResult DeliveryServerList = getDeliveryServerData();
            //DriverAssignResult OutletDeliveryServerList = getOutletDeliveryServerData();
            PickupAssignResult PickupServerList = getPickupServerData();

            long maxCount = 0;
            long successCount = 0;

            if (DeliveryServerList != null) {
                maxCount += DeliveryServerList.getResultObject().size();
            }

          /*  if (OutletDeliveryServerList != null && OutletDeliveryServerList.getResultObject() != null) {
                maxCount += OutletDeliveryServerList.getResultObject().size();
            }*/

            if (PickupServerList != null) {
                maxCount += PickupServerList.getResultObject().size();
            }

            progressDialog.setMax((int) maxCount);

            if (maxCount < 1) {

                return maxCount;
            }

            if (DeliveryServerList != null) {
                for (DriverAssignResult.QSignDeliveryList shippingInfo : DeliveryServerList.getResultObject()) {

                    successCount = insertDeviceDeliveryData(shippingInfo);
                    publishProgress(1);
                }
            }

            /*if (OutletDeliveryServerList != null && OutletDeliveryServerList.getResultObject() != null) {

                for (DriverAssignResult.QSignDeliveryList outlet_shippingInfo : OutletDeliveryServerList.getResultObject()) {

                    successCount = insertDeviceOutletDeliveryData(outlet_shippingInfo);
                    publishProgress(1);
                }

          *//*     //   TODO  7E TEST
                //   가상의 값 DB에 밀어넣기~
                testInsertOutletDeliveryData("55001497", "TSGP177621", "7E 001 CR20181107001");
                testInsertOutletDeliveryData("55001498", "TSGP177622", "7E 001 CR20181107002");
                testInsertOutletDeliveryData("55001499", "TSGP177623", "7E 002 CR20181107001");*//*
            }*/

            if (PickupServerList != null) {
                for (PickupAssignResult.QSignPickupList pickupInfo : PickupServerList.getResultObject()) {

                    successCount = insertDevicePickupData(pickupInfo);
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
            }

            if (result < 1) {

                showResultDialog(context.getResources().getString(R.string.msg_no_data_to_download));
            } else {

                if (eventListener != null) {
                    eventListener.onDownloadResult();
                }
            }
        }
    }


    //
    private DriverAssignResult getDeliveryServerData() {

        DriverAssignResult resultObj;

        // JSON Parser
        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("officeCd", officeCode);
            job.accumulate("device_id", deviceID);
            job.accumulate("network_type", networkType);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);


            String methodName = "GetDeliveryList";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

            resultObj = gson.fromJson(jsonString, DriverAssignResult.class);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList Json Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }

    private PickupAssignResult getPickupServerData() {

        PickupAssignResult resultObj;

        // JSON Parser
        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            job.accumulate("officeCd", officeCode);
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


    private DriverAssignResult getOutletDeliveryServerData() {

        DriverAssignResult resultObj;

        // JSON Parser
        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", opID);
            // job.accumulate("opId", "7Eleven.Ajib");      // Syed_7E
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


    private long insertDeviceDeliveryData(DriverAssignResult.QSignDeliveryList data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

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
        // krm0219
        contentVal.put("order_type_etc", data.getOrder_type_etc());


        long insertCount = DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);

        return insertCount;
    }

    private long insertDeviceOutletDeliveryData(DriverAssignResult.QSignDeliveryList data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

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
        // krm0219
        contentVal.put("order_type_etc", data.getOrder_type_etc());


        long insertCount = DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);

        return insertCount;
    }

    private long insertDevicePickupData(PickupAssignResult.QSignPickupList data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", data.getContrNo());

        // NOTIFICATION.  19.10 - Ref.Pickup No가 존재하면 리스트에서 해당 번호로 표시
        if (!data.getRef_pickup_no().equals("")) {
            Log.e("krm0219", TAG + "  Ref. Pickup No >> " + data.getRef_pickup_no() + " / " + data.getPartnerRefNo());
            contentVal.put("partner_ref_no", data.getRef_pickup_no());
        } else {
            contentVal.put("partner_ref_no", data.getPartnerRefNo());
        }

        contentVal.put("invoice_no", data.getPartnerRefNo());  //invoice_no = partnerRefNo 사용
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

        if (data.getRoute().equals("RPC")) {
            contentVal.put("desired_time", data.getPickupHopeTime());
        }


        long insertCount = DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);

        return insertCount;
    }


    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_downloading));
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private AlertDialog getResultAlertDialog(Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_download_result))
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                        if (eventListener != null) {
                            eventListener.onDownloadResult();
                        }
                    }
                })
                .create();
        return dialog;
    }

    private void showResultDialog(String message) {
        try {

            resultDialog.setMessage(message);
            resultDialog.show();
        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }


    public ServerDownloadHelper execute() {
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute();
        return this;
    }

    public interface OnServerDownloadEventListener {
        void onDownloadResult();
    }
}