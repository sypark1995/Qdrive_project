package com.giosis.util.qdrive.main;


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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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

import com.giosis.util.qdrive.list.ChildItem;
import com.giosis.util.qdrive.list.CustomExpandableAdapter;
import com.giosis.util.qdrive.list.RowItem;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/***************
 * @author jtpark_qxpress
 * In-Progress Tab Fragment
 * Expandable ListView
 *
 */
public class RpcListFragment extends Fragment implements OnQueryTextListener, OnCloseListener, CustomExpandableAdapter.OnMoveUpListener {
    String TAG = "RpcListFragment";

    //
    private PermissionChecker checker;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};
    //

    View view;

    SearchView searchview_list;
    FrameLayout layout_list_sort;
    Spinner spinner_list_sort;
    ExpandableListView exlist_card_list;

    //
    Context context;

    private String opID;
    private boolean isOpen = false;

    private ArrayList<String> spinnerlist;
    private String orderby = "zip_code asc";

    private CustomExpandableAdapter adapter;

    private ArrayList<RowItem> rowItems;
    private ArrayList<ChildItem> childItems;
    private RowItem moveItems;
    private int list_position;

    DatabaseHelper dbHelper;

    //
    private boolean isDnd = false;


    public RpcListFragment() {
        super();

    }


    //Adapter 인터페이스 구현, Custom Order 퀵액션에서 Up/Down 이동 시 콜백
    @Override
    public void onMoveUp(int pos) {

        if (isOpen) {
            exlist_card_list.expandGroup(pos);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checker = new PermissionChecker(getActivity());

        if (checker.lacksPermissions(PERMISSIONS)) {

            PermissionActivity.startActivityForResult(getActivity(), PERMISSION_REQUEST_CODE, PERMISSIONS);
            getActivity().overridePendingTransition(0, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();
        opID = SharedPreferencesHelper.getSigninOpID(getActivity());

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

        dbHelper = DatabaseHelper.getInstance();

        // Search
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchview_list.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchview_list.setQueryHint("Search");
        searchview_list.setOnQueryTextListener(this);
        searchview_list.setOnCloseListener(this);

        int id = searchview_list.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText edit_list_searchview = searchview_list.findViewById(id);
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

        spinnerlist = new ArrayList<>(Arrays.asList("Postal Code : Low to High",
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

            MyApplication.preferences.setSortIndex(0);
            OrderBySeq = 0;
        }

        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, spinnerlist);

        spinner_list_sort.setPrompt("Sort by"); // 스피너 제목
        spinner_list_sort.setAdapter(adapter_spinner);
        spinner_list_sort.setSelection(OrderBySeq);
        orderby = orderbyQuery[OrderBySeq];

        spinner_list_sort.setOnItemSelectedListener(new OnItemSelectedListener() {

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


        // Data Porting
        Cursor cs = dbHelper.get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and route='RPC' and reg_id='" + opID + "' order by " + orderby);

        rowItems = new ArrayList<>();
        ChildItem child;

        if (cs.moveToFirst()) {
            do {

                long delay = 0;
                if (cs.getString(cs.getColumnIndex("delivery_dt")) != null && !cs.getString(cs.getColumnIndex("delivery_dt")).equals("")) {
                    try {
                        delay = diffOfDate(cs.getString(cs.getColumnIndex("delivery_dt")));
                    } catch (Exception e) {
                        Log.e("Exception", TAG + "  diffOfDate Exception : " + e.toString());
                    }
                }
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
                        cs.getString(cs.getColumnIndex("route")),
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

                rowitem.setItems(childItems);
                rowItems.add(rowitem);

            } while (cs.moveToNext());
        }

        adapter = new CustomExpandableAdapter(getActivity(), rowItems);
        adapter.setOnMoveUpListener(this);
        exlist_card_list.setAdapter(adapter);

        exlist_card_list.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

                isOpen = false;
            }

        });

        exlist_card_list.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

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
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                return false;
            }

        });


        exlist_card_list.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {

                return false;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        opID = SharedPreferencesHelper.getSigninOpID(getActivity());

        dbHelper = DatabaseHelper.getInstance();
        Cursor cs = dbHelper.get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and route='RPC' and reg_id='" + opID + "' order by " + orderby);
        ArrayList<RowItem> newItems = new ArrayList<>();
        ChildItem child;

        if (cs.moveToFirst()) {
            do {

                long delay = 0;
                if (cs.getString(cs.getColumnIndex("delivery_dt")) != null && cs.getString(cs.getColumnIndex("delivery_dt")) != "") {
                    try {
                        delay = diffOfDate(cs.getString(cs.getColumnIndex("delivery_dt")));
                    } catch (Exception e) {

                    }
                }
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
                        cs.getString(cs.getColumnIndex("route")),
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

                rowitem.setItems(childItems);
                newItems.add(rowitem);
            } while (cs.moveToNext());
        }

        adapter.setSorting(newItems);


        int groupCount = adapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            exlist_card_list.collapseGroup(i);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (list_position > 0) {

                    //Delivered 성공 시 리스트에서 삭제
                    moveItems = rowItems.remove(list_position);
                    adapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");
            }
        }
    }

    public static long diffOfDate(String begin) throws Exception {
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


    public void drag(int from, int to) {
        if (!isDnd) {
            isDnd = true;
            Log.i("Drag and Drop : drag", "from : " + from + ", to : " + to);
        }
    }

    public void drop(int from, int to) {
        if (isDnd) {
            Log.i("Drag and Drop : drop", "from : " + from + ", to : " + to);
            if (from == to)
                return;

            moveItems = rowItems.remove(from);
            rowItems.add(to, moveItems);
            //data.add(to, item);
            //String item = data.remove(from);

            isDnd = false;
            adapter.notifyDataSetChanged();
        }
    }
}