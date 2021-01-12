package com.giosis.library.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * @author krm0219  2018.07.26
 */
public class ListNotInHousedActivity extends CommonActivity {
    String TAG = "ListNotInHousedActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    ExpandableListView exlist_not_in_housed_list;
    TextView text_not_in_housed_empty;

    ListNotInHousedAdapter listNotInHousedAdapter;

    Context context;

    String opID;
    String officeCode;
    String deviceID;
    String networkType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_in_housed);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);


        exlist_not_in_housed_list = findViewById(R.id.exlist_not_in_housed_list);
        text_not_in_housed_empty = findViewById(R.id.text_not_in_housed_empty);


        //
        context = getApplicationContext();

        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        deviceID = Preferences.INSTANCE.getDeviceUUID();

        networkType = NetworkUtil.getNetworkType(context);


        text_top_title.setText(R.string.navi_sub_not_in_housed);

        layout_top_back.setOnClickListener(view -> finish());

        exlist_not_in_housed_list.setOnGroupExpandListener(groupPosition -> {
            int group_count = listNotInHousedAdapter.getGroupCount();

            for (int i = 0; i < group_count; i++) {
                if (i != groupPosition)
                    exlist_not_in_housed_list.collapseGroup(i);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (NetworkUtil.isNetworkAvailable(ListNotInHousedActivity.this)) {

            NotInHousedServerDownloadAsyncTask asyncTask = new NotInHousedServerDownloadAsyncTask(opID, officeCode, deviceID, networkType);
            asyncTask.execute();
        } else {

            Toast.makeText(ListNotInHousedActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
        }
    }


    private class NotInHousedServerDownloadAsyncTask extends AsyncTask<Void, Void, NotInHousedResult> {

        String op_id;
        String office_code;
        String device_id;
        String network_type;

        ProgressDialog progressDialog = new ProgressDialog(ListNotInHousedActivity.this);

        public NotInHousedServerDownloadAsyncTask(String opID, String officeCode, String deviceID, String networkType) {

            op_id = opID;
            office_code = officeCode;
            device_id = deviceID;
            network_type = networkType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected NotInHousedResult doInBackground(Void... params) {

            NotInHousedResult resultObj;
            Gson gson = new Gson();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("opId", op_id);
                job.accumulate("officeCd", office_code);
                job.accumulate("device_id", device_id);
                job.accumulate("network_type", network_type);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());


                String methodName = "GetOutStandingInhousedPickupList";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                resultObj = gson.fromJson(jsonString, NotInHousedResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetOutStandingInhousedPickupList Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }

        @Override
        protected void onPostExecute(NotInHousedResult result) {
            super.onPostExecute(result);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                if (result.getResultObject().size() == 0) {

                    text_not_in_housed_empty.setText(context.getResources().getString(R.string.text_empty));
                    text_not_in_housed_empty.setVisibility(View.VISIBLE);
                    exlist_not_in_housed_list.setVisibility(View.GONE);
                } else {

                    text_not_in_housed_empty.setVisibility(View.GONE);
                    exlist_not_in_housed_list.setVisibility(View.VISIBLE);

                    listNotInHousedAdapter = new ListNotInHousedAdapter(ListNotInHousedActivity.this, result);
                    exlist_not_in_housed_list.setAdapter(listNotInHousedAdapter);
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                text_not_in_housed_empty.setText(context.getResources().getString(R.string.msg_please_try_again));
                text_not_in_housed_empty.setVisibility(View.VISIBLE);
                exlist_not_in_housed_list.setVisibility(View.GONE);
            }
        }
    }
}