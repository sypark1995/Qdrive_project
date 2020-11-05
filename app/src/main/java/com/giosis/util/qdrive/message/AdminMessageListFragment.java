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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * @author krm0219
 **/
public class AdminMessageListFragment extends Fragment {
    String TAG = "AdminMessageListFragment";

    Context mContext;
    View view;

    ListView list_message_list;
    TextView text_message_list_empty;
    RelativeLayout layout_message_list_bottom;


    MessageListAdapter messageListAdapter;
    private static ArrayList<MessageListResult.MessageList> messageList;

    String opID = "";

    // 5 min refresh
    AsyncHandler handler;
    Thread adminThread;
    public static final int SEND_ADMIN_START = 200;

    String old_resultString = null;
    String new_resultString = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();

        view = inflater.inflate(R.layout.fragment_message_list, container, false);

        list_message_list = view.findViewById(R.id.list_message_list);
        text_message_list_empty = view.findViewById(R.id.text_message_list_empty);

        layout_message_list_bottom = view.findViewById(R.id.layout_message_list_bottom);
        layout_message_list_bottom.setVisibility(View.GONE);

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

            opID = MyApplication.preferences.getUserId();

            handler = new AsyncHandler();
            adminThread = new AdminThread();
            adminThread.start();
        }
    }


    private class AsyncHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            try {
                if (getActivity() != null && !getActivity().isFinishing()) {

                    AdminMessageListAsyncTask adminMessageListAsyncTask = new AdminMessageListAsyncTask(opID);
                    adminMessageListAsyncTask.execute();
                } else {
                    Log.e("krm0219", TAG + "  getActivity().isFinishing()");
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
    public void onStop() {
        super.onStop();

        try {

            adminThread.interrupt();
        } catch (Exception e) {

            Thread.currentThread().interrupt();
        }
    }


    //NOTI  :  AdminMessageListAsyncTask
    private class AdminMessageListAsyncTask extends AsyncTask<Void, Void, MessageListResult> {

        String qdriver_id;

        ProgressDialog progressDialog = new ProgressDialog(mContext);

        public AdminMessageListAsyncTask(String QdriverID) {

            qdriver_id = QdriverID;
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
        protected MessageListResult doInBackground(Void... params) {

            MessageListResult resultObj;
            Gson gson = new Gson();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("qdriver_id", qdriver_id);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "GetQdriverMessageListFromMessenger";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                new_resultString = jsonString;

                resultObj = gson.fromJson(jsonString, MessageListResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetQdriverMessageListFromMessenger Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }

        @Override
        protected void onPostExecute(MessageListResult result) {
            super.onPostExecute(result);

            DisplayUtil.dismissProgressDialog(progressDialog);

            try {
                if (old_resultString != null && old_resultString.equalsIgnoreCase(new_resultString)) {

                    Log.e("krm0219", TAG + "  AdminMessageListAsyncTask  EQUAL");
                } else {

//                    MessageListResult result = Custom_XmlPullParser.getAdminMessageList(resultString);

                    if (result != null) {

                        messageList = (ArrayList<MessageListResult.MessageList>) result.getResultObject();

                        if (messageList.size() > 0) {

                            list_message_list.setVisibility(View.VISIBLE);
                            text_message_list_empty.setVisibility(View.GONE);

                            messageListAdapter = new MessageListAdapter(getActivity(), "A", messageList);
                            list_message_list.setAdapter(messageListAdapter);

                            int count = 0;

                            for (int i = 0; i < messageList.size(); i++) {
                                if (messageList.get(i).getRead_yn().equals("N")) {
                                    count++;
                                }
                            }

                            ((MessageListActivity) getActivity()).setAdminNewImage(count);
                        } else {

                            list_message_list.setVisibility(View.GONE);
                            text_message_list_empty.setVisibility(View.VISIBLE);

                            text_message_list_empty.setText("Empty");
                        }
                    }
                }
            } catch (Exception e) {

                list_message_list.setVisibility(View.GONE);
                text_message_list_empty.setVisibility(View.VISIBLE);

                text_message_list_empty.setText(getResources().getString(R.string.text_error));

                Toast.makeText(mContext, getResources().getString(R.string.text_error) + "!! " + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + "  AdminMessageListAsyncTask Exception : " + e.toString());
            }
        }
    }


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

            AdminMessageListAsyncTask adminMessageListAsyncTask = new AdminMessageListAsyncTask(opID);
            adminMessageListAsyncTask.execute();
        } catch (Exception e) {

            Log.e("krm0219", TAG + "  Exception : " + e.toString());
            Toast.makeText(mContext, getResources().getString(R.string.msg_left_and_come_back), Toast.LENGTH_SHORT).show();
        }
    }
}