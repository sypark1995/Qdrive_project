package com.giosis.util.qdrive.list.delivery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.OnServerEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

public class DeliveryReturnedActivity extends CommonActivity {
    String TAG = "DeliveryReturnedActivity";

    private static String RECEIVE_TYPE_SELF = "RC";

    //
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_d_r_tracking_no;
    TextView text_sign_d_r_receiver;
    ImageView img_sign_d_r_receiver_self;
    TextView text_sign_d_r_receiver_self;
    ImageView img_sign_d_r_receiver_substitute;
    TextView text_sign_d_r_receiver_substitute;
    ImageView img_sign_d_r_receiver_other;
    TextView text_sign_d_r_receiver_other;
    TextView text_sign_d_r_sender;

    LinearLayout layout_sign_d_r_sign_eraser;
    SigningView sign_view_sign_d_r_signature;
    EditText edit_sign_d_r_memo;
    Button btn_sign_d_r_save;


    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mStrWaybillNo = "";
    String mReceiveType = RECEIVE_TYPE_SELF;
    String mType = "";
    String senderName;
    String receiverName;


    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_returned);

        //
        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_d_r_tracking_no = findViewById(R.id.text_sign_d_r_tracking_no);
        text_sign_d_r_receiver = findViewById(R.id.text_sign_d_r_receiver);
        img_sign_d_r_receiver_self = findViewById(R.id.img_sign_d_r_receiver_self);
        text_sign_d_r_receiver_self = findViewById(R.id.text_sign_d_r_receiver_self);
        img_sign_d_r_receiver_substitute = findViewById(R.id.img_sign_d_r_receiver_substitute);
        text_sign_d_r_receiver_substitute = findViewById(R.id.text_sign_d_r_receiver_substitute);
        img_sign_d_r_receiver_other = findViewById(R.id.img_sign_d_r_receiver_other);
        text_sign_d_r_receiver_other = findViewById(R.id.text_sign_d_r_receiver_other);
        text_sign_d_r_sender = findViewById(R.id.text_sign_d_r_sender);

        layout_sign_d_r_sign_eraser = findViewById(R.id.layout_sign_d_r_sign_eraser);
        sign_view_sign_d_r_signature = findViewById(R.id.sign_view_sign_d_r_signature);
        edit_sign_d_r_memo = findViewById(R.id.edit_sign_d_r_memo);
        btn_sign_d_r_save = findViewById(R.id.btn_sign_d_r_save);


        layout_top_back.setOnClickListener(clickListener);
        img_sign_d_r_receiver_self.setOnClickListener(clickListener);
        text_sign_d_r_receiver_self.setOnClickListener(clickListener);
        img_sign_d_r_receiver_substitute.setOnClickListener(clickListener);
        text_sign_d_r_receiver_substitute.setOnClickListener(clickListener);
        img_sign_d_r_receiver_other.setOnClickListener(clickListener);
        text_sign_d_r_receiver_other.setOnClickListener(clickListener);
        layout_sign_d_r_sign_eraser.setOnClickListener(clickListener);
        btn_sign_d_r_save.setOnClickListener(clickListener);


        // -----
        context = getApplicationContext();
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");
        mType = getIntent().getStringExtra("type");
        mStrWaybillNo = getIntent().getStringExtra("trackingNo");
        String strReceiverName = getIntent().getStringExtra("receiverName");
        String strSenderName = getIntent().getStringExtra("senderName");

        getDeliveryInfo(mStrWaybillNo);

        text_top_title.setText(strTitle);
        text_sign_d_r_tracking_no.setText(mStrWaybillNo);
        text_sign_d_r_receiver.setText(receiverName);
        text_sign_d_r_sender.setText(senderName);

        // Memo 입력제한
        edit_sign_d_r_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_d_r_memo.length()) {
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

                DataUtil.enableLocationSettings(DeliveryReturnedActivity.this, context);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
            Log.e("krm0219", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

            isPermissionTrue = true;
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


    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-01-22
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

            if (!sign_view_sign_d_r_signature.getIsTouche()) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            String driverMemo = edit_sign_d_r_memo.getText().toString().trim();
            if (driverMemo.equals("")) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show();
                return;
            }


            DataUtil.logEvent("button_click", TAG, "setDeliveryRTNDPTypeUploadData");

            new DeliveryReturnedUploadHelper.Builder(this, opID, officeCode, deviceID,
                    mStrWaybillNo, sign_view_sign_d_r_signature, driverMemo, mReceiveType,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            DataUtil.inProgressListPosition = 0;
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


    public void getDeliveryInfo(String barcodeNo) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"));
        }

        if (cursor != null) cursor.close();
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

                case R.id.img_sign_d_r_receiver_self:
                case R.id.text_sign_d_r_receiver_self: {

                    img_sign_d_r_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                    img_sign_d_r_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_r_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                    mReceiveType = RECEIVE_TYPE_SELF;
                }
                break;

                case R.id.img_sign_d_r_receiver_substitute:
                case R.id.text_sign_d_r_receiver_substitute: {

                    img_sign_d_r_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_r_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                    img_sign_d_r_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                    mReceiveType = "AG";
                }
                break;

                case R.id.img_sign_d_r_receiver_other:
                case R.id.text_sign_d_r_receiver_other: {

                    img_sign_d_r_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_r_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_r_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);

                    mReceiveType = "ET";
                }
                break;

                case R.id.layout_sign_d_r_sign_eraser: {

                    sign_view_sign_d_r_signature.clearText();
                }
                break;

                case R.id.btn_sign_d_r_save: {

                    saveServerUploadSign();
                }
                break;
            }
        }
    };
}