package com.giosis.util.qdrive.message;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_XmlPullParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

/**
 * @author krm0219
 */
public class CustomerMessageListDetailActivity extends AppCompatActivity {
    String TAG = "CustomerMessageListDetailActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_message_detail_title;
    ListView list_message_detail_message;
    EditText edit_message_detail_input;
    LinearLayout layout_message_detail_send;


    Context mContext;

    String opID;
    Boolean isConn = false;

    String questionNo;
    String trackingNo;

    MessageDetailAdapter messageDetailAdapter;
    private static ArrayList<MessageDetailResult.MessageDetailList> messageDetailList;

    AsyncHandler handler;
    CustomerThread customerThread = null;
    public static final int SEND_CUTOMER_START = 100;

    String old_resultString = null;
    String new_resultString = null;

    String send_title;
    String send_message;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_message_detail_title = findViewById(R.id.text_message_detail_title);
        list_message_detail_message = findViewById(R.id.list_message_detail_message);
        edit_message_detail_input = findViewById(R.id.edit_message_detail_input);
        layout_message_detail_send = findViewById(R.id.layout_message_detail_send);

        layout_top_back.setOnClickListener(clickListener);
        layout_message_detail_send.setOnClickListener(clickListener);

        list_message_detail_message.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);       // message 입력 후, List 최하단으로 이동

        //
        mContext = getApplicationContext();
        opID = SharedPreferencesHelper.getSigninOpID(mContext);

        questionNo = Integer.toString(getIntent().getIntExtra("question_no", 0));       // 최초 0
        trackingNo = getIntent().getStringExtra("tracking_no");

        Log.e("krm0219", TAG + "  " + questionNo + " / " + trackingNo);

        text_top_title.setText(trackingNo);


        edit_message_detail_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                layout_message_detail_send.setBackgroundResource(R.drawable.btn_send_qpost);
                edit_message_detail_input.setHint("");
            }
        });

        edit_message_detail_input.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layout_message_detail_send.setBackgroundResource(R.drawable.btn_send_qpost);
                edit_message_detail_input.setHint("");
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        DataUtil.setCustomerMessageListDetailActivity(this);
        isConn = NetworkUtil.isNetworkAvailable(mContext);

        if (!isConn) {

            try {

                showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
            } catch (Exception e) {

            }
            return;
        } else if (questionNo.equals("0")) {

            Log.e("krm0219", "in LIST");
            GetQuestionNumberAsyncTask getQuestionNumberAsyncTask = new GetQuestionNumberAsyncTask(opID, trackingNo);
            getQuestionNumberAsyncTask.execute();
        } else {

            handler = new AsyncHandler();
            customerThread = new CustomerThread();
            customerThread.start();
        }
    }


    private class AsyncHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            try {

                if (!isFinishing()) {

                    CustomerMessageDetailAsyncTask CustomerMessageDetailAsyncTask = new CustomerMessageDetailAsyncTask(opID, questionNo);
                    CustomerMessageDetailAsyncTask.execute();
                }
            } catch (Exception e) {

                Log.e("krm0219", TAG + "  AsyncHandler Exception : " + e.toString());
            }
        }
    }

    // NOTI  :  CustomerThread
    class CustomerThread extends Thread {

        public CustomerThread() {
        }

        @Override
        public void run() {
            super.run();

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    Message message = handler.obtainMessage();
                    message.what = SEND_CUTOMER_START;
                    handler.sendMessage(message);

                    sleep(60 * 1000);
                } catch (InterruptedException e) {

                    Log.e("krm0219", TAG + "  CustomerThread Exception : " + e.toString());
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }

            Log.e("krm0219", TAG + "  CustomerThread while break");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {

            customerThread.interrupt();
        } catch (Exception e) {

            Thread.currentThread().interrupt();
        }
    }


    //NOTIFICATION.  GetQuestionNumberAsyncTask
    private class GetQuestionNumberAsyncTask extends AsyncTask<Void, Void, String> {

        String qdriver_id;
        String tracking_no;

        ProgressDialog progressDialog = new ProgressDialog(CustomerMessageListDetailActivity.this);

        public GetQuestionNumberAsyncTask(String QdriverID, String TrackingNo) {

            qdriver_id = QdriverID;
            tracking_no = TrackingNo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.text_please_wait));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("driverId", qdriver_id);
            hmActionParam.put("trackingNo", tracking_no);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetMessageToQPostOnPickupMenu";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":[],"ResultCode":0,"ResultMsg":"OK"}

            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    progressDialog.dismiss();
                }
            } catch (Exception e) {

                // !((Activity)context).isFinishing()
            }

            try {

                MessageQuestionNumberResult result = Custom_XmlPullParser.getQuestionNumber(resultString);
                questionNo = "0";

                if (result != null && result.getQuestionNo() > 0) {

                    questionNo = Integer.toString(result.getQuestionNo());
                }

                handler = new AsyncHandler();
                customerThread = new CustomerThread();
                customerThread.start();
            } catch (Exception e) {

                Toast.makeText(mContext, getResources().getString(R.string.text_error) + "!! " + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + " GetQuestionNumberAsyncTask Exception : " + e.toString());
            }
        }
    }


    //NOTIFICATION.  CustomerMessageDetailAsyncTask      1분  refresh
    private class CustomerMessageDetailAsyncTask extends AsyncTask<Void, Void, String> {

        String qdriver_id;
        String question_seq_no;

        ProgressDialog progressDialog = new ProgressDialog(CustomerMessageListDetailActivity.this);

        public CustomerMessageDetailAsyncTask(String QdriverID, String QuestionNo) {

            qdriver_id = QdriverID;
            question_seq_no = QuestionNo;

            if (question_seq_no == null) {

                question_seq_no = "0";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            old_resultString = new_resultString;

            if (new_resultString == null) {

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getResources().getString(R.string.text_please_wait));
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("qdriver_id", qdriver_id);
            hmActionParam.put("question_seq_no", question_seq_no);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetQdriverMessageDetail";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":[{"rownum":"0","title":"This is Qxpress Driver","rcv_id":"245310801","send_place":"P","rcv_place":"F","reg_id":"Qxmessage","sender_id":"Qxmessage","sender_nm":"","recv_id":"245310801","recv_nm":"Eil***","read_dt":"2019-08-02 오후 10:36:20","align":"right","qlps_admin_yn":"N","tracking_No":"SGP148544451","question_seq_no":"261829215","seq_no":"261829215","contr_no":null,"svc_nation_cd":null,"read_yn":null,"contents":"Hi I\u0027m jumali from Qexpress. I\u0027m on my way to your delivery address, i will like to check if there is anyone to receive the parcel.","send_dt":"2019-08-02 오후 6:31:07"},{"rownum":"1","title":"[Re] This is Qxpress Driver","rcv_id":"Qxmessage","send_place":"F","rcv_place":"P","reg_id":"245310801","sender_id":"245310801","sender_nm":"Eil***","recv_id":"Qxmessage","recv_nm":"","read_dt":"2019-08-13 오후 1:01:37","align":"left","qlps_admin_yn":"N","tracking_No":"SGP148544451","question_seq_no":"261829215","seq_no":"261831370","contr_no":null,"svc_nation_cd":null,"read_yn":null,"contents":"Thank you Jumali, I’ve received your delivery in good condition! Sorry for the late response! 😂 have a good weekend! ✌🏻","send_dt":"2019-08-02 오후 10:36:59"}],"ResultCode":0,"ResultMsg":"OK"}

            new_resultString = resultString;
            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    progressDialog.dismiss();
                }
            } catch (Exception e) {

                // !((Activity)context).isFinishing()
            }

            try {
                if (old_resultString != null && old_resultString.equalsIgnoreCase(new_resultString)) {

                    Log.e("krm0219", TAG + "  CustomerMessageDetailAsyncTask  EQUAL");
                } else {

                    MessageDetailResult result = Custom_XmlPullParser.getMessageDetailList(resultString);

                    if (result != null) {

                        messageDetailList = (ArrayList<MessageDetailResult.MessageDetailList>) result.getResultObject();
                        Log.e("krm0219", TAG + " CustomerMessageDetailAsyncTask  LIST Size : " + messageDetailList.size());

                        if (messageDetailList.size() > 0) {

                            for (int i = 0; i < messageDetailList.size(); i++) {        // 초 second 제거

                                String date_string = messageDetailList.get(i).getSend_date();
                                String[] date_array = date_string.split(":");

                                date_string = date_array[0] + ":" + date_array[1];
                                messageDetailList.get(i).setSend_date(date_string);
                            }

                            messageDetailAdapter = new MessageDetailAdapter(mContext, messageDetailList, "C");
                            list_message_detail_message.setAdapter(messageDetailAdapter);

                            text_message_detail_title.setText(messageDetailList.get(0).getTitle());
                        } else {        // Driver가 처음 message 보낼 때~  (LIST에서 들어옴)

                            text_message_detail_title.setText(getResources().getString(R.string.text_qxpress_driver));

                            messageDetailList = new ArrayList<>();
                            messageDetailAdapter = new MessageDetailAdapter(mContext, messageDetailList, "C");
                            list_message_detail_message.setAdapter(messageDetailAdapter);
                        }
                    }
                }
            } catch (Exception e) {

                Toast.makeText(mContext, getResources().getString(R.string.text_error) + "!! " + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + " CustomerMessageDetailAsyncTask Exception : " + e.toString());
            }
        }
    }


    private void sendChatMessage() {

        send_title = text_message_detail_title.getText().toString().trim();
        send_message = edit_message_detail_input.getText().toString().trim();

        if (send_message.equals("")) {
            Toast.makeText(CustomerMessageListDetailActivity.this, getResources().getString(R.string.msg_enter_message), Toast.LENGTH_SHORT).show();
            return;
        }

        SendMessageAsyncTask sendMessageAsyncTask = new SendMessageAsyncTask(trackingNo, "SG", send_title, send_message, opID, questionNo, "P");
        sendMessageAsyncTask.execute();
    }

    //NOTIFICATION.  SendMessageAsyncTask
    private class SendMessageAsyncTask extends AsyncTask<Void, Void, String> {

        String tracking_no;
        String svc_nation_cd;       // 'SG'
        String title;
        String contents;
        String driver_id;
        String question_seq_no;     // 최초 0
        String send_place;          // 'P'

        ProgressDialog progressDialog = new ProgressDialog(CustomerMessageListDetailActivity.this);

        public SendMessageAsyncTask(String TrackingNo, String NationCode, String Title, String Message, String QdriverID, String QuestionNo, String SendPlace) {

            tracking_no = TrackingNo;
            svc_nation_cd = NationCode;
            title = Title;
            contents = Message;
            driver_id = QdriverID;
            question_seq_no = QuestionNo;
            send_place = SendPlace;

            Log.e("message", TAG + "  SendMessageAsyncTask DATA \n" + tracking_no + " / " + svc_nation_cd + " / " + title + " / " +
                    contents + " / " + driver_id + " / " + question_seq_no + " / " + send_place);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.text_send_message));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("tracking_no", tracking_no);
            hmActionParam.put("svc_nation_cd", svc_nation_cd);
            hmActionParam.put("title", title);
            hmActionParam.put("contents", contents);
            hmActionParam.put("driver_id", driver_id);
            hmActionParam.put("question_seq_no", question_seq_no);
            hmActionParam.put("send_place", send_place);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "SendQdriverMessage";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);

            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            try {

                if (progressDialog != null && progressDialog.isShowing()) {

                    progressDialog.dismiss();
                }
            } catch (Exception e) {

                // !((Activity)context).isFinishing()
            }

            try {
                MessageSendResult result = Custom_XmlPullParser.sendMessageResult(resultString);

                if (result != null) {
                    if (result.getObject_resultCode().equals("0")) {

                        layout_message_detail_send.setBackgroundResource(R.color.color_ebebeb);
                        edit_message_detail_input.setHint(R.string.msg_qpost_edit_text_hint);
                        edit_message_detail_input.setText("");

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd a HH:mm");
                        String today = simpleDateFormat.format(Calendar.getInstance().getTime());

                        MessageDetailResult.MessageDetailList item = new MessageDetailResult.MessageDetailList();
                        item.setTracking_no(tracking_no);
                        item.setQuestion_seq_no(questionNo);
                        item.setTitle(send_title);
                        item.setMessage(send_message);
                        item.setSender_id(opID);
                        item.setSend_date(today);
                        item.setAlign("right");

                        messageDetailList.add(item);
                        messageDetailAdapter.notifyDataSetChanged();
                    } else {

                        Toast.makeText(CustomerMessageListDetailActivity.this, getResources().getString(R.string.msg_send_message_error) +
                                " : " + result.getObject_resultMsg(), Toast.LENGTH_SHORT).show();
                        Log.e("krm0219", "SendMessageAsyncTask  ResultCode : " + result.getObject_resultCode());
                    }
                } else {

                    Toast.makeText(CustomerMessageListDetailActivity.this, getResources().getString(R.string.msg_send_message_error) +
                            " \n" + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                    Log.e("krm0219", "SendMessageAsyncTask  result null");
                }
            } catch (Exception e) {

                Toast.makeText(CustomerMessageListDetailActivity.this, getResources().getString(R.string.msg_send_message_error) +
                        " \n" + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + "  SendMessageAsyncTask Exception : " + e.toString());
            }
        }
    }


    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.layout_message_detail_send: {

                    isConn = NetworkUtil.isNetworkAvailable(mContext);

                    if (!isConn) {

                        try {
                            showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
                        } catch (Exception e) {

                        }
                        return;
                    } else {

                        sendChatMessage();
                    }
                }
                break;
            }
        }
    };

    public void showDialog(String title, String msg) {

        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(mContext);
        alert_internet_status.setTitle(title);
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alert_internet_status.show();
    }

    public void refreshData() {

        Log.e("krm0219", TAG + "  refreshData");

        try {

            CustomerMessageDetailAsyncTask CustomerMessageDetailAsyncTask = new CustomerMessageDetailAsyncTask(opID, questionNo);
            CustomerMessageDetailAsyncTask.execute();
        } catch (Exception e) {

            Toast.makeText(mContext, getResources().getString(R.string.msg_left_and_come_back), Toast.LENGTH_SHORT).show();
            Log.e("krm0219", TAG + "  Exception : " + e.toString());
        }
    }
}