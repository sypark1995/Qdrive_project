package com.giosis.util.qdrive.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.giosis.util.qdrive.international.R;

import java.util.ArrayList;

public class TodayDonePickupScanListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<PickupScanListItem> mItems;


    TodayDonePickupScanListAdapter(Context context, ArrayList<PickupScanListItem> items) {

        this.context = context;
        this.mItems = items;
    }


    @Override
    public int getCount() {
        if (mItems != null && 0 < mItems.size()) {
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
            view = inflater.inflate(R.layout.today_done_pickup_scanned_list_item, null);
        } else {

            view = convertView;
        }

        TextView text_scan_list_item_tracking_no = view.findViewById(R.id.text_scan_list_item_tracking_no);
        TextView text_scan_list_item_date = view.findViewById(R.id.text_scan_list_item_date);

        text_scan_list_item_tracking_no.setText(mItems.get(position).getTracking_no());
        text_scan_list_item_date.setText(mItems.get(position).getScanned_date());

        return view;
    }
}