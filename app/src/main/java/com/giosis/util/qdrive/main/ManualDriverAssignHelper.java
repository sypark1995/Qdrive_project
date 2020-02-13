package com.giosis.util.qdrive.main;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.barcodescanner.DriverAssignResult;
import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

public class ManualDriverAssignHelper extends ManualHelper {
    String TAG = "ManualDriverAssignHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final ArrayList<CaptureActivity.BarcodeListData> assignBarcodeList;

    private final String networkType;
    private final OnDriverAssignEventListener eventListener;
    private final ProgressDialog progressDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private final ArrayList<CaptureActivity.BarcodeListData> assignBarcodeList;

        private String networkType;
        private OnDriverAssignEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID, ArrayList<CaptureActivity.BarcodeListData> assignBarcodeList) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.assignBarcodeList = assignBarcodeList;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public ManualDriverAssignHelper build() {
            return new ManualDriverAssignHelper(this);
        }

        public Builder setOnDriverAssignEventListener(OnDriverAssignEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualDriverAssignHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.assignBarcodeList = builder.assignBarcodeList;

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
                int maxCount = assignBarcodeList.size();
                progressDialog.setMax(maxCount);
                progressDialog.show();
            }
        }

        @Override
        protected DriverAssignResult doInBackground(Void... params) {

            DriverAssignResult result = null;

            if (assignBarcodeList != null && assignBarcodeList.size() > 0) {

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

                if (progressDialog != null && progressDialog.isShowing()) {

                    progressDialog.dismiss();
                }
            } catch (Exception e) {

                // !((Activity)context).isFinishing()
            }

            int resultCode = resultList.getResultCode();

            if (resultCode == 0) {

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

            try {

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
            }

            return resultObj;
        }
    }


    private void insertDriverAssignInfo(DriverAssignResult.QSignDeliveryList assignInfo) {

        String opId = SharedPreferencesHelper.getSigninOpID(context);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

        // eylee 2015.08.26 add nonqoo10 - contr_no 로 sqlite 체크 후 있다면 삭제하는 로직 add start
        String contry_no = assignInfo.getContrNo();
        int cnt = getContrNoCount(contry_no);
        if (0 < cnt) {
            deleteContrNo(contry_no);
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
        contentVal2.put("type", BarcodeType.TYPE_DELIVERY);
        contentVal2.put("route", assignInfo.getRoute());
        contentVal2.put("reg_id", opId);
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

    private void deleteContrNo(String contr_no) {

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "contr_no='" + contr_no + "' COLLATE NOCASE");
    }

    public ManualDriverAssignHelper execute() {
        DriverAssignTask driverAssignTask = new DriverAssignTask();
        driverAssignTask.execute();
        return this;
    }

    public interface OnDriverAssignEventListener {
        void onPostAssignResult(DriverAssignResult stdResult);
    }
}