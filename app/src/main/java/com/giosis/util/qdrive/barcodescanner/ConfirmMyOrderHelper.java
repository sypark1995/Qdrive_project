package com.giosis.util.qdrive.barcodescanner;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.GeocoderUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ConfirmMyOrderHelper extends ManualHelper {
    String TAG = "ConfirmMyOrderHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final ArrayList<CaptureActivity.BarcodeListData> assignBarcodeList;

    private final OnDriverAssignEventListener eventListener;
    private final ProgressDialog progressDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private final ArrayList<CaptureActivity.BarcodeListData> assignBarcodeList;
        private OnDriverAssignEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID, ArrayList<CaptureActivity.BarcodeListData> assignBarcodeList) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.assignBarcodeList = assignBarcodeList;
        }

        public ConfirmMyOrderHelper build() {
            return new ConfirmMyOrderHelper(this);
        }

        Builder setOnDriverAssignEventListener(OnDriverAssignEventListener eventListener) {

            this.eventListener = eventListener;
            return this;
        }
    }

    private ConfirmMyOrderHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.assignBarcodeList = builder.assignBarcodeList;

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


    @SuppressLint("StaticFieldLeak")
    class DriverAssignTask extends AsyncTask<Void, Integer, DriverAssignResult> {

        int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {

                int maxCount = assignBarcodeList.size();
                progressDialog.setMax(maxCount);
                progressDialog.show();
            }
        }

        @Override
        protected DriverAssignResult doInBackground(Void... params) {

            String str = null;

            for (CaptureActivity.BarcodeListData assignData : assignBarcodeList) {

                if (!TextUtils.isEmpty(assignData.getBarcode())) {

                    if (str == null) {

                        str = assignData.getBarcode();
                    } else {

                        str = str + "," + assignData.getBarcode();
                    }
                }
            }

            DriverAssignResult result = requestDriverAssign(str);
            publishProgress(1);

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
                        insertDriverAssignInfo(qSignDeliveryList);
                    }
                }
            }

            if (eventListener != null) {
                eventListener.onPostAssignResult(resultList);
            }
        }


        private DriverAssignResult requestDriverAssign(String assignNo) {

            DriverAssignResult resultObj;

            /*try {

                GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
                HashMap<String, String> hmActionParam = new HashMap<>();
                hmActionParam.put("assignList", assignNo);
                hmActionParam.put("office_code", officeCode);
                hmActionParam.put("del_driver_id", opID);
                hmActionParam.put("device_id", deviceID);
                hmActionParam.put("stat_chg_gubun", "D");
                hmActionParam.put("app_id", DataUtil.appID);
                hmActionParam.put("nation_cd", DataUtil.nationCode);

                String methodName = "SetShippingStatDpc3out";
                Serializer serializer = new Persister();

                GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
                String resultString = response.getResultString();
                Log.e("Server", methodName + "  Result : " + resultString);
                // <ResultCode>0</ResultCode><ResultMsg>Success</ResultMsg><ResultObject><QSignDeliveryList><contr_no>55003829</contr_no><partner_ref_no>SGSG105653</partner_ref_no><invoice_no>SG19611819</invoice_no><stat>D3</stat><rcv_nm>Eunyoung Lee</rcv_nm><tel_no>+65--</tel_no><hp_no>+65-8888-8888</hp_no><zip_code>408601</zip_code><address>LIFELONG LEARNING INSTITUTE 11 EUNOS ROAD 8test bbb</address><sender_nm>eeee</sender_nm><del_memo /><driver_memo /><fail_reason>  </fail_reason><delivery_count>0</delivery_count><delivery_first_date>2019-08-20</delivery_first_date><route>GIO</route><secret_no_type> </secret_no_type><secret_no /><secure_delivery_yn>N</secure_delivery_yn><parcel_amount>25.0000</parcel_amount><currency>SGD</currency><delivery_nation_cd>SG</delivery_nation_cd></QSignDeliveryList></ResultObject>

                resultObj = serializer.read(DriverAssignResult.class, resultString);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetShippingStatDpc3out Exception : " + e.toString());
                resultObj = new DriverAssignResult();
                resultObj.setResultCode(-1);
                resultObj.setResultMsg(context.getResources().getString(R.string.text_fail_update));

                ArrayList<DriverAssignResult.QSignDeliveryList> resultList = new ArrayList<>();
                DriverAssignResult.QSignDeliveryList result = new DriverAssignResult.QSignDeliveryList();
                result.setPartnerRefNoFailAssign(assignNo);
                result.setReasonFailAssign(context.getResources().getString(R.string.text_http_request_error));
                resultList.add(result);

                resultObj.setResultObject(resultList);
            }*/

            // JSON Parser
            Gson gson = new Gson();

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
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                resultObj = gson.fromJson(jsonString, DriverAssignResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetChangeDeliveryDriver Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }


    private void insertDriverAssignInfo(DriverAssignResult.QSignDeliveryList assignInfo) {

        String opId = SharedPreferencesHelper.getSigninOpID(context);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        // eylee 2015.08.26 add non q10 - contr_no 로 sqlite 체크 후 있다면 삭제하는 로직 add start
        String contr_no = assignInfo.getContrNo();
        int cnt = getContrNoCount(contr_no);
        if (0 < cnt) {
            deleteContrNo(contr_no);
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
        contentVal.put("order_type_etc", assignInfo.getOrder_type_etc());        // krm0219

        // 2020.06 위, 경도 저장
        String[] latLng = GeocoderUtil.getLatLng(assignInfo.getLat_lng());
        contentVal.put("lat", latLng[0]);
        contentVal.put("lng", latLng[1]);

        DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
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

    private void deleteContrNo(String contr_no) {

        DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "contr_no='" + contr_no + "' COLLATE NOCASE");
    }

    public ConfirmMyOrderHelper execute() {
        DriverAssignTask driverAssignTask = new DriverAssignTask();
        driverAssignTask.execute();
        return this;
    }

    public interface OnDriverAssignEventListener {
        void onPostAssignResult(DriverAssignResult stdResult);
    }
}