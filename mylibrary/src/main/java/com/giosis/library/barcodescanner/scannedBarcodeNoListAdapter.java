package com.giosis.library.barcodescanner;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giosis.library.R;
import com.giosis.library.list.BarcodeData;
import com.giosis.library.util.BarcodeType;

import java.util.ArrayList;


public class scannedBarcodeNoListAdapter extends BaseAdapter {
    String TAG = "scannedBarcodeNoListAdapter";

    Context context;

    private String scanType;
    private ArrayList<BarcodeData> items;


    public scannedBarcodeNoListAdapter(Context context, ArrayList<BarcodeData> objects, String mScanType) {

        this.context = context;
        this.scanType = mScanType;
        this.items = objects;
    }

    @Override
    public int getCount() {

        if (items != null && items.size() > 0) {
            return items.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_capture_scanned, null);
        } else {

            view = convertView;
        }

        LinearLayout layout_caption_list_item = view.findViewById(R.id.layout_caption_list_item);
        ImageView img_capture_list_item_barcode = view.findViewById(R.id.img_capture_list_item_barcode);
        TextView text_capture_list_item_barcode = view.findViewById(R.id.text_capture_list_item_barcode);
        Button btn_capture_list_item_state = view.findViewById(R.id.btn_capture_list_item_state);


        String barcodeNumber = items.get(position).getBarcode();
        String barcodeState = items.get(position).getState();


        if (barcodeNumber != null) {

            text_capture_list_item_barcode.setText(barcodeNumber);

            if (barcodeState.equals("SUCCESS")) {

                layout_caption_list_item.setBackgroundResource(R.drawable.bg_round_10_cccccc);
                img_capture_list_item_barcode.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode);
                text_capture_list_item_barcode.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_36px));
                text_capture_list_item_barcode.setTextColor(context.getResources().getColor(R.color.color_303030));
                btn_capture_list_item_state.setVisibility(View.VISIBLE);
                btn_capture_list_item_state.setBackgroundResource(R.drawable.qdrive_btn_icon_big_on);

                if (scanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                    text_capture_list_item_barcode.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_24px));
                    btn_capture_list_item_state.setVisibility(View.GONE);
                } else if (scanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                    layout_caption_list_item.setBackgroundResource(R.drawable.bg_round_5_ffcc00);
                }
            } else if (barcodeState.equals("FAIL")) {

                layout_caption_list_item.setBackgroundResource(R.drawable.bg_round_10_dedede);
                img_capture_list_item_barcode.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode_off);
                text_capture_list_item_barcode.setTextColor(context.getResources().getColor(R.color.color_909090));
                btn_capture_list_item_state.setBackgroundResource(R.drawable.qdrive_btn_icon_big_off);

                if (scanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                    btn_capture_list_item_state.setVisibility(View.INVISIBLE);
                } else {

                    btn_capture_list_item_state.setVisibility(View.VISIBLE);
                }
            }
        }

        return view;
    }
}