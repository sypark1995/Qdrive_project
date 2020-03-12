package com.giosis.util.qdrive.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.DriverAssignResult;
import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.list.ChildItem;
import com.giosis.util.qdrive.list.PickupAssignResult;
import com.giosis.util.qdrive.list.RowItem;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

// Outlet Order Status
public class OutletStatusDownloadHelper extends ManualHelper {
    String TAG = "OutletStatusDownloadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final int outletStatusPosition;

    private final String networkType;
    private final OnOutletStatusDownloadListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;


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

        public OutletStatusDownloadHelper build() {
            return new OutletStatusDownloadHelper(this);
        }

        public Builder setOnOutletStatusDownloadListener(OnOutletStatusDownloadListener eventListener) {

            this.eventListener = eventListener;
            return this;
        }
    }

    private OutletStatusDownloadHelper(Builder builder) {
        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.outletStatusPosition = builder.outletStatusPosition;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
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

            DisplayUtil.dismissProgressDialog(progressDialog);

            if (eventListener != null) {
                eventListener.onDownloadResult(outletDataArrayList);
            }
        }
    }


    private DriverAssignResult getOutletRealTimeData(String type) {

        DriverAssignResult resultObj = null;

        try {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("type", type);
            hmActionParam.put("opId", opID);
            hmActionParam.put("officeCd", officeCode);
            hmActionParam.put("exceptList", "");
            hmActionParam.put("assignList", "");
            hmActionParam.put("device_id", deviceID);
            hmActionParam.put("network_type", networkType);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            Log.e("krm0219", "DATA : " + opID + " / " + officeCode + " / " + deviceID);

            String methodName = "GetDeliveryList_OutletRealTime";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);

            resultObj = serializer.read(DriverAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList_OutletRealTime(" + type + ")  Exception : " + e.toString());
        }

        return resultObj;
    }


    private DriverAssignResult getOutletDeliveryServerData() {

        DriverAssignResult resultObj = null;

        try {

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

            resultObj = serializer.read(DriverAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetDeliveryList_Outlet Exception : " + e.toString());
        }
        return resultObj;
    }

    private PickupAssignResult getPickupServerData() {

        PickupAssignResult resultObj = null;

        try {

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

            String methodName = "GetPickupList";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);

            resultObj = serializer.read(PickupAssignResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetPickupList Exception : " + e.toString());
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

                    Log.e("Exception", TAG + "  Exception : " + e.toString());
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
            long delay = 0;

            RowItem rowItem = new RowItem(data.getContrNo(), "D+" + delay, data.getInvoiceNo(), data.getReqName(),
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

        dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
    }


    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private AlertDialog getResultAlertDialog(Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("[Download] Result")
                .setCancelable(true).setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                        if (eventListener != null) {
                            eventListener.onDownloadResult(null);
                        }
                    }
                })
                .create();
        return dialog;
    }


    //
    public interface OnOutletStatusDownloadListener {
        void onDownloadResult(ArrayList<RowItem> resultList);
    }

    public OutletStatusDownloadHelper execute() {
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute();
        return this;
    }
}