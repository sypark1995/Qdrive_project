package com.giosis.util.qdrive.barcodescanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CnRPickupValidationCheckHelper2 extends ManualHelper {
    String TAG = "CnRPickupValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String scanNo;

    private final String networkType;
    private final OnCnRPickupValidationCheckListener eventListener;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String scanNo;

        private String networkType;
        private OnCnRPickupValidationCheckListener eventListener;

        public Builder(Context context, String opID, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.scanNo = scanNo;
        }

        public CnRPickupValidationCheckHelper2 build() {
            return new CnRPickupValidationCheckHelper2(this);
        }

        Builder setOnCnRPickupValidationCheckListener(OnCnRPickupValidationCheckListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }
    }

    private CnRPickupValidationCheckHelper2(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.scanNo = builder.scanNo;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
    }


    private AlertDialog getResultAlertDialog(final Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("[" + context.getResources().getString(R.string.text_scanned_failed) + "]")
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }).create();

        return dialog;
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    class CnRPickupValidationTask extends AsyncTask<Void, Void, CnRPickupResult> {

        @Override
        protected CnRPickupResult doInBackground(Void... params) {

            return validationCheck(scanNo);
        }

        @Override
        protected void onPostExecute(CnRPickupResult result) {
            super.onPostExecute(result);

            try {

                if (result.getResultCode() == 0) {

                    boolean isDBDuplicate = checkDBDuplicate(result.getResultObject().getContrNo(), result.getResultObject().getInvoiceNo());

                    String requester;
                    Log.e("krm0219", "  DB Duplicate  > " + isDBDuplicate);

                    if (isDBDuplicate) {

                        requester = getCnrRequester(result.getResultObject().getInvoiceNo());
                    } else {

                        requester = insertCnRData(result.getResultObject());
                    }
                    Log.e("krm0219", "requester  > " + requester);

                    if (eventListener != null)
                        eventListener.OnCnRPickupValidationCheckResult(result);
                } else {

                    showResultDialog(result.getResultMsg());

                    if (eventListener != null)
                        eventListener.OnCnRPickupValidationCheckFail();
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();
            }
        }


        private CnRPickupResult validationCheck(String scanNo) {

            CnRPickupResult result = new CnRPickupResult();


            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

            try {

                JSONObject job = new JSONObject();
                job.accumulate("opId", opID);
                job.accumulate("pickup_no", scanNo);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetCnROrderCheck";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultObject":{"contr_no":"55003355","partner_ref_no":"C2859SGSG","invoice_no":"C2859SGSG","stat":"P2","req_nm":"normal order","req_dt":"2019-08-2010:00-19:00","tel_no":"+65--","hp_no":"+65-8424-2354","zip_code":"048741","address":"11 PEKIN STREEThyemi3333","pickup_hopeday":"2019-08-20","pickup_hopetime":"10:00-19:00","sender_nm":"normal order","del_memo":"","driver_memo":"","fail_reason":"WA","qty":"1","cust_nm":"test191919","partner_id":"hyemi223","dr_assign_requestor":"","dr_assign_req_dt":"","dr_assign_stat":"","dr_req_no":"","failed_count":"0","route":"C2C","del_driver_id":null,"cust_no":"100054639"},"ResultCode":0,"ResultMsg":"Success"}

               /* Serializer serializer = new Persister();
                resultObj = serializer.read(PickupCNRResult.class, jsonString);*/


                JSONObject jsonObject = new JSONObject(jsonString);
                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                JSONObject resultObject = jsonObject.getJSONObject("ResultObject");
                CnRPickupResult.CnRPickupData cnRPickupData = new CnRPickupResult.CnRPickupData();

                // DATA
                cnRPickupData.setContrNo(resultObject.getString("contr_no"));
                cnRPickupData.setPartnerRefNo(resultObject.getString("partner_ref_no"));
                cnRPickupData.setInvoiceNo(resultObject.getString("partner_ref_no"));
                cnRPickupData.setStat(resultObject.getString("stat"));
                cnRPickupData.setTelNo(resultObject.getString("tel_no"));
                cnRPickupData.setHpNo(resultObject.getString("hp_no"));
                cnRPickupData.setZipCode(resultObject.getString("zip_code"));
                cnRPickupData.setAddress(resultObject.getString("address"));
                cnRPickupData.setRoute(resultObject.getString("route"));
                cnRPickupData.setPickupHopeDay(resultObject.getString("pickup_hopeday"));
                cnRPickupData.setPickupHopeTime(resultObject.getString("pickup_hopetime"));
                cnRPickupData.setQty(resultObject.getString("qty"));
                cnRPickupData.setReqName(resultObject.getString("req_nm"));
                cnRPickupData.setFailedCount(resultObject.getString("failed_count"));
                cnRPickupData.setFailReason(resultObject.getString("fail_reason"));
                cnRPickupData.setDelMemo(resultObject.getString("del_memo"));

                result.setResultObject(cnRPickupData);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetCnROrderCheck Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }


        private boolean checkDBDuplicate(String contrNo, String invoiceNo) {

            String selectQuery = "SELECT  partner_ref_no, invoice_no, stat, rcv_nm, sender_nm "
                    + " FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no= '" + invoiceNo + "'" + " and contr_no= '" + contrNo + "'";
            Cursor cs = DatabaseHelper.getInstance().get(selectQuery);
            return 0 < cs.getCount();
        }


        private String getCnrRequester(String invoiceNo) {

            String requester = "";
            String barcodeNo = invoiceNo.trim().toUpperCase();

            String selectQuery = "SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no = '" + barcodeNo + "'";
            Cursor cursor = DatabaseHelper.getInstance().get(selectQuery);

            if (0 < cursor.getCount()) {
                if (cursor.moveToFirst()) {
                    do {
                        requester = cursor.getString(cursor.getColumnIndex("req_nm"));
                    } while (cursor.moveToNext());
                }
            }

            return requester;
        }


        private String insertCnRData(CnRPickupResult.CnRPickupData data) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String regDataString = dateFormat.format(new Date());


            try {

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
                contentVal.put("type", BarcodeType.TYPE_PICKUP);
                contentVal.put("desired_date", data.getPickupHopeDay());
                contentVal.put("req_qty", data.getQty());
                contentVal.put("req_nm", data.getReqName());
                contentVal.put("failed_count", data.getFailedCount());
                contentVal.put("rcv_request", data.getDelMemo());
                contentVal.put("sender_nm", "");
                contentVal.put("punchOut_stat", "N");
                contentVal.put("reg_id", "");
                contentVal.put("reg_dt", regDataString);
                contentVal.put("fail_reason", data.getFailReason());
                contentVal.put("secret_no_type", "");
                contentVal.put("secret_no", "");

                //TODO
                contentVal.put("lat", "0");
                contentVal.put("lng", "0");

                DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
            } catch (Exception ignored) {

            }

            return data.getReqName();
        }
    }


    public CnRPickupValidationCheckHelper2 execute() {
        CnRPickupValidationTask validationTask = new CnRPickupValidationTask();
        validationTask.execute();
        return this;
    }

    public interface OnCnRPickupValidationCheckListener {

        void OnCnRPickupValidationCheckResult(CnRPickupResult result);

        void OnCnRPickupValidationCheckFail();
    }
}