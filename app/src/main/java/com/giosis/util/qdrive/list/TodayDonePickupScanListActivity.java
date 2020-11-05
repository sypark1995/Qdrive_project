package com.giosis.util.qdrive.list;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.util.ArrayList;

/**
 * @author eylee 2017-03-14
 * @editor LIST > TODAY DONE > 'ADD SCAN' Button
 * LIST > TODAY DONE > 'Take Back' Button
 */
public class TodayDonePickupScanListActivity extends AppCompatActivity {
    String TAG = "TodayDonePickupScanListActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_add_scan_pickup_no;
    TextView text_add_scan_scanned_qty;
    Button btn_add_scan_add;

    ListView list_add_scan_scanned_list;

    ArrayList<PickupScanListItem> itemArrayList;
    TodayDonePickupScanListAdapter todayDonePickupScanListAdapter;


    Context context;
    String opID = "";
    String opName = "";
    String officeCode = "";
    String deviceID = "";

    String button_type;
    String pickup_no = "";
    String applicant = "";
    String scanned_qty;
    static int activeInstances = 0;


    static boolean isActive() {
        return (activeInstances > 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        activeInstances++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        activeInstances = 0;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_scan_list);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_add_scan_pickup_no = findViewById(R.id.text_add_scan_pickup_no);
        text_add_scan_scanned_qty = findViewById(R.id.text_add_scan_scanned_qty);
        btn_add_scan_add = findViewById(R.id.btn_add_scan_add);

        list_add_scan_scanned_list = findViewById(R.id.list_add_scan_scanned_list);


        //
        context = getApplicationContext();
//        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//        opName = SharedPreferencesHelper.getSigninOpName(getApplicationContext());
//        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());
        opID = MyApplication.preferences.getUserId();
        opName = MyApplication.preferences.getUserName();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        String title = getIntent().getStringExtra("title");
        pickup_no = getIntent().getStringExtra("pickup_no");
        applicant = getIntent().getStringExtra("applicant");
        button_type = getIntent().getStringExtra("button_type");


        text_top_title.setText(title);
        text_add_scan_pickup_no.setText(pickup_no);

        if (button_type.equals("Add Scan")) {

            btn_add_scan_add.setText(R.string.button_add_scan_list);
        } else if (button_type.equals("Take Back")) {

            btn_add_scan_add.setText(R.string.button_take_back);
        }


        layout_top_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = getIntent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });


        btn_add_scan_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button_type.equals("Add Scan")) {

                    click_add_scan();
                } else if (button_type.equals("Take Back")) {

                    click_take_back();
                }
            }
        });

        new ScanPackingListDownloadHelper.Builder(this, opID, pickup_no)
                .setOnScanPackingListDownloadEventListener(result -> setScannedList(result)).build().execute();
    }

    public void setScannedList(PickupPackingListResult result) {

        scanned_qty = Integer.toString(result.getResultObject().size());
        text_add_scan_scanned_qty.setText(scanned_qty);


        itemArrayList = new ArrayList<>();

        for (PickupPackingListResult.ScanPackingList scanPackingList : result.getResultObject()) {

            PickupScanListItem item = new PickupScanListItem();
            item.setTracking_no(scanPackingList.getPackingNo());
            item.setScanned_date(scanPackingList.getRegDt());
            itemArrayList.add(item);
        }

        todayDonePickupScanListAdapter = new TodayDonePickupScanListAdapter(context, itemArrayList);
        list_add_scan_scanned_list.setAdapter(todayDonePickupScanListAdapter);
        setListViewHeightBasedOnChildren(list_add_scan_scanned_list);
    }

    // packing list 추가
    public void click_add_scan() {

        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra("title", "ADD Scan List");
        intent.putExtra("type", BarcodeType.PICKUP_ADD_SCAN);
        intent.putExtra("pickup_no", pickup_no);
        intent.putExtra("applicant", applicant);
        startActivityForResult(intent, List_TodayDoneFragment.REQUEST_ADD_SCAN);
    }

    public void click_take_back() {

        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra("title", "Take Back List");
        intent.putExtra("type", BarcodeType.PICKUP_TAKE_BACK);
        intent.putExtra("pickup_no", pickup_no);
        intent.putExtra("applicant", applicant);
        intent.putExtra("scanned_qty", scanned_qty);
        startActivityForResult(intent, List_TodayDoneFragment.REQUEST_TAKE_BACK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == List_TodayDoneFragment.REQUEST_ADD_SCAN
                || requestCode == List_TodayDoneFragment.REQUEST_TAKE_BACK) {

            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {

            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;

        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}