package com.giosis.util.qdrive.singapore.list.delivery;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.giosis.util.qdrive.singapore.MemoryStatus;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.list.SigningView;
import com.giosis.util.qdrive.singapore.util.CommonActivity;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.util.FirebaseEvent;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.OnServerEventListener;
import com.giosis.util.qdrive.singapore.util.PermissionActivity;
import com.giosis.util.qdrive.singapore.util.PermissionChecker;
import com.giosis.util.qdrive.singapore.util.Preferences;


// TODO_kjyoo _TEST
public class QuickReturnedActivity extends CommonActivity {
    String TAG = "QuickReturnedActivity";

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

    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mStrWaybillNo = "";
    String mReceiveType = "RC";
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

        FirebaseEvent.INSTANCE.createEvent(this, TAG);

        //
        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_d_r_tracking_no = findViewById(R.id.text_tracking_no);
        text_sign_d_r_receiver = findViewById(R.id.text_receiver);
        img_sign_d_r_receiver_self = findViewById(R.id.img_receiver_self);
        text_sign_d_r_receiver_self = findViewById(R.id.text_receiver_self);
        img_sign_d_r_receiver_substitute = findViewById(R.id.img_receiver_substitute);
        text_sign_d_r_receiver_substitute = findViewById(R.id.text_receiver_substitute);
        img_sign_d_r_receiver_other = findViewById(R.id.img_receiver_other);
        text_sign_d_r_receiver_other = findViewById(R.id.text_receiver_other);
        text_sign_d_r_sender = findViewById(R.id.text_sender);

        layout_sign_d_r_sign_eraser = findViewById(R.id.layout_sign_eraser);
        sign_view_sign_d_r_signature = findViewById(R.id.sign_signature);
        edit_sign_d_r_memo = findViewById(R.id.edit_memo);
        btn_sign_d_r_save = findViewById(R.id.btn_save);


        layout_top_back.setOnClickListener(clickListener);
        img_sign_d_r_receiver_self.setOnClickListener(clickListener);
        text_sign_d_r_receiver_self.setOnClickListener(clickListener);
        img_sign_d_r_receiver_substitute.setOnClickListener(clickListener);
        text_sign_d_r_receiver_substitute.setOnClickListener(clickListener);
        img_sign_d_r_receiver_other.setOnClickListener(clickListener);
        text_sign_d_r_receiver_other.setOnClickListener(clickListener);
        layout_sign_d_r_sign_eraser.setOnClickListener(clickListener);
        btn_sign_d_r_save.setOnClickListener(clickListener);

        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        deviceID = Preferences.INSTANCE.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");

        mStrWaybillNo = getIntent().getStringExtra("waybillNo");

        getDeliveryInfo(mStrWaybillNo);

        text_top_title.setText(strTitle);
        text_sign_d_r_tracking_no.setText(mStrWaybillNo);
        text_sign_d_r_receiver.setText(receiverName);
        text_sign_d_r_sender.setText(senderName);

        // Memo ????????????
        edit_sign_d_r_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_d_r_memo.length()) {
                    Toast.makeText(QuickReturnedActivity.this, getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        PermissionChecker checker = new PermissionChecker(this);

        // ?????? ?????? ?????? (????????? true, ????????? false)
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

            gpsTrackerManager = new GPSTrackerManager(this);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.gpsTrackerStart();

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(QuickReturnedActivity.this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
            Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

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
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss())
                .show();

    }


    /*
     * ????????? Upload ??????
     * add by jmkang 2014-01-22
     */
    public void saveServerUploadSign() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveServerUploadSign  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            if (!sign_view_sign_d_r_signature.isTouch()) {
                Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }

            String driverMemo = edit_sign_d_r_memo.getText().toString().trim();
            if (driverMemo.equals("")) {
                Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show();
                return;
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            FirebaseEvent.INSTANCE.clickEvent(this, TAG, "setDeliveryRTNDPTypeUploadData");

            new QuickReturnedUploadHelper.Builder(this, opID, officeCode, deviceID,
                    mStrWaybillNo, mReceiveType, sign_view_sign_d_r_signature, driverMemo,
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

            Log.e("Exception", TAG + "  Exception : " + e);
            Toast.makeText(this, getResources().getString(R.string.text_error) + " - " + e, Toast.LENGTH_SHORT).show();
        }
    }


    public void getDeliveryInfo(String barcodeNo) {

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"));
        }

        cursor.close();
    }


    private void AlertShow(String msg) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.text_warning))
                .setMessage(msg)
                .setPositiveButton(getResources().getString(R.string.button_close),
                        (dialog, which) -> {
                            dialog.dismiss(); // ??????
                            finish();
                        })
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (id == R.id.layout_top_back) {
                cancelSigning();
            } else if (id == R.id.img_receiver_self || id == R.id.text_receiver_self) {
                img_sign_d_r_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                img_sign_d_r_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_r_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                mReceiveType = "RC";
            } else if (id == R.id.img_receiver_substitute || id == R.id.text_receiver_substitute) {
                img_sign_d_r_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_r_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                img_sign_d_r_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                mReceiveType = "AG";
            } else if (id == R.id.img_receiver_other || id == R.id.text_receiver_other) {
                img_sign_d_r_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_r_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_r_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);

                mReceiveType = "ET";
            } else if (id == R.id.layout_sign_eraser) {
                sign_view_sign_d_r_signature.clearText();
            } else if (id == R.id.btn_save) {
                saveServerUploadSign();
            }
        }
    };
}