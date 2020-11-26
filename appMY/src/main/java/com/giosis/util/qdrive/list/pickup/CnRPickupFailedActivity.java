package com.giosis.util.qdrive.list.pickup;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.OnServerEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.list.BarcodeData;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class CnRPickupFailedActivity extends CommonActivity {
    String TAG = "CnRPickupFailedActivity";


    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_cnr_f_pickup_no;
    TextView text_sign_cnr_f_requestor;
    TextView text_sign_cnr_f_request_qty;

    RelativeLayout layout_sign_cnr_f_failed_reason;
    TextView text_sign_cnr_f_failed_reason;
    RelativeLayout layout_sign_cnr_f_retry_date;
    TextView text_sign_cnr_f_retry_date;
    EditText edit_sign_cnr_f_memo;
    Button btn_sign_cnr_f_save;


    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    Spinner spin;
    String[] failReasonCode = {"WA", "WP", "NA", "NO", "NR", "NQ", "ET"}; //Wrong address, Wrong phone number,No answer,No one available,Not ready for parcel,Others

    DatePicker datePicker;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener dateListener;

    String mStrWaybillNo = "";
    String mReceiveType = "RC";
    String mType = "";
    ArrayList<BarcodeData> PickupNoList;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cnr_fail);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_cnr_f_pickup_no = findViewById(R.id.text_sign_cnr_f_pickup_no);
        text_sign_cnr_f_requestor = findViewById(R.id.text_sign_cnr_f_requestor);
        text_sign_cnr_f_request_qty = findViewById(R.id.text_sign_cnr_f_request_qty);

        layout_sign_cnr_f_failed_reason = findViewById(R.id.layout_sign_cnr_f_failed_reason);
        text_sign_cnr_f_failed_reason = findViewById(R.id.text_sign_cnr_f_failed_reason);
        layout_sign_cnr_f_retry_date = findViewById(R.id.layout_sign_cnr_f_retry_date);
        text_sign_cnr_f_retry_date = findViewById(R.id.text_sign_cnr_f_retry_date);
        edit_sign_cnr_f_memo = findViewById(R.id.edit_sign_cnr_f_memo);
        btn_sign_cnr_f_save = findViewById(R.id.btn_sign_cnr_f_save);


        layout_top_back.setOnClickListener(clickListener);
        btn_sign_cnr_f_save.setOnClickListener(clickListener);


        //
        context = getApplicationContext();
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");
        mStrWaybillNo = getIntent().getStringExtra("waybillNo");
        String strSenderName = getIntent().getStringExtra("senderName");
        String strReqQty = getIntent().getStringExtra("reqQty");
        mType = getIntent().getStringExtra("type");         // "P"

        BarcodeData failBarcodeData = new BarcodeData();
        failBarcodeData.setBarcode(mStrWaybillNo);
        failBarcodeData.setState(mType);

        PickupNoList = new ArrayList<>();
        PickupNoList.add(failBarcodeData);

        datePicker = new DatePicker(CnRPickupFailedActivity.this);
        myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.DATE, 1); // 내일로 셋팅

        //Date 선택 시
        dateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                Calendar maxDay = Calendar.getInstance();
                maxDay.add(Calendar.DATE, 7);

                long dateDiff = maxDay.getTimeInMillis() - myCalendar.getTimeInMillis();
                int diff = (int) (dateDiff / (24 * 60 * 60 * 1000));

                String restDay = getRestDay(year, monthOfYear + 1, dayOfMonth);

                if (diff < 0) { // 7일 이상 선택 시

                    Toast calToast = Toast.makeText(CnRPickupFailedActivity.this, context.getResources().getString(R.string.msg_choose_one_week), Toast.LENGTH_SHORT);
                    calToast.setGravity(Gravity.CENTER, 0, 10);
                    calToast.show();
                    text_sign_cnr_f_retry_date.setText(getString(R.string.text_select));
                    //mNextPickupDate.performClick();
                } else if (diff > 6) { //과거 선택 시

                    Toast calToast = Toast.makeText(CnRPickupFailedActivity.this, context.getResources().getString(R.string.msg_choose_one_week), Toast.LENGTH_SHORT);
                    calToast.setGravity(Gravity.CENTER, 0, 10);
                    calToast.show();
                    //mNextPickupDate.performClick();
                    text_sign_cnr_f_retry_date.setText(getString(R.string.text_select));
                } else if (myCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

                    Toast calToast = Toast.makeText(CnRPickupFailedActivity.this, context.getResources().getString(R.string.msg_choose_sunday_error), Toast.LENGTH_SHORT);
                    calToast.setGravity(Gravity.CENTER, 0, 10);
                    calToast.show();
                    //mNextPickupDate.performClick();
                    text_sign_cnr_f_retry_date.setText(getString(R.string.text_select));
                } else if (!restDay.isEmpty()) {    //휴무일 선택 시

                    Toast calToast = Toast.makeText(CnRPickupFailedActivity.this, restDay + context.getResources().getString(R.string.msg_choose_another_day), Toast.LENGTH_SHORT);
                    calToast.setGravity(Gravity.CENTER, 0, 10);
                    calToast.show();
                    text_sign_cnr_f_retry_date.setText(getString(R.string.text_select));
                } else {

                    String myFormat = "yyyy-MM-dd";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    text_sign_cnr_f_retry_date.setText(sdf.format(myCalendar.getTime()));
                }
            }
        };

        //버튼 클릭 시 DatePickerDialog 띄우기
        layout_sign_cnr_f_retry_date.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(CnRPickupFailedActivity.this,
                        dateListener,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //스피너 Fail_Reason 바인딩
        spin = findViewById(R.id.spinner_fail);
        ArrayAdapter<CharSequence> spin_adapter = ArrayAdapter.createFromResource(this, R.array.fail_reason_array, android.R.layout.simple_spinner_item);
        spin_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(spin_adapter);
        spin.setPrompt(context.getResources().getString(R.string.text_failed_reason));

        layout_sign_cnr_f_failed_reason.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 최초에 Gone으로 설정 해야 Select 문구를 보여줄 수 있고 버튼 클릭 시
                // Invisible로 값을 바꿔서 스피너 선택 리스너 setOnItemSelectedListener를 호출 할 수 있다
                spin.setVisibility(View.INVISIBLE);
                spin.performClick();
            }

        });

        //Reason 스피너 선택 시
        spin.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View arg1, int position, long arg3) {

                String selected_text = parentView.getItemAtPosition(position).toString();
                text_sign_cnr_f_failed_reason.setText(selected_text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        text_top_title.setText(strTitle);
        text_sign_cnr_f_pickup_no.setText(mStrWaybillNo);
        text_sign_cnr_f_requestor.setText(strSenderName);
        text_sign_cnr_f_request_qty.setText(strReqQty);

        // Memo 입력제한
        edit_sign_cnr_f_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_cnr_f_memo.length()) {
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

                DataUtil.enableLocationSettings(CnRPickupFailedActivity.this, context);
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
                Log.e("krm0219", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }

    View.OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    cancelSigning();
                }
                break;

                case R.id.btn_sign_cnr_f_save: {

                    saveServerUploadSign();
                }
                break;
            }
        }
    };

    private String getRestDay(int year, int month, int day) {

        String rest_dt = "";
        String rtn = "";
        String s_year = Integer.toString(year);
        String s_month = Integer.toString(month);
        String s_day = Integer.toString(day);

        if (s_month.length() == 1) {
            s_month = "0" + s_month;
        }
        if (s_day.length() == 1) {
            s_day = "0" + s_day;
        }
        rest_dt = s_year + "-" + s_month + "-" + s_day;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cs = dbHelper.get("SELECT title FROM " + DatabaseHelper.DB_TABLE_REST_DAYS + " WHERE rest_dt = '" + rest_dt + "'");

        if (cs != null && cs.moveToFirst()) {
            rtn = cs.getString(cs.getColumnIndex("title"));
        } else {

            rtn = null;
        }

        return rtn;
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

            if (text_sign_cnr_f_failed_reason.getText().equals(getString(R.string.text_select))) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.msg_select_fail_reason), Toast.LENGTH_SHORT).show();
                return;
            } else if (text_sign_cnr_f_retry_date.getText().equals(getString(R.string.text_select))) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.msg_select_retry_date), Toast.LENGTH_SHORT).show();
                return;
            }

            String driverMemo = edit_sign_cnr_f_memo.getText().toString();

            if (spin.getSelectedItemPosition() == failReasonCode.length - 1 && driverMemo.isEmpty()) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.msg_must_enter_memo), Toast.LENGTH_SHORT).show();
                return;
            }

            String retry_day = text_sign_cnr_f_retry_date.getText().toString();
            String realQty = "0";
            String fail_code = failReasonCode[spin.getSelectedItemPosition()];


            DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadPickupData);

            new CnRPickupUploadHelper.Builder(this, opID, officeCode, deviceID,
                    PickupNoList, null, null,
                    driverMemo, mReceiveType, mType, realQty,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude, "PF", fail_code, retry_day)
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            DataUtil.inProgressListPosition = 0;
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
}

