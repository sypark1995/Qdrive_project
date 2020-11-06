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

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;


/**
 * @author krm0219
 **/
public class CustomerMessageListFragment extends Fragment {
    String TAG = "CustomerMessageListFragment";

    private Context mContext;
    View view;

    ListView list_message_list;
    TextView text_message_list_empty;

    RelativeLayout layout_message_list_bottom;
    LinearLayout layout_message_list_prev;
    LinearLayout layout_message_list_next;
    TextView text_message_list_current_page;
    TextView text_message_list_total_page;


    MessageListAdapter messageListAdapter;
    private static ArrayList<MessageListResult.MessageList> messageList;

    Boolean isNetworkConnect = false;
    String opID;

    int current_page_no = 1;
    int total_page_no = 1;

    // 1 min refresh
    AsyncHandler handler;
    Thread customerThread;
    public static final int SEND_CUTOMER_START = 100;

    String old_resultString = null;
    String new_resultString = null;


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

        isNetworkConnect = NetworkUtil.isNetworkAvailable(mContext);

        if (!isNetworkConnect) {

            try {
                showDialog(getResources().getString(R.string.text_warning), getResources().getString(R.string.msg_network_connect_error));
            } catch (Exception e) {

            }

            return;
        } else {

            opID = MyApplication.preferences.getUserId();

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


    private class CustomerMessageListAsyncTask extends AsyncTask<Void, Void, MessageListResult> {

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
                progressDialog.show();
            }
        }

        @Override
        protected MessageListResult doInBackground(Void... params) {

            Log.e("krm0219", TAG + "  CustomerMessageListAsyncTask  PageNumber : " + page_no);
            MessageListResult resultObj;
            Gson gson = new Gson();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("qdriver_id", qdriver_id);
                job.accumulate("page_no", page_no);
                job.accumulate("page_size", page_size);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -14); // 최근 2주
                Date yDate = cal.getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                search_start_date = dateFormat.format(yDate) + " 00:00:00";
                search_end_date = dateFormat.format(new Date()) + " 23:59:59";

                try {

                    job.accumulate("search_start_dt", URLEncoder.encode(search_start_date, "UTF-8"));
                    job.accumulate("search_end_dt", URLEncoder.encode(search_end_date, "UTF-8"));
                } catch (Exception ignored) {

                }


                String methodName = "GetQdriverMessageList";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
                new_resultString = jsonString;

                resultObj = gson.fromJson(jsonString, MessageListResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetQdriverMessageList Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }

        @Override
        protected void onPostExecute(MessageListResult result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            try {

                if (old_resultString != null && old_resultString.equalsIgnoreCase(new_resultString)) {

                    Log.e("krm0219", TAG + "  CustomerMessageListAsyncTask  EQUAL");
                } else {

                    if (result != null) {

                        messageList = (ArrayList<MessageListResult.MessageList>) result.getResultObject();

                        if (0 < messageList.size()) {

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

                            text_message_list_empty.setText(getResources().getString(R.string.text_empty));
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

                    isNetworkConnect = NetworkUtil.isNetworkAvailable(mContext);

                    if (!isNetworkConnect) {

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

                    isNetworkConnect = NetworkUtil.isNetworkAvailable(mContext);

                    if (!isNetworkConnect) {

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
            Log.e("krm0219", TAG + "  Exception : " + e.toString());
        }
    }
}