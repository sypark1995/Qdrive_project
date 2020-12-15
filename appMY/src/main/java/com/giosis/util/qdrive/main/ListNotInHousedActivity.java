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

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.ui.CommonActivity;

import org.json.JSONObject;

/**
 * @author krm0219
 */
public class ListNotInHousedActivity extends CommonActivity {
    String TAG = "ListNotInHousedActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    ExpandableListView exlist_not_in_housed_list;
    TextView text_not_in_housed_empty;

    ListNotInHousedAdapter listNotInHousedAdapter;

    Context context;

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

            NotInHousedServerDownloadAsyncTask asyncTask = new NotInHousedServerDownloadAsyncTask(networkType);
            asyncTask.execute();
        } else {

            Toast.makeText(ListNotInHousedActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
        }
    }


    private class NotInHousedServerDownloadAsyncTask extends AsyncTask<Void, Void, ListNotInHousedResult> {

        String network_type;

        ProgressDialog progressDialog = new ProgressDialog(ListNotInHousedActivity.this);

        NotInHousedServerDownloadAsyncTask(String networkType) {

            network_type = networkType;
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
        protected ListNotInHousedResult doInBackground(Void... params) {

            ListNotInHousedResult result = new ListNotInHousedResult();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("opId", MyApplication.preferences.getUserId());
                job.accumulate("officeCd", MyApplication.preferences.getOfficeCode());
                job.accumulate("device_id", MyApplication.preferences.getDeviceUUID());
                job.accumulate("network_type", network_type);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetOutStandingInhousedPickupList";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // Result : {"ResultObject":[{"contr_no":"55004095","partner_ref_no":"P42319N","invoice_no":"P42319N","stat":"P3","req_nm":"KARAM","req_dt":"2019-10-0109:00-17:00","tel_no":"01012345678","hp_no":"01012345678","zip_code":"48780","address":"50 PEKIN STREET#958","pickup_hopeday":"2019-10-01","pickup_hopetime":"09:00-17:00","sender_nm":"","del_memo":"pickup","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Qxpress","partner_id":"qxpress.sg","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":null,"del_driver_id":null,"cust_no":"100012253","real_qty":"2","not_processed_qty":"2","pickup_cmpl_dt":"Oct  1 2019  1:58PM","qdriveOutstandingInhousedPickupLists":[{"packing_no":"SG19611942","shipping_no":"","tracking_no":"SG19611942","purchased_amt":"25.00","purchased_currency":"SGD","stat":"P3"},{"packing_no":"SG19611941","shipping_no":"","tracking_no":"SG19611941","purchased_amt":"25.00","purchased_currency":"SGD","stat":"P3"}]},{"contr_no":"55004087","partner_ref_no":"P42314N","invoice_no":"P42314N","stat":"P3","req_nm":"KARAM","req_dt":"2019-09-2709:00-17:00","tel_no":"01012345678","hp_no":"01012345678","zip_code":"88702","address":"10 RAEBURN PARK#10-50","pickup_hopeday":"2019-09-27","pickup_hopetime":"09:00-17:00","sender_nm":"","del_memo":"test","driver_memo":"(by Qdrive RealTime-Upload)","fail_reason":"  ","qty":"1","cust_nm":"Qxpress","partner_id":"qxpress.sg","dr_assign_requestor":null,"dr_assign_req_dt":null,"dr_assign_stat":null,"dr_req_no":null,"failed_count":null,"route":null,"del_driver_id":null,"cust_no":"100012253","real_qty":"2","not_processed_qty":"2","pickup_cmpl_dt":"Sep 27 2019 10:51AM","qdriveOutstandingInhousedPickupLists":[{"packing_no":"SG19611931","shipping_no":"","tracking_no":"SG19611931","purchased_amt":"25.00","purchased_currency":"SGD","stat":"P3"},{"packing_no":"SG19611932","shipping_no":"","tracking_no":"SG19611932","purchased_amt":"25.00","purchased_currency":"SGD","stat":"P3"}]}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                result = Custom_JsonParser.getNotInHousedList(jsonString);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetOutStandingInhousedPickupList Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ListNotInHousedResult result) {
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

                text_not_in_housed_empty.setText(context.getResources().getString(R.string.msg_please_try_again));
                text_not_in_housed_empty.setVisibility(View.VISIBLE);
                exlist_not_in_housed_list.setVisibility(View.GONE);
            }
        }
    }
}