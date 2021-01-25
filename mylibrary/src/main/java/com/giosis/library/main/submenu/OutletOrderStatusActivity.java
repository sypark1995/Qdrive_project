package com.giosis.library.main.submenu;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.giosis.library.R;
import com.giosis.library.main.RowItem;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.Preferences;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author krm0219
 * HOME > Outlet Order Status
 */
public class OutletOrderStatusActivity extends CommonActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    String TAG = "OutletOrderStatusActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_outlet_status_total;
    TextView text_outlet_status_total_count;
    LinearLayout layout_outlet_status_delivery;
    TextView text_outlet_status_delivery_count;
    LinearLayout layout_outlet_status_retrieve;
    TextView text_outlet_status_retrieve_count;

    RelativeLayout layout_outlet_status_condition;
    TextView text_outlet_status_condition;
    Spinner spinner_outlet_status_condition;
    RelativeLayout layout_outlet_status_outlet_code;
    TextView text_outlet_status_outlet_code;
    Spinner spinner_outlet_status_outlet_code;
    RelativeLayout layout_outlet_status_outlet_name;
    TextView text_outlet_status_outlet_name;
    Spinner spinner_outlet_status_outlet_name;

    SearchView searchview_outlet_status;
    FrameLayout layout_outlet_status_sort;
    Spinner spinner_outlet_status_sort;
    ExpandableListView exlist_outlet_status_card;


    //
    String opID;
    String officeCode;
    String deviceID;
    DatabaseHelper databaseHelper;


    String selectedType = "ALL";


    String selectedOutletCondition;
    ArrayAdapter outletConditionArrayAdapter;

    String selectedOutletCode = "ALL";
    boolean firstOutletCode = true;
    ArrayAdapter outletCodeArrayAdapter;

    String selectedOutletName = "ALL";
    boolean firstOutletName = true;
    ArrayList outletNameArrayList;
    ArrayAdapter outletNameArrayAdapter;

    ArrayList sortArrayList;


    OutletOrderStatusAdapter outletOrderStatusAdapter;
    private ArrayList<RowItem> conditionArrayList;     // condition spinner 검색으로 모든 data 저장
    private ArrayList<RowItem> totalArrayList;
    private ArrayList<RowItem> deliveryArrayList;
    private ArrayList<RowItem> retrieveArrayList;
    int deliveryCount;
    int retrieveCount;


    Comparator<RowItem> zipCodeAsc = (o1, o2) -> o1.getZip_code().compareTo(o2.getZip_code());
    Comparator<RowItem> zipCodeDesc = (o1, o2) -> o2.getZip_code().compareTo(o1.getZip_code());

    // NOTIFICATION.
    void setOutletArrayList(String outlet_code, String outlet_name) {

        totalArrayList = new ArrayList<>();
        deliveryArrayList = new ArrayList<>();
        retrieveArrayList = new ArrayList<>();
        deliveryCount = 0;
        retrieveCount = 0;

        Log.e("krm0219", TAG + "  Selected Outlet Code : " + selectedOutletCode + " /  Selected Outlet Name : " + selectedOutletName);

        if (conditionArrayList == null) {

            outletNameArrayList.clear();
            outletNameArrayList.add("ALL");

            text_outlet_status_total_count.setText(Integer.toString(deliveryCount + retrieveCount));
            text_outlet_status_delivery_count.setText(Integer.toString(deliveryCount));
            text_outlet_status_retrieve_count.setText(Integer.toString(retrieveCount));

            outletOrderStatusAdapter = null;
            exlist_outlet_status_card.setAdapter(outletOrderStatusAdapter);
            outletNameArrayAdapter.notifyDataSetChanged();

            return;
        }


        if (outlet_code.equals("ALL") && outlet_name.equals("ALL")) {

            totalArrayList = conditionArrayList;
        } else if (!outlet_code.equals("ALL") && outlet_name.equals("ALL")) {
            // 7E or FL

            for (int i = 0; i < conditionArrayList.size(); i++) {

                String routeString = conditionArrayList.get(i).getRoute();
                String[] routeSplitString = routeString.split(" ");

                if (routeSplitString[0].equals(outlet_code)) {
                    totalArrayList.add(conditionArrayList.get(i));
                }
            }
        } else if (outlet_code.equals("ALL") && !outlet_name.equals("ALL")) {

            for (int i = 0; i < conditionArrayList.size(); i++) {

                String routeString = conditionArrayList.get(i).getRoute();

                if (routeString.contains(outlet_name)) {
                    totalArrayList.add(conditionArrayList.get(i));
                }
            }
        } else if (!outlet_code.equals("ALL") && !outlet_name.equals("ALL")) {

            for (int i = 0; i < conditionArrayList.size(); i++) {

                String routeString = conditionArrayList.get(i).getRoute();

                if (routeString.contains(outlet_name)) {
                    totalArrayList.add(conditionArrayList.get(i));
                }
            }
        }


        // outlet name setting
        outletNameArrayList.clear();
        outletNameArrayList.add("ALL");

        if (outlet_code.equals("ALL")) {

            for (int i = 0; i < conditionArrayList.size(); i++) {
                String[] routeSplit = conditionArrayList.get(i).getRoute().split(" ");

                if (1 < routeSplit.length) {

                    StringBuilder sb = new StringBuilder();

                    for (int j = 2; j < routeSplit.length; j++) {

                        sb.append(routeSplit[j]);
                        sb.append(" ");
                    }

                    if (!outletNameArrayList.contains(sb.toString().trim())) {

                        outletNameArrayList.add(sb.toString().trim());
                    }
                }
            }
        } else {

            for (int i = 0; i < conditionArrayList.size(); i++) {
                if (conditionArrayList.get(i).getRoute().contains(outlet_code)) {
                    String[] routeSplit = conditionArrayList.get(i).getRoute().split(" ");

                    if (1 < routeSplit.length) {

                        StringBuilder sb = new StringBuilder();

                        for (int j = 2; j < routeSplit.length; j++) {

                            sb.append(routeSplit[j]);
                            sb.append(" ");
                        }

                        if (!outletNameArrayList.contains(sb.toString().trim())) {

                            outletNameArrayList.add(sb.toString().trim());
                        }
                    }
                }
            }
        }


        for (int i = 0; i < totalArrayList.size(); i++) {
            //    Log.e("krm0219", "total > " + totalArrayList.get(i).getShipping());

            if (totalArrayList.get(i).getType().equals("D")) {

                deliveryArrayList.add(totalArrayList.get(i));
                deliveryCount += 1;
            } else if (totalArrayList.get(i).getType().equals("P")) {

                retrieveArrayList.add(totalArrayList.get(i));
                retrieveCount += 1;
            }
        }

        text_outlet_status_total_count.setText(Integer.toString(deliveryCount + retrieveCount));
        text_outlet_status_delivery_count.setText(Integer.toString(deliveryCount));
        text_outlet_status_retrieve_count.setText(Integer.toString(retrieveCount));

        if (selectedType.equals("D")) {

            outletOrderStatusAdapter = new OutletOrderStatusAdapter(OutletOrderStatusActivity.this, deliveryArrayList, selectedOutletCondition);
        } else if (selectedType.equals("P")) {

            outletOrderStatusAdapter = new OutletOrderStatusAdapter(OutletOrderStatusActivity.this, retrieveArrayList, selectedOutletCondition);
        } else {

            outletOrderStatusAdapter = new OutletOrderStatusAdapter(OutletOrderStatusActivity.this, totalArrayList, selectedOutletCondition);
        }

        exlist_outlet_status_card.setAdapter(outletOrderStatusAdapter);
        outletNameArrayAdapter.notifyDataSetChanged();
    }

    Comparator<RowItem> trackingNoAsc = (o1, o2) -> o1.getShipping().compareTo(o2.getShipping());


    // NOTI - Sort
    void setSortList(int position) {

        if (conditionArrayList != null) {
            switch (position) {
                case 0: {

                    Collections.sort(conditionArrayList, zipCodeAsc);
                }
                break;

                case 1: {

                    Collections.sort(conditionArrayList, zipCodeDesc);
                }
                break;

                case 2: {

                    Collections.sort(conditionArrayList, trackingNoAsc);
                }
                break;

                case 3: {

                    Collections.sort(conditionArrayList, trackingNoDesc);
                }
                break;

                case 4: {

                    Collections.sort(conditionArrayList, nameAsc);
                }
                break;

                case 5: {

                    Collections.sort(conditionArrayList, nameDesc);
                }
                break;
            }
        }


        setOutletArrayList(selectedOutletCode, selectedOutletName);
    }

    Comparator<RowItem> trackingNoDesc = (o1, o2) -> o2.getShipping().compareTo(o1.getShipping());
    Comparator<RowItem> nameAsc = (o1, o2) -> o1.getOutlet_store_name().compareTo(o2.getOutlet_store_name());
    Comparator<RowItem> nameDesc = (o1, o2) -> o2.getOutlet_store_name().compareTo(o1.getOutlet_store_name());
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            searchview_outlet_status.clearFocus();

            int id = view.getId();
            if (id == R.id.layout_top_back) {

                finish();
            } else if (id == R.id.layout_outlet_status_total) {

                selectedType = "ALL";
                layout_outlet_status_total.setSelected(true);
                layout_outlet_status_delivery.setSelected(false);
                layout_outlet_status_retrieve.setSelected(false);

                outletOrderStatusAdapter = new OutletOrderStatusAdapter(OutletOrderStatusActivity.this, totalArrayList, selectedOutletCondition);
                exlist_outlet_status_card.setAdapter(outletOrderStatusAdapter);

                if (!searchview_outlet_status.getQuery().toString().isEmpty()) {

                    outletOrderStatusAdapter.filterData(searchview_outlet_status.getQuery().toString());
                }
            } else if (id == R.id.layout_outlet_status_delivery) {

                selectedType = "D";
                layout_outlet_status_total.setSelected(false);
                layout_outlet_status_delivery.setSelected(true);
                layout_outlet_status_retrieve.setSelected(false);

                outletOrderStatusAdapter = new OutletOrderStatusAdapter(OutletOrderStatusActivity.this, deliveryArrayList, selectedOutletCondition);
                exlist_outlet_status_card.setAdapter(outletOrderStatusAdapter);

                if (!searchview_outlet_status.getQuery().toString().isEmpty()) {

                    outletOrderStatusAdapter.filterData(searchview_outlet_status.getQuery().toString());
                }
            } else if (id == R.id.layout_outlet_status_retrieve) {

                selectedType = "P";
                layout_outlet_status_total.setSelected(false);
                layout_outlet_status_delivery.setSelected(false);
                layout_outlet_status_retrieve.setSelected(true);

                outletOrderStatusAdapter = new OutletOrderStatusAdapter(OutletOrderStatusActivity.this, retrieveArrayList, selectedOutletCondition);
                exlist_outlet_status_card.setAdapter(outletOrderStatusAdapter);

                if (!searchview_outlet_status.getQuery().toString().isEmpty()) {

                    outletOrderStatusAdapter.filterData(searchview_outlet_status.getQuery().toString());
                }
            } else if (id == R.id.layout_outlet_status_condition) {

                spinner_outlet_status_condition.performClick();
            } else if (id == R.id.layout_outlet_status_outlet_code) {

                spinner_outlet_status_outlet_code.performClick();
            } else if (id == R.id.layout_outlet_status_outlet_name) {

                spinner_outlet_status_outlet_name.performClick();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_order_status);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_outlet_status_total = findViewById(R.id.layout_outlet_status_total);
        text_outlet_status_total_count = findViewById(R.id.text_outlet_status_total_count);
        layout_outlet_status_delivery = findViewById(R.id.layout_outlet_status_delivery);
        text_outlet_status_delivery_count = findViewById(R.id.text_outlet_status_delivery_count);
        layout_outlet_status_retrieve = findViewById(R.id.layout_outlet_status_retrieve);
        text_outlet_status_retrieve_count = findViewById(R.id.text_outlet_status_retrieve_count);

        layout_outlet_status_condition = findViewById(R.id.layout_outlet_status_condition);
        text_outlet_status_condition = findViewById(R.id.text_outlet_status_condition);
        spinner_outlet_status_condition = findViewById(R.id.spinner_outlet_status_condition);
        layout_outlet_status_outlet_code = findViewById(R.id.layout_outlet_status_outlet_code);
        text_outlet_status_outlet_code = findViewById(R.id.text_outlet_status_outlet_code);
        spinner_outlet_status_outlet_code = findViewById(R.id.spinner_outlet_status_outlet_code);
        layout_outlet_status_outlet_name = findViewById(R.id.layout_outlet_status_outlet_name);
        text_outlet_status_outlet_name = findViewById(R.id.text_outlet_status_outlet_name);
        spinner_outlet_status_outlet_name = findViewById(R.id.spinner_outlet_status_outlet_name);

        searchview_outlet_status = findViewById(R.id.searchview_outlet_status);
        layout_outlet_status_sort = findViewById(R.id.layout_outlet_status_sort);
        spinner_outlet_status_sort = findViewById(R.id.spinner_outlet_status_sort);
        exlist_outlet_status_card = findViewById(R.id.exlist_outlet_status_card);

        //
        layout_top_back.setOnClickListener(clickListener);
        layout_outlet_status_total.setOnClickListener(clickListener);
        layout_outlet_status_delivery.setOnClickListener(clickListener);
        layout_outlet_status_retrieve.setOnClickListener(clickListener);

        layout_outlet_status_condition.setOnClickListener(clickListener);
        layout_outlet_status_outlet_code.setOnClickListener(clickListener);
        layout_outlet_status_outlet_name.setOnClickListener(clickListener);


        //
        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        deviceID = Preferences.INSTANCE.getDeviceUUID();
        databaseHelper = DatabaseHelper.getInstance();


        //
        text_top_title.setText(getResources().getString(R.string.text_outlet_order_status));

        layout_outlet_status_total.setSelected(true);
        layout_outlet_status_delivery.setSelected(false);
        layout_outlet_status_retrieve.setSelected(false);

        //
        spinner_outlet_status_condition.setPrompt(getResources().getString(R.string.text_outlet_status_2));
        outletConditionArrayAdapter = ArrayAdapter.createFromResource(this, R.array.outlet_status_condition, android.R.layout.simple_spinner_item);
        outletConditionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_outlet_status_condition.setAdapter(outletConditionArrayAdapter);
        spinner_outlet_status_condition.setSelection(1);

        spinner_outlet_status_condition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedOutletCondition = parent.getItemAtPosition(position).toString();
                text_outlet_status_condition.setText(selectedOutletCondition);
                Log.e("krm0219", TAG + "  Selected Outlet Condition : " + selectedOutletCondition + " / " +
                        spinner_outlet_status_outlet_code.getSelectedItemPosition() + " / " + spinner_outlet_status_outlet_name.getSelectedItemPosition());

                deliveryCount = 0;
                retrieveCount = 0;

                if (spinner_outlet_status_outlet_code.getSelectedItemPosition() != 0) {

                    firstOutletCode = true;
                    spinner_outlet_status_outlet_code.setSelection(0);
                }

                if (spinner_outlet_status_outlet_name.getSelectedItemPosition() != 0) {

                    firstOutletName = true;
                    spinner_outlet_status_outlet_name.setSelection(0);
                }


                setConditionArrayList(selectedOutletCondition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //
        spinner_outlet_status_outlet_code.setPrompt(getResources().getString(R.string.text_all));
        outletCodeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.outlet_company_code, android.R.layout.simple_spinner_item);
        outletCodeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_outlet_status_outlet_code.setAdapter(outletCodeArrayAdapter);

        spinner_outlet_status_outlet_code.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                deliveryCount = 0;
                retrieveCount = 0;

                selectedOutletCode = parent.getItemAtPosition(position).toString();
                text_outlet_status_outlet_code.setText(selectedOutletCode);

                selectedOutletCode = selectedOutletCode.replace("LA", "FL");
                Log.e("krm0219", "onItemSelected  code : " + selectedOutletCode + " / " + firstOutletCode);

                if (!firstOutletCode) {

                    if (spinner_outlet_status_outlet_name.getSelectedItemPosition() != 0) {

                        firstOutletName = true;
                        spinner_outlet_status_outlet_name.setSelection(0);
                        selectedOutletName = "ALL";
                    }
                    setOutletArrayList(selectedOutletCode, selectedOutletName);
                } else {

                    firstOutletCode = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        outletNameArrayList = new ArrayList<>();
        spinner_outlet_status_outlet_name.setPrompt(getResources().getString(R.string.text_all));
        outletNameArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, outletNameArrayList);
        outletNameArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_outlet_status_outlet_name.setAdapter(outletNameArrayAdapter);

        spinner_outlet_status_outlet_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                deliveryCount = 0;
                retrieveCount = 0;

                selectedOutletName = parent.getItemAtPosition(position).toString();
                text_outlet_status_outlet_name.setText(selectedOutletName);
                Log.e("krm0219", "onItemSelected  name : " + selectedOutletName + " / " + firstOutletName);

                if (!firstOutletName) {

                    setOutletArrayList(selectedOutletCode, selectedOutletName);
                } else {

                    firstOutletName = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        try {

            // Spinner 최대 높이 300dp
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner_outlet_status_outlet_name);
            popupWindow.setHeight(dpTopx(300));
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }


        // Search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchview_outlet_status.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchview_outlet_status.setQueryHint("Search");
        searchview_outlet_status.setOnQueryTextListener(this);
        searchview_outlet_status.setOnCloseListener(this);

        int id = searchview_outlet_status.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText edit_list_searchview = searchview_outlet_status.findViewById(id);
        edit_list_searchview.setTextColor(getResources().getColor(R.color.color_8f8f8f));
        edit_list_searchview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_26px));
        edit_list_searchview.setHintTextColor(getResources().getColor(R.color.color_8f8f8f));

        // Sort
        layout_outlet_status_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                spinner_outlet_status_sort.performClick();
            }
        });

        sortArrayList = new ArrayList<>(Arrays.asList(
                "Postal Code : Low to High",
                "Postal Code : High to Low",
                "Tracking No : Low to High",
                "Tracking No : High to Low",
                "Name : Low to High",
                "Name : High to Low"));

        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortArrayList);

        spinner_outlet_status_sort.setPrompt("Sort by"); // 스피너 제목
        spinner_outlet_status_sort.setAdapter(adapter_spinner);
        spinner_outlet_status_sort.setSelection(0);

        spinner_outlet_status_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                deliveryCount = 0;
                retrieveCount = 0;

                setSortList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // NOTIFICATION.  outlet condition 선택에 따라 값 재정비
    void setConditionArrayList(String condition) {

        conditionArrayList = new ArrayList<>();

        // TEST
        // 7Eleven.Ajib / 0040

        if (condition.equals(getResources().getString(R.string.text_outlet_status_1))) {

            new OutletStatusDownloadHelper.Builder(this, opID, officeCode, deviceID, 1).setOnOutletStatusDownloadListener(
                    resultList -> {

                        if (resultList != null) {
                            if (resultList.size() == 0) {

                                conditionArrayList = null;
                            } else {
                                conditionArrayList = resultList;
                            }

                            setOutletArrayList("ALL", "ALL");
                        }
                    }).build().execute();
        } else if (condition.equals(getResources().getString(R.string.text_outlet_status_2))) {

            databaseHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");

            new OutletStatusDownloadHelper.Builder(this, opID, officeCode, deviceID, 2).setOnOutletStatusDownloadListener(
                    resultList -> {

                        if (resultList != null) {

                            conditionArrayList = resultList;
                            setOutletArrayList("ALL", "ALL");
                        } else {

                            Log.e("krm0219", "Error  !!!!!!!!!!!");
                        }
                    }).build().execute();
        } else if (condition.equals(getResources().getString(R.string.text_outlet_status_3))) {

            new OutletStatusDownloadHelper.Builder(this, opID, officeCode, deviceID, 3).setOnOutletStatusDownloadListener(
                    resultList -> {

                        if (resultList != null) {

                            conditionArrayList = resultList;
                            setOutletArrayList("ALL", "ALL");
                        } else {

                            Log.e("krm0219", "Error  !!!!!!!!!!!");
                        }
                    }).build().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (conditionArrayList != null) {
            Log.e("krm0219", "Condition Size : " + conditionArrayList.size());
        }

        deliveryCount = 0;
        retrieveCount = 0;

        exlist_outlet_status_card.setOnGroupExpandListener(groupPosition -> {

            int groupCount = outletOrderStatusAdapter.getGroupCount();

            for (int i = 0; i < groupCount; i++) {
                if (!(i == groupPosition))
                    exlist_outlet_status_card.collapseGroup(i);
            }
        });

        exlist_outlet_status_card.setOnGroupClickListener((parent, v, groupPosition, id) -> {

            if (selectedOutletCondition.equals(getResources().getString(R.string.text_outlet_status_2))) {

                return false;
            } else {

                return true;
            }
        });
    }

    private int dpTopx(float dp) {

        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return pixel;
    }


    @Override
    public boolean onClose() {

        outletOrderStatusAdapter.filterData("");
        return false;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        outletOrderStatusAdapter.filterData(query);
        return false;
    }


    @Override
    public boolean onQueryTextChange(String query) {

        try {

            outletOrderStatusAdapter.filterData(query);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onQueryTextChange Exception : " + e.toString());
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("krm0219", TAG + "  onActivityResult > " + requestCode + " / " + resultCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                setConditionArrayList(selectedOutletCondition);
            }
        }
    }
}