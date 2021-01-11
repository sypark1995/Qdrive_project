/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.graphics.drawable.Drawable;
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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.util.qdrive.barcodescanner.bluetooth.BluetoothChatService;
import com.giosis.util.qdrive.barcodescanner.bluetooth.DeviceListActivity;
import com.giosis.util.qdrive.barcodescanner.bluetooth.KScan;
import com.giosis.util.qdrive.barcodescanner.bluetooth.KTSyncData;
import com.giosis.util.qdrive.barcodescanner.camera.CameraManager;
import com.giosis.util.qdrive.barcodescanner.history.HistoryManager;
import com.giosis.util.qdrive.list.delivery.DeliveryDoneActivity;
import com.giosis.util.qdrive.list.pickup.CnRPickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.OutletPickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.OutletPickupDoneResult;
import com.giosis.util.qdrive.list.pickup.PickupAddScanActivity;
import com.giosis.util.qdrive.list.pickup.PickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.PickupTakeBackActivity;
import com.giosis.util.qdrive.main.SelfCollectionDoneActivity;
import com.giosis.util.qdrive.singapore.BuildConfig;
import com.giosis.util.qdrive.singapore.LoginActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;
import com.google.zxing.Result;

import java.io.Serializable;
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
        OnFocusChangeListener, TextWatcher, SensorEventListener, OnKeyListener {
    private static final String TAG = "CaptureActivity";


    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_DELIVERY_DONE = 10;
    private static final int REQUEST_PICKUP_CNR = 11;
    private static final int REQUEST_PICKUP_ADD_SCAN = 12;
    private static final int REQUEST_PICKUP_TAKE_BACK = 13;
    private static final int REQUEST_SELF_COLLECTION = 20;           //2016-09-09 eylee


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DISPLAY = 6;
    public static final int MESSAGE_SEND = 7;
    public static final int MESSAGE_SETTING = 255;
    public static final int MESSAGE_EXIT = 0;
    // Key names received from the BluetoothChatService Handler
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

    SurfaceView surface_capture_preview;
    ViewfinderView viewfinder_capture_preview;
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
    ListView list_capture_scan_barcode;

    Button btn_capture_barcode_reset;
    Button btn_capture_barcode_confirm;

    Drawable editTextDelButtonDrawable;
    EditText currentFocusEditText;

    //
    Context context;
    String opID;
    String officeCode;
    String deviceID;

    String title;
    String mScanType;

    String pickupNo;
    String pickupApplicantName;         //2016-09-26 eylee
    String pickupCNRRequester;          //2016-09-03 pickup cnr Requester
    String outletDriverYN;              //krm0219  outlet
    String mQty;
    String mRoute;
    OutletPickupDoneResult resultData;


    int mScanCount = 0;
    private ArrayList<BarcodeListData> scanBarcodeArrayList = null;
    private InputBarcodeNoListAdapter scanBarcodeNoListAdapter;
    // resume 시 recreate 할 data list
    private ArrayList<String> barcodeList = new ArrayList<>();
    private ArrayList<ChangeDriverResult.Data> changeDriverObjectArrayList = new ArrayList<>();
    private ChangeDriverResult.Data changeDriverResult;
    private HistoryManager historyManager;


    public boolean isNonQ10QFSOrder = false;


    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    InputMethodManager inputMethodManager;
    private BeepManager beepManager;
    private BeepManager beepManagerError;
    private BeepManager beepManagerDuple;

    SurfaceHolder surfaceHolder;
    private boolean hasSurface;
    SensorManager m_sensor_manager;
    Sensor m_light_sensor;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;

    private BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothDevice connectedDevice = null;
    private String connectedDeviceName = null;
    boolean mIsScanDeviceListActivityRun = false;


    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA};
    // -------------------------------------------


    ViewfinderView getViewfinderView() {
        return viewfinder_capture_preview;
    }

    public Handler getHandler() {
        return handler;
    }


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
        btn_capture_type_number_add = findViewById(R.id.btn_capture_type_number_add);

        layout_capture_scan_count = findViewById(R.id.layout_capture_scan_count);
        text_capture_scan_count = findViewById(R.id.text_capture_scan_count);
        list_capture_scan_barcode = findViewById(R.id.list_capture_scan_barcode);

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
        edit_capture_type_number.setOnFocusChangeListener(this);
        edit_capture_type_number.addTextChangedListener(this);
        edit_capture_type_number.setOnKeyListener(this);
        edit_capture_type_number.setLongClickable(false);
        edit_capture_type_number.setTextIsSelectable(false);

        editTextDelButtonDrawable = getResources().getDrawable(R.drawable.btn_delete);
        editTextDelButtonDrawable.setBounds(0, 0, editTextDelButtonDrawable.getIntrinsicWidth(), editTextDelButtonDrawable.getIntrinsicHeight());


        //------------------
        context = getApplicationContext();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();
        try {
//            outletDriverYN = SharedPreferencesHelper.getPrefSignInOutletDriver(getApplicationContext());
            outletDriverYN = MyApplication.preferences.getOutletDriver();
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


                ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeListData data = new BarcodeListData();
                    data.setState("FAIL");
                    data.setBarcode(listItem.get(i).getTrackingNo());

                    scanBarcodeArrayList.add(i, data);
                }
            }
            break;
        }


        scanBarcodeNoListAdapter = new InputBarcodeNoListAdapter(this, scanBarcodeArrayList, mScanType);
        list_capture_scan_barcode.setAdapter(scanBarcodeNoListAdapter);

        if (0 < scanBarcodeArrayList.size()) {

            list_capture_scan_barcode.setSelection(scanBarcodeArrayList.size() - 1);
        }


        historyManager = new HistoryManager(this);
        historyManager.clearHistory();
        inactivityTimer = new InactivityTimer(this);

        beepManager = new BeepManager(this, 1);         // 띵동
        beepManagerError = new BeepManager(this, 2);    // 삐~
        beepManagerDuple = new BeepManager(this, 3);    // 삐비~


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
        // Get local Bluetooth adapter        // Bluetooth 지원 여부 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported          // Bluetooth 지원하지 않음
        if (mBluetoothAdapter == null && !BuildConfig.DEBUG) {

            Toast.makeText(this, context.getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_LONG).show();
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

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_update));      //onUpdateButtonClick
                break;
            case BarcodeType.CHANGE_DELIVERY_DRIVER:

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_done));         //onUpdateButtonClick
                break;
            case BarcodeType.DELIVERY_DONE: {

                layout_capture_scan_count.setVisibility(View.GONE);
                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         //onConfirmButtonClick
            }
            break;
            case BarcodeType.PICKUP_CNR:
            case BarcodeType.PICKUP_SCAN_ALL:
            case BarcodeType.PICKUP_ADD_SCAN:
            case BarcodeType.PICKUP_TAKE_BACK:
            case BarcodeType.OUTLET_PICKUP_SCAN:

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            //onNextButtonClick
                break;
            case BarcodeType.SELF_COLLECTION:

                btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick
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
        Log.e(TAG, TAG + "   onResume");


        if (MyApplication.preferences.getUserId().equals("")) {

            Toast.makeText(CaptureActivity.this, getResources().getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CaptureActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

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


        // Scanned List
        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR) || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)
                || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            try {

                scanBarcodeArrayList.clear();
                scanBarcodeNoListAdapter.notifyDataSetChanged();

                if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                        || mScanType.equals(BarcodeType.PICKUP_CNR) || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)
                        || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                    if (barcodeList != null && 0 < barcodeList.size()) {
                        for (int i = 0; i < barcodeList.size(); i++) {

                            BarcodeListData data = new BarcodeListData();
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
                } else if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                    mScanCount = 0;

                    if (resultData != null) {

                        ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();

                        for (int i = 0; i < listItem.size(); i++) {

                            String tracking_no = listItem.get(i).getTrackingNo();
                            boolean isScanned = listItem.get(i).isScanned();

                            BarcodeListData data = new BarcodeListData();
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

                Log.e("krm0219", TAG + "  Scan Count : " + mScanCount);
            } catch (Exception e) {

                Toast.makeText(CaptureActivity.this, context.getResources().getString(R.string.text_data_error), Toast.LENGTH_SHORT).show();

                scanBarcodeArrayList.clear();
                scanBarcodeNoListAdapter.notifyDataSetChanged();
                removeBarcodeListInstance();
            }
        } else if (mScanType.equals(BarcodeType.SELF_COLLECTION)) {

            text_top_title.setText(context.getResources().getString(R.string.text_title_scan_barcode));
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));

            scanBarcodeArrayList.clear();
            scanBarcodeNoListAdapter.notifyDataSetChanged();
        }

        inactivityTimer.onResume();
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

                    layout_capture_camera.setBackgroundResource(R.drawable.bg_tab_bottom_46a73f);
                    layout_capture_scanner.setBackgroundResource(R.drawable.bg_ffffff);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_ffffff);
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

                    layout_capture_camera.setBackgroundResource(R.drawable.bg_ffffff);
                    layout_capture_scanner.setBackgroundResource(R.drawable.bg_tab_bottom_46a73f);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_ffffff);
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

                    layout_capture_camera.setBackgroundResource(R.drawable.bg_ffffff);
                    layout_capture_scanner.setBackgroundResource(R.drawable.bg_ffffff);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.bg_tab_bottom_46a73f);
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

                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                    }
                    KTSyncData.bIsRunning = true;
                }
                break;

                case R.id.btn_capture_bluetooth_device_find: {

                    mIsScanDeviceListActivityRun = true;
                    Intent intent = new Intent(CaptureActivity.this, DeviceListActivity.class);
                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
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
        KTSyncData.mChatService = new BluetoothChatService(this, bluetoothHandler);
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (KTSyncData.AutoConnect && KTSyncData.bIsRunning)
                KTSyncData.mChatService.connect(connectedDevice);
        }
    };


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
                    Toast.makeText(this, R.string.msg_bluetooth_enabled, Toast.LENGTH_SHORT).show();
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

            case PERMISSION_REQUEST_CODE: {
                if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                    Log.e("eylee", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                    isPermissionTrue = true;
                }
            }
            break;
        }
    }


    public static class BarcodeListData implements Serializable {
        private String barcode;
        private String state;

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    // NOTIFICATION.  Camera  /  Bluetooth Setting
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
                CameraManager.get().CameraOpenDriver(surfaceHolder);
                // Creating the handler starts the preview, which can also throw a
                // RuntimeException.
                if (handler == null) {
                    handler = new CaptureActivityHandler(this);
                }
            }
        } catch (Exception e) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.msg_camera_framework_bug) + "\n" + e.toString());
            builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
            builder.setOnCancelListener(new FinishListener(this));
            builder.show();
        }
    }

    public void drawViewfinder() {
        viewfinder_capture_preview.drawViewfinder();
    }

    // Bluetooth
    @SuppressLint("HandlerLeak")
    private final Handler bluetoothHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_connected));
                            text_capture_bluetooth_device_name.setVisibility(View.VISIBLE);
                            text_capture_bluetooth_device_name.setText("(" + connectedDeviceName + ")");
                            btn_capture_bluetooth_device_find.setVisibility(View.GONE);

                            removeCallbacks(mUpdateTimeTask);
                            KTSyncData.mKScan.DeviceConnected(true);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_connecting));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.GONE);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            break;
                        case BluetoothChatService.STATE_LOST:

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            KTSyncData.bIsConnected = false;
                            postDelayed(mUpdateTimeTask, 2000);
                            break;
                        case BluetoothChatService.STATE_FAILED:

                            text_capture_bluetooth_connect_state.setText(context.getResources().getString(R.string.text_disconnected));
                            text_capture_bluetooth_device_name.setVisibility(View.GONE);
                            btn_capture_bluetooth_device_find.setVisibility(View.VISIBLE);

                            postDelayed(mUpdateTimeTask, 5000);
                            break;
                    }
                    break;

                case MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;

                    for (int i = 0; i < msg.arg1; i++)
                        KTSyncData.mKScan.HandleInputData(readBuf[i]);
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.text_connected_to) + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_DISPLAY:

                    byte[] displayBuf = (byte[]) msg.obj;
                    String displayMessage = new String(displayBuf, 0, msg.arg1);
                    onBluetoothBarcodeAdd(displayMessage);
                    KTSyncData.bIsSyncFinished = true;
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
    public boolean onTouch(View v, MotionEvent event) {

        if (mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            // TEST.
            if (opID.equals("karam.kim") || (opID.equals("YuMin.Dwl") && BuildConfig.DEBUG)) {

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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if (hasFocus) {
            currentFocusEditText = (EditText) v;
        }
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


    public void removeBarcodeListInstance() {
        if (barcodeList != null) {
            barcodeList.clear();
        }
    }


    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     */
    // NOTIFICATION.  Camera  /  Scanner  /  Bluetooth  /  EditText scan
    // Camera
    public void handleDecode(Result rawResult) {

        inactivityTimer.onActivity();

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

    // Scanner
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) {

            String tempStrScanNo = edit_capture_type_number.getText().toString().trim();

            if (!tempStrScanNo.equals("")) {

                boolean isDuplicate = historyManager.addHistoryItem(new Result(tempStrScanNo, null, null, null));
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

        if (0 < inputBarcodeNumber.length()) {

            boolean isDuplicate = historyManager.addHistoryItem(new Result(inputBarcodeNumber, null, null, null));
            Log.i(TAG, "  onAddButtonClick > " + inputBarcodeNumber + " / " + isDuplicate);

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

            beepManagerDuple.playBeepSoundAndVibrate();
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

                new ConfirmMyOrderValidationCheckHelper.Builder(this, opID, outletDriverYN, strBarcodeNo)
                        .setOnDpc3OutValidationCheckListener(new ConfirmMyOrderValidationCheckHelper.OnDpc3OutValidationCheckListener() {

                            @Override
                            public void OnDpc3OutValidationCheckResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManagerError.playBeepSoundAndVibrate();
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                    inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                } else {

                                    beepManager.playBeepSoundAndVibrate();
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

                                    beepManagerError.playBeepSoundAndVibrate();
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                    inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                } else {

                                    beepManager.playBeepSoundAndVibrate();
                                    changeDriverResult = result.getResultObject();
                                    addScannedBarcode(scanNo, "checkValidation - CHANGE_DELIVERY_DRIVER");
                                }
                            }
                        }).build().execute();

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

                new CnRPickupValidationCheckHelper2.Builder(this, opID, strBarcodeNo)
                        .setOnCnRPickupValidationCheckListener(new CnRPickupValidationCheckHelper2.OnCnRPickupValidationCheckListener() {
                            @Override
                            public void OnCnRPickupValidationCheckResult(CnRPickupResult result) {

                                beepManager.playBeepSoundAndVibrate();
                                pickupCNRRequester = result.getResultObject().getReqName();
                                addScannedBarcode(scanNo, "checkValidation - PICKUP_CNR");
                            }

                            @Override
                            public void OnCnRPickupValidationCheckFail() {

                                beepManagerError.playBeepSoundAndVibrate();
                                deletePrevious(scanNo);
                                edit_capture_type_number.setText("");
                            }
                        }).build().execute();
            }
            break;
            case BarcodeType.PICKUP_SCAN_ALL: {

                final String scanNo = strBarcodeNo;

                new PickupScanValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                        .setOnPickupAddScanNoOneByOneUploadListener(new PickupScanValidationCheckHelper.OnPickupAddScanNoOneByOneUploadListener() {

                            @Override
                            public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {

                                if (result.getResultCode() < 0) {

                                    beepManagerError.playBeepSoundAndVibrate();
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate();
                                    addScannedBarcode(scanNo, "checkValidation - PICKUP_SCAN_ALL");
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

                                    beepManagerError.playBeepSoundAndVibrate();
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate();
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

                                    beepManagerError.playBeepSoundAndVibrate();
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate();
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

                                    beepManagerError.playBeepSoundAndVibrate();
                                    deletePrevious(scanNo);
                                    edit_capture_type_number.setText("");
                                } else {

                                    beepManager.playBeepSoundAndVibrate();
                                    addScannedBarcode(scanNo, "checkValidation - OUTLET_PICKUP_SCAN");
                                }
                            }
                        }).build().execute();

                break;
            }
            case BarcodeType.SELF_COLLECTION: {     // 2016-09-20 eylee

                if (!isInvoiceCodeRule(strBarcodeNo, mScanType)) {

                    beepManagerError.playBeepSoundAndVibrate();
                    Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
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
            default: {

                beepManager.playBeepSoundAndVibrate();
                addScannedBarcode(strBarcodeNo, "checkValidation - Default");
            }
        }
    }


    // NOTIFICATION.  Add Barcode List
    private void addScannedBarcode(String barcodeNo, String where) {
        Log.e(TAG, "  addScannedBarcode  > " + where + " // " + barcodeNo);

        mScanCount++;
        text_capture_scan_count.setText(String.valueOf(mScanCount));


        BarcodeListData data = new BarcodeListData();
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
                scanBarcodeNoListAdapter.notifyDataSetChanged();
                list_capture_scan_barcode.setSelection(0);
                list_capture_scan_barcode.smoothScrollToPosition(0);

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
                break;
            default:
                //스캔 시 최근 스캔한 바코드가 아래로 추가됨.
                // maybe.. DELIVERY DONE, SELF COLLECTION
                scanBarcodeArrayList.add(data);
                scanBarcodeNoListAdapter.notifyDataSetChanged();
                break;
        }

        if (!mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) && !mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                && !mScanType.equals(BarcodeType.PICKUP_CNR)) {

            updateInvoiceNO(mScanType, barcodeNo);
        }

        edit_capture_type_number.setText("");
        inputMethodManager.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
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

            if (isInvoiceCodeRule(invoiceNo, mScanType)) {
                updateCount = 1;
            }
        }


        String message = String.format(" [ %s ] ", title);
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

            BarcodeListData data = new BarcodeListData();
            data.setBarcode(inputBarcode);
            data.setState(result);

            scanBarcodeArrayList.set(scanBarcodeArrayList.size() - 1, data);
            scanBarcodeNoListAdapter.notifyDataSetChanged();
        }

        // 교체 후 Adapter.notifyDataSetChanged() 메서드로 listview  변경 add comment by eylee 2016-09-08
        if (updateCount < 1) { // 실패일때만 보여준다.

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {

                vibrator.vibrate(200L);
            }

            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);
            toast.show();
        }
    }

    // invalidation 일 때, History SQLite  data 삭제
    public void deletePrevious(String text) {

        if (!text.equals("")) {
            historyManager.deletePrevious(text);
        }
    }


    // TODO.  하단 버튼 클릭 이벤트
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

            DataUtil.logEvent("button_click", TAG, "SetShippingStatDpc3out");

            new ConfirmMyOrderHelper.Builder(this, opID, officeCode, deviceID, scanBarcodeArrayList)
                    .setOnDriverAssignEventListener(stdResult -> {

                        String msg = "";

                        if (stdResult != null) {
                            if (stdResult.getResultCode() == 0)
                                onResetButtonClick();

                            msg = stdResult.getResultMsg();
                        } else {

                            msg = context.getResources().getString(R.string.text_fail_update);
                        }


                        // BadTokenException 예방
                        if (!CaptureActivity.this.isFinishing()) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                            builder.setTitle(context.getResources().getString(R.string.text_driver_assign_result));
                            builder.setMessage(msg);
                            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog, id) -> dialog.cancel());
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

                        String msg = "";

                        if (stdResult != null) {

                            if (stdResult.getResultCode() == 0)
                                onResetButtonClick();

                            msg = stdResult.getResultMsg();
                        } else {

                            msg = context.getResources().getString(R.string.text_fail_update);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                        builder.setTitle(context.getResources().getString(R.string.text_driver_assign_result));
                        builder.setMessage(msg);
                        builder.setPositiveButton(context.getResources().getString(R.string.button_ok), (dialog, id) -> dialog.cancel());
                        builder.show();
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

        DeliveryInfo info;
        String receiverName = "";
        boolean diffReceiverName = false;
        ArrayList<BarcodeListData> deliveryBarcodeList = new ArrayList<>();

        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            BarcodeListData barcodeListData = scanBarcodeArrayList.get(i);

            if (barcodeListData.getState().equals("SUCCESS")) {

                info = getDeliveryInfo(barcodeListData.getBarcode());

                try {

                    // 수취인성명이 틀린경우
                    if (!receiverName.equals("")) {
                        if (!receiverName.toUpperCase().equals(info.getReceiverName().toUpperCase())) {
                            diffReceiverName = true;
                        }
                    }
                } catch (Exception e) {

                    diffReceiverName = true;
                }

                receiverName = info.getReceiverName();
                deliveryBarcodeList.add(barcodeListData);
            }
        }

        // 받는사람이 틀리다면 에러 메세지
        if (diffReceiverName) {

            Toast toast = Toast.makeText(this, R.string.msg_different_recipient_address, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }


        if (0 < deliveryBarcodeList.size()) {

            Intent intent = new Intent(this, DeliveryDoneActivity.class);
            intent.putExtra("data", deliveryBarcodeList);
            this.startActivityForResult(intent, REQUEST_DELIVERY_DONE);
        } else {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
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


        String scannedQty = String.valueOf(scanBarcodeArrayList.size());
        String scannedList = "";

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
                break;
            }
            case BarcodeType.PICKUP_SCAN_ALL: {

                Intent intent = new Intent(this, PickupDoneActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_start_to_scan));
                intent.putExtra("pickup_no", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList);
                intent.putExtra("scannedQty", scannedQty);
                startActivity(intent);
                finish();
                break;
            }
            case BarcodeType.PICKUP_ADD_SCAN: {

                Intent intent = new Intent(this, PickupAddScanActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.text_title_add_pickup));
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList);
                intent.putExtra("scannedQty", scannedQty);
                this.startActivityForResult(intent, REQUEST_PICKUP_ADD_SCAN);
                break;
            }
            case BarcodeType.PICKUP_TAKE_BACK: {

                Intent intent = new Intent(this, PickupTakeBackActivity.class);
                intent.putExtra("title", context.getResources().getString(R.string.button_take_back));
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("scannedList", scannedList);
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

                String scanned_list = "";
                for (int i = 0; i < resultData.getResultObject().getTrackingNoList().size(); i++) {

                    if (resultData.getResultObject().getTrackingNoList().get(i).isScanned()) {

                        if (!scanned_list.equals("")) {
                            scanned_list += ",";
                        }

                        scanned_list += resultData.getResultObject().getTrackingNoList().get(i).getTrackingNo();
                    }
                }
                Log.e(TAG, "Outlet Pickup Scanned List : " + scanned_list);

                Intent intent = new Intent(this, OutletPickupDoneActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("pickupNo", pickupNo);
                intent.putExtra("applicant", pickupApplicantName);
                intent.putExtra("qty", mQty);
                intent.putExtra("route", mRoute);
                intent.putExtra("scannedQty", scanned_qty);
                intent.putExtra("tracking_data", resultData);
                intent.putExtra("scannedList", scanned_list);
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
            scanBarcodeNoListAdapter.notifyDataSetChanged();
            historyManager.clearHistory();


            if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                ArrayList<OutletPickupDoneResult.OutletPickupDoneItem.OutletPickupDoneTrackingNoItem> listItem = resultData.getResultObject().getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeListData data = new BarcodeListData();
                    data.setBarcode(listItem.get(i).getTrackingNo());
                    data.setState("FAIL");
                    scanBarcodeArrayList.add(i, data);
                }
                scanBarcodeNoListAdapter.notifyDataSetChanged();
            }
        }

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            removeBarcodeListInstance();
        }
    }


    public DeliveryInfo getDeliveryInfo(String barcodeNo) {

        DeliveryInfo info = new DeliveryInfo();
        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {

            info.setReceiverName(cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm")));
            info.setSenderName(cursor.getString(cursor.getColumnIndexOrThrow("sender_nm")));
        }

        cursor.close();

        return info;
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
        inactivityTimer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        inactivityTimer.shutdown();
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

        if (!isInvoiceCodeRule(scanBarcodeArrayList.get(0).getBarcode(), mScanType)) {

            Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        // SELF_COLLECTION            //복수건 가져다가 self-collection by 2016-09-09
        // 넘기는 데이터 재정의 스캔성공된 것들만 보낸다.
        ArrayList<BarcodeListData> newBarcodeNoList = new ArrayList<>();
        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {

            BarcodeListData barcodeListData = scanBarcodeArrayList.get(i);

            if (barcodeListData.state.equals("FAIL")) {

                Toast toast = Toast.makeText(this, R.string.msg_invalid_scan, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            } else {

                newBarcodeNoList.add(barcodeListData);
            }
        }


        if (0 < newBarcodeNoList.size()) {

            Intent intentSign = new Intent(this, SelfCollectionDoneActivity.class);
            intentSign.putExtra("title", title);
            intentSign.putExtra("data", newBarcodeNoList);
            intentSign.putExtra("nonq10qfs", String.valueOf(isNonQ10QFSOrder));    //09-12 add isNonQ10QFSOrder
            this.startActivityForResult(intentSign, REQUEST_SELF_COLLECTION);
        } else {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    /*
     * Qxpress송장번호 규칙(범용)
     * 운송장번호 규칙이 맞는지 체크
     * 10문자 안넘으면 false, 맨앞두글자가 KR,SG,QX,JP,CN이 아닐경우 false, 5,6번째가 숫자가 아닐경우 false, 영문숫자조합
      SELF_COLLECTION */
    public static boolean isInvoiceCodeRule(String invoiceNo, String mType) {

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

    public static boolean isStringDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}