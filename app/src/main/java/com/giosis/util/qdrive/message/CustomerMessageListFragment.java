package com.giosis.util.qdrive.message;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_XmlPullParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;


/**
 * @author krm0219
 **/
public class CustomerMessageListFragment extends Fragment {
    private String TAG = "CustomerMessageListFragment";

    private Context mContext;
    private View view;

    private ListView list_message_list;
    private TextView text_message_list_empty;

    private RelativeLayout layout_message_list_bottom;
    private LinearLayout layout_message_list_prev;
    private LinearLayout layout_message_list_next;
    private TextView text_message_list_current_page;
    private TextView text_message_list_total_page;


    private MessageListAdapter messageListAdapter;
    private static ArrayList<MessageListResult.MessageList> messageList;

    private String opID;

    private int current_page_no = 1;
    private int total_page_no = 1;

    // 1 min refresh
    private AsyncHandler handler;
    private Thread customerThread;
    private static final int SEND_CUTOMER_START = 100;

    private String old_resultString = null;
    private String new_resultString = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        view = inflater.inflate(R.layout.fragment_message_list, container, false);

        list_message_list = view.findViewById(R.id.list_message_list);
        text_message_list_empty = view.findViewById(R.id.text_message_list_empty);

        layout_message_list_bottom = view.findViewById(R.id.layout_message_list_bottom);
        layout_message_list_prev = view.findViewById(R.id.layout_message_list_prev);
        layout_message_list_next = view.findViewById(R.id.layout_message_list_next);
        text_message_list_current_page = view.findViewById(R.id.text_message_list_current_page);
        text_message_list_total_page = view.findViewById(R.id.text_message_list_total_page);


        layout_message_list_prev.setOnClickListener(clickListener);
        layout_message_list_next.setOnClickListener(clickListener);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!NetworkUtil.isNetworkAvailable(mContext)) {

            try {
                showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
            } catch (Exception e) {

            }

            return;
        } else {

            opID = SharedPreferencesHelper.getSigninOpID(mContext);

            handler = new AsyncHandler();
            customerThread = new CustomerThread();
            customerThread.start();
        }
    }


    private class AsyncHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            try {

                if (getActivity() != null && !getActivity().isFinishing()) {

                    CustomerMessageListAsyncTask customerMessageListAsyncTask = new CustomerMessageListAsyncTask(opID, "", "", Integer.toString(current_page_no), "15");
                    customerMessageListAsyncTask.execute();
                } else {

                    Log.e("krm0219", TAG + "  getActivity().isFinishing()");
                }
            } catch (Exception e) {

                Log.e("krm0219", TAG + "  AsyncHandler Exception : " + e.toString());
            }
        }
    }

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
                } catch (Exception e) {

                    Log.e("krm0219", TAG + "  CustomerThread Exception : " + e.toString());
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }

            Log.e("krm0219", TAG + "  CustomerThread while break");
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        try {

            customerThread.interrupt();
        } catch (Exception e) {

            Thread.currentThread().interrupt();
        }
    }


    //NOTIFICATION.  CustomerMessageListAsyncTask
    private class CustomerMessageListAsyncTask extends AsyncTask<Void, Void, String> {

        String qdriver_id;
        String search_start_date;
        String search_end_date;
        String page_no;
        String page_size;

        ProgressDialog progressDialog = new ProgressDialog(mContext);


        public CustomerMessageListAsyncTask(String QdriverID, String StartDate, String EndDate, String PageNo, String PageSize) {

            qdriver_id = QdriverID;
            search_start_date = StartDate;  // ""
            search_end_date = EndDate;      // ""
            page_no = PageNo;
            page_size = PageSize;           // 15
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

            Log.i("krm0219", TAG + "  CustomerMessageListAsyncTask  PageNumber : " + page_no);

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();
            hmActionParam.put("qdriver_id", qdriver_id);
            hmActionParam.put("page_no", page_no);              // current page number
            hmActionParam.put("page_size", page_size);          // list size
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -14); // 최근 2주
            Date yDate = cal.getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            search_start_date = dateFormat.format(yDate) + " 00:00:00";
            search_end_date = dateFormat.format(new Date()) + " 23:59:59";

            try {

                hmActionParam.put("search_start_dt", URLEncoder.encode(search_start_date, "UTF-8"));
                hmActionParam.put("search_end_dt", URLEncoder.encode(search_end_date, "UTF-8"));
            } catch (Exception e) {

            }

            String methodName = "GetQdriverMessageList";

            GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":[{"total_count":"1","total_page":"1","rownum":"1","sender_id":null,"tracking_No":"SGP148544451","question_seq_no":"261829215","seq_no":"261831370","contr_no":"300781188","svc_nation_cd":"SG","read_yn":"Y","contents":"Thank you Jumali, I’ve received your delivery in good condition! Sorry for the late response! 😂 have a good weekend! ✌🏻","send_dt":"2019-08-02 오후 10:36:59"}],"ResultCode":0,"ResultMsg":"OK"}

            new_resultString = resultString;
            return resultString;
        }

        @Override
        protected void onPostExecute(String resultString) {
            super.onPostExecute(resultString);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {

                if (old_resultString != null && old_resultString.equalsIgnoreCase(new_resultString)) {

                    Log.e("krm0219", TAG + "  CustomerMessageListAsyncTask  EQUAL");
                } else {

                    MessageListResult result = Custom_XmlPullParser.getCustomerMessageList(resultString);

                    if (result != null) {

                        messageList = (ArrayList<MessageListResult.MessageList>) result.getResultObject();

                        if (messageList.size() > 0) {

                            list_message_list.setVisibility(View.VISIBLE);
                            text_message_list_empty.setVisibility(View.GONE);
                            layout_message_list_bottom.setVisibility(View.VISIBLE);

                            messageListAdapter = new MessageListAdapter(getActivity(), "C", messageList);
                            list_message_list.setAdapter(messageListAdapter);

                            total_page_no = messageList.get(0).getTotal_page_size();
                            text_message_list_current_page.setText(Integer.toString(current_page_no));
                            text_message_list_total_page.setText(Integer.toString(total_page_no));

                            int count = 0;

                            for (int i = 0; i < messageList.size(); i++) {
                                if (messageList.get(i).getRead_yn().equals("N")) {
                                    count++;
                                }
                            }

                            ((MessageListActivity) getActivity()).setCustomerNewImage(count);
                        } else {        // List Size 0 : Empty

                            list_message_list.setVisibility(View.GONE);
                            text_message_list_empty.setVisibility(View.VISIBLE);
                            layout_message_list_bottom.setVisibility(View.GONE);

                            text_message_list_empty.setText("Empty");
                        }
                    }
                }
            } catch (Exception e) {

                list_message_list.setVisibility(View.GONE);
                text_message_list_empty.setVisibility(View.VISIBLE);
                layout_message_list_bottom.setVisibility(View.GONE);

                text_message_list_empty.setText(getResources().getString(R.string.text_error));

                Toast.makeText(mContext, getResources().getString(R.string.text_error) + "!! " + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + "  CustomerMessageListAsyncTask Exception : " + e.toString());
            }
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.layout_message_list_prev: {

                    if (!NetworkUtil.isNetworkAvailable(mContext)) {

                        showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
                        return;
                    } else {

                        if (current_page_no > 1) {
                            current_page_no = current_page_no - 1;
                            CustomerMessageListAsyncTask customerMessageListAsyncTask = new CustomerMessageListAsyncTask(opID, "", "", Integer.toString(current_page_no), "15");
                            customerMessageListAsyncTask.execute();
                        } else {

                            Toast.makeText(mContext, getResources().getString(R.string.text_first_page), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;

                case R.id.layout_message_list_next: {

                    if (!NetworkUtil.isNetworkAvailable(mContext)) {

                        showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
                        return;
                    } else {
                        if (!(total_page_no < current_page_no + 1)) {
                            current_page_no = current_page_no + 1;
                            CustomerMessageListAsyncTask customerMessageListAsyncTask = new CustomerMessageListAsyncTask(opID, "", "", Integer.toString(current_page_no), "15");
                            customerMessageListAsyncTask.execute();
                        } else {

                            Toast.makeText(mContext, getResources().getString(R.string.text_last_page), Toast.LENGTH_SHORT).show();
                        }
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
                        getActivity().finish();
                    }
                });
        alert_internet_status.show();
    }

    public void refreshData() {

        try {

            CustomerMessageListAsyncTask customerMessageListAsyncTask = new CustomerMessageListAsyncTask(opID, "", "", Integer.toString(current_page_no), "15");
            customerMessageListAsyncTask.execute();
        } catch (Exception e) {

            Toast.makeText(mContext, getResources().getString(R.string.msg_left_and_come_back), Toast.LENGTH_SHORT).show();
            Log.e("Exception", TAG + "  Exception : " + e.toString());
        }
    }
}