package com.giosis.util.qdrive.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

import java.util.ArrayList;

/**
 * @author krm0219
 **/
public class NoticeListAdapter extends BaseAdapter {

    private Context context;


    private ArrayList<NoticeResult.NoticeListItem> noticeListItemArrayList;

    public NoticeListAdapter(Context mContext, ArrayList<NoticeResult.NoticeListItem> noticeListItemArrayList) {

        this.context = mContext;
        this.noticeListItemArrayList = noticeListItemArrayList;
    }


    @Override
    public int getCount() {
        if (noticeListItemArrayList != null && noticeListItemArrayList.size() > 0) {
            return noticeListItemArrayList.size();
        }

        return 0;
    }


    @Override
    public Object getItem(int position) {
        return noticeListItemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_notice_list, null);
        } else {

            view = convertView;
        }

        final RelativeLayout layout_notice_list_item = view.findViewById(R.id.layout_notice_list_item);
        TextView text_notice_list_item_title = view.findViewById(R.id.text_notice_list_item_title);
        TextView text_notice_list_item_date = view.findViewById(R.id.text_notice_list_item_date);


        text_notice_list_item_title.setText(noticeListItemArrayList.get(position).getNoticeTitle());
        text_notice_list_item_date.setText(noticeListItemArrayList.get(position).getNoticeDate());


        layout_notice_list_item.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        layout_notice_list_item.setBackgroundColor(context.getResources().getColor(R.color.color_f6f6f6));
                    }
                    break;

                    case MotionEvent.ACTION_UP: {

                        layout_notice_list_item.setBackgroundColor(context.getResources().getColor(R.color.white));

                        Intent intent = new Intent(context, NoticeDetailActivity.class);
                        intent.putExtra("notice_no", noticeListItemArrayList.get(position).getNoticeNo());
                        context.startActivity(intent);
                    }
                    break;

                    case MotionEvent.ACTION_CANCEL: {

                        layout_notice_list_item.setBackgroundColor(context.getResources().getColor(R.color.white));
                    }
                    break;
                }

                return true;
            }
        });

        return view;
    }
}
