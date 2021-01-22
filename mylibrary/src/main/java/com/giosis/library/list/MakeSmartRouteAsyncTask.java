package com.giosis.library.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.ServerResult;

import org.json.JSONObject;

public class MakeSmartRouteAsyncTask extends AsyncTask<Void, Void, ServerResult> {
    String TAG = "MakeSmartRouteAsyncTask";

    @SuppressLint("StaticFieldLeak")
    private Context context;

    private AsyncTaskCallback callback;
    private String driverID;

    public MakeSmartRouteAsyncTask(Context context, String driverID, AsyncTaskCallback callback) {

        this.context = context;

        this.callback = callback;
        this.driverID = driverID;
    }

    @Override
    protected ServerResult doInBackground(Void... params) {

        ServerResult result = new ServerResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode("-16");
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("driver_id", driverID);

            // http://xrouter.qxpress.asia/api/make_smart_route.qx?driver_id=hyemi
            String methodName = "make_smart_route.qx";
            String jsonString = Custom_JsonParser.requestGetDataReturnJSON(DataUtil.smart_route_url, methodName, driverID);
            // {"api":"make_smart_route","code":"0000","desc":"Succeed"}

            JSONObject jsonObject = new JSONObject(jsonString);

            String resultCode = jsonObject.getString("code");
            String resultMsg = jsonObject.getString("desc");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

        } catch (Exception e) {

            Log.e("Exception", TAG + "  make_smart_route Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode("-15");
            result.setResultMsg(msg);
        }

        return result;
    }

    @Override
    protected void onPostExecute(ServerResult result) {
        super.onPostExecute(result);

        if (callback != null) {

            if (result.getResultCode().equals("0000")) {

                callback.onSuccess(result);
            } else {

                callback.onFailure(result);
            }
        }
    }

    public interface AsyncTaskCallback {
        void onSuccess(ServerResult result);

        void onFailure(ServerResult result);
    }
}

