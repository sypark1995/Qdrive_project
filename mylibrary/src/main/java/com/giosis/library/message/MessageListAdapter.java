package com.giosis.library.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.library.R;

import java.util.ArrayList;

public class MessageListAdapter extends BaseAdapter {

    private final Context context;
    String called_fragment;

    RelativeLayout layout_message_list_item;
    TextView text_message_list_item_title;
    TextView text_message_list_item_message;
    TextView text_message_list_item_date;


    private final ArrayList<MessageListResult.MessageList> mItems;

    public MessageListAdapter(Context context, String called, ArrayList<MessageListResult.MessageList> messageItemArrayList) {

        this.context = context;           // getActivity()
        this.called_fragment = called;
        this.mItems = messageItemArrayList;
    }


    @Override
    public int getCount() {
        if (mItems != null && mItems.size() > 0) {
            return mItems.size();
        }

        return 0;
    }


    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_message_list, null);
        } else {

            view = convertView;
        }

        layout_message_list_item = view.findViewById(R.id.layout_message_list_item);
        text_message_list_item_title = view.findViewById(R.id.text_message_list_item_title);
        text_message_list_item_message = view.findViewById(R.id.text_message_list_item_message);
        text_message_list_item_date = view.findViewById(R.id.text_message_list_item_date);


        if (mItems.get(position).getRead_yn().equals("Y")) {

            text_message_list_item_message.setTextColor(Color.parseColor("#444444"));
        } else {

            text_message_list_item_message.setTextColor(Color.parseColor("#f22020"));
        }

        if (called_fragment.equalsIgnoreCase("C")) {     // Customer

            text_message_list_item_title.setText(mItems.get(position).getTracking_no());
        } else if (called_fragment.equalsIgnoreCase("A")) {  // Admin

            text_message_list_item_title.setText(mItems.get(position).getSender_id());
        }


        if (Build.VERSION.SDK_INT >= 24) {

            text_message_list_item_message.setText(Html.fromHtml(mItems.get(position).getMessage(), Html.FROM_HTML_MODE_LEGACY));
        } else {

            text_message_list_item_message.setText(Html.fromHtml(mItems.get(position).getMessage()));
        }

        text_message_list_item_date.setText(mItems.get(position).getTime(called_fragment));


        layout_message_list_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (called_fragment.equalsIgnoreCase("C")) {     // Customer

                    Intent intent = new Intent(context, CustomerMessageListDetailActivity.class);
                    intent.putExtra("question_no", mItems.get(position).getQuestion_seq_no());
                    intent.putExtra("tracking_no", mItems.get(position).getTracking_no());
                    context.startActivity(intent);
                } else if (called_fragment.equalsIgnoreCase("A")) {  // Admin

                    Intent intent = new Intent(context, AdminMessageListDetailActivity.class);
                    intent.putExtra("sender_id", mItems.get(position).getSender_id());
                    context.startActivity(intent);
                }
            }
        });

        return view;
    }
}
