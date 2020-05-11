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
import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.list.PickupAssignResult;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;


public class ServerDownloadHelper extends ManualHelper {
    String TAG = "ServerDownloadHelper";

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
        String resultMsg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog != null) {
                progressDialog.show();
            }
        }

        @Override
        protected Long doInBackground(Void... params) {

            try {
                DriverAssignResult DeliveryServerList = getDeliveryServerData();
                DriverAssignResult OutletDeliveryServerList = getOutletDeliveryServerData();
                PickupAssignResult PickupServerList = getPickupServerData();

                long maxCount = 0;
                long resultCode = 0;
                long successCount = 0;

                if (DeliveryServerList != null) {

                    resultCode += DeliveryServerList.getResultCode();
                    if (resultCode < 0)
                        resultMsg += DeliveryServerList.getResultMsg();
                }
                if (DeliveryServerList != null && DeliveryServerList.getResultObject() != null) {
                    maxCount += DeliveryServerList.getResultObject().size();
                }

                if (OutletDeliveryServerList != null) {

                    resultCode += OutletDeliveryServerList.getResultCode();
                    if (resultCode < 0)
                        resultMsg += OutletDeliveryServerList.getResultMsg();
                }
                if (OutletDeliveryServerList != null && OutletDeliveryServerList.getResultObject() != null) {
                    maxCount += OutletDeliveryServerList.getResultObject().size();
                }

                if (PickupServerList != null) {

                    resultCode += PickupServerList.getResultCode();
                    if (resultCode < 0)
                        resultMsg += PickupServerList.getResultMsg();
                }
                if (PickupServerList != null && PickupServerList.getResultObject() != null) {
                    maxCount += PickupServerList.getResultObject().size();
                }

                progressDialog.setMax((int) maxCount);

                // FIXME.  서버에서 (-) Result 값이 내려왔을 때의 처리방법 바꾸기
                    /*
                Log.e("Server", "Download Result Code : " + resultCode + " / " + resultMsg);


                if (resultCode < 0) {

                    return resultCode;
                }*/

                if (maxCount < 1) {

                    return maxCount;
                }

                if (DeliveryServerList != null) {

                    for (DriverAssignResult.QSignDeliveryList shippingInfo : DeliveryServerList.getResultObject()) {

                        successCount = insertDeviceDeliveryData(shippingInfo);
                        publishProgress(1);
                    }
                }

                if (OutletDeliveryServerList != null && OutletDeliveryServerList.getResultObject() != null) {
                    for (DriverAssignResult.QSignDeliveryList outlet_shippingInfo : OutletDeliveryServerList.getResultObject()) {
                        successCount = insertDeviceOutletDeliveryData(outlet_shippingInfo);
                        publishProgress(1);
                    }

              /*  // TODO
                testOutletDeliveryData("1", "SG19611661", "7E 014 The Clementi Mall");
                testOutletDeliveryData("2", "SG19611662", "7E 775 UE Square");*/
                }

                if (PickupServerList != null) {
                    for (PickupAssignResult.QSignPickupList pickupInfo : PickupServerList.getResultObject()) {
                        successCount = insertDevicePickupData(pickupInfo);
                        publishProgress(1);
                    }
                }

                return successCount;
            } catch (Exception e) {

                Log.e("Exception", "ServerDownloadHelper Exception : " + e.toString());


                long successCount = 0;
                return successCount;
            }
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

            DisplayUtil.dismissProgressDialog(progressDialog);

            if (result < 0) {

                showResultDialog(resultMsg);
            } else if (result == 0) {

                showResultDialog(context.getResources().getString(R.string.msg_no_data_to_download));
            } else {

                if (eventListener != null) {

                    eventListener.onDownloadResult();
                }
            }
        }
    }


    private DriverAssignResult getDeliveryServerData() {

        DriverAssignResult resultObj;

        try {

            // TEST.
        /*    String MOBILE_SERVER_URL = "https://qxapi.qxpress.asia/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi";
            String opID = "hdsg_sallehudin";*/

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", opID);
            hmActionParam.put("officeCd", officeCode);
            hmActionParam.put("exceptList", "");
            hmActionParam.put("assignList", "");
            hmActionParam.put("device_id", deviceID);
            hmActionParam.put("network_type", networkType);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetDeliveryList";
            Serializer serializer = new Persister();


            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // <ResultCode>0</ResultCode><ResultMsg>SUCCESS</ResultMsg><ResultObject><QSignDeliveryList><contr_no>55003828</contr_no><partner_ref_no>SGSG105652</partner_ref_no><invoice_no>SG19611818</invoice_no><stat>D3</stat><rcv_nm>Eunyoung Lee</rcv_nm><tel_no>+65--</tel_no><hp_no>+65-8888-8888</hp_no><zip_code>408601</zip_code><address>LIFELONG LEARNING INSTITUTE 11 EUNOS ROAD 8 test bbb</address><sender_nm>eeee</sender_nm><del_memo /><driver_memo /><fail_reason>  </fail_reason><delivery_count>0</delivery_count><delivery_first_date>2019-08-20</delivery_first_date><route>GIO</route><secret_no_type> </secret_no_type><secret_no /><del_hopeday /><secure_delivery_yn>N</secure_delivery_yn><parcel_amount>25.00</parcel_amount><currency>SGD</currency><order_type_etc>ETC</order_type_etc></QSignDeliveryList><QSignDeliveryList><contr_no>55003829</contr_no><partner_ref_no>SGSG105653</partner_ref_no><invoice_no>SG19611819</invoice_no><stat>D3</stat><rcv_nm>Eunyoung Lee</rcv_nm><tel_no>+65--</tel_no><hp_no>+65-8888-8888</hp_no><zip_code>408601</zip_code><address>LIFELONG LEARNING INSTITUTE 11 EUNOS ROAD 8 test bbb</address><sender_nm>eeee</sender_nm><del_memo /><driver_memo /><fail_reason>  </fail_reason><delivery_count>0</delivery_count><delivery_first_date>2019-08-20</delivery_first_date><route>GIO</route><secret_no_type> </secret_no_type><secret_no /><del_hopeday /><secure_delivery_yn>N</secure_delivery_yn><parcel_amount>25.00</parcel_amount><currency>SGD</currency><order_type_etc>ETC</order_type_etc></QSignDeliveryList></ResultObject>

            resultObj = serializer.read(DriverAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }


    private DriverAssignResult getOutletDeliveryServerData() {

        DriverAssignResult resultObj;

        try {

          /*  // TODO. Outlet TEST
            String MOBILE_SERVER_URL = "https://qxapi.qxpress.asia/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi";
            "Ramlan_7E"*/

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", opID);
            hmActionParam.put("officeCd", officeCode);
            hmActionParam.put("exceptList", "");
            hmActionParam.put("assignList", "");
            hmActionParam.put("device_id", deviceID);
            hmActionParam.put("network_type", networkType);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            Log.e("krm0219", "DATA : " + opID + " / " + officeCode + " / " + deviceID);

            String methodName = "GetDeliveryList_Outlet";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // <ResultCode>0</ResultCode><ResultMsg>SUCCESS</ResultMsg><ResultObject />

            resultObj = serializer.read(DriverAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList_Outlet Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }

    private PickupAssignResult getPickupServerData() {

        PickupAssignResult resultObj;

        try {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", opID);            // 필수
            hmActionParam.put("officeCd", officeCode);
            hmActionParam.put("exceptList", "");
            hmActionParam.put("assignList", "");
            hmActionParam.put("device_id", deviceID);
            hmActionParam.put("network_type", networkType);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

     /*       // TODO.  TEST
            String MOBILE_SERVER_URL = "https://qxapi.qxpress.asia/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi";
            hmActionParam.put("opId", "Taufik.FSA");*/

            String methodName = "GetPickupList";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // <ResultCode>0</ResultCode><ResultMsg>SUCCESS</ResultMsg><ResultObject><QSignPickupList><contr_no>55003355</contr_no><partner_ref_no>C2859SGSG</partner_ref_no><invoice_no>C2859SGSG</invoice_no><stat>P2</stat><req_nm>normal order</req_nm><req_dt>2019-08-2010:00-19:00</req_dt><tel_no>+65--</tel_no><hp_no>+65-8424-2354</hp_no><zip_code>048741</zip_code><address>11 PEKIN STREEThyemi3333</address><pickup_hopeday>2019-08-20</pickup_hopeday><pickup_hopetime>10:00-19:00</pickup_hopetime><sender_nm>normal order</sender_nm><del_memo /><driver_memo /><fail_reason>WA</fail_reason><qty>1</qty><cust_nm>test191919</cust_nm><partner_id>hyemi223</partner_id><dr_assign_requestor /><dr_assign_req_dt /><dr_assign_stat /><dr_req_no /><failed_count>0</failed_count><route>C2C</route><cust_no>100054639</cust_no></QSignPickupList></ResultObject>

            resultObj = serializer.read(PickupAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetPickupList Exception : " + e.toString());
            resultObj = null;
        }

        return resultObj;
    }


    // DB 저장
    private long insertDeviceDeliveryData(DriverAssignResult.QSignDeliveryList data) {

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
        contentVal.put("type", BarcodeType.TYPE_DELIVERY);
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

        return dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
    }

    // Outlet Delivery DB 저장
    private long insertDeviceOutletDeliveryData(DriverAssignResult.QSignDeliveryList data) {

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
        contentVal.put("type", BarcodeType.TYPE_DELIVERY);
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

        return dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
    }

    //픽업 데이타  DB 저장
    private long insertDevicePickupData(PickupAssignResult.QSignPickupList data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", data.getContrNo());

        // NOTIFICATION.  19/10 - Ref.Pickup No가 존재하면 리스트에서 해당 번호로 표시
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
        contentVal.put("cust_no", data.getCustNo()); //QLPS cust_no
        contentVal.put("partner_id", data.getPartnerID()); //QLPS partner_cust_id

        if (data.getRoute().equals("RPC")) {
            contentVal.put("desired_time", data.getPickupHopeTime());
        }

        return dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
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


    // TODO.  Outlet TEST
    private long testOutletDeliveryData(String contrNo, String trackingNo, String route) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", contrNo);
        contentVal.put("partner_ref_no", trackingNo);
        contentVal.put("invoice_no", trackingNo);
        contentVal.put("stat", "D3");
        contentVal.put("rcv_nm", "karam");
        contentVal.put("sender_nm", "KARAM");
        contentVal.put("tel_no", "01012345678");
        contentVal.put("hp_no", "01012345678");
        contentVal.put("zip_code", "129588");
        contentVal.put("address", "THE CLEMENTI MALL 3155 COMMONWEALTH AVENUE WEST #01-03 (Operation hours: 24 hours)");
        contentVal.put("rcv_request", "");
        contentVal.put("delivery_dt", "2019-08-17 오후 2:49:29");
        contentVal.put("delivery_cnt", "0");
        contentVal.put("type", BarcodeType.TYPE_DELIVERY);
        contentVal.put("route", route);
        contentVal.put("reg_id", opID);
        contentVal.put("reg_dt", regDataString);
        contentVal.put("punchOut_stat", "N");
        contentVal.put("driver_memo", "");
        contentVal.put("fail_reason", "");
        contentVal.put("secret_no_type", "");
        contentVal.put("secret_no", "");
        contentVal.put("secure_delivery_yn", "N");
        contentVal.put("parcel_amount", "32.6");
        contentVal.put("currency", "SGD");
        contentVal.put("order_type_etc", "DPC");

        return dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
    }
}