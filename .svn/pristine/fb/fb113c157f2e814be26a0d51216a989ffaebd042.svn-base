package com.giosis.util.qdrive.list;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;


public class ScanPackingListDownloadHelper extends ManualHelper {
    String TAG = "ScanPackingListDownloadHelper";

    private final Context context;
    private final String pickupNo;
    private final String opID;

    private OnScanPackingListDownloadEventListener eventListener;

    public static class Builder {

        private final Context context;
        private final String opID;
        private final String pickupNo;

        private OnScanPackingListDownloadEventListener eventListener;

        public Builder(Context context, String opID, String pickupNo) {

            this.context = context;
            this.opID = opID;
            this.pickupNo = pickupNo;
        }

        public ScanPackingListDownloadHelper build() {
            return new ScanPackingListDownloadHelper(this);
        }

        Builder setOnScanPackingListDownloadEventListener(OnScanPackingListDownloadEventListener eventListener) {
            this.eventListener = eventListener;

            return this;
        }
    }

    private ScanPackingListDownloadHelper(Builder builder) {

        this.context = builder.context;
        this.opID = builder.opID;
        this.pickupNo = builder.pickupNo;

        this.eventListener = builder.eventListener;
    }


    class ScannedPackingListAsyncTask extends AsyncTask<Void, Void, Integer> {

        int resultCode = -999;
        PickupPackingListResult pickupPackingResult = null;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            pickupPackingResult = getPickupPackingListData();

            if (pickupPackingResult != null && pickupPackingResult.getResultObject() != null) {

                resultCode = pickupPackingResult.getResultCode();
            }

            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    DisplayUtil.dismissProgressDialog(progressDialog);
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }

            try {

                if (result == 0) {

                    if (eventListener != null) {
                        eventListener.onScanPackingListDownloadResult(pickupPackingResult);
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
            }
        }
    }


    private PickupPackingListResult getPickupPackingListData() {

        PickupPackingListResult resultObj = null;

        try {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", opID);
            hmActionParam.put("pickup_no", pickupNo);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "getScanPackingList";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            // {"ResultObject":[{"packing_no":"SG19611828","reg_dt":"Aug 21 2019 10:41AM","op_id":"karam.kim","pickup_no":"P42147N"}],"ResultCode":0,"ResultMsg":"SUCCESS"}
            Log.e("Server", methodName + "  Result : " + resultString);

            resultObj = serializer.read(PickupPackingListResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getScanPackingList Exception : " + e.toString());
        }

        return resultObj;
    }


    public ScanPackingListDownloadHelper execute() {
        ScannedPackingListAsyncTask scannedPackingListAsyncTask = new ScannedPackingListAsyncTask();
        scannedPackingListAsyncTask.execute();
        return this;
    }

    public interface OnScanPackingListDownloadEventListener {
        void onScanPackingListDownloadResult(PickupPackingListResult result);
    }
}