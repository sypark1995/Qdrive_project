package com.giosis.util.qdrive.singapore.list.pickup;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.giosis.util.qdrive.singapore.MemoryStatus;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.list.ListActivity;
import com.giosis.util.qdrive.singapore.list.SigningView;
import com.giosis.util.qdrive.singapore.util.CommonActivity;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.PermissionActivity;
import com.giosis.util.qdrive.singapore.util.PermissionChecker;
import com.giosis.util.qdrive.singapore.util.Preferences;

// TODO_kjyoo TEST    UI는 확인했음 // Done(OutletPickupDoneHelper) 테스트 필요
public class OutletPickupStep3Activity extends CommonActivity {
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};
    String TAG = "OutletPickupDoneActivity";

    TextView text_top_title;
    FrameLayout layout_top_back;
    TextView text_sign_p_outlet_pickup_no;
    TextView text_sign_p_outlet_applicant;
    TextView text_sign_p_outlet_total_qty;
    TextView text_sign_p_outlet_scanned_qty;
    ListView list_sign_p_outlet_tracking_no;
    LinearLayout layout_sign_p_sign_memo;
    LinearLayout layout_sign_p_outlet_sign_eraser;
    SigningView sign_view_sign_p_outlet_signature;
    EditText edit_sign_p_outlet_memo;

    Button btn_sign_p_outlet_save;
    String mTitle;
    String mPickupNo;
    String mApplicant;
    String mQty;
    String mRoute;
    int mScannedQty;
    OutletPickupDoneResult resultData;
    String scanned_list = "";
    OutletPickupDoneTrackingNoAdapter outletPickupDoneTrackingNoAdapter;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    String mDriverMemo;
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (id == R.id.layout_top_back) {

                cancelSigning();
            } else if (id == R.id.layout_sign_p_outlet_sign_eraser) {

                sign_view_sign_p_outlet_signature.clearText();
            } else if (id == R.id.btn_sign_p_outlet_save) {

                saveOutletPickupDone();
            }
        }
    };

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_pickup_step3);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_p_outlet_pickup_no = findViewById(R.id.text_sign_p_outlet_pickup_no);
        text_sign_p_outlet_applicant = findViewById(R.id.text_sign_p_outlet_applicant);
        text_sign_p_outlet_total_qty = findViewById(R.id.text_sign_p_outlet_total_qty);
        text_sign_p_outlet_scanned_qty = findViewById(R.id.text_sign_p_outlet_scanned_qty);
        list_sign_p_outlet_tracking_no = findViewById(R.id.list_sign_p_outlet_tracking_no);

        layout_sign_p_sign_memo = findViewById(R.id.layout_sign_p_sign_memo);
        layout_sign_p_outlet_sign_eraser = findViewById(R.id.layout_sign_p_outlet_sign_eraser);
        sign_view_sign_p_outlet_signature = findViewById(R.id.sign_view_sign_p_outlet_signature);
        edit_sign_p_outlet_memo = findViewById(R.id.edit_sign_p_outlet_memo);
        btn_sign_p_outlet_save = findViewById(R.id.btn_sign_p_outlet_save);

        layout_top_back.setOnClickListener(clickListener);
        layout_sign_p_outlet_sign_eraser.setOnClickListener(clickListener);
        btn_sign_p_outlet_save.setOnClickListener(clickListener);

        //
        mTitle = getIntent().getStringExtra("title");
        mPickupNo = getIntent().getStringExtra("pickupNo");
        mApplicant = getIntent().getStringExtra("applicant");
        mQty = getIntent().getStringExtra("qty");
        mRoute = getIntent().getStringExtra("route");
        mScannedQty = getIntent().getIntExtra("scannedQty", 1);
        resultData = (OutletPickupDoneResult) getIntent().getSerializableExtra("tracking_data");
        scanned_list = getIntent().getStringExtra("scannedList");


        outletPickupDoneTrackingNoAdapter = new OutletPickupDoneTrackingNoAdapter(OutletPickupStep3Activity.this, resultData, mRoute);
        outletPickupDoneTrackingNoAdapter.notifyDataSetChanged();
        list_sign_p_outlet_tracking_no.setAdapter(outletPickupDoneTrackingNoAdapter);
        setListViewHeightBasedOnChildren(list_sign_p_outlet_tracking_no);

        if (mRoute.equals("7E")) {

            text_top_title.setText(mTitle);
            layout_sign_p_sign_memo.setVisibility(View.VISIBLE);
        } else if (mRoute.equals("FL")) {

            text_top_title.setText(R.string.text_title_fl_pickup);
            layout_sign_p_sign_memo.setVisibility(View.GONE);
        }
        text_sign_p_outlet_pickup_no.setText(mPickupNo);
        text_sign_p_outlet_applicant.setText(mApplicant);
        text_sign_p_outlet_total_qty.setText(mQty);
        text_sign_p_outlet_scanned_qty.setText(String.valueOf(mScannedQty));

        // Memo 입력제한
        edit_sign_p_outlet_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_p_outlet_memo.length()) {
                    Toast.makeText(OutletPickupStep3Activity.this, getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //
        PermissionChecker checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(PERMISSIONS)) {

            isPermissionTrue = false;
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, PERMISSIONS);
            overridePendingTransition(0, 0);
        } else {

            isPermissionTrue = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        cancelSigning();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(OutletPickupStep3Activity.this);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.gpsTrackerStart();

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(OutletPickupStep3Activity.this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
    }

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> finish())
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    private void AlertShow(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(getResources().getString(R.string.button_close),
                (dialog, which) -> {
                    dialog.dismiss(); // 닫기
                    finish();
                });
        alert_internet_status.show();
    }

    public void saveOutletPickupDone() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {
                AlertShow(getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {
                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveOutletPickupDone  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            if (mRoute.equals("7E")) {
                if (!sign_view_sign_p_outlet_signature.isTouch()) {
                    Toast.makeText(OutletPickupStep3Activity.this, getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            mDriverMemo = edit_sign_p_outlet_memo.getText().toString();

            DataUtil.logEvent("button_click", TAG, "SetOutletPickupUploadData");

            new OutletPickupDoneHelper.Builder(this, Preferences.INSTANCE.getUserId(), Preferences.INSTANCE.getOfficeCode(), Preferences.INSTANCE.getDeviceUUID(),
                    mPickupNo, sign_view_sign_p_outlet_signature, mDriverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude, Integer.toString(mScannedQty), scanned_list, mRoute)
                    .setOnOutletDataUploadEventListener(() -> {

                        Intent intent = new Intent(OutletPickupStep3Activity.this, ListActivity.class);
                        startActivity(intent);
                    }).build().execute();

        } catch (Exception e) {
            String msg = String.format(getResources().getString(R.string.text_exception), e);
            Toast.makeText(OutletPickupStep3Activity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}