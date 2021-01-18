package com.giosis.util.qdrive.list.pickup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

/**
 * @author krm0219
 * @date 2019-01
 * LIST > TODAY DONE > Take Back
 */

public class PickupTakeBackActivity extends CommonActivity {
    String TAG = "PickupTakeBackActivity";

    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_p_tb_pickup_no;
    TextView text_sign_p_tb_applicant;
    TextView text_sign_p_tb_total_qty;
    TextView text_sign_p_tb_take_back_qty;
    TextView text_sign_p_tb_result_total_qty;

    LinearLayout layout_sign_p_tb_applicant_eraser;
    SigningView sign_view_sign_p_tb_applicant_signature;
    LinearLayout layout_sign_p_tb_collector_eraser;
    SigningView sign_view_sign_p_tb_collector_signature;
    Button btn_sign_p_tb_save;

    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String pickup_no;
    String applicant;
    String total_qty;
    String take_back_qty;
    int result_total_qty;
    String packing_no_list;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};


    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_take_back);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_p_tb_pickup_no = findViewById(R.id.text_sign_p_tb_pickup_no);
        text_sign_p_tb_applicant = findViewById(R.id.text_sign_p_tb_applicant);
        text_sign_p_tb_total_qty = findViewById(R.id.text_sign_p_tb_total_qty);
        text_sign_p_tb_take_back_qty = findViewById(R.id.text_sign_p_tb_take_back_qty);
        text_sign_p_tb_result_total_qty = findViewById(R.id.text_sign_p_tb_result_total_qty);

        layout_sign_p_tb_applicant_eraser = findViewById(R.id.layout_sign_p_tb_applicant_eraser);
        sign_view_sign_p_tb_applicant_signature = findViewById(R.id.sign_view_sign_p_tb_applicant_signature);
        layout_sign_p_tb_collector_eraser = findViewById(R.id.layout_sign_p_tb_collector_eraser);
        sign_view_sign_p_tb_collector_signature = findViewById(R.id.sign_view_sign_p_tb_collector_signature);
        btn_sign_p_tb_save = findViewById(R.id.btn_sign_p_tb_save);


        layout_top_back.setOnClickListener(clickListener);
        layout_sign_p_tb_applicant_eraser.setOnClickListener(clickListener);
        layout_sign_p_tb_collector_eraser.setOnClickListener(clickListener);
        btn_sign_p_tb_save.setOnClickListener(clickListener);


        //
        context = getApplicationContext();
//        opID = SharedPreferencesHelper.getSigninOpID(context);
//        officeCode = SharedPreferencesHelper.getSigninOfficeCode(context);
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(context);
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");
        pickup_no = getIntent().getStringExtra("pickupNo");
        applicant = getIntent().getStringExtra("applicant");
        packing_no_list = getIntent().getStringExtra("scannedList");
        total_qty = getIntent().getStringExtra("totalQty");
        take_back_qty = getIntent().getStringExtra("takeBackQty");
        result_total_qty = Integer.parseInt(total_qty) - Integer.parseInt(take_back_qty);


        text_top_title.setText(strTitle);
        text_sign_p_tb_pickup_no.setText(pickup_no);
        text_sign_p_tb_applicant.setText(applicant);
        text_sign_p_tb_total_qty.setText(total_qty);
        text_sign_p_tb_take_back_qty.setText(take_back_qty);
        text_sign_p_tb_result_total_qty.setText(Integer.toString(result_total_qty));

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

                DataUtil.enableLocationSettings(PickupTakeBackActivity.this, context);
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

                        setResult(Activity.RESULT_CANCELED);
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


            if (!sign_view_sign_p_tb_applicant_signature.getIsTouche()) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!sign_view_sign_p_tb_collector_signature.getIsTouche()) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }

            //서버에 올리기전 용량체크  내장메모리가 10메가 안남은경우
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }


            com.giosis.library.util.DataUtil.logEvent("button_click", TAG, "SetPickupUploadData_TakeBack");

            new PickupTakeBackUploadHelper.Builder(this, opID, officeCode, deviceID,
                    pickup_no, packing_no_list, result_total_qty, sign_view_sign_p_tb_applicant_signature, sign_view_sign_p_tb_collector_signature,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            setResult(Activity.RESULT_OK);
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

                case R.id.layout_sign_p_tb_applicant_eraser: {

                    sign_view_sign_p_tb_applicant_signature.clearText();
                }
                break;

                case R.id.layout_sign_p_tb_collector_eraser: {

                    sign_view_sign_p_tb_collector_signature.clearText();
                }
                break;

                case R.id.btn_sign_p_tb_save: {

                    saveServerUploadSign();
                }
                break;
            }
        }
    };
}