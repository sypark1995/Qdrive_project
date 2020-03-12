package com.giosis.util.qdrive.qdelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

public class MyQDeliveryActivity extends AppCompatActivity {
    String TAG = "MyQDeliveryActivity";

    Context context;


    FrameLayout layout_top_back;
    TextView text_top_title;

    RelativeLayout layout_qd_my_select_date;
    TextView text_qd_my_select_date;
    Spinner spinner_qd_my_select_date;
    RelativeLayout layout_qd_my_search_option;
    TextView text_qd_my_search_option;
    Spinner spinner_qd_my_search_option;
    SearchView search_qd_my_search_option;
    ListView list_qd_my;
    TextView text_qd_my_no_result;

    String selectedDate;
    ArrayAdapter selectDateArrayAdapter;
    String selectedOption;
    ArrayAdapter searchOptionArrayAdapter;

    ProgressDialog progressDialog = null;
    ArrayList<MyQDeliveryResult.MYQDeliveryItem> myQDeliveryArrayList;
    MyQDeliveryListAdapter myQDeliveryListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qdelivery_my);
        context = getApplicationContext();
        progressDialog = new ProgressDialog(MyQDeliveryActivity.this);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_qd_my_select_date = findViewById(R.id.layout_qd_my_select_date);
        text_qd_my_select_date = findViewById(R.id.text_qd_my_select_date);
        spinner_qd_my_select_date = findViewById(R.id.spinner_qd_my_select_date);
        layout_qd_my_search_option = findViewById(R.id.layout_qd_my_search_option);
        text_qd_my_search_option = findViewById(R.id.text_qd_my_search_option);
        spinner_qd_my_search_option = findViewById(R.id.spinner_qd_my_search_option);
        search_qd_my_search_option = findViewById(R.id.search_qd_my_search_option);
        list_qd_my = findViewById(R.id.list_qd_my);
        text_qd_my_no_result = findViewById(R.id.text_qd_my_no_result);

        //
        text_top_title.setText(R.string.text_my_qdelivery);
        layout_top_back.setOnClickListener(clickListener);
        layout_qd_my_select_date.setOnClickListener(clickListener);
        layout_qd_my_search_option.setOnClickListener(clickListener);
        search_qd_my_search_option.setOnQueryTextListener(queryTextListener);
        search_qd_my_search_option.setOnCloseListener(closeListener);


        //
        spinner_qd_my_select_date.setPrompt(getResources().getString(R.string.text_1_month));
        selectDateArrayAdapter = ArrayAdapter.createFromResource(this, R.array.qdelivery_select_date, android.R.layout.simple_spinner_item);
        selectDateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_qd_my_select_date.setAdapter(selectDateArrayAdapter);
        spinner_qd_my_select_date.setSelection(1);

        spinner_qd_my_select_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDate = parent.getItemAtPosition(position).toString();
                text_qd_my_select_date.setText(selectedDate);

                MyQDeliveryAsyncTask myQDeliveryAsyncTask = new MyQDeliveryAsyncTask();
                myQDeliveryAsyncTask.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_qd_my_search_option.setPrompt(getResources().getString(R.string.text_pickup_no));
        searchOptionArrayAdapter = ArrayAdapter.createFromResource(this, R.array.qdelivery_search_option, android.R.layout.simple_spinner_item);
        searchOptionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_qd_my_search_option.setAdapter(searchOptionArrayAdapter);

        spinner_qd_my_search_option.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedOption = parent.getItemAtPosition(position).toString();
                text_qd_my_search_option.setText(selectedOption);

                myQDeliveryListAdapter.setSearchOption(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.layout_qd_my_select_date: {

                    spinner_qd_my_select_date.performClick();
                }
                break;

                case R.id.layout_qd_my_search_option: {

                    spinner_qd_my_search_option.performClick();
                }
                break;
            }
        }
    };


    public class MyQDeliveryAsyncTask extends AsyncTask<Void, Void, MyQDeliveryResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
            progressDialog.show();
        }

        @Override
        protected MyQDeliveryResult doInBackground(Void... params) {

            MyQDeliveryResult result = new MyQDeliveryResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

            try {

                JSONObject job = new JSONObject();
                job.accumulate("gubun", "LIST");
                job.accumulate("kind", "QSIGN");
                job.accumulate("page_no", 1);
                job.accumulate("page_size", 30);
                job.accumulate("nid", 0);
                job.accumulate("svc_nation_cd", "SG");
                job.accumulate("opId", "karam.kim");
                job.accumulate("officeCd", "0000");
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetNoticeData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                int resultCode = jsonObject.getInt("ResultCode");
                String resultMsg = jsonObject.getString("ResultMsg");
                result.setResultCode(resultCode);
                result.setResultMsg(resultMsg);

                myQDeliveryArrayList = new ArrayList<>();
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetNoticeData Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }

        @Override
        protected void onPostExecute(MyQDeliveryResult result) {
            super.onPostExecute(result);

            DisplayUtil.dismissProgressDialog(progressDialog);

            if (result.getResultCode() == 0) {

                MyQDeliveryResult.MYQDeliveryItem item = new MyQDeliveryResult.MYQDeliveryItem("P123", "O456", "2019-09-01", "P1", "가방", "200", "18.99");
                myQDeliveryArrayList.add(item);

                item = new MyQDeliveryResult.MYQDeliveryItem("P456", "O789", "2019-09-01", "P1", "옷", "150", "7.99");
                myQDeliveryArrayList.add(item);
                item = new MyQDeliveryResult.MYQDeliveryItem("P789", "O123", "2019-08-30", "P1", "연필", "10", "4.85");
                myQDeliveryArrayList.add(item);
                item = new MyQDeliveryResult.MYQDeliveryItem("P147", "O258", "2019-08-20", "P3", "이어폰", "56", "4.68");
                myQDeliveryArrayList.add(item);
                item = new MyQDeliveryResult.MYQDeliveryItem("P258", "O369", "2019-08-02", "P3", "핸드폰", "480", "24.50");
                myQDeliveryArrayList.add(item);
                item = new MyQDeliveryResult.MYQDeliveryItem("P369", "O147", "2019-08-01", "P3", "음식", "136", "16.88");
                myQDeliveryArrayList.add(item);

                myQDeliveryListAdapter = new MyQDeliveryListAdapter(context, myQDeliveryArrayList);
                list_qd_my.setAdapter(myQDeliveryListAdapter);
            } else {

            }
        }
    }


    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            myQDeliveryListAdapter.searchData(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            myQDeliveryListAdapter.searchData(query);
            return false;
        }
    };

    SearchView.OnCloseListener closeListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {
            myQDeliveryListAdapter.searchData("");
            return false;
        }
    };
}