package com.giosis.util.qdrive.list;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

public class GetRouteMasterAsyncTask extends AsyncTask<Void, Void, SmartRouteResult> {
    String TAG = "GetRouteMasterAsyncTask";

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog progressDialog;

    private AsyncTaskCallback callback;
    private String driverID;

    public GetRouteMasterAsyncTask(Context context, ProgressDialog progressDialog, String driverID, AsyncTaskCallback callback) {

        this.context = context;
        this.progressDialog = progressDialog;

        this.callback = callback;
        this.driverID = driverID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected SmartRouteResult doInBackground(Void... params) {

        SmartRouteResult result = new SmartRouteResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode("-16");
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", driverID);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetSmartRouteInfo";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultObject":[{"seq_no":"149","route_id":"149","route_name":"smart route 1","short_google_url":"https://xroute.page.link/62Fx","reg_dt":"2019-08-26 오전 10:04:39"}],"ResultCode":0,"ResultMsg":"SUCCESS"}


            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("ResultObject");

            String resultCode = jsonObject.getString("ResultCode");
            String resultMsg = jsonObject.getString("ResultMsg");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

            ArrayList<SmartRouteResult.RouteMaster> routeMasterArrayList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject resultObject = jsonArray.getJSONObject(i);

                SmartRouteResult.RouteMaster routeMaster = new SmartRouteResult.RouteMaster();
                routeMaster.setRouteID(resultObject.getString("route_id"));
                routeMaster.setRouteName(resultObject.getString("route_name"));
                routeMaster.setRouteRegDate(resultObject.getString("reg_dt"));
                routeMaster.setRouteNo(resultObject.getString("seq_no"));
                routeMaster.setGoogleURL(resultObject.getString("short_google_url"));
                routeMasterArrayList.add(routeMaster);
            }

            result.setRouteMasterList(routeMasterArrayList);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetSmartRouteInfo Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode("-15");
            result.setResultMsg(msg);
        }

        return result;
    }

    @Override
    protected void onPostExecute(SmartRouteResult result) {
        super.onPostExecute(result);

        DisplayUtil.dismissProgressDialog(progressDialog);

        if (callback != null) {


            if (result.getResultCode().equals("0") && result.getRouteMasterList() != null) {

                callback.onSuccess(result);
            } else {

                callback.onFailure(result);
            }
        }
    }


    public interface AsyncTaskCallback {
        void onSuccess(SmartRouteResult result);

        void onFailure(SmartRouteResult result);
    }
}