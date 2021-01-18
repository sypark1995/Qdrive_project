package com.giosis.util.qdrive.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.giosis.library.list.delivery.DeliveryFailedActivity;
import com.giosis.library.message.CustomerMessageListDetailActivity;
import com.giosis.library.server.data.FailedCodeResult;
import com.giosis.library.setting.bluetooth.BluetoothDeviceData;
import com.giosis.library.setting.bluetooth.PrinterSettingActivity;
import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.delivery.DeliveryReturnFailedActivity;
import com.giosis.util.qdrive.list.delivery.DeliveryReturnedActivity;
import com.giosis.util.qdrive.list.pickup.ManualCnRPrintDataHelper;
import com.giosis.util.qdrive.list.pickup.OutletPickupScanActivity;
import com.giosis.util.qdrive.list.pickup.PickupFailedActivity;
import com.giosis.util.qdrive.list.pickup.PickupZeroQtyActivity;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterBroadcastReceiver;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterData;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterHandler;
import com.giosis.util.qdrive.portableprinter.bluetooth.PrinterConnManager;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @author jtpark_eurasia
 * List 클릭 시 확장
 * <p></p>
 * @editor krm0219
 */
public class InProgressExpandableListAdapter extends BaseExpandableListAdapter implements GPrinterHandler.OnGPrinterReadyListener {
    String TAG = "CustomExpandableAdapter";

    Context context;
    private DatabaseHelper dbHelper = DatabaseHelper.getInstance();


    private OnMoveUpListener onMoveUpListener;

    private ArrayList<RowItem> rowItem;
    private ArrayList<RowItem> originalRowItem;

    public InProgressExpandableListAdapter(Context context, ArrayList<RowItem> rowItem) {

        this.context = context;             // getActivity()
        this.rowItem = new ArrayList<>();
        this.rowItem.addAll(rowItem);
        this.originalRowItem = new ArrayList<>();
        this.originalRowItem.addAll(rowItem);
    }

    // 인터페이스
    public interface OnMoveUpListener {
        public void onMoveUp(int pos);
    }

    public void setOnMoveUpListener(OnMoveUpListener listener) {
        this.onMoveUpListener = listener;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final int position = groupPosition;

        if (convertView == null) {

            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.activity_item, null);
        }

        LinearLayout layout_list_item_card_view = convertView.findViewById(R.id.layout_list_item_card_view);            // background change

        TextView text_list_item_d_day = convertView.findViewById(R.id.text_list_item_d_day);
        ImageView img_list_item_secure_delivery = convertView.findViewById(R.id.img_list_item_secure_delivery);
        ImageView img_list_item_station_icon = convertView.findViewById(R.id.img_list_item_station_icon);
        TextView text_list_item_tracking_no = convertView.findViewById(R.id.text_list_item_tracking_no);
        TextView text_list_item_pickup_state = convertView.findViewById(R.id.text_list_item_pickup_state);
        ImageView img_list_item_up_icon = convertView.findViewById(R.id.img_list_item_up_icon);

        TextView text_list_item_address = convertView.findViewById(R.id.text_list_item_address);
        final FrameLayout layout_list_item_menu_icon = convertView.findViewById(R.id.layout_list_item_menu_icon);

        LinearLayout layout_list_item_delivery_info = convertView.findViewById(R.id.layout_list_item_delivery_info);
        TextView text_list_item_receipt_name = convertView.findViewById(R.id.text_list_item_receipt_name);
        LinearLayout layout_list_item_delivery_outlet_info = convertView.findViewById(R.id.layout_list_item_delivery_outlet_info);
        TextView text_list_item_parcel_qty = convertView.findViewById(R.id.text_list_item_parcel_qty);
        RelativeLayout layout_list_item_pickup_info = convertView.findViewById(R.id.layout_list_item_pickup_info);
        TextView text_list_item_desired_date = convertView.findViewById(R.id.text_list_item_desired_date);
        TextView text_list_item_qty = convertView.findViewById(R.id.text_list_item_qty);

        LinearLayout layout_list_item_request = convertView.findViewById(R.id.layout_list_item_request);
        TextView text_list_item_request = convertView.findViewById(R.id.text_list_item_request);

        LinearLayout layout_list_item_driver_memo = convertView.findViewById(R.id.layout_list_item_driver_memo);
        TextView text_list_item_driver_memo = convertView.findViewById(R.id.text_list_item_driver_memo);


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
        Log.e("krm0219", TAG + "  Route : " + row_pos.getRoute() + " / Stat : " + row_pos.getStat() + " / Number : " + row_pos.getShipping());

        text_list_item_d_day.setText(row_pos.getDelay());
        if (row_pos.getDelay().equals("D+0") || row_pos.getDelay().equals("D+1")) {

            text_list_item_d_day.setTextColor(context.getResources().getColor(R.color.color_303030));
        } else {

            text_list_item_d_day.setTextColor(context.getResources().getColor(R.color.color_ff0000));
        }

        //
        if (row_pos.getOutlet_company().equals("7E")) {

            img_list_item_station_icon.setVisibility(View.VISIBLE);
            img_list_item_station_icon.setBackgroundResource(R.drawable.qdrive_btn_icon_seven);
        } else if (row_pos.getOutlet_company().equals("FL")) {

            img_list_item_station_icon.setVisibility(View.VISIBLE);
            img_list_item_station_icon.setBackgroundResource(R.drawable.qdrive_btn_icon_locker);
        } else {

            img_list_item_station_icon.setVisibility(View.GONE);
        }


        //NOTIFICATION.  19/10 - 참조 픽업번호가 있으면 해당 번호로 표시, 없으면 기존 픽업번호 (Ref. Pickup No)
        if (row_pos.getType().equals("P")) {        // Pickup
            if (!row_pos.getRef_pickup_no().equals("")) {

                text_list_item_tracking_no.setText(row_pos.getRef_pickup_no());
            } else {

                text_list_item_tracking_no.setText(row_pos.getShipping());
            }
        } else {        // Delivery

            text_list_item_tracking_no.setText(row_pos.getShipping());
        }

        //    text_list_item_tracking_no.setText(row_pos.getShipping());
        text_list_item_address.setText(row_pos.getAddress());
        layout_list_item_menu_icon.setTag(row_pos.getShipping());
        text_list_item_receipt_name.setText(row_pos.getName());

        //픽업
        if (row_pos.getType().equals("P")) {

            text_list_item_tracking_no.setTextColor(context.getResources().getColor(R.color.color_363BE7));
            img_list_item_secure_delivery.setVisibility(View.GONE);

            if (row_pos.getStat().equals("RE")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_pickup_reassigned));
            } else if (row_pos.getStat().equals("PF")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_pickup_failed));
            } else {

                text_list_item_pickup_state.setVisibility(View.GONE);
            }

            if (row_pos.getOutlet_store_name() != null) {

                // 2019.04
                if (row_pos.getOutlet_company().equals("7E")) {

                    text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name() + " (" + row_pos.getOutlet_store_code() + ")");
                } else {

                    text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name());
                }
            }

            layout_list_item_delivery_info.setVisibility(View.GONE);
            layout_list_item_delivery_outlet_info.setVisibility(View.GONE);
            layout_list_item_pickup_info.setVisibility(View.VISIBLE);
            text_list_item_desired_date.setText(row_pos.getDesiredDate());
            text_list_item_qty.setText(row_pos.getQty());

            if (row_pos.getRoute().equalsIgnoreCase("RPC")) {

                text_list_item_desired_date.setText(row_pos.getDesiredDate() + " / " + row_pos.getDesired_time());
            }
        } else {       //배송

            text_list_item_tracking_no.setTextColor(context.getResources().getColor(R.color.color_32bd87));

            if (row_pos.getSecure_delivery_yn() != null && row_pos.getSecure_delivery_yn().equals("Y")) {

                img_list_item_secure_delivery.setVisibility(View.VISIBLE);
            } else {

                img_list_item_secure_delivery.setVisibility(View.GONE);
            }

            if (row_pos.getStat().equals("DX")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_failed));
            } else {

                text_list_item_pickup_state.setVisibility(View.GONE);
            }


            layout_list_item_delivery_info.setVisibility(View.VISIBLE);
            layout_list_item_pickup_info.setVisibility(View.GONE);

            if (row_pos.getOutlet_store_name() != null) {

                //text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name());
                layout_list_item_delivery_outlet_info.setVisibility(View.VISIBLE);
                text_list_item_parcel_qty.setText(String.format("%d", row_pos.getOutlet_qty()));

                // 2019.04
                if (row_pos.getOutlet_company().equals("7E")) {

                    text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name() + " (" + row_pos.getOutlet_store_code() + ")");
                } else {

                    text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name());
                }
                layout_list_item_delivery_info.setVisibility(View.GONE);
            } else {

                layout_list_item_delivery_outlet_info.setVisibility(View.GONE);
            }
        }

        //요청
        if (row_pos.getRequest() == null || row_pos.getRequest().length() == 0) {

            layout_list_item_request.setVisibility(View.GONE);
        } else {

            layout_list_item_request.setVisibility(View.VISIBLE);
            text_list_item_request.setText(row_pos.getRequest());
        }

        //드라이버 셀프 메모
        if (row_pos.getSelfMemo() == null || row_pos.getSelfMemo().length() == 0) {

            layout_list_item_driver_memo.setVisibility(View.GONE);
        } else {

            layout_list_item_driver_memo.setVisibility(View.VISIBLE);
            text_list_item_driver_memo.setText(row_pos.getSelfMemo());
        }

        //  2019.04 Outlet
        if (row_pos.getOutlet_company().equals("7E") || row_pos.getOutlet_company().equals("FL")) {
            if (row_pos.getType().equals("P")) {

                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_retrieve));
            } else if (row_pos.getType().equals("D")) {

                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_delivery));
            }

            text_list_item_pickup_state.setVisibility(View.VISIBLE);
            layout_list_item_request.setVisibility(View.GONE);
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

                        //
                        switch (item.getItemId()) {
                            case R.id.menu_one:

                                Cursor cs = dbHelper.get("SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " where invoice_no='" + layout_list_item_menu_icon.getTag().toString() + "' LIMIT 1");
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
                                    originalRowItem.clear();
                                    originalRowItem.addAll(rowItem);
                                    notifyDataSetChanged();

                                    for (int i = 0; i < originalRowItem.size(); i++) {
                                        String val = String.valueOf(i);
                                        if (i < 10) {
                                            val = "00" + val;
                                        } else if (i < 100) {
                                            val = "0" + val;
                                        }
                                        ContentValues ContentVal = new ContentValues();
                                        ContentVal.put("seq_orderby", val);

                                        dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, ContentVal,
                                                "invoice_no=? COLLATE NOCASE ", new String[]{originalRowItem.get(i).getShipping()});
                                    }

                                    if (onMoveUpListener != null) {
                                        onMoveUpListener.onMoveUp(position - 1);
                                    }
                                }
                                break;

                            case R.id.menu_down:
                                if (position < rowItem.size() - 1) {
                                    RowItem downItem = rowItem.remove(position);
                                    rowItem.add(position + 1, downItem);
                                    originalRowItem.clear();
                                    originalRowItem.addAll(rowItem);
                                    notifyDataSetChanged();

                                    for (int i = 0; i < originalRowItem.size(); i++) {
                                        String val = String.valueOf(i);
                                        if (i < 10) {
                                            val = "00" + val;
                                        } else if (i < 100) {
                                            val = "0" + val;
                                        }
                                        ContentValues ContentVal = new ContentValues();
                                        ContentVal.put("seq_orderby", val);

                                        dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, ContentVal,
                                                "invoice_no=? COLLATE NOCASE ", new String[]{originalRowItem.get(i).getShipping()});
                                    }

                                    if (onMoveUpListener != null) {
                                        onMoveUpListener.onMoveUp(position + 1);
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

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.activity_child_item, null);
        }

        LinearLayout layout_list_item_child_failed = convertView.findViewById(R.id.layout_list_item_child_failed);
        TextView text_list_item_child_failed_reason = convertView.findViewById(R.id.text_list_item_child_failed_reason);

        LinearLayout layout_list_item_child_parcel_amount = convertView.findViewById(R.id.layout_list_item_child_parcel_amount);
        TextView text_list_item_child_parcel_amount_title = convertView.findViewById(R.id.text_list_item_child_parcel_amount_title);
        TextView text_list_item_child_parcel_amount = convertView.findViewById(R.id.text_list_item_child_parcel_amount);
        TextView text_list_item_child_parcel_amount_unit = convertView.findViewById(R.id.text_list_item_child_parcel_amount_unit);

        LinearLayout layout_list_item_child_telephone = convertView.findViewById(R.id.layout_list_item_child_telephone);
        TextView text_list_item_child_telephone_number = convertView.findViewById(R.id.text_list_item_child_telephone_number);

        LinearLayout layout_list_item_child_mobile = convertView.findViewById(R.id.layout_list_item_child_mobile);
        TextView text_list_item_child_mobile_number = convertView.findViewById(R.id.text_list_item_child_mobile_number);

        ImageView img_list_item_child_sms = convertView.findViewById(R.id.img_list_item_child_sms);
        ImageView img_list_item_child_live10 = convertView.findViewById(R.id.img_list_item_child_live10);
        ImageView img_list_item_child_qpost = convertView.findViewById(R.id.img_list_item_child_qpost);
        ImageView img_list_item_child_driver_memo = convertView.findViewById(R.id.img_list_item_child_driver_memo);

        RelativeLayout layout_list_item_child_delivery_buttons = convertView.findViewById(R.id.layout_list_item_child_delivery_buttons);
        Button btn_list_item_child_delivered = convertView.findViewById(R.id.btn_list_item_child_delivered);
        Button btn_list_item_child_delivery_failed = convertView.findViewById(R.id.btn_list_item_child_delivery_failed);

        RelativeLayout layout_list_item_child_quick_buttons = convertView.findViewById(R.id.layout_list_item_child_quick_buttons);
        Button btn_list_item_child_quick_delivered = convertView.findViewById(R.id.btn_list_item_child_quick_delivered);
        Button btn_list_item_child_quick_failed = convertView.findViewById(R.id.btn_list_item_child_quick_failed);

        LinearLayout layout_list_item_child_pickup_buttons = convertView.findViewById(R.id.layout_list_item_child_pickup_buttons);
        Button btn_list_item_child_pickup_scan = convertView.findViewById(R.id.btn_list_item_child_pickup_scan);
        Button btn_list_item_child_pickup_zero_qty = convertView.findViewById(R.id.btn_list_item_child_pickup_zero_qty);
        Button btn_list_item_child_pickup_visit_log = convertView.findViewById(R.id.btn_list_item_child_pickup_visit_log);

        LinearLayout layout_list_item_child_cnr_buttons = convertView.findViewById(R.id.layout_list_item_child_cnr_buttons);
        Button btn_list_item_child_cnr_failed = convertView.findViewById(R.id.btn_list_item_child_cnr_failed);
        Button btn_list_item_child_cnr_print = convertView.findViewById(R.id.btn_list_item_child_cnr_print);

        RelativeLayout layout_list_item_child_outlet_pickup = convertView.findViewById(R.id.layout_list_item_child_outlet_pickup);
        Button btn_list_item_child_outlet_pickup_scan = convertView.findViewById(R.id.btn_list_item_child_outlet_pickup_scan);


        //

        final RowItem group_item = rowItem.get(groupPosition);
        final ChildItem child = (ChildItem) getChild(groupPosition, childPosition);

        final String tracking_no = rowItem.get(groupPosition).getShipping();
        final String receiver = rowItem.get(groupPosition).getName();
        final String sender = rowItem.get(groupPosition).getSender();
        final String requestor = rowItem.get(groupPosition).getName();
        final String route = rowItem.get(groupPosition).getRoute();
        final String qty = rowItem.get(groupPosition).getQty();


        // Qtalk 안심번호 타입 T - Qnumber 사용
        if (child.getSecretNoType().equals("T")) {

            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.GONE);
            img_list_item_child_live10.setVisibility(View.VISIBLE);
        }
        // Phone 안심번호 - 핸드폰만 활성화
        else if (child.getSecretNoType().equals("P")) {

            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.VISIBLE);
            img_list_item_child_live10.setVisibility(View.GONE);

            SpannableString content = new SpannableString(child.getHp());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            text_list_item_child_mobile_number.setText(content);
        } else {          //안심번호 사용안함

            if (child.getTel().toString() != null && child.getTel().toString().length() > 5) {

                layout_list_item_child_telephone.setVisibility(View.VISIBLE);

                SpannableString content = new SpannableString(child.getTel());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                text_list_item_child_telephone_number.setText(content);
            } else {
                layout_list_item_child_telephone.setVisibility(View.GONE);
            }

            if (child.getHp().toString() != null && child.getHp().toString().length() > 5) {

                layout_list_item_child_mobile.setVisibility(View.VISIBLE);

                SpannableString content = new SpannableString(child.getHp());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                text_list_item_child_mobile_number.setText(content);
            } else {
                layout_list_item_child_mobile.setVisibility(View.GONE);
            }

            img_list_item_child_live10.setVisibility(View.GONE);
        }

        String authNo = MyApplication.preferences.getAuthNo();

        if (authNo.contains("137")) {
            img_list_item_child_live10.setVisibility(View.VISIBLE);
        } else {
            img_list_item_child_live10.setVisibility(View.GONE);
        }

        try {

            String orderType = rowItem.get(groupPosition).getOrder_type_etc();

            Log.e("krm0219", "Order Type ETC : " + rowItem.get(groupPosition).getOrder_type_etc());
            if (orderType != null && orderType.equalsIgnoreCase("DPC")) {

                img_list_item_child_qpost.setVisibility(View.VISIBLE);
            } else {

                img_list_item_child_qpost.setVisibility(View.GONE);
            }
        } catch (Exception e) {

            Log.e("krm0219", "Order Type ETC Exception : " + e.toString());
            img_list_item_child_qpost.setVisibility(View.GONE);
        }

        // 2018.12.26  Delivery / Pickup  Fail Reason
        if (child.getStatReason() != null && 0 < child.getStatReason().length()) {

            String reasonCode = child.getStatReason();
            String reasonText = "";

            if (child.getStat().equals("DX")) {

                ArrayList<FailedCodeResult.FailedCode> arrayList = DataUtil.getFailCode("D");

                if (arrayList != null) {

                    for (int i = 0; i < arrayList.size(); i++) {

                        FailedCodeResult.FailedCode failedCode = arrayList.get(i);

                        if (failedCode.getFailedCode().equals(reasonCode)) {

                            reasonText = failedCode.getFailedString();
                        }
                    }
                }

                layout_list_item_child_failed.setVisibility(View.VISIBLE);
                text_list_item_child_failed_reason.setText(reasonText);
            } else if (child.getStat().equals("PF")) {

                ArrayList<FailedCodeResult.FailedCode> arrayList = DataUtil.getFailCode("P");

                if (arrayList != null) {

                    for (int i = 0; i < arrayList.size(); i++) {

                        FailedCodeResult.FailedCode failedCode = arrayList.get(i);

                        if (failedCode.getFailedCode().equals(reasonCode)) {

                            reasonText = failedCode.getFailedString();
                        }
                    }
                }

                layout_list_item_child_failed.setVisibility(View.VISIBLE);
                text_list_item_child_failed_reason.setText(reasonText);
            } else {

                layout_list_item_child_failed.setVisibility(View.GONE);
            }
        } else {

            layout_list_item_child_failed.setVisibility(View.GONE);
        }


        //
        if (rowItem.get(groupPosition).getType().equals("D")) {

            text_list_item_child_parcel_amount_title.setText(context.getResources().getString(R.string.text_parcel_amount));

            String parcelAmount = rowItem.get(groupPosition).getParcel_amount();
            if (parcelAmount == null) {

                parcelAmount = "0.00";
            } else if (parcelAmount.equals("") || parcelAmount.toLowerCase().equals("null")) {

                parcelAmount = "0.00";
            }
            text_list_item_child_parcel_amount.setText(parcelAmount);

            String parcelAmountUnit = rowItem.get(groupPosition).getCurrency();
            String currencyUnit = "S$";

            if (parcelAmountUnit == null) {
                parcelAmountUnit = "SGD";
            } else if (parcelAmountUnit.equals("") || parcelAmountUnit.toLowerCase().equals("null")) {
                parcelAmountUnit = "SGD";
            }

            if (parcelAmountUnit.equals("SGD")) {
                currencyUnit = "S$";
            } else if (parcelAmountUnit.equals("USD")) {
                currencyUnit = "$";
            } else {
                currencyUnit = parcelAmountUnit;
            }
            text_list_item_child_parcel_amount_unit.setVisibility(View.VISIBLE);
            text_list_item_child_parcel_amount_unit.setText(currencyUnit);

            if (rowItem.get(groupPosition).getRoute().equals("QXQ")) {

                layout_list_item_child_delivery_buttons.setVisibility(View.GONE);
                layout_list_item_child_quick_buttons.setVisibility(View.VISIBLE);
            } else {

                layout_list_item_child_delivery_buttons.setVisibility(View.VISIBLE);
                layout_list_item_child_quick_buttons.setVisibility(View.GONE);
            }

            layout_list_item_child_pickup_buttons.setVisibility(View.GONE);
            layout_list_item_child_cnr_buttons.setVisibility(View.GONE);
            layout_list_item_child_outlet_pickup.setVisibility(View.GONE);

            if (rowItem.get(groupPosition).getOutlet_company().equals("7E") || rowItem.get(groupPosition).getOutlet_company().equals("FL")) {

                layout_list_item_child_parcel_amount.setVisibility(View.GONE);
                // k. 2018.10.24   VisitLog 시 화물이 DPC3-Out 처리됨... 7E 화물은 DPC2-Out 까지만 처리되야 함..
                btn_list_item_child_delivery_failed.setVisibility(View.GONE);

                // 2019.04
                layout_list_item_child_telephone.setVisibility(View.GONE);
                layout_list_item_child_mobile.setVisibility(View.GONE);
                img_list_item_child_sms.setVisibility(View.GONE);
                img_list_item_child_live10.setVisibility(View.GONE);
                img_list_item_child_qpost.setVisibility(View.GONE);
            } else {

                layout_list_item_child_parcel_amount.setVisibility(View.VISIBLE);
                btn_list_item_child_delivery_failed.setVisibility(View.VISIBLE);
            }
        } else {            // Pickup

            text_list_item_child_parcel_amount_title.setText(context.getResources().getString(R.string.text_name));
            text_list_item_child_parcel_amount.setText(rowItem.get(groupPosition).getName());
            text_list_item_child_parcel_amount_unit.setVisibility(View.GONE);

            layout_list_item_child_delivery_buttons.setVisibility(View.GONE);
            layout_list_item_child_quick_buttons.setVisibility(View.GONE);

            //tracking_no 에 따라서 layout 선택하기  by 2016-09-23
            boolean isNotCNR = isPickupNotCNR(tracking_no);

            /*// TEST.
            isNotCNR = true;*/

            if (isNotCNR) { // true 이면 cnr      // C&R  주문건

                layout_list_item_child_pickup_buttons.setVisibility(View.GONE);
                layout_list_item_child_cnr_buttons.setVisibility(View.VISIBLE);
                layout_list_item_child_outlet_pickup.setVisibility(View.GONE);

            } else if (rowItem.get(groupPosition).getOutlet_company().equals("7E") || rowItem.get(groupPosition).getOutlet_company().equals("FL")) {       // 7E, FL

                layout_list_item_child_pickup_buttons.setVisibility(View.GONE);
                layout_list_item_child_cnr_buttons.setVisibility(View.GONE);
                layout_list_item_child_outlet_pickup.setVisibility(View.VISIBLE);

                layout_list_item_child_telephone.setVisibility(View.GONE);
                layout_list_item_child_mobile.setVisibility(View.GONE);
                img_list_item_child_sms.setVisibility(View.GONE);
                img_list_item_child_live10.setVisibility(View.GONE);
                img_list_item_child_qpost.setVisibility(View.GONE);
            } else {    //  일반 Pickup

                layout_list_item_child_pickup_buttons.setVisibility(View.VISIBLE);
                layout_list_item_child_cnr_buttons.setVisibility(View.GONE);
                layout_list_item_child_outlet_pickup.setVisibility(View.GONE);
            }
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

                    String smsBody = String.format(context.getResources().getString(R.string.msg_delivery_start_sms), receiver);
                    Uri smsUri = Uri.parse("sms:" + child.getHp());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
                    intent.putExtra("sms_body", smsBody);
                    context.startActivity(intent);
                } catch (Exception e) {

                    Toast.makeText(context, context.getResources().getString(R.string.msg_send_sms_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        img_list_item_child_live10.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                final String p_qlps_cust_no = group_item.getCustNo();
                final String p_delivery_type = group_item.getType();  //P,D
                final String p_order_type = group_item.getRoute(); // RPC, C2C, GIO
                final String p_tracking_no = group_item.getShipping();
                final String p_svc_nation_cd = "SG";
                final String p_seller_id = group_item.getPartnerID();

                DialogSelectOption(p_qlps_cust_no, p_delivery_type, p_order_type, p_tracking_no, p_svc_nation_cd, p_seller_id);
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
                alert.setTitle(context.getResources().getString(R.string.text_driver_memo1));
                alert.setMessage(shipping);

                // Set an EditText view to get user input
                final EditText input = new EditText(context);
                input.setText(msg);
                input.setTextColor(Color.BLACK);
                alert.setView(input);

                alert.setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
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

                alert.setNegativeButton(context.getResources().getString(R.string.button_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });

                alert.show();
            }
        });


        // NOTIFICATION.   Delivery
        btn_list_item_child_delivered.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

               /* // TEST.
                isConnectPortablePrint(tracking_no);*/

                Intent intent = new Intent(context, DeliveryDoneActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_signature));
                intent.putExtra("type", "D");
                intent.putExtra("waybillNo", tracking_no);
                intent.putExtra("route", route);
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

        btn_list_item_child_delivery_failed.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DeliveryFailedActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_visit_log));
                intent.putExtra("trackingNo", tracking_no);
                intent.putExtra("receiverName", receiver);
                intent.putExtra("senderName", sender);
                context.startActivity(intent);
            }
        });

        // NOTIFICATION.   Pickup
        btn_list_item_child_pickup_scan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                CaptureActivity.removeBarcodeListInstance();

                Intent intent = new Intent(context, CaptureActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_start_to_scan));
                intent.putExtra("type", BarcodeType.PICKUP_START_SCAN);
                intent.putExtra("pickup_no", tracking_no);
                intent.putExtra("applicant", requestor);
                context.startActivity(intent);
            }
        });

        btn_list_item_child_pickup_zero_qty.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            /*    // TEST
                count = 0;
                isConnectPortablePrint(tracking_no);*/

                Intent intent = new Intent(context, PickupZeroQtyActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_zero_qty));
                intent.putExtra("pickupNo", tracking_no);
                intent.putExtra("applicant", requestor);
                context.startActivity(intent);
            }
        });

        btn_list_item_child_pickup_visit_log.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PickupFailedActivity.class);
                intent.putExtra("type", BarcodeType.TYPE_PICKUP);
                intent.putExtra("reqQty", qty);
                intent.putExtra("applicant", requestor);
                intent.putExtra("pickupNo", tracking_no);
                context.startActivity(intent);
            }
        });

        // krm0219  Outlet Pickup Done
        btn_list_item_child_outlet_pickup_scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, OutletPickupScanActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_outlet_pickup_done));
                intent.putExtra("pickup_no", tracking_no);
                intent.putExtra("applicant", requestor);
                intent.putExtra("qty", qty);
                intent.putExtra("route", route);
                context.startActivity(intent);
            }
        });

        //  QUICK Delivery
        btn_list_item_child_quick_delivered.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DeliveryReturnedActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_signature));
                intent.putExtra("type", "D");
                intent.putExtra("trackingNo", tracking_no);
                intent.putExtra("receiverName", receiver);
                intent.putExtra("senderName", sender);
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

        btn_list_item_child_quick_failed.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DeliveryReturnFailedActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_visit_log));
                intent.putExtra("type", "D");
                intent.putExtra("trackingNo", tracking_no);
                intent.putExtra("receiverName", receiver);
                intent.putExtra("senderName", sender);
                context.startActivity(intent);
            }
        });

        //  CNR
        btn_list_item_child_cnr_failed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PickupFailedActivity.class);
                intent.putExtra("type", BarcodeType.TYPE_CNR);
                intent.putExtra("reqQty", qty);
                intent.putExtra("applicant", requestor);
                intent.putExtra("pickupNo", tracking_no);
                context.startActivity(intent);
            }
        });

        btn_list_item_child_cnr_print.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                count = 0;
                isConnectPortablePrint(tracking_no);


               /* new ManualCnRPrintDataHelper.Builder(context, MyApplication.preferences.getUserId(), tracking_no)
                        .setOnCnRPrintDataEventListener(new ManualCnRPrintDataHelper.OnCnRPrintDataEventListener() {

                            @Override
                            public void onPostAssignResult(PrintDataResult stdResult) {

                                PrintDataResult.ResultObject result = stdResult.getResultObject();
                                Log.e("print_list", result.getInvoiceNo() + " / " + result.getCustName() + " / " +
                                        result.getZipCode() + " / " + result.getDeliveryCouse() + " / " + result.getBackaddress() + " / " +
                                        result.getFrontAddress() + " / " + result.getSellerShop());

                            }
                        }).build().execute();*/
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

        Log.e("krm0219", "filterData ");

        try {
            query = query.toUpperCase();
            rowItem.clear();

            if (query.isEmpty()) {
                rowItem.addAll(originalRowItem);
            } else {
                ArrayList<RowItem> newList = new ArrayList<RowItem>();
                for (RowItem rowitem : originalRowItem) {
                    //이름 or 송장번호 조회
                    if (rowitem.getName().toUpperCase().contains(query) || rowitem.getShipping().toUpperCase().contains(query)) {
                        newList.add(rowitem);
                    }
                }
                if (0 < newList.size()) {
                    //RowItem nRowItem = new RowItem(continent.getName(), newList);
                    rowItem.addAll(newList);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {

            Log.e("krm0219", "filterData  Exception  " + e.toString());
        }
    }

    public void setSorting(ArrayList<RowItem> sortedItems) {
        rowItem.clear();
        rowItem.addAll(sortedItems);
        originalRowItem.clear();
        originalRowItem.addAll(sortedItems);
        notifyDataSetChanged();
    }


    public class SendLive10MessageTask extends AsyncTask<String, Integer, StdResult> {

        @Override
        protected StdResult doInBackground(String... params) {

            StdResult ht = SendLive10Message(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            return ht;
        }

        @Override
        protected void onPostExecute(StdResult result) {

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            if (resultCode != 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getResources().getString(R.string.text_alert));
                builder.setMessage(resultMsg);
                builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

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
    private void DialogSelectOption(String qlps_cust_no, String delivery_type, String order_type, String tracking_no, String svc_nation_cd, String seller_id) {

        final String _qlps_cust_no = qlps_cust_no;
        final String _delivery_type = delivery_type;
        final String _order_type = order_type;
        final String _tracking_no = tracking_no;
        final String _svc_nation_cd = svc_nation_cd;
        final String _qsign_id = MyApplication.preferences.getUserId();
        final String _qsign_name = MyApplication.preferences.getUserName();
        final String _seller_id = seller_id;

        final String Pickup_items[] = {
                context.getResources().getString(R.string.msg_qpost_pickup1),
                context.getResources().getString(R.string.msg_qpost_pickup2)
        };
        final String Delivery_items[] = {
                context.getResources().getString(R.string.msg_qpost_delivery1),
                context.getResources().getString(R.string.msg_qpost_delivery2)
        };

        final String[] delivery_qtalk_message_array = {
                context.getResources().getString(R.string.msg_qpost_delivery_1),
                context.getResources().getString(R.string.msg_qpost_delivery_2)
        };
        final String[] pickup_qtalk_message_array = {
                context.getResources().getString(R.string.msg_qpost_pickup_1),
                context.getResources().getString(R.string.msg_qpost_pickup_2)
        };

        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle(context.getResources().getString(R.string.text_qpost_auto_message));
        ab.setSingleChoiceItems(delivery_type.equals("P") ? Pickup_items : Delivery_items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        ListView lv = ((AlertDialog) dialog).getListView();
                        lv.setTag(new Integer(whichButton));
                    }
                })
                .setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

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
                }).setNegativeButton(context.getResources().getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel 버튼 클릭시
                    }
                });
        ab.show();
    }


    private StdResult SendLive10Message(String qlps_cust_no, String delivery_type, String order_type, String tracking_no, String svc_nation_cd, String msg, String qsign_id) {

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

    // 2016-09-23 eylee pickup C&R number check
    public boolean isPickupNotCNR(String trackingNo) {

        boolean isCNR = false;

        if (!trackingNo.equals("")) {

            String ScanNoFirst = trackingNo.substring(0, 1).toUpperCase();
            String ScanNoTwo = trackingNo.substring(0, 2).toUpperCase();

            if (ScanNoTwo.equals("FL")) {

                isCNR = false;
            } else if (!ScanNoFirst.equals("P")) { // cnr 일 때, true

                isCNR = true;

                if (ScanNoFirst.equals("7")) {
                    isCNR = false;
                }
            }
        }

        return isCNR;
    }


    // NOTIFICATION.  Print
    private int count = 0;

    private void isConnectPortablePrint(String tracking_no) {

        //     BluetoothDeviceData.connectedPrinterAddress = "DC:1D:30:92:0A:5C";
        //   BluetoothDeviceData.connectedPrinterAddress = "50:65:83:8D:D5:A9";

        // 연결된 print 없으면..
        if (BluetoothDeviceData.connectedPrinterAddress == null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_first_connect_printer), Toast.LENGTH_SHORT).show();

            try { // kjyoo static 임시
                if (GPrinterData.mBluetoothAdapter != null) {
                    GPrinterData.mBluetoothAdapter.cancelDiscovery();
                    GPrinterData.mBluetoothAdapter = null;
                }

                if (GPrinterData.printerConnManagerList != null) {
                    for (int i = 0; i < GPrinterData.printerConnManagerList.size(); i++) {
                        GPrinterData.printerConnManagerList.get(i).closePort();
                    }
                    GPrinterData.printerConnManagerList = null;
                }

                if (GPrinterData.printerReceiver != null) {
                    context.unregisterReceiver(GPrinterData.printerReceiver);
                    GPrinterData.printerReceiver = null;
                }

                if (GPrinterData.gPrinterHandler != null) {
                    GPrinterData.gPrinterHandler = null;
                }
            } catch (Exception e) {

            }

            Intent intent = new Intent(context, PrinterSettingActivity.class);
            context.startActivity(intent);
            return;
        }


        String deviceAddress = getBluetoothPrinterAddress();
        count++;

        if (!deviceAddress.equals("")) {  // 프린터 연결됨     // 출력시작

            if (count < 10) {
                Toast.makeText(context, context.getResources().getString(R.string.msg_wait_while_print_job), Toast.LENGTH_SHORT).show();
                printLabel(deviceAddress, tracking_no, "isConnectPortablePrint");
            } else {

                BluetoothDeviceData.connectedPrinterAddress = null;
                Toast.makeText(context, "Print Connect Error", Toast.LENGTH_SHORT).show();
                Log.e("print_list", "Print Connect Error");
            }
        } else {

            checkBluetoothState(tracking_no);
        }
    }

    private String getBluetoothPrinterAddress() {

        String address = "";
        PrinterConnManager printerConnManager;

        if (GPrinterData.printerConnManagerList != null && 0 < GPrinterData.printerConnManagerList.size()) {

            printerConnManager = GPrinterData.printerConnManagerList.get(0);
            address = printerConnManager.getMacAddress();
            Log.e("print_list", "getBluetoothPrinterAddress  address : " + address);
        }

        return address;
    }

    private void checkBluetoothState(String tracking_no) {
        Log.e("print_list", TAG + "  checkBluetoothState");

        // Bluetooth 지원 여부 확인
        GPrinterData.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth 지원하지 않음
        if (GPrinterData.mBluetoothAdapter == null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_SHORT).show();
        } else {

            // Bluetooth 지원 && 비활성화 상태
            if (!GPrinterData.mBluetoothAdapter.isEnabled()) {

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) context).startActivityForResult(intent, GPrinterData.REQUEST_ENABLE_BT);
            } else {

                GPrinterData.TRACKING_NO = tracking_no;
                GPrinterData.printerConnManagerList = new ArrayList<>();

                // Bluetooth 지원 && 활성화 상태
                registerReceiver();
                discoveryDevice();
            }
        }
    }


    private void registerReceiver() {

        if (GPrinterData.printerReceiver == null) {

            Log.e("print_list", TAG + "  Called");
            GPrinterData.printerReceiver = new GPrinterBroadcastReceiver(context, ((Activity) context));

            // 인텐트 동록
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);                 // 기기 검색됨
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);   // 기기 검색 종료
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);      // 연결 끊김 확인
            filter.addAction(GPrinterData.ACTION_CONN_STATE);               // action_connect_state
            MyApplication.getContext().registerReceiver(GPrinterData.printerReceiver, filter);
        }

        if (GPrinterData.gPrinterHandler == null) {

            GPrinterData.gPrinterHandler = new GPrinterHandler(context, (Activity) context, this);
        }
    }

    private void discoveryDevice() {

        // startDiscovery()를 호출하여 디바이스 검색을 시작합니다.
        // 만약 이미 검색중이라면 cancelDiscovery()를 호출하여 검색을 멈춘 후 다시 검색해야 합니다.
        if (GPrinterData.mBluetoothAdapter.isDiscovering()) {
            GPrinterData.mBluetoothAdapter.cancelDiscovery();
        }

        GPrinterData.mBluetoothAdapter.startDiscovery();
    }

    private void printLabel(String address, String tracking_no, String where) {

        if (GPrinterData.printerConnManagerList == null || GPrinterData.printerConnManagerList.size() == 0 || !GPrinterData.printerConnManagerList.get(0).getConnState()) {

            return;
        }

        // 위에 if 문은 아마 그냥 통과 될 것 왜냐면 커넥션을 자동으로 하고 바로 프린터 버튼 누른 것처럼 trigger 보완 소스 넣고 있음
        //  handler 에서 메시지 받으면 다시 버튼 클릭을 interface 함수로 호출 하고 있음 - onStartGprinter
        if (GPrinterData.printerConnManagerList.get(0).getCurrentPrinterCommand() == PrinterConnManager.PrinterCommand.TSC) {

            String opId = MyApplication.preferences.getUserId();
            Log.e("print_list", TAG + "  printLabel Command : " + GPrinterData.printerConnManagerList.get(0).getCurrentPrinterCommand() + " / " + address + " / " + tracking_no + " / " + where);

            new ManualCnRPrintDataHelper.Builder(context, opId, tracking_no)
                    .setOnCnRPrintDataEventListener(new ManualCnRPrintDataHelper.OnCnRPrintDataEventListener() {

                        @Override
                        public void onPostAssignResult(PrintDataResult stdResult) {
                            try {
                                if (stdResult != null) {
                                    if (stdResult.getResultCode() == 0) {

                                        Log.e("print_list", TAG + "  sendLabel");
                                        sendLabel(stdResult);
                                    } else {

                                        Toast.makeText(context, stdResult.getResultMsg(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {

                                    Toast.makeText(context, "GetCnRPrintData Error..", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {

                                Toast.makeText(context, "GetCnRPrintData Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("Exception", TAG + "  GetCnRPrintData Exception : " + e.toString());
                            }
                        }
                    }).build().execute();
        } else {

            //   Log.e("print", TAG + "  printLabel : " + where + " / " + tracking_no + "  PRINTER_COMMAND_ERROR");
            GPrinterData.gPrinterHandler.obtainMessage(GPrinterData.PRINTER_COMMAND_ERROR).sendToTarget();
        }
    }


    private void sendLabel(PrintDataResult stdResult) {
        // tsc.addUserCommand("BARCODE 10, 0, \"39\", 80, 0, 0, 2, 5, \"C828996SGSG\"");  - Custom

        PrintDataResult.ResultObject result = stdResult.getResultObject();
        LabelCommand tsc = new LabelCommand();

        tsc.addSize(80, 52); // label 크기 설정 -- mm
        tsc.addGap(0);
        // 인쇄방향 설정
//        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.MIRROR);
//        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.NORMAL);  마지막 버전
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
        //연속인쇄용?
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON);
        //원점좌표설정(인쇄방향하고 같이)
        tsc.addReference(0, 0);
//        tsc.addTear(EscCommand.ENABLE.ON);
        tsc.addTear(EscCommand.ENABLE.OFF);
//        tsc.addPeel(EscCommand.ENABLE.ON); //방법 설명 : 프린터 스트립 모드 설정
        // 인쇄 버퍼 데이터 지우기
        tsc.addCls();

        //첫번째 row
        tsc.add1DBarcode(20, 0, LabelCommand.BARCODETYPE.CODE39S, 80, LabelCommand.READABEL.EANBEL,
                LabelCommand.ROTATION.ROTATION_0, 2, 5, result.getInvoiceNo());
        //   tsc.addQRCode(450, 0, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, result.getInvoiceNo());
        Bitmap bitmap = DataUtil.stringToDataMatrix(result.getInvoiceNo());
        tsc.addBitmap(450, 0, 100, bitmap);

        // 두번째 row
        ArrayList<String> list = cutString(result.getCustName(), 1);
        String consignee = list.get(0);

        tsc.addText(15, 130, LabelCommand.FONTTYPE.FONT_2, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, "To(consignee)");
        tsc.addReverse(10, 115, 186, 50);
        tsc.addBox(195, 115, 565, 165, 1);
        tsc.addText(215, 130, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, consignee);

        // 세번째 row
        tsc.addBox(10, 165, 195, 265, 1);
        tsc.addText(35, 170, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, "Postal code");
        tsc.addText(55, 200, LabelCommand.FONTTYPE.FONT_3, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, result.getZipCode());
        tsc.addText(25, 235, LabelCommand.FONTTYPE.FONT_3, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, result.getDeliveryCouse());
        tsc.addErase(194, 165, 1, 100);

        String address = result.getBackaddress() + " " + result.getFrontAddress();
        //address = "#06-189SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5";
        list = cutString(address, 3);

        tsc.addBox(195, 165, 565, 265, 1);
        for (int i = 0; i < list.size(); i++) {

            int positionY = 175 + (30 * i);

            tsc.addText(215, positionY, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                    com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, list.get(i));
        }

        // 네번째 row
        tsc.addBox(10, 265, 565, 305, 1);
        tsc.addText(35, 275, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, "(" + result.getHpNo() + "/" + result.getTelNo() + ")");

        // 마지막 row
        list = cutString(result.getSellerShop(), 1);
        String seller_shop_nm = list.get(0);

        tsc.addText(15, 320, LabelCommand.FONTTYPE.FONT_2, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, "From(shipper)");
        tsc.addReverse(10, 305, 186, 50);
        tsc.addBox(195, 305, 565, 355, 1);
        tsc.addText(215, 320, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, com.gprinter.command.LabelCommand.ROTATION.ROTATION_0,
                com.gprinter.command.LabelCommand.FONTMUL.MUL_1, com.gprinter.command.LabelCommand.FONTMUL.MUL_1, seller_shop_nm);

        // 라벨인쇄
        tsc.addPrint(1, 1);
        tsc.addSound(1, 100);
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
        Vector<Byte> datas = tsc.getCommand();

        if (GPrinterData.printerConnManagerList.get(0) == null) {
            return;
        }

        // 여기서 Gprinter 함수 콜
        GPrinterData.printerConnManagerList.get(0).sendDataImmediately(datas);
    }

    public ArrayList<String> cutString(String originStr, int lineNum) {

        ArrayList<String> arrayList = new ArrayList();
        if (originStr == null) {
            originStr = "";
        }
        String oriStr = originStr.trim();
        String str1 = "", str2 = "", str3 = "", temp = "";

        if (lineNum == 1) {
            if (oriStr.length() > 27) {
                str1 = oriStr.substring(0, 26);
                str1 += "...";
            } else {
                str1 = oriStr;
            }
        } else if (lineNum == 3) {
            if (oriStr.length() > 27) {
                str1 = oriStr.substring(0, 26);
                str2 = oriStr.substring(26);
                if (str2.length() > 27) {
                    temp = oriStr;
                    str2 = temp.substring(0, 26);
                    str3 = temp.substring(26);
                    if (str3.length() > 27) {
                        str3 = str3.substring(0, 26);
                        str3 += "...";
                    }
                }
            } else {
                str1 = oriStr;
            }
        }
        arrayList.add(str1);
        if (!str2.equals("")) {
            arrayList.add(str2);
        }
        if (!str3.equals("")) {
            arrayList.add(str3);
        }
        return arrayList;
    } // end of cutString

    @Override
    public void onStartGprinter(String tracking_no, String mac_addr) {
        //   Log.e("print", TAG + "  onStartGprinter > " + mac_addr);

        if (mac_addr.equals("")) {

            isConnectPortablePrint(tracking_no);
        } else {

            printLabel(mac_addr, tracking_no, "onStartGprinter");
        }
    }
}