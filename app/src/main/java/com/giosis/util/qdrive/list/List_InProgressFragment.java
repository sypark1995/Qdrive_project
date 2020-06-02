package com.giosis.util.qdrive.list;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterData;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.NDSpinner;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/***************
 * @author jtpark_qxpress
 * In-Progress Tab Fragment
 * @editor krm0219
 */
public class List_InProgressFragment extends Fragment implements OnQueryTextListener, OnCloseListener, CustomExpandableAdapter.OnMoveUpListener {
    String TAG = "List_InProgressFragment";

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};
    //

    View view;

    private SearchView searchview_list;
    private EditText edit_list_searchview;
    private FrameLayout layout_list_sort;
    private NDSpinner spinner_list_sort;

    private ExpandableListView exlist_card_list;
    private ExpandableListView exlist_smart_route;


    //
    Context context;
    private ProgressDialog progressDialog;


    //
    private String opID;
    private OnCountListener mCountCallback;
    private OnTodayDoneCountListener onTodayDoneCountListener;
    private boolean isOpen = false;

    private String selectedSort;

    private CustomExpandableAdapter adapter;
    private ArrayList<RowItem> rowItems;

    private DatabaseHelper dbHelper;

    // 2019.07  Smart Route
    private SharedPreferences sharedPreferences;
    private ArrayList<SmartRouteResult.RouteMaster> routeMasterArrayList;
    private SmartRouteExpandableAdapter smartRouteExpandableAdapter;

    // 2020 Sort - Nearer
    private int check = 0;
    private GPSTrackerManager gpsTrackerManager;
    private boolean gpsEnable = false;
    private double latitude = 0;
    private double longitude = 0;

    private ProgressBar progress_in_progress;

    private String[] orderbyQuery = {
            "zip_code asc",
            "zip_code desc",
            "invoice_no asc",
            "invoice_no desc",
            "rcv_nm asc",
            "rcv_nm desc"
            , "Smart Route"
            , "Nearer"
    };


    public List_InProgressFragment() {
        super();
    }

    public interface OnCountListener {
        void onCountRefresh(int count);
    }

    public interface OnTodayDoneCountListener {
        void onTodayDoneCountRefresh(int count);
    }

    void setSortSpinner() {

        spinner_list_sort.setSelection(0);
    }

    //부모 Activity 와 통신을 하기 위한 연결
    @Override
    public void onAttach(@NotNull Context context) {
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


    @Override
    public void onMoveUp(int pos) {
        if (isOpen) {
            exlist_card_list.expandGroup(pos);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        PermissionChecker checker = new PermissionChecker(getActivity());

        if (checker.lacksPermissions(PERMISSIONS)) {

            isPermissionTrue = false;
            PermissionActivity.startActivityForResult(getActivity(), PERMISSION_REQUEST_CODE, PERMISSIONS);
            getActivity().overridePendingTransition(0, 0);
        } else {

            isPermissionTrue = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();
        progressDialog = new ProgressDialog(getActivity());
        opID = SharedPreferencesHelper.getSigninOpID(getActivity());
        dbHelper = DatabaseHelper.getInstance();

        view = inflater.inflate(R.layout.fragment_inprogress, container, false);

        searchview_list = view.findViewById(R.id.searchview_list);
        layout_list_sort = view.findViewById(R.id.layout_list_sort);
        spinner_list_sort = view.findViewById(R.id.spinner_list_sort);

        exlist_card_list = view.findViewById(R.id.exlist_card_list);
        exlist_smart_route = view.findViewById(R.id.exlist_smart_route);

        progress_in_progress = view.findViewById(R.id.progress_in_progress);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.e("krm0219", TAG + "  onActivityCreated");
        sharedPreferences = getActivity().getSharedPreferences(DataUtil.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

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


        ArrayList<String> sortArrayList = new ArrayList<>(Arrays.asList(
                getResources().getString(R.string.text_sort_postal_code_asc),
                getResources().getString(R.string.text_sort_postal_code_desc),
                getResources().getString(R.string.text_sort_tracking_no_asc),
                getResources().getString(R.string.text_sort_tracking_no_desc),
                getResources().getString(R.string.text_sort_name_asc),
                getResources().getString(R.string.text_sort_name_desc)
                , getResources().getString(R.string.text_smart_route)
                , getResources().getString(R.string.text_nearer)
        ));


        ArrayAdapter<String> sortArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sortArrayList);
        spinner_list_sort.setAdapter(sortArrayAdapter);

        try {

            spinner_list_sort.setSelection(MyApplication.preferences.getSortIndex());
            selectedSort = orderbyQuery[MyApplication.preferences.getSortIndex()];
        } catch (Exception e) {

            Log.e("Exception", TAG + "  Spinner Exception : " + e.toString());
            MyApplication.preferences.setSortIndex(0);
            spinner_list_sort.setSelection(0);
            selectedSort = orderbyQuery[0];
        }


        spinner_list_sort.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.e("krm0219", TAG + "  onItemSelected");

                if (++check > 1) {

                    MyApplication.preferences.setSortIndex(position);
                    selectedSort = orderbyQuery[position];
                    Log.e("krm0219", TAG + "  spinner position : " + position + " / " + selectedSort);
                    getGPSCount = 0;
                    onResume();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        rowItems = new ArrayList<>();
        adapter = new CustomExpandableAdapter(getActivity(), rowItems);

        exlist_card_list.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

                isOpen = false;
            }
        });

        exlist_card_list.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                isOpen = true;
                DataUtil.inProgressListPosition = groupPosition;

                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (i != groupPosition)
                        exlist_card_list.collapseGroup(i);
                }
            }
        });


        exlist_smart_route.setSelectionAfterHeaderView();
        exlist_smart_route.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                final int position = groupPosition;
                SmartRouteResult.RouteMaster routeMasterItem = routeMasterArrayList.get(position);

                for (int i = 0; i < smartRouteExpandableAdapter.getGroupCount(); i++) {
                    if (i != position)
                        exlist_smart_route.collapseGroup(i);
                }

                Log.e("krm0219", TAG + "  Smart Route MasterItem : " + position + " / " + routeMasterItem.getRouteNo());

                // 처음 클릭시 API 호출
                if (routeMasterItem.getRouteDetailList() == null) {

                    Log.e("krm0219", TAG + "  Smart Route MasterItem Null");

                    // NOTIFICATION.  GetRouteDetail
                    new GetRouteDetailAsyncTask(context, progressDialog, opID, routeMasterItem.getRouteNo(), new GetRouteDetailAsyncTask.AsyncTaskCallback() {
                        @Override
                        public void onSuccess(SmartRouteResult.RouteMaster result) {

                            SmartRouteResult.RouteMaster routeMasterItem = routeMasterArrayList.get(position);

                            ArrayList<RowItem> cardRowItemArrayList = getRouteList(result.getRouteDetailArrayList());
                            routeMasterItem.setRouteDetailArrayList(result.getRouteDetailArrayList());
                            routeMasterItem.setRouteDetailList(cardRowItemArrayList);
                            smartRouteExpandableAdapter.notifyDataSetChanged();

                            Gson gson = new GsonBuilder().create();
                            Type listType = new TypeToken<ArrayList<SmartRouteResult.RouteMaster>>() {
                            }.getType();
                            String strResult = gson.toJson(routeMasterArrayList, listType);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("SRResult", strResult);
                            editor.apply();
                        }

                        @Override
                        public void onFailure(SmartRouteResult.RouteMaster result) {

                            Toast.makeText(getActivity(), result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }).execute();
                } else {

                    Log.e("krm0219", TAG + "  Smart Route MasterItem Not Null");
                    // 기존에 호출했던 API 결과값으로 보여주기
                    ArrayList<RowItem> cardRowItemArrayList = getRouteList(routeMasterItem.getRouteDetailArrayList());
                    routeMasterItem.setRouteDetailList(cardRowItemArrayList);
                    smartRouteExpandableAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        // Location
        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(context);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();
                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(getActivity(), context);
            }


            try {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_list_searchview.getWindowToken(), 0);

                edit_list_searchview.setText("");
                edit_list_searchview.clearFocus();
            } catch (Exception e) {
                Log.e("Exception", "search init  Exception : " + e.toString());
            }


            int createdSRCount = sharedPreferences.getInt("createdSRCount", 0);
            int clickedSRCount = sharedPreferences.getInt("clickedSRCount", 0);

            Log.e("krm0219", TAG + "  onResume   SmartRoute  DATA > " + createdSRCount + " / " + clickedSRCount);
            if (selectedSort.equals(context.getResources().getString(R.string.text_smart_route)) && createdSRCount != 0) {

                exlist_card_list.setVisibility(View.GONE);
                exlist_smart_route.setVisibility(View.VISIBLE);

                // 'Smart Route' 선택하고 LIST 나갔다 오면 Count 표시 안됨
                // 그래서 DB in-progress 상태인 주문건 Count 표시하기
                Cursor cursor = dbHelper.get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and reg_id='" + opID + "'");
                mCountCallback.onCountRefresh(cursor.getCount());


                if (createdSRCount != clickedSRCount) {

                    // NOTIFICATION.  GetRouteMaster
                    new GetRouteMasterAsyncTask(context, progressDialog, opID, new GetRouteMasterAsyncTask.AsyncTaskCallback() {
                        @Override
                        public void onSuccess(SmartRouteResult result) {

                            if (result != null) {

                                routeMasterArrayList = result.getRouteMasterList();

                                Gson gson = new GsonBuilder().create();
                                Type listType = new TypeToken<ArrayList<SmartRouteResult.RouteMaster>>() {
                                }.getType();
                                String strResult = gson.toJson(routeMasterArrayList, listType);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("clickedSRCount", sharedPreferences.getInt("createdSRCount", 0));
                                editor.putString("SRResult", strResult);
                                editor.apply();

                                smartRouteExpandableAdapter = new SmartRouteExpandableAdapter(getActivity(), routeMasterArrayList);
                                exlist_smart_route.setAdapter(smartRouteExpandableAdapter);
                            }
                        }

                        @Override
                        public void onFailure(SmartRouteResult result) {

                            Toast.makeText(getActivity(), result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }).execute();
                } else {

                    String strResult = sharedPreferences.getString("SRResult", null);

                    if (strResult != null) {

                        Gson gson = new GsonBuilder().create();
                        Type listType = new TypeToken<ArrayList<SmartRouteResult.RouteMaster>>() {
                        }.getType();

                        routeMasterArrayList = gson.fromJson(strResult, listType);
                        smartRouteExpandableAdapter = new SmartRouteExpandableAdapter(getActivity(), routeMasterArrayList);
                        exlist_smart_route.setAdapter(smartRouteExpandableAdapter);
                    }
                }
            } else {

                exlist_card_list.setVisibility(View.VISIBLE);
                exlist_smart_route.setVisibility(View.GONE);
                exlist_smart_route.setAdapter((ExpandableListAdapter) null);

                if (selectedSort.equals(context.getResources().getString(R.string.text_smart_route))) {

                    Toast.makeText(context, context.getResources().getString(R.string.msg_please_create_smart_route), Toast.LENGTH_SHORT).show();
                    spinner_list_sort.setSelection(0);
                    return;
                }

                Cursor cs;
                if (selectedSort.equals(context.getResources().getString(R.string.text_nearer))) {

                    cs = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and reg_id='" + opID + "' order by zip_code asc");
                } else {

                    cs = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and reg_id='" + opID + "' order by " + selectedSort);
                }
                rowItems = new ArrayList<>();

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
                        //   Log.i("krm0219", rowItems.get(i).getType() + " / " + rowItems.get(i).getRoute() + " / " + rowItems.get(i).getShipping());
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


                adapter = new CustomExpandableAdapter(getActivity(), rowItems);
                adapter.setOnMoveUpListener(this);
                exlist_card_list.setAdapter(adapter);

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

                    Log.e("Exception", TAG + "  setSelectedGroup Exception : " + e.toString());
                }

                mCountCallback.onCountRefresh(groupCount);


                if (selectedSort.equals(context.getResources().getString(R.string.text_nearer))) {
                    // Driver 현재 위치 기준 가까운 위치 순으로 정렬
                    getDriverGPSLocation();
                } else {

                    adapter.setSorting(rowItems);
                }
            }


            // 2019.01  krm0219
            // LIST 들어갈 때 TODAY DONE Count 표시하기 위함.
            // ViewPage 특성상 TODAY DONE 페이지는 처음에 호출되지 않아서 0 으로 표시되어있음.

            new TodayDonePickupListDownloadHelper.Builder(getActivity(), opID)
                    .setOnTodayDonePickupOrderDownloadEventListener(new TodayDonePickupListDownloadHelper.OnTodayDonePickupOrderDownloadEventListener() {

                        @Override
                        public void onTodayDonePickupOrderDownloadResult(ArrayList<Object> resultList) {
                            final int resultCode = Integer.parseInt((String) resultList.get(0));

                            if (resultCode == 0) {
                                PickupAssignResult pickupAssignResult = (PickupAssignResult) resultList.get(2);

                                int todayDoneCount = pickupAssignResult.getResultObject().size();
                                onTodayDoneCountListener.onTodayDoneCountRefresh(todayDoneCount);
                            }
                        }
                    }).build().execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("krm0219", TAG + "  onActivityResult " + resultCode + " / " + requestCode);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        GPrinterData.TEMP_TRACKING_NO = "";
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
        DataUtil.stopGPSManager(gpsTrackerManager);

        GPrinterData.TEMP_TRACKING_NO = "";
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

    private static long diffOfDate(String begin) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date beginDate = formatter.parse(begin);
        Date endDate = new Date();

        long diff = endDate.getTime() - beginDate.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
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

        try {

            adapter.filterData(query);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onQueryTextChange Exception : " + e.toString());
        }
        return false;
    }


    // NOTIFICATION
    private ArrayList<RowItem> getRouteList(ArrayList<SmartRouteResult.RouteMaster.RouteDetail> list) {

        ArrayList<RowItem> resultArrayList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            Cursor cs = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat='N' and chg_dt is null and invoice_no='" + list.get(i).getTrackingNo() + "'");

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

                        for (int j = 0; j < resultArrayList.size(); j++) {
                            if (deliveryType.equalsIgnoreCase("D")) {
                                if (routeType.contains("7E") || routeType.contains("FL")) {
                                    // ex. 7E 001 name1, 7E 002 name2  / ex. FL FLA10001 mrtA, FL FLS10001 mrtB

                                    String[] routeSplit = routeType.split(" ");

                                    if (1 < routeSplit.length) {

                                        String routeNumber = routeSplit[0] + " " + routeSplit[1];
                                        if (resultArrayList.get(j).getType().equals("D") && resultArrayList.get(j).getRoute().contains(routeNumber)) {
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
        }

        // k. Outlet 정보 추가
        for (int i = 0; i < resultArrayList.size(); i++) {
            if (resultArrayList.get(i).getType().equalsIgnoreCase("D")) {

                resultArrayList.get(i).setOutlet_company(resultArrayList.get(i).getRoute());
                if (resultArrayList.get(i).getRoute().contains("7E") || resultArrayList.get(i).getRoute().contains("FL")) {

                    String[] routeSplit = resultArrayList.get(i).getRoute().split(" ");

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


    private ArrayList<RowItem> resultItems;
    private int getGPSCount = 0;

    // NOTIFICATION.  Sort - Nearer
    private void getDriverGPSLocation() {

        getGPSCount++;
        progress_in_progress.setVisibility(View.VISIBLE);
        latitude = gpsTrackerManager.getLatitude();
        longitude = gpsTrackerManager.getLongitude();

        Log.e(TAG, " setSortNearer : " + latitude + "  " + longitude + "  ");
        if (latitude == 0 && longitude == 0) {

            if (getGPSCount < 10) {
                // 위치를 가져오는데 시간이 살짝 소요되므로 1초 이후에 다시 시도
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        getDriverGPSLocation();
                    }
                }, 1000);
            } else {
                // 무한반복 방지를 위해 최대 10번만 시도

                progress_in_progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), context.getResources().getString(R.string.msg_error_get_gps), Toast.LENGTH_SHORT).show();
                MyApplication.preferences.setSortIndex(0);
                spinner_list_sort.setSelection(0);
                selectedSort = orderbyQuery[0];
            }
        } else {

         /*   // TEST         1.356619,103.8632591     // 3.063302,101.6951980
            latitude = 1.3649634;
            longitude = 103.7650991;*/
            resultItems = new ArrayList<>();

            for (int i = 0; i < rowItems.size(); i++) {

                if (i != 0) {
                    latitude = resultItems.get(i - 1).getLat();
                    longitude = resultItems.get(i - 1).getLng();
                }

                RowItem item = setSortNearer(latitude, longitude);
                resultItems.add(item);
            }

            for (int i = 0; i < resultItems.size(); i++) {

                Log.e("krm0219", "Sort DATA > " + resultItems.get(i).getShipping() + " / " + resultItems.get(i).getDistance() + " / " + resultItems.get(i).getAddress());
            }

            adapter.setSorting(resultItems);
            progress_in_progress.setVisibility(View.GONE);
        }
    }

    @SuppressLint("DefaultLocale")
    private RowItem setSortNearer(double _latitude, double _longitude) {

        List<RowItem> tempItems = rowItems;
        RowItem item = null;

        //    Log.e("krm0219", "-- " + _latitude + ", " + _longitude + " --");
        for (int i = 0; i < rowItems.size(); i++) {

            Location locationA = new Location("point A");
            locationA.setLatitude(_latitude);
            locationA.setLongitude(_longitude);

            Location locationB = new Location("point B");
            locationB.setLatitude(Double.parseDouble(String.format("%.7f", tempItems.get(i).getLat())));
            locationB.setLongitude(Double.parseDouble(String.format("%.7f", tempItems.get(i).getLng())));

            float distance = locationA.distanceTo(locationB);
            tempItems.get(i).setDistance(distance);
        }

        Collections.sort(tempItems, new CompareDistanceAsc());

        /*for (int i = 0; i < tempItems.size(); i++) {

            Log.e("krm0219", " ** " + tempItems.get(i).getShipping() + " - " + tempItems.get(i).getDistance() + "");
        }*/


        for (int i = 0; i < tempItems.size(); i++) {

            item = tempItems.get(i);

            if (!resultItems.contains(item)) {
                break;
            }
        }

        Log.e("krm0219", "PICK DATA : " + item.getShipping() + " / " + item.getDistance());

        return item;
    }

    class CompareDistanceAsc implements Comparator<RowItem> {

        @Override
        public int compare(RowItem o1, RowItem o2) {

            return Float.compare(o1.getDistance(), o2.getDistance());
        }
    }
}