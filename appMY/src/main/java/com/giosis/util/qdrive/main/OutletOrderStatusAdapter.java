package com.giosis.util.qdrive.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.library.list.delivery.DeliveryDoneActivity;
import com.giosis.library.message.CustomerMessageListDetailActivity;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.ChildItem;
import com.giosis.util.qdrive.list.RowItem;
import com.giosis.util.qdrive.list.pickup.OutletPickupScanActivity;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.NetworkUtil;

import org.json.JSONObject;

import java.util.ArrayList;


public class OutletOrderStatusAdapter extends BaseExpandableListAdapter {
    String TAG = "OutletOrderStatusAdapter";

    Context context;
    private DatabaseHelper dbHelper = DatabaseHelper.getInstance();

    private ArrayList<RowItem> rowItem;
    private ArrayList<RowItem> originalrowItem;
    private String outletCondition;

    private final String[] delivery_qtalk_message_array = {
            "First Message",
            "Second Message"
    };
    private final String[] pickup_qtalk_message_array = {
            "[Qxpress]\r\nPickup vehicle departed.\r\n-Seller : %s\r\n-Pickup No.:  %s \r\n-Qxpress Driver:  %s",
            "[Qxpress]\r\nThe pickup was failed by the absence of seller.\r\n-Seller : %s\r\n-Pickup No.:  %s \r\n-Qxpress Driver:  %s"
    };


    OutletOrderStatusAdapter(Context context, ArrayList<RowItem> rowItem, String outletCondition) {

        this.context = context;
        this.rowItem = new ArrayList<>();
        this.rowItem.addAll(rowItem);
        this.originalrowItem = new ArrayList<>();
        this.originalrowItem.addAll(rowItem);
        this.outletCondition = outletCondition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final int position = groupPosition;

        if (convertView == null) {

            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.outlet_order_status_item, null);
        }

        LinearLayout layout_list_item_card_view = convertView.findViewById(R.id.layout_list_item_card_view);            // background change

        TextView text_list_item_d_day = convertView.findViewById(R.id.text_list_item_d_day);
        ImageView img_list_item_station_icon = convertView.findViewById(R.id.img_list_item_station_icon);
        TextView text_list_item_tracking_no = convertView.findViewById(R.id.text_list_item_tracking_no);
        TextView text_list_item_outlet_order_type = convertView.findViewById(R.id.text_list_item_outlet_order_type);
        ImageView img_list_item_up_icon = convertView.findViewById(R.id.img_list_item_up_icon);

        TextView text_list_item_store_name = convertView.findViewById(R.id.text_list_item_store_name);
        TextView text_list_item_address = convertView.findViewById(R.id.text_list_item_address);
        final FrameLayout layout_list_item_menu_icon = convertView.findViewById(R.id.layout_list_item_menu_icon);

        TextView text_list_item_name = convertView.findViewById(R.id.text_list_item_name);


        if (isExpanded) {

            layout_list_item_card_view.setBackgroundResource(R.drawable.bg_top_radius_10_ffffff);
            img_list_item_up_icon.setVisibility(View.VISIBLE);
        } else {

            layout_list_item_card_view.setBackgroundResource(R.drawable.bg_radius_10_ffffff_shadows);
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


        final RowItem row_pos = rowItem.get(groupPosition);

        text_list_item_d_day.setText(row_pos.getDelay());
        if (row_pos.getDelay().equals("D+0") || row_pos.getDelay().equals("D+1")) {

            text_list_item_d_day.setTextColor(context.getResources().getColor(R.color.color_303030));
        } else {

            text_list_item_d_day.setTextColor(context.getResources().getColor(R.color.color_ff0000));
        }

        //
        if (row_pos.getRoute().contains("7E")) {

            img_list_item_station_icon.setBackgroundResource(R.drawable.qdrive_btn_icon_seven);
        } else if (row_pos.getRoute().contains("FL")) {

            img_list_item_station_icon.setBackgroundResource(R.drawable.qdrive_btn_icon_locker);
        }

        text_list_item_tracking_no.setText(row_pos.getShipping());
        text_list_item_address.setText(row_pos.getAddress());
        layout_list_item_menu_icon.setTag(row_pos.getShipping());
        text_list_item_name.setText(row_pos.getName());


        if (row_pos.getOutlet_store_name() == null || row_pos.getOutlet_store_name().length() == 0) {

            text_list_item_store_name.setVisibility(View.GONE);
        } else {

            text_list_item_store_name.setVisibility(View.VISIBLE);
            text_list_item_store_name.setText(row_pos.getOutlet_store_name());
        }


        if (row_pos.getType().equals("P")) {

            text_list_item_tracking_no.setTextColor(context.getResources().getColor(R.color.color_363BE7));
            text_list_item_outlet_order_type.setText(R.string.text_retrieve);
        } else {

            text_list_item_tracking_no.setTextColor(context.getResources().getColor(R.color.color_32bd87));
            text_list_item_outlet_order_type.setText(R.string.text_delivery);
        }


        //우측 메뉴 아이콘 클릭 이벤트  Quick Menu
        layout_list_item_menu_icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, layout_list_item_menu_icon);
                popup.getMenuInflater().inflate(R.menu.quickmenu, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.menu_one:
                                Cursor cs = dbHelper.get("SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + layout_list_item_menu_icon.getTag().toString() + "' LIMIT 1");
                                if (cs != null) {
                                    cs.moveToFirst();

                                    // 구글맵 이동
                                    String addr = cs.getString(cs.getColumnIndex("address"));
                                    Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + addr);
                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                    context.startActivity(it);

                                }
                                break;

                            case R.id.menu_up:
                                if (0 < position) {
                                    RowItem upItem = rowItem.remove(position);
                                    rowItem.add(position - 1, upItem);
                                    originalrowItem.clear();
                                    originalrowItem.addAll(rowItem);
                                    notifyDataSetChanged();

                                    for (int i = 0; i < originalrowItem.size(); i++) {
                                        String val = String.valueOf(i);
                                        if (i < 10) {
                                            val = "00" + val;
                                        } else if (i < 100) {
                                            val = "0" + val;
                                        }
                                        ContentValues ContentVal = new ContentValues();
                                        ContentVal.put("seq_orderby", val);

                                        dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, ContentVal,
                                                "invoice_no=? COLLATE NOCASE ", new String[]{originalrowItem.get(i).getShipping()});
                                    }
                                }
                                break;

                            case R.id.menu_down:
                                if (position < rowItem.size() - 1) {
                                    RowItem downItem = rowItem.remove(position);
                                    rowItem.add(position + 1, downItem);
                                    originalrowItem.clear();
                                    originalrowItem.addAll(rowItem);
                                    notifyDataSetChanged();

                                    for (int i = 0; i < originalrowItem.size(); i++) {
                                        String val = String.valueOf(i);
                                        if (i < 10) {
                                            val = "00" + val;
                                        } else if (i < 100) {
                                            val = "0" + val;
                                        }
                                        ContentValues ContentVal = new ContentValues();
                                        ContentVal.put("seq_orderby", val);

                                        dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, ContentVal,
                                                "invoice_no=? COLLATE NOCASE ", new String[]{originalrowItem.get(i).getShipping()});
                                    }
                                }
                                break;
                        }
                        return true;
                    }
                });
            }
        });

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {

            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.outlet_order_status_child_item, null);
        }

        LinearLayout layout_list_item_child_telephone = convertView.findViewById(R.id.layout_list_item_child_telephone);
        TextView text_list_item_child_telephone_number = convertView.findViewById(R.id.text_list_item_child_telephone_number);
        LinearLayout layout_list_item_child_mobile = convertView.findViewById(R.id.layout_list_item_child_mobile);
        TextView text_list_item_child_mobile_number = convertView.findViewById(R.id.text_list_item_child_mobile_number);

        ImageView img_list_item_child_sms = convertView.findViewById(R.id.img_list_item_child_sms);
        ImageView img_list_item_child_live10 = convertView.findViewById(R.id.img_list_item_child_live10);
        ImageView img_list_item_child_qpost = convertView.findViewById(R.id.img_list_item_child_qpost);
        ImageView img_list_item_child_driver_memo = convertView.findViewById(R.id.img_list_item_child_driver_memo);

        RelativeLayout layout_list_item_child_buttons = convertView.findViewById(R.id.layout_list_item_child_buttons);
        Button btn_list_item_child = convertView.findViewById(R.id.btn_list_item_child);

        //
        final RowItem group_item = rowItem.get(groupPosition);
        final ChildItem child = (ChildItem) getChild(groupPosition, childPosition);

        final String tracking_no = rowItem.get(groupPosition).getShipping();
        final String name = rowItem.get(groupPosition).getName();
        final String sender = rowItem.get(groupPosition).getSender();
        final String route = rowItem.get(groupPosition).getRoute();
        final String qty = rowItem.get(groupPosition).getQty();
        final String type = rowItem.get(groupPosition).getType();


        if (child.getSecretNoType().equals("T")) {   // Qtalk 안심번호 타입 T - Qnumber 사용

            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.GONE);
            //img_list_item_child_live10.setVisibility(View.VISIBLE);
        } else if (child.getSecretNoType().equals("P")) { // Phone 안심번호 - 핸드폰만 활성화

            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.VISIBLE);
            img_list_item_child_live10.setVisibility(View.GONE);

            SpannableString content = new SpannableString(child.getHp());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            text_list_item_child_mobile_number.setText(content);
        } else {          //안심번호 사용안함

            if (child.getTel() != null && child.getTel().length() > 5) {

                layout_list_item_child_telephone.setVisibility(View.VISIBLE);

                SpannableString content = new SpannableString(child.getTel());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                text_list_item_child_telephone_number.setText(content);
            } else {
                layout_list_item_child_telephone.setVisibility(View.GONE);
            }

            if (child.getHp() != null && child.getHp().length() > 5) {

                layout_list_item_child_mobile.setVisibility(View.VISIBLE);

                SpannableString content = new SpannableString(child.getHp());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                text_list_item_child_mobile_number.setText(content);
            } else {
                layout_list_item_child_mobile.setVisibility(View.GONE);
            }

            img_list_item_child_live10.setVisibility(View.GONE);
        }

        try {

            img_list_item_child_qpost.setVisibility(View.GONE);
        } catch (Exception e) {

            img_list_item_child_qpost.setVisibility(View.GONE);
        }

        // 2019.04
        if (outletCondition.equals(context.getResources().getString(R.string.text_outlet_status_2))) {

            layout_list_item_child_buttons.setVisibility(View.VISIBLE);
        } else {

            layout_list_item_child_buttons.setVisibility(View.GONE);
        }

        //
        if (rowItem.get(groupPosition).getType().equals("D")) {

            btn_list_item_child.setText(R.string.button_delivered);
        } else {            // Pickup

            btn_list_item_child.setText(R.string.button_pickup_done);
        }

        text_list_item_child_telephone_number.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Uri callUri = Uri.parse("tel:" + child.getTel());
                Intent intent = new Intent(Intent.ACTION_DIAL, callUri);
                context.startActivity(intent);
            }
        });


        text_list_item_child_mobile_number.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Uri callUri = Uri.parse("tel:" + child.getHp());
                Intent intent = new Intent(Intent.ACTION_DIAL, callUri);
                context.startActivity(intent);
            }
        });

        img_list_item_child_sms.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                try {

                    String smsBody = "Dear " + name + ", Your parcels has been started to delivery by Qxpress. Thank you.";
                    Uri smsUri = Uri.parse("sms:" + child.getHp());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
                    intent.putExtra("sms_body", smsBody);
                    context.startActivity(intent);
                } catch (Exception e) {

                    Toast.makeText(context, "SMS Send Error..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        img_list_item_child_live10.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                final String p_qlps_cust_no = group_item.getCustNo();
                final String p_delivery_type = group_item.getType();
                final String p_order_type = group_item.getRoute();
                final String p_tracking_no = group_item.getShipping();
                final String p_seller_id = group_item.getPartnerID();

                DialogSelectOption(p_qlps_cust_no, p_delivery_type, p_order_type, p_tracking_no, p_seller_id);
            }
        });


        img_list_item_child_qpost.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, CustomerMessageListDetailActivity.class);
                intent.putExtra("tracking_no", tracking_no);
                context.startActivity(intent);
            }
        });


        img_list_item_child_driver_memo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                String msg = group_item.getSelfMemo();
                String shipping = group_item.getShipping();
                alert.setTitle("Driver Memo");
                alert.setMessage(shipping);

                final EditText input = new EditText(context);
                input.setText(msg);
                input.setTextColor(Color.BLACK);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = input.getText().toString();

                        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

                        ContentValues contentVal = new ContentValues();
                        contentVal.put("self_memo", value);

                        dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                                "invoice_no= ? COLLATE NOCASE ", new String[]{tracking_no});

                        group_item.setSelfMemo(value);
                        notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });

                alert.show();
            }
        });


        btn_list_item_child.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (type.equals("D")) {

                    Intent intent = new Intent(context, DeliveryDoneActivity.class);

                    intent.putExtra("title", "Signature");
                    intent.putExtra("type", "D");
                    intent.putExtra("waybillNo", tracking_no);
                    ((Activity) context).startActivityForResult(intent, 1);

                } else if (type.equals("P")) {

                    Intent intent = new Intent(context, OutletPickupScanActivity.class);

                    intent.putExtra("title", "Qsuttle : Pickup Done");
                    intent.putExtra("pickup_no", tracking_no);
                    intent.putExtra("applicant", name);
                    intent.putExtra("qty", qty);
                    intent.putExtra("route", route);
                    ((Activity) context).startActivityForResult(intent, 100);
                }
            }
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

            rowItem.addAll(originalrowItem);
        } else {

            ArrayList<RowItem> newList = new ArrayList<>();
            for (RowItem rowitem : originalrowItem) {

                if (rowitem.getName().toUpperCase().contains(query) || rowitem.getShipping().toUpperCase().contains(query)) {
                    newList.add(rowitem);
                }
            }
            if (0 < newList.size()) {

                rowItem.addAll(newList);
            }
        }

        notifyDataSetChanged();
    }


    public class SendLive10MessageTask extends AsyncTask<String, Integer, StdResult> {
        @Override
        protected StdResult doInBackground(String... params) {

            return SendLive10Message(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
        }

        @Override
        protected void onPostExecute(StdResult result) {

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            if (resultCode != 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("alert");
                builder.setMessage(resultMsg);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    // Qtalk 메시지 선택 창
    private void DialogSelectOption(String qlps_cust_no, String delivery_type, String order_type, String tracking_no, String seller_id) {

        final String _qlps_cust_no = qlps_cust_no;
        final String _delivery_type = delivery_type;
        final String _order_type = order_type;
        final String _tracking_no = tracking_no;
        final String _svc_nation_cd = "SG";
        final String _qsign_id = MyApplication.preferences.getUserId();
        final String _qsign_name = MyApplication.preferences.getUserName();
        final String _seller_id = seller_id;

        final String Pickup_items[] = {
                "Pickup vehicle departed",
                "Pickup Failed by Absence"
        };
        final String Delivery_items[] = {
                "Out for delivery",
                "Delivery Failed by Absence"
        };
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle("QTalk Auto Message");
        ab.setSingleChoiceItems(delivery_type.equals("P") ? Pickup_items : Delivery_items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        ListView lv = ((AlertDialog) dialog).getListView();
                        lv.setTag(new Integer(whichButton));
                        //Toast.makeText(context, String.valueOf(whichButton)  , Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //
                        ListView lv = ((AlertDialog) dialog).getListView();
                        Integer selected = (Integer) lv.getTag();
                        if (selected != null) {
                            String msg = _delivery_type.equals("P") ? pickup_qtalk_message_array[selected] : delivery_qtalk_message_array[selected];
                            msg = String.format(msg, _seller_id, _tracking_no, _qsign_name);
                            String[] qtalk_params = {_qlps_cust_no, _delivery_type, _order_type, _tracking_no, _svc_nation_cd, msg, _qsign_id};

                            SendLive10MessageTask sendLive10MessageTask = new SendLive10MessageTask();
                            sendLive10MessageTask.execute(qtalk_params);
                        }
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel 버튼 클릭시
                    }
                });
        ab.show();
    }


    private StdResult SendLive10Message(String qlps_cust_no, String delivery_type, String order_type, String tracking_no,
                                        String svc_nation_cd, String msg, String qsign_id) {

        StdResult result = new StdResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("qlps_cust_no", qlps_cust_no);
            job.accumulate("delivery_type", delivery_type);
            job.accumulate("order_type", order_type);
            job.accumulate("tracking_no", tracking_no);
            job.accumulate("svc_nation_cd", svc_nation_cd);
            job.accumulate("msg", msg);
            job.accumulate("qsign_id", qsign_id);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);


            String methodName = "SetSendQtalkMessagebyQsign";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultCode":-99,"ResultMsg":"Cannot send a content-body with this verb-type."}

            JSONObject jsonObject = new JSONObject(jsonString);
            result.setResultCode(jsonObject.getInt("ResultCode"));
            result.setResultMsg(jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            Log.e("Exception", TAG + "  SetSendQtalkMessagebyQsign Exception : " + e.toString());

            String msg1 = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg1);
        }

        return result;
    }
}