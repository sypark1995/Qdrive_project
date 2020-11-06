package com.giosis.util.qdrive.list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import androidx.fragment.app.Fragment;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.pickup.PickupAssignResult;
import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterData;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

import java.util.ArrayList;

/***************
 * @author jtpark_qxpress
 * In-Progress Tab Fragment
 * Expandable ListView
 *
 */
public class TodayDoneListFragment extends Fragment implements OnQueryTextListener, OnCloseListener {
    String TAG = "List_TodayDoneFragment";

    Context context;
    DatabaseHelper dbHelper;
    private String opID;


    //
    View view;

    private SearchView searchview_list;
    private EditText edit_list_searchview;
    private FrameLayout layout_list_sort;
    private ExpandableListView exlist_card_list;


    OnTodayDoneCountListener mCountCallback;
    private static final int REQUEST_SCAN_ADD_LIST = 32;
    private static final int REQUEST_TAKE_BACK = 33;

    private ArrayList<RowItem> rowItems;
    private ArrayList<ChildItem> childItems;
    private TodayDoneExpandableListAdapter adapter;

    //
    private PermissionChecker checker;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};


    public TodayDoneListFragment() {
        super();
    }

    //리스트 카운트를 갱신하기 위한 인터페이스
    public interface OnTodayDoneCountListener {
        void onTodayDoneCountRefresh(int count);
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

            mCountCallback = (OnTodayDoneCountListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCountListener");
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
        opID = MyApplication.preferences.getUserId();

        view = inflater.inflate(R.layout.fragment_inprogress, container, false);

        searchview_list = view.findViewById(R.id.searchview_list);
        layout_list_sort = view.findViewById(R.id.layout_list_sort);
        exlist_card_list = view.findViewById(R.id.exlist_card_list);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        opID = MyApplication.preferences.getUserId();
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

        layout_list_sort.setVisibility(View.GONE);

        rowItems = new ArrayList<>();
        adapter = new TodayDoneExpandableListAdapter(getActivity(), rowItems);
        exlist_card_list.setAdapter(adapter);

        exlist_card_list.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
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

        try {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_list_searchview.getWindowToken(), 0);

            edit_list_searchview.setText("");
            edit_list_searchview.clearFocus();
        } catch (Exception e) {
            Log.e("Exception", "search init  Exception : " + e.toString());
        }


        opID = MyApplication.preferences.getUserId();
        dbHelper = DatabaseHelper.getInstance();

        new TodayDonePickupListDownloadHelper.Builder(getActivity(), opID)
                .setOnTodayDonePickupOrderDownloadEventListener(new TodayDonePickupListDownloadHelper.OnTodayDonePickupOrderDownloadEventListener() {

                    @Override
                    public void onTodayDonePickupOrderDownloadResult(PickupAssignResult result) {

                        rowItems = new ArrayList<>();
                        ChildItem child;

                        for (PickupAssignResult.QSignPickupList pickupInfo : result.getResultObject()) {

                            childItems = new ArrayList<>();
                            child = new ChildItem();
                            child.setHp(pickupInfo.getHpNo());
                            child.setTel(pickupInfo.getTelNo());
                            child.setStat(pickupInfo.getStat());
                            child.setStatMsg(pickupInfo.getDriverMemo());
                            child.setStatReason(pickupInfo.getFailReason());
                            child.setSecretNoType(pickupInfo.getSecretNoType());
                            child.setSecretNo(pickupInfo.getSecretNo());

                            childItems.add(child);

                            //배송 타입
                            String deliveryType = "P";
                            String rcv_name = "";

                            rcv_name = pickupInfo.getReqName(); //픽업 요청 셀러
                            long delay = 0;
                            RowItem rowitem = new RowItem(pickupInfo.getContrNo(),
                                    "D+" + delay,
                                    pickupInfo.getInvoiceNo(),
                                    rcv_name,
                                    "(" + pickupInfo.getZipCode() + ") "
                                            + pickupInfo.getAddress(),
                                    pickupInfo.getDelMemo(),
                                    deliveryType,
                                    pickupInfo.getRoute(),
                                    "", //cs.getString(cs.getColumnIndex("sender_nm")),
                                    pickupInfo.getPickupHopeDay(),
                                    pickupInfo.getQty(),
                                    "", // cs.getString(cs.getColumnIndex("self_memo")),
                                    0, // cs.getDouble(cs.getColumnIndex("lat")),
                                    0, //cs.getDouble(cs.getColumnIndex("lng")),
                                    pickupInfo.getStat(), //cs.getString(cs.getColumnIndex("stat")),
                                    pickupInfo.getCustNo(), //cs.getString(cs.getColumnIndex("cust_no")),
                                    pickupInfo.getPartnerID(),//cs.getString(cs.getColumnIndex("partner_id"))
                                    "",
                                    "",
                                    ""
                            );

                            rowitem.setItems(childItems);
                            rowItems.add(rowitem);
                        }

                        adapter.setSorting(rowItems);


                        int groupCount = adapter.getGroupCount();

                        for (int i = 0; i < groupCount; i++) {
                            exlist_card_list.collapseGroup(i);
                        }

                        //카운트 전달
                        mCountCallback.onTodayDoneCountRefresh(groupCount);
                    }
                }).build().execute();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "  onActivityResult > " + resultCode + " / " + requestCode);

        if (requestCode == REQUEST_SCAN_ADD_LIST || requestCode == REQUEST_TAKE_BACK) {

            onResume();
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                Log.e(TAG, "   onActivityResult  PERMISSIONS_GRANTED");
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

        }
        return false;
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
        }
    }
}