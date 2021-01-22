package com.giosis.util.qdrive.main;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.library.list.BarcodeData;
import com.giosis.util.qdrive.barcodescanner.DriverAssignResult;
import com.giosis.util.qdrive.international.R;
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
import java.util.List;
import java.util.TimeZone;

public class ManualDriverAssignHelper {
    String TAG = "ManualDriverAssignHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final ArrayList<BarcodeData> confirmOrderBarcodeList;

    private final String networkType;
    private final OnDriverAssignV2EventListener eventListener;
    private final ProgressDialog progressDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private final ArrayList<BarcodeData> confirmOrderBarcodeList;

        private String networkType;
        private OnDriverAssignV2EventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<BarcodeData> confirmOrderBarcodeList) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.confirmOrderBarcodeList = confirmOrderBarcodeList;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public ManualDriverAssignHelper build() {
            return new ManualDriverAssignHelper(this);
        }

        public Builder setOnDriverAssignV2EventListener(OnDriverAssignV2EventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualDriverAssignHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.confirmOrderBarcodeList = builder.confirmOrderBarcodeList;

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


    class DriverAssignTask extends AsyncTask<Void, Integer, DriverAssignResult> {
        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {
                int maxCount = confirmOrderBarcodeList.size();
                progressDialog.setMax(maxCount);
                progressDialog.show();
            }
        }

        @Override
        protected DriverAssignResult doInBackground(Void... params) {

            DriverAssignResult result = null;

            if (confirmOrderBarcodeList != null && 0 < confirmOrderBarcodeList.size()) {

                String str = null;
                for (BarcodeData assignData : confirmOrderBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        if (str == null) {
                            str = assignData.getBarcode();
                        } else {
                            str = str + "," + assignData.getBarcode();
                        }
                    }
                }

                result = requestDriverAssign(str);
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

            try {

                DisplayUtil.dismissProgressDialog(progressDialog);


                int resultCode = resultList.getResultCode();

                if (resultCode == 0) {

                    List<DriverAssignResult.QSignDeliveryList> resultObject = resultList.getResultObject();

                    for (DriverAssignResult.QSignDeliveryList qSignDeliveryList : resultObject) {
                        if (!TextUtils.isEmpty(qSignDeliveryList.getPartnerRefNo().trim())) {
                            insertDriverAssignInfo(qSignDeliveryList);
                        }
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }

            if (eventListener != null) {
                eventListener.onPostAssignResult(resultList);
            }
        }


        private DriverAssignResult requestDriverAssign(String assignNo) {

            Gson gson = new Gson();
            DriverAssignResult resultObj;

            // JSON Parser
            try {

                JSONObject job = new JSONObject();
                job.accumulate("assignList", assignNo);
                job.accumulate("office_code", officeCode);
                job.accumulate("del_driver_id", opID);
                job.accumulate("device_id", deviceID);
                job.accumulate("stat_chg_gubun", "D");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "SetShippingStatDpc3out";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                resultObj = gson.fromJson(jsonString, DriverAssignResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetChangeDeliveryDriver Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }


    private void insertDriverAssignInfo(DriverAssignResult.QSignDeliveryList assignInfo) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

        // eylee 2015.08.26 add nonqoo10 - contr_no 로 sqlite 체크 후 있다면 삭제하는 로직 add start
        String contry_no = assignInfo.getContrNo();
        int cnt = getContrNoCount(contry_no);
        if (0 < cnt) {
            deleteSelectedContrNo(contry_no);
        }

        // eylee 2015.08.26 add end
        //성공 시 통합리스트 테이블 저장
        ContentValues contentVal2 = new ContentValues();
        contentVal2.put("contr_no", assignInfo.getContrNo());
        contentVal2.put("partner_ref_no", assignInfo.getPartnerRefNo());
        contentVal2.put("invoice_no", assignInfo.getInvoiceNo());
        contentVal2.put("stat", assignInfo.getStat());
        contentVal2.put("rcv_nm", assignInfo.getRcvName());
        contentVal2.put("sender_nm", assignInfo.getSenderName());
        contentVal2.put("tel_no", assignInfo.getTelNo());
        contentVal2.put("hp_no", assignInfo.getHpNo());
        contentVal2.put("zip_code", assignInfo.getZipCode());
        contentVal2.put("address", assignInfo.getAddress());
        contentVal2.put("rcv_request", assignInfo.getDelMemo());
        contentVal2.put("delivery_dt", assignInfo.getDeliveryFirstDate());
        contentVal2.put("delivery_cnt", assignInfo.getDeliveryCount());
        contentVal2.put("type", "D");
        contentVal2.put("route", assignInfo.getRoute());
        contentVal2.put("reg_id", opID);
        contentVal2.put("reg_dt", regDataString);
        contentVal2.put("punchOut_stat", "N");
        contentVal2.put("driver_memo", assignInfo.getDriverMemo());
        contentVal2.put("fail_reason", assignInfo.getFailReason());
        contentVal2.put("secret_no_type", assignInfo.getSecretNoType());
        contentVal2.put("secret_no", assignInfo.getSecretNo());

        // 2018-03-09 eylee bug fix
        contentVal2.put("secure_delivery_yn", assignInfo.getSecureDeliveryYN());
        contentVal2.put("parcel_amount", assignInfo.getParcelAmount());
        contentVal2.put("currency", assignInfo.getCurrency());
        // krm0219
        contentVal2.put("order_type_etc", assignInfo.getOrder_type_etc());

        dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2);
    }

    private int getContrNoCount(String contr_no) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        String sql = "SELECT count(*) as contryno_cnt FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE contr_no='" + contr_no + "' COLLATE NOCASE";
        Cursor cursor = dbHelper.get(sql);

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("contryno_cnt"));
        }

        cursor.close();
        return count;
    }

    private void deleteSelectedContrNo(String contr_no) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "contr_no='" + contr_no + "' COLLATE NOCASE");
    }


    public ManualDriverAssignHelper execute() {
        DriverAssignTask driverAssignTask = new DriverAssignTask();
        driverAssignTask.execute();
        return this;
    }

    public interface OnDriverAssignV2EventListener {
        void onPostAssignResult(DriverAssignResult stdResult);
    }
}