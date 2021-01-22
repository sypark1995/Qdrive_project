package com.giosis.util.qdrive.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.library.list.pickup.PickupScannedListActivity;
import com.giosis.library.setting.bluetooth.BluetoothDeviceData;
import com.giosis.library.setting.bluetooth.PrinterSettingActivity;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterBroadcastReceiver;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterData;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterHandler;
import com.giosis.util.qdrive.portableprinter.bluetooth.PrinterConnManager;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;

import java.util.ArrayList;
import java.util.Vector;

/***********************
 * @author jtpark_eurasia
 *
 *  Child View 를 갖는 Adapter
 *  List 클릭 시 확장 
 */
public class CustomTodayDoneExpandableAdapter extends BaseExpandableListAdapter implements GPrinterHandler.OnGPrinterReadyListener {
    String TAG = "CustomTodayDoneExpandableAdapter";

    Context context;

    private ArrayList<RowItem> rowItem;
    private ArrayList<RowItem> originalRowItem;

    private OnMoveUpListener onMoveUpListener;


    CustomTodayDoneExpandableAdapter(Context context, ArrayList<RowItem> rowItem) {

        this.context = context;                     // getActivity()
        this.rowItem = new ArrayList<>();
        this.rowItem.addAll(rowItem);
        this.originalRowItem = new ArrayList<>();
        this.originalRowItem.addAll(rowItem);
    }


    // 인터페이스
    public interface OnMoveUpListener {
        void onMoveUp(int pos);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {

            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_list_group, null);
        }

        LinearLayout layout_list_item_card_view = convertView.findViewById(R.id.layout_list_item_card_view);                // background change

        if (isExpanded) {

            layout_list_item_card_view.setBackgroundResource(R.drawable.bg_top_round_10_ffffff);
        } else {

            layout_list_item_card_view.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow);
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

            text_list_item_d_day.setTextColor(context.getResources().getColor(R.color.color_303030));
        } else {

            text_list_item_d_day.setTextColor(context.getResources().getColor(R.color.color_ff0000));
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

            text_list_item_tracking_no.setTextColor(context.getResources().getColor(R.color.color_363BE7));

            if (row_pos.getStat().equals("RE")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_pickup_reassigned));
            } else if (row_pos.getStat().equals("PF")) {

                text_list_item_pickup_state.setVisibility(View.VISIBLE);
                text_list_item_pickup_state.setText(context.getResources().getString(R.string.text_pickup_failed));
            } else {

                text_list_item_pickup_state.setVisibility(View.GONE);
            }
        }

        text_list_item_tracking_no.setText(row_pos.getShipping());
        text_list_item_address.setText(row_pos.getAddress());
        layout_list_item_menu_icon.setTag(row_pos.getShipping());
        text_list_item_receipt_name.setText(row_pos.getName());
        layout_list_item_delivery_outlet_info.setVisibility(View.GONE);

        text_list_item_desired_date_title.setText(context.getResources().getString(R.string.text_scanned_qty));
        text_list_item_desired_date.setText(row_pos.getQty());
        text_list_item_qty_title.setVisibility(View.GONE);
        text_list_item_qty.setVisibility(View.GONE);
        layout_list_item_request.setVisibility(View.GONE);

        //우측 메뉴 아이콘 클릭 이벤트  Quick Menu
        layout_list_item_menu_icon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, layout_list_item_menu_icon);
                popup.getMenuInflater().inflate(R.menu.quickmenu_pickup, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        //
                        switch (item.getItemId()) {
                            case R.id.menu_one:

                                String map_addr = row_pos.getAddress();

                                int split_index = map_addr.indexOf(")");
                                String split_addr = map_addr.substring(split_index + 1);

                                if (!split_addr.equals("")) {

                                    Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + split_addr.trim());
                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                    context.startActivity(it);
                                }
                                break;

                            case R.id.menu_up:

                                if (position > 0) {
                                    RowItem upItem = rowItem.remove(position);
                                    rowItem.add(position - 1, upItem);
                                    originalRowItem.clear();
                                    originalRowItem.addAll(rowItem);
                                    notifyDataSetChanged();

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

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
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

        if (!NetworkUtil.isNetworkAvailable(context)) {

            isAbleScanAddPage = false;
            AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
        }


        if ((route.equals("RPC") || route.equals("C2C")) && isAbleScanAddPage) {

            layout_list_item_child_done_pickup.setVisibility(View.GONE);
            btn_list_item_child_done_print_label.setVisibility(View.VISIBLE);
        } else {

            layout_list_item_child_done_pickup.setVisibility(View.VISIBLE);
            btn_list_item_child_done_print_label.setVisibility(View.GONE);

            Log.e("krm0219", "Scanned Qty :  " + scanned_qty);
            if (scanned_qty.equals("0")) {

                btn_list_item_child_done_take_back.setVisibility(View.GONE);
            } else {

                btn_list_item_child_done_take_back.setVisibility(View.VISIBLE);
            }
        }


        btn_list_item_child_done_print_label.setOnClickListener(v -> isConnectPortablePrint(tracking_no));


        btn_list_item_child_done_add_scan.setOnClickListener(v -> {

            Intent intent = new Intent(context, PickupScannedListActivity.class);
            intent.putExtra("pickupNo", tracking_no);
            intent.putExtra("applicant", applicant);
            intent.putExtra("buttonType", BarcodeType.PICKUP_ADD_SCAN);
            ((Activity) context).startActivityForResult(intent, List_TodayDoneFragment.REQUEST_ADD_SCAN);
        });

        // 2019.02 - Take Back
        btn_list_item_child_done_take_back.setOnClickListener(v -> {

            Intent intent = new Intent(context, PickupScannedListActivity.class);
            intent.putExtra("pickupNo", tracking_no);
            intent.putExtra("applicant", applicant);
            intent.putExtra("buttonType", BarcodeType.PICKUP_TAKE_BACK);
            ((Activity) context).startActivityForResult(intent, List_TodayDoneFragment.REQUEST_TAKE_BACK);
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

    private void AlertShow(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(context);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 닫기

                    }
                });
        alert_internet_status.show();
    }

    // NOTIFICATION.  Print
    private void isConnectPortablePrint(final String tracking_no) {

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
        }

        final String deviceAddress = getBluetoothPrinterAddress();
        if (!deviceAddress.equals("")) {  // 프린터 연결됨     // 출력시작

            Toast.makeText(context, context.getResources().getString(R.string.msg_wait_while_print_job), Toast.LENGTH_SHORT).show();
            printLabel(tracking_no, "isConnectPortablePrint");
        } else {

            checkBluetoothState(tracking_no);
        }
    }

    private void checkBluetoothState(String tracking_no) {
        Log.e("print", TAG + "  checkBluetoothState");

        // Bluetooth 지원 여부 확인
        GPrinterData.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth 지원하지 않음
        if (GPrinterData.mBluetoothAdapter == null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_SHORT).show();
        } else {

            // Bluetooth 지원 && 비활성화 상태
            if (!GPrinterData.mBluetoothAdapter.isEnabled()) {

                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) context).startActivityForResult(enableIntent, GPrinterData.REQUEST_ENABLE_BT);
            } else {

                // Bluetooth 지원 && 활성화 상태
                registerPrintReceiver(tracking_no);
            }
        }
    }

    private void registerPrintReceiver(String tracking_no) {

        GPrinterData.printerConnManagerList = new ArrayList<>();

        // 전역 변수로 프린터 세팅 해 놓고 커넥션 되면 바로 프린터 되도록 하기
        GPrinterData.isGPrint = true;
        GPrinterData.TRACKING_NO = tracking_no;

        try {

            registerReceiver();
            discoveryDevice();
        } catch (Exception e) {

            Log.e("Exception", TAG + "  registerPrintReceiver Exception : " + e.toString());
        }
    }

    private void registerReceiver() {

        if (GPrinterData.printerReceiver == null) {

            GPrinterData.printerReceiver = new GPrinterBroadcastReceiver(context, ((Activity) context));

            // 인텐트 동록
            IntentFilter filter = new IntentFilter();
            filter.addAction(GPrinterData.ACTION_CONN_STATE);  // action_connect_state
            filter.addAction(BluetoothDevice.ACTION_FOUND);                 // 기기 검색됨
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);   // 기기 검색 종료
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);      // 연결 끊김 확인
            context.registerReceiver(GPrinterData.printerReceiver, filter);
        }

        // handler 만들기
        if (GPrinterData.gPrinterHandler == null) {
            Activity myActivity = (Activity) context;
            GPrinterData.gPrinterHandler = new GPrinterHandler(context, myActivity, this);
        }
    }

    private void discoveryDevice() {

        // If we're already discovering, stop it
        if (GPrinterData.mBluetoothAdapter.isDiscovering()) {
            GPrinterData.mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        GPrinterData.mBluetoothAdapter.startDiscovery();
    }

    private void printLabel(String tracking_no, String where) {

        if (GPrinterData.printerConnManagerList == null || GPrinterData.printerConnManagerList.size() == 0 || !GPrinterData.printerConnManagerList.get(0).getConnState()) {

            Log.e("print", TAG + "  printLabel : " + tracking_no + "  return");
            return;
        }

        // 위에 if 문은 아마 그냥 통과 될 것 왜냐면 커넥션을 자동으로 하고 바로 프린터 버튼 누른 것처럼 trigger 보완 소스 넣고 있음
        //  handler 에서 메시지 받으면 다시 버튼 클릭을 interface 함수로 호출 하고 있음 - onStartGprinter
        if (GPrinterData.printerConnManagerList.get(0).getCurrentPrinterCommand() == PrinterConnManager.PrinterCommand.TSC) {

//            String opId = SharedPreferencesHelper.getSigninOpID(context);
            String opId = MyApplication.preferences.getUserId();
            Log.e("print", TAG + "  printLabel Command : " + GPrinterData.printerConnManagerList.get(0).getCurrentPrinterCommand() + " / " + tracking_no);

            new CnRPickupInfoGetHelper.Builder(context, opId, tracking_no)
                    .setOnCnRPrintDataEventListener(new CnRPickupInfoGetHelper.OnCnRPrintDataEventListener() {

                        @Override
                        public void onPostAssignResult(PrintDataResult stdResult) {
                            try {
                                if (stdResult != null) {
                                    if (stdResult.getResultCode() == 0) {

                                        Log.e("print", TAG + "  sendLabel");
                                        // TEST.
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

    private String getBluetoothPrinterAddress() {

        String address = "";
        PrinterConnManager printerConnManager;

        if (GPrinterData.printerConnManagerList != null && GPrinterData.printerConnManagerList.size() > 0) {

            printerConnManager = GPrinterData.printerConnManagerList.get(0);
            address = printerConnManager.getMacAddress();
        }

        return address;
    }

    private void sendLabel(PrintDataResult stdResult) {

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
        tsc.add1DBarcode(20, 0, LabelCommand.BARCODETYPE.CODE128, 80, LabelCommand.READABEL.EANBEL,
                LabelCommand.ROTATION.ROTATION_0, result.getInvoiceNo());
//        tsc.add1DBarcode(20, 0, LabelCommand.BARCODETYPE.CODE39S, 80, LabelCommand.READABEL.EANBEL,
//                LabelCommand.ROTATION.ROTATION_0, 2, 5, result.getInvoiceNo());
        //    tsc.addQRCode(450, 0, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, result.getInvoiceNo());
        Bitmap bitmap = DataUtil.stringToDataMatrix(result.getInvoiceNo());
        tsc.addBitmap(450, 0, 100, bitmap);

        // 두번째 row
        ArrayList<String> list = cutString(result.getCustName(), 1);
        String consignee = list.get(0);

        tsc.addText(15, 130, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "To(consignee)");
        tsc.addReverse(10, 115, 186, 50);
        tsc.addBox(195, 115, 565, 165, 1);
        tsc.addText(215, 130, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, consignee);

        // 세번째 row
        tsc.addBox(10, 165, 195, 265, 1);
        tsc.addText(35, 170, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "Postal code");
        tsc.addText(55, 200, LabelCommand.FONTTYPE.FONT_3, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, result.getZipCode());
        tsc.addText(25, 235, LabelCommand.FONTTYPE.FONT_3, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, result.getDeliveryCouse());
        tsc.addErase(194, 165, 1, 100);

        String address = result.getBackaddress() + " " + result.getFrontAddress();
        //address = "#06-189SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5";
        list = cutString(address, 3);

        tsc.addBox(195, 165, 565, 265, 1);
        for (int i = 0; i < list.size(); i++) {

            int positionY = 175 + (30 * i);

            tsc.addText(215, positionY, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                    LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, list.get(i));
        }

        // 네번째 row
        tsc.addBox(10, 265, 565, 305, 1);
        tsc.addText(35, 275, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "(" + result.getHpNo() + "/" + result.getTelNo() + ")");

        // 마지막 row
        list = cutString(result.getSellerShop(), 1);
        String seller_shop_nm = list.get(0);

        tsc.addText(15, 320, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "From(shipper)");
        tsc.addReverse(10, 305, 186, 50);
        tsc.addBox(195, 305, 565, 355, 1);
        tsc.addText(215, 320, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, seller_shop_nm);

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

    private ArrayList<String> cutString(String originStr, int lineNum) {

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
    }


    @Override
    public void onStartGprinter(String tracking_no, String mac_addr) {
        // Log.e("print", TAG + "  onStartGprinter > " + mac_addr);

        GPrinterData.TEMP_TRACKING_NO = tracking_no;

        if (mac_addr.equals("")) {

            isConnectPortablePrint(tracking_no);
        } else {

            printLabel(tracking_no, "onStartGprinter");
        }
    }
}