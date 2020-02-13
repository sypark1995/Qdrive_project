package com.giosis.util.qdrive.qdelivery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

import java.util.ArrayList;

/**
 * @author krm0219
 **/
public class MyQDeliveryListAdapter extends BaseAdapter {

    private Context context;


    private ArrayList<MyQDeliveryResult.MYQDeliveryItem> myqDeliveryItemArrayList;
    private ArrayList<MyQDeliveryResult.MYQDeliveryItem> tempArrayList;
    static String searchOption = "pickup";


    public MyQDeliveryListAdapter(Context mContext, ArrayList<MyQDeliveryResult.MYQDeliveryItem> list) {

        this.context = mContext;
        this.myqDeliveryItemArrayList = list;
        this.tempArrayList = list;
    }


    @Override
    public int getCount() {

        if (myqDeliveryItemArrayList != null && myqDeliveryItemArrayList.size() > 0) {
            return myqDeliveryItemArrayList.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return myqDeliveryItemArrayList.get(position);
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
            view = inflater.inflate(R.layout.item_my_qdelivery, null);
        } else {

            view = convertView;
        }

        LinearLayout layout_qd_my_item = view.findViewById(R.id.layout_qd_my_item);
        FrameLayout layout_qd_my_item_status = view.findViewById(R.id.layout_qd_my_item_status);
        TextView text_qd_my_item_pickup_no = view.findViewById(R.id.text_qd_my_item_pickup_no);
        TextView text_qd_my_item_status = view.findViewById(R.id.text_qd_my_item_status);
        TextView text_qd_my_item_date = view.findViewById(R.id.text_qd_my_item_date);


        if (position == 0) {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            layout_qd_my_item.setLayoutParams(lp);
        } else {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 20, 0, 0);
            layout_qd_my_item.setLayoutParams(lp);
        }


        MyQDeliveryResult.MYQDeliveryItem myqDeliveryItem = myqDeliveryItemArrayList.get(position);

        text_qd_my_item_pickup_no.setText(myqDeliveryItem.getPickupNo());
        text_qd_my_item_date.setText(myqDeliveryItem.getRegisteDate());

        if(myqDeliveryItem.getStatus().equals("P1")) {

            layout_qd_my_item_status.setBackgroundColor(context.getResources().getColor(R.color.color_4fb648));
            text_qd_my_item_status.setText("Pickup Request");
        } else if(myqDeliveryItem.getStatus().equals("P3")) {

            layout_qd_my_item_status.setBackgroundColor(context.getResources().getColor(R.color.color_ff7611));
            text_qd_my_item_status.setText("Pickup Done");
        }

        return view;
    }


    // Pickup no. / Order no
    public static void setSearchOption(String option) {
        searchOption = option;
    }

    //NOTIFICATION.  Search
    public void searchData(String query) {

        try {
            query = query.toUpperCase();
            myqDeliveryItemArrayList.clear();

            if (query.isEmpty()) {

                myqDeliveryItemArrayList.addAll(tempArrayList);
            } else {

                ArrayList<MyQDeliveryResult.MYQDeliveryItem> newList = new ArrayList<>();

                for (MyQDeliveryResult.MYQDeliveryItem item : tempArrayList) {

                    if (searchOption.contains("Pickup")) {

                        if (item.getPickupNo().toUpperCase().contains(query)) {

                            newList.add(item);
                        }
                    } else if (searchOption.contains("Order")) {

                        if (item.getOrderNo().toUpperCase().contains(query)) {

                            newList.add(item);
                        }
                    }
                }

                if (newList.size() > 0) {

                    myqDeliveryItemArrayList.addAll(newList);
                }
            }

            notifyDataSetChanged();
        } catch (Exception e) {

            Log.e("krm0219", "filterData  Exception  " + e.toString());
        }
    }
}