package com.giosis.util.qdrive.singapore.list.pickup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.gps.GpsUpdateDialog;
import com.giosis.util.qdrive.singapore.gps.LocationModel;
import com.giosis.util.qdrive.singapore.list.BarcodeData;
import com.giosis.util.qdrive.singapore.list.SigningView;
import com.giosis.util.qdrive.singapore.server.Custom_JsonParser;
import com.giosis.util.qdrive.singapore.server.ImageUpload;
import com.giosis.util.qdrive.singapore.util.StatueType;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.util.DisplayUtil;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.OnServerEventListener;
import com.giosis.util.qdrive.singapore.util.Preferences;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * CnR Done
 */

@Deprecated // TODO_sypark ver 3.9.0 부터 사용 안함 추후 삭제
public class CnRPickupUploadHelper {
    String TAG = "CnRlPickupUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final ArrayList<BarcodeData> assignBarcodeList;
    private final SigningView signingView;
    private final SigningView collectorSigningView;

    private final long disk_size;
    private final LocationModel locationModel;

    private final String networkType;
    private final OnServerEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;
    int count = 0;
    boolean gpsUpdate = false;

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_set_transfer));
        progressDialog.setCancelable(false);
        return progressDialog;
    }


    private CnRPickupUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.assignBarcodeList = builder.assignBarcodeList;
        this.signingView = builder.signingView;
        this.collectorSigningView = builder.collectorSigningView;

        this.disk_size = builder.disk_size;
        this.locationModel = builder.locationModel;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(false).setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {

                    try {
                        if (dialog1 != null)
                            dialog1.dismiss();
                    } catch (Exception e) {
                        Log.e("Exception", TAG + "getResultAlertDialog Exception : " + e.toString());
                    }

                    Log.e("GpsUpdate", "Count : " + count);
                    if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG") && count == 1) {   // MY,ID
                        if (locationModel.getDriverLat() != 0 && locationModel.getDriverLng() != 0
                                && locationModel.getParcelLat() != 0 && locationModel.getParcelLng() != 0) {
                            // Parcel & Driver 위치정보 수집 했을 때      (0일 경우 제외)
                            //    Log.e("GpsUpdate", "DATA : " + locationModel.getDifferenceLat() + " / " + locationModel.getDifferenceLng());
                            if (locationModel.getDifferenceLat() < 0.05 && locationModel.getDifferenceLng() < 0.05) {
                                // 두 값의 차이가 0.05 이내의 범위일 경우     (0.05 이상이면 부정확)
                                // 소수점 이하 3까지만 비교       (값이 너무 작으면 빈번하게 호출됨)
                                gpsUpdate = 0.001 <= locationModel.getDifferenceLat() || 0.001 <= locationModel.getDifferenceLng();
                            }
                        }
                    }

                    if (gpsUpdate) {

                        GpsUpdateDialog gpsDialog = new GpsUpdateDialog(context, locationModel, eventListener);
                        gpsDialog.show();
                        gpsDialog.setCanceledOnTouchOutside(false);
                        Window window = gpsDialog.getWindow();
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    } else {

                        if (eventListener != null) {
                            eventListener.onPostResult();
                        }
                    }
                })
                .create();

        return dialog;
    }

    private void showResultDialog(String message, int count) {

        this.count = count;
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final ArrayList<BarcodeData> assignBarcodeList;
        private final SigningView signingView;
        private final SigningView collectorSigningView;

        private final long disk_size;
        private final LocationModel locationModel;

        private OnServerEventListener eventListener;
        private final String networkType;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<BarcodeData> assignBarcodeList, SigningView signingView, SigningView collectorSigningView,
                       long disk_size, LocationModel locationModel) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.assignBarcodeList = assignBarcodeList;
            this.signingView = signingView;
            this.collectorSigningView = collectorSigningView;

            this.disk_size = disk_size;
            this.locationModel = locationModel;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public CnRPickupUploadHelper build() {
            return new CnRPickupUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    class CnRPickupUploadTask extends AsyncTask<Void, Integer, ArrayList<StdResult>> {
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
        protected ArrayList<StdResult> doInBackground(Void... params) {

            ArrayList<StdResult> resultList = new ArrayList<>();

            if (assignBarcodeList != null && assignBarcodeList.size() > 0) {
                StdResult stdResult = new StdResult();
                stdResult.setResultCode(-999);
                stdResult.setResultMsg("Error.");

                for (BarcodeData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        stdResult = requestPickupUpload(assignData.getBarcode());
                    }
                    resultList.add(stdResult);
                    publishProgress(1);
                }
            }

            return resultList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progress += values[0];
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(ArrayList<StdResult> resultList) {
            super.onPostExecute(resultList);

            DisplayUtil.dismissProgressDialog(progressDialog);

            int successCount = 0;
            int failCount = 0;
            int resultCode = -999;
            String resultMsg;
            String fail_reason = "";

            try {

                for (int i = 0; i < resultList.size(); i++) {

                    StdResult result = resultList.get(i);
                    resultCode = result.getResultCode();
                    resultMsg = result.getResultMsg();
                    Log.e("krm0219", "DeviceData Result : " + resultCode + " / " + resultMsg);

                    if (resultCode < 0) {

                        if (resultCode == -14) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_14);
                        } else if (resultCode == -15) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_15);
                        } else if (resultCode == -16) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_16);
                        } else {
                            //   fail_reason += String.format(context.getResources().getString(R.string.msg_upload_fail_etc), resultCode);
                            fail_reason += resultMsg;
                        }

                        failCount++;
                    } else {
                        successCount++;
                    }
                }

                if (0 < successCount && failCount == 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showResultDialog(msg, successCount);
                } else {

                    if (resultCode == -16) {
                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved), 0);
                    } else {
                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), successCount, failCount, fail_reason);
                        showResultDialog(msg, 0);
                    }
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestPickupUpload(String assignNo) {

            DataUtil.capture("/QdrivePickup", assignNo, signingView);
            DataUtil.capture("/QdriveCollector", assignNo, collectorSigningView);


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            ContentValues contentVal = new ContentValues();
            contentVal.put("stat", StatueType.PICKUP_DONE);
            contentVal.put("real_qty", "1");
            contentVal.put("chg_dt", dateFormat.format(date));
            contentVal.put("fail_reason", "");
            contentVal.put("retry_dt", "");
            contentVal.put("driver_memo", "");
            contentVal.put("reg_id", opID);


            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});


            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

//             // TEST_Upload Failed 시 주석 풀고 실행
//            if (true) {
//
//                result.setResultCode(-15);
//                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_15));
//                return result;
//            }


            try {

                String bitmapString = "";
                String bitmapString2 = "";

                signingView.buildDrawingCache();
                collectorSigningView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                Bitmap captureView2 = collectorSigningView.getDrawingCache();
                bitmapString = DataUtil.bitmapToString(context, captureView, ImageUpload.QXPOP, "qdriver/sign", assignNo);
                bitmapString2 = DataUtil.bitmapToString(context, captureView2, ImageUpload.QXPOP, "qdriver/sign", assignNo);

                if (bitmapString.equals("") || bitmapString2.equals("")) {
                    result.setResultCode(-100);
                    result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", "RC");
                job.accumulate("stat", StatueType.PICKUP_DONE);
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)"); // 내부관리자용 메세지
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("fileData2", bitmapString2);
                job.accumulate("remark", "");                   // 드라이버 메세지 driver_memo	== remark
                job.accumulate("disk_size", disk_size);
                job.accumulate("lat", locationModel.getDriverLat());
                job.accumulate("lon", locationModel.getDriverLng());
                job.accumulate("real_qty", "1");
                job.accumulate("fail_reason", "");
                job.accumulate("retry_day", "");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());
                Log.e("Server", "CNRDone SetPickupUploadData  DATA : " + assignNo);


                String methodName = "SetPickupUploadData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"SUCCESS"}
                // {"ResultCode":-11,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int resultCode = jsonObject.getInt("ResultCode");

                result.setResultCode(resultCode);
                result.setResultMsg(jsonObject.getString("ResultMsg"));

                if (resultCode == 0) {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", "S");

                    dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{assignNo, opID});
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetPickupUploadData Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg("");
            }

            return result;
        }
    }


    public CnRPickupUploadHelper execute() {
        CnRPickupUploadTask serverUploadTask = new CnRPickupUploadTask();
        serverUploadTask.execute();
        return this;
    }
}