package com.giosis.util.qdrive.barcodescanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.giosis.util.qdrive.barcodescanner.bluetooth.BluetoothChatService;
import com.giosis.util.qdrive.barcodescanner.bluetooth.DeviceListActivity;
import com.giosis.util.qdrive.barcodescanner.bluetooth.KScan;
import com.giosis.util.qdrive.barcodescanner.bluetooth.KTSyncData;
import com.giosis.util.qdrive.barcodescanner.camera.CameraManager;
import com.giosis.util.qdrive.barcodescanner.history.HistoryManager;
import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.international.SigningActivity;
import com.giosis.util.qdrive.international.UploadData;
import com.giosis.util.qdrive.list.BarcodeData;
import com.giosis.util.qdrive.list.OutletPickupDoneResult;
import com.giosis.util.qdrive.list.delivery.DeliveryDoneActivity;
import com.giosis.util.qdrive.list.pickup.CnRPickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.OutletPickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.PickupAddScanActivity;
import com.giosis.util.qdrive.list.pickup.PickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.PickupTakeBackActivity;
import com.giosis.util.qdrive.main.ChangeDriverValidationCheckHelper;
import com.giosis.util.qdrive.main.Dpc3OutValidationCheckHelper;
import com.giosis.util.qdrive.main.ManualChangeDelDriverHelper;
import com.giosis.util.qdrive.main.ManualChangeDelDriverHelper.OnChangeDelDriverEventListener;
import com.giosis.util.qdrive.main.ManualDriverAssignHelper;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * The barcode reader activity itself. This is loosely based on the
 * CameraPreview example included in the Android SDK.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */

public final class CaptureActivity extends CommonActivity implements SurfaceHolder.Callback, OnTouchListener,
        TextWatcher, SensorEventListener, OnKeyListener {
    static String TAG = "CaptureActivity";


    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_DELIVERY_DONE = 10;
    private static final int REQUEST_PICKUP_CNR = 11;
    private static final int REQUEST_PICKUP_ADD_SCAN = 12;
    private static final int REQUEST_PICKUP_TAKE_BACK = 13;

    private static final int REQUEST_SELF_COLLECTION = 20;
    private static final int REQUEST_POD_SCAN = 21;


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DISPLAY = 6;
    public static final int MESSAGE_SEND = 7;
    public static final int MESSAGE_SETTING = 255;


    // View
    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_capture_camera;
    TextView text_capture_camera;
    LinearLayout layout_capture_scanner;
    TextView text_capture_scanner;
    LinearLayout layout_capture_bluetooth;
    TextView text_capture_bluetooth;

    SurfaceView surface_capture_preview;
    ViewfinderView viewfinder_capture_preview;
    ToggleButton toggle_btn_capture_camera_flash;

    LinearLayout layout_capture_scanner_mode;
    LinearLayout layout_capture_bluetooth_mode;
    TextView text_capture_bluetooth_connect_state;
    TextView text_capture_bluetooth_device_name;
    Button btn_capture_bluetooth_device_find;

    EditText edit_capture_type_number;
    ImageView img_capture_type_number_delete;
    Button btn_capture_type_number_add;

    RelativeLayout layout_capture_scan_count;
    TextView text_capture_scan_count;
    ListView list_capture_scan_barcode;

    Button btn_capture_barcode_reset;
    Button btn_capture_barcode_confirm;
    Button btn_pod_upload;

    //
    Context context;
    String opID;
    String officeCode;
    String deviceID;

    String mScanType;
    String mScanTitle;

    String pickupNo;
    String pickupApplicantName;
    String pickupCNRRequester;
    String outletDriverYN;          //krm0219  outlet driver
    String mQty;
    String mRoute;
    OutletPickupDoneResult resultData;


    int mScanCount = 0;
    private ArrayList<BarcodeData> scanBarcodeArrayList;        // scanned Barcode List
    private scanBarcodeNoListAdapter scanBarcodeNoListAdapter;
    // resume 시 recreate 할 data list
    private static ArrayList<String> barcodeList = new ArrayList<>();
    private ArrayList<ChangeDriverResult> changeDriverArrayList = new ArrayList<>();
    private ChangeDriverResult changeDriverResult;
    private HistoryManager historyManager;


    public boolean isNonQ10QFSOrder = false;


    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    InputMethodManager inputMethodManager;
    private BeepManager beepManager;

    // CAMERA
    SurfaceHolder surfaceHolder;
    private boolean hasSurface;
    // 센서 관련 객체
    SensorManager m_sensor_manager;
    Sensor m_light_sensor;
    private CaptureActivityHandler handler;

    // Bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothDevice connectDevice = null;
    private String connectDeviceName = null;
    boolean mIsScanDeviceListActivityRun = false;


    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA};


    ViewfinderView getViewfinderView() {
        return viewfinder_capture_preview;
    }

    public Handler getHandler() {
        return handler;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_capture);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_capture_camera = findViewById(R.id.layout_capture_camera);
        text_capture_camera = findViewById(R.id.text_capture_camera);
        layout_capture_scanner = findViewById(R.id.layout_capture_scanner);
        text_capture_scanner = findViewById(R.id.text_capture_scanner);
        layout_capture_bluetooth = findViewById(R.id.layout_capture_bluetooth);
        text_capture_bluetooth = findViewById(R.id.text_capture_bluetooth);

        surface_capture_preview = findViewById(R.id.surface_capture_preview);
        viewfinder_capture_preview = findViewById(R.id.viewfinder_capture_preview);
        toggle_btn_capture_camera_flash = findViewById(R.id.toggle_btn_capture_camera_flash);

        layout_capture_scanner_mode = findViewById(R.id.layout_capture_scanner_mode);
        layout_capture_bluetooth_mode = findViewById(R.id.layout_capture_bluetooth_mode);
        text_capture_bluetooth_connect_state = findViewById(R.id.text_capture_bluetooth_connect_state);
        text_capture_bluetooth_device_name = findViewById(R.id.text_capture_bluetooth_device_name);
        btn_capture_bluetooth_device_find = findViewById(R.id.btn_capture_bluetooth_device_find);

        edit_capture_type_number = findViewById(R.id.edit_capture_type_number);
        img_capture_type_number_delete = findViewById(R.id.img_capture_type_number_delete);
        btn_capture_type_number_add = findViewById(R.id.btn_capture_type_number_add);

        layout_capture_scan_count = findViewById(R.id.layout_capture_scan_count);
        text_capture_scan_count = findViewById(R.id.text_capture_scan_count);
        list_capture_scan_barcode = findViewById(R.id.list_capture_scan_barcode);

        btn_capture_barcode_reset = findViewById(R.id.btn_capture_barcode_reset);
        btn_capture_barcode_confirm = findViewById(R.id.btn_capture_barcode_confirm);
        btn_pod_upload = findViewById(R.id.barcode_pod_upload);


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


        //------------------
        context = getApplicationContext();
        beepManager = new BeepManager(this);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        try {
            outletDriverYN = MyApplication.preferences.getOutletDriver();
        } catch (Exception e) {
            outletDriverYN = "N";
        }


        mScanTitle = getIntent().getStringExtra("title");
        mScanType = getIntent().getStringExtra("type");
        text_top_title.setText(mScanTitle);


        scanBarcodeArrayList = new ArrayList<>();

        // eylee 2015.10.06
        switch (mScanType) {
            case BarcodeType.PICKUP_START_SCAN:
            case BarcodeType.PICKUP_ADD_SCAN: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
            }
            break;
            case BarcodeType.PICKUP_TAKE_BACK: {

                pickupNo = getIntent().getStringExtra("pickup_no");
                pickupApplicantName = getIntent().getStringExtra("applicant");
                mQty = getIntent().getStringExtra("scanned_qty");
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


                ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeData data = new BarcodeData();
                    data.setState("FAIL");
                    data.setBarcode(listItem.get(i).getTrackingNo());

                    scanBarcodeArrayList.add(i, data);
                }
            }
            break;
            case BarcodeType.SCAN_DELIVERY_SHEET:
            case BarcodeType.SELF_COLLECTION: {

                text_top_title.setText(context.getResources().getString(R.string.text_title_scan_barcode));
                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next_step2));            // onCaptureConfirmButtonClick

                String podCount = getScanDeliveryCount();
                text_capture_scan_count.setText(podCount);

                if (0 < Integer.parseInt(podCount)) {

                    btn_pod_upload.setVisibility(View.VISIBLE);
                } else {

                    btn_pod_upload.setVisibility(View.GONE);
                }
            }
            break;
        }


        scanBarcodeNoListAdapter = new scanBarcodeNoListAdapter(this, scanBarcodeArrayList, mScanType);
        list_capture_scan_barcode.setAdapter(scanBarcodeNoListAdapter);

        if (0 < scanBarcodeArrayList.size()) {

            list_capture_scan_barcode.setSelection(scanBarcodeArrayList.size() - 1);
        }


        historyManager = new HistoryManager(this);
        historyManager.clearHistory();

        handler = null;
        m_sensor_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        m_light_sensor = m_sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT);

        CameraManager.init(getApplication());
        surfaceHolder = surface_capture_preview.getHolder();
        surfaceHolder.addCallback(this);
        hasSurface = false;

        toggle_btn_capture_camera_flash.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (handler != null) {
                    handler.quitSynchronously();
                }

                if (isChecked) {

                    CameraManager.get().onFlash();
                } else {

                    CameraManager.get().offFlash();
                }

                if (handler != null) {
                    handler = new CaptureActivityHandler(CaptureActivity.this);
                }
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

        // Bluetooth 지원 여부 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth 지원하지 않음
        if (mBluetoothAdapter == null) {

            Toast.makeText(this, context.getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        KTSyncData.mKScan = new KScan(bluetoothHandler);

        for (int i = 0; i < 10; i++) {
            KTSyncData.SerialNumber[i] = '0';
            KTSyncData.FWVersion[i] = '0';
        }


        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        KTSyncData.SyncNonCompliant = app_preferences.getBoolean("SyncNonCompliant", false);
    }

    private void initManualScanViews(String scanType) {

        layout_capture_scan_count.setVisibility(View.VISIBLE);
        btn_pod_upload.setVisibility(View.GONE);

        switch (scanType) {
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER: {

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_update));      //onUpdateButtonClick
            }
            break;
            case BarcodeType.CHANGE_DELIVERY_DRIVER: {

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_done));         //onUpdateButtonClick
            }
            break;
            case BarcodeType.DELIVERY_DONE: {

                layout_capture_scan_count.setVisibility(View.GONE);
                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         //onConfirmButtonClick
            }
            break;
            case BarcodeType.PICKUP_CNR:            // pickup C&R by 2016-08-30 eylee
            case BarcodeType.PICKUP_START_SCAN:     // 2016-09-26 pickup scan all
            case BarcodeType.PICKUP_ADD_SCAN:       // 2017-03-15 pickup  add scan list
            case BarcodeType.PICKUP_TAKE_BACK:      // 2019.02 krm0219
            case BarcodeType.OUTLET_PICKUP_SCAN:    // krm0219
            {

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            //onNextButtonClick
            }
            break;

            case BarcodeType.SELF_COLLECTION: {

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick
            }
            break;
            case BarcodeType.SCAN_DELIVERY_SHEET: {   // Scan capture Delivered

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick
                btn_pod_upload.setVisibility(View.VISIBLE);
            }
            break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothAdapter.isEnabled()) {
            if (KTSyncData.mChatService == null)
                setupChat();
        }
    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, TAG + "   onResume");

        try {

            edit_capture_type_number.requestFocus();
        } catch (Exception e) {

            Log.e("Exception", TAG + "  requestFocus Exception : " + e.toString());
        }


        beepManager.updatePrefs();

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


        // Camera
        m_sensor_manager.registerListener(this, m_light_sensor, SensorManager.SENSOR_DELAY_UI);

        if (isPermissionTrue) {
            // Camera
            if (hasSurface) {

                initCamera(surfaceHolder);
            } else {
                // Install the callback and wait for surfaceCreated() to init the camera.
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }

            // Location
            if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                gpsTrackerManager = new GPSTrackerManager(context);
                gpsEnable = gpsTrackerManager.enableGPSSetting();

                if (gpsEnable && gpsTrackerManager != null) {

                    gpsTrackerManager.GPSTrackerStart();
                    latitude = gpsTrackerManager.getLatitude();
                    longitude = gpsTrackerManager.getLongitude();
                    Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
                } else {

                    DataUtil.enableLocationSettings(CaptureActivity.this, context);
                }
            }
        }


        // Scanned List 다시 그리기..

        switch (mScanType) {
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER:
            case BarcodeType.CHANGE_DELIVERY_DRIVER:
            case BarcodeType.PICKUP_CNR:
            case BarcodeType.PICKUP_START_SCAN:
            case BarcodeType.PICKUP_ADD_SCAN:
            case BarcodeType.PICKUP_TAKE_BACK: {

                scanBarcodeArrayList.clear();
                scanBarcodeNoListAdapter.notifyDataSetChanged();

                if (barcodeList != null && 0 < barcodeList.size()) {
                    for (int i = 0; i < barcodeList.size(); i++) {

                        BarcodeData data = new BarcodeData();
                        data.setState("SUCCESS");
                        data.setBarcode(barcodeList.get(i));
                        scanBarcodeArrayList.add(0, data);

                        historyManager.addHistoryItem(new Result(barcodeList.get(i), null, null, null));
                    }

                    mScanCount = scanBarcodeArrayList.size();
                    text_capture_scan_count.setText(String.valueOf(mScanCount));

                    scanBarcodeNoListAdapter.notifyDataSetChanged();
                    list_capture_scan_barcode.setSelection(0);
                    list_capture_scan_barcode.smoothScrollToPosition(0);
                }
            }
            break;
            case BarcodeType.OUTLET_PICKUP_SCAN: {

                scanBarcodeArrayList.clear();
                scanBarcodeNoListAdapter.notifyDataSetChanged();

                mScanCount = 0;

                if (resultData != null) {

                    ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();

                    for (int i = 0; i < listItem.size(); i++) {

                        String tracking_no = listItem.get(i).getTrackingNo();
                        boolean isScanned = listItem.get(i).isScanned();

                        BarcodeData data = new BarcodeData();
                        data.setBarcode(tracking_no);

                        if (isScanned) {

                            data.setState("SUCCESS");
                            mScanCount++;
                            historyManager.addHistoryItem(new Result(tracking_no, null, null, null));
                        } else {

                            data.setState("FAIL");
                        }

                        scanBarcodeArrayList.add(i, data);
                    }
                }

                text_capture_scan_count.setText(String.valueOf(mScanCount));

                scanBarcodeNoListAdapter.notifyDataSetChanged();
                list_capture_scan_barcode.setSelection(0);
                list_capture_scan_barcode.smoothScrollToPosition(0);
            }
            break;
            case BarcodeType.SCAN_DELIVERY_SHEET:
            case BarcodeType.SELF_COLLECTION: {
                // Scan Sheet Delivery 경우 데이터초기화 CameraActivity 에서 돌아오는경우 아답터초기화

                scanBarcodeArrayList.clear();
                scanBarcodeNoListAdapter.notifyDataSetChanged();

                String podCount = getScanDeliveryCount();
                text_capture_scan_count.setText(podCount);

                if (0 < Integer.parseInt(podCount)) {
                    btn_pod_upload.setVisibility(View.VISIBLE);
                } else {
                    btn_pod_upload.setVisibility(View.GONE);
                }

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next_step2));
            }
            break;
        }
    }


    // NOTIFICATION.  Click Event
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.layout_top_back: {

                    onResetButtonClick();
                    finish();
                }
                break;

                case R.id.layout_capture_camera: {

                    layout_capture_camera.setBackgroundResource(R.drawable.bg_rect_ffffff_shadow_ff0000);
                    layout_capture_scanner.setBackgroundResource(R.drawable.bg_rect_ffffff);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_rect_ffffff);
                    text_capture_camera.setTextColor(getResources().getColor(R.color.color_ff0000));
                    text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.BOLD);
                    text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);
                    text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.NORMAL);

                    viewfinder_capture_preview.setVisibility(View.VISIBLE);
                    layout_capture_scanner_mode.setVisibility(View.GONE);
                    layout_capture_bluetooth_mode.setVisibility(View.GONE);

                    // bluetooth
                    if (KTSyncData.mChatService != null)
                        KTSyncData.mChatService.stop();
                    KTSyncData.bIsRunning = false;

                    onResume();
                }
                break;

                case R.id.layout_capture_scanner: {

                    layout_capture_camera.setBackgroundResource(R.drawable.bg_rect_ffffff);
                    layout_capture_scanner.setBackgroundResource(R.drawable.bg_rect_ffffff_shadow_ff0000);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_rect_ffffff);
                    text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                    text_capture_scanner.setTextColor(getResources().getColor(R.color.color_ff0000));
                    text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.BOLD);
                    text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.NORMAL);

                    viewfinder_capture_preview.setVisibility(View.GONE);
                    layout_capture_scanner_mode.setVisibility(View.VISIBLE);
                    layout_capture_bluetooth_mode.setVisibility(View.GONE);

                    // camera
                    if (handler != null) {
                        handler.quitSynchronously();
                        handler = null;
                    }
                    CameraManager.get().closeDriver();

                    // bluetooth
                    if (KTSyncData.mChatService != null)
                        KTSyncData.mChatService.stop();
                    KTSyncData.bIsRunning = false;
                }
                break;

                case R.id.layout_capture_bluetooth: {

                    layout_capture_camera.setBackgroundResource(R.drawable.bg_rect_ffffff);
                    layout_capture_scanner.setBackgroundResource(R.drawable.bg_rect_ffffff);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_rect_ffffff_shadow_ff0000);
                    text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                    text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);
                    text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_ff0000));
                    text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.BOLD);

                    viewfinder_capture_preview.setVisibility(View.GONE);
                    layout_capture_scanner_mode.setVisibility(View.GONE);
                    layout_capture_bluetooth_mode.setVisibility(View.VISIBLE);

                    // camera
                    if (handler != null) {
                        handler.quitSynchronously();
                        handler = null;
                    }
                    CameraManager.get().closeDriver();

                    // Bluetooth 지원 && 비활성화 상태
                    if (!mBluetoothAdapter.isEnabled()) {

                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    }
                    KTSyncData.bIsRunning = true;
                }
                break;

                case R.id.btn_capture_bluetooth_device_find: {

                    mIsScanDeviceListActivityRun = true;
                    Intent serverIntent = new Intent(CaptureActivity.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }
                break;

                case R.id.btn_capture_type_number_add: {

                    onAddButtonClick();
                }
                break;

                case R.id.btn_capture_barcode_reset: {

                    onResetButtonClick();
                }
                break;

                case R.id.btn_capture_barcode_confirm: {

                    switch (mScanType) {
                        case BarcodeType.CONFIRM_MY_DELIVERY_ORDER:
                        case BarcodeType.CHANGE_DELIVERY_DRIVER: {

                            onUpdateButtonClick();
                        }
                        break;
                        case BarcodeType.DELIVERY_DONE: {

                            onConfirmButtonClick();
                        }
                        break;
                        case BarcodeType.PICKUP_CNR:
                        case BarcodeType.PICKUP_START_SCAN:
                        case BarcodeType.PICKUP_ADD_SCAN:
                        case BarcodeType.PICKUP_TAKE_BACK:
                        case BarcodeType.OUTLET_PICKUP_SCAN: {

                            onNextButtonClick();
                        }
                        break;
                        case BarcodeType.SCAN_DELIVERY_SHEET:
                        case BarcodeType.SELF_COLLECTION: {

                            onCaptureConfirmButtonClick();
                        }
                        break;
                    }
                }
                break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (handler != null) {
                handler.sendEmptyMessage(R.id.restart_preview);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // Handle these events so they don't launch the Camera app
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        KTSyncData.mChatService = new BluetoothChatService(bluetoothHandler);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult " + requestCode + " / " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: {
                // Bluetooth Device 연결
                if (resultCode == Activity.RESULT_OK) {

                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    connectDevice = mBluetoothAdapter.getRemoteDevice(address);
                    KTSyncData.mChatService.connect(connectDevice);
                }
            }
            break;

            case REQUEST_ENABLE_BT: {
                if (resultCode == Activity.RESULT_OK) {
                    //  블루투스 승인 요청 'YES'
                    setupChat();
                } else {
                    // 블루투스 승인 요청 'NO'
                    Toast.makeText(this, R.string.msg_bluetooth_not_enabled, Toast.LENGTH_SHORT).show();
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

            case REQUEST_SELF_COLLECTION: {
                onResetButtonClick();
            }
            break;

            case REQUEST_POD_SCAN: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getExtras().getString("data").equals("2")) {
                        finish();
                    }
                } else {

                    finish();
                }
            }
            break;

            case PERMISSION_REQUEST_CODE:
                if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                    Log.e("eylee", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                    isPermissionTrue = true;
                }
                break;
        }
    }


    // NOTIFICATION.  Camera / Bluetooth Setting
    // Camera
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private void initCamera(SurfaceHolder surfaceHolder) {

        try {
            if (isPermissionTrue) {
                CameraManager.get().openDriver(surfaceHolder);
                // Creating the handler starts the preview, which can also throw a
                // RuntimeException.
                if (handler == null) {
                    handler = new CaptureActivityHandler(CaptureActivity.this);
                }
            }
        } catch (Exception e) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.msg_camera_framework_bug) + "\n" + e.toString());
            builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                    finish();
                }
            });
            builder.show();
        }
    }

    public void drawViewfinder() {
        viewfinder_capture_preview.drawViewfinder();
    }


    // Bluetooth
    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler bluetoothHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            Log.e("capture", TAG + "  Message : " + msg.what + " / " + msg.arg1);

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE: {
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED: {

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_connected));
                            text_capture_bluetooth_device_name.setVisibility(View.VISIBLE);
                            text_capture_bluetooth_device_name.setText("(" + connectDeviceName + ")");
                            btn_capture_bluetooth_device_find.setVisibility(View.GONE);

                            KTSyncData.mKScan.DeviceConnected();
                        }
                        break;

                        case BluetoothChatService.STATE_CONNECTING: {

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_connecting));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.GONE);
                        }
                        break;

                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);
                            break;

                        case BluetoothChatService.STATE_LOST: {

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            KTSyncData.bIsConnected = false;
                        }
                        break;

                        case BluetoothChatService.STATE_FAILED: {

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
                break;

                case MESSAGE_READ: {

                    byte[] readBuf = (byte[]) msg.obj;

                    for (int i = 0; i < msg.arg1; i++)
                        KTSyncData.mKScan.HandleInputData(readBuf[i]);
                }
                break;

                case MESSAGE_DEVICE_NAME: {
                    // save the connected device's name
                    connectDeviceName = msg.getData().getString("deviceName");
                    Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.text_connected_to) + connectDeviceName, Toast.LENGTH_SHORT).show();
                }
                break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("Toast"), Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_DISPLAY: {

                    byte[] displayBuf = (byte[]) msg.obj;
                    String displayMessage = new String(displayBuf, 0, msg.arg1);
                    onBluetoothBarcodeAdd(displayMessage);
                    KTSyncData.bIsSyncFinished = true;
                }
                break;

                case MESSAGE_SEND:

                    byte[] sendBuf = (byte[]) msg.obj;
                    KTSyncData.mChatService.write(sendBuf);
                    break;
            }
        }
    };


    // EditText
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {     // edit_capture_type_number  touch

        if (mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_START_SCAN) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            // TEST.
            if (opID.equals("karam.kim")) {

                Log.e(TAG, "  EditText onTouch  > karam !!");
                inputMethodManager.showSoftInput(edit_capture_type_number, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {

            inputMethodManager.showSoftInput(edit_capture_type_number, InputMethodManager.SHOW_IMPLICIT);
        }

        return event.getAction() == MotionEvent.ACTION_UP;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (1 <= edit_capture_type_number.length()) {

            img_capture_type_number_delete.setVisibility(View.VISIBLE);
        } else {

            img_capture_type_number_delete.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }


    private void AlertShow(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(context.getResources().getString(R.string.text_warning));
        builder.setMessage(msg);
        builder.setPositiveButton(context.getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        builder.show();
    }


    public static void removeBarcodeListInstance() {
        if (barcodeList != null) {
            barcodeList.clear();
        }
    }


    // NOTIFICATION.  Camera / Scanner / Bluetooth / EditText  scan
    // Camera
    public void handleDecode(Result rawResult) {

        boolean isDuplicate = historyManager.addHistoryItem(rawResult);
        Log.i(TAG, "  handleDecode > " + rawResult.getText() + " / " + isDuplicate);

        if (!isDuplicate) {

            checkValidation(rawResult.getText(), false, "handleDecode");
        }

        //  Camera 연속 scan 가능 코드
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }
/*
    // TEST.
    public void handleDecode(String barcode) {

        boolean isHistorySave = historyManager.addHistoryItem(barcode);

        Log.e(TAG, TAG + "  handleDecode  " + isHistorySave);
        if (!isHistorySave) {

            // checkValidation(barcode, false);
        }

       *//* //  Camera 연속 scan 가능하도록...
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }*//*
    }*/

    // Scanner
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) {

            String tempStrScanNo = edit_capture_type_number.getText().toString().trim().toUpperCase();

            if (!tempStrScanNo.isEmpty()) {

                boolean isDuplicate = historyManager.addHistoryItem(new Result(tempStrScanNo, null, null, null));
                Log.i(TAG, "  onKey  KEYCODE_ENTER : " + tempStrScanNo + " / " + isDuplicate + "  //  " + event.getAction());

                // Scan Sheet의 경우 송장번호스캔은 1건만 가능하다.
                if (mScanType.equals(BarcodeType.SCAN_DELIVERY_SHEET)) {

                    mScanCount = 0;
                    isDuplicate = false;
                }


                if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                        || mScanType.equals(BarcodeType.PICKUP_CNR)
                        || mScanType.equals(BarcodeType.PICKUP_START_SCAN) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                        || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                    //This is the filter  2번 fired 해서 막음
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

        // bluetooth "\n"이 포함되어서 다른번호로 인식 > trim 으로 공백 없애기
        strBarcodeNo = strBarcodeNo.trim();

        if (!strBarcodeNo.isEmpty()) {

            boolean isDuplicate = historyManager.addHistoryItem(new Result(strBarcodeNo, null, null, null));
            Log.i(TAG, "  onBluetoothBarcodeAdd > " + strBarcodeNo + " / " + isDuplicate);

            checkValidation(strBarcodeNo, isDuplicate, "onBluetoothBarcodeAdd");
        }
    }

    // EditText
    public void onAddButtonClick() {

        String inputBarcodeNumber = edit_capture_type_number.getText().toString().trim().toUpperCase();

        if (!inputBarcodeNumber.isEmpty()) {

            boolean isDuplicate = historyManager.addHistoryItem(new Result(inputBarcodeNumber, null, null, null));
            Log.i(TAG, "  onAddButtonClick > " + inputBarcodeNumber + " / " + isDuplicate);


            if (mScanType.equals(BarcodeType.SCAN_DELIVERY_SHEET)) {       // POD SCAN // Scan Sheet 경우 송장번호스캔은 1건만 가능하다.

                mScanCount = 0;
                isDuplicate = false;
            }

            checkValidation(inputBarcodeNumber, isDuplicate, "onAddButtonClick");
        }
    }


    // TODO.  Add Barcode  (Validation Check / Add List)
    // NOTIFICATION.  Barcode Validation Check
    private void checkValidation(String strBarcodeNo, boolean isDuplicate, String where) {
        Log.e(TAG, "checkValidation called > " + where);
        Log.e(TAG, "checkValidation - " + strBarcodeNo + "  Duplicate : " + isDuplicate);

        strBarcodeNo = strBarcodeNo.replaceAll("\\r\\n|\\r|\\n", "");

        if (!NetworkUtil.isNetworkAvailable(context)) {
            AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
            return;
        }


        if (isDuplicate) {

            beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_DUPLE);
            Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 20);
            toast.show();

            edit_capture_type_number.setText("");
            inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
            return;
        }


        switch (mScanType) {
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER: {

                final String scanNo = strBarcodeNo;

                new Dpc3OutValidationCheckHelper.Builder(this, opID, outletDriverYN, strBarcodeNo)
                        .setOnDpc3OutValidationCheckListener(new Dpc3OutValidationCheckHelper.OnDpc3OutValidationCheckListener() {

                            @Override
                            public void OnDpc3OutValidationCheckResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                    inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                } else {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                    addScannedBarcode(scanNo, "checkValidation - CONFIRM_MY_DELIVERY_ORDER");
                                }
                            }

                            @Override
                            public void OnDpc3OutValidationCheckFailList(StdResult result) {

                                Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.CHANGE_DELIVERY_DRIVER: {

                final String scanNo = strBarcodeNo;

                new ChangeDriverValidationCheckHelper.Builder(this, opID, strBarcodeNo)
                        .setOnChangeDelDriverValidCheckListener(new ChangeDriverValidationCheckHelper.OnChangeDelDriverValidCheckListener() {

                            @Override
                            public void OnChangeDelDriverValidCheckResult(ChangeDriverResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                    inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                } else {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                    changeDriverResult = result;
                                    addScannedBarcode(scanNo, "checkValidation - CHANGE_DELIVERY_DRIVER");
                                }
                            }

                            @Override
                            public void OnChangeDelDriverValidCheckFailList(ChangeDriverResult result) {
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.PICKUP_CNR: {  //2016-09-21 add type validation

                // 2016-09-01 eylee 여기서 유효성 검사해서 네트워크 타기
                // 유효성 검사에 통과하면 여기서 소리 추가하면서 addBarcode
                // sqlite 에 cnr barcode scan no 가 있는지 확인하고 insert 하는 sqlite validation 부분 필요
                // validation 성공했을 때, ediText 에 넣고 실패하면, alert 띄우고 editText 에 들어가지 않음
                // 성공하면 sqlite 에 insert

                // Edit.  기존 CNR 중복 허용됨 > 중복 허용X 수정    by krm0219
                final String scanNo = strBarcodeNo;

                new CnRPickupValidationCheckHelper.Builder(this, opID, strBarcodeNo)
                        .setOnCnRPickupValidationCheckListener(new CnRPickupValidationCheckHelper.OnCnRPickupValidationCheckListener() {
                            @Override
                            public void OnCnRPickupValidationCheckResult(CnRPickupResult result) {

                                beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                pickupCNRRequester = result.getResultObject().getReqName();
                                addScannedBarcode(scanNo, "checkValidation - PICKUP_CNR");
                            }

                            @Override
                            public void OnCnRPickupValidationCheckFail() {

                                beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                deletePrevious(scanNo);
                                edit_capture_type_number.setText("");
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.PICKUP_START_SCAN: {

                final String scanNo = strBarcodeNo;

                new PickupScanValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                        .setOnPickupAddScanNoOneByOneUploadListener(new PickupScanValidationCheckHelper.OnPickupAddScanNoOneByOneUploadListener() {

                            @Override
                            public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                    addScannedBarcode(scanNo, "checkValidation - PICKUP_START_SCAN");
                                }
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.PICKUP_ADD_SCAN: {

                final String scanNo = strBarcodeNo;

                new PickupScanValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                        .setOnPickupAddScanNoOneByOneUploadListener(new PickupScanValidationCheckHelper.OnPickupAddScanNoOneByOneUploadListener() {

                            @Override
                            public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                    addScannedBarcode(scanNo, "checkValidation - PICKUP_ADD_SCAN");
                                }
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.PICKUP_TAKE_BACK: {

                final String scanNo = strBarcodeNo;

                new PickupTakeBackValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                        .setOnPickupTakeBackValidationCheckListener(new PickupTakeBackValidationCheckHelper.OnPickupTakeBackValidationCheckListener() {

                            @Override
                            public void onPickupTakeBackValidationCheckResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                    addScannedBarcode(scanNo, "checkValidation - PICKUP_TAKE_BACK");
                                }
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.OUTLET_PICKUP_SCAN: {

                final String scanNo = strBarcodeNo;

                new OutletPickupScanValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo, mRoute)
                        .setOnPickupAddScanNoOneByOneUploadListener(new OutletPickupScanValidationCheckHelper.OnPickupAddScanNoOneByOneUploadListener() {

                            @Override
                            public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                                    addScannedBarcode(scanNo, "checkValidation - OUTLET_PICKUP_SCAN");
                                }
                            }
                        }).build().execute();
                break;
            }
            case BarcodeType.SELF_COLLECTION: {     // 2016-09-20 eylee


                if (!isInvoiceCodeRule(strBarcodeNo, mScanType)) {

                    beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_ERROR);
                    Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    return;
                }

                beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);

                //2016-09-12 eylee  nq 끼리만 self collector 가능하게 수정하기
                //직접 add btn 을 누르는 것 뿐만 아니라 스캔해서 들어오는 number 들 validation check 위해서 이 부분으로 이동이 필요함
                if (!scanBarcodeArrayList.isEmpty()) {

                    boolean tempIsNonQ10QFSOrder = isNonQ10QFSOrder;
                    boolean tempValidation = isNonQ10QFSOrderForSelfCollection(strBarcodeNo);

                    if (tempIsNonQ10QFSOrder != tempValidation) {
                        // alert 띄워줘야 함 type 이 다르다는 - 기존 Self - Collection 과 새로 NQ 가
                        Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_different_order_type), Toast.LENGTH_SHORT);
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
            default:

                beepManager.playBeepSoundAndVibrate(BeepManager.BELL_SOUNDS_SUCCESS);
                addScannedBarcode(strBarcodeNo, "checkValidation - Default");
                break;
        }
    }


    // NOTIFICATION.  Add Barcode List
    private void addScannedBarcode(String strBarcodeNo, String where) {
        Log.e(TAG, "  addScannedBarcode  > " + where + " // " + strBarcodeNo);

        mScanCount++;
        text_capture_scan_count.setText(String.valueOf(mScanCount));


        BarcodeData data = new BarcodeData();
        data.setBarcode(strBarcodeNo.toUpperCase());
        data.setState("NONE");

        if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {
            data.setBarcode(changeDriverResult.getResultObject().getTrackingNo() + "  |  " + changeDriverResult.getResultObject().getStatus() + "  |  " + changeDriverResult.getResultObject().getCurrentDriver());
        }


        // 스캔 시 최근 스캔한 바코드가 제일 위로 셋팅됨.
        switch (mScanType) {
            case BarcodeType.CONFIRM_MY_DELIVERY_ORDER:
            case BarcodeType.CHANGE_DELIVERY_DRIVER:
            case BarcodeType.PICKUP_CNR:
            case BarcodeType.PICKUP_START_SCAN:
            case BarcodeType.PICKUP_ADD_SCAN:
            case BarcodeType.PICKUP_TAKE_BACK: {

                data.setState("SUCCESS");

                if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                    data.setChangeDeliveryText(changeDriverResult.getResultObject().getTrackingNo() + "  |  " + changeDriverResult.getResultObject().getStatus() + "  |  " + changeDriverResult.getResultObject().getCurrentDriver());
                    barcodeList.add(changeDriverResult.getResultObject().getTrackingNo() + "  |  " + changeDriverResult.getResultObject().getStatus() + "  |  " + changeDriverResult.getResultObject().getCurrentDriver());
                    changeDriverArrayList.add(changeDriverResult);
                } else {

                    barcodeList.add(strBarcodeNo);
                }

                scanBarcodeArrayList.add(0, data);
                scanBarcodeNoListAdapter.notifyDataSetChanged();
                list_capture_scan_barcode.setSelection(0);
                list_capture_scan_barcode.smoothScrollToPosition(0);
            }
            break;

            case BarcodeType.OUTLET_PICKUP_SCAN: {

                ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();
                int position = -400;

                for (int i = 0; i < listItem.size(); i++) {

                    String tracking_no = listItem.get(i).getTrackingNo();

                    if (tracking_no.equalsIgnoreCase(strBarcodeNo)) {
                        Log.e("krm0219", "Compare : " + tracking_no + " vs " + strBarcodeNo);
                        position = i;
                        data.setState("SUCCESS");
                        listItem.get(i).setScanned(true);
                    }
                }


                if (0 <= position) {

                    Log.e("krm0219", " Position : " + position);
                    barcodeList.add(strBarcodeNo);

                    scanBarcodeArrayList.set(position, data);
                    scanBarcodeNoListAdapter.notifyDataSetChanged();
                    list_capture_scan_barcode.setSelection(0);
                    list_capture_scan_barcode.smoothScrollToPosition(0);
                } else {

                    mScanCount--;
                    text_capture_scan_count.setText(String.valueOf(mScanCount));

                    scanBarcodeNoListAdapter.notifyDataSetChanged();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle(context.getResources().getString(R.string.text_warning));
                    alertDialog.setMessage(context.getResources().getString(R.string.msg_no_outlet_parcels));
                    alertDialog.setPositiveButton(context.getResources().getString(R.string.button_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
            break;

            case BarcodeType.SCAN_DELIVERY_SHEET: {

                if (isScanBarcode(strBarcodeNo)) {

                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 20);
                    toast.show();

                    scanBarcodeArrayList.clear();
                    edit_capture_type_number.setText("");
                    inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                    return;
                } else {

                    scanBarcodeArrayList.clear();
                    scanBarcodeArrayList.add(data);
                }
            }
            break;
            default:   // 스캔 시 최근 스캔한 바코드가 아래로 추가됨.
                // maybe.. DELIVERY DONE
                scanBarcodeArrayList.add(data);
                scanBarcodeNoListAdapter.notifyDataSetChanged();
                break;
        }


        if (!mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) && !mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                && !mScanType.equals(BarcodeType.PICKUP_CNR)) {

            updateInvoiceNO(mScanType, strBarcodeNo);
        }

        edit_capture_type_number.setText("");
        inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
    }


    private void updateInvoiceNO(String scanType, String invoiceNo) {

        int updateCount = 0;

        if (scanType.equals(BarcodeType.PICKUP_START_SCAN) || scanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || scanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || scanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            updateCount = 1;
        } else if (mScanType.equals(BarcodeType.DELIVERY_DONE)) {

            // 복수건 배달완료 시점에서는 아무것도 안함 사인전 jmkang 2013-05-08
            ContentValues contentValues = new ContentValues();
            contentValues.put("reg_id", opID); // 해당 배송번호를 가지고 자신의아이디만 없데이트

            updateCount = DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentValues,
                    "invoice_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?", new String[]{invoiceNo, opID});
        } else if (mScanType.equals(BarcodeType.SCAN_DELIVERY_SHEET) || mScanType.equals(BarcodeType.SELF_COLLECTION)) {

            if (isInvoiceCodeRule(invoiceNo, mScanType)) {
                updateCount = 1;
            }
        }


        String message = String.format(" [ %s ] ", mScanTitle);
        String result;
        String inputBarcode = scanBarcodeArrayList.get(scanBarcodeArrayList.size() - 1).getBarcode();

        if (updateCount < 1) {

            message += context.getResources().getString(R.string.text_not_assigned);
            result = "FAIL";
        } else {

            message += context.getResources().getString(R.string.text_success);
            result = "SUCCESS";
        }

        if (!mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            BarcodeData data = new BarcodeData();
            data.setBarcode(inputBarcode);
            data.setState(result);

            scanBarcodeArrayList.set(scanBarcodeArrayList.size() - 1, data);
            scanBarcodeNoListAdapter.notifyDataSetChanged();
        }

        // 교체 후 Adapter.notifyDataSetChanged() 메서드로 listview  변경 add comment by eylee 2016-09-08
        if (updateCount < 1) { // 실패일때만 보여준다.

            // SCAN > Delivery done
            // Server Result.. 무조건 성공이므로 BeepManager.java 에서 처리 불가능. 실패시 이쪽으로 이동.

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {

                vibrator.vibrate(200L);
            }

            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);
            toast.show();
        }
    }


    //invalidation 일 때, SQLite  data 삭제
    public void deletePrevious(String text) {

        if (!text.equals("")) {
            historyManager.deletePrevious(text);
        }
    }


    // TODO.  하단 버튼 클릭 이벤트 (작업 수행)
    // NOTIFICATION.  Confirm my delivery order / Change Delivery Driver
    public void onUpdateButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(context)) {
            AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
            return;
        }

        if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
            AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
            return;
        }


        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER)) {

            new ManualDriverAssignHelper.Builder(this, opID, officeCode, deviceID, scanBarcodeArrayList)
                    .setOnDriverAssignV2EventListener(new ManualDriverAssignHelper.OnDriverAssignV2EventListener() {

                        @Override
                        public void onPostAssignResult(DriverAssignResult stdResult) {

                            if (stdResult.getResultCode() == 0) {

                                onResetButtonClick();
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                            builder.setTitle(context.getResources().getString(R.string.text_driver_assign_result));
                            builder.setMessage(stdResult.getResultMsg());
                            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    }).build().execute();
        } else if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

            if (gpsEnable && gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " saveServerUploadSign  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }


            new ManualChangeDelDriverHelper.Builder(this, opID, officeCode, deviceID, changeDriverArrayList, latitude, longitude)
                    .setOnChangeDelDriverEventListener(new OnChangeDelDriverEventListener() {

                        @Override
                        public void onPostAssignResult(DriverAssignResult stdResult) {

                            if (stdResult.getResultCode() == 0) {

                                onResetButtonClick();
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                            builder.setTitle(context.getResources().getString(R.string.text_driver_assign_result));
                            builder.setMessage(stdResult.getResultMsg());
                            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    }).build().execute();
        }
    }


    // NOTIFICATION.  Scan - Delivery Done
    public void onConfirmButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }


        String name;
        String receiverName = "";
        boolean diffReceiverName = false;
        ArrayList<BarcodeData> deliveryDoneBarcodeArrayList = new ArrayList<>();

        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            BarcodeData barcodeData = scanBarcodeArrayList.get(i);

            if (barcodeData.getState().equals("SUCCESS")) {

                name = getDeliveryReceiverName(barcodeData.getBarcode());

                // 수취인성명이 틀린경우
                if (!receiverName.equals("")) {
                    if (!receiverName.toUpperCase().equals(name.toUpperCase())) {
                        diffReceiverName = true;
                    }
                }

                receiverName = name;
                deliveryDoneBarcodeArrayList.add(barcodeData);
            }
        }

        // 받는사람이 틀리다면 에러 메세지
        if (diffReceiverName) {

            Toast toast = Toast.makeText(this, R.string.msg_different_recipient_address, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }


        if (0 < deliveryDoneBarcodeArrayList.size()) {

            Intent intent = new Intent(this, DeliveryDoneActivity.class);
            intent.putExtra("title", mScanTitle);
            intent.putExtra("type", "D");
            intent.putExtra("data", deliveryDoneBarcodeArrayList);
            this.startActivityForResult(intent, REQUEST_DELIVERY_DONE);
        } else {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }


    /**
     * date : 2016-08-30  eylee
     * desc : pickup C&R scan next 버튼 클릭했을 때
     */
    // NOTIFICATION.  Pickup (CnR / Scan All / Add Scan / Take Back / Outlet)
    public void onNextButtonClick() {

        if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            boolean isScanned = false;

            for (int i = 0; i < resultData.getTrackingNoList().size(); i++) {

                if (resultData.getTrackingNoList().get(i).isScanned()) {
                    isScanned = true;
                }
            }

            if (!isScanned) {

                Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
        } else {

            if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

                Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
        }


        String scannedQty = String.valueOf(scanBarcodeArrayList.size());    // scanned count
        String scannedList = "";                                            // scanned barcode list

        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            scannedList += scanBarcodeArrayList.get(i).getBarcode();

            if (i != (scanBarcodeArrayList.size() - 1)) {
                scannedList += ",";
            }
        }

        removeBarcodeListInstance();
        switch (mScanType) {
            case BarcodeType.PICKUP_CNR: {

                Intent intent = new Intent(this, CnRPickupDoneActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_cnr_pickup_done));
                intent.putExtra("type", BarcodeType.PICKUP_CNR);
                intent.putExtra("senderName", pickupCNRRequester);
                intent.putExtra("scannedList", scannedList);
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_CNR);
            }
            break;

            case BarcodeType.PICKUP_START_SCAN: {

                Intent intent = new Intent(this, PickupDoneActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_start_to_scan));
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList);
                intent.putExtra("scannedQty", scannedQty);
                startActivity(intent);
                finish();
            }
            break;

            case BarcodeType.PICKUP_ADD_SCAN: {

                Intent intent = new Intent(this, PickupAddScanActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_title_add_pickup));
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("senderName", pickupApplicantName);
                intent.putExtra("scannedList", scannedList);
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_ADD_SCAN);
            }
            break;

            case BarcodeType.PICKUP_TAKE_BACK: {

                Intent intent = new Intent(this, PickupTakeBackActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.button_take_back));
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList);
                intent.putExtra("totalQty", mQty);
                intent.putExtra("takeBackQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_TAKE_BACK);
            }
            break;

            case BarcodeType.OUTLET_PICKUP_SCAN: {

                int scanned_qty = 0;
                for (int i = 0; i < resultData.getTrackingNoList().size(); i++) {

                    if (resultData.getTrackingNoList().get(i).isScanned()) {
                        scanned_qty++;
                    }
                }

                String scanned_list = "";
                for (int i = 0; i < resultData.getTrackingNoList().size(); i++) {

                    if (resultData.getTrackingNoList().get(i).isScanned()) {

                        if (!scanned_list.equals("")) {
                            scanned_list += ",";
                        }

                        scanned_list += resultData.getTrackingNoList().get(i).getTrackingNo();
                    }
                }
                Log.e(TAG, "Outlet Pickup Scanned List : " + scanned_list);


                Intent intent = new Intent(this, OutletPickupDoneActivity.class);
                intent.putExtra("title", mScanTitle);
                intent.putExtra("pickup_no", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("qty", mQty);
                intent.putExtra("route", mRoute);
                intent.putExtra("scanned_qty", scanned_qty);
                intent.putExtra("tracking_data", resultData);
                intent.putExtra("scanned_list", scanned_list);
                startActivity(intent);
                finish();
            }
            break;
        }
    }


    // NOTIFICATION.  Reset
    public void onResetButtonClick() {

        if (scanBarcodeArrayList != null && !scanBarcodeArrayList.isEmpty()) {

            mScanCount = 0;
            text_capture_scan_count.setText(String.valueOf(mScanCount));

            scanBarcodeArrayList.clear();
            scanBarcodeNoListAdapter.notifyDataSetChanged();
            historyManager.clearHistory();


            if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeData data = new BarcodeData();
                    data.setState("FAIL");
                    data.setBarcode(listItem.get(i).getTrackingNo());
                    scanBarcodeArrayList.add(i, data);
                }

                scanBarcodeNoListAdapter.notifyDataSetChanged();
            }
        }

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_START_SCAN) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            removeBarcodeListInstance();
        }
    }


    public String getDeliveryReceiverName(String barcodeNo) {

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        String receiverName = "";

        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
        }

        cursor.close();

        return receiverName;
    }


    @Override
    public synchronized void onPause() {
        super.onPause();

        if (mIsScanDeviceListActivityRun || KTSyncData.bIsRunning) {
            mIsScanDeviceListActivityRun = false;
            return;
        }

        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        CameraManager.get().closeDriver();
        m_sensor_manager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        historyManager.clearHistory();

        DataUtil.stopGPSManager(gpsTrackerManager);

        // Stop the Bluetooth chat services
        if (KTSyncData.mChatService != null)
            KTSyncData.mChatService.stop();
        KTSyncData.mChatService = null;
        KTSyncData.bIsRunning = false;

        if (beepManager != null) {
            beepManager.destroy();
        }
    }


    // TODO.    자세히 모름..  self-collection / POD
    public String getScanDeliveryCount() {          // 시트스캔건수

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT count(*) as scan_cnt FROM " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY + " where reg_id='" + opID + "'");

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("scan_cnt"));
        }

        cursor.close();
        return String.valueOf(count);
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
     * 이미 바코드스캔조회 (Scan Barcode)
     */
    public boolean isScanBarcode(String barcodeNo) {

        boolean status = false;

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT count(*) as scan_cnt FROM " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY + " where invoice_no = '" + barcodeNo + "' and reg_id='" + opID + "'");

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("scan_cnt"));
            if (0 < count) {
                status = true;
            }
        }

        cursor.close();
        return status;
    }


    /*
     * NOTIFICATION  -  Self Collection
     * modified : 2016-09-09 eylee self-collection 복수 건 처리 추가
     */
    public void onCaptureConfirmButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (!isInvoiceCodeRule(scanBarcodeArrayList.get(0).getBarcode(), mScanType)) {

            Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        // POD 스캔 Step2
        if (mScanType.equals(BarcodeType.SCAN_DELIVERY_SHEET)) {

            Intent intentCamera = new Intent(this, CameraActivity.class);
            intentCamera.putExtra("barcode", scanBarcodeArrayList.get(0).getBarcode());
            this.startActivityForResult(intentCamera, REQUEST_POD_SCAN);
        } else {  // SELF COLLECTOR     //복수건 가져다가 self-collection by 2016-09-09

            BarcodeData barcodeData;
            ArrayList<BarcodeData> barcodeDataArrayList = new ArrayList<>();

            for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

                barcodeData = scanBarcodeArrayList.get(i);

                if (barcodeData.getState().equals("FAIL")) {

                    Toast toast = Toast.makeText(this, R.string.msg_invalid_scan, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    return;
                } else {

                    barcodeDataArrayList.add(barcodeData);
                }
            }

            if (0 < barcodeDataArrayList.size()) {

                Intent intentSign = new Intent(this, SigningActivity.class);
                intentSign.putExtra("title", mScanTitle);
                intentSign.putExtra("data", barcodeDataArrayList);
                intentSign.putExtra("nonq10qfs", String.valueOf(isNonQ10QFSOrder));
                this.startActivityForResult(intentSign, REQUEST_SELF_COLLECTION);
            } else {

                Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }
    }


    public void onPodUpdateButtonClick() {

        //스캔한 건이 있으면  2단계 진행 후 업로드
        if (scanBarcodeArrayList != null && 0 < scanBarcodeArrayList.size()) {

            Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_click_next_before_upload), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(context)) {
            AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
            return;
        }

        if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
            AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
            return;
        }

        ArrayList<UploadData> songjanglist = new ArrayList<>();

        // 업로드 대상건 로컬 DB 조회
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        String selectQuery = "select invoice_no" + " , stat " + " from " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY +
                " where reg_id= '" + opID + "'" + " and punchOut_stat <> 'S' ";
        Cursor cs = dbHelper.get(selectQuery);

        if (cs.moveToFirst()) {
            do {
                UploadData data = new UploadData();
                data.setNoSongjang(cs.getString(cs.getColumnIndex("invoice_no")));
                data.setStat(cs.getString(cs.getColumnIndex("stat")));
                data.setType("D");
                songjanglist.add(data);
            } while (cs.moveToNext());
        }

        if (0 < songjanglist.size()) {

            new ManualPodUploadHelper.Builder(this, opID, officeCode, deviceID, songjanglist).
                    setOnPodUploadEventListener(new ManualPodUploadHelper.OnPodUploadEventListener() {

                        @Override
                        public void onPostResult() {

                            Intent intent = getIntent();
                            intent.putExtra("result", "OK");
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }

                        @Override
                        public void onPostFailList(ArrayList<String> resultList) {
                            // 여기서 finish() 하면서 failList 전달
                            StringBuilder result = new StringBuilder();
                            for (String failInfo : resultList) {  //no songjang:Reason
                                result.append(failInfo);
                            }

                            Intent intent = getIntent();
                            intent.putExtra("result", result.toString());
                            intent.putExtra("type", "D4");
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }).build().execute();
        }
    }


    /*
     * Qxpress송장번호 규칙(범용)
     * 운송장번호 규칙이 맞는지 체크
     * 10문자 안넘으면 false, 맨앞두글자가 KR,SG,QX,JP,CN이 아닐경우 false, 5,6번째가 숫자가 아닐경우 false, 영문숫자조합
     */
    // SELF_COLLECTION , SCAN_DELIVERY_SHEET
    public static boolean isInvoiceCodeRule(String invoiceNo, String mType) {

        // 2016-08-23 eylee C2C, RPC 번호 스캔할 수 있도록 기능확장
        if (!mType.equals(BarcodeType.SELF_COLLECTION)) {
            if (invoiceNo.length() < 10) {
                return false;
            }
        }

        //영문숫자만 가능
        boolean bln = Pattern.matches("^[a-zA-Z0-9]*$", invoiceNo);
        if (!bln) {
            return false;
        }
        if (invoiceNo.length() >= 10) {    // self collection c2c 아닐 때

            String sub_invoice_int = invoiceNo.substring(4, 6);
            return isStringDouble(sub_invoice_int);
        }

        return true;
    }

    //숫자인지체크
    public static boolean isStringDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}