package com.giosis.library.main.submenu;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.giosis.library.R;
import com.giosis.library.bluetooth.BluetoothClass;
import com.giosis.library.list.ChildItem;
import com.giosis.library.list.ListInProgressAdapter;
import com.giosis.library.list.RowItem;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.NDSpinner;
import com.giosis.library.util.PermissionActivity;
import com.giosis.library.util.PermissionChecker;
import com.giosis.library.util.Preferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class RpcListActivity extends CommonActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, ListInProgressAdapter.OnMoveUpListener {
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};
    String TAG = "RpcListActivity";
    //
    FrameLayout layout_top_back;
    TextView text_top_title;
    //
    InputMethodManager inputMethodManager;
    private SearchView searchview_list;
    private EditText edit_list_searchview;
    private FrameLayout layout_list_sort;
    private NDSpinner spinner_list_sort;
    private ExpandableListView exlist_card_list;
    private String opID;
    private boolean isOpen = false;
    private String orderby = "zip_code asc";
    private ListInProgressAdapter adapter;
    private ArrayList<RowItem> rowItems;
    //
    private PermissionChecker checker;

    BluetoothClass bluetoothClass;

    public static long diffOfDate(String begin) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date beginDate = formatter.parse(begin);
        Date endDate = new Date();

        long diff = endDate.getTime() - beginDate.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpc_list);

        Log.e("krm0219", "HERe");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        bluetoothClass = new BluetoothClass(this);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);


        searchview_list = findViewById(R.id.searchview_list);
        layout_list_sort = findViewById(R.id.layout_list_sort);
        spinner_list_sort = findViewById(R.id.spinner_list_sort);
        exlist_card_list = findViewById(R.id.exlist_card_list);


        //
        layout_top_back.setOnClickListener(v -> finish());
        text_top_title.setText(R.string.text_rpc_change_driver);
        opID = Preferences.INSTANCE.getUserId();

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        // Search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchview_list.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchview_list.setQueryHint("Search");
        searchview_list.setOnQueryTextListener(this);
        searchview_list.setOnCloseListener(this);

        int id = searchview_list.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        edit_list_searchview = searchview_list.findViewById(id);
        edit_list_searchview.setTextColor(getResources().getColor(R.color.color_8f8f8f));
        edit_list_searchview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_26px));
        edit_list_searchview.setHintTextColor(getResources().getColor(R.color.color_8f8f8f));


        layout_list_sort.setOnClickListener(v -> spinner_list_sort.performClick());


        // 스피너  sort by 초기화
        int OrderBySeq = Preferences.INSTANCE.getSortIndex();

        ArrayList<String> spinnerlist = new ArrayList<>(Arrays.asList("Postal Code : Low to High",
                "Postal Code : High to Low",
                "Tracking No : Low to High",
                "Tracking No : High to Low",
                "Name : Low to High",
                "Name : High to Low"));

        final String[] orderbyQuery = {
                "zip_code asc",
                "zip_code desc",
                "invoice_no asc",
                "invoice_no desc",
                "rcv_nm asc",
                "rcv_nm desc"
        };

        if (OrderBySeq <= 6) {

            Preferences.INSTANCE.setSortIndex(0);
            OrderBySeq = 0;
        }

        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerlist);

        spinner_list_sort.setPrompt("Sort by"); // 스피너 제목
        spinner_list_sort.setAdapter(adapter_spinner);
        spinner_list_sort.setSelection(OrderBySeq);
        orderby = orderbyQuery[OrderBySeq];

        spinner_list_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Preferences.INSTANCE.setSortIndex(position);
                orderby = orderbyQuery[position];
                onResume();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        rowItems = new ArrayList<>();
        adapter = new ListInProgressAdapter(rowItems, bluetoothClass);


        exlist_card_list.setOnGroupCollapseListener(groupPosition -> isOpen = false);

        exlist_card_list.setOnGroupExpandListener(groupPosition -> {

            int groupCount = adapter.getGroupCount();

            // 한 그룹을 클릭하면 나머지 그룹들은 닫힌다.
            for (int i = 0; i < groupCount; i++) {
                if (!(i == groupPosition))
                    exlist_card_list.collapseGroup(i);
            }

            isOpen = true;
        });

        exlist_card_list.setOnChildClickListener((parent, v, groupPosition, childPosition, id1) -> false);

        exlist_card_list.setOnGroupClickListener((parent, v, groupPosition, id12) -> false);


        //
        checker = new PermissionChecker(this);

        if (checker.lacksPermissions(PERMISSIONS)) {

            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, PERMISSIONS);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {

            inputMethodManager.hideSoftInputFromWindow(edit_list_searchview.getWindowToken(), 0);
            edit_list_searchview.setText("");
            edit_list_searchview.clearFocus();
        } catch (Exception e) {

            Log.e("Exception", "search init  Exception : " + e.toString());
        }


        rowItems = getSortList(orderby);
        adapter = new ListInProgressAdapter(rowItems, bluetoothClass);
        adapter.setOnMoveUpListener(this);
        exlist_card_list.setAdapter(adapter);
        adapter.setSorting(rowItems);


        int groupCount = adapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            exlist_card_list.collapseGroup(i);
        }
    }

    @Override
    public boolean onClose() {

        adapter.filterData("");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        adapter.filterData(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        try {

            adapter.filterData(newText);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onQueryTextChange Exception : " + e.toString());
        }
        return false;
    }

    @Override
    public void onMoveUp(int pos) {

        if (isOpen) {
            exlist_card_list.expandGroup(pos);
        }
    }

    // NOTIFICATION. 기본 정렬
    private ArrayList<RowItem> getSortList(String orderby) {

        ArrayList<RowItem> resultArrayList = new ArrayList<>();


        Cursor cs = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and route='RPC' and reg_id='" + opID + "' order by " + orderby);
        // TEST.
        // Cursor cs = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and route='QSM' and reg_id='" + opID + "' order by " + orderby);


        if (cs.moveToFirst()) {
            do {

                ArrayList<ChildItem> childItems = new ArrayList<>();
                ChildItem child = new ChildItem();
                child.setHp(cs.getString(cs.getColumnIndex("hp_no")));
                child.setTel(cs.getString(cs.getColumnIndex("tel_no")));
                child.setStat(cs.getString(cs.getColumnIndex("stat")));
                child.setStatMsg(cs.getString(cs.getColumnIndex("driver_memo")));
                child.setStatReason(cs.getString(cs.getColumnIndex("fail_reason")));
                child.setSecretNoType(cs.getString(cs.getColumnIndex("secret_no_type")));
                child.setSecretNo(cs.getString(cs.getColumnIndex("secret_no")));
                childItems.add(child);

                long delay = 0;
                if (cs.getString(cs.getColumnIndex("delivery_dt")) != null && !cs.getString(cs.getColumnIndex("delivery_dt")).equals("")) {
                    try {

                        delay = diffOfDate(cs.getString(cs.getColumnIndex("delivery_dt")));
                    } catch (Exception e) {

                        Log.e("Exception", TAG + "  diffOfDate Exception : " + e.toString());
                    }
                }

                // Route
                String routeType = cs.getString(cs.getColumnIndex("route"));
                //배송 타입
                String deliveryType = cs.getString(cs.getColumnIndex("type"));
                String rcv_name = "";
                if (deliveryType.equals("D")) {
                    rcv_name = cs.getString(cs.getColumnIndex("rcv_nm")); //구매자
                } else if (deliveryType.equals("P")) {
                    rcv_name = cs.getString(cs.getColumnIndex("req_nm")); //픽업 요청 셀러
                }

                RowItem rowitem = new RowItem(cs.getString(cs.getColumnIndex("contr_no")),
                        "D+" + delay,
                        cs.getString(cs.getColumnIndex("invoice_no")),
                        rcv_name,
                        "(" + cs.getString(cs.getColumnIndex("zip_code")) + ") "
                                + cs.getString(cs.getColumnIndex("address")),
                        cs.getString(cs.getColumnIndex("rcv_request")),
                        deliveryType,
                        routeType,
                        cs.getString(cs.getColumnIndex("sender_nm")),
                        cs.getString(cs.getColumnIndex("desired_date")),
                        cs.getString(cs.getColumnIndex("req_qty")),
                        cs.getString(cs.getColumnIndex("self_memo")),
                        cs.getDouble(cs.getColumnIndex("lat")),
                        cs.getDouble(cs.getColumnIndex("lng")),
                        cs.getString(cs.getColumnIndex("stat")),
                        cs.getString(cs.getColumnIndex("cust_no")),
                        cs.getString(cs.getColumnIndex("partner_id")),
                        cs.getString(cs.getColumnIndex("secure_delivery_yn")),
                        cs.getString(cs.getColumnIndex("parcel_amount")),
                        cs.getString(cs.getColumnIndex("currency"))
                );
//                rowitem.setZip_code(cs.getString(cs.getColumnIndex("zip_code")));

                // NOTIFICATION.  2019.10  invoice와 같은지 체크! 같으면 저장 x
                if (deliveryType.equals("P")) {
                    if (cs.getString(cs.getColumnIndex("invoice_no")).equals(cs.getString(cs.getColumnIndex("partner_ref_no")))) {

                        rowitem.setRef_pickup_no("");
                    } else {

                        Log.e("krm0219", "Ref. Pickup > " + cs.getString(cs.getColumnIndex("invoice_no")) + " / " + cs.getString(cs.getColumnIndex("partner_ref_no")));
                        rowitem.setRef_pickup_no(cs.getString(cs.getColumnIndex("partner_ref_no")));
                    }
                }


                if (deliveryType.equals("D")) {
                    rowitem.setOrder_type_etc(cs.getString(cs.getColumnIndex("order_type_etc")));
                }

                if (routeType.equals("RPC")) {
                    rowitem.setDesired_time(cs.getString(cs.getColumnIndex("desired_time")));
                }

                rowitem.setItems(childItems);


                // k. Outlet Delivery 경우 같은 지점은 하나만 나오도록 수정
                if (0 < resultArrayList.size()) {
                    boolean isRegisteredRoute = false;

                    for (int i = 0; i < resultArrayList.size(); i++) {
                        if (deliveryType.equalsIgnoreCase("D")) {
                            if (routeType.contains("7E") || routeType.contains("FL")) {
                                // ex. 7E 001 name1, 7E 002 name2  / ex. FL FLA10001 mrtA, FL FLS10001 mrtB

                                String[] routeSplit = routeType.split(" ");

                                if (1 < routeSplit.length) {

                                    String routeNumber = routeSplit[0] + " " + routeSplit[1];
                                    if (resultArrayList.get(i).getType().equals("D") && resultArrayList.get(i).getRoute().contains(routeNumber)) {
                                        isRegisteredRoute = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!isRegisteredRoute) {

                        resultArrayList.add(rowitem);
                    }
                } else {

                    resultArrayList.add(rowitem);
                }
            } while (cs.moveToNext());
        }


        // k. Outlet 정보 추가
        for (int i = 0; i < resultArrayList.size(); i++) {
            if (resultArrayList.get(i).getType().equalsIgnoreCase("D")) {

                resultArrayList.get(i).setOutlet_company(resultArrayList.get(i).getRoute());
                if (resultArrayList.get(i).getRoute().contains("7E") || resultArrayList.get(i).getRoute().contains("FL")) {

                    String[] routeSplit = resultArrayList.get(i).getRoute().split(" ");

                    if (1 < routeSplit.length) {

                        String routeNumber = routeSplit[0] + " " + routeSplit[1];
                        Cursor cursor = DatabaseHelper.getInstance().get("SELECT count(*) FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and route LIKE '%" + routeNumber + "%'");
                        cursor.moveToFirst();
                        int count = cursor.getInt(0);


                        StringBuilder sb = new StringBuilder();

                        for (int j = 2; j < routeSplit.length; j++) {

                            sb.append(routeSplit[j]);
                            sb.append(" ");
                        }

                        resultArrayList.get(i).setOutlet_company(routeSplit[0]);
                        resultArrayList.get(i).setOutlet_store_code(routeSplit[1]);
                        resultArrayList.get(i).setOutlet_store_name(sb.toString().trim());
                        resultArrayList.get(i).setOutlet_qty(count);
                    }
                }
            } else {        // Pickup

                resultArrayList.get(i).setOutlet_company(resultArrayList.get(i).getRoute());
                if (resultArrayList.get(i).getRoute().contains("7E") || resultArrayList.get(i).getRoute().contains("FL")) {

                    String[] routeSplit = resultArrayList.get(i).getRoute().split(" ");

                    if (1 < routeSplit.length) {

                        StringBuilder sb = new StringBuilder();

                        for (int j = 2; j < routeSplit.length; j++) {

                            sb.append(routeSplit[j]);
                            sb.append(" ");
                        }

                        resultArrayList.get(i).setOutlet_company(routeSplit[0]);
                        resultArrayList.get(i).setOutlet_store_code(routeSplit[1]);
                        resultArrayList.get(i).setOutlet_store_name(sb.toString().trim());
                    }
                }
            }
        }

        return resultArrayList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothClass.clearBluetoothAdapter();
    }
}