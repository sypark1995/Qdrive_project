package com.giosis.util.qdrive.message;

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

import com.giosis.util.qdrive.international.R;

import java.util.ArrayList;

public class MessageListAdapter extends BaseAdapter {

    private Context context;
    private String calledFragment;
    private ArrayList<MessageListResult.MessageList> messageLists;

    MessageListAdapter(Context context, String called, ArrayList<MessageListResult.MessageList> messageItemArrayList) {

        this.context = context;
        this.calledFragment = called;
        this.messageLists = messageItemArrayList;
    }


    @Override
    public int getCount() {

        if (messageLists != null && 0 < messageLists.size()) {
            return messageLists.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return messageLists.get(position);
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
            view = inflater.inflate(R.layout.message_list_item, null);
        } else {

            view = convertView;
        }

        RelativeLayout layout_message_list_item = view.findViewById(R.id.layout_message_list_item);
        TextView text_message_list_item_title = view.findViewById(R.id.text_message_list_item_title);
        TextView text_message_list_item_message = view.findViewById(R.id.text_message_list_item_message);
        TextView text_message_list_item_date = view.findViewById(R.id.text_message_list_item_date);


        if (messageLists.get(position).getRead_yn().equals("Y")) {

            text_message_list_item_message.setTextColor(Color.parseColor("#444444"));
        } else {

            text_message_list_item_message.setTextColor(Color.parseColor("#f22020"));
        }

        if (calledFragment.equalsIgnoreCase("C")) {     // Customer

            text_message_list_item_title.setText(messageLists.get(position).getTracking_no());
        } else if (calledFragment.equalsIgnoreCase("A")) {  // Admin

            text_message_list_item_title.setText(messageLists.get(position).getSender_id());
        }


        if (Build.VERSION.SDK_INT >= 24) {

            text_message_list_item_message.setText(Html.fromHtml(messageLists.get(position).getMessage(), Html.FROM_HTML_MODE_LEGACY));
        } else {

            text_message_list_item_message.setText(Html.fromHtml(messageLists.get(position).getMessage()));
        }

        text_message_list_item_date.setText(messageLists.get(position).getTime(calledFragment));


        layout_message_list_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (calledFragment.equalsIgnoreCase("C")) {     // Customer

                    Intent intent = new Intent(context, CustomerMessageListDetailActivity.class);
                    intent.putExtra("question_no", messageLists.get(position).getQuestion_seq_no());
                    intent.putExtra("tracking_no", messageLists.get(position).getTracking_no());
                    context.startActivity(intent);
                } else if (calledFragment.equalsIgnoreCase("A")) {  // Admin

                    Intent intent = new Intent(context, AdminMessageListDetailActivity.class);
                    intent.putExtra("sender_id", messageLists.get(position).getSender_id());
                    context.startActivity(intent);
                }
            }
        });

        return view;
    }
}