package com.giosis.util.qdrive.singapore.list.pickup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;


public class OutletPickupDoneTrackingNoAdapter extends BaseAdapter {
    String TAG = "OutletPickupDoneTrackingNoAdapter";

    Context context;
    OutletPickupDoneResult result;
    String route;

    public OutletPickupDoneTrackingNoAdapter(Context context, OutletPickupDoneResult result, String route) {
        this.context = context;
        this.result = result;
        this.route = route;
    }

    @Override
    public int getCount() {
        if (result != null) {
            return result.getResultObject().getTrackingNoList().size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return result.getResultObject().getTrackingNoList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_outlet_pickup_done_tracking_no, null);
        } else {
            view = convertView;
        }

        LinearLayout layout_outlet_pickup_done_tracking_no_item = view.findViewById(R.id.layout_outlet_pickup_done_tracking_no_item);
        ImageView img_outlet_pickup_done_tracking_no_item = view.findViewById(R.id.img_outlet_pickup_done_tracking_no_item);
        TextView text_outlet_pickup_done_tracking_no_item = view.findViewById(R.id.text_outlet_pickup_done_tracking_no_item);
        Button img_outlet_pickup_done_tracking_no_item_check = view.findViewById(R.id.img_outlet_pickup_done_tracking_no_item_check);

        final OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem item = result.getResultObject().getTrackingNoList().get(position);

        text_outlet_pickup_done_tracking_no_item.setText(item.getTrackingNo());

        if (item.isScanned()) {

            layout_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.bg_round_10_cccccc);
            img_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode);
            text_outlet_pickup_done_tracking_no_item.setTextColor(context.getResources().getColor(R.color.color_303030));
            img_outlet_pickup_done_tracking_no_item_check.setBackgroundResource(R.drawable.qdrive_btn_icon_big_on);
        } else {

            layout_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.bg_round_10_dedede);
            img_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode_off);
            text_outlet_pickup_done_tracking_no_item.setTextColor(context.getResources().getColor(R.color.color_909090));
            img_outlet_pickup_done_tracking_no_item_check.setBackgroundResource(R.drawable.qdrive_btn_icon_big_off);
        }

        return view;
    }
}
