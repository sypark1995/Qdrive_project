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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author krm0219
 **/
public class NoticeDetailActivity extends AppCompatActivity {
    String TAG = "NoticeDetailActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_notice_detail;
    TextView text_notice_detail_content;
    TextView text_notice_detail_date;
    LinearLayout layout_notice_detail_prev;
    LinearLayout layout_notice_detail_next;

    LinearLayout layout_notice_detail_reload;
    Button btn_notice_detail_reload;

    Context context;
    String opID;
    String officeCode;

    String noticeNo;
    String prevNoticeNo = null;
    String nextNoticeNo = null;

    ProgressDialog progressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_notice_detail = findViewById(R.id.layout_notice_detail);
        text_notice_detail_content = findViewById(R.id.text_notice_detail_content);
        text_notice_detail_date = findViewById(R.id.text_notice_detail_date);
        layout_notice_detail_prev = findViewById(R.id.layout_notice_detail_prev);
        layout_notice_detail_next = findViewById(R.id.layout_notice_detail_next);

        layout_notice_detail_reload = findViewById(R.id.layout_notice_detail_reload);
        btn_notice_detail_reload = findViewById(R.id.btn_notice_detail_reload);


        //
        text_top_title.setText(R.string.text_title_notice);

        layout_top_back.setOnClickListener(clickListener);
        layout_notice_detail_prev.setOnClickListener(clickListener);
        layout_notice_detail_next.setOnClickListener(clickListener);
        btn_notice_detail_reload.setOnClickListener(clickListener);

        progressDialog = new ProgressDialog(NoticeDetailActivity.this);
        context = getApplicationContext();
//        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();

        noticeNo = getIntent().getStringExtra("notice_no");
        Log.e("krm0219", TAG + "  notice No > " + noticeNo);

        NoticeDetailAsyncTask noticeDetailAsyncTask = new NoticeDetailAsyncTask(noticeNo);
        noticeDetailAsyncTask.execute();
    }


    public class NoticeDetailAsyncTask extends AsyncTask<Void, Void, NoticeResult> {

        String noticeNo;

        NoticeDetailAsyncTask(String noticeNo) {

            this.noticeNo = noticeNo;
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
        protected NoticeResult doInBackground(Void... params) {

            NoticeResult result = new NoticeResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode("-16");
                result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));

                return result;
            }

            try {

                JSONObject job = new JSONObject();
                job.accumulate("gubun", "DETAIL");
                job.accumulate("kind", "QSIGN");
                job.accumulate("page_no", 0);
                job.accumulate("page_size", 0);
                job.accumulate("nid", noticeNo);
                job.accumulate("svc_nation_cd", "SG");
                job.accumulate("opId", opID);
                job.accumulate("officeCd", officeCode);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetNoticeData";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray resultArray = jsonObject.getJSONArray("ResultObject");
                JSONObject resultObject = resultArray.getJSONObject(0);

                String resultCode = jsonObject.getString("ResultCode");
                String resultMsg = jsonObject.getString("ResultMsg");

                result.setResultCode(resultCode);
                result.setResultMsg(resultMsg);

                ArrayList<NoticeResult.NoticeListItem> listItemArrayList = new ArrayList<>();

                NoticeResult.NoticeListItem noticeListItem = new NoticeResult.NoticeListItem();
                noticeListItem.setNoticeNo(resultObject.getString("nid"));
                noticeListItem.setNoticeDate(resultObject.getString("reg_dt_long"));
                noticeListItem.setNoticeContent(resultObject.getString("title"));
                noticeListItem.setPrevNo(resultObject.getString("prevnid"));
                noticeListItem.setNextNo(resultObject.getString("nextnid"));
                listItemArrayList.add(noticeListItem);

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

            DisplayUtil.dismissProgressDialog(progressDialog);

            if (result.getResultCode().equals("0")) {

                layout_notice_detail.setVisibility(View.VISIBLE);
                layout_notice_detail_reload.setVisibility(View.GONE);

                text_notice_detail_content.setText(result.getResultObject().get(0).getNoticeContent());
                text_notice_detail_date.setText(result.getResultObject().get(0).getNoticeDate());


                if (result.getResultObject().get(0).getNextNo().length() == 0) {

                    layout_notice_detail_next.setVisibility(View.GONE);
                } else {

                    layout_notice_detail_next.setVisibility(View.VISIBLE);
                    nextNoticeNo = result.getResultObject().get(0).getNextNo();
                }

                if (result.getResultObject().get(0).getPrevNo().length() == 0) {

                    layout_notice_detail_prev.setVisibility(View.GONE);
                } else {

                    layout_notice_detail_prev.setVisibility(View.VISIBLE);
                    prevNoticeNo = result.getResultObject().get(0).getPrevNo();
                }
            } else {

                layout_notice_detail.setVisibility(View.GONE);
                layout_notice_detail_reload.setVisibility(View.VISIBLE);
            }
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.layout_notice_detail_prev: {

                    noticeNo = prevNoticeNo;

                    NoticeDetailAsyncTask noticeDetailAsyncTask = new NoticeDetailAsyncTask(noticeNo);
                    noticeDetailAsyncTask.execute();
                }
                break;

                case R.id.layout_notice_detail_next: {

                    noticeNo = nextNoticeNo;

                    NoticeDetailAsyncTask noticeDetailAsyncTask = new NoticeDetailAsyncTask(noticeNo);
                    noticeDetailAsyncTask.execute();
                }
                break;

                case R.id.btn_notice_detail_reload: {

                    NoticeDetailAsyncTask noticeDetailAsyncTask = new NoticeDetailAsyncTask(noticeNo);
                    noticeDetailAsyncTask.execute();
                }
                break;
            }
        }
    };
}