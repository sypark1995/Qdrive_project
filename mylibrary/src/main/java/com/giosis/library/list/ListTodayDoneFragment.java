package com.giosis.library.list;

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
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.giosis.library.R;
import com.giosis.library.bluetooth.BluetoothListener;
import com.giosis.library.main.PickupAssignResult;
import com.giosis.library.server.RetrofitClient;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ListTodayDoneFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    String TAG = "ListTodayDoneFragment";

    private SearchView searchview_list;
    private EditText edit_list_searchview;
    private FrameLayout layout_list_sort;
    private ExpandableListView exlist_card_list;

    private OnTodayDoneCountListener mCountCallback;
    public static final int REQUEST_ADD_SCAN = 30;
    public static final int REQUEST_TAKE_BACK = 31;

    private ArrayList<RowItem> rowItems;
    private ArrayList<ChildItem> childItems;
    private ListTodayDoneAdapter2 adapter;

    BluetoothListener bluetoothListener;

    public ListTodayDoneFragment(BluetoothListener bluetoothListener) {
        super();
        this.bluetoothListener = bluetoothListener;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inprogress, container, false);

        searchview_list = view.findViewById(R.id.searchview_list);
        layout_list_sort = view.findViewById(R.id.layout_list_sort);
        exlist_card_list = view.findViewById(R.id.exlist_card_list);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Search
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchview_list.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchview_list.setQueryHint(getResources().getString(R.string.text_search));
        searchview_list.setOnQueryTextListener(this);
        searchview_list.setOnCloseListener(this);

        int id = searchview_list.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        edit_list_searchview = searchview_list.findViewById(id);
        edit_list_searchview.setTextColor(getResources().getColor(R.color.color_8f8f8f));
        edit_list_searchview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_26px));
        edit_list_searchview.setHintTextColor(getResources().getColor(R.color.color_8f8f8f));

        layout_list_sort.setVisibility(View.GONE);

        exlist_card_list.setOnGroupExpandListener(groupPosition -> {
            int groupCount = adapter.getGroupCount();

            // 한 그룹을 클릭하면 나머지 그룹들은 닫힌다.
            for (int i = 0; i < groupCount; i++) {
                if (!(i == groupPosition))
                    exlist_card_list.collapseGroup(i);
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

        try {
            RetrofitClient.INSTANCE.instanceDynamic().requestGetTodayPickupDoneList(Preferences.INSTANCE.getUserId(), "", "",
                    DataUtil.appID, Preferences.INSTANCE.getUserNation())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(it -> {

                        Log.e("Server", " TodayDone requestGetTodayPickupDoneList  result  " + it.getResultCode());

                        if (it.getResultCode() == 0) {

                            ArrayList<PickupAssignResult.QSignPickupList> list = new Gson().fromJson(it.getResultObject(), new TypeToken<ArrayList<PickupAssignResult.QSignPickupList>>() {
                            }.getType());

                            if (isAdded()) {
                                rowItems = new ArrayList<>();

                                for (PickupAssignResult.QSignPickupList pickupInfo : list) {

                                    childItems = new ArrayList<>();
                                    ChildItem child = new ChildItem();
                                    child.setHp(pickupInfo.getHpNo());
                                    child.setTel(pickupInfo.getTelNo());
                                    child.setStat(pickupInfo.getStat());
                                    child.setStatMsg(pickupInfo.getDriverMemo());
                                    child.setStatReason(pickupInfo.getFailReason());
                                    child.setSecretNoType(pickupInfo.getSecretNoType());
                                    child.setSecretNo(pickupInfo.getSecretNo());
                                    childItems.add(child);

                                    RowItem rowitem = new RowItem(pickupInfo.getContrNo(),
                                            "D+0",
                                            pickupInfo.getInvoiceNo(),
                                            pickupInfo.getReqName(),
                                            "(" + pickupInfo.getZipCode() + ") " + pickupInfo.getAddress(),
                                            pickupInfo.getDelMemo(),
                                            "P",
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
                                            "", ""
                                    );

                                    rowitem.setItems(childItems);
                                    rowItems.add(rowitem);
                                }

                                adapter = new ListTodayDoneAdapter2(rowItems, bluetoothListener);
                                exlist_card_list.setAdapter(adapter);
                                adapter.setSorting(rowItems);

                                int groupCount = adapter.getGroupCount();

                                for (int i = 0; i < groupCount; i++) {
                                    exlist_card_list.collapseGroup(i);
                                }

                                //카운트 전달
                                mCountCallback.onTodayDoneCountRefresh(groupCount);
                            }
                        }
                    }, it -> {

                        if (getActivity() != null && isAdded()) {

                            Log.e(RetrofitClient.errorTag, TAG + " - " + it.toString());
                            Toast.makeText(getActivity(), getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "  onActivityResult > " + resultCode + " / " + requestCode);

        if (requestCode == REQUEST_ADD_SCAN || requestCode == REQUEST_TAKE_BACK) {

            onResume();

        }
    }

    @Override
    public boolean onClose() {
        adapter.filterData("");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            adapter.filterData(query);
        } catch (Exception e) {
            Log.e("Exception", TAG + "  onQueryTextSubmit  Exception : " + e.toString());
        }
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

    @Override
    public void onPause() {
        super.onPause();
        bluetoothListener.clearBluetoothAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothListener.clearBluetoothAdapter();
    }

}
