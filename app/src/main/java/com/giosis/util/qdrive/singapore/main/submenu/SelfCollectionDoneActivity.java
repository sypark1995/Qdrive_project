package com.giosis.util.qdrive.singapore.main.submenu;


import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.giosis.util.qdrive.singapore.MemoryStatus;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.list.BarcodeData;
import com.giosis.util.qdrive.singapore.list.SigningView;
import com.giosis.util.qdrive.singapore.util.CommonActivity;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.util.FirebaseEvent;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.PermissionActivity;
import com.giosis.util.qdrive.singapore.util.PermissionChecker;
import com.giosis.util.qdrive.singapore.util.Preferences;

import java.util.ArrayList;


public class SelfCollectionDoneActivity extends CommonActivity {
    String TAG = "SelfCollectionDoneActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_tracking_no_title;
    TextView text_sign_tracking_no;
    TextView text_sign_tracking_no_more;

    RelativeLayout layout_sign_receiver;
    TextView text_sign_receiver_title;
    TextView text_sign_receiver;
    RelativeLayout layout_sign_receiver_check;
    ImageView img_sign_receiver_self;
    TextView text_sign_receiver_self;
    ImageView img_sign_receiver_substitute;
    TextView text_sign_receiver_substitute;
    ImageView img_sign_receiver_other;
    TextView text_sign_receiver_other;
    RelativeLayout layout_sign_sender;
    TextView text_sign_sender_title;
    TextView text_sign_sender;

    LinearLayout layout_sign_sign_eraser;
    SigningView sign_view_sign_signature;
    EditText edit_sign_memo;
    Button btn_sign_save;


    String mReceiveType = "RC";
    ArrayList<BarcodeData> barcodeList;
    ArrayList<BarcodeData> songjanglist;

    String senderName;
    String receiverName;

    boolean isNonQ10QFSOrder = false;
    int tempSize = 0;
    String tempList = "";

    //
    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_COARSE_LOCATION, PermissionChecker.ACCESS_FINE_LOCATION};


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (id == R.id.layout_top_back) {

                cancelSigning();
            } else if (id == R.id.img_sign_receiver_self || id == R.id.text_sign_receiver_self) {

                img_sign_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                img_sign_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                mReceiveType = "RC";
            } else if (id == R.id.img_sign_receiver_substitute || id == R.id.text_sign_receiver_substitute) {

                img_sign_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                img_sign_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                mReceiveType = "AG";
            } else if (id == R.id.img_sign_receiver_other || id == R.id.text_sign_receiver_other) {

                img_sign_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);

                mReceiveType = "ET";
            } else if (id == R.id.layout_sign_sign_eraser) {

                sign_view_sign_signature.clearText();
            } else if (id == R.id.btn_sign_save) {

                saveServerUploadSign();
            }
        }
    };


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_collection);

        FirebaseEvent.INSTANCE.createEvent(this, TAG);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_tracking_no_title = findViewById(R.id.text_sign_tracking_no_title);
        text_sign_tracking_no = findViewById(R.id.text_sign_tracking_no);
        text_sign_tracking_no_more = findViewById(R.id.text_sign_tracking_no_more);
        layout_sign_receiver = findViewById(R.id.layout_sign_receiver);
        text_sign_receiver_title = findViewById(R.id.text_sign_receiver_title);
        text_sign_receiver = findViewById(R.id.text_sign_receiver);
        layout_sign_receiver_check = findViewById(R.id.layout_sign_receiver_check);
        img_sign_receiver_self = findViewById(R.id.img_sign_receiver_self);
        text_sign_receiver_self = findViewById(R.id.text_sign_receiver_self);
        img_sign_receiver_substitute = findViewById(R.id.img_sign_receiver_substitute);
        text_sign_receiver_substitute = findViewById(R.id.text_sign_receiver_substitute);
        img_sign_receiver_other = findViewById(R.id.img_sign_receiver_other);
        text_sign_receiver_other = findViewById(R.id.text_sign_receiver_other);
        layout_sign_sender = findViewById(R.id.layout_sign_sender);
        text_sign_sender_title = findViewById(R.id.text_sign_sender_title);
        text_sign_sender = findViewById(R.id.text_sign_sender);

        layout_sign_sign_eraser = findViewById(R.id.layout_sign_sign_eraser);
        sign_view_sign_signature = findViewById(R.id.sign_view_sign_signature);
        edit_sign_memo = findViewById(R.id.edit_sign_memo);
        btn_sign_save = findViewById(R.id.btn_sign_save);

        layout_top_back.setOnClickListener(clickListener);
        img_sign_receiver_self.setOnClickListener(clickListener);
        text_sign_receiver_self.setOnClickListener(clickListener);
        img_sign_receiver_substitute.setOnClickListener(clickListener);
        text_sign_receiver_substitute.setOnClickListener(clickListener);
        img_sign_receiver_other.setOnClickListener(clickListener);
        text_sign_receiver_other.setOnClickListener(clickListener);
        layout_sign_sign_eraser.setOnClickListener(clickListener);
        btn_sign_save.setOnClickListener(clickListener);


        //---------
        String strTitle = getIntent().getStringExtra("title");
        String temp_qfs_order = getIntent().getStringExtra("nonq10qfs");
        //바코드 정보리스트 인텐트로 받음 ArrayList 시리얼라이즈화  add by jmkang 2013-05-09
        barcodeList = (ArrayList<BarcodeData>) getIntent().getSerializableExtra("data");
        Log.e(TAG, "  QFS Order : " + temp_qfs_order);


        // 단건 다수건 바코드정보에 대한 바코드정보 리스트 재정의 songjanglist
        songjanglist = new ArrayList<>();

        for (int i = 0; i < barcodeList.size(); i++) {

            BarcodeData songData = new BarcodeData();
            songData.setBarcode(barcodeList.get(i).getBarcode());
            songData.setState(barcodeList.get(i).getState());
            songjanglist.add(songData);
        }


        String barcodeMsg = "";
        int songJangListSize = songjanglist.size();
        for (int i = 0; i < songJangListSize; i++) {
            barcodeMsg += songjanglist.get(i).getBarcode().toUpperCase() + "  ";
        }

        // 디비로 부터 운송장 정보를 바탕으로 배달정보(주문자, 구매자) 를 습득
        getDeliveryInfo(songjanglist.get(0).getBarcode());

        isNonQ10QFSOrder = Boolean.valueOf(temp_qfs_order);

        if (isNonQ10QFSOrder) {

            text_sign_tracking_no_title.setText(R.string.text_receiver);
            text_sign_receiver_title.setText(R.string.text_parcels);
            text_sign_sender_title.setText(R.string.text_detail_list);

            layout_sign_receiver_check.setVisibility(View.GONE);
            layout_sign_sender.setVisibility(View.VISIBLE);

            tempSize = songJangListSize;
            tempList = barcodeMsg;

            text_sign_tracking_no.setText(getResources().getString(R.string.text_non_q10_qfs));
            String qtyFormat = String.format(getResources().getString(R.string.text_total_qty_count), songJangListSize);
            text_sign_receiver.setText(qtyFormat);
            text_sign_sender.setText(barcodeMsg);
        } else {
            if (1 < songJangListSize) {

                layout_sign_sender.setVisibility(View.GONE);
                String qtyFormat = String.format(getResources().getString(R.string.text_total_qty_count), songJangListSize);
                text_sign_tracking_no.setText(qtyFormat);
                text_sign_tracking_no_more.setVisibility(View.VISIBLE);
                text_sign_tracking_no_more.setText(barcodeMsg);
            } else {

                text_sign_tracking_no.setText(barcodeMsg);
                text_sign_tracking_no_more.setVisibility(View.GONE);
            }
        }

        text_top_title.setText(strTitle);
        text_sign_receiver.setText(receiverName);
        text_sign_sender.setText(senderName);


        // Memo 입력제한
        edit_sign_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_memo.length()) {
                    Toast.makeText(SelfCollectionDoneActivity.this, getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
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

        // Self-Collector 경우 서버로부터 수취인, 셀러명을 가지고 온다. (비동기)
        FirebaseEvent.INSTANCE.clickEvent(this, TAG, "GetContrInfo");

        //2016-09-12 eylee        // 배송상태값에 따른 정보 습득
        // 콜백으로 받은 결과값을 쓰레드를 이용하여 TextView 갱신한다.
        new SelfCollectionShippingInfoHelper.Builder(songjanglist)
                .setOnShippingInfoEventListener(resultList -> runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                            ShippingInfoResult list = resultList.get(0);
                            ShippingInfoResult.ShippingInfo alist = list.getResultObject();

                            // 바코드스캔한 번호가 전부 NQ 이면 "Non-Q10 QFS (Route: Other)"
                            if (isNonQ10QFSOrder) {

                                String qtyFormat = String.format(getResources().getString(R.string.text_total_qty_count), tempSize);

                                text_sign_receiver.setText(qtyFormat);
                                text_sign_sender.setText(tempList);
                            } else {

//                                        text_sign_receiver.setText(alist.get(0));
//                                        text_sign_sender.setText(alist.get(1));
                                text_sign_receiver.setText(alist.getRev_nm());
                                text_sign_sender.setText(alist.getCust_nm());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })).build().execute();
    }

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {

                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    /*
     * Self-Collection 처리
     * add by jmkang 2013-12-31
     */
    public void saveServerUploadSign() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (!sign_view_sign_signature.isTouch()) {
                Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                return;
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            String driverMemo = edit_sign_memo.getText().toString();

            FirebaseEvent.INSTANCE.clickEvent(this, TAG, "SetSelfCollectorData");

            new SelfCollectionDoneHelper.Builder(this, Preferences.INSTANCE.getUserId(), Preferences.INSTANCE.getOfficeCode(), Preferences.INSTANCE.getDeviceUUID(),
                    songjanglist, sign_view_sign_signature, driverMemo, mReceiveType)
                    .setOnSelfCollectorEventListener(new SelfCollectionDoneHelper.OnSelfCollectorEventListener() {

                        @Override
                        public void onPostResult() {
                        }

                        @Override
                        public void onPostFailList() {

                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    }).build().execute();
        } catch (Exception e) {

            Log.e("Exception", TAG + "  Exception : " + e.toString());
            Toast.makeText(SelfCollectionDoneActivity.this, getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getDeliveryInfo(String barcodeNo) {

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"));
        }

        cursor.close();
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
}