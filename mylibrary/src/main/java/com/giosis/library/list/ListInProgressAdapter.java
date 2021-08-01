package com.giosis.library.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.CaptureActivity1;
import com.giosis.library.bluetooth.BluetoothListener;
import com.giosis.library.list.delivery.DeliveryDoneActivity;
import com.giosis.library.list.delivery.DeliveryFailedActivity;
import com.giosis.library.list.delivery.QuickReturnFailedActivity;
import com.giosis.library.list.delivery.QuickReturnedActivity;
import com.giosis.library.list.pickup.OutletPickupStep1Activity;
import com.giosis.library.list.pickup.PickupFailedActivity;
import com.giosis.library.list.pickup.PickupZeroQtyActivity;
import com.giosis.library.message.CustomerMessageListDetailActivity;
import com.giosis.library.server.data.FailedCodeResult;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.Preferences;

import java.util.ArrayList;

public class ListInProgressAdapter extends BaseExpandableListAdapter {

    private OnMoveUpListener onMoveUpListener;

    private ArrayList<RowItem> rowItem;
    private ArrayList<RowItem> originalRowItem;
    BluetoothListener bluetoothListener;

    public ListInProgressAdapter(ArrayList<RowItem> rowItems, BluetoothListener bluetoothListener) {
        this.rowItem = new ArrayList<>();
        this.rowItem.addAll(rowItems);

        this.originalRowItem = new ArrayList<>();
        this.originalRowItem.addAll(rowItems);

        this.bluetoothListener = bluetoothListener;
    }

    public void setOnMoveUpListener(OnMoveUpListener listener) {
        this.onMoveUpListener = listener;
    }

    public interface OnMoveUpListener {
        void onMoveUp(int pos);
    }

    @Override
    public int getGroupCount() {
        if (rowItem != null) {
            return rowItem.size();
        }
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (rowItem != null) {
            try {
                return rowItem.get(groupPosition).getItems().size();
            } catch (Exception e) {

            }
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        try {
            return rowItem.get(groupPosition);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        try {
            return rowItem.get(groupPosition).getItems().get(childPosition);
        } catch (Exception e) {

        }
        return null;
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
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final int position = groupPosition;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_group_item, null);
        }

        LinearLayout layout_list_item_card_view = convertView.findViewById(R.id.layout_list_item_card_view);            // background change

        TextView text_list_item_d_day = convertView.findViewById(R.id.text_list_item_d_day);
        ImageView img_list_item_secure_delivery = convertView.findViewById(R.id.img_list_item_secure_delivery);
        ImageView img_list_item_station_icon = convertView.findViewById(R.id.img_list_item_station_icon);
        TextView text_list_item_tracking_no = convertView.findViewById(R.id.text_list_item_tracking_no);
        TextView text_list_item_pickup_state = convertView.findViewById(R.id.text_list_item_pickup_state);
        TextView text_list_item_high_amount = convertView.findViewById(R.id.text_list_item_high_amount);
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
            lp.setMargins(0, 24, 0, 0);
            layout_list_item_card_view.setLayoutParams(lp);
        }


        final RowItem row_pos = rowItem.get(groupPosition);
        Log.i("krm0219", "  Route : " + row_pos.getRoute() + " / Stat : " + row_pos.getStat() + " / Number : " + row_pos.getShipping());

        text_list_item_d_day.setText(row_pos.getDelay());
        if (row_pos.getDelay().equals("D+0") || row_pos.getDelay().equals("D+1")) {
            text_list_item_d_day.setTextColor(parent.getContext().getResources().getColor(R.color.color_303030));
        } else {
            text_list_item_d_day.setTextColor(parent.getContext().getResources().getColor(R.color.color_ff0000));
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

        // 2019.10 - 참조 픽업번호가 있으면 해당 번호로 표시, 없으면 기존 픽업번호 (Ref. Pickup No)
        if (row_pos.getType().equals("P")) {        // Pickup
            if (!row_pos.getRef_pickup_no().equals("")) {

                text_list_item_tracking_no.setText(row_pos.getRef_pickup_no());
            } else {

                text_list_item_tracking_no.setText(row_pos.getShipping());
            }
        } else {        // Delivery

            text_list_item_tracking_no.setText(row_pos.getShipping());
        }

        text_list_item_address.setText(row_pos.getAddress());
        layout_list_item_menu_icon.setTag(row_pos.getShipping());
        text_list_item_receipt_name.setText(row_pos.getName());

        //픽업
        if (row_pos.getType().equals("P")) {

            text_list_item_tracking_no.setTextColor(parent.getContext().getResources().getColor(R.color.color_363BE7));
            img_list_item_secure_delivery.setVisibility(View.GONE);
            text_list_item_high_amount.setVisibility(View.GONE);

            if (row_pos.getStat().equals("RE")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(parent.getContext().getResources().getString(R.string.text_pickup_reassigned));
            } else if (row_pos.getStat().equals("PF")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(parent.getContext().getResources().getString(R.string.text_pickup_failed));
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

            text_list_item_tracking_no.setTextColor(parent.getContext().getResources().getColor(R.color.color_32bd87));

            if (row_pos.getSecure_delivery_yn() != null && row_pos.getSecure_delivery_yn().equals("Y")) {

                img_list_item_secure_delivery.setVisibility(View.VISIBLE);
            } else {

                img_list_item_secure_delivery.setVisibility(View.GONE);
            }

            if (row_pos.getStat().equals("DX")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(parent.getContext().getResources().getString(R.string.text_failed));
            } else {

                text_list_item_pickup_state.setVisibility(View.GONE);
            }

            // 2021.04  High amount
            if (row_pos.getHigh_amount_yn().equals("Y")) {

                text_list_item_high_amount.setVisibility(View.VISIBLE);
            } else {

                text_list_item_high_amount.setVisibility(View.GONE);
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

                text_list_item_pickup_state.setText(parent.getContext().getResources().getString(R.string.text_retrieve));
            } else if (row_pos.getType().equals("D")) {

                text_list_item_pickup_state.setText(parent.getContext().getResources().getString(R.string.text_delivery));
            }

            text_list_item_pickup_state.setVisibility(View.VISIBLE);
            layout_list_item_request.setVisibility(View.GONE);
        }


        //우측 메뉴 아이콘 클릭 이벤트  Quick Menu
        layout_list_item_menu_icon.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(v.getContext(), layout_list_item_menu_icon);
            popup.getMenuInflater().inflate(R.menu.quickmenu, popup.getMenu());
            popup.show();

            popup.setOnMenuItemClickListener(item -> {

                int itemId = item.getItemId();
                if (itemId == R.id.menu_one) {
                    Cursor cs = DatabaseHelper.getInstance().get("SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + layout_list_item_menu_icon.getTag().toString() + "' LIMIT 1");
                    if (cs != null) {

                        cs.moveToFirst();

                        String address = cs.getString(cs.getColumnIndex("address"));
                        Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + address);
                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                        v.getContext().startActivity(it);
                    }

                } else if (itemId == R.id.menu_up) {
                    if (0 < position) {

                        RowItem upItem = rowItem.remove(position);
                        rowItem.add(position - 1, upItem);
                        originalRowItem.clear();
                        originalRowItem.addAll(rowItem);
                        notifyDataSetChanged();

                        if (onMoveUpListener != null) {
                            onMoveUpListener.onMoveUp(position - 1);
                        }
                    }

                } else if (itemId == R.id.menu_down) {
                    if (position < rowItem.size() - 1) {
                        RowItem downItem = rowItem.remove(position);
                        rowItem.add(position + 1, downItem);
                        originalRowItem.clear();
                        originalRowItem.addAll(rowItem);
                        notifyDataSetChanged();

                        if (onMoveUpListener != null) {
                            onMoveUpListener.onMoveUp(position + 1);
                        }
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

            LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_child, null);
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

        LinearLayout layout_list_item_child_buttons2 = convertView.findViewById(R.id.layout_list_item_child_buttons2);
        Button btn_list_item_child_detail_button = convertView.findViewById(R.id.btn_list_item_child_detail_button);


        String authNo = Preferences.INSTANCE.getAuthNo();

        final RowItem group_item = rowItem.get(groupPosition);
        final ChildItem child = (ChildItem) getChild(groupPosition, childPosition);

        final String tracking_no = rowItem.get(groupPosition).getShipping();
        final String receiver = rowItem.get(groupPosition).getName();
        final String sender = rowItem.get(groupPosition).getSender();
        final String requester = rowItem.get(groupPosition).getName();
        final String route = rowItem.get(groupPosition).getRoute();
        final String qty = rowItem.get(groupPosition).getQty();
        final String highAmountYn = rowItem.get(groupPosition).getHigh_amount_yn();


        if (child.getSecretNoType().equals("T")) {    // Qtalk 안심번호 타입 T - Qnumber 사용
            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.GONE);
            img_list_item_child_live10.setVisibility(View.VISIBLE);

        } else if (child.getSecretNoType().equals("P")) {  // Phone 안심번호 - 핸드폰만 활성화

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
        if (child.getStatReason() != null && child.getStatReason().length() > 0) {

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

            text_list_item_child_parcel_amount_title.setText(parent.getContext().getResources().getString(R.string.text_parcel_amount));

            String parcelAmount = rowItem.get(groupPosition).getParcel_amount();

            if (parcelAmount == null) {
                parcelAmount = "0.00";

            } else if (parcelAmount.equals("") || parcelAmount.toLowerCase().equals("null")) {
                parcelAmount = "0.00";
            }

            text_list_item_child_parcel_amount.setText(parcelAmount);

            String parcelAmountUnit = rowItem.get(groupPosition).getCurrency();
            String currencyUnit;

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

            layout_list_item_child_buttons2.setVisibility(View.GONE);
        } else {            // Pickup

            text_list_item_child_parcel_amount_title.setText(parent.getContext().getResources().getString(R.string.text_name));
            text_list_item_child_parcel_amount.setText(rowItem.get(groupPosition).getName());
            text_list_item_child_parcel_amount_unit.setVisibility(View.GONE);

            layout_list_item_child_delivery_buttons.setVisibility(View.GONE);
            layout_list_item_child_quick_buttons.setVisibility(View.GONE);

            //tracking_no 에 따라서 layout 선택하기  by 2016-09-23
            boolean isNotCNR = isPickupNotCNR(tracking_no);

           /* //TEST.  CNR
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

            if ("SG".equals(Preferences.INSTANCE.getUserNation())) {
                // Trip
                if (rowItem.get(groupPosition).isPrimaryKey()) {
                    layout_list_item_child_buttons2.setVisibility(View.VISIBLE);
                } else {
                    layout_list_item_child_buttons2.setVisibility(View.GONE);
                }

            } else {
                layout_list_item_child_buttons2.setVisibility(View.GONE);
            }
        }

        if ("SG".equals(Preferences.INSTANCE.getUserNation())) {

            btn_list_item_child_detail_button.setVisibility(View.VISIBLE);
            btn_list_item_child_detail_button.setOnClickListener(v -> {

                ArrayList<RowItem> tripDataArrayList = group_item.getTripSubDataArrayList();

                /*for (int i = 0; i < tripDataArrayList.size(); i++) {
                    Log.e("trip", "DATA :: " + tripDataArrayList.get(i).getShipping() + tripDataArrayList.get(i).getAddress());
                }*/

                PickupTripDetailDialog dialog = new PickupTripDetailDialog(v.getContext(), tripDataArrayList, bluetoothListener);
                dialog.show();
                //    dialog.setCanceledOnTouchOutside(false);
                Window window = dialog.getWindow();
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            });

        } else {
            btn_list_item_child_detail_button.setVisibility(View.GONE);
        }

        text_list_item_child_telephone_number.setOnClickListener(v -> {
            Uri callUri = Uri.parse("tel:" + child.getTel());
            Intent intent = new Intent(Intent.ACTION_DIAL, callUri);
            v.getContext().startActivity(intent);
        });


        text_list_item_child_mobile_number.setOnClickListener(v -> {
            Uri callUri = Uri.parse("tel:" + child.getHp());
            Intent intent = new Intent(Intent.ACTION_DIAL, callUri);
            v.getContext().startActivity(intent);
        });

        img_list_item_child_sms.setOnClickListener(v -> {

            try {
                String smsBody = String.format(v.getContext().getResources().getString(R.string.msg_delivery_start_sms), receiver);
                Uri smsUri = Uri.parse("sms:" + child.getHp());
                Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
                intent.putExtra("sms_body", smsBody);
                v.getContext().startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.msg_send_sms_error), Toast.LENGTH_SHORT).show();
            }
        });

        img_list_item_child_live10.setOnClickListener(v -> {

            final String p_qlps_cust_no = group_item.getCustNo();
            final String p_delivery_type = group_item.getType();  //P,D
            final String p_order_type = group_item.getRoute(); // RPC, C2C, GIO
            final String p_tracking_no = group_item.getShipping();
            final String p_seller_id = group_item.getPartnerID();

            SendLive10Message sendLive10Message = new SendLive10Message(v.getContext());
            sendLive10Message.dialogSelectOption(v.getContext(), p_qlps_cust_no, p_delivery_type, p_order_type, p_tracking_no, p_seller_id);
        });

        img_list_item_child_qpost.setOnClickListener(view -> {

            Intent intent = new Intent(view.getContext(), CustomerMessageListDetailActivity.class);
            intent.putExtra("tracking_no", tracking_no);
            view.getContext().startActivity(intent);
        });

        img_list_item_child_driver_memo.setOnClickListener(v -> {

            AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

            String msg = group_item.getSelfMemo();
            String shipping = group_item.getShipping();
            alert.setTitle(v.getContext().getResources().getString(R.string.text_driver_memo1));
            alert.setMessage(shipping);

            // Set an EditText view to get user input
            final EditText input = new EditText(v.getContext());
            input.setText(msg);
            input.setTextColor(Color.BLACK);
            alert.setView(input);

            alert.setPositiveButton(v.getContext().getResources().getString(R.string.button_ok),
                    (dialog, whichButton) -> {

                        String selfMemo = input.getText().toString();

                        ContentValues contentVal = new ContentValues();
                        contentVal.put("self_memo", selfMemo);

                        DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                                "invoice_no= ? COLLATE NOCASE ", new String[]{tracking_no});

                        group_item.setSelfMemo(selfMemo);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton(v.getContext().getResources().getString(R.string.button_cancel),
                            (dialog, whichButton) -> {

                            });

            alert.show();
        });

        btn_list_item_child_delivered.setOnClickListener(v -> {

            if (route.contains("7E") || route.contains("FL")) {

                Intent intent = new Intent(v.getContext(), DeliveryDoneActivity.class);
                intent.putExtra("parcel", rowItem.get(groupPosition));
                intent.putExtra("route", route);
                v.getContext().startActivity(intent);
            } else {

                Intent intent = new Intent(v.getContext(), DeliveryDoneActivity.class);
                intent.putExtra("parcel", rowItem.get(groupPosition));
                v.getContext().startActivity(intent);
            }
        });

        btn_list_item_child_delivery_failed.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), DeliveryFailedActivity.class);
            intent.putExtra("trackingNo", tracking_no);
            intent.putExtra("receiverName", receiver);
            intent.putExtra("senderName", sender);
            v.getContext().startActivity(intent);
        });


        btn_list_item_child_pickup_scan.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), CaptureActivity1.class);
            intent.putExtra("title", v.getContext().getResources().getString(R.string.text_start_to_scan));
            intent.putExtra("type", BarcodeType.PICKUP_SCAN_ALL);
            intent.putExtra("pickup_no", tracking_no);
            intent.putExtra("applicant", requester);
            v.getContext().startActivity(intent);
        });


        btn_list_item_child_pickup_zero_qty.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), PickupZeroQtyActivity.class);
            intent.putExtra("title", v.getContext().getResources().getString(R.string.text_zero_qty));
            intent.putExtra("pickupNo", tracking_no);
            intent.putExtra("applicant", requester);
            v.getContext().startActivity(intent);

        });


        btn_list_item_child_pickup_visit_log.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), PickupFailedActivity.class);
            intent.putExtra("type", BarcodeType.TYPE_PICKUP);
            intent.putExtra("reqQty", qty);
            intent.putExtra("applicant", requester);
            intent.putExtra("pickupNo", tracking_no);
            v.getContext().startActivity(intent);

        });

        // NOTIFICATION.  Outlet Pickup Done
        btn_list_item_child_outlet_pickup_scan.setOnClickListener(view -> {

            Intent intent = new Intent(view.getContext(), OutletPickupStep1Activity.class);
            intent.putExtra("title", view.getContext().getResources().getString(R.string.text_outlet_pickup_done));
            intent.putExtra("pickup_no", tracking_no);
            intent.putExtra("applicant", requester);
            intent.putExtra("qty", qty);
            intent.putExtra("route", route);
            view.getContext().startActivity(intent);
        });


        btn_list_item_child_quick_delivered.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), QuickReturnedActivity.class);
            intent.putExtra("title", v.getContext().getResources().getString(R.string.text_signature));
            intent.putExtra("waybillNo", tracking_no);
            v.getContext().startActivity(intent);

        });

        btn_list_item_child_quick_failed.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), QuickReturnFailedActivity.class);
            intent.putExtra("title", v.getContext().getResources().getString(R.string.text_visit_log));
            intent.putExtra("waybillNo", tracking_no);
            v.getContext().startActivity(intent);

        });

        btn_list_item_child_cnr_failed.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), PickupFailedActivity.class);
            intent.putExtra("type", BarcodeType.TYPE_CNR);
            intent.putExtra("reqQty", qty);
            intent.putExtra("applicant", requester);
            intent.putExtra("pickupNo", tracking_no);
            v.getContext().startActivity(intent);

        });

        btn_list_item_child_cnr_print.setOnClickListener(v -> {

            DataUtil.logEvent("button_click", "ListActivity", "Print_CNR");
            bluetoothListener.isConnectPortablePrint(tracking_no);
        });

        return convertView;
    }


    // 2016-09-23 eylee pickup C&R number check
    private boolean isPickupNotCNR(String trackingNo) {

        boolean isCNR = false;

        if (!trackingNo.equals("")) {

            String ScanNoFirst = trackingNo.substring(0, 1).toUpperCase();
            String ScanNoTwo = trackingNo.substring(0, 2).toUpperCase();

            if (ScanNoTwo.equals("FL")) {
                return false;

            } else if (ScanNoFirst.equals("7")) {
                return false;

            } else if (!ScanNoFirst.equals("P")) { // cnr 일 때, true
                isCNR = true;

            }
        }

        return isCNR;
    }

    //Search
    public void filterData(String query) {
        try {
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


}
