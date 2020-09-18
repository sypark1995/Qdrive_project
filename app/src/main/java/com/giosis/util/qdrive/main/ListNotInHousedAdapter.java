package com.giosis.util.qdrive.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author krm0219
 * NavigationBar > LIST > Not-IN Parcels
 **/
public class ListNotInHousedAdapter extends BaseExpandableListAdapter {
    String TAG = "ListNotInHousedAdapter";

    Context context;
    NotInHousedResult result;


    public ListNotInHousedAdapter(Context context, NotInHousedResult result) {

        this.context = context;
        this.result = result;
    }

    @Override
    public Object getGroup(int i) {

        if (result.getResultObject() == null) {
            return 0;
        }

        return result.getResultObject().get(i);
    }

    @Override
    public int getGroupCount() {
        return result.getResultObject().size();
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public View getGroupView(int group_position, boolean isExpanded, View convertView, ViewGroup viewGroup) {

        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_not_in_housed, null);
        } else {
            view = convertView;
        }

        LinearLayout layout_not_in_parcels_item_card_view = view.findViewById(R.id.layout_not_in_parcels_item_card_view);
        TextView text_not_in_parcels_item_pickup_no = view.findViewById(R.id.text_not_in_parcels_item_pickup_no);
        TextView text_not_in_parcels_item_seller_name = view.findViewById(R.id.text_not_in_parcels_item_seller_name);
        ImageView img_not_in_parcels_item_up_icon = view.findViewById(R.id.img_not_in_parcels_item_up_icon);
        TextView text_not_in_parcels_item_address = view.findViewById(R.id.text_not_in_parcels_item_address);
        final FrameLayout layout_not_in_parcels_item_menu_icon = view.findViewById(R.id.layout_not_in_parcels_item_menu_icon);
        TextView text_not_in_parcels_item_desired_date = view.findViewById(R.id.text_not_in_parcels_item_desired_date);
        TextView text_not_in_parcels_item_qty = view.findViewById(R.id.text_not_in_parcels_item_qty);
        TextView text_not_in_parcels_item_not_processed_qty = view.findViewById(R.id.text_not_in_parcels_item_not_processed_qty);


        final NotInHousedResult.NotInHousedList item = result.getResultObject().get(group_position);
        final int position = group_position;

        if (isExpanded && result.getResultObject().get(group_position).getSubLists() != null) {

            layout_not_in_parcels_item_card_view.setBackgroundResource(R.drawable.custom_background_card_view_top);
            img_not_in_parcels_item_up_icon.setVisibility(View.VISIBLE);
        } else {

            layout_not_in_parcels_item_card_view.setBackgroundResource(R.drawable.custom_background_card_view_shadow);
            img_not_in_parcels_item_up_icon.setVisibility(View.GONE);
        }

        text_not_in_parcels_item_pickup_no.setText(item.getInvoiceNo());
        text_not_in_parcels_item_seller_name.setText(item.getReqName());
        text_not_in_parcels_item_address.setText("(" + item.getZipCode() + ") " + item.getAddress());
        text_not_in_parcels_item_qty.setText(item.getReal_qty());

        if(item.getSubLists().size() == 0) {

            text_not_in_parcels_item_not_processed_qty.setText("0");
        } else {

            text_not_in_parcels_item_not_processed_qty.setText(item.getNot_processed_qty());
        }


        try {

            Date date = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.ENGLISH).parse(item.getPickup_date());      //"Jul 24 2018  4:01PM"
            String pickup_date = new SimpleDateFormat("yyyy-MM-dd").format(date);
            text_not_in_parcels_item_desired_date.setText(pickup_date);
        } catch (ParseException e) {

            Log.e("krm0219", TAG + " Exception : " + e.toString());
            text_not_in_parcels_item_desired_date.setText("Error");
            e.printStackTrace();
        }


        layout_not_in_parcels_item_menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(context, layout_not_in_parcels_item_menu_icon);
                popupMenu.getMenuInflater().inflate(R.menu.quickmenu_pickup, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.menu_one: {

                                String map_addr = item.getAddress().trim();

                                if (map_addr != null && !map_addr.equals("")) {

                                    // 구글맵 이동
                                    Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + map_addr);
                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                    context.startActivity(it);
                                }
                            }
                            break;

                            case R.id.menu_up: {

                                if (position > 0) {

                                    NotInHousedResult.NotInHousedList upItem = result.getResultObject().remove(position);
                                    result.getResultObject().add(position - 1, upItem);
                                    notifyDataSetChanged();
                                }
                            }
                            break;

                            case R.id.menu_down: {

                                if (position < result.getResultObject().size() - 1) {

                                    NotInHousedResult.NotInHousedList downItem = result.getResultObject().remove(position);
                                    result.getResultObject().add(position + 1, downItem);
                                    notifyDataSetChanged();
                                }
                            }
                            break;
                        }

                        return false;
                    }
                });
            }
        });

        return view;
    }

    @Override
    public int getChildrenCount(int i) {

        if (result.getResultObject().get(i).getSubLists() == null) {
            return 0;
        }

        return result.getResultObject().get(i).getSubLists().size();
    }


    @Override
    public Object getChild(int i, int i1) {
        return result.getResultObject().get(i).getSubLists().get(i1);
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getChildView(int group_position, int child_position, boolean b, View convertView, ViewGroup viewGroup) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_not_in_housed_child, null);
        } else {

            view = convertView;
        }


        LinearLayout layout_not_in_parcels_child_view = view.findViewById(R.id.layout_not_in_parcels_child_view);
        TextView text_not_in_parcels_child_scanned_no = view.findViewById(R.id.text_not_in_parcels_child_scanned_no);
        TextView text_not_in_parcels_child_amount = view.findViewById(R.id.text_not_in_parcels_child_amount);
        TextView text_not_in_parcels_child_currency = view.findViewById(R.id.text_not_in_parcels_child_currency);


        NotInHousedResult.NotInHousedList.NotInHousedSubList subitem = result.getResultObject().get(group_position).getSubLists().get(child_position);
        int sub_size = result.getResultObject().get(group_position).getSubLists().size();
        Log.e("krm0219", result.getResultObject().get(group_position).getSubLists().size() + "  " + child_position);

        if (sub_size - 1 == child_position) {

           /* LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 40);
            layout_not_in_parcels_child_view.setLayoutParams(lp);*/
            layout_not_in_parcels_child_view.setPadding(0, 0, 0, 40);
            layout_not_in_parcels_child_view.setBackgroundResource(R.drawable.custom_background_card_view_bottom);
        } else {

            layout_not_in_parcels_child_view.setPadding(0, 0, 0, 0);
            layout_not_in_parcels_child_view.setBackgroundColor(context.getResources().getColor(R.color.white));
        }


        text_not_in_parcels_child_scanned_no.setText(subitem.getPackingNo());
        text_not_in_parcels_child_amount.setText(subitem.getPurchasedAmount());
        text_not_in_parcels_child_currency.setText("(" + subitem.getPurchaseCurrency() + ")");

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
