package com.giosis.library.barcodescanner;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
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
import androidx.recyclerview.widget.RecyclerView;

import com.giosis.library.BuildConfig;
import com.giosis.library.MemoryStatus;
import com.giosis.library.R;
import com.giosis.library.barcodescanner.bluetooth.BluetoothChatService;
import com.giosis.library.barcodescanner.bluetooth.DeviceListActivity;
import com.giosis.library.barcodescanner.bluetooth.KScan;
import com.giosis.library.barcodescanner.bluetooth.KTSyncData;
import com.giosis.library.barcodescanner.helper.ChangeDriverHelper;
import com.giosis.library.barcodescanner.helper.ConfirmMyOrderHelper;
import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.library.list.BarcodeData;
import com.giosis.library.list.delivery.DeliveryDoneActivity;
import com.giosis.library.list.pickup.CnRPickupDoneActivity;
import com.giosis.library.list.pickup.OutletPickupDoneResult;
import com.giosis.library.list.pickup.OutletPickupStep3Activity;
import com.giosis.library.list.pickup.PickupAddScanActivity;
import com.giosis.library.list.pickup.PickupDoneActivity;
import com.giosis.library.list.pickup.PickupTakeBackActivity;
import com.giosis.library.main.DriverAssignResult;
import com.giosis.library.main.submenu.SelfCollectionDoneActivity;
import com.giosis.library.server.RetrofitClient;
import com.giosis.library.server.data.CnRPickupResult;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.GeoCodeUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.PermissionActivity;
import com.giosis.library.util.PermissionChecker;
import com.giosis.library.util.Preferences;
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

// TODO_ Outlet 확인 후 옮기기
public final class CaptureActivity extends CommonActivity implements DecoratedBarcodeView.TorchListener, OnTouchListener,
        TextWatcher, OnKeyListener {
    private static final String TAG = "CaptureActivity";
    private static final String bluetoothTAG = "Capture_Bluetooth";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_DELIVERY_DONE = 10;
    private static final int REQUEST_PICKUP_CNR = 11;
    private static final int REQUEST_PICKUP_ADD_SCAN = 12;
    private static final int REQUEST_PICKUP_TAKE_BACK = 13;
    private static final int REQUEST_SELF_COLLECTION = 20;


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_EXIT = 0;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DISPLAY = 6;
    public static final int MESSAGE_SEND = 7;
    public static final int MESSAGE_SETTING = 255;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    // View
    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_capture_camera;
    TextView text_capture_camera;
    LinearLayout layout_capture_scanner;
    TextView text_capture_scanner;
    LinearLayout layout_capture_bluetooth;
    TextView text_capture_bluetooth;
    public BluetoothDevice connectedDevice = null;
    ToggleButton toggle_btn_capture_camera_flash;

    LinearLayout layout_capture_scanner_mode;
    LinearLayout layout_capture_bluetooth_mode;
    TextView text_capture_bluetooth_connect_state;
    TextView text_capture_bluetooth_device_name;
    Button btn_capture_bluetooth_device_find;

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
    // resume 시 recreate 할 data list
    private ArrayList<String> barcodeList = new ArrayList<>();

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    InputMethodManager inputMethodManager;
    private BeepManager beepManager;
    private BeepManager beepManagerError;
    private BeepManager beepManagerDuple;

    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayList<ChangeDriverResult.Data> changeDriverObjectArrayList = new ArrayList<>();
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
                layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_ffffff);
                text_capture_camera.setTextColor(getResources().getColor(R.color.color_ff0000));
                text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.BOLD);
                text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);
                text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.NORMAL);

                layout_capture_scanner_mode.setVisibility(View.GONE);
                layout_capture_bluetooth_mode.setVisibility(View.GONE);

                // bluetooth
                if (KTSyncData.mChatService != null)
                    KTSyncData.mChatService.stop();
                KTSyncData.bIsRunning = false;

                onResume();
            } else if (id == R.id.layout_capture_scanner) {

                layout_capture_camera.setBackgroundResource(R.drawable.bg_ffffff);
                layout_capture_scanner.setBackgroundResource(R.drawable.bg_tab_bottom_ff0000);
                layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_ffffff);
                text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                text_capture_scanner.setTextColor(getResources().getColor(R.color.color_ff0000));
                text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.BOLD);
                text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.NORMAL);

                layout_capture_scanner_mode.setVisibility(View.VISIBLE);
                layout_capture_bluetooth_mode.setVisibility(View.GONE);

                // Camera
                cameraManager.onPause();

                // bluetooth
                if (KTSyncData.mChatService != null)
                    KTSyncData.mChatService.stop();
                KTSyncData.bIsRunning = false;
            } else if (id == R.id.layout_capture_bluetooth) {

                layout_capture_camera.setBackgroundResource(R.drawable.bg_ffffff);
                layout_capture_scanner.setBackgroundResource(R.drawable.bg_ffffff);
                layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_tab_bottom_ff0000);
                text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);
                text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_ff0000));
                text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.BOLD);

                layout_capture_scanner_mode.setVisibility(View.GONE);
                layout_capture_bluetooth_mode.setVisibility(View.VISIBLE);

                // Camera
                cameraManager.onPause();

                // Bluetooth 지원 && 비활성화 상태
                if (mBluetoothAdapter != null) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                    }
                }

                KTSyncData.bIsRunning = true;
            } else if (id == R.id.btn_capture_bluetooth_device_find) {

                mIsScanDeviceListActivityRun = true;
                Intent intent = new Intent(CaptureActivity.this, DeviceListActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            } else if (id == R.id.btn_capture_type_number_add) {

                onAddButtonClick();
            } else if (id == R.id.btn_capture_barcode_reset) {

                onResetButtonClick();
            } else if (id == R.id.btn_capture_barcode_confirm) {

                switch (mScanType) {
                    case BarcodeType.CONFIRM_MY_DELIVERY_ORDER:
                    case BarcodeType.CHANGE_DELIVERY_DRIVER:

                        onUpdateButtonClick();
                        break;
                    case BarcodeType.PICKUP_CNR:
                    case BarcodeType.PICKUP_SCAN_ALL:
                    case BarcodeType.PICKUP_ADD_SCAN:
                    case BarcodeType.PICKUP_TAKE_BACK:
                    case BarcodeType.OUTLET_PICKUP_SCAN:

                        onNextButtonClick();
                        break;
                    case BarcodeType.DELIVERY_DONE:

                        onConfirmButtonClick();
                        break;
                    case BarcodeType.SELF_COLLECTION:

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
    private Runnable mUpdateTimeTask = () -> {

        if (KTSyncData.AutoConnect && KTSyncData.bIsRunning)
            KTSyncData.mChatService.connect(connectedDevice);
    };
    // Bluetooth
    @SuppressLint("HandlerLeak")
    private final Handler bluetoothHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            Log.i(bluetoothTAG, "handleMessage " + msg.what);

            switch (msg.what) {
                case BluetoothChatService.MESSAGE_STATE_CHANGE:
                    Log.i(bluetoothTAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:

                            text_capture_bluetooth_connect_state.setText(getResources().getString(R.string.text_connected));
                            text_capture_bluetooth_device_name.setVisibility(View.VISIBLE);
                            text_capture_bluetooth_device_name.setText("(" + connectedDeviceName + ")");
                            btn_capture_bluetooth_device_find.setVisibility(View.GONE);

                            removeCallbacks(mUpdateTimeTask);
                            KTSyncData.mKScan.DeviceConnected(true);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:

                            text_capture_bluetooth_connect_state.setText(getResources().getString(R.string.text_connecting));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.GONE);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:

                            text_capture_bluetooth_connect_state.setText(getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            break;
                        case BluetoothChatService.STATE_LOST:

                            text_capture_bluetooth_connect_state.setText(getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            KTSyncData.bIsConnected = false;
                            postDelayed(mUpdateTimeTask, 2000);
                            break;
                        case BluetoothChatService.STATE_FAILED:

                            text_capture_bluetooth_connect_state.setText(getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            postDelayed(mUpdateTimeTask, 5000);
                            break;
                    }
                    break;

                case BluetoothChatService.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;

                    for (int i = 0; i < msg.arg1; i++)
                        KTSyncData.mKScan.HandleInputData(readBuf[i]);
                    break;

                case BluetoothChatService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(BluetoothChatService.DEVICE_NAME);
                    Toast.makeText(CaptureActivity.this, getResources().getString(R.string.text_connected_to) + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;

                case BluetoothChatService.MESSAGE_TOAST:
                    Toast.makeText(CaptureActivity.this, msg.getData().getString(BluetoothChatService.TOAST), Toast.LENGTH_SHORT).show();
                    break;

                case KScan.MESSAGE_DISPLAY:

                    byte[] displayBuf = (byte[]) msg.obj;
                    String displayMessage = new String(displayBuf, 0, msg.arg1);
                    onBluetoothBarcodeAdd(displayMessage);
                    KTSyncData.bIsSyncFinished = true;
                    break;

                case KScan.MESSAGE_SEND:

                    byte[] sendBuf = (byte[]) msg.obj;
                    KTSyncData.mChatService.write(sendBuf);
                    break;
            }
        }
    };

    /*
     * Qxpress송장번호 규칙(범용)
     * 운송장번호 규칙이 맞는지 체크
     * 10문자 안넘으면 false, 맨앞두글자가 KR,SG,QX,JP,CN이 아닐경우 false, 5,6번째가 숫자가 아닐경우 false, 영문숫자조합
      SELF_COLLECTION */
    public static boolean isInvoiceCodeRule(String invoiceNo) {

        if (invoiceNo.length() < 10)
            return false;

        boolean bln = Pattern.matches("^[a-zA-Z0-9]*$", invoiceNo);
        if (!bln) {
            return false;
        }

        if (10 <= invoiceNo.length()) {    // self collection c2c 아닐 때

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

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_capture_camera = findViewById(R.id.layout_capture_camera);
        text_capture_camera = findViewById(R.id.text_capture_camera);
        layout_capture_scanner = findViewById(R.id.layout_capture_scanner);
        text_capture_scanner = findViewById(R.id.text_capture_scanner);
        layout_capture_bluetooth = findViewById(R.id.layout_capture_bluetooth);
        text_capture_bluetooth = findViewById(R.id.text_capture_bluetooth);

        barcode_scanner = findViewById(R.id.barcode_scanner);
        toggle_btn_capture_camera_flash = findViewById(R.id.toggle_btn_capture_camera_flash);

        layout_capture_scanner_mode = findViewById(R.id.layout_capture_scanner_mode);
        layout_capture_bluetooth_mode = findViewById(R.id.layout_capture_bluetooth_mode);
        text_capture_bluetooth_connect_state = findViewById(R.id.text_capture_bluetooth_connect_state);
        text_capture_bluetooth_device_name = findViewById(R.id.text_capture_bluetooth_device_name);
        btn_capture_bluetooth_device_find = findViewById(R.id.btn_capture_bluetooth_device_find);

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
        layout_capture_bluetooth.setOnClickListener(clickListener);
        btn_capture_bluetooth_device_find.setOnClickListener(clickListener);
        edit_capture_type_number.setOnClickListener(clickListener);
        btn_capture_type_number_add.setOnClickListener(clickListener);
        btn_capture_barcode_reset.setOnClickListener(clickListener);
        btn_capture_barcode_confirm.setOnClickListener(clickListener);


        edit_capture_type_number.setOnTouchListener(this);
        edit_capture_type_number.addTextChangedListener(this);
        edit_capture_type_number.setOnKeyListener(this);
        edit_capture_type_number.setLongClickable(false);
        edit_capture_type_number.setTextIsSelectable(false);

        editTextDelButtonDrawable = getResources().getDrawable(R.drawable.btn_delete);
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
            case BarcodeType.PICKUP_SCAN_ALL:
            case BarcodeType.PICKUP_ADD_SCAN: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
            }
            break;
            case BarcodeType.PICKUP_TAKE_BACK: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
                mQty = getIntent().getStringExtra("qty");
            }
            break;
            case BarcodeType.OUTLET_PICKUP_SCAN: {

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


        // 블루투스 초기화
        initBluetoothDevice();
        // 초기화
        initManualScanViews(mScanType);

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

    private void initBluetoothDevice() {
        // Get local Bluetooth adapter        // Bluetooth 지원 여부 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported          // Bluetooth 지원하지 않음
        if (mBluetoothAdapter == null && !BuildConfig.DEBUG) {

            Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        KTSyncData.mKScan = new KScan(this, bluetoothHandler);

        for (int i = 0; i < 10; i++) {
            KTSyncData.SerialNumber[i] = '0';
            KTSyncData.FWVersion[i] = '0';
        }


        byte[] temp;

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        KTSyncData.AutoConnect = app_preferences.getBoolean("Auto Connect", false);
        KTSyncData.AttachTimestamp = app_preferences.getBoolean("AttachTimeStamp", false);
        KTSyncData.AttachType = app_preferences.getBoolean("AttachBarcodeType", false);
        KTSyncData.AttachSerialNumber = app_preferences.getBoolean("AttachSerialNumber", false);
        temp = app_preferences.getString("Data Delimiter", "4").getBytes();
        KTSyncData.DataDelimiter = temp[0] - '0';
        temp = app_preferences.getString("Record Delimiter", "1").getBytes();
        KTSyncData.RecordDelimiter = temp[0] - '0';
        KTSyncData.AttachLocation = app_preferences.getBoolean("AttachLocationData", false);
        KTSyncData.SyncNonCompliant = app_preferences.getBoolean("SyncNonCompliant", false);
        KTSyncData.AttachQuantity = app_preferences.getBoolean("AttachQuantity", false);
    }

    private void initManualScanViews(String scanType) {

        layout_capture_scan_count.setVisibility(View.VISIBLE);

        switch (scanType) {
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_update));      //onUpdateButtonClick
                break;
            case BarcodeType.CHANGE_DELIVERY_DRIVER:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_done));         //onUpdateButtonClick
                break;
            case BarcodeType.DELIVERY_DONE: {

                layout_capture_scan_count.setVisibility(View.GONE);
                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_confirm));         //onConfirmButtonClick
            }
            break;
            case BarcodeType.PICKUP_CNR:
            case BarcodeType.PICKUP_SCAN_ALL:
            case BarcodeType.PICKUP_ADD_SCAN:
            case BarcodeType.PICKUP_TAKE_BACK:
            case BarcodeType.OUTLET_PICKUP_SCAN:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_next));            //onNextButtonClick
                break;
            case BarcodeType.SELF_COLLECTION:

                btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (KTSyncData.mChatService == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "   onResume");

        if (Preferences.INSTANCE.getUserId().equals("")) {

            Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show();

            try {
                Intent intent;
                if ("SG".equals(Preferences.INSTANCE.getUserNation())) {
                    intent = new Intent(CaptureActivity.this, Class.forName("com.giosis.util.qdrive.singapore.LoginActivity"));
                } else {
                    intent = new Intent(CaptureActivity.this, Class.forName("com.giosis.util.qdrive.international.LoginActivity"));
                }
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

        // Bluetooth
        if (KTSyncData.bIsRunning)
            return;

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (KTSyncData.mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (KTSyncData.mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                KTSyncData.mChatService.start();
            }
        }

        if (KTSyncData.bIsConnected && KTSyncData.LockUnlock) {
            // Toast.makeText(this, "KTDemo Main Screen",
            // Toast.LENGTH_LONG).show();
            KTSyncData.mKScan.LockUnlockScanButton(true);
        }
        KTSyncData.mKScan.mHandler = bluetoothHandler;


        if (isPermissionTrue) {
            // Camera
            cameraManager.onResume();

            // Location
            if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

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
        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
                || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)
                || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            try {

                scanBarcodeArrayList.clear();
                adapter.notifyDataSetChanged();

                if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
                        || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                        || mScanType.equals(BarcodeType.PICKUP_CNR)
                        || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)
                        || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                        || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

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
                } else if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

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
        } else if (mScanType.equals(BarcodeType.SELF_COLLECTION)) {

            text_top_title.setText(getResources().getString(R.string.text_title_scan_barcode));
            btn_capture_barcode_confirm.setText(getResources().getString(R.string.button_next));

            scanBarcodeArrayList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        KTSyncData.mChatService = new BluetoothChatService(this, bluetoothHandler);
    }

    // EditText
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

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

            DataUtil.logEvent("capture", TAG, "Scanner");
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

                if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER) ||
                        mScanType.equals(BarcodeType.PICKUP_CNR)
                        || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                        || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK) || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {


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

    // Bluetooth
    private void onBluetoothBarcodeAdd(String strBarcodeNo) {

        DataUtil.logEvent("capture", TAG, "Bluetooth");
        // bluetooth "\n"이 포함되어서 다른번호로 인식 > trim 으로 공백 없애기
        strBarcodeNo = strBarcodeNo.trim();

        if (!strBarcodeNo.isEmpty()) {

            boolean isDuplicate = false;

            for (int i = 0; i < scannedBarcode.size(); i++) {

                if (scannedBarcode.get(i).equalsIgnoreCase(strBarcodeNo)) {
                    isDuplicate = true;
                }
            }

            if (!isDuplicate) {

                scannedBarcode.add(strBarcodeNo);
            }
            Log.i(TAG, "  onBluetoothBarcodeAdd > " + strBarcodeNo + " / " + isDuplicate);

            checkValidation(strBarcodeNo, isDuplicate, "onBluetoothBarcodeAdd");
        }
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

            DataUtil.logEvent("capture", TAG, "EditText");
            checkValidation(inputBarcodeNumber, isDuplicate, "onAddButtonClick");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult " + requestCode + " / " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: {
                //  Bluetooth Device 연결
                if (resultCode == Activity.RESULT_OK) {

                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    connectedDevice = mBluetoothAdapter.getRemoteDevice(address);
                    KTSyncData.mChatService.connect(connectedDevice);
                }
            }
            break;

            case REQUEST_ENABLE_BT: {

                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth 승인 요청 'YES'
                    setupChat();
                } else {
                    // Bluetooth 승인 요청 'NO'
                    Toast.makeText(CaptureActivity.this, R.string.msg_bluetooth_enabled, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;

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
                    Log.e("eylee", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

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

        if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

            data.setBarcode(changeDriverResult.getTrackingNo() + "  |  " + changeDriverResult.getStatus() + "  |  " + changeDriverResult.getCurrentDriver());
        }


        switch (mScanType) {
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER:
            case BarcodeType.CHANGE_DELIVERY_DRIVER:
            case BarcodeType.PICKUP_CNR:
            case BarcodeType.PICKUP_SCAN_ALL:
            case BarcodeType.PICKUP_ADD_SCAN:
            case BarcodeType.PICKUP_TAKE_BACK:
                // 스캔 시 최근 스캔한 바코드가 제일 위로 셋팅됨.
                data.setState("SUCCESS");

                if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                    barcodeList.add(changeDriverResult.getTrackingNo() + "  |  " + changeDriverResult.getStatus() + "  |  " + changeDriverResult.getCurrentDriver());
                    changeDriverObjectArrayList.add(changeDriverResult);
                } else {

                    barcodeList.add(barcodeNo);
                }

                scanBarcodeArrayList.add(0, data);
                adapter.notifyDataSetChanged();
                recycler_scanned_barcode.smoothScrollToPosition(0);

                break;
            case BarcodeType.OUTLET_PICKUP_SCAN:

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
                //스캔 시 최근 스캔한 바코드가 아래로 추가됨.
                // maybe.. DELIVERY DONE, SELF COLLECTION
                scanBarcodeArrayList.add(data);
                adapter.notifyDataSetChanged();
                break;
        }

        if (!mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) && !mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                && !mScanType.equals(BarcodeType.PICKUP_CNR)) {

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
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER: {

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
            case BarcodeType.CHANGE_DELIVERY_DRIVER: {

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
            case BarcodeType.PICKUP_CNR: {  //2016-09-21 add type validation

                // 2016-09-01 eylee 여기서 유효성 검사해서 네트워크 타기
                // 유효성 검사에 통과하면 여기서 소리 추가하면서 addBarcode
                // sqlite 에 cnr barcode scan no 가 있는지 확인하고 insert 하는 sqlite validation 부분 필요
                // validation 성공했을 때, editext 에 넣고 실패하면, alert 띄우고 editText 에 들어가지 않음
                // 성공하면 sqlite 에 insert

                // Edit.  2020.03  배포 (기존 CNR 중복 허용됨 > 중복 허용X 수정)    by krm0219
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
            case BarcodeType.PICKUP_SCAN_ALL: {

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
            case BarcodeType.PICKUP_ADD_SCAN: {

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
            case BarcodeType.PICKUP_TAKE_BACK: {

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
            case BarcodeType.OUTLET_PICKUP_SCAN: {

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
            case BarcodeType.SELF_COLLECTION: {     // 2016-09-20 eylee

                if (!isInvoiceCodeRule(strBarcodeNo)) {

                    beepManagerError.playBeepSoundAndVibrate();
                    Toast toast = Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    return;
                }

                beepManager.playBeepSoundAndVibrate();

                //2016-09-12 eylee nq 끼리만 self collector 가능하게 수정하기
                if (!scanBarcodeArrayList.isEmpty()) {

                    boolean tempIsNonQ10QFSOrder = isNonQ10QFSOrder;
                    boolean tempValidation = isNonQ10QFSOrderForSelfCollection(strBarcodeNo);

                    if (tempIsNonQ10QFSOrder != tempValidation) {
                        // alert 띄워줘야 함 type 이 다르다는 - 기존 Self - Collection 과 새로 NQ 가
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
            contentVal.put("type", BarcodeType.TYPE_PICKUP);
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


    // 하단 버튼 클릭 이벤트
    // NOTIFICATION.  Confirm my delivery order / Change Delivery Driver
    public void onUpdateButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

            Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(CaptureActivity.this)) {
            AlertShow(getResources().getString(R.string.msg_network_connect_error));
            return;
        }

        if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
            AlertShow(getResources().getString(R.string.msg_disk_size_error));
            return;
        }


        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER)) {

            DataUtil.logEvent("button_click", TAG, "SetShippingStatDpc3out");

            new ConfirmMyOrderHelper.Builder(this, opID, officeCode, deviceID, scanBarcodeArrayList)
                    .setOnDriverAssignEventListener(stdResult -> {

                        String msg;

                        if (stdResult != null) {
                            if (stdResult.getResultCode() == 0)
                                onResetButtonClick();

                            msg = stdResult.getResultMsg();
                        } else {

                            msg = getResources().getString(R.string.text_fail_update);
                        }


                        if (!CaptureActivity.this.isFinishing()) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                            builder.setTitle(getResources().getString(R.string.text_driver_assign_result));
                            builder.setMessage(msg);
                            builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, id) -> dialog.cancel());
                            builder.show();
                        }
                    }).build().execute();
        } else if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

            DataUtil.logEvent("button_click", TAG, "SetChangeDeliveryDriver");

            if (gpsEnable && gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " onUpdateButtonClick GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            new ChangeDriverHelper.Builder(this, opID, officeCode, deviceID, changeDriverObjectArrayList, latitude, longitude)
                    .setOnChangeDelDriverEventListener(stdResult -> {

                        String msg;

                        if (stdResult != null) {

                            if (stdResult.getResultCode() == 0)
                                onResetButtonClick();

                            msg = stdResult.getResultMsg();
                        } else {

                            msg = getResources().getString(R.string.text_fail_update);
                        }

                        if (!CaptureActivity.this.isFinishing()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                            builder.setTitle(getResources().getString(R.string.text_driver_assign_result));
                            builder.setMessage(msg);
                            builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, id) -> dialog.cancel());
                            builder.show();
                        }
                    }).build().execute();
        }
    }

    private boolean insertDriverAssignInfo(DriverAssignResult.QSignDeliveryList assignInfo) {

        String opId = Preferences.INSTANCE.getUserId();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        // eylee 2015.08.26 add non q10 - contr_no 로 sqlite 체크 후 있다면 삭제하는 로직 add start
        String contr_no = assignInfo.getContrNo();
        int cnt = DataUtil.getContrNoCount(contr_no);
        Log.e("TAG", "insertDriverAssignInfo  check count : " + cnt);
        if (0 < cnt) {
            DataUtil.deleteContrNo(contr_no);
        }

        // eylee 2015.08.26 add end
        //성공 시 통합리스트 테이블 저장
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
        contentVal.put("type", BarcodeType.TYPE_DELIVERY);
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

        // 2020.06 위, 경도 저장
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

                    // 수취인성명이 틀린경우
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

        // 받는사람이 틀리다면 에러 메세지
        if (diffReceiverName) {

            Toast toast = Toast.makeText(CaptureActivity.this, R.string.msg_different_recipient_address, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }


        if (0 < deliveryBarcodeList.size()) {

            Intent intent = new Intent(this, DeliveryDoneActivity.class);
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

        if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

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
            case BarcodeType.PICKUP_CNR: {

                Intent intent = new Intent(this, CnRPickupDoneActivity.class);
                intent.putExtra("senderName", pickupCNRRequester);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_CNR);
                break;
            }
            case BarcodeType.PICKUP_SCAN_ALL: {

                Intent intent = new Intent(this, PickupDoneActivity.class);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("scannedQty", scannedQty);
                startActivity(intent);
                finish();
                break;
            }
            case BarcodeType.PICKUP_ADD_SCAN: {

                Intent intent = new Intent(this, PickupAddScanActivity.class);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_ADD_SCAN);
                break;
            }
            case BarcodeType.PICKUP_TAKE_BACK: {

                Intent intent = new Intent(this, PickupTakeBackActivity.class);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList.toString());
                intent.putExtra("totalQty", mQty);
                intent.putExtra("takeBackQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_TAKE_BACK);
                break;
            }
            case BarcodeType.OUTLET_PICKUP_SCAN: {

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
                Log.e(TAG, "Outlet Pickup Scanned List : " + scanned_list);

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

            if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

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

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

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

        if (mIsScanDeviceListActivityRun || KTSyncData.bIsRunning) {
            mIsScanDeviceListActivityRun = false;
        }
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

        // Stop the Bluetooth chat services
        if (KTSyncData.mChatService != null)
            KTSyncData.mChatService.stop();
        KTSyncData.mChatService = null;
        KTSyncData.bIsRunning = false;


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

    //2016-09-12 eylee  self-collection nq 인지 아닌지 판단하는
    public boolean isNonQ10QFSOrderForSelfCollection(String barcodeNo) {
        boolean isNQ = false;

        int len = barcodeNo.length();
        String ScanNoLast = barcodeNo.substring(len - 2).toUpperCase();
        if (ScanNoLast.equals("NQ")) {
            isNQ = true;
        }
        // return 해서 isNonQ10QFSOrder 여기에 setting 하기
        return isNQ;
    }

    /*
     * update delivery set stat = @stat , chg_id = localStorage.getItem('opId')
     * , chg_dt = datetime('now') where invoice_no = @invoice_no COLLATE NOCASE
     * and punchOut_stat <> 'S' and reg_id = localStorage.getItem('opId')
     */
    private void updateInvoiceNO(String scanType, String invoiceNo) {

        int updateCount = 0;

        if (scanType.equals(BarcodeType.PICKUP_SCAN_ALL) || scanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || scanType.equals(BarcodeType.PICKUP_TAKE_BACK) || scanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            updateCount = 1;
        } else if (mScanType.equals(BarcodeType.DELIVERY_DONE)) {
            // 복수건 배달완료 시점에서는 아무것도 안함 사인전 jmkang 2013-05-08

            ContentValues contentVal = new ContentValues();
            contentVal.put("reg_id", opID); // 해당 배송번호를 가지고 자신의아이디만 없데이트
            updateCount = DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?", new String[]{invoiceNo, opID});
        } else if (mScanType.equals(BarcodeType.SELF_COLLECTION)) {

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

        if (!mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            BarcodeData data = new BarcodeData();
            data.setBarcode(inputBarcode);
            data.setState(result);

            scanBarcodeArrayList.set(scanBarcodeArrayList.size() - 1, data);
            adapter.notifyDataSetChanged();
        }

        // 교체 후 Adapter.notifyDataSetChanged() 메서드로 listview  변경 add comment by eylee 2016-09-08
        if (updateCount < 1) { // 실패일때만 보여준다.

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
     * 송장번호 규칙에 맞는지 체크한후 프리뷰영역에 이미지를 보여준다.
     * modified : 2016-09-09 eylee self-collection 복수 건 처리 add
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

        // SELF_COLLECTION            //복수건 가져다가 self-collection by 2016-09-09
        // 넘기는 데이터 재정의 스캔성공된 것들만 보낸다.
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