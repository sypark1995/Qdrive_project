package com.giosis.util.qdrive.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.list.PickupAssignResult;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;


public class PickupAssignCheckHelper extends ManualHelper {
    String TAG = "PickupAssignCheckHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final String networkType;
    private final OnPickupAssignCheckListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private String networkType;
        private OnPickupAssignCheckListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public PickupAssignCheckHelper build() {
            return new PickupAssignCheckHelper(this);
        }

        Builder setOnPickupAssignCheckListener(OnPickupAssignCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private PickupAssignCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }


    class PickupCheckTask extends AsyncTask<Void, Integer, Integer> {
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

            int assignCount = getManualAssignCount();

            if (0 < assignCount) {

                if (progressDialog != null) {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMessage(context.getResources().getString(R.string.text_downloading) + "...");
                    progressDialog.setMax(assignCount);
                    progressDialog.show();
                }


                PickupAssignResult pickupAssignResult = getManualAssignedServerData();
                String req_no_list = "";

                for (PickupAssignResult.QSignPickupList pickupInfo : pickupAssignResult.getResultObject()) {

                    long result = insertDevicePickupData(pickupInfo);

                    if (0 < result) {

                        req_no_list += (pickupInfo.getDrReqNo() + ",");
                    }

                    publishProgress(1);
                }

                // 다운로드 완료 서버 전송
                if (0 < req_no_list.length()) {

                    int req = setManualAssignReceived(req_no_list);

                    if (req < 0) {
                        return req;
                    }
                }
            }

            return assignCount;
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

                    progressDialog.dismiss();
                }


                if (0 < result) {    // RPC 다운로드 성공

                    if (eventListener != null) {
                        eventListener.onDownloadResult(0);
                    }
                } else if (result == -1) {    //Deactivated 사용자

                    if (eventListener != null) {
                        eventListener.onDownloadFailList(-1);
                    }
                } else if (result == -16) {

                    showDisconnectedDialog();
                } else if (result < 0) {

                    showResultDialog(context.getResources().getString(R.string.msg_manual_assign_failed) + " : " + result);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }
    }


    private int getManualAssignCount() {

        if (!NetworkUtil.isNetworkAvailable(context)) {
            return -16;
        }

        int result = -15;

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opid", opID);
            job.accumulate("datatype", "MA");
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetManualAssignCount";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultCode":0,"ResultMsg":"OK"}

            JSONObject jsonObject = new JSONObject(jsonString);
            result = jsonObject.getInt("ResultCode");
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetManualAssignCount Exception : " + e.toString());
        }

        return result;
    }


    private PickupAssignResult getManualAssignedServerData() {

        PickupAssignResult resultObj = null;

        try {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", opID);
            hmActionParam.put("officeCd", officeCode);
            hmActionParam.put("dataType", "MA");
            hmActionParam.put("device_id", deviceID);
            hmActionParam.put("network_type", networkType);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetChangedPickupList";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // <ResultCode>0</ResultCode><ResultMsg>SUCCESS</ResultMsg><ResultObject/>

            resultObj = serializer.read(PickupAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetChangedPickupList Exception : " + e.toString());
        }

        return resultObj;
    }


    private int setManualAssignReceived(String req_no_list) {

        if (!NetworkUtil.isNetworkAvailable(context)) {
            return -16;
        }

        int result = -15;

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opid", opID);
            job.accumulate("chg_stat", "S");
            job.accumulate("req_no_list", req_no_list);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "SetManualAssignReceived";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultCode":1,"ResultMsg":"SUCCESS"}  123, 123
            // {"ResultCode":-10,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            result = jsonObject.getInt("ResultCode");
        } catch (Exception e) {

            Log.e("Exception", TAG + "  SetManualAssignReceived Exception : " + e.toString());
        }

        return result;
    }


    private long insertDevicePickupData(PickupAssignResult.QSignPickupList data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

        try {

            ContentValues contentVal = new ContentValues();
            contentVal.put("contr_no", data.getContrNo());
            contentVal.put("partner_ref_no", data.getPartnerRefNo());
            contentVal.put("invoice_no", data.getPartnerRefNo());  //invoice_no = partnerRefNo 사용
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
            contentVal.put("reg_id", opID);
            contentVal.put("reg_dt", regDataString);
            contentVal.put("fail_reason", data.getFailReason());
            contentVal.put("secret_no_type", data.getSecretNoType());
            contentVal.put("secret_no", data.getSecretNo());

            dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  insertDevicePickupData Exception : " + e.toString());
            return 0;
        }

        return 1;
    }


    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        String dialogTitle = context.getResources().getString(R.string.text_download_result);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(dialogTitle)
                .setCancelable(true).setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                        if (eventListener != null) {
                            eventListener.onDownloadResult(0);
                        }
                    }
                })
                .create();
        return dialog;
    }

    private void showResultDialog(String message) {
        resultDialog.setTitle(context.getResources().getString(R.string.text_download_result));
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    private void showDisconnectedDialog() {
        resultDialog.setTitle("");
        resultDialog.setMessage(context.getResources().getString(R.string.msg_network_connect_error));
        resultDialog.show();
    }

    public PickupAssignCheckHelper execute() {

        PickupCheckTask pickupCheckTask = new PickupCheckTask();
        pickupCheckTask.execute();
        return this;
    }

    public interface OnPickupAssignCheckListener {
        void onDownloadResult(Integer resultList);

        void onDownloadFailList(Integer resultList);
    }
}