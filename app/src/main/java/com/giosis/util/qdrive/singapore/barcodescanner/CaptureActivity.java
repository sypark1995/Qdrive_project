package com.giosis.util.qdrive.singapore.barcodescanner;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.giosis.util.qdrive.singapore.LoginActivity;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.data.CnRPickupResult;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.list.BarcodeData;
import com.giosis.util.qdrive.singapore.list.delivery.DeliveryDoneActivity2;
import com.giosis.util.qdrive.singapore.list.pickup.CnRPickupDoneActivity;
import com.giosis.util.qdrive.singapore.list.pickup.OutletPickupDoneResult;
import com.giosis.util.qdrive.singapore.list.pickup.OutletPickupStep3Activity;
import com.giosis.util.qdrive.singapore.list.pickup.PickupAddScanActivity;
import com.giosis.util.qdrive.singapore.list.pickup.PickupDoneActivity;
import com.giosis.util.qdrive.singapore.list.pickup.PickupTakeBackActivity;
import com.giosis.util.qdrive.singapore.main.DriverAssignResult;
import com.giosis.util.qdrive.singapore.main.submenu.SelfCollectionDoneActivity;
import com.giosis.util.qdrive.singapore.server.RetrofitClient;
import com.giosis.util.qdrive.singapore.util.CommonActivity;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.util.FirebaseEvent;
import com.giosis.util.qdrive.singapore.util.GeoCodeUtil;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.PermissionActivity;
import com.giosis.util.qdrive.singapore.util.PermissionChecker;
import com.giosis.util.qdrive.singapore.util.Preferences;
import com.giosis.util.qdrive.singapore.util.StatueType;
import com.google.gson.Gson;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

// TODO_kjyoo Outlet ?????? ??? ?????????
public final class CaptureActivity extends CommonActivity implements DecoratedBarcodeView.TorchListener, OnTouchListener,
        TextWatcher, OnKeyListener {

    private static final String TAG = "CaptureActivity";

    private static final int REQUEST_DELIVERY_DONE = 10;
    private static final int REQUEST_PICKUP_CNR = 11;
    private static final int REQUEST_PICKUP_ADD_SCAN = 12;
    private static final int REQUEST_PICKUP_TAKE_BACK = 13;
    private static final int REQUEST_SELF_COLLECTION = 20;

    // View
    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_capture_camera;
    TextView text_capture_camera;
    LinearLayout layout_capture_scanner;
    TextView text_capture_scanner;
    ToggleButton toggle_btn_capture_camera_flash;

    LinearLayout layout_capture_scanner_mode;

    EditText edit_capture_type_number;
    Button btn_capture_type_number_add;

    RelativeLayout layout_capture_scan_count;
    TextView text_capture_scan_count;
    RecyclerView recycler_scanned_barcode;

    Button btn_capture_barcode_reset;
    Button btn_capture_barcode_confirm;
    DecoratedBarcodeView barcode_scanner;
    Drawable editTextDelButtonDrawable;

    //
    String opID;
    String officeCode;
    String deviceID;
    EditText currentFocusEditText;
    String mScanType;

    String pickupNo;
    String title;
    String pickupApplicantName;         //2016-09-26 eylee
    String pickupCNRRequester;          //2016-09-03 pickup cnr Requester
    String mQty;
    String mRoute;
    OutletPickupDoneResult resultData;


    int mScanCount = 0;
    String outletDriverYN;              //krm0219  outlet
    ScannedBarcodeAdapter adapter;
    CaptureManager cameraManager;
    ArrayList<String> scannedBarcode = new ArrayList<>();
    private ChangeDriverResult.Data changeDriverResult;

    public boolean isNonQ10QFSOrder = false;
    private ArrayList<BarcodeData> scanBarcodeArrayList = null;
    // resume ??? recreate ??? data list
    private ArrayList<String> barcodeList = new ArrayList<>();

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    InputMethodManager inputMethodManager;
    private BeepManager beepManager;
    private BeepManager beepManagerError;
    private BeepManager beepManagerDuple;

    // NOTIFICATION.  Click Event
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int id = v.getId();
            if (id == R.id.layout_top_back) {

                onResetButtonClick();
                finish();
            } else if (id == R.id.layout_capture_camera) {

                layout_capture_camera.setBackgroundResource(R.drawable.bg_tab_bottom_ff0000);
                layout_capture_scanner.setBackgroundResource(R.drawable.bg_ffffff);
                text_capture_camera.setTextColor(getResources().getColor(R.color.color_ff0000));
                text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.BOLD);
                text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);

                layout_capture_scanner_mode.setVisibility(View.GONE);


                onResume();
            } else if (id == R.id.layout_capture_scanner) {

                layout_capture_camera.setBackgroundResource(R.drawable.bg_ffffff);
                layout_capture_scanner.setBackgroundResource(R.drawable.bg_tab_bottom_ff0000);
                text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                text_capture_scanner.setTextColor(getResources().getColor(R.color.color_ff0000));
                text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.BOLD);

                layout_capture_scanner_mode.setVisibility(View.VISIBLE);

                // Camera
                cameraManager.onPause();

            } else if (id == R.id.btn_capture_type_number_add) {

                onAddButtonClick();
            } else if (id == R.id.btn_capture_barcode_reset) {

                onResetButtonClick();
            } else if (id == R.id.btn_capture_barcode_confirm) {

                switch (mScanType) {
                    case CaptureType.CONFIRM_MY_DELIVERY_ORDER:
                    case CaptureType.CHANGE_DELIVERY_DRIVER:
                        break;
                    case CaptureType.PICKUP_CNR:
                    case CaptureType.PICKUP_SCAN_ALL:
                    case CaptureType.PICKUP_ADD_SCAN:
                    case CaptureType.PICKUP_TAKE_BACK:
                    case CaptureType.OUTLET_PICKUP_SCAN:

                        onNextButtonClick();
                        break;
                    case CaptureType.DELIVERY_DONE:

                        onConfirmButtonClick();
                        break;
                    case CaptureType.SELF_COLLECTION:

                        onCaptureConfirmButtonClick();
                        break;
                }
            }
        }
    };
    boolean mIsScanDeviceListActivityRun = false;


    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA};
    // -------------------------------------------
    private String connectedDeviceName = null;


    /*
     * Qxpress???????????? ??????(??????)
     * ??????????????? ????????? ????????? ??????
     * 10?????? ???????????? false, ?????????????????? KR,SG,QX,JP,CN??? ???????????? false, 5,6????????? ????????? ???????????? false, ??????????????????
      SELF_COLLECTION */
    public static boolean isInvoiceCodeRule(String invoiceNo) {

        if (invoiceNo.length() < 10)
            return false;

        boolean bln = Pattern.matches("^[a-zA-Z0-9]*$", invoiceNo);
        if (!bln) {
            return false;
        }

        if (10 <= invoiceNo.length()) {    // self collection c2c ?????? ???

            String sub_invoice_int = invoiceNo.substring(4, 6);
            return isStringDouble(sub_invoice_int);
        }

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_capture1);

        FirebaseEvent.INSTANCE.createEvent(this, TAG);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_capture_camera = findViewById(R.id.layout_capture_camera);
        text_capture_camera = findViewById(R.id.text_capture_camera);
        layout_capture_scanner = findViewById(R.id.layout_capture_scanner);
        text_capture_scanner = findViewById(R.id.text_capture_scanner);

        barcode_scanner = findViewById(R.id.barcode_scanner);
        toggle_btn_capture_camera_flash = findViewById(R.id.toggle_btn_capture_camera_flash);

        layout_capture_scanner_mode = findViewById(R.id.layout_capture_scanner_mode);

        edit_capture_type_number = findViewById(R.id.edit_capture_type_number);
        btn_capture_type_number_add = findViewById(R.id.btn_capture_type_number_add);

        layout_capture_scan_count = findViewById(R.id.layout_capture_scan_count);
        text_capture_scan_count = findViewById(R.id.text_capture_scan_count);
        recycler_scanned_barcode = findViewById(R.id.recycler_scanned_barcode);

        btn_capture_barcode_reset = findViewById(R.id.btn_capture_barcode_reset);
        btn_capture_barcode_confirm = findViewById(R.id.btn_capture_barcode_confirm);


        layout_top_back.setOnClickListener(clickListener);
        layout_capture_camera.setOnClickListener(clickListener);
        layout_capture_scanner.setOnClickListener(clickListener);
        edit_capture_type_number.setOnClickListener(clickListener);
        btn_capture_type_number_add.setOnClickListener(clickListener);
        btn_capture_barcode_reset.setOnClickListener(clickListener);
        btn_capture_barcode_confirm.setOnClickListener(clickListener);

        edit_capture_type_number.setOnTouchListener(this);
        edit_capture_type_number.addTextChangedListener(this);
        edit_capture_type_number.setOnKeyListener(this);
        edit_capture_type_number.setLongClickable(false);
        edit_capture_type_number.setTextIsSelectable(false);

        editTextDelButtonDrawable = ContextCompat.getDrawable(this, R.drawable.btn_delete);
        editTextDelButtonDrawable.setBounds(0, 0, editTextDelButtonDrawable.getIntrinsicWidth(), editTextDelButtonDrawable.getIntrinsicHeight());


        //------------------
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        deviceID = Preferences.INSTANCE.getDeviceUUID();
        try {

            outletDriverYN = Preferences.INSTANCE.getOutletDriver();
        } catch (Exception e) {
            outletDriverYN = "N";
        }
        Log.e(TAG, "  outletDriverYN : " + outletDriverYN);


        title = getIntent().getStringExtra("title");
        mScanType = getIntent().getStringExtra("type");
        text_top_title.setText(title);


        scanBarcodeArrayList = new ArrayList<>();

        // eylee 2015.10.06
        switch (mScanType) {
            case CaptureType.PICKUP_SCAN_ALL:
            case CaptureType.PICKUP_ADD_SCAN: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
            }
            break;
            case CaptureType.PICKUP_TAKE_BACK: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
                mQty = getIntent().getStringExtra("qty");
            }
            break;
            case CaptureType.OUTLET_PICKUP_SCAN: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
                mQty = getIntent().getStringExtra("qty");
                mRoute = getIntent().getStringExtra("route");
                resultData = (OutletPickupDoneResult) getIntent().getSerializableExtra("tracking_data");

                if (mRoute.equals("FL")) {
                    text_top_title.setText(R.string.text_title_fl_pickup);
                }


                ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeData data = new BarcodeData();
                    data.setState("FAIL");
                    data.setBarcode(listItem.get(i).getTrackingNo());

                    scanBarcodeArrayList.add(i, data);
                }
            }
            break;
        }

        // FIXME_ New
        adapter = new ScannedBarcodeAdapter(scanBarcodeArrayList, mScanType);
        recycler_scanned_barcode.setAdapter(adapter);

        if (0 < scanBarcodeArrayList.size()) {

            recycler_scanned_barcode.scrollToPosition(scanBarcodeArrayList.size() - 1);
        }


        beepManager = new BeepManager(this, BeepManager.BELL_SOUNDS_SUCCESS);
        beepManagerError = new BeepManager(this, BeepManager.BELL_SOUNDS_ERROR);
        beepManagerDuple = new BeepManager(this, BeepManager.BELL_SOUNDS_DUPLE);

        //
        barcode_scanner.setTorchListener(this);
        cameraManager = new CaptureManager(this, barcode_scanner);
        cameraManager.initializeFromIntent(getIntent(), savedInstanceState);

        barcode_scanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {

                boolean exist = false;
                String barcode = result.toString();


                for (int i = 0; i < scannedBarcode.size(); i++) {

                    if (barcode.equals(scannedBarcode.get(i))) {

                        exist = true;
                    }
                }


                if (!exist) {

                    Log.e("Barcode", "Camera   Barcode  " + barcode);
                    scannedBarcode.add(barcode);

                    //    DataUtil.logEvent("capture", TAG, "Camera");
                    checkValidation(barcode, false, "Camera");
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        });

        toggle_btn_capture_camera_flash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                barcode_scanner.setTorchOn();
            } else {

                barcode_scanner.setTorchOff();
            }
        });

        // ?????????
        initManualScanViews(mScanType);

        //
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


    private void initManualScanViews(String scanType) {

        layout_capture_scan_count.setVisibility(View.VISIBLE);

        switch (scanType) {
            case CaptureType.CONFIRM_MY_DELIVERY_ORDER:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_update));      //onUpdateButtonClick
                break;
            case CaptureType.CHANGE_DELIVERY_DRIVER:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_done));         //onUpdateButtonClick
                break;
            case CaptureType.DELIVERY_DONE: {

                layout_capture_scan_count.setVisibility(View.GONE);
                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_confirm));         //onConfirmButtonClick
            }
            break;
            case CaptureType.PICKUP_CNR:
            case CaptureType.PICKUP_SCAN_ALL:
            case CaptureType.PICKUP_ADD_SCAN:
            case CaptureType.PICKUP_TAKE_BACK:
            case CaptureType.OUTLET_PICKUP_SCAN:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_next));            //onNextButtonClick
                break;
            case CaptureType.SELF_COLLECTION:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "   onResume");

        if (Preferences.INSTANCE.getUserId().equals("")) {

            Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show();

            try {
                Intent intent = new Intent(CaptureActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception ignored) {

            }
        }

        try {

            edit_capture_type_number.requestFocus();
        } catch (Exception e) {

            Log.e("Exception", TAG + "  requestFocus Exception : " + e.toString());
        }

        beepManager.updatePrefs();
        beepManagerError.updatePrefs();
        beepManagerDuple.updatePrefs();

        if (isPermissionTrue) {
            // Camera
            cameraManager.onResume();

            // Location
            if (mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)) {

                gpsTrackerManager = new GPSTrackerManager(CaptureActivity.this);
                gpsEnable = gpsTrackerManager.enableGPSSetting();

                if (gpsEnable && gpsTrackerManager != null) {

                    gpsTrackerManager.gpsTrackerStart();
                    latitude = gpsTrackerManager.getLatitude();
                    longitude = gpsTrackerManager.getLongitude();
                    Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
                } else {

                    DataUtil.enableLocationSettings(CaptureActivity.this);
                }
            }
        }


        // Scanned List
        if (mScanType.equals(CaptureType.CONFIRM_MY_DELIVERY_ORDER)
                || mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(CaptureType.PICKUP_CNR)
                || mScanType.equals(CaptureType.PICKUP_SCAN_ALL)
                || mScanType.equals(CaptureType.PICKUP_ADD_SCAN)
                || mScanType.equals(CaptureType.PICKUP_TAKE_BACK)
                || mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {

            try {

                scanBarcodeArrayList.clear();
                adapter.notifyDataSetChanged();

                if (mScanType.equals(CaptureType.CONFIRM_MY_DELIVERY_ORDER)
                        || mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)
                        || mScanType.equals(CaptureType.PICKUP_CNR)
                        || mScanType.equals(CaptureType.PICKUP_SCAN_ALL)
                        || mScanType.equals(CaptureType.PICKUP_ADD_SCAN)
                        || mScanType.equals(CaptureType.PICKUP_TAKE_BACK)) {

                    if (barcodeList != null && 0 < barcodeList.size()) {
                        for (int i = 0; i < barcodeList.size(); i++) {

                            BarcodeData data = new BarcodeData();
                            data.setState("SUCCESS");
                            data.setBarcode(barcodeList.get(i));
                            scanBarcodeArrayList.add(0, data);

                            scannedBarcode.add(barcodeList.get(i));
                        }

                        mScanCount = scanBarcodeArrayList.size();
                        text_capture_scan_count.setText(String.valueOf(mScanCount));

                        adapter.notifyDataSetChanged();
                        recycler_scanned_barcode.smoothScrollToPosition(0);
                    }
                } else if (mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {

                    mScanCount = 0;

                    if (resultData != null) {

                        ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();

                        for (int i = 0; i < listItem.size(); i++) {

                            String tracking_no = listItem.get(i).getTrackingNo();
                            boolean isScanned = listItem.get(i).isScanned();

                            BarcodeData data = new BarcodeData();
                            data.setBarcode(tracking_no);

                            if (isScanned) {

                                data.setState("SUCCESS");
                                mScanCount++;

                                scannedBarcode.add(tracking_no);
                            } else {

                                data.setState("FAIL");
                            }

                            scanBarcodeArrayList.add(i, data);
                        }
                    }

                    text_capture_scan_count.setText(String.valueOf(mScanCount));

                    adapter.notifyDataSetChanged();
                    recycler_scanned_barcode.smoothScrollToPosition(0);
                }

                Log.e("krm0219", TAG + "  Scan Count : " + mScanCount);
            } catch (Exception e) {

                Toast.makeText(CaptureActivity.this, getResources().getString(R.string.text_data_error), Toast.LENGTH_SHORT).show();

                scanBarcodeArrayList.clear();
                adapter.notifyDataSetChanged();
                removeBarcodeListInstance();
            }
        } else if (mScanType.equals(CaptureType.SELF_COLLECTION)) {

            text_top_title.setText(getResources().getString(R.string.text_title_scan_barcode));
            btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_next));

            scanBarcodeArrayList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    // EditText
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mScanType.equals(CaptureType.PICKUP_CNR)
                || mScanType.equals(CaptureType.PICKUP_SCAN_ALL) || mScanType.equals(CaptureType.PICKUP_ADD_SCAN)
                || mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN) || mScanType.equals(CaptureType.PICKUP_TAKE_BACK)) {

            // TEST.
            if (opID.equals("karam.kim")) {

                Log.e(TAG, "  EditText onTouch  > karam !!");
                inputMethodManager.showSoftInput(edit_capture_type_number, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {

            inputMethodManager.showSoftInput(edit_capture_type_number, InputMethodManager.SHOW_IMPLICIT);
        }


        if (event.getAction() != MotionEvent.ACTION_UP) {
            return false;
        }

        EditText editText = (EditText) v;

        if (event.getX() > (editText.getWidth() - editText.getPaddingRight() - editTextDelButtonDrawable.getIntrinsicWidth())) {
            editText.setText("");
            editText.setCompoundDrawables(null, null, null, null);
        }

        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (currentFocusEditText != null) {
            Editable inputText = currentFocusEditText.getText();
            Drawable delDrawable = editTextDelButtonDrawable;

            if (inputText == null || inputText.toString().isEmpty()) {
                delDrawable = null;
            }

            currentFocusEditText.setCompoundDrawables(null, null, delDrawable, null);
            currentFocusEditText.setCompoundDrawablePadding(28);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }


    private void AlertShow(String msg) {

        if (!CaptureActivity.this.isFinishing()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
            builder.setTitle(getResources().getString(R.string.text_warning));
            builder.setMessage(msg);
            builder.setPositiveButton(getResources().getString(R.string.button_close), (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            builder.show();
        }
    }


    public void removeBarcodeListInstance() {
        if (barcodeList != null) {
            barcodeList.clear();
        }
    }


    // Scanner
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) {

            String tempStrScanNo = edit_capture_type_number.getText().toString().trim();

            if (!tempStrScanNo.equals("")) {

                boolean isDuplicate = false;

                for (int i = 0; i < scannedBarcode.size(); i++) {

                    if (scannedBarcode.get(i).equalsIgnoreCase(tempStrScanNo)) {
                        isDuplicate = true;
                    }
                }

                if (!isDuplicate) {

                    scannedBarcode.add(tempStrScanNo);
                }
                Log.i(TAG, "  onKey  KEYCODE_ENTER : " + tempStrScanNo + " / " + isDuplicate + "  //  " + event.getAction());

                if (mScanType.equals(CaptureType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER) ||
                        mScanType.equals(CaptureType.PICKUP_CNR)
                        || mScanType.equals(CaptureType.PICKUP_SCAN_ALL) || mScanType.equals(CaptureType.PICKUP_ADD_SCAN)
                        || mScanType.equals(CaptureType.PICKUP_TAKE_BACK) || mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {


                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                }

                checkValidation(tempStrScanNo, isDuplicate, "onKey KEYCODE_ENTER");
            }
            return true;
        }

        return false;
    }


    // EditText
    public void onAddButtonClick() {

        String inputBarcodeNumber = edit_capture_type_number.getText().toString().trim().toUpperCase();

        if (0 < inputBarcodeNumber.length()) {

            boolean isDuplicate = false;

            for (int i = 0; i < scannedBarcode.size(); i++) {

                if (scannedBarcode.get(i).equalsIgnoreCase(inputBarcodeNumber)) {
                    isDuplicate = true;
                }
            }

            if (!isDuplicate) {

                scannedBarcode.add(inputBarcodeNumber);
            }
            Log.i(TAG, "  onAddButtonClick > " + inputBarcodeNumber + " / " + isDuplicate);

            checkValidation(inputBarcodeNumber, isDuplicate, "onAddButtonClick");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult " + requestCode + " / " + resultCode);

        switch (requestCode) {


            case REQUEST_DELIVERY_DONE: {
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                }
            }
            break;

            case REQUEST_SELF_COLLECTION:
            case REQUEST_PICKUP_CNR: {

                onResetButtonClick();

                if (resultCode == Activity.RESULT_OK) {

                    finish();
                }
            }
            break;

            case REQUEST_PICKUP_ADD_SCAN:
            case REQUEST_PICKUP_TAKE_BACK: {

                onResetButtonClick();

                if (resultCode == Activity.RESULT_OK) {

                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
            break;

            case PERMISSION_REQUEST_CODE: {
                if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                    isPermissionTrue = true;
                }
            }
            break;
        }
    }


    // NOTIFICATION.  Add Barcode List
    private void addScannedBarcode(String barcodeNo, String where) {
        Log.e(TAG, "  addScannedBarcode  > " + where + " // " + barcodeNo);

        mScanCount++;
        text_capture_scan_count.setText(String.valueOf(mScanCount));


        BarcodeData data = new BarcodeData();
        data.setBarcode(barcodeNo.toUpperCase());
        data.setState("NONE");

        if (mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)) {

            data.setBarcode(changeDriverResult.getTrackingNo() + "  |  " + changeDriverResult.getStatus() + "  |  " + changeDriverResult.getCurrentDriver());
        }


        switch (mScanType) {
            case CaptureType.CONFIRM_MY_DELIVERY_ORDER:
            case CaptureType.CHANGE_DELIVERY_DRIVER:
            case CaptureType.PICKUP_CNR:
            case CaptureType.PICKUP_SCAN_ALL:
            case CaptureType.PICKUP_ADD_SCAN:
            case CaptureType.PICKUP_TAKE_BACK:
                // ?????? ??? ?????? ????????? ???????????? ?????? ?????? ?????????.
                data.setState("SUCCESS");

                if (mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)) {

                    barcodeList.add(changeDriverResult.getTrackingNo() + "  |  " + changeDriverResult.getStatus() + "  |  " + changeDriverResult.getCurrentDriver());
                } else {

                    barcodeList.add(barcodeNo);
                }

                scanBarcodeArrayList.add(0, data);
                adapter.notifyDataSetChanged();
                recycler_scanned_barcode.smoothScrollToPosition(0);

                break;
            case CaptureType.OUTLET_PICKUP_SCAN:

                ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();
                int position = -400;

                for (int i = 0; i < listItem.size(); i++) {

                    String tracking_no = listItem.get(i).getTrackingNo();

                    if (tracking_no.equalsIgnoreCase(barcodeNo)) {
                        Log.e("krm0219", "Compare : " + tracking_no + " vs " + barcodeNo);
                        position = i;
                        data.setState("SUCCESS");
                        listItem.get(i).setScanned(true);
                    }
                }


                if (0 <= position) {

                    Log.e("krm0219", " Position : " + position);
                    barcodeList.add(barcodeNo);

                    scanBarcodeArrayList.set(position, data);
                    adapter.notifyDataSetChanged();
                    recycler_scanned_barcode.smoothScrollToPosition(0);
                } else {

                    mScanCount--;
                    text_capture_scan_count.setText(String.valueOf(mScanCount));

                    adapter.notifyDataSetChanged();

                    if (!CaptureActivity.this.isFinishing()) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CaptureActivity.this);
                        alertDialog.setTitle(getResources().getString(R.string.text_warning));
                        alertDialog.setMessage(getResources().getString(R.string.msg_no_outlet_parcels));
                        alertDialog.setPositiveButton(getResources().getString(R.string.button_ok),
                                (dialog, which) -> dialog.dismiss());
                        alertDialog.show();
                    }
                }
                break;
            default:
                //?????? ??? ?????? ????????? ???????????? ????????? ?????????.
                // maybe.. DELIVERY DONE, SELF COLLECTION
                scanBarcodeArrayList.add(data);
                adapter.notifyDataSetChanged();
                break;
        }

        if (!mScanType.equals(CaptureType.CONFIRM_MY_DELIVERY_ORDER) && !mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)
                && !mScanType.equals(CaptureType.PICKUP_CNR)) {

            updateInvoiceNO(mScanType, barcodeNo);
        }

        edit_capture_type_number.setText("");
        inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
    }


    // Add Barcode  (Validation Check / Add List)
    // NOTIFICATION.  Barcode Validation Check
    private void checkValidation(String strBarcodeNo, boolean isDuplicate, String where) {
        Log.e(TAG, "checkValidation called > " + where);
        Log.e(TAG, "checkValidation - " + strBarcodeNo + "  Duplicate : " + isDuplicate);

        strBarcodeNo = strBarcodeNo.replaceAll("\\r\\n|\\r|\\n", "");

        if (!NetworkUtil.isNetworkAvailable(CaptureActivity.this)) {
            AlertShow(getResources().getString(R.string.msg_network_connect_error));
            return;
        }


        if (isDuplicate) {

            beepManagerDuple.playBeepSoundAndVibrate();
            Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 20);
            toast.show();

            edit_capture_type_number.setText("");
            inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
            return;
        }


        switch (mScanType) {
            case CaptureType.CONFIRM_MY_DELIVERY_ORDER: {

                final String scanNo = strBarcodeNo;
                String type = "STD";

                if (outletDriverYN.equals("Y"))
                    type = "OL";        // Outlet

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckDpc3Out(strBarcodeNo, type, Preferences.INSTANCE.getUserId(),
                                DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "requestValidationCheckDpc3Out  result  " + it.getResultCode());

                            if (it.getResultCode() < 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        if (dialog1 != null)
                                            dialog1.dismiss();
                                    });

                                    builder.show();
                                }
                            } else {

                                beepManager.playBeepSoundAndVibrate();
                                addScannedBarcode(scanNo, "checkValidation - CONFIRM_MY_DELIVERY_ORDER");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
                break;
            }
            case CaptureType.CHANGE_DELIVERY_DRIVER: {

                final String scanNo = strBarcodeNo;

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckChangeDriver(strBarcodeNo, Preferences.INSTANCE.getUserId(),
                                DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "result  " + it.getResultCode());


                            if (it.getResultCode() < 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        if (dialog1 != null)
                                            dialog1.dismiss();
                                    });

                                    builder.show();
                                }
                            } else {

                                beepManager.playBeepSoundAndVibrate();
                                changeDriverResult = new Gson().fromJson(it.getResultObject(), ChangeDriverResult.Data.class);
                                addScannedBarcode(scanNo, "checkValidation - CHANGE_DELIVERY_DRIVER");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
                break;
            }
            case CaptureType.PICKUP_CNR: {  //2016-09-21 add type validation

                // 2016-09-01 eylee ????????? ????????? ???????????? ???????????? ??????
                // ????????? ????????? ???????????? ????????? ?????? ??????????????? addBarcode
                // sqlite ??? cnr barcode scan no ??? ????????? ???????????? insert ?????? sqlite validation ?????? ??????
                // validation ???????????? ???, editext ??? ?????? ????????????, alert ????????? editText ??? ???????????? ??????
                // ???????????? sqlite ??? insert

                // Edit.  2020.03  ?????? (?????? CNR ?????? ????????? > ?????? ??????X ??????)    by krm0219
                final String scanNo = strBarcodeNo;

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckCnR(strBarcodeNo, Preferences.INSTANCE.getUserId(),
                                DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "result  " + it.getResultCode() + it.getResultObject());

                            if (it.getResultCode() != 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        if (dialog1 != null)
                                            dialog1.dismiss();
                                    });

                                    builder.show();
                                }
                            } else {

                                CnRPickupResult cnRPickupData = new Gson().fromJson(it.getResultObject(), CnRPickupResult.class);

                                boolean isDBDuplicate = checkDBDuplicate(cnRPickupData.getContrNo(), cnRPickupData.getInvoiceNo());
                                Log.e(TAG, "  DB Duplicate  > " + isDBDuplicate + " / " + cnRPickupData.getInvoiceNo());

                                if (isDBDuplicate) {

                                    getCnrRequester(cnRPickupData.getInvoiceNo());
                                } else {

                                    insertCnRData(cnRPickupData);
                                }

                                beepManager.playBeepSoundAndVibrate();
                                pickupCNRRequester = cnRPickupData.getReqName();
                                addScannedBarcode(scanNo, "checkValidation - PICKUP_CNR");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
            }
            break;
            case CaptureType.PICKUP_SCAN_ALL: {

                final String scanNo = strBarcodeNo;

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckPickup(pickupNo, strBarcodeNo, "QX",
                                Preferences.INSTANCE.getUserId(), DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "result  " + it.getResultCode());

                            if (it.getResultCode() < 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        dialog1.dismiss();
                                    });
                                    builder.show();
                                }
                            } else {

                                beepManager.playBeepSoundAndVibrate();
                                addScannedBarcode(scanNo, "checkValidation - PICKUP_SCAN_ALL");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
                break;
            }
            case CaptureType.PICKUP_ADD_SCAN: {

                final String scanNo = strBarcodeNo;

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckPickup(pickupNo, scanNo, "QX",
                                Preferences.INSTANCE.getUserId(), DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "result  " + it.getResultCode());

                            if (it.getResultCode() < 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        dialog1.dismiss();
                                    });
                                    builder.show();
                                }
                            } else {

                                beepManager.playBeepSoundAndVibrate();
                                addScannedBarcode(scanNo, "checkValidation - PICKUP_ADD_SCAN");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
                break;
            }
            case CaptureType.PICKUP_TAKE_BACK: {

                final String scanNo = strBarcodeNo;

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckTakeBack(pickupNo, scanNo, Preferences.INSTANCE.getUserId(),
                                DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "result  " + it.getResultCode());

                            if (it.getResultCode() < 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        dialog1.dismiss();
                                    });
                                    builder.show();
                                }
                            } else {

                                beepManager.playBeepSoundAndVibrate();
                                addScannedBarcode(scanNo, "checkValidation - PICKUP_TAKE_BACK");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());
                break;
            }
            case CaptureType.OUTLET_PICKUP_SCAN: {

                final String scanNo = strBarcodeNo;

                RetrofitClient.INSTANCE.instanceDynamic().requestValidationCheckPickup(pickupNo, strBarcodeNo, mRoute,
                                Preferences.INSTANCE.getUserId(), DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                            Log.e("Server", "result  " + it.getResultCode());

                            if (it.getResultCode() < 0) {

                                beepManagerError.playBeepSoundAndVibrate();
                                edit_capture_type_number.setText("");
                                inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                scannedBarcode.remove(scanNo);

                                if (!CaptureActivity.this.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.text_scanned_failed));
                                    builder.setMessage(it.getResultMsg());
                                    builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which) -> {

                                        dialog1.dismiss();
                                    });
                                    builder.show();
                                }
                            } else {

                                beepManager.playBeepSoundAndVibrate();
                                addScannedBarcode(scanNo, "checkValidation - OUTLET_PICKUP_SCAN");
                            }
                        }, it -> Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show());

//                new OutletPickupScanValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo, mRoute)
//                        .setOnPickupAddScanNoOneByOneUploadListener(result -> {
//
//                            if (result.getResultCode() < 0) {
//
                //  beepManagerError.playBeepSoundAndVibrate();
//                                deletePrevious(scanNo);
//                                edit_capture_type_number.setText("");
//                            } else {
//
                //  beepManager.playBeepSoundAndVibrate();
//                                addScannedBarcode(scanNo, "checkValidation - OUTLET_PICKUP_SCAN");
//                            }
//                        }).build().execute();

                break;
            }
            case CaptureType.SELF_COLLECTION: {     // 2016-09-20 eylee

                if (!isInvoiceCodeRule(strBarcodeNo)) {

                    beepManagerError.playBeepSoundAndVibrate();
                    Toast toast = Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    return;
                }

                beepManager.playBeepSoundAndVibrate();

                //2016-09-12 eylee nq ????????? self collector ???????????? ????????????
                if (!scanBarcodeArrayList.isEmpty()) {

                    boolean tempIsNonQ10QFSOrder = isNonQ10QFSOrder;
                    boolean tempValidation = isNonQ10QFSOrderForSelfCollection(strBarcodeNo);

                    if (tempIsNonQ10QFSOrder != tempValidation) {
                        // alert ???????????? ??? type ??? ???????????? - ?????? Self - Collection ??? ?????? NQ ???
                        Toast toast = Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_different_order_type), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    } else {
                        isNonQ10QFSOrder = tempValidation;
                    }
                } else {
                    isNonQ10QFSOrder = isNonQ10QFSOrderForSelfCollection(strBarcodeNo);
                }

                addScannedBarcode(strBarcodeNo, "checkValidation - SELF_COLLECTION");
                break;
            }
            default: {

                beepManager.playBeepSoundAndVibrate();
                addScannedBarcode(strBarcodeNo, "checkValidation - Default");
            }
        }
    }


    private boolean checkDBDuplicate(String contrNo, String invoiceNo) {

        String selectQuery = "SELECT  partner_ref_no, invoice_no, stat, rcv_nm, sender_nm "
                + " FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no= '" + invoiceNo + "'" + " and contr_no= '" + contrNo + "'";
        Cursor cs = DatabaseHelper.getInstance().get(selectQuery);

        Log.e("krm0219", "DATA >>>>> " + contrNo + " / " + invoiceNo + " ==== " + cs.getCount());
        return 0 < cs.getCount();
    }

    private String getCnrRequester(String invoiceNo) {

        String requester = "";
        String barcodeNo = invoiceNo.trim().toUpperCase();

        String selectQuery = "SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no = '" + barcodeNo + "'";
        Cursor cursor = DatabaseHelper.getInstance().get(selectQuery);

        if (0 < cursor.getCount()) {
            if (cursor.moveToFirst()) {
                do {
                    requester = cursor.getString(cursor.getColumnIndex("req_nm"));
                } while (cursor.moveToNext());
            }
        }

        return requester;
    }


    private String insertCnRData(CnRPickupResult data) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        try {

            ContentValues contentVal = new ContentValues();
            contentVal.put("contr_no", data.getContrNo());
            contentVal.put("partner_ref_no", data.getPartnerRefNo());
            contentVal.put("invoice_no", data.getPartnerRefNo());
            contentVal.put("stat", data.getStat());
            contentVal.put("tel_no", data.getTelNo());
            contentVal.put("hp_no", data.getHpNo());
            contentVal.put("zip_code", data.getZipCode());
            contentVal.put("address", data.getAddress());
            contentVal.put("route", data.getRoute());
            contentVal.put("type", StatueType.TYPE_PICKUP);
            contentVal.put("desired_date", data.getPickupHopeDay());
            contentVal.put("req_qty", data.getQty());
            contentVal.put("req_nm", data.getReqName());
            contentVal.put("rcv_request", data.getDelMemo());
            contentVal.put("sender_nm", "");
            contentVal.put("punchOut_stat", "N");
            contentVal.put("reg_id", "");
            contentVal.put("reg_dt", regDataString);
            contentVal.put("fail_reason", data.getFailReason());
            contentVal.put("secret_no_type", "");
            contentVal.put("secret_no", "");

            contentVal.put("lat", "0");
            contentVal.put("lng", "0");

            contentVal.put("state", "");
            contentVal.put("city", "");
            contentVal.put("street", "");

            DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
        } catch (Exception ignored) {

        }

        return data.getReqName();
    }

    private boolean insertDriverAssignInfo(DriverAssignResult.QSignDeliveryList assignInfo) {

        String opId = Preferences.INSTANCE.getUserId();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        // eylee 2015.08.26 add non q10 - contr_no ??? sqlite ?????? ??? ????????? ???????????? ?????? add start
        String contr_no = assignInfo.getContrNo();
        int cnt = DataUtil.getContrNoCount(contr_no);
        Log.e("TAG", "insertDriverAssignInfo  check count : " + cnt);
        if (0 < cnt) {
            DataUtil.deleteContrNo(contr_no);
        }

        // eylee 2015.08.26 add end
        //?????? ??? ??????????????? ????????? ??????
        ContentValues contentVal = new ContentValues();
        contentVal.put("contr_no", assignInfo.getContrNo());
        contentVal.put("partner_ref_no", assignInfo.getPartnerRefNo());
        contentVal.put("invoice_no", assignInfo.getInvoiceNo());
        contentVal.put("stat", assignInfo.getStat());
        contentVal.put("rcv_nm", assignInfo.getRcvName());
        contentVal.put("sender_nm", assignInfo.getSenderName());
        contentVal.put("tel_no", assignInfo.getTelNo());
        contentVal.put("hp_no", assignInfo.getHpNo());
        contentVal.put("zip_code", assignInfo.getZipCode());
        contentVal.put("address", assignInfo.getAddress());
        contentVal.put("rcv_request", assignInfo.getDelMemo());
        contentVal.put("delivery_dt", assignInfo.getDeliveryFirstDate());
        contentVal.put("type", StatueType.TYPE_DELIVERY);
        contentVal.put("route", assignInfo.getRoute());
        contentVal.put("reg_id", opId);
        contentVal.put("reg_dt", regDataString);
        contentVal.put("punchOut_stat", "N");
        contentVal.put("driver_memo", assignInfo.getDriverMemo());
        contentVal.put("fail_reason", assignInfo.getFailReason());
        contentVal.put("secret_no_type", assignInfo.getSecretNoType());
        contentVal.put("secret_no", assignInfo.getSecretNo());
        contentVal.put("secure_delivery_yn", assignInfo.getSecureDeliveryYN());
        contentVal.put("parcel_amount", assignInfo.getParcelAmount());
        contentVal.put("currency", assignInfo.getCurrency());
        contentVal.put("order_type_etc", assignInfo.getOrder_type_etc());

        // 2020.06 ???, ?????? ??????
        String[] latLng = GeoCodeUtil.getLatLng(assignInfo.getLat_lng());
        contentVal.put("lat", latLng[0]);
        contentVal.put("lng", latLng[1]);

        // 2021.04  High Value
        contentVal.put("high_amount_yn", assignInfo.getHigh_amount_yn());

        contentVal.put("state", assignInfo.getState());
        contentVal.put("city", assignInfo.getCity());
        contentVal.put("street", assignInfo.getStreet());

        long insertCount = DatabaseHelper.getInstance().insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
        return insertCount >= 0;
    }

    // NOTIFICATION.  Scan - Delivery Done
    public void onConfirmButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

            Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }


        String name;
        String receiverName = "";
        boolean diffReceiverName = false;
        ArrayList<BarcodeData> deliveryBarcodeList = new ArrayList<>();

        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            BarcodeData BarcodeData = scanBarcodeArrayList.get(i);

            if (BarcodeData.getState().equals("SUCCESS")) {

                name = getDeliveryReceiver(BarcodeData.getBarcode());

                try {

                    // ?????????????????? ????????????
                    if (!receiverName.equals("")) {
                        if (!receiverName.toUpperCase().equals(name.toUpperCase())) {
                            diffReceiverName = true;
                        }
                    }
                } catch (Exception e) {

                    diffReceiverName = true;
                }

                receiverName = name;
                deliveryBarcodeList.add(BarcodeData);
            }
        }

        // ??????????????? ???????????? ?????? ?????????
        if (diffReceiverName) {

            Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_different_recipient_address, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }


        if (0 < deliveryBarcodeList.size()) {

            Intent intent = new Intent(this, DeliveryDoneActivity2.class);
            intent.putExtra("data", deliveryBarcodeList);
            this.startActivityForResult(intent, REQUEST_DELIVERY_DONE);
        } else {

            Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }


    // NOTIFICATION.  Pickup (CnR / Scan All / Add Scan / Take Back / Outlet)
    public void onNextButtonClick() {

        if (mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {

            boolean isScanned = false;

            for (int i = 0; i < resultData.getResultObject().getTrackingNoList().size(); i++) {

                if (resultData.getResultObject().getTrackingNoList().get(i).isScanned()) {
                    isScanned = true;
                }
            }

            if (!isScanned) {

                Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
        } else {

            if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

                Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
        }


        String scannedQty = String.valueOf(scanBarcodeArrayList.size());
        StringBuilder scannedList = new StringBuilder();

        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            scannedList.append(scanBarcodeArrayList.get(i).getBarcode());

            if (i != (scanBarcodeArrayList.size() - 1)) {
                scannedList.append(",");
            }
        }

        removeBarcodeListInstance();
        switch (mScanType) {
            case CaptureType.PICKUP_CNR: {

                Intent intent = new Intent(this, CnRPickupDoneActivity.class);
                intent.putExtra("senderName", pickupCNRRequester);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_CNR);
                break;
            }
            case CaptureType.PICKUP_SCAN_ALL: {

                Intent intent = new Intent(this, PickupDoneActivity.class);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("scannedQty", scannedQty);
                startActivity(intent);
                finish();
                break;
            }
            case CaptureType.PICKUP_ADD_SCAN: {

                Intent intent = new Intent(this, PickupAddScanActivity.class);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_ADD_SCAN);
                break;
            }
            case CaptureType.PICKUP_TAKE_BACK: {

                Intent intent = new Intent(this, PickupTakeBackActivity.class);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("totalQty", mQty);
                intent.putExtra("takeBackQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_TAKE_BACK);
                break;
            }
            case CaptureType.OUTLET_PICKUP_SCAN: {

                int scanned_qty = 0;
                for (int i = 0; i < resultData.getResultObject().getTrackingNoList().size(); i++) {
                    if (resultData.getResultObject().getTrackingNoList().get(i).isScanned()) {

                        scanned_qty++;
                    }
                }

                StringBuilder scanned_list = new StringBuilder();
                for (int i = 0; i < resultData.getResultObject().getTrackingNoList().size(); i++) {

                    if (resultData.getResultObject().getTrackingNoList().get(i).isScanned()) {

                        if (!scanned_list.toString().equals("")) {
                            scanned_list.append(",");
                        }

                        scanned_list.append(resultData.getResultObject().getTrackingNoList().get(i).getTrackingNo());
                    }
                }

                Intent intent = new Intent(this, OutletPickupStep3Activity.class);
                intent.putExtra("title", title);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("qty", mQty);
                intent.putExtra("route", mRoute);
                intent.putExtra("scannedQty", scanned_qty);
                intent.putExtra("tracking_data", resultData);
                intent.putExtra("scannedList", scanned_list.toString());
                startActivity(intent);
                finish();
                break;
            }
        }
    }


    // NOTIFICATION.  Reset
    public void onResetButtonClick() {

        if (scanBarcodeArrayList != null && !scanBarcodeArrayList.isEmpty()) {

            mScanCount = 0;
            text_capture_scan_count.setText(String.valueOf(mScanCount));

            scanBarcodeArrayList.clear();
            adapter.notifyDataSetChanged();

            if (mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {

                ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeData data = new BarcodeData();
                    data.setBarcode(listItem.get(i).getTrackingNo());
                    data.setState("FAIL");
                    scanBarcodeArrayList.add(i, data);
                }
                adapter.notifyDataSetChanged();
            }
        }

        if (!scannedBarcode.isEmpty()) {

            scannedBarcode.clear();
        }

        if (mScanType.equals(CaptureType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(CaptureType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(CaptureType.PICKUP_CNR)
                || mScanType.equals(CaptureType.PICKUP_SCAN_ALL) || mScanType.equals(CaptureType.PICKUP_ADD_SCAN)
                || mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN) || mScanType.equals(CaptureType.PICKUP_TAKE_BACK)) {

            removeBarcodeListInstance();
        }
    }


    public String getDeliveryReceiver(String barcodeNo) {

        String name = "";
        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {

            name = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
        }

        cursor.close();

        return name;
    }


    @Override
    public synchronized void onPause() {
        super.onPause();

        cameraManager.onPause();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        cameraManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cameraManager.onDestroy();
        onResetButtonClick();

        DataUtil.stopGPSManager(gpsTrackerManager);

        if (beepManager != null) {
            beepManager.destroy();
        }
        if (beepManagerError != null) {
            beepManagerError.destroy();
        }
        if (beepManagerDuple != null) {
            beepManagerDuple.destroy();
        }
    }

    //2016-09-12 eylee  self-collection nq ?????? ????????? ????????????
    public boolean isNonQ10QFSOrderForSelfCollection(String barcodeNo) {
        boolean isNQ = false;

        int len = barcodeNo.length();
        String ScanNoLast = barcodeNo.substring(len - 2).toUpperCase();
        if (ScanNoLast.equals("NQ")) {
            isNQ = true;
        }
        // return ?????? isNonQ10QFSOrder ????????? setting ??????
        return isNQ;
    }

    /*
     * update delivery set stat = @stat , chg_id = localStorage.getItem('opId')
     * , chg_dt = datetime('now') where invoice_no = @invoice_no COLLATE NOCASE
     * and punchOut_stat <> 'S' and reg_id = localStorage.getItem('opId')
     */
    private void updateInvoiceNO(String scanType, String invoiceNo) {

        int updateCount = 0;

        if (scanType.equals(CaptureType.PICKUP_SCAN_ALL) || scanType.equals(CaptureType.PICKUP_ADD_SCAN)
                || scanType.equals(CaptureType.PICKUP_TAKE_BACK) || scanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {

            updateCount = 1;
        } else if (mScanType.equals(CaptureType.DELIVERY_DONE)) {
            // ????????? ???????????? ??????????????? ???????????? ?????? ????????? jmkang 2013-05-08

            ContentValues contentVal = new ContentValues();
            contentVal.put("reg_id", opID); // ?????? ??????????????? ????????? ????????????????????? ????????????
            updateCount = DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?", new String[]{invoiceNo, opID});
        } else if (mScanType.equals(CaptureType.SELF_COLLECTION)) {

            if (isInvoiceCodeRule(invoiceNo)) {
                updateCount = 1;
            }
        }


        String message = String.format(" [ %s ] ", title);
        String result;
        String inputBarcode = scanBarcodeArrayList.get(scanBarcodeArrayList.size() - 1).getBarcode();

        if (updateCount < 1) {

            message += getResources().getString(R.string.text_not_assigned);
            result = "FAIL";
        } else {

            message += getResources().getString(R.string.text_success);
            result = "SUCCESS";
        }

        if (!mScanType.equals(CaptureType.OUTLET_PICKUP_SCAN)) {

            BarcodeData data = new BarcodeData();
            data.setBarcode(inputBarcode);
            data.setState(result);

            scanBarcodeArrayList.set(scanBarcodeArrayList.size() - 1, data);
            adapter.notifyDataSetChanged();
        }

        // ?????? ??? Adapter.notifyDataSetChanged() ???????????? listview  ?????? add comment by eylee 2016-09-08
        if (updateCount < 1) { // ??????????????? ????????????.

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {

                vibrator.vibrate(200L);
            }

            Toast toast = Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);
            toast.show();
        }
    }

    // NOTIFICATION.  SELF_COLLECTION
    /*
     * ???????????? ????????? ????????? ???????????? ?????????????????? ???????????? ????????????.
     * modified : 2016-09-09 eylee self-collection ?????? ??? ?????? add
     */
    public void onCaptureConfirmButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (!isInvoiceCodeRule(scanBarcodeArrayList.get(0).getBarcode())) {

            Toast toast = Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        // SELF_COLLECTION            //????????? ???????????? self-collection by 2016-09-09
        // ????????? ????????? ????????? ??????????????? ????????? ?????????.
        ArrayList<BarcodeData> newBarcodeNoList = new ArrayList<>();
        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            BarcodeData barcodeListData = scanBarcodeArrayList.get(i);

            if (barcodeListData.getState().equals("FAIL")) {

                Toast toast = Toast.makeText(this, R.string.msg_invalid_scan, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            } else {

                newBarcodeNoList.add(barcodeListData);
            }
        }


        if (0 < newBarcodeNoList.size()) {

            Intent intent = new Intent(this, SelfCollectionDoneActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("data", newBarcodeNoList);
            intent.putExtra("nonq10qfs", String.valueOf(isNonQ10QFSOrder));    //09-12 add isNonQ10QFSOrder
            this.startActivityForResult(intent, REQUEST_SELF_COLLECTION);
        } else {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    public static boolean isStringDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onTorchOn() {

    }

    @Override
    public void onTorchOff() {

    }
}