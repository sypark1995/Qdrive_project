package com.giosis.util.qdrive.barcodescanner;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.GeocoderUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChangeDriverHelper {
    String TAG = "ChangeDriverHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final ArrayList<ChangeDriverResult.Data> changeDriverObjectArrayList;
    private final double lat;
    private final double lon;

    private final String networkType;
    private final OnChangeDelDriverEventListener eventListener;
    private final ProgressDialog progressDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private final ArrayList<ChangeDriverResult.Data> changeDriverObjectArrayList;
        private final double lat;
        private final double lon;

        private String networkType;
        private OnChangeDelDriverEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<ChangeDriverResult.Data> list, double lat, double lon) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.changeDriverObjectArrayList = list;
            this.lat = lat;
            this.lon = lon;
        }

        public ChangeDriverHelper build() {
            return new ChangeDriverHelper(this);
        }

        Builder setOnChangeDelDriverEventListener(OnChangeDelDriverEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ChangeDriverHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.changeDriverObjectArrayList = builder.changeDriverObjectArrayList;
        this.lat = builder.lat;
        this.lon = builder.lon;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
    }

    private ProgressDialog getProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_driver_assign));
        progressDialog.setCancelable(false);

        return progressDialog;
    }


    class ChangeDriverAsyncTask extends AsyncTask<Void, Integer, DriverAssignResult> {

        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {

                int maxCount = changeDriverObjectArrayList.size();
                progressDialog.setMax(maxCount);
                progressDialog.show();
            }
        }

        @Override
        protected DriverAssignResult doInBackground(Void... params) {

            DriverAssignResult result = null;

            if (changeDriverObjectArrayList != null && 0 < changeDriverObjectArrayList.size()) {

                String ContrNoStr = null;

                for (ChangeDriverResult.Data item : changeDriverObjectArrayList) {

                    if (!TextUtils.isEmpty(item.getContrNo())) {

                        if (ContrNoStr == null) {

                            ContrNoStr = item.getContrNo();
                        } else {

                            ContrNoStr = ContrNoStr + "," + item.getContrNo();
                        }
                    }
                }

                result = changeDriver(ContrNoStr);
                publishProgress(1);
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progress += values[0];
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(DriverAssignResult resultList) {
            super.onPostExecute(resultList);

            DisplayUtil.dismissProgressDialog(progressDialog);


            if (resultList != null && resultList.getResultCode() == 0) {

                List<DriverAssignResult.QSignDeliveryList> resultObject = resultList.getResultObject();

                for (DriverAssignResult.QSignDeliveryList qSignDeliveryList : resultObject) {
                    if (!TextUtils.isEmpty(qSignDeliveryList.getPartnerRefNo().trim())) {

                        boolean success_insert = insertDriverAssignInfo(qSignDeliveryList);

                        if (success_insert) {
                            ChangeMessageAsyncTask changeMessageAsyncTask = new ChangeMessageAsyncTask(opID, qSignDeliveryList.getInvoiceNo());
                            changeMessageAsyncTask.execute();
                        }
                    }
                }
            }

            if (eventListener != null) {
                eventListener.onPostAssignResult(resultList);
            }
        }


        private DriverAssignResult changeDriver(String assignNo) {

            Gson gson = new Gson();
            DriverAssignResult resultObj;

            // JSON Parser
            try {

                JSONObject job = new JSONObject();
                job.accumulate("assignList", assignNo);
                job.accumulate("office_code", officeCode);
                job.accumulate("network_type", networkType);
                job.accumulate("del_driver_id", opID);
                job.accumulate("device_id", deviceID);
                job.accumulate("lat", String.valueOf(lat));
                job.accumulate("lon", String.valueOf(lon));
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "SetChangeDeliveryDriver";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultObject":[{"contr_no":"90256451","partner_ref_no":"SGSG23614214","invoice_no":"SGP163532597","stat":"D3","rcv_nm":"Ang Boon Sin","tel_no":"+65--","hp_no":"+65-9172-5419","zip_code":"791412","address":"412A FERNVALE LINK#05-13,  Singapore","sender_nm":"jenny","del_memo":"","driver_memo":"","fail_reason":"  ","partner_ref_no_fail_assign":null,"reason_fail_assign":null,"delivery_count":"0","delivery_first_date":"2020-08-23","route":"GIO","secret_no_type":" ","secret_no":"","del_hopeday":null,"course":null,"course_driver":null,"secure_delivery_yn":"N","parcel_amount":"20.41","currency":"SGD","qwms_yn":null,"order_type_etc":"DPC","del_hopedaybyDBData":null,"del_hopetime":null,"GoogleMap":null,"delivery_nation_cd":null,"lat_lng":"1.389179,103.877918"}],"ResultCode":0,"ResultMsg":"Success"}
                resultObj = gson.fromJson(jsonString, DriverAssignResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetChangeDeliveryDriver Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }


    private boolean insertDriverAssignInfo(DriverAssignResult.QSignDeliveryList assignInfo) {

//        String opId = SharedPreferencesHelper.getSigninOpID(context);
        String opId = MyApplication.preferences.getUserId();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        // eylee 2015.08.26 add nonqoo10 - contr_no 로 sqlite 체크 후 있다면 삭제하는 로직 add start
        String contr_no = assignInfo.getContrNo();
        int cnt = getContrNoCount(contr_no);
        if (0 < cnt) {
            delSelectedContrNo(contr_no);
        }

        // eylee 2015.08.26 add end
        //성공 시 통합리스트 테이블 저장
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", assignInfo.getContrNo());
        contentVal.put("partner_ref_no", assignInfo.getPartnerRefNo());
        contentVal.put("invoice_no", assignInfo.getInvoiceNo());
        contentVal.put("stat", assignInfo.getStat());
        contentVal.put("rcv_nm", assignInfo.getRcvName());
        contentVal.put("sender_nm", assignInfo.getSenderName());
        contentVal.put("tel_no", assignInfo.getTelNo());
        contentVal.put("hp_no", assignInfo.getHpNo());
        contentVal.put("zip_code", assignInfo.getZipCode());
        contentVal.put("address", assignInfo.getAddress());
        contentVal.put("rcv_request", assignInfo.getDelMemo());
        contentVal.put("delivery_dt", assignInfo.getDeliveryFirstDate());
        contentVal.put("delivery_cnt", assignInfo.getDeliveryCount());
        contentVal.put("type", BarcodeType.TYPE_DELIVERY);
        contentVal.put("route", assignInfo.getRoute());
        contentVal.put("reg_id", opId);
        contentVal.put("reg_dt", regDataString);
        contentVal.put("punchOut_stat", "N");
        contentVal.put("driver_memo", assignInfo.getDriverMemo());
        contentVal.put("fail_reason", assignInfo.getFailReason());
        contentVal.put("secret_no_type", assignInfo.getSecretNoType());
        contentVal.put("secret_no", assignInfo.getSecretNo());
        contentVal.put("secure_delivery_yn", assignInfo.getSecureDeliveryYN());
        contentVal.put("parcel_amount", assignInfo.getParcelAmount());
        contentVal.put("currency", assignInfo.getCurrency());
        contentVal.put("order_type_etc", assignInfo.getOrder_type_etc());   // krm0219

        // 2020.06 위, 경도 저장
        String[] latLng = GeocoderUtil.getLatLng(assignInfo.getLat_lng());
        contentVal.put("lat", latLng[0]);
        contentVal.put("lng", latLng[1]);

        long insertCount = DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
        return insertCount >= 0;
    }

    // krm0219
    @SuppressLint("StaticFieldLeak")
    class ChangeMessageAsyncTask extends AsyncTask<Void, Void, StdResult> {

        String driverId;
        String trackingNo;


        ChangeMessageAsyncTask(String DriverID, String TrackingNo) {

            driverId = DriverID;
            trackingNo = TrackingNo;
        }

        @Override
        protected StdResult doInBackground(Void... voids) {

            StdResult result = new StdResult();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("tracking_no", trackingNo);
                job.accumulate("svc_nation_cd", "SG");
                job.accumulate("qdriver_id", driverId);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "SetQdriverMessageChangeQdriver";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultObject":null,"ResultCode":0,"ResultMsg":"OK"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetQdriverMessageChangeQdriver Exception : " + e.toString());

                result.setResultCode(-15);
                result.setResultMsg("Exception : " + e.toString());
            }

            return result;
        }

        @Override
        protected void onPostExecute(StdResult result) {
            super.onPostExecute(result);

            if (result.getResultCode() == 0) {

                Log.e(TAG, "  ChangeMessageAsyncTask Success");
            } else {

                Toast.makeText(context, context.getResources().getString(R.string.msg_message_change_error), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private int getContrNoCount(String contr_no) {

        String sql = "SELECT count(*) as contrno_cnt FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE contr_no='" + contr_no + "' COLLATE NOCASE";
        Cursor cursor = DatabaseHelper.getInstance().get(sql);

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("contrno_cnt"));
        }

        cursor.close();

        return count;
    }


    private void delSelectedContrNo(String contr_no) {

        DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "contr_no='" + contr_no + "' COLLATE NOCASE");
    }

    public ChangeDriverHelper execute() {
        ChangeDriverAsyncTask changeDriverAsyncTask = new ChangeDriverAsyncTask();
        changeDriverAsyncTask.execute();
        return this;
    }

    public interface OnChangeDelDriverEventListener {
        void onPostAssignResult(DriverAssignResult stdResult);
    }
}