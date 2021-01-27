package com.giosis.util.qdrive.barcodescanner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.server.ImageUpload;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.international.UploadData;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class ManualPodUploadHelper {
    String TAG = "ManualPodUploadHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;
    private final ArrayList<UploadData> assignBarcodeList;

    private final String networkType;
    private final OnPodUploadEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog allSuccessDialog;
    private final AlertDialog resultDialog;
    private ArrayList<String> failedList;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;
        private final ArrayList<UploadData> assignBarcodeList;

        private String networkType;
        private OnPodUploadEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID, ArrayList<UploadData> assignBarcodeList) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.assignBarcodeList = assignBarcodeList;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public ManualPodUploadHelper build() {
            return new ManualPodUploadHelper(this);
        }

        Builder setOnPodUploadEventListener(OnPodUploadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualPodUploadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;
        this.assignBarcodeList = builder.assignBarcodeList;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.allSuccessDialog = getAllSuccessAlertDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);

    }

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_set_transfer));
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private AlertDialog getAllSuccessAlertDialog(Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();

                        if (eventListener != null) {
                            eventListener.onPostResult();
                        }
                    }
                }).create();

        return dialog;
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_upload_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();

                        if (eventListener != null) {
                            eventListener.onPostFailList(failedList);
                        }
                    }
                })
                .create();

        return dialog;
    }

    private void showAllSuccessDialog(String message) {
        allSuccessDialog.setMessage(message);
        allSuccessDialog.show();
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }


    class PodUploadTask extends AsyncTask<Void, Integer, ArrayList<Integer>> {
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
        protected ArrayList<Integer> doInBackground(Void... params) {

            ArrayList<Integer> resultList = new ArrayList<>();
            failedList = new ArrayList<>();

            if (assignBarcodeList != null && 0 < assignBarcodeList.size()) {

                int resultCode = -999;
                for (UploadData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getNoSongjang())) {
                        resultCode = requestServerUpload(assignData);
                    }

                    if (resultCode < 0) {
                        failedList.add(assignData.getNoSongjang());
                    }

                    resultList.add(resultCode);
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
        protected void onPostExecute(ArrayList<Integer> resultList) {
            super.onPostExecute(resultList);

            DisplayUtil.dismissProgressDialog(progressDialog);


            int successCount = 0;
            int failCount = 0;
            int resultCode = -999;

            try {

                for (int i = 0; i < resultList.size(); i++) {

                    resultCode = resultList.get(i);
                    if (resultCode < 0) {

                        failCount++;
                    } else {
                        successCount++;
                    }
                }

                if (0 < successCount && failCount == 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showAllSuccessDialog(msg);
                } else {

                    if (resultCode == -16) {

                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved));
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count3), successCount, failCount);
                        showResultDialog(msg);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private int requestServerUpload(UploadData uploadData) {

            if (!NetworkUtil.isNetworkAvailable(context)) {
                return -16;
            }

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();
            int result = -15;

            try {

                JSONObject job = new JSONObject();

                if (uploadData.getType().equals("D")) {

                    String bitmapString = "";
                    String dirPath = Environment.getExternalStorageDirectory().toString() + "/Qdrive";
                    String filePath = dirPath + "/SC_" + uploadData.getNoSongjang() + ".png";   //SC_SGSG101402.png
                    File imgFile = new File(filePath);

                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        bitmapString = DataUtil.bitmapToString(myBitmap, ImageUpload.QXPOP, "qdriver/sign", uploadData.getNoSongjang());

                        if (bitmapString.equals("")) {
                            return -100;
                        }
                    }

                    if (bitmapString.equals("")) {
                        return -14;
                    }

                    job.accumulate("stat", uploadData.getStat());
                    job.accumulate("chg_id", opID);
                    job.accumulate("deliv_msg", "(by Qdrive Scan Delivery Sheet)");
                    job.accumulate("opId", opID);
                    job.accumulate("officeCd", officeCode);
                    job.accumulate("device_id", deviceID);
                    job.accumulate("network_type", networkType);
                    job.accumulate("no_songjang", uploadData.getNoSongjang());
                    job.accumulate("fileData", bitmapString);
                    job.accumulate("punchOut_stat", "N");
                    job.accumulate("app_id", DataUtil.appID);
                    job.accumulate("nation_cd", DataUtil.nationCode);
                }

                String methodName = "SetScanTransportDataRefac";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":-12,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result = jsonObject.getInt("ResultCode");

                if (result == 0) {
                    dbHelper.delete(DatabaseHelper.DB_TABLE_SCAN_DELIVERY, "invoice_no='" + uploadData.getNoSongjang() + "' COLLATE NOCASE  and reg_id ='" + opID + "'");
                } else {

                    ContentValues contentVal2 = new ContentValues();
                    contentVal2.put("punchOut_stat", String.valueOf(result).replace('-', 'F'));

                    dbHelper.update(DatabaseHelper.DB_TABLE_SCAN_DELIVERY, contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{uploadData.getNoSongjang(), opID});
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetScanTransportDataRefac Exception : " + e.toString());
                ContentValues contentVal2 = new ContentValues();
                contentVal2.put("punchOut_stat", "F15");

                dbHelper.update(DatabaseHelper.DB_TABLE_SCAN_DELIVERY, contentVal2,
                        "invoice_no=? COLLATE NOCASE " + "and reg_id = ?", new String[]{uploadData.getNoSongjang(), opID});
            }

            return result;
        }
    }


    public ManualPodUploadHelper execute() {
        PodUploadTask PodUploadTask = new PodUploadTask();
        PodUploadTask.execute();
        return this;
    }

    public interface OnPodUploadEventListener {
        void onPostResult();

        void onPostFailList(ArrayList<String> resultList);
    }
}