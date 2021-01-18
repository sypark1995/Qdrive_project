package com.giosis.util.qdrive.list.pickup;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.library.server.data.FailedCodeResult;
import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Camera2APIs;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/***
 * @author eylee
 * @date 2016-09-30
 * Pickup Cancel -> Visit log 로 기능 개선
 *
 *
 * @editor krm0219
 * 2019.12 - Fail Reason / Retry Date 추가
 * 2020.06  CNR Failed 합치기
 */
public class PickupFailedActivity extends CommonActivity implements Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {
    String TAG = "PickupFailedActivity";

    //
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_p_f_pickup_no;
    TextView text_sign_p_f_applicant_title;
    TextView text_sign_p_f_applicant;
    TextView text_sign_p_f_requested_qty;

    RelativeLayout layout_sign_p_f_failed_reason;
    TextView text_sign_p_f_failed_reason;
    Spinner spinner_p_f_failed_reason;
    RelativeLayout layout_sign_p_f_retry_date;
    TextView text_sign_p_f_retry_date;

    LinearLayout layout_sign_p_f_memo;
    EditText edit_sign_p_f_memo;
    LinearLayout layout_sign_p_f_take_photo;
    LinearLayout layout_sign_p_f_gallery;
    TextureView texture_sign_p_f_preview;
    ImageView img_sign_p_f_preview_bg;
    ImageView img_sign_p_f_visit_log;
    Button btn_sign_p_f_save;


    //---
    Context context;
    String pickupType;      //  P, CNR
    String opID = "";
    String officeCode = "";
    String deviceID = "";
    String pickupNo;
    String rcvType;         // VL, RC


    DatePickerDialog datePickerDialog;
    Calendar mCalendar;
    DatePickerDialog.OnDateSetListener dateListener;

    // Camera & Gallery
    Camera2APIs camera2;
    String cameraId;
    private static final int RESULT_LOAD_IMAGE = 3;
    boolean isGalleryActivate = false;


    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA};


    ArrayList<FailedCodeResult.FailedCode> arrayList;
    ArrayList<String> failedCodeArrayList;
    ArrayAdapter<String> failedCodeArrayAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_pickup_visit_log);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_p_f_pickup_no = findViewById(R.id.text_sign_p_f_pickup_no);
        text_sign_p_f_applicant_title = findViewById(R.id.text_sign_p_f_applicant_title);
        text_sign_p_f_applicant = findViewById(R.id.text_sign_p_f_applicant);
        text_sign_p_f_requested_qty = findViewById(R.id.text_sign_p_f_requested_qty);

        layout_sign_p_f_failed_reason = findViewById(R.id.layout_sign_p_f_failed_reason);
        text_sign_p_f_failed_reason = findViewById(R.id.text_sign_p_f_failed_reason);
        spinner_p_f_failed_reason = findViewById(R.id.spinner_p_f_failed_reason);
        layout_sign_p_f_retry_date = findViewById(R.id.layout_sign_p_f_retry_date);
        text_sign_p_f_retry_date = findViewById(R.id.text_sign_p_f_retry_date);

        layout_sign_p_f_memo = findViewById(R.id.layout_sign_p_f_memo);
        edit_sign_p_f_memo = findViewById(R.id.edit_sign_p_f_memo);
        layout_sign_p_f_take_photo = findViewById(R.id.layout_sign_p_f_take_photo);
        layout_sign_p_f_gallery = findViewById(R.id.layout_sign_p_f_gallery);
        texture_sign_p_f_preview = findViewById(R.id.texture_sign_p_f_preview);
        img_sign_p_f_preview_bg = findViewById(R.id.img_sign_p_f_preview_bg);
        img_sign_p_f_visit_log = findViewById(R.id.img_sign_p_f_visit_log);
        btn_sign_p_f_save = findViewById(R.id.btn_sign_p_f_save);


        //------------
        context = getApplicationContext();
        camera2 = new Camera2APIs(this);
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        pickupType = getIntent().getStringExtra("type");
        pickupNo = getIntent().getStringExtra("pickupNo");
        String applicant = getIntent().getStringExtra("applicant");
        String strReqQty = getIntent().getStringExtra("reqQty");


        text_top_title.setText(context.getResources().getString(R.string.text_visit_log));
        text_sign_p_f_pickup_no.setText(pickupNo);
        text_sign_p_f_applicant.setText(applicant);
        text_sign_p_f_requested_qty.setText(strReqQty);
        DisplayUtil.setPreviewCamera(img_sign_p_f_preview_bg);

        if (pickupType.equals(BarcodeType.TYPE_PICKUP)) {

            text_sign_p_f_applicant_title.setText(context.getResources().getString(R.string.text_applicant));
            rcvType = "VL";
        } else if (pickupType.equals(BarcodeType.TYPE_CNR)) {

            text_sign_p_f_applicant_title.setText(context.getResources().getString(R.string.text_requestor));
            rcvType = "RC";
        }


        //
        mCalendar = Calendar.getInstance();

        dateListener = (view, year, monthOfYear, dayOfMonth) -> {
            Log.i("krm0219", "DATE : " + year + " / " + monthOfYear + " / " + dayOfMonth);

            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String restDay = getRestDay(year, monthOfYear + 1, dayOfMonth);

            // NOTIFICATION
            // 2020.06  SG 일요일도 픽업 업무 시작  --> 일요일도 재시도 날짜에 포함 및 재시도 날짜선택 D+3일로 수정
            if (!restDay.isEmpty()) {    //휴무일 선택 시

                Toast calToast = Toast.makeText(PickupFailedActivity.this, restDay + context.getResources().getString(R.string.msg_choose_another_day), Toast.LENGTH_SHORT);
                calToast.setGravity(Gravity.CENTER, 0, 10);
                calToast.show();
                text_sign_p_f_retry_date.setText(getString(R.string.text_select));
            } else {

                String myFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                text_sign_p_f_retry_date.setText(sdf.format(mCalendar.getTime()));
            }
        };


        datePickerDialog = new DatePickerDialog(PickupFailedActivity.this,
                dateListener,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));


        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_YEAR, 1);
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 3);
        // 2020.06  재시도 날짜 D+3일로 수정
        /* //TEST
        minDate.set(2020, 8-1, 8);
        maxDate.set(2020, 8-1, 10);*/

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        layout_sign_p_f_retry_date.setOnClickListener(v -> datePickerDialog.show());


        // 2020.12  Pickup Failed Code
        setFailedCode();

        layout_sign_p_f_failed_reason.setOnClickListener(v -> {

            spinner_p_f_failed_reason.performClick();
        });

        spinner_p_f_failed_reason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View arg1, int position, long arg3) {

                String selectedText = parentView.getItemAtPosition(position).toString();
                Log.e("krm0219", "spinner  " + position + " / " + selectedText);


                if (selectedText.toUpperCase().contains(context.getResources().getString(R.string.text_other).toUpperCase())) {

                    layout_sign_p_f_memo.setVisibility(View.VISIBLE);
                } else {

                    layout_sign_p_f_memo.setVisibility(View.GONE);
                }

                text_sign_p_f_failed_reason.setText(selectedText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        //
        layout_top_back.setOnClickListener(view -> cancelSigning());

        layout_sign_p_f_take_photo.setOnClickListener(view -> {

            if (cameraId != null) {

                camera2.takePhoto(texture_sign_p_f_preview, img_sign_p_f_visit_log);
            } else {

                Toast.makeText(PickupFailedActivity.this, context.getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
            }
        });

        layout_sign_p_f_gallery.setOnClickListener(view -> getImageFromAlbum());


        btn_sign_p_f_save.setOnClickListener(v -> saveServerUploadSign());


        // Memo 입력제한
        edit_sign_p_f_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_p_f_memo.length()) {
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


    private void setFailedCode() {

        arrayList = DataUtil.getFailCode("P");

        if (arrayList == null) {

            AlertShow(context.getResources().getString(R.string.msg_failed_code_error));
        } else {

            failedCodeArrayList = new ArrayList<>();

            for (int i = 0; i < arrayList.size(); i++) {

                FailedCodeResult.FailedCode failedCode = arrayList.get(i);
                failedCodeArrayList.add(failedCode.getFailedString());
            }

            spinner_p_f_failed_reason.setPrompt(context.getResources().getString(R.string.text_failed_reason));
            failedCodeArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, failedCodeArrayList);
            failedCodeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_p_f_failed_reason.setAdapter(failedCodeArrayAdapter);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isPermissionTrue) {

            // Camera
            camera2 = new Camera2APIs(this);

            if (texture_sign_p_f_preview.isAvailable()) {

                openCamera();
            } else {

                texture_sign_p_f_preview.setSurfaceTextureListener(this);
            }

            // Location
            gpsTrackerManager = new GPSTrackerManager(context);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(PickupFailedActivity.this, context);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isGalleryActivate = false;

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            try {

                Uri selectedImageUri = data.getData();

                Bitmap selectedImage = Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                Bitmap resizeBitmap = camera2.getResizeBitmap(selectedImage);
                img_sign_p_f_visit_log.setImageBitmap(resizeBitmap);
                img_sign_p_f_visit_log.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                onResume();
            } catch (Exception e) {
                Log.e("eylee", e.toString());
                e.printStackTrace();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        cancelSigning();
    }

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> finish())
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    /*
     * 실시간 Upload 처리
     * 2016-10-04 eylee added
     * visit log 페이지에서 저장 버튼 눌렀을 때
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
                Log.e("Location", TAG + " saveServerUploadSign  : " + latitude + "  " + longitude + "  ");
            }

            if (text_sign_p_f_failed_reason.getText().equals(getString(R.string.text_select))) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.msg_select_fail_reason), Toast.LENGTH_SHORT).show();
                return;
            }
            if (text_sign_p_f_retry_date.getText().equals(getString(R.string.text_select))) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.msg_select_retry_date), Toast.LENGTH_SHORT).show();
                return;
            }


            // other 선택시에만 메모 필수
            FailedCodeResult.FailedCode code = arrayList.get(spinner_p_f_failed_reason.getSelectedItemPosition());
            String failedCode = code.getFailedCode();
            Log.e("krm0219", "PickupFailedUploadHelper   failedCode  " + failedCode);
            String retry_day = text_sign_p_f_retry_date.getText().toString();

            String driverMemo = "";

            if (code.getFailedString().toUpperCase().contains(context.getResources().getString(R.string.text_other).toUpperCase())) {
                driverMemo = edit_sign_p_f_memo.getText().toString().trim();

                if (driverMemo.equals("")) {
                    Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            if (!camera2.hasImage(img_sign_p_f_visit_log)) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_visit_photo_require), Toast.LENGTH_SHORT).show();
                return;
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }


            com.giosis.library.util.DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadPickupData);

            new PickupFailedUploadHelper.Builder(this, opID, officeCode, deviceID,
                    rcvType, pickupNo, failedCode, retry_day, driverMemo, img_sign_p_f_visit_log,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
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

            Log.e("Server", TAG + "  Exception : " + e.toString());
            Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void AlertShow(String msg) {

        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                (dialog, which) -> {
                    dialog.dismiss(); // 닫기
                    finish();
                });
        alert_internet_status.show();
    }

    private String getRestDay(int year, int month, int day) {

        String rest_dt;
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
        Log.i("krm0219", TAG + "  RestDay 1 : " + rest_dt);

        Cursor cs = DatabaseHelper.getInstance().get("SELECT title FROM " + DatabaseHelper.DB_TABLE_REST_DAYS + " WHERE rest_dt = '" + rest_dt + "'");

        if (cs != null && cs.moveToFirst()) {
            rtn = cs.getString(cs.getColumnIndex("title"));
        }

        Log.i("krm0219", TAG + "  RestDay 2 : " + rtn);

        return rtn;
    }

    // Gallery
    private void getImageFromAlbum() {
        try {

            if (!isGalleryActivate) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                isGalleryActivate = true;
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        } catch (Exception ex) {
            isGalleryActivate = false;
            Log.i("eylee", ex.toString());
        }
    }

    // CAMERA
    private void openCamera() {

        CameraManager cameraManager = camera2.getCameraManager(this);
        cameraId = camera2.getCameraCharacteristics(cameraManager);
        Log.i("krm0219", TAG + "  openCamera " + cameraId);

        if (cameraId != null) {

            camera2.setCameraDevice(cameraManager, cameraId);
        } else {

            Toast.makeText(PickupFailedActivity.this, context.getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {

        camera2.closeCamera();
    }


    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize, int rotation) {

        texture_sign_p_f_preview.setRotation(rotation);

        SurfaceTexture texture = texture_sign_p_f_preview.getSurfaceTexture();
        texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface surface = new Surface(texture);

        camera2.setCaptureSessionRequest(cameraDevice, surface);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}