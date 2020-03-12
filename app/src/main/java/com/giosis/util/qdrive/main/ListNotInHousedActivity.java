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
import com.giosis.util.qdrive.util.Custom_XmlPullParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

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


    private class NotInHousedServerDownloadAsyncTask extends AsyncTask<Void, Void, String> {

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
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("opId", op_id);
            hmActionParam.put("officeCd", office_code);
            hmActionParam.put("device_id", device_id);
            hmActionParam.put("network_type", network_type);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetOutStandingInhousedPickupList";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // <ResultCode>0</ResultCode><ResultMsg>SUCCESS</ResultMsg><ResultObject><QdriveCheckNotInhousedPickupList><contr_no>55003835</contr_no><partner_ref_no>P42147N</partner_ref_no><invoice_no>P42147N</invoice_no><stat>P3</stat><req_nm>KARAM</req_nm><req_dt>2019-08-2109:00-17:00</req_dt><tel_no>01012345678</tel_no><hp_no>01012345678</hp_no><zip_code>99785</zip_code><address>13 BUKIT TERESA CLOSE#10-16</address><pickup_hopeday>2019-08-21</pickup_hopeday><pickup_hopetime>09:00-17:00</pickup_hopetime><sender_nm /><del_memo /><driver_memo>(by Qdrive RealTime-Upload)</driver_memo><fail_reason>  </fail_reason><qty>1</qty><cust_nm>Qxpress</cust_nm><partner_id>qxpress.sg</partner_id><cust_no>100012253</cust_no><real_qty>1</real_qty><not_processed_qty>1</not_processed_qty><pickup_cmpl_dt>Aug 21 2019 11:42AM</pickup_cmpl_dt><qdriveOutstandingInhousedPickupLists><QdriveOutstandingInhousedPickupList><packing_no>SG19611828</packing_no><shipping_no /><tracking_no>SG19611828</tracking_no><purchased_amt>25.00</purchased_amt><purchased_currency>SGD</purchased_currency><stat>P3</stat></QdriveOutstandingInhousedPickupList></qdriveOutstandingInhousedPickupLists></QdriveCheckNotInhousedPickupList></ResultObject>

            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                NotInHousedResult result = Custom_XmlPullParser.getNotInHousedList(resultString);
                Log.e("krm0219", TAG + " Size : " + result.getResultObject().size());

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