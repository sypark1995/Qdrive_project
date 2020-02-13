package com.giosis.util.qdrive.qdelivery;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

public class SearchAddressDialog extends Dialog {
    String TAG = "SearchAddressDialog";

    SearchListener searchListener;
    Context context;

    Button btn_search_address_close;
    TextView text_search_address_country;
    EditText edit_search_address_keyword;
    Button btn_search_address_delete;
    Button btn_search_address_search;
    ListView list_search_address;
    Button btn_search_address_apply;


    ProgressDialog progressDialog = null;
    ArrayList<String> zipCodeArrayList;
    ArrayList<String> addressArrayList;
    SearchAddressListAdapter searchAddressListAdapter;

    String keyword;
    String selectedZipCode;
    String selectedFrontAddress;


    public SearchAddressDialog(Context context) {
        super(context);
        this.context = context;
    }

    interface SearchListener {
        void onSubmitClicked(String zipcode, String address);
    }

    public void setDialogListener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search_address);
        //  context = getApplicationContext();

        btn_search_address_close = findViewById(R.id.btn_search_address_close);
        text_search_address_country = findViewById(R.id.text_search_address_country);
        edit_search_address_keyword = findViewById(R.id.edit_search_address_keyword);
        btn_search_address_delete = findViewById(R.id.btn_search_address_delete);
        btn_search_address_search = findViewById(R.id.btn_search_address_search);
        list_search_address = findViewById(R.id.list_search_address);
        btn_search_address_apply = findViewById(R.id.btn_search_address_apply);


        btn_search_address_close.setOnClickListener(clickListener);
        btn_search_address_delete.setOnClickListener(clickListener);
        btn_search_address_search.setOnClickListener(clickListener);
        btn_search_address_apply.setOnClickListener(clickListener);


        //
        edit_search_address_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (1 <= charSequence.length()) {

                    btn_search_address_delete.setVisibility(View.VISIBLE);
                } else {

                    btn_search_address_delete.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        list_search_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                selectedZipCode = zipCodeArrayList.get(position);
                selectedFrontAddress = addressArrayList.get(position);

                SearchAddressListAdapter.setSelectedItem(position);
                searchAddressListAdapter.notifyDataSetChanged();
            }
        });


        progressDialog = new ProgressDialog(context);
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.btn_search_address_close: {

                    dismiss();
                }
                break;

                case R.id.btn_search_address_delete: {

                }
                break;

                case R.id.btn_search_address_search: {

                    keyword = edit_search_address_keyword.getText().toString();

                    if (keyword.length() != 0) {

                        Log.e("krm0219", "Keyword : " + keyword);
                    } else {

                        Toast.makeText(context, "Please enter zipcode or keyword", Toast.LENGTH_SHORT).show();
                    }

                    SearchAddressAsyncTask searchAddressAsyncTask = new SearchAddressAsyncTask();
                    searchAddressAsyncTask.execute();
                }
                break;


                case R.id.btn_search_address_apply: {

                    searchListener.onSubmitClicked(selectedZipCode, selectedFrontAddress);
                    dismiss();
                }
                break;
            }
        }
    };


    public class SearchAddressAsyncTask extends AsyncTask<Void, Void, SearchAddressResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
            progressDialog.show();
        }

        @Override
        protected SearchAddressResult doInBackground(Void... params) {

            SearchAddressResult result = new SearchAddressResult();

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

                zipCodeArrayList = new ArrayList<>();
                addressArrayList = new ArrayList<>();
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetNoticeData Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }

        @Override
        protected void onPostExecute(SearchAddressResult result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if (result.getResultCode() == 0) {

                addressArrayList.add("11 PEKIN STREET 123 123");
                addressArrayList.add("LIFELONG LEARNING INSTITUTE 11 EUNOS ROAD 8");
                addressArrayList.add("13 BUKIT TERESA CLOSE");
                addressArrayList.add("11 PEKIN STREET 123 123");
                addressArrayList.add("LIFELONG LEARNING INSTITUTE 11 EUNOS ROAD 8");
                addressArrayList.add("13 BUKIT TERESA CLOSE");
                addressArrayList.add("11 PEKIN STREET 123 123");
                addressArrayList.add("LIFELONG LEARNING INSTITUTE 11 EUNOS ROAD 8");
                addressArrayList.add("13 BUKIT TERESA CLOSE");
                result.setAdddressArrayList(addressArrayList);

                zipCodeArrayList.add("12345");
                zipCodeArrayList.add("67890");
                zipCodeArrayList.add("12786");
                zipCodeArrayList.add("12345");
                zipCodeArrayList.add("67890");
                zipCodeArrayList.add("12786");
                zipCodeArrayList.add("12345");
                zipCodeArrayList.add("67890");
                zipCodeArrayList.add("12786");
                result.setZipCodeArrayList(zipCodeArrayList);

                searchAddressListAdapter = new SearchAddressListAdapter(context, result);
                list_search_address.setAdapter(searchAddressListAdapter);
            } else {

            }
        }
    }


    @Override
    public void onBackPressed() {
    }
}

