package com.giosis.util.qdrive.international;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.giosis.library.list.BarcodeData;
import com.giosis.library.list.SigningView;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.server.ImageUpload;
import com.giosis.library.util.NetworkUtil;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;

import org.json.JSONObject;

import java.util.ArrayList;


public class ManualSelfCollectorHelper {
    String TAG = "ManualSelfCollectorHelper";

    private final Context context;
    private final String opID;
    private final String officeCode;
    private final String deviceID;

    private final ArrayList<BarcodeData> assignBarcodeList;
    private final SigningView signingView;
    private final String driverMemo;
    private final String receiveType;

    private final String networkType;
    private final OnSelfCollectorEventListener eventListener;
    private final ProgressDialog progressDialog;
    private final AlertDialog allSuccessDialog;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String officeCode;
        private final String deviceID;

        private final ArrayList<BarcodeData> assignBarcodeList;
        private final SigningView signingView;
        private final String driverMemo;
        private final String receiveType;

        private String networkType;
        private OnSelfCollectorEventListener eventListener;

        public Builder(Context context, String opID, String officeCode, String deviceID,
                       ArrayList<BarcodeData> assignBarcodeList, SigningView signingView, String driverMemo, String receiveType) {

            this.context = context;
            this.opID = opID;
            this.officeCode = officeCode;
            this.deviceID = deviceID;
            this.networkType = NetworkUtil.getNetworkType(context);

            this.assignBarcodeList = assignBarcodeList;
            this.signingView = signingView;
            this.driverMemo = driverMemo;
            this.receiveType = receiveType;
        }

        public ManualSelfCollectorHelper build() {
            return new ManualSelfCollectorHelper(this);
        }

        public Builder setOnSelfCollectorEventListener(OnSelfCollectorEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ManualSelfCollectorHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.officeCode = builder.officeCode;
        this.deviceID = builder.deviceID;

        this.assignBarcodeList = builder.assignBarcodeList;
        this.signingView = builder.signingView;
        this.driverMemo = builder.driverMemo;
        this.receiveType = builder.receiveType;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.progressDialog = getProgressDialog(this.context);
        this.allSuccessDialog = getAllSuccessAlertDialog(this.context);
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private ProgressDialog getProgressDialog(Context context) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.text_self_collector));
        progressDialog.setCancelable(false);

        return progressDialog;
    }

    private AlertDialog getAllSuccessAlertDialog(Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_self_collector_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (eventListener != null) {
                            eventListener.onPostFailList();
                        }
                    }
                }).create();

        return dialog;
    }

    private AlertDialog getResultAlertDialog(final Context context) {

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_self_collector_result))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.button_close), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (eventListener != null) {
                            eventListener.onPostFailList();
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


    class SelfCollectorTask extends AsyncTask<Void, Integer, ArrayList> {

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
        protected ArrayList doInBackground(Void... params) {

            ArrayList<String> resultList = new ArrayList<>();

            if (assignBarcodeList != null && 0 < assignBarcodeList.size()) {
                String result = null;
                for (BarcodeData assignData : assignBarcodeList) {
                    if (!TextUtils.isEmpty(assignData.getBarcode())) {
                        result = requestSelfCollection(assignData.getBarcode());
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
        protected void onPostExecute(ArrayList resultList) {
            super.onPostExecute(resultList);

            DisplayUtil.dismissProgressDialog(progressDialog);


            int successCount = 0;
            int failCount = 0;
            int resultCode = -999;
            String resultMsg;
            String fail_reason = "";

            try {

                for (int i = 0; i < resultList.size(); i++) {

                    String response = (String) resultList.get(i);
                    JSONObject jsonObject = new JSONObject(response);
                    resultCode = jsonObject.getInt("ResultCode");
                    resultMsg = jsonObject.getString("ResultMsg");

                    if (resultCode < 0) {

                        failCount++;

                        if (!resultMsg.equals("")) { //웹서비스에서 Msg 리턴
                            fail_reason += resultMsg;
                        } else if (resultCode == -11) {
                            fail_reason += context.getResources().getString(R.string.msg_self_collection_fail_11);
                        } else if (resultCode == -12) {
                            fail_reason += context.getResources().getString(R.string.msg_self_collection_fail_12);
                        } else if (resultCode == -13) {
                            fail_reason += context.getResources().getString(R.string.msg_self_collection_fail_13);
                        } else if (resultCode == -14) {
                            fail_reason += context.getResources().getString(R.string.msg_self_collection_fail_14);
                        } else if (resultCode == -15) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_15);
                        } else if (resultCode == -16) {
                            fail_reason += context.getResources().getString(R.string.msg_upload_fail_16);
                        } else {
                            fail_reason += context.getResources().getString(R.string.msg_self_collection_fail_etc) + String.valueOf(resultCode);
                        }
                    } else {

                        successCount++;
                    }
                }

                if (eventListener != null) {
                    eventListener.onPostResult();
                }

                if (0 < successCount && failCount == 0) {

                    String msg = String.format(context.getResources().getString(R.string.text_upload_success_count), successCount);
                    showAllSuccessDialog(msg);
                } else {

                    if (resultCode == -16) {

                        showResultDialog(context.getResources().getString(R.string.msg_network_connect_error_saved));
                    } else {

                        String msg = String.format(context.getResources().getString(R.string.text_upload_fail_count), successCount, failCount, fail_reason);
                        showResultDialog(msg);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }


        private String requestSelfCollection(String assignNo) {

            String result;

            if (!NetworkUtil.isNetworkAvailable(context)) {

                return context.getResources().getString(R.string.msg_network_connect_error_saved);
            }

            try {

                signingView.buildDrawingCache();
                Bitmap captureView = signingView.getDrawingCache();
                String bitmapString = DataUtil.bitmapToString(captureView, ImageUpload.QXPOP, "qdriver/sign", assignNo);

                if (bitmapString.equals("")) {
                    result = context.getResources().getString(R.string.msg_upload_fail_image);
                    return result;
                }


                JSONObject job = new JSONObject();
                job.accumulate("rcv_type", receiveType);
                job.accumulate("stat", BarcodeType.DELIVERY_DONE);
                job.accumulate("chg_id", opID);
                job.accumulate("deliv_msg", "(by Qdrive Self-Collector)");  //내부관리용 드라이버 메세지
                job.accumulate("opId", "SELF");  // 프로시져(up_gmkt_set_self_collector_data) 에서 del_driver_id 로 사용됨
                job.accumulate("officeCd", officeCode);
                job.accumulate("device_id", deviceID);
                job.accumulate("network_type", networkType);
                job.accumulate("no_songjang", assignNo);
                job.accumulate("fileData", bitmapString);
                job.accumulate("remark", driverMemo);  //공개용 드라이버 메세지
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "SetSelfCollectorData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":-12,"ResultMsg":"This shipping no (SG19611681) is not exist."}

                result = jsonString;
            } catch (Exception e) {

                Log.e("Exception", TAG + "  SetSelfCollectorData Exception : " + e.toString());
                result = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            }

            return result;
        }
    }


    public ManualSelfCollectorHelper execute() {
        SelfCollectorTask driverAssignTask = new SelfCollectorTask();
        driverAssignTask.execute();

        return this;
    }

    public interface OnSelfCollectorEventListener {
        void onPostResult();

        void onPostFailList();
    }
}