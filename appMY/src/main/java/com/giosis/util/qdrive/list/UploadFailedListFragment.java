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

import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

import java.util.ArrayList;
import java.util.Arrays;

/***************
 * @author jtpark_qxpress
 * In-Progress Tab Fragment
 *
 * @editor krm0219
 */
public class UploadFailedListFragment extends Fragment implements OnQueryTextListener, OnCloseListener, UploadFailedExpandableListAdapter.AdapterInterface {
    String TAG = "List_UploadFailedFragment";

    Context context;
    View view;

    SearchView searchview_list;
    FrameLayout layout_list_sort;
    Spinner spinner_list_sort;
    ExpandableListView exlist_card_list;

    //
    private String opID;
    private String orderby = "zip_code desc";
    OnFailedCountListener mFailedCountCallback;
    private ArrayList<RowItemNotUpload> rowItems;
    private UploadFailedExpandableListAdapter adapter;


    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};

    //
    public UploadFailedListFragment() {
        super();
    }

    //리스트 카운트를 갱신하기 위한 인터페이스
    public interface OnFailedCountListener {
        void onFailedCountRefresh(int count);
    }

    @Override
    public void getFailedCountRefresh() {
        //Adapter 에서 단일건 업로드 후 카운트 갱신
        mFailedCountCallback.onFailedCountRefresh(adapter.getGroupCount());

        for (int i = 0; i < adapter.getGroupCount(); i++) {
            exlist_card_list.collapseGroup(i);
        }
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

            mFailedCountCallback = (OnFailedCountListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFailedCountListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        opID = MyApplication.preferences.getUserId();

        view = inflater.inflate(R.layout.fragment_inprogress, container, false);

        searchview_list = view.findViewById(R.id.searchview_list);
        layout_list_sort = view.findViewById(R.id.layout_list_sort);
        spinner_list_sort = view.findViewById(R.id.spinner_list_sort);
        exlist_card_list = view.findViewById(R.id.exlist_card_list);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Search
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchview_list.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchview_list.setQueryHint(context.getResources().getString(R.string.text_search));
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

        ArrayList<String> spinnerlist = new ArrayList<>(Arrays.asList(
                getResources().getString(R.string.text_sort_postal_code_asc),
                getResources().getString(R.string.text_sort_postal_code_desc),
                getResources().getString(R.string.text_sort_tracking_no_asc),
                getResources().getString(R.string.text_sort_tracking_no_desc),
                getResources().getString(R.string.text_sort_name_asc),
                getResources().getString(R.string.text_sort_name_desc)));

        final String[] orderbyQuery = {
                "zip_code asc",
                "zip_code desc",
                "invoice_no asc",
                "invoice_no desc",
                "rcv_nm asc",
                "rcv_nm desc"
        };

        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, spinnerlist);

        spinner_list_sort.setPrompt("Sort by"); // 스피너 제목
        spinner_list_sort.setAdapter(adapter_spinner);
        spinner_list_sort.setSelection(1); //Zipcode desc

        spinner_list_sort.post(new Runnable() {
            @Override
            public void run() {

                spinner_list_sort.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

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
        adapter = new UploadFailedExpandableListAdapter(getActivity(), rowItems, this);
        exlist_card_list.setAdapter(adapter);

        exlist_card_list.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        exlist_card_list.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                DataUtil.uploadFailedListPosition = groupPosition;
                int groupCount = adapter.getGroupCount();

                for (int i = 0; i < groupCount; i++) {
                    if (!(i == groupPosition))
                        exlist_card_list.collapseGroup(i);
                }
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

        opID = MyApplication.preferences.getUserId();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat <> 'S' and chg_dt is not null and reg_id='" + opID + "' order by " + orderby);

        rowItems = new ArrayList<>();

        ArrayList<ChildItemNotUpload> childItems = new ArrayList<>();
        ChildItemNotUpload child;

        if (cursor != null && cursor.moveToFirst()) {
            do {

                child = new ChildItemNotUpload();
                child.setHp(cursor.getString(cursor.getColumnIndex("hp_no")));
                child.setTel(cursor.getString(cursor.getColumnIndex("tel_no")));
                child.setStat(cursor.getString(cursor.getColumnIndex("stat")));
                child.setStatMsg(cursor.getString(cursor.getColumnIndex("driver_memo")));
                child.setStatReason(cursor.getString(cursor.getColumnIndex("fail_reason")));
                child.setReceiveType(cursor.getString(cursor.getColumnIndex("rcv_type")));
                child.setRealQty(cursor.getString(cursor.getColumnIndex("real_qty")));
                child.setRetryDay(cursor.getString(cursor.getColumnIndex("retry_dt")));
                child.setSecretNoType(cursor.getString(cursor.getColumnIndex("secret_no_type")));
                child.setSecretNo(cursor.getString(cursor.getColumnIndex("secret_no")));
                childItems.add(child);

                //배송 타입
                String deliveryType = cursor.getString(cursor.getColumnIndex("type"));
                String rcv_name = "";
                if (deliveryType.equals("D")) {
                    rcv_name = cursor.getString(cursor.getColumnIndex("rcv_nm")); //구매자
                } else if (deliveryType.equals("P")) {
                    rcv_name = cursor.getString(cursor.getColumnIndex("req_nm")); //픽업 요청 셀러
                }

                RowItemNotUpload rowItemNotUpload = new RowItemNotUpload(cursor.getString(cursor.getColumnIndex("stat")),
                        cursor.getString(cursor.getColumnIndex("invoice_no")),
                        rcv_name,
                        "(" + cursor.getString(cursor.getColumnIndex("zip_code")) + ") "
                                + cursor.getString(cursor.getColumnIndex("address")),
                        cursor.getString(cursor.getColumnIndex("rcv_request")),
                        deliveryType,
                        cursor.getString(cursor.getColumnIndex("route")),
                        cursor.getString(cursor.getColumnIndex("sender_nm"))
                );

                rowItemNotUpload.setItems(childItems);
                rowItems.add(rowItemNotUpload);
            } while (cursor.moveToNext());
        }

        adapter.setSorting(rowItems);

        int groupCount = adapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            exlist_card_list.collapseGroup(i);
        }

        try {

            if (groupCount <= DataUtil.uploadFailedListPosition) {
                DataUtil.uploadFailedListPosition = 0;
            }

            exlist_card_list.setSelectedGroup(DataUtil.uploadFailedListPosition);
            if (DataUtil.uploadFailedListPosition != 0) {

                exlist_card_list.expandGroup(DataUtil.uploadFailedListPosition);
            }
        } catch (Exception e) {

            Log.e("Exception", TAG + "  list Position Exception : " + e.toString());
        }


        //카운트 갱신
        mFailedCountCallback.onFailedCountRefresh(groupCount);

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(context);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();
                adapter.setGpsTrackerManager(gpsTrackerManager);
            } else {

                DataUtil.enableLocationSettings((Activity) context, context);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (gpsTrackerManager != null) {

            gpsTrackerManager.stopFusedProviderService();
            gpsTrackerManager = null;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                isPermissionTrue = true;
                Log.e("krm0219", TAG + "   onActivityResult  PERMISSIONS_GRANTED");
            }
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
    public boolean onQueryTextChange(String query) {

        try {

            adapter.filterData(query);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onQueryTextChange Exception : " + e.toString());
        }
        return false;
    }
}