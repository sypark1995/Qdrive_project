package com.giosis.util.qdrive.list;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.pickup.PickupAssignResult;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterData;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/***************
 * @author jtpark_qxpress
 * In-Progress Tab Fragment
 * Expandable ListView
 */
public class InProgressListFragment extends Fragment implements OnQueryTextListener, OnCloseListener, InProgressExpandableListAdapter.OnMoveUpListener {
    String TAG = "List_InProgressFragment";

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};
    //

    View view;

    SearchView searchview_list;
    EditText edit_list_searchview;
    FrameLayout layout_list_sort;
    Spinner spinner_list_sort;

    ExpandableListView exlist_card_list;


    //
    Context context;

    // Search
    final String[] orderbyQuery = {
            "zip_code asc",
            "zip_code desc",
            "invoice_no asc",
            "invoice_no desc",
            "rcv_nm asc",
            "rcv_nm desc",
            "seq_orderby asc"
    };


    //
    private String opID;
    OnCountListener mCountCallback;
    OnTodayDoneCountListener onTodayDoneCountListener;
    private boolean isOpen = false;

    private ArrayList<String> spinnerlist;
    private String orderby = "zip_code asc";

    private InProgressExpandableListAdapter adapter;

    private ArrayList<RowItem> rowItems;
    private ArrayList<ChildItem> childItems;
    private int list_position;

    DatabaseHelper dbHelper;


    public InProgressListFragment() {
        super();
    }

    //리스트 카운트를 갱신하기 위한 인터페이스
    public interface OnCountListener {
        public void onCountRefresh(int count);
    }

    public interface OnTodayDoneCountListener {
        public void onTodayDoneCountRefresh(int count);
    }

    //부모 Activity와 통신을 하기 위한 연결
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = null;

        if (context instanceof Activity) {

            activity = (Activity) context;
        }

        try {

            mCountCallback = (OnCountListener) activity;
            onTodayDoneCountListener = (OnTodayDoneCountListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCountListener");
        }
    }

    //Adapter 인터페이스 구현, Custom Order 퀵액션에서 Up/Down 이동 시 콜백
    @Override
    public void onMoveUp(int pos) {

        orderby = "seq_orderby asc";

        spinner_list_sort.setSelection(6);

        if (isOpen) {
            exlist_card_list.expandGroup(pos);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PermissionChecker checker = new PermissionChecker(getActivity());

        if (checker.lacksPermissions(PERMISSIONS)) {

            PermissionActivity.startActivityForResult(getActivity(), PERMISSION_REQUEST_CODE, PERMISSIONS);
            getActivity().overridePendingTransition(0, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("krm0219", TAG + "  onCreateView  ");
        context = getActivity();
        opID = MyApplication.preferences.getUserId();


        view = inflater.inflate(R.layout.fragment_inprogress, container, false);

        // ID 바꾸기!
        searchview_list = view.findViewById(R.id.searchview_list);
        layout_list_sort = view.findViewById(R.id.layout_list_sort);
        spinner_list_sort = view.findViewById(R.id.spinner_list_sort);
        exlist_card_list = view.findViewById(R.id.exlist_card_list);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.e("krm0219", TAG + "  onActivityCreated");
        dbHelper = DatabaseHelper.getInstance();

        // Search
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchview_list.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchview_list.setQueryHint(context.getResources().getString(R.string.text_search));
        searchview_list.setOnQueryTextListener(this);
        searchview_list.setOnCloseListener(this);

        int id = searchview_list.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        edit_list_searchview = searchview_list.findViewById(id);
        edit_list_searchview.setTextColor(getResources().getColor(R.color.color_8f8f8f));
        edit_list_searchview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_26px));
        edit_list_searchview.setHintTextColor(getResources().getColor(R.color.color_8f8f8f));

        layout_list_sort.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner_list_sort.performClick();
            }
        });


        // 스피너  sort by 초기화
        int OrderBySeq = MyApplication.preferences.getSortIndex();
        Log.e("krm0219", "OrderBy : " + OrderBySeq);

        spinnerlist = new ArrayList<>(Arrays.asList(
                getResources().getString(R.string.text_sort_postal_code_asc),
                getResources().getString(R.string.text_sort_postal_code_desc),
                getResources().getString(R.string.text_sort_tracking_no_asc),
                getResources().getString(R.string.text_sort_tracking_no_desc),
                getResources().getString(R.string.text_sort_name_asc),
                getResources().getString(R.string.text_sort_name_desc)));


        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, spinnerlist);

        spinner_list_sort.setPrompt("Sort by"); // 스피너 제목
        spinner_list_sort.setAdapter(adapter_spinner);
        spinner_list_sort.setSelection(OrderBySeq);
        orderby = orderbyQuery[OrderBySeq];


        spinner_list_sort.post(new Runnable() {
            @Override
            public void run() {

                spinner_list_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        MyApplication.preferences.setSortIndex(position);
                        orderby = orderbyQuery[position];
                        onResume();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });


        rowItems = new ArrayList<>();
        adapter = new InProgressExpandableListAdapter(getActivity(), rowItems);

        exlist_card_list.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

                isOpen = false;
            }

        });

        exlist_card_list.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                DataUtil.inProgressListPosition = groupPosition;
                int groupCount = adapter.getGroupCount();

                // 한 그룹을 클릭하면 나머지 그룹들은 닫힌다.
                for (int i = 0; i < groupCount; i++) {
                    if (!(i == groupPosition))
                        exlist_card_list.collapseGroup(i);
                }

                isOpen = true;
            }
        });

        exlist_card_list.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                return false;
            }
        });

        exlist_card_list.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                return false;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.e("krm0219", TAG + "  onResume");

        try {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_list_searchview.getWindowToken(), 0);

            edit_list_searchview.setText("");
            edit_list_searchview.clearFocus();
        } catch (Exception e) {
            Log.e("Exception", "search init  Exception : " + e.toString());
        }

        // Data Porting
        opID = MyApplication.preferences.getUserId();

        dbHelper = DatabaseHelper.getInstance();
        Cursor cs = dbHelper.get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and reg_id='" + opID + "' order by " + orderby);

        rowItems = new ArrayList<>();
        ChildItem child;

        if (cs.moveToFirst()) {
            do {

                childItems = new ArrayList<>();
                child = new ChildItem();
                child.setHp(cs.getString(cs.getColumnIndex("hp_no")));
                child.setTel(cs.getString(cs.getColumnIndex("tel_no")));
                child.setStat(cs.getString(cs.getColumnIndex("stat")));
                child.setStatMsg(cs.getString(cs.getColumnIndex("driver_memo")));
                child.setStatReason(cs.getString(cs.getColumnIndex("fail_reason")));
                child.setSecretNoType(cs.getString(cs.getColumnIndex("secret_no_type")));
                child.setSecretNo(cs.getString(cs.getColumnIndex("secret_no")));
                childItems.add(child);

                long delay = 0;
                if (cs.getString(cs.getColumnIndex("delivery_dt")) != null && cs.getString(cs.getColumnIndex("delivery_dt")) != "") {
                    try {

                        delay = diffOfDate(cs.getString(cs.getColumnIndex("delivery_dt")));
                    } catch (Exception e) {

                        delay = 0;
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

                // NOTIFICATION.  19/10 - invoice 와 같은지 체크! 같으면 저장 x
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
                if (0 < rowItems.size()) {
                    boolean isRegisteredRoute = false;

                    for (int i = 0; i < rowItems.size(); i++) {
                        if (deliveryType.equalsIgnoreCase("D")) {
                            if (routeType.contains("7E") || routeType.contains("FL")) {
                                // ex. 7E 001 name1, 7E 002 name2  / ex. FL FLA10001 mrtA, FL FLS10001 mrtB

                                String[] routeSplit = routeType.split(" ");

                                if (1 < routeSplit.length) {

                                    String routeNumber = routeSplit[0] + " " + routeSplit[1];
                                    if (rowItems.get(i).getType().equals("D") && rowItems.get(i).getRoute().contains(routeNumber)) {
                                        isRegisteredRoute = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!isRegisteredRoute) {

                        rowItems.add(rowitem);
                    }
                } else {

                    rowItems.add(rowitem);
                }
            } while (cs.moveToNext());
        }


        // k. Outlet 정보 추가
        for (int i = 0; i < rowItems.size(); i++) {

            if (rowItems.get(i).getType().equalsIgnoreCase("D")) {

                rowItems.get(i).setOutlet_company(rowItems.get(i).getRoute());
                if (rowItems.get(i).getRoute().contains("7E") || rowItems.get(i).getRoute().contains("FL")) {

                    String[] routeSplit = rowItems.get(i).getRoute().split(" ");

                    if (1 < routeSplit.length) {

                        String routeNumber = routeSplit[0] + " " + routeSplit[1];
                        Cursor cursor = dbHelper.get("SELECT count(*) FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and route LIKE '%" + routeNumber + "%'");
                        cursor.moveToFirst();
                        int count = cursor.getInt(0);


                        StringBuilder sb = new StringBuilder();

                        for (int j = 2; j < routeSplit.length; j++) {

                            sb.append(routeSplit[j]);
                            sb.append(" ");
                        }

                        rowItems.get(i).setOutlet_company(routeSplit[0]);
                        rowItems.get(i).setOutlet_store_code(routeSplit[1]);
                        rowItems.get(i).setOutlet_store_name(sb.toString().trim());
                        rowItems.get(i).setOutlet_qty(count);
                    }
                }
            } else {        // Pickup

                Log.e("krm0219", rowItems.get(i).getType() + " / " + rowItems.get(i).getRoute() + " / " + rowItems.get(i).getShipping());
                rowItems.get(i).setOutlet_company(rowItems.get(i).getRoute());
                if (rowItems.get(i).getRoute().contains("7E") || rowItems.get(i).getRoute().contains("FL")) {

                    String[] routeSplit = rowItems.get(i).getRoute().split(" ");

                    if (1 < routeSplit.length) {

                        StringBuilder sb = new StringBuilder();

                        for (int j = 2; j < routeSplit.length; j++) {

                            sb.append(routeSplit[j]);
                            sb.append(" ");
                        }

                        rowItems.get(i).setOutlet_company(routeSplit[0]);
                        rowItems.get(i).setOutlet_store_code(routeSplit[1]);
                        rowItems.get(i).setOutlet_store_name(sb.toString().trim());
                    }
                }
            }
        }


        adapter = new InProgressExpandableListAdapter(getActivity(), rowItems);
        adapter.setOnMoveUpListener(this);
        exlist_card_list.setAdapter(adapter);

        adapter.setSorting(rowItems);

        int groupCount = adapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            exlist_card_list.collapseGroup(i);
        }

        try {

            Log.e("krm0219", groupCount + "   In Progress List Position : " + DataUtil.inProgressListPosition);

            if (groupCount <= DataUtil.inProgressListPosition) {
                DataUtil.inProgressListPosition = 0;
            }

            exlist_card_list.setSelectedGroup(DataUtil.inProgressListPosition);
            if (DataUtil.inProgressListPosition != 0) {

                exlist_card_list.expandGroup(DataUtil.inProgressListPosition);
            }
        } catch (Exception e) {

        }


        //카운트 전달
        mCountCallback.onCountRefresh(groupCount);

        // 2019.01  krm0219
        // LIST 들어갈 때 TODAY DONE Count 표시하기 위함.
        // ViewPage 특성상 TODAY DONE 페이지는 처음에 호출되지 않아서 0 으로 표시되어있음.

        new TodayDonePickupListDownloadHelper.Builder(getActivity(), opID)
                .setOnTodayDonePickupOrderDownloadEventListener(new TodayDonePickupListDownloadHelper.OnTodayDonePickupOrderDownloadEventListener() {

                    @Override
                    public void onTodayDonePickupOrderDownloadResult(PickupAssignResult result) {

                        int todayDoneCount = result.getResultObject().size();
                        onTodayDoneCountListener.onTodayDoneCountRefresh(todayDoneCount);
                    }
                }).build().execute();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (0 < list_position) {
                    dbHelper = DatabaseHelper.getInstance();

                    //Delivered 성공 시 리스트에서 삭제
                    rowItems.remove(list_position);
                    adapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                Log.e("krm0219", TAG + "   onActivityResult  PERMISSIONS_GRANTED");
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();

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

        if (GPrinterData.gPrinterHandler != null) {

            GPrinterData.gPrinterHandler = null;
        }

        try {

            if (GPrinterData.printerReceiver != null) {

                getActivity().unregisterReceiver(GPrinterData.printerReceiver);
                GPrinterData.printerReceiver = null;
            }
        } catch (Exception e) {

            Log.w("new", "PageInProgress onPause Exception : " + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (GPrinterData.mBluetoothAdapter != null) {
            GPrinterData.mBluetoothAdapter.cancelDiscovery();
        }

        if (GPrinterData.printerConnManagerList != null) {
            for (int i = 0; i < GPrinterData.printerConnManagerList.size(); i++) {
                GPrinterData.printerConnManagerList.get(i).closePort();
            }
            GPrinterData.printerConnManagerList = null;
        }

        if (GPrinterData.gPrinterHandler != null) {

            GPrinterData.gPrinterHandler = null;
        }

        try {

            if (GPrinterData.printerReceiver != null) {

                getActivity().unregisterReceiver(GPrinterData.printerReceiver);
                GPrinterData.printerReceiver = null;
            }
        } catch (Exception e) {

            Log.w("new", "PageInProgress onDestroy Exception : " + e.toString());
        }
    }

    public static long diffOfDate(String begin) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date beginDate = formatter.parse(begin);
        Date endDate = new Date();

        long diff = endDate.getTime() - beginDate.getTime();

        return diff / (24 * 60 * 60 * 1000);
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
    public boolean onQueryTextChange(String query) {

        Log.e("krm0219", "onQueryTextChange  " + query);
        adapter.filterData(query);
        return false;
    }
}