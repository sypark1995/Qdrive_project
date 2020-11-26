package com.giosis.util.qdrive.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.OnServerEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.international.UploadData;
import com.giosis.util.qdrive.main.DeviceDataUploadHelper;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * @author jtpark_eurasia
 * @editor krm0219
 */
public class UploadFailedExpandableListAdapter extends BaseExpandableListAdapter {
    String TAG = "UploadFailedExpandableListAdapter";


    Context context;
    private DatabaseHelper dbHelper = DatabaseHelper.getInstance();
    private String opID;
    private String officeCode;
    private String deviceID;

    private GPSTrackerManager gpsTrackerManager;
    private boolean gpsEnable = false;

    private AdapterInterface mCountListener;

    private ArrayList<RowItemNotUpload> rowItem;
    private ArrayList<RowItemNotUpload> originalRowItem;

    private String[] dx_array = {"NH", "IA", "CR", "MR", "ET"};
    private String[] px_array = {"WA", "RC", "ET"};
    private String[] pf_array = {"WA", "WP", "NA", "NO", "NR", "NQ", "ET"};

    private final String DELIVERY_FAIL = "DX";
    private final String PICKUP_FAIL = "PF";
    private final String PICKUP_CANCEL = "PX";
    private final String PICKUP_DONE = "P3";


    UploadFailedExpandableListAdapter(Context context, ArrayList<RowItemNotUpload> rowItem, AdapterInterface listener) {

        this.context = context;             // getActivity()
        this.rowItem = new ArrayList<>();
        this.rowItem.addAll(rowItem);
        this.originalRowItem = new ArrayList<>();
        this.originalRowItem.addAll(rowItem);

        this.mCountListener = listener;
    }

    public void setGpsTrackerManager(GPSTrackerManager gpsTrackerManager) {

        this.gpsTrackerManager = gpsTrackerManager;
        gpsEnable = gpsTrackerManager.enableGPSSetting();
    }


    public interface AdapterInterface {
        void getFailedCountRefresh();
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.activity_item, null);
        }

        if (gpsEnable && gpsTrackerManager != null) {

            double latitude = gpsTrackerManager.getLatitude();
            double longitude = gpsTrackerManager.getLongitude();
            Log.e("Location", TAG + " getGroupView  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
        }

        LinearLayout layout_list_item_card_view = convertView.findViewById(R.id.layout_list_item_card_view);            // background image change

        TextView text_list_item_d_day = convertView.findViewById(R.id.text_list_item_d_day);
        TextView text_list_item_upload_failed_state = convertView.findViewById(R.id.text_list_item_upload_failed_state);
        ImageView img_list_item_secure_delivery = convertView.findViewById(R.id.img_list_item_secure_delivery);
        ImageView img_list_item_station_icon = convertView.findViewById(R.id.img_list_item_station_icon);
        TextView text_list_item_tracking_no = convertView.findViewById(R.id.text_list_item_tracking_no);
        TextView text_list_item_pickup_state = convertView.findViewById(R.id.text_list_item_pickup_state);
        ImageView img_list_item_up_icon = convertView.findViewById(R.id.img_list_item_up_icon);
        TextView text_list_item_address = convertView.findViewById(R.id.text_list_item_address);
        final FrameLayout layout_list_item_menu_icon = convertView.findViewById(R.id.layout_list_item_menu_icon);

        TextView text_list_item_receipt_name = convertView.findViewById(R.id.text_list_item_receipt_name);
        LinearLayout layout_list_item_delivery_outlet_info = convertView.findViewById(R.id.layout_list_item_delivery_outlet_info);
        RelativeLayout layout_list_item_pickup_info = convertView.findViewById(R.id.layout_list_item_pickup_info);
        LinearLayout layout_list_item_request = convertView.findViewById(R.id.layout_list_item_request);
        TextView text_list_item_request = convertView.findViewById(R.id.text_list_item_request);
        LinearLayout layout_list_item_driver_memo = convertView.findViewById(R.id.layout_list_item_driver_memo);


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


        final RowItemNotUpload row_pos = rowItem.get(groupPosition);

        String status = "";
        switch (row_pos.getStat()) {
            case DELIVERY_FAIL:
                status = context.getResources().getString(R.string.text_d_failed);
                break;
            case BarcodeType.DELIVERY_DONE:
                status = context.getResources().getString(R.string.text_delivered);
                break;
            case PICKUP_FAIL:
                status = context.getResources().getString(R.string.text_p_failed);
                break;
            case PICKUP_CANCEL:
                status = context.getResources().getString(R.string.text_p_cancelled);
                break;
            case PICKUP_DONE:
                status = context.getResources().getString(R.string.text_p_done);
                break;
        }

        text_list_item_d_day.setVisibility(View.GONE);
        text_list_item_upload_failed_state.setVisibility(View.VISIBLE);
        text_list_item_upload_failed_state.setTextColor(context.getResources().getColor(R.color.color_ff0000));
        text_list_item_upload_failed_state.setText(status);

        img_list_item_secure_delivery.setVisibility(View.GONE);
        img_list_item_station_icon.setVisibility(View.GONE);
        text_list_item_tracking_no.setText(row_pos.getShipping());
        text_list_item_pickup_state.setVisibility(View.GONE);
        text_list_item_address.setText(row_pos.getAddress());
        text_list_item_receipt_name.setText(row_pos.getName());
        layout_list_item_delivery_outlet_info.setVisibility(View.GONE);
        layout_list_item_pickup_info.setVisibility(View.GONE);
        layout_list_item_driver_memo.setVisibility(View.GONE);

        if (row_pos.getRequest() == null || row_pos.getRequest().length() == 0) {

            layout_list_item_request.setVisibility(View.GONE);
        } else {

            layout_list_item_request.setVisibility(View.VISIBLE);
            text_list_item_request.setText(row_pos.getRequest());
        }

        layout_list_item_menu_icon.setTag(row_pos.getShipping()); // 퀵메뉴 아이콘에 shipping no

        layout_list_item_menu_icon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, layout_list_item_menu_icon);
                popup.getMenuInflater().inflate(R.menu.quickmenu_failed, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        Cursor cursor = dbHelper.get("SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + layout_list_item_menu_icon.getTag().toString() + "' LIMIT 1");
                        if (cursor != null) {
                            cursor.moveToFirst();
                            String addr = cursor.getString(cursor.getColumnIndex("address"));
                            Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + addr);
                            Intent it = new Intent(Intent.ACTION_VIEW, uri);
                            context.startActivity(it);
                        }

                        return true;
                    }
                });

                popup.show();
            }
        });

        return convertView;
    }


    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_child_item_not_upload, null);
        }

        LinearLayout layout_list_item_child_telephone = convertView.findViewById(R.id.layout_list_item_child_telephone);
        TextView text_list_item_child_telephone_number = convertView.findViewById(R.id.text_list_item_child_telephone_number);
        LinearLayout layout_list_item_child_mobile = convertView.findViewById(R.id.layout_list_item_child_mobile);
        TextView text_list_item_child_mobile_number = convertView.findViewById(R.id.text_list_item_child_mobile_number);
        LinearLayout layout_list_item_child_failed_reason = convertView.findViewById(R.id.layout_list_item_child_failed_reason);
        TextView text_list_item_child_failed_reason = convertView.findViewById(R.id.text_list_item_child_failed_reason);
        LinearLayout layout_list_item_child_memo = convertView.findViewById(R.id.layout_list_item_child_memo);
        TextView text_list_item_child_memo = convertView.findViewById(R.id.text_list_item_child_memo);

        ImageView img_list_item_child_sms = convertView.findViewById(R.id.img_list_item_child_sms);
        ImageView img_list_item_child_live10 = convertView.findViewById(R.id.img_list_item_child_live10);

        LinearLayout layout_list_item_child_requestor = convertView.findViewById(R.id.layout_list_item_child_requestor);
        TextView text_list_item_child_requestor = convertView.findViewById(R.id.text_list_item_child_requestor);
        ImageView img_list_item_child_requestor_sign = convertView.findViewById(R.id.img_list_item_child_requestor_sign);
        LinearLayout layout_list_item_child_driver = convertView.findViewById(R.id.layout_list_item_child_driver);
        ImageView img_list_item_child_driver_sign = convertView.findViewById(R.id.img_list_item_child_driver_sign);

        Button btn_list_item_child_upload = convertView.findViewById(R.id.btn_list_item_child_upload);


        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        final String tracking_no = rowItem.get(groupPosition).getShipping();
        final String receiver = rowItem.get(groupPosition).getName();
        final ChildItemNotUpload child = (ChildItemNotUpload) getChild(groupPosition, childPosition);

        if (child.getSecretNoType().equals("T")) {     // Qtalk 안심번호 타입 T - Qnumber 사용

            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.GONE);
            img_list_item_child_live10.setVisibility(View.VISIBLE);
        } else if (child.getSecretNoType().equals("P")) {  // Phone 안심번호 - 핸드폰만 활성화

            layout_list_item_child_telephone.setVisibility(View.GONE);
            layout_list_item_child_mobile.setVisibility(View.VISIBLE);
            text_list_item_child_mobile_number.setText(child.getHp());
            img_list_item_child_live10.setVisibility(View.GONE);
        } else {          //안심번호 사용안함
            img_list_item_child_live10.setVisibility(View.GONE);

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
        }

        //  Reason
        if (child.getStatReason() != null && !child.getStatReason().contains(" ") && 0 < child.getStatReason().length()) {

            String reasonText = "";
            String reasonCode = child.getStatReason();
            layout_list_item_child_failed_reason.setVisibility(View.VISIBLE);

            switch (child.getStat()) {
                case DELIVERY_FAIL:

                    String[] dxStringArray = context.getResources().getStringArray(R.array.delivery_fail_reason_array);
                    for (int i = 0; i < dx_array.length; i++) {
                        if (dx_array[i].equals(reasonCode)) {
                            reasonText = dxStringArray[i];
                        }
                    }
                    text_list_item_child_failed_reason.setText(reasonText);
                    break;
                case PICKUP_CANCEL:

                    String[] pxStringArray = context.getResources().getStringArray(R.array.cancel_reason_array);
                    for (int i = 0; i < px_array.length; i++) {
                        if (px_array[i].equals(reasonCode)) {
                            reasonText = pxStringArray[i];
                        }
                    }
                    text_list_item_child_failed_reason.setText(reasonText);
                    break;
                case PICKUP_FAIL:

                    String[] pfStringArray = context.getResources().getStringArray(R.array.fail_reason_array);
                    for (int i = 0; i < pf_array.length; i++) {
                        if (pf_array[i].equals(reasonCode)) {
                            reasonText = pfStringArray[i];
                        }
                    }
                    text_list_item_child_failed_reason.setText(reasonText);
                    break;
            }
        } else {

            layout_list_item_child_failed_reason.setVisibility(View.GONE);
        }

        // 메모
        if (!child.getStat().equals(PICKUP_DONE)) {

            if (0 < child.getStatMsg().length()) {

                layout_list_item_child_memo.setVisibility(View.VISIBLE);
                text_list_item_child_memo.setText(child.getStatMsg());
            } else {

                layout_list_item_child_memo.setVisibility(View.GONE);
            }
        } else {

            layout_list_item_child_memo.setVisibility(View.GONE);
        }


        String pickupSign = "/QdrivePickup";
        String pickupDriverSign = "/QdriveCollector";
        String deliverySign = "/Qdrive";

        Bitmap myBitmap;
        switch (child.getStat()) {
            case BarcodeType.DELIVERY_DONE: {        // Delivery   sign 1개

                String dirPath = Environment.getExternalStorageDirectory().toString() + deliverySign;
                String filePath = dirPath + "/" + tracking_no + ".png";
                File imgFile = new File(filePath);

                if (imgFile.exists()) {

                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    text_list_item_child_requestor.setText(context.getResources().getString(R.string.text_receiver));
                    img_list_item_child_requestor_sign.setImageBitmap(myBitmap);
                    layout_list_item_child_driver.setVisibility(View.GONE);
                }
                break;
            }
            case PICKUP_DONE:
            case PICKUP_CANCEL: {

                String dirPath = Environment.getExternalStorageDirectory().toString() + "/" + pickupSign;
                String dirPath2 = Environment.getExternalStorageDirectory().toString() + "/" + pickupDriverSign;

                String filePath = dirPath + "/" + tracking_no + ".png";
                String filePath2 = dirPath2 + "/" + tracking_no + ".png";

                File imgFile = new File(filePath);
                File imgFile2 = new File(filePath2);

                layout_list_item_child_driver.setVisibility(View.VISIBLE);

                if (imgFile.exists()) {

                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    text_list_item_child_requestor.setText(context.getResources().getString(R.string.text_requestor));
                    img_list_item_child_requestor_sign.setImageBitmap(myBitmap);
                }
                if (imgFile2.exists()) {

                    Bitmap myBitmap2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                    img_list_item_child_driver_sign.setImageBitmap(myBitmap2);
                }
                break;
            }
            default:

                layout_list_item_child_requestor.setVisibility(View.GONE);
                layout_list_item_child_driver.setVisibility(View.GONE);
                break;
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

                String smsBody = String.format(context.getResources().getString(R.string.msg_delivery_start_sms), receiver);
                Uri smsUri = Uri.parse("sms:" + child.getHp());
                Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
                intent.putExtra("sms_body", smsBody);
                context.startActivity(intent);
            }

        });


        img_list_item_child_live10.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                String msg = String.format(context.getResources().getString(R.string.msg_delivery_start_sms), receiver);
                alert.setTitle(context.getResources().getString(R.string.text_qpost_message));

                final EditText input = new EditText(context);
                input.setText(msg);
                alert.setView(input);

                alert.setPositiveButton(context.getResources().getString(R.string.button_send), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("qtalk://link?qnumber=" + child.getSecretNo() + "&msg=" + value + "&link=&execurl="));
                        context.startActivity(intent);
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


        btn_list_item_child_upload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                ArrayList<UploadData> songjanglist = new ArrayList<>();
                // 업로드 대상건 로컬 DB 조회
                DatabaseHelper dbHelper = DatabaseHelper.getInstance();
                String selectQuery = "select invoice_no" +
                        " , stat " +
                        " , ifnull(rcv_type, '')  as rcv_type" +
                        " , ifnull(fail_reason, '')  as fail_reason" +
                        " , ifnull(driver_memo, '') as driver_memo" +
                        " , ifnull(real_qty, '') as real_qty" +
                        " , ifnull(retry_dt , '') as retry_dt" +
                        " , type " +
                        " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                        " where reg_id= '" + opID + "'" +
                        " and invoice_no = '" + tracking_no + "'" +
                        " and punchOut_stat <> 'S' ";
                Cursor cs = dbHelper.get(selectQuery);

                if (cs.moveToFirst()) {
                    do {
                        UploadData data = new UploadData();
                        data.setNoSongjang(cs.getString(cs.getColumnIndex("invoice_no")));
                        data.setStat(cs.getString(cs.getColumnIndex("stat")));
                        data.setReceiveType(cs.getString(cs.getColumnIndex("rcv_type")));
                        data.setFailReason(cs.getString(cs.getColumnIndex("fail_reason")));
                        data.setDriverMemo(cs.getString(cs.getColumnIndex("driver_memo")));
                        data.setRealQty(cs.getString(cs.getColumnIndex("real_qty")));
                        data.setRetryDay(cs.getString(cs.getColumnIndex("retry_dt")));
                        data.setType(cs.getString(cs.getColumnIndex("type")));
                        songjanglist.add(data);
                    } while (cs.moveToNext());
                }


                double latitude = 0;
                double longitude = 0;

                if (gpsEnable && gpsTrackerManager != null) {

                    latitude = gpsTrackerManager.getLatitude();
                    longitude = gpsTrackerManager.getLongitude();

                    Log.e("Location", TAG + " btn_list_item_upload  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
                }

                if (0 < songjanglist.size()) {


                    DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadDeliveryData + "/" + com.giosis.library.util.DataUtil.requestSetUploadPickupData);

                    new DeviceDataUploadHelper.Builder(context, opID, officeCode, deviceID, songjanglist, "QL", latitude, longitude).
                            setOnServerEventListener(new OnServerEventListener() {

                                @Override
                                public void onPostResult() {

                                    try {
                                        rowItem.remove(groupPosition);
                                    } catch (Exception e) {

                                    }

                                    DataUtil.uploadFailedListPosition = 0;
                                    originalRowItem = rowItem;
                                    notifyDataSetChanged();
                                    mCountListener.getFailedCountRefresh();
                                }

                                @Override
                                public void onPostFailList() {
                                }

                            }).build().execute();
                } else {

                    new AlertDialog.Builder(context)
                            .setMessage(context.getResources().getString(R.string.text_data_error))
                            .setTitle("[" + context.getResources().getString(R.string.button_upload) + "]")
                            .setCancelable(false).setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
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

        ArrayList<ChildItemNotUpload> chList = rowItem.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {

        return rowItem.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        ArrayList<ChildItemNotUpload> chList = rowItem.get(groupPosition).getItems();
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
            ArrayList<RowItemNotUpload> newList = new ArrayList<>();
            for (RowItemNotUpload rowItemNotUpload : originalRowItem) {
                //이름 or 송장번호 조회
                if (rowItemNotUpload.getName().toUpperCase().contains(query) || rowItemNotUpload.getShipping().toUpperCase().contains(query)) {
                    newList.add(rowItemNotUpload);
                }
            }
            if (0 < newList.size()) {
                rowItem.addAll(newList);
            }
        }

        notifyDataSetChanged();
    }

    public void setSorting(ArrayList<RowItemNotUpload> sortedItems) {
        rowItem.clear();
        rowItem.addAll(sortedItems);
        originalRowItem = sortedItems;
        notifyDataSetChanged();
    }
}