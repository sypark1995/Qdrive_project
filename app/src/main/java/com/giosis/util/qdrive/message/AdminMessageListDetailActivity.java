package com.giosis.util.qdrive.message;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_XmlPullParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
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
public class AdminMessageListDetailActivity extends AppCompatActivity {
    String TAG = "AdminMessageListDetailActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_message_detail_title;
    ListView list_message_detail_message;
    EditText edit_message_detail_input;
    LinearLayout layout_message_detail_send;


    Context mContext;

    String opID;
    Boolean isConn = false;
    String senderID;

    MessageDetailAdapter messageDetailAdapter;
    private static ArrayList<MessageDetailResult.MessageDetailList> messageDetailList;

    AsyncHandler handler;
    AdminThread adminThread = null;
    public static final int SEND_ADMIN_START = 200;

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
        senderID = getIntent().getStringExtra("sender_id");

        text_top_title.setText(senderID);
        text_message_detail_title.setVisibility(View.GONE);

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

        DataUtil.setAdminMessageListDetailActivity(this);
        isConn = NetworkUtil.isNetworkAvailable(mContext);

        if (!isConn) {

            try {

                showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
            } catch (Exception e) {

            }
            return;
        } else {

            handler = new AsyncHandler();
            adminThread = new AdminThread();
            adminThread.start();
        }
    }


    private class AsyncHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            try {

                if (!isFinishing()) {

                    AdminMessageDetailAsyncTask adminMessageDetailAsyncTask = new AdminMessageDetailAsyncTask(opID, senderID);
                    adminMessageDetailAsyncTask.execute();
                }
            } catch (Exception e) {

                Log.e("krm0219", TAG + "  AsyncHandler Exception : " + e.toString());
            }
        }
    }

    // NOTI  :  AdminThread
    class AdminThread extends Thread {

        public AdminThread() {
        }

        @Override
        public void run() {
            super.run();

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    Message message = handler.obtainMessage();
                    message.what = SEND_ADMIN_START;
                    handler.sendMessage(message);

                    sleep(5 * 60 * 1000);
                } catch (InterruptedException e) {

                    Log.e("krm0219", TAG + "  AdminThread Exception : " + e.toString());
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }

            Log.e("krm0219", TAG + "  AdminThread while break");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        try {

            adminThread.interrupt();
        } catch (Exception e) {

            Thread.currentThread().interrupt();
        }
    }


    //NOTIFICATION.  AdminMessageDetailAsyncTask
    private class AdminMessageDetailAsyncTask extends AsyncTask<Void, Void, String> {

        String qdriver_id;
        String sender_id;

        ProgressDialog progressDialog = new ProgressDialog(AdminMessageListDetailActivity.this);

        public AdminMessageDetailAsyncTask(String QdriverID, String SenderID) {

            qdriver_id = QdriverID;
            sender_id = SenderID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            old_resultString = new_resultString;

            if (new_resultString == null) {

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getResources().getString(R.string.text_please_wait));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("qdriver_id", qdriver_id);
            hmActionParam.put("senderID", sender_id);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetQdriverMessageDetailFromMessenger";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":[{"rownum":"1","title":"driver4 and karam.kim\u0027s messages","rcv_id":null,"send_place":"M","rcv_place":"","reg_id":null,"sender_id":"driver4","sender_nm":null,"recv_id":"karam.kim","recv_nm":null,"read_dt":"2018-08-30 14:52:07.257","align":"left","qlps_admin_yn":null,"tracking_No":null,"question_seq_no":"1","seq_no":"11933","contr_no":null,"svc_nation_cd":null,"read_yn":null,"contents":"1","send_dt":"2018-08-30 14:52:02"},{"rownum":"1","title":"driver4 and karam.kim\u0027s messages","rcv_id":null,"send_place":"P","rcv_place":"","reg_id":null,"sender_id":"karam.kim","sender_nm":null,"recv_id":"driver4","recv_nm":null,"read_dt":"2018-08-30 15:01:54.107","align":"right","qlps_admin_yn":null,"tracking_No":null,"question_seq_no":"1","seq_no":"11935","contr_no":null,"svc_nation_cd":null,"read_yn":null,"contents":"ha😀","send_dt":"2018-08-30 15:01:39"},{"rownum":"1","title":"driver4 and karam.kim\u0027s messages","rcv_id":null,"send_place":"P","rcv_place":"","reg_id":null,"sender_id":"karam.kim","sender_nm":null,"recv_id":"driver4","recv_nm":null,"read_dt":"2018-08-30 15:20:12.520","align":"right","qlps_admin_yn":null,"tracking_No":null,"question_seq_no":"1","seq_no":"11938","contr_no":null,"svc_nation_cd":null,"read_yn":null,"contents":"ho😘","send_dt":"2018-08-30 15:07:44"},{"rownum":"1","title":"driver4 and karam.kim\u0027s messages","rcv_id":null,"send_place":"P","rcv_place":"","reg_id":null,"sender_id":"karam.kim","sender_nm":null,"recv_id":"driver4","recv_nm":null,"read_dt":"2018-08-31 09:24:41.160","align":"right","qlps_admin_yn":null,"tracking_No":null,"question_seq_no":"1","seq_no":"11983","contr_no":null,"svc_nation_cd":null,"read_yn":null,"contents":"hmm....😫","send_dt":"2018-08-31 09:24:31"}],"ResultCode":0,"ResultMsg":"OK"}

            new_resultString = resultString;
            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {
                if (old_resultString != null && old_resultString.equalsIgnoreCase(new_resultString)) {

                    Log.e("krm0219", TAG + "  AdminMessageDetailAsyncTask  EQUAL");
                } else {

                    MessageDetailResult result = Custom_XmlPullParser.getMessageDetailList(resultString);

                    if (result != null) {

                        messageDetailList = (ArrayList<MessageDetailResult.MessageDetailList>) result.getResultObject();

                        if (messageDetailList.size() > 0) {

                            for (int i = 0; i < messageDetailList.size(); i++) {        // 초 second 제거

                                String date_string = messageDetailList.get(i).getSend_date();
                                String[] date_array = date_string.split(":");

                                date_string = date_array[0] + ":" + date_array[1];
                                messageDetailList.get(i).setSend_date(date_string);
                            }

                            messageDetailAdapter = new MessageDetailAdapter(mContext, messageDetailList, "A");
                            list_message_detail_message.setAdapter(messageDetailAdapter);

                            text_message_detail_title.setText(messageDetailList.get(0).getTitle());
                            list_message_detail_message.setSelection(list_message_detail_message.getCount() - 1);
                        } else {

                            Toast.makeText(mContext, getResources().getString(R.string.text_empty), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {

                Toast.makeText(mContext, getResources().getString(R.string.text_error) + "!! " + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + " AdminMessageDetailAsyncTask Exception : " + e.toString());
            }
        }
    }


    private void sendChatMessage() {

        send_title = text_message_detail_title.getText().toString().trim();
        send_message = edit_message_detail_input.getText().toString().trim();

        if (send_message.equals("")) {
            Toast.makeText(mContext, getResources().getString(R.string.msg_enter_message), Toast.LENGTH_SHORT).show();
            return;
        }

        SendMessageAdminAsyncTask sendMessageAdminAsyncTask = new SendMessageAdminAsyncTask(send_message, opID, senderID);
        sendMessageAdminAsyncTask.execute();
    }

    //NOTIFICATION.  SendMessageAdminAsyncTask
    private class SendMessageAdminAsyncTask extends AsyncTask<Void, Void, String> {

        String contents;
        String driver_id;
        String sender_id;

        ProgressDialog progressDialog = new ProgressDialog(AdminMessageListDetailActivity.this);

        public SendMessageAdminAsyncTask(String Message, String QdriverID, String SenderID) {

            contents = Message;
            driver_id = QdriverID;
            sender_id = SenderID;

            Log.e("krm0219", TAG + "  SendMessageAdminAsyncTask  " + contents + "  " + driver_id + "  " + sender_id);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.text_send_message));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("contents", contents);
            hmActionParam.put("driver_id", driver_id);
            hmActionParam.put("sender_id", sender_id);
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "SendQdriveToMessengerMessage";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":{"ResultCode":"12242","ResultMsg":"SUCCESS"},"ResultCode":0,"ResultMsg":"OK"}

            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                MessageSendResult result = Custom_XmlPullParser.sendMessageResult(resultString);

                if (result != null) {

                    layout_message_detail_send.setBackgroundResource(R.drawable.btn_send_qpost);
                    edit_message_detail_input.setHint(R.string.msg_qpost_edit_text_hint);
                    edit_message_detail_input.setText("");

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String today = simpleDateFormat.format(Calendar.getInstance().getTime());

                    MessageDetailResult.MessageDetailList item = new MessageDetailResult.MessageDetailList();
                    item.setMessage(send_message);
                    item.setSender_id(sender_id);
                    item.setReceive_id(opID);
                    item.setSend_date(today);
                    item.setAlign("right");

                    messageDetailList.add(item);
                    messageDetailAdapter.notifyDataSetChanged();

                    Log.e("krm0219", "SendMessageAdminAsyncTask Size : " + messageDetailList.size());
                } else {

                    Toast.makeText(AdminMessageListDetailActivity.this, getResources().getString(R.string.msg_send_message_error) +
                            " \n" + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                    Log.e("krm0219", "SendMessageAdminAsyncTask  result null");
                }
            } catch (Exception e) {

                Toast.makeText(AdminMessageListDetailActivity.this, getResources().getString(R.string.msg_send_message_error) +
                        " \n" + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + " SendMessageAdminAsyncTask Exception : " + e.toString());
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

            AdminMessageDetailAsyncTask adminMessageDetailAsyncTask = new AdminMessageDetailAsyncTask(opID, senderID);
            adminMessageDetailAsyncTask.execute();
        } catch (Exception e) {

            Toast.makeText(mContext, getResources().getString(R.string.msg_left_and_come_back), Toast.LENGTH_SHORT).show();
            Log.e("krm0219", TAG + "  Exception : " + e.toString());
        }
    }
}