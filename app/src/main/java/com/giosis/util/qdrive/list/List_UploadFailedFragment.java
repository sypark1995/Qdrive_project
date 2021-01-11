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
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author jtpark_qxpress
 * In-Progress Tab Fragment
 * <p></p>
 * @editor krm0219
 */
public class List_UploadFailedFragment extends Fragment implements OnQueryTextListener, OnCloseListener, CustomNotUploadAdapter.AdapterInterface {
    String TAG = "List_UploadFailedFragment";

    private GPSTrackerManager gpsTrackerManager;
    private boolean gpsEnable = false;

    //
    private PermissionChecker checker;
    private boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};
    //

    View view;

    private SearchView searchview_list;
    private FrameLayout layout_list_sort;
    private Spinner spinner_list_sort;
    private ExpandableListView exlist_card_list;


    //
    Context context;
    private String opID;

    OnFailedCountListener mFailedCountCallback;

    private ArrayList<String> spinnerlist;
    private String orderby = "zip_code desc";

    private CustomNotUploadAdapter adapter;

    private ArrayList<RowItemNotUpload> rowItems;
    private ArrayList<ChildItemNotUpload> childItems;
    private int list_position;

    DatabaseHelper dbHelper;
    private RowItemNotUpload moveItems;

    //
    private boolean isDnd = false;


    public List_UploadFailedFragment() {
        super();
    }

    //리스트 카운트를 갱신하기 위한 인터페이스
    public interface OnFailedCountListener {
        public void onFailedCountRefresh(int count);
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

        checker = new PermissionChecker(getActivity());

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
//        opID = SharedPreferencesHelper.getSigninOpID(getActivity());
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

        dbHelper = DatabaseHelper.getInstance();

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

        spinnerlist = new ArrayList<>(Arrays.asList(
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
        adapter = new CustomNotUploadAdapter(getActivity(), rowItems, this);
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

                // 한 그룹을 클릭하면 나머지 그룹들은 닫힌다.
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

//        opID = SharedPreferencesHelper.getSigninOpID(getActivity());
        opID = MyApplication.preferences.getUserId();

        Cursor cs2 = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat <> 'S' and chg_dt is not null and reg_id='" + opID + "' order by " + orderby);

        rowItems = new ArrayList<>();

        if (cs2 != null && cs2.moveToFirst()) {
            do {

                childItems = new ArrayList<>();
                ChildItemNotUpload child = new ChildItemNotUpload();
                child.setHp(cs2.getString(cs2.getColumnIndex("hp_no")));
                child.setTel(cs2.getString(cs2.getColumnIndex("tel_no")));
                child.setStat(cs2.getString(cs2.getColumnIndex("stat")));
                child.setStatMsg(cs2.getString(cs2.getColumnIndex("driver_memo")));
                child.setStatReason(cs2.getString(cs2.getColumnIndex("fail_reason")));
                child.setReceiveType(cs2.getString(cs2.getColumnIndex("rcv_type")));
                child.setRealQty(cs2.getString(cs2.getColumnIndex("real_qty")));
                child.setRetryDay(cs2.getString(cs2.getColumnIndex("retry_dt")));
                child.setSecretNoType(cs2.getString(cs2.getColumnIndex("secret_no_type")));
                child.setSecretNo(cs2.getString(cs2.getColumnIndex("secret_no")));
                childItems.add(child);

                //배송 타입
                String deliveryType = cs2.getString(cs2.getColumnIndex("type"));
                String rcv_name = "";
                if (deliveryType.equals("D")) {
                    rcv_name = cs2.getString(cs2.getColumnIndex("rcv_nm")); //구매자
                } else if (deliveryType.equals("P")) {
                    rcv_name = cs2.getString(cs2.getColumnIndex("req_nm")); //픽업 요청 셀러
                }

                RowItemNotUpload rowitem = new RowItemNotUpload(cs2.getString(cs2.getColumnIndex("stat")),
                        cs2.getString(cs2.getColumnIndex("invoice_no")),
                        rcv_name,
                        "(" + cs2.getString(cs2.getColumnIndex("zip_code")) + ") "
                                + cs2.getString(cs2.getColumnIndex("address")),
                        cs2.getString(cs2.getColumnIndex("rcv_request")),
                        deliveryType,
                        cs2.getString(cs2.getColumnIndex("route")),
                        cs2.getString(cs2.getColumnIndex("sender_nm"))
                );

                rowitem.setItems(childItems);
                rowItems.add(rowitem);
            } while (cs2.moveToNext());
        }

        adapter.setSorting(rowItems);

        int groupCount = adapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            exlist_card_list.collapseGroup(i);
        }

        try {

            Log.e("krm0219", "Upload Failed List Position : " + DataUtil.uploadFailedListPosition);

            if (groupCount <= DataUtil.uploadFailedListPosition) {
                DataUtil.uploadFailedListPosition = 0;
            }

            exlist_card_list.setSelectedGroup(DataUtil.uploadFailedListPosition);
            if (DataUtil.uploadFailedListPosition != 0) {

                exlist_card_list.expandGroup(DataUtil.uploadFailedListPosition);
            }
        } catch (Exception e) {

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

                isPermissionTrue = true;
                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");
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