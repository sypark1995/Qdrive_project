package com.giosis.library.list;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetRouteDetailAsyncTask extends AsyncTask<Void, Void, SmartRouteResult.RouteMaster> {
    String TAG = "GetRouteDetailAsyncTask";

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog progressDialog;

    private AsyncTaskCallback callback;
    private String driverID;
    private String routeNo;

    public GetRouteDetailAsyncTask(Context context, ProgressDialog progressDialog, String driverID, String routeNo, AsyncTaskCallback callback) {

        this.context = context;
        this.progressDialog = progressDialog;

        this.callback = callback;
        this.driverID = driverID;
        this.routeNo = routeNo;
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
    protected SmartRouteResult.RouteMaster doInBackground(Void... params) {

        SmartRouteResult.RouteMaster result = new SmartRouteResult.RouteMaster();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode("-16");
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("opId", driverID);
            job.accumulate("route_no", routeNo);
            job.accumulate("app_id", Preferences.INSTANCE.getUserId());
            job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

            String methodName = "GetSmartRouteParcelList";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultObject":[{"route_no":"149","tracking_no":"SG19611830","contr_no":"55003837","sort_order":"1"},{"route_no":"149","tracking_no":"SG19611819","contr_no":"55003829","sort_order":"2"},{"route_no":"149","tracking_no":"C2859SGSG","contr_no":"55003355","sort_order":"3"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("ResultObject");

            String resultCode = jsonObject.getString("ResultCode");
            String resultMsg = jsonObject.getString("ResultMsg");

            result.setResultCode(resultCode);
            result.setResultMsg(resultMsg);

            ArrayList<SmartRouteResult.RouteMaster.RouteDetail> routeDetailArrayList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject resultObject = jsonArray.getJSONObject(i);

                SmartRouteResult.RouteMaster.RouteDetail routeDetail = new SmartRouteResult.RouteMaster.RouteDetail();
                routeDetail.setRouteNo(resultObject.getString("route_no"));
                routeDetail.setTrackingNo(resultObject.getString("tracking_no"));
                routeDetail.setContrNo(resultObject.getString("contr_no"));
                routeDetail.setSortNo(resultObject.getInt("sort_order"));
                routeDetailArrayList.add(routeDetail);
            }

            result.setRouteDetailArrayList(routeDetailArrayList);

        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetSmartRouteParcelList Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode("-15");
            result.setResultMsg(msg);
        }

        return result;
    }

    @Override
    protected void onPostExecute(SmartRouteResult.RouteMaster result) {
        super.onPostExecute(result);

        DisplayUtil.dismissProgressDialog(progressDialog);

        if (callback != null) {

            if (result.getResultCode().equals("0") && result.getRouteDetailArrayList() != null) {

                callback.onSuccess(result);
            } else {

                callback.onFailure(result);
            }
        }
    }


    public interface AsyncTaskCallback {
        void onSuccess(SmartRouteResult.RouteMaster result);

        void onFailure(SmartRouteResult.RouteMaster result);
    }
}

