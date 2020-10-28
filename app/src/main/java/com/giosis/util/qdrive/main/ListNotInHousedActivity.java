package com.giosis.util.qdrive.main;

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

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * @author krm0219  2018.07.26
 */
public class ListNotInHousedActivity extends AppCompatActivity {
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

        opID = SharedPreferencesHelper.getSigninOpID(context);
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(context);
        deviceID = SharedPreferencesHelper.getSigninDeviceID(context);
        networkType = NetworkUtil.getNetworkType(context);


        text_top_title.setText(R.string.navi_sub_not_in_housed);

        layout_top_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        exlist_not_in_housed_list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                int group_count = listNotInHousedAdapter.getGroupCount();

                for (int i = 0; i < group_count; i++) {
                    if (i != groupPosition)
                        exlist_not_in_housed_list.collapseGroup(i);
                }
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

            Toast.makeText(ListNotInHousedActivity.this, getString(R.string.wifi_connect_failed), Toast.LENGTH_SHORT).show();
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
                job.accumulate("nation_cd", DataUtil.nationCode);


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

//                NotInHousedResult result = Custom_XmlPullParser.getNotInHousedList(resultString);
//                Log.e("krm0219", TAG + " Size : " + result.getResultObject().size());

                if (result.getResultObject().size() == 0) {

                    text_not_in_housed_empty.setText("Empty");
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
                text_not_in_housed_empty.setText("Error.\nPlease try again..");
                text_not_in_housed_empty.setVisibility(View.VISIBLE);
                exlist_not_in_housed_list.setVisibility(View.GONE);
            }
        }
    }
}