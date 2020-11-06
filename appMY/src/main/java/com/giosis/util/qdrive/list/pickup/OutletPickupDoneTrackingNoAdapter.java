package com.giosis.util.qdrive.list.pickup;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.list.OutletPickupDoneResult;
import com.giosis.util.qdrive.international.R;

/**
 * @author krm0219
 **/
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
            return result.getTrackingNoList().size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return result.getTrackingNoList().get(position);
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
            view = inflater.inflate(R.layout.outlet_pickup_done_tracking_no_item, null);
        } else {
            view = convertView;
        }

        RelativeLayout layout_outlet_pickup_done_tracking_no = view.findViewById(R.id.layout_outlet_pickup_done_tracking_no);
        LinearLayout layout_outlet_pickup_done_tracking_no_item = view.findViewById(R.id.layout_outlet_pickup_done_tracking_no_item);
        ImageView img_outlet_pickup_done_tracking_no_item = view.findViewById(R.id.img_outlet_pickup_done_tracking_no_item);
        TextView text_outlet_pickup_done_tracking_no_item = view.findViewById(R.id.text_outlet_pickup_done_tracking_no_item);
        Button img_outlet_pickup_done_tracking_no_item_check = view.findViewById(R.id.img_outlet_pickup_done_tracking_no_item_check);

        final OutletPickupDoneResult.OutletPickupDoneTrackingNoItem item = result.getTrackingNoList().get(position);

        text_outlet_pickup_done_tracking_no_item.setText(item.getTrackingNo());

        if (item.isScanned()) {

            layout_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.bg_radius_10_cccccc);
            img_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode);
            text_outlet_pickup_done_tracking_no_item.setTextColor(context.getResources().getColor(R.color.color_303030));
            img_outlet_pickup_done_tracking_no_item_check.setBackgroundResource(R.drawable.qdrive_btn_icon_big_on);
        } else {

            layout_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.bg_radius_10_dedede);
            img_outlet_pickup_done_tracking_no_item.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode_off);
            text_outlet_pickup_done_tracking_no_item.setTextColor(context.getResources().getColor(R.color.color_909090));
            img_outlet_pickup_done_tracking_no_item_check.setBackgroundResource(R.drawable.qdrive_btn_icon_big_off);
        }

/*
        if (route.equals("FL")) {

            if (result.getTrackingNoList().size() == 1) {

                layout_outlet_pickup_done_tracking_no.setPadding(0, dpTopx(20), 0, dpTopx(20));
            } else {

                if (position == 0) {

                    layout_outlet_pickup_done_tracking_no.setPadding(0, dpTopx(20), 0, dpTopx(7));
                } else if (position == result.getTrackingNoList().size() - 1) {

                    layout_outlet_pickup_done_tracking_no.setPadding(0, dpTopx(7), 0, dpTopx(20));
                } else {

                    layout_outlet_pickup_done_tracking_no.setPadding(0, dpTopx(7), 0, dpTopx(7));
                }
            }
        }*/

        return view;
    }

    private int dpTopx(float dp) {

        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return pixel;
    }
}
