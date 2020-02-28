package com.giosis.util.qdrive.barcodescanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

public class ChangeDriverValidationCheckHelper extends ManualHelper {
    String TAG = "ChangeDriverValidationCheckHelper";

    private final Context context;
    private final String opID;
    private final String scanNo;

    private final String networkType;
    private final OnChangeDelDriverValidCheckListener eventListener;
    private final AlertDialog resultDialog;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String scanNo;

        private String networkType;
        private OnChangeDelDriverValidCheckListener eventListener;

        public Builder(Context context, String opID, String scanNo) {

            this.context = context;
            this.opID = opID;
            this.scanNo = scanNo;

            this.networkType = NetworkUtil.getNetworkType(context);
        }

        public ChangeDriverValidationCheckHelper build() {
            return new ChangeDriverValidationCheckHelper(this);
        }

        Builder setOnChangeDelDriverValidCheckListener(OnChangeDelDriverValidCheckListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ChangeDriverValidationCheckHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.scanNo = builder.scanNo;

        this.networkType = builder.networkType;
        this.eventListener = builder.eventListener;
        this.resultDialog = getResultAlertDialog(this.context);
    }

    private AlertDialog getResultAlertDialog(final Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("[ " + context.getResources().getString(R.string.text_scanned_failed) + "]")
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (dialog != null)
                            dialog.dismiss();
                    }
                }).create();

        return dialog;
    }

    private void showResultDialog(String message) {
        resultDialog.setMessage(message);
        resultDialog.show();
    }

    class ChangeDriverValidationTask extends AsyncTask<Void, Void, ChangeDriverResult> {

        @Override
        protected ChangeDriverResult doInBackground(Void... params) {

            ChangeDriverResult result = new ChangeDriverResult();

            if (scanNo != null && !scanNo.equals("")) {
                result = validateScanNo(scanNo);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ChangeDriverResult result) {
            super.onPostExecute(result);

            if (result != null) {
                if (result.getResultCode() < 0) {
                    try {
                        if (!((Activity) context).isFinishing()) {
                            showResultDialog(result.getResultMsg());
                        }
                    } catch (Exception e) {

                        Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                    }
                }
            }

            if (result == null) {

                eventListener.OnChangeDelDriverValidCheckFailList(result);
            } else {

                if (eventListener != null) {
                    eventListener.OnChangeDelDriverValidCheckResult(result);
                }
            }
        }


        private ChangeDriverResult validateScanNo(String scan_no) {

            ChangeDriverResult resultObj = null;

            try {

                GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
                HashMap<String, String> hmActionParam = new HashMap<>();
                hmActionParam.put("scanData", scan_no);
                hmActionParam.put("driverId", opID);
                hmActionParam.put("app_id", DataUtil.appID);
                hmActionParam.put("nation_cd", DataUtil.nationCode);

                String methodName = "GetChangeDriverValidationCheck";
                Serializer serializer = new Persister();

                GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
                String resultString = response.getResultString();
                Log.e("Server", methodName + "  Result : " + resultString);
                // <ResultCode>0</ResultCode><ResultMsg>Success</ResultMsg><ResultObject><contr_no>55003830</contr_no><tracking_no>SG19611820</tracking_no><status>DPC3-OUT</status><del_driver_id>hyemi</del_driver_id></ResultObject>
                // <ResultCode>-3</ResultCode><ResultMsg>[SG19611819] can't be changed to you.</ResultMsg><ResultObject />
                // <ResultCode>-1</ResultCode><ResultMsg>No data.</ResultMsg><ResultObject />

                resultObj = serializer.read(ChangeDriverResult.class, resultString);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetChangeDriverValidationCheck Exception : " + e.toString());
            }

            return resultObj;
        }
    }


    public ChangeDriverValidationCheckHelper execute() {
        ChangeDriverValidationTask changeDriverValidationTask = new ChangeDriverValidationTask();
        changeDriverValidationTask.execute();
        return this;
    }

    public interface OnChangeDelDriverValidCheckListener {
        void OnChangeDelDriverValidCheckResult(ChangeDriverResult result);

        void OnChangeDelDriverValidCheckFailList(ChangeDriverResult result);
    }
}