package com.giosis.util.qdrive.message;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.international.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageDetailAdapter extends BaseAdapter {

    Context mContext;

    RelativeLayout layout_message_detail;

    LinearLayout layout_message_detail_receive_message;
    TextView text_message_detail_receiver_id;
    TextView text_message_detail_receive_message;
    TextView text_message_detail_receive_date;

    RelativeLayout layout_message_detail_send_message;
    TextView text_message_detail_send_date;
    TextView text_message_detail_send_message;

    ArrayList<MessageDetailResult.MessageDetailList> messageDetailListArrayList;
    String calledFragment;


    public MessageDetailAdapter(Context context, ArrayList<MessageDetailResult.MessageDetailList> item, String called) {

        this.mContext = context;
        this.messageDetailListArrayList = item;
        this.calledFragment = called;
    }

    @Override
    public int getCount() {
        if (messageDetailListArrayList != null && 0 < messageDetailListArrayList.size()) {
            return messageDetailListArrayList.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return messageDetailListArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.message_detail_item, null);
        } else {

            view = convertView;
        }

        layout_message_detail = view.findViewById(R.id.layout_message_detail);

        layout_message_detail_receive_message = view.findViewById(R.id.layout_message_detail_receive_message);
        text_message_detail_receiver_id = view.findViewById(R.id.text_message_detail_receiver_id);
        text_message_detail_receive_message = view.findViewById(R.id.text_message_detail_receive_message);
        text_message_detail_receive_date = view.findViewById(R.id.text_message_detail_receive_date);

        layout_message_detail_send_message = view.findViewById(R.id.layout_message_detail_send_message);
        text_message_detail_send_date = view.findViewById(R.id.text_message_detail_send_date);
        text_message_detail_send_message = view.findViewById(R.id.text_message_detail_send_message);


        MessageDetailResult.MessageDetailList item = messageDetailListArrayList.get(position);


        if (item.getAlign().equalsIgnoreCase("LEFT")) {
            /* from customer, admin > to driver */
            layout_message_detail_receive_message.setVisibility(View.VISIBLE);
            layout_message_detail_send_message.setVisibility(View.GONE);

            if (item.getSender_id() == null || item.getSender_id().equals("")) {

                text_message_detail_receiver_id.setText("null");
            } else {

                text_message_detail_receiver_id.setText(item.getSender_id());
            }
            text_message_detail_receive_date.setText(item.getSend_date());

            // Customer(Admin) ID   visible/gone
            if (position == 0) {

                text_message_detail_receiver_id.setVisibility(View.VISIBLE);
            } else {  // 1 이상일 때, 나의 이전  ALIGN  비교

                String prev_align = messageDetailListArrayList.get(position - 1).getAlign();
                String this_align = messageDetailListArrayList.get(position).getAlign();

                if (!this_align.equals(prev_align)) {   // right > left

                    text_message_detail_receiver_id.setVisibility(View.VISIBLE);
                } else {

                    String prev_date_string = messageDetailListArrayList.get(position - 1).getSend_date();
                    String this_date_string = messageDetailListArrayList.get(position).getSend_date();

                    long diffTime = diffTime(prev_date_string, this_date_string);
                    Log.e("krm0219", diffTime + " " + prev_date_string + "  " + this_date_string + "  " + messageDetailListArrayList.get(position).getMessage());

                    if (1 <= diffTime) {

                        text_message_detail_receiver_id.setVisibility(View.VISIBLE);
                    } else {

                        text_message_detail_receiver_id.setVisibility(View.GONE);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= 24) {

                text_message_detail_receive_message.setText(Html.fromHtml(item.getMessage(), Html.FROM_HTML_MODE_LEGACY));
            } else {

                text_message_detail_receive_message.setText(Html.fromHtml(item.getMessage()));
            }
        } else if (item.getAlign().equalsIgnoreCase("RIGHT")) {
            /* from driver > to customer */
            layout_message_detail_receive_message.setVisibility(View.GONE);
            layout_message_detail_send_message.setVisibility(View.VISIBLE);

            text_message_detail_send_date.setText(item.getSend_date());

            if (Build.VERSION.SDK_INT >= 24) {

                text_message_detail_send_message.setText(Html.fromHtml(item.getMessage(), Html.FROM_HTML_MODE_LEGACY));
            } else {

                text_message_detail_send_message.setText(Html.fromHtml(item.getMessage()));
            }
        }


        //  연속으로 메세지 보낸거 확인 > layout param 바꾸기 / Date  visible/gone
        if (position < messageDetailListArrayList.size() - 1) {

            String this_align = messageDetailListArrayList.get(position).getAlign();
            String next_align = messageDetailListArrayList.get(position + 1).getAlign();

            String this_date_string = messageDetailListArrayList.get(position).getSend_date();
            String next_date_string = messageDetailListArrayList.get(position + 1).getSend_date();

            long diffTime = diffTime(this_date_string, next_date_string);

            if (next_align.equalsIgnoreCase(this_align)) {  // left > left  // right > right

                if (1 <= diffTime) {

                    layout_message_detail.setPadding(dpTopx(15), dpTopx(5), dpTopx(15), dpTopx(10));
                    text_message_detail_receive_date.setVisibility(View.VISIBLE);
                    text_message_detail_send_date.setVisibility(View.VISIBLE);
                } else {        // 1분 미만으로 동일한 사람이 입력!

                    layout_message_detail.setPadding(dpTopx(15), dpTopx(5), dpTopx(15), dpTopx(5));
                    text_message_detail_receive_date.setVisibility(View.GONE);
                    text_message_detail_send_date.setVisibility(View.GONE);
                }
            } else {        // left > right  // right > left
                layout_message_detail.setPadding(dpTopx(15), dpTopx(5), dpTopx(15), dpTopx(10));
                text_message_detail_receive_date.setVisibility(View.VISIBLE);
                text_message_detail_send_date.setVisibility(View.VISIBLE);
            }
        } else {        // last

            layout_message_detail.setPadding(dpTopx(15), dpTopx(5), dpTopx(15), dpTopx(10));
            text_message_detail_receive_date.setVisibility(View.VISIBLE);
            text_message_detail_send_date.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private int dpTopx(float dp) {

        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
        return pixel;
    }

    private long diffTime(String old_date_string, String date_string) {

        long diff_time = 100;

        try {

            DateFormat dateFormat = null;
            if(calledFragment.equalsIgnoreCase("C")) {

                dateFormat = new SimpleDateFormat("yyyy-MM-dd a HH:mm");
            } else if(calledFragment.equalsIgnoreCase("A")) {

                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            }

            Date old_date = dateFormat.parse(old_date_string);
            Date date = dateFormat.parse(date_string);

            long old_date_time = old_date.getTime();
            long date_time = date.getTime();

            diff_time = (date_time - old_date_time) / 60000;

        } catch (Exception e) {

            e.printStackTrace();
            diff_time = 100;
        }

        return diff_time;
    }
}
