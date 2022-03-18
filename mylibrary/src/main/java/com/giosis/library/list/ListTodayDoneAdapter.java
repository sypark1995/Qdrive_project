package com.giosis.library.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.giosis.library.R;
import com.giosis.library.bluetooth.BluetoothListener;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.NetworkUtil;

import java.util.ArrayList;


public class ListTodayDoneAdapter extends BaseExpandableListAdapter {
    String TAG = "CustomTodayDoneExpandableAdapter";

    private ArrayList<RowItem> rowItem;
    private ArrayList<RowItem> originalRowItem;
    BluetoothListener bluetoothListener;

    ListTodayDoneAdapter(ArrayList<RowItem> rowItem, BluetoothListener bluetoothListener) {
        this.rowItem = new ArrayList<>();
        this.rowItem.addAll(rowItem);
        this.originalRowItem = new ArrayList<>();
        this.originalRowItem.addAll(rowItem);

        this.bluetoothListener = bluetoothListener;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_group_item, null);
        }

        LinearLayout layout_list_item_card_view = convertView.findViewById(R.id.layout_list_item_card_view);                // background change
        ImageView img_list_item_up_icon = convertView.findViewById(R.id.img_list_item_up_icon);

        if (isExpanded) {
            layout_list_item_card_view.setBackgroundResource(R.drawable.bg_top_round_10_ffffff);
            img_list_item_up_icon.setVisibility(View.VISIBLE);
        } else {
            layout_list_item_card_view.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow);
            img_list_item_up_icon.setVisibility(View.GONE);
        }

        if (groupPosition == 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            layout_list_item_card_view.setLayoutParams(lp);

        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 20, 0, 0);
            layout_list_item_card_view.setLayoutParams(lp);
        }

        TextView text_list_item_d_day = convertView.findViewById(R.id.text_list_item_d_day);
        ImageView img_list_item_secure_delivery = convertView.findViewById(R.id.img_list_item_secure_delivery);
        ImageView img_list_item_station_icon = convertView.findViewById(R.id.img_list_item_station_icon);
        TextView text_list_item_tracking_no = convertView.findViewById(R.id.text_list_item_tracking_no);
        TextView text_list_item_pickup_state = convertView.findViewById(R.id.text_list_item_pickup_state);

        TextView text_list_item_address = convertView.findViewById(R.id.text_list_item_address);
        final FrameLayout layout_list_item_menu_icon = convertView.findViewById(R.id.layout_list_item_menu_icon);

        TextView text_list_item_receipt_name = convertView.findViewById(R.id.text_list_item_receipt_name);
        LinearLayout layout_list_item_delivery_outlet_info = convertView.findViewById(R.id.layout_list_item_delivery_outlet_info);
        TextView text_list_item_desired_date_title = convertView.findViewById(R.id.text_list_item_desired_date_title);
        TextView text_list_item_desired_date = convertView.findViewById(R.id.text_list_item_desired_date);
        TextView text_list_item_qty_title = convertView.findViewById(R.id.text_list_item_qty_title);
        TextView text_list_item_qty = convertView.findViewById(R.id.text_list_item_qty);

        LinearLayout layout_list_item_request = convertView.findViewById(R.id.layout_list_item_request);
        LinearLayout layout_list_item_driver_memo = convertView.findViewById(R.id.layout_list_item_driver_memo);
        TextView text_list_item_driver_memo = convertView.findViewById(R.id.text_list_item_driver_memo);


        final int position = groupPosition;
        final RowItem row_pos = rowItem.get(groupPosition);

        text_list_item_d_day.setText(row_pos.getDelay());
        if (row_pos.getDelay().equals("D+0") || row_pos.getDelay().equals("D+1")) {
            text_list_item_d_day.setTextColor(convertView.getContext().getResources().getColor(R.color.color_303030));
        } else {
            text_list_item_d_day.setTextColor(convertView.getContext().getResources().getColor(R.color.color_ff0000));
        }

        //
        img_list_item_secure_delivery.setVisibility(View.GONE);
        img_list_item_station_icon.setVisibility(View.GONE);

        if (row_pos.getRoute().contains("7E")) {
            img_list_item_station_icon.setVisibility(View.VISIBLE);
        } else {
            img_list_item_station_icon.setVisibility(View.GONE);
        }

        //드라이버 셀프 메모
        if (row_pos.getSelfMemo() == null || row_pos.getSelfMemo().length() == 0) {
            layout_list_item_driver_memo.setVisibility(View.GONE);
        } else {
            layout_list_item_driver_memo.setVisibility(View.VISIBLE);
            text_list_item_driver_memo.setText(row_pos.getSelfMemo());
        }

        //픽업
        if (row_pos.getType().equals("P")) {

            text_list_item_tracking_no.setTextColor(convertView.getContext().getResources().getColor(R.color.color_363BE7));

            if (row_pos.getStat().equals("RE")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(convertView.getContext().getResources().getString(R.string.text_pickup_reassigned));
            } else if (row_pos.getStat().equals("PF")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(convertView.getContext().getResources().getString(R.string.text_pickup_failed));
            } else {

                text_list_item_pickup_state.setVisibility(View.GONE);
            }
        }

        text_list_item_tracking_no.setText(row_pos.getShipping());
        text_list_item_address.setText(row_pos.getAddress());
        layout_list_item_menu_icon.setTag(row_pos.getShipping());
        text_list_item_receipt_name.setText(row_pos.getName());
        layout_list_item_delivery_outlet_info.setVisibility(View.GONE);

        text_list_item_desired_date_title.setText(convertView.getContext().getResources().getString(R.string.text_scanned_qty));
        text_list_item_desired_date.setText(row_pos.getQty());
        text_list_item_qty_title.setVisibility(View.GONE);
        text_list_item_qty.setVisibility(View.GONE);
        layout_list_item_request.setVisibility(View.GONE);

        //우측 메뉴 아이콘 클릭 이벤트  Quick Menu
        layout_list_item_menu_icon.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(v.getContext(), layout_list_item_menu_icon);
            popup.getMenuInflater().inflate(R.menu.quickmenu_pickup, popup.getMenu());
            popup.show();

            popup.setOnMenuItemClickListener(item -> {

                int itemId = item.getItemId();
                if (itemId == R.id.menu_one) {
                    String map_addr = row_pos.getAddress();

                    int split_index = map_addr.indexOf(")");
                    String split_addr = map_addr.substring(split_index + 1);

                    if (!split_addr.equals("")) {

                        Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + split_addr.trim());
                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                        v.getContext().startActivity(it);
                    }
                } else if (itemId == R.id.menu_up) {
                    if (position > 0) {
                        RowItem upItem = rowItem.remove(position);
                        rowItem.add(position - 1, upItem);
                        originalRowItem.clear();
                        originalRowItem.addAll(rowItem);
                        notifyDataSetChanged();
                    }
                } else if (itemId == R.id.menu_down) {
                    if (position < rowItem.size() - 1) {
                        RowItem downItem = rowItem.remove(position);
                        rowItem.add(position + 1, downItem);
                        originalRowItem.clear();
                        originalRowItem.addAll(rowItem);
                        notifyDataSetChanged();
                    }
                }
                return true;
            });
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_today_done_child, null);
        }

        LinearLayout layout_list_item_child_done_pickup = convertView.findViewById(R.id.layout_list_item_child_done_pickup);
        Button btn_list_item_child_done_add_scan = convertView.findViewById(R.id.btn_list_item_child_done_add_scan);
        Button btn_list_item_child_done_take_back = convertView.findViewById(R.id.btn_list_item_child_done_take_back);
        Button btn_list_item_child_done_print_label = convertView.findViewById(R.id.btn_list_item_child_done_print_label);


        final String tracking_no = rowItem.get(groupPosition).getShipping();
        final String route = rowItem.get(groupPosition).getRoute();
        final String scanned_qty = rowItem.get(groupPosition).getQty();
        final String applicant = rowItem.get(groupPosition).getName();

        boolean isAbleScanAddPage = true;

        if (!NetworkUtil.isNetworkAvailable(convertView.getContext())) {
            isAbleScanAddPage = false;
            AlertShow(convertView.getContext(), convertView.getContext().getResources().getString(R.string.msg_network_connect_error));
        }

        if ((route.equals("RPC") || route.equals("C2C")) && isAbleScanAddPage) {

            layout_list_item_child_done_pickup.setVisibility(View.GONE);
            btn_list_item_child_done_print_label.setVisibility(View.VISIBLE);

        } else {

            layout_list_item_child_done_pickup.setVisibility(View.VISIBLE);
            btn_list_item_child_done_print_label.setVisibility(View.GONE);

            Log.e(TAG, "Scanned Qty :  " + scanned_qty);
            if (scanned_qty.equals("0")) {
                btn_list_item_child_done_take_back.setVisibility(View.GONE);
            } else {
                btn_list_item_child_done_take_back.setVisibility(View.VISIBLE);
            }
        }


        btn_list_item_child_done_add_scan.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), TodayDonePickupScanListActivity.class);
            intent.putExtra("pickup_no", tracking_no);
            intent.putExtra("applicant", applicant);
            intent.putExtra("button_type", "Add Scan");
            ((Activity) v.getContext()).startActivityForResult(intent, ListTodayDoneFragment.REQUEST_ADD_SCAN);
        });

        btn_list_item_child_done_print_label.setOnClickListener(v -> {

            DataUtil.logEvent("button_click", "ListActivity", "Print_CNR");
            bluetoothListener.isConnectPortablePrint(tracking_no);

        });

        // 2019.02 - Take Back
        btn_list_item_child_done_take_back.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), TodayDonePickupScanListActivity.class);
            intent.putExtra("pickup_no", tracking_no);
            intent.putExtra("applicant", applicant);
            intent.putExtra("button_type", "Take Back");
            ((Activity) v.getContext()).startActivityForResult(intent, ListTodayDoneFragment.REQUEST_TAKE_BACK);
        });

        return convertView;
    }


    @Override
    public int getGroupCount() {
        return rowItem.size();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ChildItem> chList = rowItem.get(groupPosition).getItems();
        return chList.size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return rowItem.get(groupPosition);
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ChildItem> chList = rowItem.get(groupPosition).getItems();
        return chList.get(childPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    //Search
    public void filterData(String query) {

        query = query.toUpperCase();
        rowItem.clear();

        if (query.isEmpty()) {
            rowItem.addAll(originalRowItem);
        } else {
            ArrayList<RowItem> newList = new ArrayList<>();
            for (RowItem rowitem : originalRowItem) {
                //이름 or 송장번호 조회
                if (rowitem.getName().toUpperCase().contains(query) || rowitem.getShipping().toUpperCase().contains(query)) {
                    newList.add(rowitem);
                }
            }
            if (newList.size() > 0) {
                rowItem.addAll(newList);
            }
        }

        notifyDataSetChanged();
    }

    void setSorting(ArrayList<RowItem> sortedItems) {
        rowItem.clear();
        rowItem.addAll(sortedItems);
        originalRowItem.clear();
        originalRowItem.addAll(sortedItems);
        notifyDataSetChanged();
    }

    private void AlertShow(Context context, String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(context);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                (dialog, which) -> {
                    dialog.dismiss(); // 닫기

                });
        alert_internet_status.show();
    }

}