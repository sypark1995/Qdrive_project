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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

/**
 * @author eylee
 * @date 2016-09-28
 * Pickup done -> Bundle type scan all 로 기능개선
 * @editor krm0219
 * LIST > In-Progress > 'Start To Scan'
 */

public class PickupDoneActivity extends AppCompatActivity {
    String TAG = "PickupDoneActivity";

    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_p_pickup_no;
    TextView text_sign_p_applicant;
    ImageView img_sign_p_start_scan;
    ImageView img_sign_p_zero_qty;
    TextView text_sign_p_total_qty;

    LinearLayout layout_sign_p_applicant_eraser;
    SigningView sign_view_sign_p_applicant_signature;
    LinearLayout layout_sign_p_collector_eraser;
    SigningView sign_view_sign_p_collector_signature;
    EditText edit_sign_p_memo;
    Button btn_sign_p_save;

    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String pickup_no;
    String mStrWaybillNo = "";
    String realQty;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_start_to_scan);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_p_pickup_no = findViewById(R.id.text_sign_p_pickup_no);
        text_sign_p_applicant = findViewById(R.id.text_sign_p_applicant);
        img_sign_p_start_scan = findViewById(R.id.img_sign_p_start_scan);
        img_sign_p_zero_qty = findViewById(R.id.img_sign_p_zero_qty);
        text_sign_p_total_qty = findViewById(R.id.text_sign_p_total_qty);

        layout_sign_p_applicant_eraser = findViewById(R.id.layout_sign_p_applicant_eraser);
        sign_view_sign_p_applicant_signature = findViewById(R.id.sign_view_sign_p_applicant_signature);
        layout_sign_p_collector_eraser = findViewById(R.id.layout_sign_p_collector_eraser);
        sign_view_sign_p_collector_signature = findViewById(R.id.sign_view_sign_p_collector_signature);
        edit_sign_p_memo = findViewById(R.id.edit_sign_p_memo);
        btn_sign_p_save = findViewById(R.id.btn_sign_p_save);


        layout_top_back.setOnClickListener(clickListener);
        layout_sign_p_applicant_eraser.setOnClickListener(clickListener);
        layout_sign_p_collector_eraser.setOnClickListener(clickListener);
        btn_sign_p_save.setOnClickListener(clickListener);

        //
        context = getApplicationContext();
//        opID = SharedPreferencesHelper.getSigninOpID(context);
//        officeCode = SharedPreferencesHelper.getSigninOfficeCode(context);
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(context);
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");
        pickup_no = getIntent().getStringExtra("pickup_no");
        String applicant = getIntent().getStringExtra("applicant");
        mStrWaybillNo = getIntent().getStringExtra("scannedList");
        String strReqQty = getIntent().getStringExtra("scannedQty");


        text_top_title.setText(strTitle);
        text_sign_p_pickup_no.setText(pickup_no);
        text_sign_p_applicant.setText(applicant);

        img_sign_p_start_scan.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
        img_sign_p_zero_qty.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

        text_sign_p_total_qty.setText(strReqQty);


        // Memo 입력제한
        edit_sign_p_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_p_memo.length()) {
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

                DataUtil.enableLocationSettings(PickupDoneActivity.this, context);
            }
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

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //  setResult(Activity.RESULT_CANCELED);
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


    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-07-15
     */
    public void saveServerUploadSign() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveServerUploadSign  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            realQty = text_sign_p_total_qty.getText().toString();

            //사인이미지를 그리지 않았다면
            if (!sign_view_sign_p_applicant_signature.getIsTouche()) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }
            //사인이미지를 그리지 않았다면
            if (!sign_view_sign_p_collector_signature.getIsTouche()) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }

            String driverMemo = edit_sign_p_memo.getText().toString().trim();
            if (driverMemo.equals("")) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_must_enter_memo), Toast.LENGTH_SHORT).show();
                return;
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }


            DataUtil.logEvent("button_click", TAG, "SetPickupUploadData_ScanAll");

            new PickupDoneUploadHelper.Builder(this, opID, officeCode, deviceID,
                    pickup_no, mStrWaybillNo, realQty, sign_view_sign_p_applicant_signature, sign_view_sign_p_collector_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            DataUtil.inProgressListPosition = 0;

                            //    setResult(Activity.RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onPostFailList() {
                        }
                    }).build().execute();
        } catch (Exception e) {

            Log.e("krm0219", TAG + "  Exception : " + e.toString());
            Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.layout_top_back: {

                    cancelSigning();
                }
                break;

                case R.id.layout_sign_p_applicant_eraser: {

                    sign_view_sign_p_applicant_signature.clearText();
                }
                break;

                case R.id.layout_sign_p_collector_eraser: {

                    sign_view_sign_p_collector_signature.clearText();
                }
                break;

                case R.id.btn_sign_p_save: {

                    saveServerUploadSign();
                }
                break;
            }
        }
    };
}