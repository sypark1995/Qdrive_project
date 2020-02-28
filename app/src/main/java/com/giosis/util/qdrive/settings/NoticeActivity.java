package com.giosis.util.qdrive.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

/**
 * @author krm0219
 **/
public class NoticeActivity extends AppCompatActivity {
    String TAG = "NoticeActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    ListView list_notice;
    LinearLayout layout_notice_reload;
    Button btn_notice_reload;

    Context context;
    String opID;
    String officeCode;

    ProgressDialog progressDialog = null;
    NoticeListAdapter noticeListAdapter;
    ArrayList<NoticeResult.NoticeListItem> listItemArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        list_notice = findViewById(R.id.list_notice);

        layout_notice_reload = findViewById(R.id.layout_notice_reload);
        btn_notice_reload = findViewById(R.id.btn_notice_reload);


        //
        text_top_title.setText(R.string.text_title_notice);

        layout_top_back.setOnClickListener(clickListener);

        progressDialog = new ProgressDialog(NoticeActivity.this);
        context = getApplicationContext();
        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());


        NoticeAsyncTask noticeAsyncTask = new NoticeAsyncTask();
        noticeAsyncTask.execute();
    }


    public class NoticeAsyncTask extends AsyncTask<Void, Void, NoticeResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
            progressDialog.show();
        }

        @Override
        protected NoticeResult doInBackground(Void... params) {

            NoticeResult result = new NoticeResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode("-16");
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
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetNoticeData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray resultArray = jsonObject.getJSONArray("ResultObject");

                String resultCode = jsonObject.getString("ResultCode");
                String resultMsg = jsonObject.getString("ResultMsg");

                result.setResultCode(resultCode);
                result.setResultMsg(resultMsg);

                listItemArrayList = new ArrayList<>();

                for (int i = 0; i < resultArray.length(); i++) {

                    JSONObject resultObject = resultArray.getJSONObject(i);

                    NoticeResult.NoticeListItem noticeListItem = new NoticeResult.NoticeListItem();
                    noticeListItem.setNoticeNo(resultObject.getString("nid"));
                    noticeListItem.setNoticeDate(resultObject.getString("reg_dt_short"));
                    noticeListItem.setNoticeTitle(resultObject.getString("title"));
                    listItemArrayList.add(noticeListItem);
                }
                result.setResultObject(listItemArrayList);

            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetNoticeData Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode("-15");
                result.setResultMsg(msg);
            }

            return result;
        }

        @Override
        protected void onPostExecute(NoticeResult result) {
            super.onPostExecute(result);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    progressDialog.dismiss();
                }
            } catch (Exception e) {

                // !((Activity)context).isFinishing()
            }

            if (result.getResultCode().equals("0")) {

                list_notice.setVisibility(View.VISIBLE);
                layout_notice_reload.setVisibility(View.GONE);


                noticeListAdapter = new NoticeListAdapter(NoticeActivity.this, listItemArrayList);
                list_notice.setAdapter(noticeListAdapter);
            } else {

                list_notice.setVisibility(View.GONE);
                layout_notice_reload.setVisibility(View.VISIBLE);
            }
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.layout_top_back) {
                finish();
            }
        }
    };
}