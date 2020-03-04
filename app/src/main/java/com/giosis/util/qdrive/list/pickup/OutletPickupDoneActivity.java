package com.giosis.util.qdrive.list.pickup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.list.ListActivity;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.list.delivery.OnOutletDataUploadEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

/**
 * @author krm0219
 * LIST > In-Progress > Outlet Pickup (Step 3)
 */
public class OutletPickupDoneActivity extends AppCompatActivity {
    String TAG = "OutletPickupDoneActivity";

    //krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

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


    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mTitle;
    String mPickupNo;
    String mApplicant;
    String mQty;
    String mRoute;
    int mScannedQty;
    OutletPickupDoneResult resultData;
    String scanned_list = "";

    OutletPickupDoneTrackingNoAdapter outletPickupDoneTrackingNoAdapter;

    String mDriverMemo;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_pickup_done);

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
        context = getApplicationContext();
        opID = SharedPreferencesHelper.getSigninOpID(context);
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(context);
        deviceID = SharedPreferencesHelper.getSigninDeviceID(context);

        mTitle = getIntent().getStringExtra("title");
        mPickupNo = getIntent().getStringExtra("pickupNo");
        mApplicant = getIntent().getStringExtra("applicant");
        mQty = getIntent().getStringExtra("qty");
        mRoute = getIntent().getStringExtra("route");
        mScannedQty = getIntent().getIntExtra("scannedQty", 1);
        resultData = (OutletPickupDoneResult) getIntent().getSerializableExtra("tracking_data");
        scanned_list = getIntent().getStringExtra("scannedList");


        outletPickupDoneTrackingNoAdapter = new OutletPickupDoneTrackingNoAdapter(context, resultData, mRoute);
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
        text_sign_p_outlet_scanned_qty.setText(Integer.toString(mScannedQty));

        // Memo 입력제한
        edit_sign_p_outlet_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_p_outlet_memo.length()) {
                    Toast.makeText(context, context.getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(context);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(OutletPickupDoneActivity.this, context);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
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

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void AlertShow(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 닫기
                        finish();
                    }
                });
        alert_internet_status.show();
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.layout_top_back: {

                    cancelSigning();
                }
                break;

                case R.id.layout_sign_p_outlet_sign_eraser: {

                    sign_view_sign_p_outlet_signature.clearText();
                }
                break;

                case R.id.btn_sign_p_outlet_save: {

                    saveOutletPickupDone();
                }
                break;
            }
        }
    };


    public void saveOutletPickupDone() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveOutletPickupDone  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            if (mRoute.equals("7E")) {
                if (!sign_view_sign_p_outlet_signature.getIsTouche()) {
                    Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            mDriverMemo = edit_sign_p_outlet_memo.getText().toString();

            try {

                Bundle params = new Bundle();
                params.putString("Activity", TAG);
                params.putString("method", "SetOutletPickupUploadData");
                DataUtil.mFirebaseAnalytics.logEvent("button_click", params);
            } catch (Exception e) {

                Log.e("Firebase", "mFirebaseAnalytics error : " + e.toString());
            }

            new OutletPickupDoneHelper.Builder(this, opID, officeCode, deviceID, mPickupNo,
                    sign_view_sign_p_outlet_signature, mDriverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude, Integer.toString(mScannedQty), scanned_list, mRoute)
                    .setOnOutletDataUploadEventListener(new OnOutletDataUploadEventListener() {

                        @Override
                        public void onPostResult() {

                            DataUtil.inProgressListPosition = 0;
                            Intent intent = new Intent(OutletPickupDoneActivity.this, ListActivity.class);
                            startActivity(intent);
                        }

                    }).build().execute();

        } catch (Exception e) {

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            Log.e("krm0219", msg);
            Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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