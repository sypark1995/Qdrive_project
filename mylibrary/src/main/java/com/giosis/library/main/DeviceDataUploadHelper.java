package com.giosis.library.main;

/*
 디바이스에 저장 된 상태변경  데이터 업로드
 홈에서 일괄 업로드 (Delivery & Pickup)
 업로드 대상건을 홈에서 넘겨 줌
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.UploadData;
import com.giosis.library.barcodescanner.StdResult;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.server.ImageUpload;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.DataUtil;
import com.giosis.library.database.DatabaseHelper;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.OnServerEventListener;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class DeviceDataUploadHelper {
    String TAG = "DeviceDataUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final ArrayList<UploadData> assignBarcodeList;
    private final String upload_channel;
    private final Double latitude;
    private final Double longitude;

    private final String networkType;
    private final OnServerEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog resultDialog;

    private AlertDialog getResultAlertDialog(final Context context) {

        return new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog1, which) -> {
                    if (dialog1 != null)
                        dialog1.dismiss();

                    if (eventListener != null) {
                        eventListener.onPostResult();
                    }
                })
                .create();
    }

    private DeviceDataUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.assignBarcodeList = builder.assignBarcodeList;
        this.upload_channel = builder.upload_channel;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_set_transfer));
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final ArrayList<UploadData> assignBarcodeList;
        private final String upload_channel;
        private final Double latitude;
        private final Double longitude;

        private final String networkType;
        private OnServerEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<UploadData> assignBarcodeList, String upload_channel, Double latitude, Double longitude) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;

            this.assignBarcodeList = assignBarcodeList;
            this.upload_channel = upload_channel;
            this.latitude = latitude;
            this.longitude = longitude;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public DeviceDataUploadHelper build() {
            return new DeviceDataUploadHelper(this);
        }

        public Builder setOnServerEventListener(OnServerEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    class ServerUploadTask extends AsyncTask<Void, Integer, ArrayList<StdResult>> {
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

            if (assignBarcodeList != null && 0 < assignBarcodeList.size()) {

                StdResult result = new StdResult();
                result.setResultCode(-999);
                result.setResultMsg(context.getResources().getString(R.string.text_error));

                for (UploadData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getNoSongjang())) {
                        result = requestServerUpload(assignData);
                    }
                    resultList.add(result);
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
            StringBuilder fail_reason = new StringBuilder();

            try {

                for (int i = 0; i < resultList.size(); i++) {

                    StdResult result = resultList.get(i);
                    resultCode = result.getResultCode();
                    resultMsg = result.getResultMsg();
                    Log.e(TAG, "DeviceData Result : " + resultCode + " / " + resultMsg);

                    if (resultCode < 0) {

                        if (resultCode == -14) {
                            fail_reason.append(context.getResources().getString(R.string.msg_upload_fail_14));
                        } else if (resultCode == -15) {
                            fail_reason.append(context.getResources().getString(R.string.msg_upload_fail_15));
                        } else if (resultCode == -16) {
                            fail_reason.append(context.getResources().getString(R.string.msg_upload_fail_16));
                        } else {
                            //   fail_reason += String.format(context.getResources().getString(R.string.msg_upload_fail_etc), resultCode);
                            fail_reason.append(resultMsg);
                        }

                        failCount++;
                    } else {
                        successCount++;
                    }
                }

                if (0 < successCount && failCount == 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showResultDialog(msg);
                } else {

                    if (resultCode == -16) {

                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error));
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), successCount, failCount, fail_reason.toString());
                        showResultDialog(msg);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private StdResult requestServerUpload(UploadData uploadData) {

            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg("");
                return result;
            }


            try {

                DatabaseHelper dbHelper = DatabaseHelper.getInstance();
                String methodName = "";

                JSONObject job = new JSONObject();

                if (uploadData.getType().equals(BarcodeType.TYPE_DELIVERY)) {

                    String bitmapString = "";
                    String bitmapString1 = "";

                    // 2020.11  - sign, picture 업로드
                    if (uploadData.getStat().equals(BarcodeType.DELIVERY_DONE)) {

                        String dirPath = Environment.getExternalStorageDirectory().toString() + "/Qdrive";
                        String filePath = dirPath + "/" + uploadData.getNoSongjang() + ".png";
                        File imgFile = new File(filePath);
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            bitmapString = DataUtil.bitmapToString(context, myBitmap, ImageUpload.QXPOD, "qdriver/sign", uploadData.getNoSongjang());
                        }

                        dirPath = Environment.getExternalStorageDirectory().toString() + "/Qdrive";
                        filePath = dirPath + "/" + uploadData.getNoSongjang() + "_1.png";
                        imgFile = new File(filePath);
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            bitmapString1 = DataUtil.bitmapToString(context, myBitmap, ImageUpload.QXPOD, "qdriver/delivery", uploadData.getNoSongjang());
                        }
                        Log.e(TAG, " RE-Upload DATA 1 : " + bitmapString);
                        Log.e(TAG, " RE-Upload DATA 2 : " + bitmapString1);

                        // 사인, 이미지가 둘 다 없으면 업로드 불가능
                        if (bitmapString.equals("") && bitmapString1.equals("")) {
                            result.setResultCode(-14);
                            result.setResultMsg("");
                            return result;
                        }
                    } else if (uploadData.getStat().equals(BarcodeType.DELIVERY_FAIL)) {

                        String dirPath = Environment.getExternalStorageDirectory().toString() + "/QdriveFailed";
                        String filePath = dirPath + "/" + uploadData.getNoSongjang() + ".png";
                        File imgFile = new File(filePath);

                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            bitmapString = DataUtil.bitmapToString(context, myBitmap, ImageUpload.QXPOD, "qdriver/sign", uploadData.getNoSongjang());

                            if (bitmapString.equals("")) {
                                result.setResultCode(-100);
                                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                                return result;
                            }
                        }
                    }
                    Log.e("Server", TAG + "  DATA : " + uploadData.getStat() + " / " + bitmapString);


                    job.accumulate("rcv_type", uploadData.getReceiveType());
                    job.accumulate("stat", uploadData.getStat());
                    job.accumulate("chg_id", opID);
                    job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)");
                    job.accumulate("opId", opID);
                    job.accumulate("officeCd", officeCode);
                    job.accumulate("device_id", deviceID);
                    job.accumulate("network_type", networkType);
                    job.accumulate("no_songjang", uploadData.getNoSongjang());
                    job.accumulate("fileData", bitmapString);
                    job.accumulate("delivery_photo_url", bitmapString1);
                    job.accumulate("remark", uploadData.getDriverMemo());  // 드라이버 메세지 driver_memo	== remark
                    job.accumulate("disk_size", "999999");
                    job.accumulate("lat", latitude);
                    job.accumulate("lon", longitude);
                    job.accumulate("stat_reason", uploadData.getFailReason());
                    job.accumulate("del_channel", upload_channel);          // QH:Qdrive Home, QL: Qdrive List
                    job.accumulate("app_id", DataUtil.appID);
                    job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                    methodName = "SetDeliveryUploadData";
                } else if (uploadData.getType().equals("P")) {

                    String bitmapString = "";
                    String bitmapString2 = "";

                    if (!uploadData.getStat().equals(BarcodeType.PICKUP_FAIL)) {

                        String dirPath = Environment.getExternalStorageDirectory().toString() + "/QdrivePickup";
                        String dirPath2 = Environment.getExternalStorageDirectory().toString() + "/QdriveCollector";
                        String filePath = dirPath + "/" + uploadData.getNoSongjang() + ".png";
                        String filePath2 = dirPath2 + "/" + uploadData.getNoSongjang() + ".png";
                        File imgFile = new File(filePath);
                        File imgFile2 = new File(filePath2);

                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            bitmapString = DataUtil.bitmapToString(context, myBitmap, ImageUpload.QXPOD, "qdriver/sign", uploadData.getNoSongjang());

                            if (bitmapString.equals("")) {
                                result.setResultCode(-100);
                                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                                return result;
                            }
                        } else {
                            result.setResultCode(-14);
                            result.setResultMsg("");
                            return result;
                        }

                        if (imgFile2.exists()) {
                            Bitmap myBitmap2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                            bitmapString2 = DataUtil.bitmapToString(context, myBitmap2, ImageUpload.QXPOD, "qdriver/delivery", uploadData.getNoSongjang());

                            if (bitmapString2.equals("")) {
                                result.setResultCode(-100);
                                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                                return result;
                            }
                        } else {
                            result.setResultCode(-14);
                            result.setResultMsg("");
                            return result;
                        }
                    } else if (uploadData.getStat().equals(BarcodeType.PICKUP_FAIL)) {

                        String dirPath = Environment.getExternalStorageDirectory().toString() + "/QdrivePickup";
                        String filePath = dirPath + "/" + uploadData.getNoSongjang() + ".png";
                        File imgFile = new File(filePath);

                        if (imgFile.exists()) {

                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            bitmapString = DataUtil.bitmapToString(context, myBitmap, ImageUpload.QXPOD, "qdriver/sign", uploadData.getNoSongjang());

                            if (bitmapString.equals("")) {
                                result.setResultCode(-100);
                                result.setResultMsg(context.getResources().getString(R.string.msg_upload_fail_image));
                                return result;
                            }
                        }
                    }


                    job.accumulate("rcv_type", uploadData.getReceiveType());
                    job.accumulate("stat", uploadData.getStat());           // P3:Pickup Done / PF:Pickup Failed
                    job.accumulate("chg_id", opID);
                    job.accumulate("deliv_msg", "(by Qdrive RealTime-Upload)"); // 내부관리자용 메세지
                    job.accumulate("opId", opID);
                    job.accumulate("officeCd", officeCode);
                    job.accumulate("device_id", deviceID);
                    job.accumulate("network_type", networkType);
                    job.accumulate("no_songjang", uploadData.getNoSongjang());
                    job.accumulate("fileData", bitmapString);
                    job.accumulate("fileData2", bitmapString2);
                    job.accumulate("remark", uploadData.getDriverMemo());  // 드라이버 메세지 driver_memo	== remark
                    job.accumulate("disk_size", "999999");
                    job.accumulate("lat", latitude);
                    job.accumulate("lon", longitude);
                    job.accumulate("real_qty", uploadData.getRealQty());
                    job.accumulate("fail_reason", uploadData.getFailReason());
                    job.accumulate("retry_day", uploadData.getRetryDay());
                    job.accumulate("app_id", DataUtil.appID);
                    job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                    methodName = "SetPickupUploadData";
                }


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
                            "invoice_no=? COLLATE NOCASE and reg_id = ?", new String[]{uploadData.getNoSongjang(), opID});
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  Upload Exception : " + e.toString());
                result.setResultCode(-15);
                result.setResultMsg("Exception : " + e.toString());
            }

            return result;
        }
    }


    public DeviceDataUploadHelper execute() {
        ServerUploadTask serverUploadTask = new ServerUploadTask();
        serverUploadTask.execute();
        return this;
    }
}