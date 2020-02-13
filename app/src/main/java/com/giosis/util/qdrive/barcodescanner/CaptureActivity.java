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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
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

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.barcodescanner.bluetooth.BluetoothChatService;
import com.giosis.util.qdrive.barcodescanner.bluetooth.DeviceListActivity;
import com.giosis.util.qdrive.barcodescanner.bluetooth.KScan;
import com.giosis.util.qdrive.barcodescanner.bluetooth.KTSyncData;
import com.giosis.util.qdrive.barcodescanner.camera.CameraManager;
import com.giosis.util.qdrive.barcodescanner.history.HistoryManager;
import com.giosis.util.qdrive.barcodescanner.result.ResultHandler;
import com.giosis.util.qdrive.barcodescanner.result.ResultHandlerFactory;
import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.list.delivery.SigningDeliveryDoneActivity;
import com.giosis.util.qdrive.list.pickup.ManualPickupAddScanNoOneByOneUploadHelper;
import com.giosis.util.qdrive.list.pickup.ManualPickupTakeBackValidationCheckHelper;
import com.giosis.util.qdrive.list.pickup.OutletPickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.OutletPickupDoneResult;
import com.giosis.util.qdrive.list.pickup.OutletPickupScanValidationCheckHelper;
import com.giosis.util.qdrive.list.pickup.PickupAddScanActivity;
import com.giosis.util.qdrive.list.pickup.CnRPickupDoneActivity;
import com.giosis.util.qdrive.list.pickup.SigningPickupScanAllDoneActivity;
import com.giosis.util.qdrive.list.pickup.SigningPickupTakeBackActivity;
import com.giosis.util.qdrive.main.ManualChangeDelDriverHelper;
import com.giosis.util.qdrive.main.ManualChangeDelDriverHelper.OnChangeDelDriverEventListener;
import com.giosis.util.qdrive.main.ManualChangeDelDriverValidCheckHelper;
import com.giosis.util.qdrive.main.ManualDpc3OutValidationCheckHelper;
import com.giosis.util.qdrive.main.ManualDriverAssignHelper;
import com.giosis.util.qdrive.main.ManualPodUploadHelper;
import com.giosis.util.qdrive.main.SigningActivity;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.UploadData;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Pattern;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

/**
 * The barcode reader activity itself. This is loosely based on the
 * CameraPreview example included in the Android SDK.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */

public final class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback, OnTouchListener,
        OnFocusChangeListener, TextWatcher, SensorEventListener, OnKeyListener {
    private static final String TAG = "CaptureActivity";

    private static final long INTENT_RESULT_DURATION = 1L;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CAMERA_REQUEST = 3;
    private static final int REQUEST_SIGN_REQUEST = 4;
    //add Intent request codes for Pickup C&R
    private static final int REQUEST_PICKUP_CNR_REQUEST = 5;
    //2016-09-09 eylee
    private static final int REQUEST_SELF_COLLECTION = 7;
    //2016-09-26 eylee
    public static final int REQUEST_PICKUP_SCAN_ALL = 9;
    //2017-03-16
    private static final int REQUEST_PICKUP_ADD_SCAN = 12;
    // krm0219
    private static final int REQUEST_OUTLET_PICKUP_SCAN = 13;
    private static final int REQUEST_PICKUP_TAKE_BACK = 14;


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DISPLAY = 6;
    public static final int MESSAGE_SEND = 7;
    public static final int MESSAGE_SETTING = 255;
    public static final int MESSAGE_EXIT = 0;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final String PACKAGE_NAME = "com.giosis.util.qdrive.barcodescanner";
    private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
    private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
    private static final String ZXING_URL = "http://zxing.appspot.com/scan";
    private static final String RETURN_CODE_PLACEHOLDER = "{CODE}";
    private static final String RETURN_URL_PARAM = "ret";
    private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES;


    // krm0219
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

    private ArrayList<BarcodeListData> scanBarcodeArrayList = null;
    private InputBarcodeNoListAdapter inputBarcodeNoListAdapter = null;

    //
    String opID = "";
    String opName = "";
    String officeCode = "";
    String deviceID = "";
    //krm0219  outlet
    String outletDriverYN = "";
    //2016-09-03 pickup cnr Requestor
    String pickupCNRRequestor = "";
    //2016-09-21 picku cnr isValidationPickupCnr
    boolean isValidationPickupCnr = false;

    private String mScanType;
    private String mScanTitle;

    //2016-09-26 eylee pickup scan all
    String pickupNo = "";
    String pickupApplicantName = "";

    Button mManualScanPodUpload;


    public static final int BELL_SOUNDS = 1; //띵동
    public static final int BELL_SOUNDS_ERROR = 2; //삐~
    public static final int BELL_SOUNDS_DUPLE = 3; // 삐비~


    SurfaceHolder surfaceHolder;
    private boolean hasSurface;

    boolean mIsScanDeviceListActivityRun = false;

    static {
        DISPLAYABLE_METADATA_TYPES = new HashSet<>(5);
        DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ISSUE_NUMBER);
        DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.SUGGESTED_PRICE);
        DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ERROR_CORRECTION_LEVEL);
        DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.POSSIBLE_COUNTRY);
    }

    private enum Source {
        NATIVE_APP_INTENT, PRODUCT_SEARCH_LINK, ZXING_LINK, NONE
    }

    private CaptureActivityHandler handler;
    private Result lastResult;
    private boolean copyToClipboard;
    private Source source;
    private String sourceUrl;
    private String returnUrlTemplate;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private HistoryManager historyManager;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private BeepManager beepManager2;
    private BeepManager beepManager3;

    Drawable editTextDelButtonDrawable;
    EditText currentFocusEditText;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    // private BluetoothChatService mChatService = null;

    public BluetoothDevice connectedDevice = null;
    // Name of the connected device
    private String connectedDeviceName = null;

    // public static KScan mKScan = null;

    private byte[] displayBuf = new byte[256];
    private String displayMessage;

    public boolean isNonQ10QFSOrder = false;

    private boolean passedValidation = false;

    private ChgDelDriverResult.ResultObject chgDelDriverResultObj = null;

    InputMethodManager inputMethodManager = null;

    // resume 시 recreate 할 data list
    private static ArrayList<String> barcodeList = null;
    // resume 시 recreate 할 data list -> Change Delivery Driver 용도
    private static Hashtable<String, String> barcodeContrNoList = null;


    public static void getBarcodeListInstance() {

        if (barcodeList == null) {
            barcodeList = new ArrayList<>();
        }

        if (barcodeContrNoList == null) {
            barcodeContrNoList = new Hashtable<>();
        }
    }

    public static void removeBarcodeListInstance() {
        if (barcodeList != null) {
            barcodeList.clear();
        }

        if (barcodeContrNoList != null) {
            barcodeContrNoList.clear();
        }
    }

    ViewfinderView getViewfinderView() {
        return viewfinder_capture_preview;
    }

    public Handler getHandler() {
        return handler;
    }


    Context context;
    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;


    String mTitle;
    String mPickupNo;
    String mApplicant;
    String mQty;
    String mRoute;
    OutletPickupDoneResult resultData;


    private PermissionChecker checker;
    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA};
    // -------------------------------------------


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

        //
        mManualScanPodUpload = findViewById(R.id.barcode_pod_upload); //Pod 업로드 버튼


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


        //------------------
        context = getApplicationContext();
        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        opName = SharedPreferencesHelper.getSigninOpName(getApplicationContext());
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());

        try {
            outletDriverYN = SharedPreferencesHelper.getPrefSignInOutletDriver(getApplicationContext());
        } catch (Exception e) {
            outletDriverYN = "N";
        }

        Log.e("krm0219", TAG + "  outletDriverYN : " + outletDriverYN);

        // 시스템서비스로부터 SensorManager 객체를 얻는다.
        m_sensor_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // SensorManager 를 이용해서 조도 센서 객체를 얻는다.
        m_light_sensor = m_sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT);

        CameraManager.init(getApplication());
        surfaceHolder = surface_capture_preview.getHolder();
        surfaceHolder.addCallback(this);
        hasSurface = false;

        handler = null;
        lastResult = null;

        historyManager = new HistoryManager(this);
        historyManager.clearHistory();

        inactivityTimer = new InactivityTimer(this);

        beepManager = new BeepManager(this, BELL_SOUNDS);
        beepManager2 = new BeepManager(this, BELL_SOUNDS_ERROR);
        beepManager3 = new BeepManager(this, BELL_SOUNDS_DUPLE);

        editTextDelButtonDrawable = getResources().getDrawable(R.drawable.btn_delete);
        editTextDelButtonDrawable.setBounds(0, 0, editTextDelButtonDrawable.getIntrinsicWidth(), editTextDelButtonDrawable.getIntrinsicHeight());

        String strTitle = getIntent().getStringExtra("title");
        String strType = getIntent().getStringExtra("type");
        if (strTitle == null) {
            strTitle = context.getResources().getString(R.string.text_scanning);
        }
        mScanTitle = strTitle;
        text_top_title.setText(mScanTitle);

        if (strType == null) {
            strType = "N";
        }
        mScanType = strType;


        // eylee 2015.10.06
        if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE) || mScanType.equals(BarcodeType.SELF_COLLECTION)) {

            text_top_title.setText(context.getResources().getString(R.string.text_title_scan_barcode));
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next_step2));            // onCaptureConfirmButtonClick

            String podCount = getScanDeliveryCount();
            text_capture_scan_count.setText(podCount);

            if (Integer.parseInt(podCount) > 0) {

                mManualScanPodUpload.setVisibility(View.VISIBLE);
            } else {

                mManualScanPodUpload.setVisibility(View.GONE);
            }
        }

        //krm0219
        if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            pickupNo = getIntent().getStringExtra("pickup_no");
            pickupApplicantName = getIntent().getStringExtra("applicant");
            mTitle = getIntent().getStringExtra("title");
            mPickupNo = getIntent().getStringExtra("pickup_no");
            mApplicant = getIntent().getStringExtra("applicant");
            mQty = getIntent().getStringExtra("qty");
            mRoute = getIntent().getStringExtra("route");
            resultData = (OutletPickupDoneResult) getIntent().getSerializableExtra("tracking_data");

            if (mRoute.equals("FL")) {
                text_top_title.setText(R.string.text_title_fl_pickup);
            }
        } else if (mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)) {       //eylee 2016-09-26

            pickupNo = getIntent().getStringExtra("pickup_no");
            pickupApplicantName = getIntent().getStringExtra("applicant");

        } else if (mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)) {

            pickupNo = getIntent().getStringExtra("pickup_no");
            pickupApplicantName = getIntent().getStringExtra("applicant");
        } else if (mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            pickupNo = getIntent().getStringExtra("pickup_no");
            pickupApplicantName = getIntent().getStringExtra("applicant");
            mQty = getIntent().getStringExtra("scanned_qty");
        }


        // krm0219
        scanBarcodeArrayList = new ArrayList<>();

        if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();

            for (int i = 0; i < listItem.size(); i++) {

                BarcodeListData data = new BarcodeListData();
                data.setState("FAIL");
                data.setBarcode(listItem.get(i).getTrackingNo());

                scanBarcodeArrayList.add(i, data);
            }
        }

        inputBarcodeNoListAdapter = new InputBarcodeNoListAdapter(this, scanBarcodeArrayList, mScanType);
        list_capture_scan_barcode.setAdapter(inputBarcodeNoListAdapter);

        if (scanBarcodeArrayList.size() > 0) {

            list_capture_scan_barcode.setSelection(scanBarcodeArrayList.size() - 1);
        }

        toggle_btn_capture_camera_flash.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (handler != null) {
                        handler.quitSynchronously();
                    }
                    CameraManager.get().onFlash();
                    resetStatusView();
                    if (handler != null) {
                        handler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats, characterSet);
                    }
                } else {

                    if (handler != null) {
                        handler.quitSynchronously();
                    }

                    CameraManager.get().offFlash();
                    resetStatusView();
                    if (handler != null) {
                        handler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats, characterSet);
                    }
                }
            }
        });

        // 현재 바코드 스캔의 타입 설정 코드로
        mScanStatusIndex = 4;

        // 블루투스
        initBluetoothDevice();
        initManualScanViews(mScanType);

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            getBarcodeListInstance();
        }


        //
        checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
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
        mManualScanPodUpload.setVisibility(View.GONE);

        if (scanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER)) {

            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_update));      //onUpdateButtonClick

        } else if (scanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_done));         //onUpdateButtonClick

        } else if (scanType.equals(BarcodeType.DELIVERY_DONE)) {

            layout_capture_scan_count.setVisibility(View.GONE);
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         //onConfirmButtonClick

        } else if (scanType.equals(BarcodeType.PICKUP_CNR)) {
            // add added pickup C&R by 2016-08-30 eylee

            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            //onNextButtonClick

        } else if (scanType.equals(BarcodeType.SELF_COLLECTION)) {

            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick

        } else if (scanType.equals(BarcodeType.TYPE_SCAN_CAPTURE)) {
            // Scan capture Delivered
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_confirm));         // onCaptureConfirmButtonClick
            mManualScanPodUpload.setVisibility(View.VISIBLE);

        } else if (scanType.equals(BarcodeType.PICKUP_SCAN_ALL)) {
            // 2016-09-26 pickup scan all
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            //onNextButtonClick

        } else if (scanType.equals(BarcodeType.PICKUP_ADD_SCAN)) {
            // 2017-03-15 pickup  add scan list

            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            //onNextButtonClick

        } else if (scanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {
            //krm0219
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            //onNextButtonClick

        } else if (scanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {
            // 2019.02 krm0219
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next));            // onNextButtonClick
        }
    }

    private void initBluetoothDevice() {
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, context.getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        KTSyncData.mKScan = new KScan(this, mHandler);

        KTSyncData.BufferRead = 0;
        KTSyncData.BufferWrite = 0;

        for (int i = 0; i < 10; i++) {
            KTSyncData.SerialNumber[i] = '0';
            KTSyncData.FWVersion[i] = '0';
        }

        GetPreferences();

        KTSyncData.bIsOver_222 = false;

        StringBuffer buf = new StringBuffer();
        buf.append(Build.VERSION.RELEASE);

        String version = buf.toString();
        String target = "2.2.2";
        if (version.compareTo(target) > 0) {
            KTSyncData.bIsOver_222 = true;
        }
    }

    public void GetPreferences() {

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

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.layout_top_back: {

                    // 닫기 버튼으로 나갈  때 barcodeList 제거하기
                    if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                            || mScanType.equals(BarcodeType.PICKUP_CNR)
                            || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                            || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                        removeBarcodeListInstance();
                    }

                    finish();
                }
                break;

                case R.id.layout_capture_camera: {

                    layout_capture_camera.setBackgroundResource(R.drawable.custom_tab_selected);
                    layout_capture_scanner.setBackgroundResource(R.drawable.custom_tab_unselected);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.custom_tab_unselected);

                    text_capture_camera.setTextColor(getResources().getColor(R.color.color_ff0000));
                    text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.BOLD);
                    text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);
                    text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.NORMAL);

                    viewfinder_capture_preview.setVisibility(View.VISIBLE);
                    layout_capture_scanner_mode.setVisibility(View.GONE);
                    layout_capture_bluetooth_mode.setVisibility(View.GONE);

                    if (KTSyncData.mChatService != null)
                        KTSyncData.mChatService.stop();
                    KTSyncData.bIsRunning = false;

                    onResume();
                }
                break;

                case R.id.layout_capture_scanner: {

                    layout_capture_camera.setBackgroundResource(R.drawable.custom_tab_unselected);
                    layout_capture_scanner.setBackgroundResource(R.drawable.custom_tab_selected);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.custom_tab_unselected);

                    text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                    text_capture_scanner.setTextColor(getResources().getColor(R.color.color_ff0000));
                    text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.BOLD);
                    text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.NORMAL);

                    viewfinder_capture_preview.setVisibility(View.GONE);
                    layout_capture_scanner_mode.setVisibility(View.VISIBLE);
                    layout_capture_bluetooth_mode.setVisibility(View.GONE);


                    if (handler != null) {
                        handler.quitSynchronously();
                        handler = null;
                    }

                    CameraManager.get().closeDriver();

                    if (KTSyncData.mChatService != null)
                        KTSyncData.mChatService.stop();
                    KTSyncData.bIsRunning = false;
                }
                break;

                case R.id.layout_capture_bluetooth: {

                    layout_capture_camera.setBackgroundResource(R.drawable.custom_tab_unselected);
                    layout_capture_scanner.setBackgroundResource(R.drawable.custom_tab_unselected);
                    layout_capture_bluetooth.setBackgroundResource(R.drawable.custom_tab_selected);

                    text_capture_camera.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_camera.setTypeface(text_capture_camera.getTypeface(), Typeface.NORMAL);
                    text_capture_scanner.setTextColor(getResources().getColor(R.color.color_303030));
                    text_capture_scanner.setTypeface(text_capture_scanner.getTypeface(), Typeface.NORMAL);
                    text_capture_bluetooth.setTextColor(getResources().getColor(R.color.color_ff0000));
                    text_capture_bluetooth.setTypeface(text_capture_bluetooth.getTypeface(), Typeface.BOLD);

                    // If BT is not on, request that it be enabled.
                    // setupChat() will then be called during onActivityResult
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    }

                    viewfinder_capture_preview.setVisibility(View.GONE);
                    layout_capture_scanner_mode.setVisibility(View.GONE);
                    layout_capture_bluetooth_mode.setVisibility(View.VISIBLE);

                    if (handler != null) {
                        handler.quitSynchronously();
                        handler = null;
                    }

                    CameraManager.get().closeDriver();
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

                    onBarcodeAddButtonClick();
                }
                break;

                case R.id.btn_capture_barcode_reset: {

                    onResetButtonClick();
                }
                break;

                case R.id.btn_capture_barcode_confirm: {

                    if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                        onUpdateButtonClick(null);
                    } else if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE) || mScanType.equals(BarcodeType.SELF_COLLECTION)) {

                        onCaptureConfirmButtonClick(null);
                    } else if (mScanType.equals(BarcodeType.PICKUP_CNR)
                            || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                            || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                        onNextButtonClick(null);
                    } else if (mScanType.equals(BarcodeType.DELIVERY_DONE)) {

                        onConfirmButtonClick();
                    }
                }
                break;
            }
        }
    };

    public void onBarcodeAddButtonClick() {

        // edit_capture_type_number 유효성 체크 해야함
        String inputBarcodeNumber = edit_capture_type_number.getText().toString().trim().toUpperCase();

        if (inputBarcodeNumber.length() > 0) {
            // 2017-04-13 eylee comment
            // historyManager 객체에서 history 테이블이란 내부 저장소에서 addHistoryItem method 로
            // scan no 가 저장되 있으면 지우고 다시 insert 하면서 isDuplicate 가 true 값으로 저장 됨
            boolean isDuplicate = historyManager.addHistoryItem(new Result(inputBarcodeNumber, null, null, null), null);

            // Scan Sheet의 경우 송장번호스캔은 1건만 가능하다.
            if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE)) {       // POD SCAN
                mScanCount = 0;
                isDuplicate = false;

                //bluetoothsounds 바코드 인식 bell 소리 변경 20150709 eylee add// 20150709 sound by eylee
                beepManager.playBeepSoundAndVibrate(); // 소리추가
            } else if (mScanTitle.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanTitle.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                if (!isDuplicate) {
                    isDuplicate = false;
                    beepManager.playBeepSoundAndVibrate(); // 소리추가
                }
            } else if (mScanType.equals(BarcodeType.SELF_COLLECTION)) {

                // 2016-09-12
//				★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
//				현재 nonq10 QFS order ####NQ 의 모든 order 가 self collection 이
//				되는데, 나중에 service 에서 delivery route 가 other 일 때만,
//				self collection 되도록 막는 logic 필요함
//				★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
                //bluetoothsounds 바코드 인식 bell 소리 변경 20150709 eylee add// 20150709 sound by eylee
                beepManager.playBeepSoundAndVibrate(); // 소리추가
            }

            addBarcodeNo(inputBarcodeNumber, isDuplicate, "onBarcodeAddButtonClick");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            // Intent enableIntent = new
            // Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (KTSyncData.mChatService == null)
                setupChat();
        }
    }

    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        KTSyncData.mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (KTSyncData.AutoConnect && KTSyncData.bIsRunning)
                KTSyncData.mChatService.connect(connectedDevice);
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
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
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer
                    // String readMessage = new String(readBuf, 0, msg.arg1);
                    // mConversationArrayAdapter.add(connectedDeviceName+":  " +
                    // readMessage);

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
                    // byte[]
                    displayBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    // String
                    displayMessage = new String(displayBuf, 0, msg.arg1);
                    // dispatchBarcode(displayBuf, msg.arg1);
                    onBluetoothBarcodeAdd(displayMessage);
                    KTSyncData.bIsSyncFinished = true;
                    break;
                case MESSAGE_SEND:
                    // mConversationArrayAdapter.add(new String("1"));
                    byte[] sendBuf = (byte[]) msg.obj;

                    KTSyncData.mChatService.write(sendBuf);
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult " + requestCode + " / " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    connectedDevice = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    KTSyncData.mChatService.connect(connectedDevice);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.msg_bluetooth_enabled, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getExtras().getString("data").equals("2")) {
                        finish();
                    }
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.msg_bluetooth_enabled, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_SIGN_REQUEST: {

                if (resultCode == Activity.RESULT_OK) {

                    finish();
                }
            }
            break;
            //add Pickup C&R done
            case REQUEST_PICKUP_CNR_REQUEST: {

                onResetButtonClick();

                if (resultCode == Activity.RESULT_OK) {

                    finish();
                }
            }
            break;
            case REQUEST_SELF_COLLECTION: {

                onResetButtonClick();
                break;
            }

            case REQUEST_PICKUP_SCAN_ALL: {

                onResetButtonClick();
                text_capture_scan_count.setText("0");
                finish();
                break;
            }
            case REQUEST_PICKUP_ADD_SCAN: {

                onResetButtonClick();
                if (resultCode == Activity.RESULT_OK) {

                    Intent i = getIntent(); //gets the intent that called this intent
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
                break;
            }

            case REQUEST_OUTLET_PICKUP_SCAN: {

                Log.e("krm0219", "REQUEST_OUTLET_PICKUP_SCAN");
                onResetButtonClick();

                if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                    text_capture_scan_count.setText("0");
                    finish();
                }
            }
            break;

            case REQUEST_PICKUP_TAKE_BACK: {

                Log.e("krm0219", "REQUEST_PICKUP_TAKE_BACK");
                onResetButtonClick();
                text_capture_scan_count.setText("0");

                if (resultCode == Activity.RESULT_OK) {

                    Intent i = getIntent(); //gets the intent that called this intent
                    setResult(Activity.RESULT_OK, i);
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

    @Override
    public void onStop() {
        super.onStop();

        KTSyncData.bIsBackground = true;
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

    @Override
    public synchronized void onResume() {
        super.onResume();

        try {

            edit_capture_type_number.requestFocus();
        } catch (Exception e) {

            Log.e("Exception", TAG + "  requestFocus Exception : " + e.toString());
            e.printStackTrace();
        }


        // Scan Sheet Delivery 경우 데이터초기화 CameraActivity에서 돌아오는경우 아답터초기화
        if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE) || mScanType.equals(BarcodeType.SELF_COLLECTION)) {
            scanBarcodeArrayList.clear();
            inputBarcodeNoListAdapter.notifyDataSetChanged();
            btn_capture_barcode_confirm.setText(context.getResources().getString(R.string.button_next_step2));

            String podCount = getScanDeliveryCount();
            text_capture_scan_count.setText(podCount);

            if (Integer.parseInt(podCount) > 0) {
                mManualScanPodUpload.setVisibility(View.VISIBLE);
            } else {
                mManualScanPodUpload.setVisibility(View.GONE);
            }
        }

        if (KTSyncData.bIsRunning)
            return;

        resetStatusView();

        m_sensor_manager.registerListener(this, m_light_sensor, SensorManager.SENSOR_DELAY_UI);

        if (isPermissionTrue) {
            if (hasSurface) {
                // The activity was paused but not stopped, so the surface still
                // exists. Therefore
                // surfaceCreated() won't be called, so init the camera here.
                initCamera(surfaceHolder);
            } else {
                // Install the callback and wait for surfaceCreated() to init the
                // camera.
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();
        String dataString = intent == null ? null : intent.getDataString();
        if (intent != null && action != null) {
            if (action.equals(Intents.Scan.ACTION)) {
                // Scan the formats the intent requested, and return the result
                // to the calling activity.
                source = Source.NATIVE_APP_INTENT;
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        CameraManager.get().setManualFramingRect(width, height);
                    }
                }

                // pplante - intents can now enable flash if they want..
                try {
                    if (intent.hasExtra(Intents.Scan.FLASH_MODE)) {
                        CameraManager.get().enableFlash(intent.getStringExtra(Intents.Scan.FLASH_MODE));
                    }
                } catch (IOException e) {
                    Log.i("flash", e.toString());
                }
                Log.d("flash", "enabled = " + intent.getStringExtra(Intents.Scan.FLASH_MODE));

            } else if (dataString != null && dataString.contains(PRODUCT_SEARCH_URL_PREFIX) && dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {
                // Scan only products and send the result to mobile Product
                // Search.
                source = Source.PRODUCT_SEARCH_LINK;
                sourceUrl = dataString;
                decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;
            } else if (dataString != null && dataString.startsWith(ZXING_URL)) {
                // Scan formats requested in query string (all formats if none
                // specified).
                // If a return URL is specified, send the results there.
                // Otherwise, handle it ourselves.
                source = Source.ZXING_LINK;
                sourceUrl = dataString;
                Uri inputUri = Uri.parse(sourceUrl);
                returnUrlTemplate = inputUri.getQueryParameter(RETURN_URL_PARAM);
                decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
            } else {
                // Scan all formats and handle the results ourselves (launched
                // from Home).
                source = Source.NONE;
                decodeFormats = null;
            }
            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
        } else {
            source = Source.NONE;
            decodeFormats = null;
            characterSet = null;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        copyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, false) && (intent == null || intent.getBooleanExtra(Intents.Scan.SAVE_HISTORY, true));

        beepManager.updatePrefs();

        inactivityTimer.onResume();

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

        KTSyncData.bIsBackground = false;

        if (KTSyncData.bIsConnected && KTSyncData.LockUnlock) {
            // Toast.makeText(this, "KTDemo Main Screen",
            // Toast.LENGTH_LONG).show();
            KTSyncData.mKScan.LockUnlockScanButton(true);
        }

        KTSyncData.mKScan.mHandler = mHandler;

        if (isPermissionTrue) {
            if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

                if (gpsTrackerManager != null) {

                    DataUtil.stopGPSManager(gpsTrackerManager);
                }

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

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            try {
                scanBarcodeArrayList.clear();
                inputBarcodeNoListAdapter.notifyDataSetChanged();

                // 다시 그리기
                if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.PICKUP_CNR)
                        || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                        || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                    if (barcodeList != null && barcodeList.size() > 0) {
                        for (int bl_size = 0; bl_size < barcodeList.size(); bl_size++) {

                            BarcodeListData data = new BarcodeListData();
                            data.setState("SUCCESS");
                            data.setBarcode(barcodeList.get(bl_size));
                            scanBarcodeArrayList.add(0, data);

                            //history 에 저장하기 -- activity 가 새로 시작 되더라고 중복 번호 스캔 못 되게
                            historyManager.addHistoryItem(new Result(barcodeList.get(bl_size), null, null, null), null);
                        }
                        mScanCount = scanBarcodeArrayList.size();

                        Log.e("krm0219", "mScan Count : " + mScanCount);
                        inputBarcodeNoListAdapter.notifyDataSetChanged();

                        list_capture_scan_barcode.setSelection(0);
                        list_capture_scan_barcode.smoothScrollToPosition(0);

                        // 스캔 카운트 보여주기
                        text_capture_scan_count.setText(String.valueOf(mScanCount));
                    }
                } else if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                    mScanCount = 0;

                    if (resultData != null) {

                        ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();

                        for (int i = 0; i < listItem.size(); i++) {

                            String tracking_no = listItem.get(i).getTrackingNo();
                            boolean isScanned = listItem.get(i).isScanned();

                            BarcodeListData data = new BarcodeListData();
                            data.setBarcode(tracking_no);

                            if (isScanned) {

                                data.setState("SUCCESS");
                                mScanCount++;
                                historyManager.addHistoryItem(new Result(tracking_no, null, null, null), null);
                            } else {

                                data.setState("FAIL");
                            }

                            scanBarcodeArrayList.add(i, data);
                        }
                    }

                    Log.e("krm0219", "mScan Count : " + mScanCount);
                    inputBarcodeNoListAdapter.notifyDataSetChanged();
                    list_capture_scan_barcode.setSelection(0);
                    list_capture_scan_barcode.smoothScrollToPosition(0);
                    text_capture_scan_count.setText(String.valueOf(mScanCount));
                } else {  //CHANGE_DELIVERY_DRIVER to do 04-05
                    if (barcodeContrNoList != null && barcodeContrNoList.size() > 0) {
//						Log.i(TAG, "CaptureActivity Resume - barcodeContrNoList.size() -" + String.valueOf( barcodeContrNoList.size()));
                        for (int bl_size = 0; bl_size < barcodeList.size(); bl_size++) {

                            BarcodeListData data = new BarcodeListData();
                            data.setState("SUCCESS");
                            data.setBarcode(barcodeList.get(bl_size));
                            scanBarcodeArrayList.add(0, data);

                            //history 에 저장하기 -- activity 가 새로 시작 되더라고 중복 번호 스캔 못 되게
                            historyManager.addHistoryItem(new Result(barcodeList.get(bl_size), null, null, null), null);
                        }
                        mScanCount = scanBarcodeArrayList.size();
                        inputBarcodeNoListAdapter.notifyDataSetChanged();

                        list_capture_scan_barcode.setSelection(0);
                        list_capture_scan_barcode.smoothScrollToPosition(0);

                        // 스캔 카운트 보여주기
                        text_capture_scan_count.setText(String.valueOf(mScanCount));

                    } // end of if(barcodeContrNoList != null && barcodeContrNoL

                }
                //다시 그리기 끝
            } catch (Exception e) {

            }
        }
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

        inactivityTimer.onPause();
        CameraManager.get().closeDriver();

        // 센서 값이 필요하지 않는 시점에 리스너를 해제해준다.
        m_sensor_manager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        historyManager.clearHistory();
        inactivityTimer.shutdown();
        super.onDestroy();

        DataUtil.stopGPSManager(gpsTrackerManager);

        // Stop the Bluetooth chat services
        if (KTSyncData.mChatService != null)
            KTSyncData.mChatService.stop();
        KTSyncData.mChatService = null;
        KTSyncData.bIsRunning = false;

        if (beepManager != null) {
            beepManager.destroy();
        }

        if (beepManager2 != null) {
            beepManager2.destroy();
        }

        if (beepManager3 != null) {
            beepManager3.destroy();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (source == Source.NATIVE_APP_INTENT) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            } else if ((source == Source.NONE || source == Source.ZXING_LINK) && lastResult != null) {
                resetStatusView();
                if (handler != null) {
                    handler.sendEmptyMessage(R.id.restart_preview);
                }
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // Handle these events so they don't launch the Camera app
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode) {

        inactivityTimer.onActivity();
        lastResult = rawResult;
        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
        boolean isHistorySave = historyManager.addHistoryItem(rawResult, resultHandler);


        if (barcode == null) {
            // This is from history -- no saved barcode
            handleDecodeInternally(rawResult, resultHandler, null);
        } else {
            if (isHistorySave) {

                resetStatusView();
                if (handler != null) {
                    handler.sendEmptyMessage(R.id.restart_preview);
                }
                return;
            }

            beepManager.playBeepSoundAndVibrate();
            drawResultPoints(barcode, rawResult);
            switch (source) {
                case NATIVE_APP_INTENT:
                case PRODUCT_SEARCH_LINK:
                    handleDecodeExternally(rawResult, resultHandler, barcode);
                    break;
                case ZXING_LINK:
                    if (returnUrlTemplate == null) {
                        handleDecodeInternally(rawResult, resultHandler, barcode);
                    } else {
                        handleDecodeExternally(rawResult, resultHandler, barcode);
                    }
                    break;
                case NONE:
                    handleDecodeInternally(rawResult, resultHandler, barcode);
                    break;
            }
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of
     * the barcode.
     *
     * @param barcode   A bitmap of the captured image.
     * @param rawResult The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, Result rawResult) {

        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {

            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_image_border));
            paint.setStrokeWidth(3.0f);
            paint.setStyle(Paint.Style.STROKE);
            Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
            canvas.drawRect(border, paint);

            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {

                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1]);
            } else if (points.length == 4
                    && (rawResult.getBarcodeFormat()
                    .equals(BarcodeFormat.UPC_A) || rawResult
                    .getBarcodeFormat().equals(BarcodeFormat.EAN_13))) {

                // Hacky special case -- draw two lines, for the barcode and
                // metadata
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            } else {

                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
        canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
    }

    // Put up our own UI for how to handle the decoded contents.
    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        CharSequence displayContents = resultHandler.getDisplayContents();
        // add barcode No validation 하는 부분 2016-08-23
        addBarcodeNo(displayContents.toString(), false, "handleDecodeInternally");

        resetStatusView();
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

    // Briefly show the contents of the barcode, then handle the result outside
    // Barcode Scanner.
    private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        if (source != Source.NATIVE_APP_INTENT) {
            viewfinder_capture_preview.drawResultBitmap(barcode);

        }

        if (copyToClipboard && !resultHandler.areContentsSecure()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(resultHandler.getDisplayContents());
        }

        if (source == Source.NATIVE_APP_INTENT) {
            // Hand back whatever action they requested - this can be changed to
            // Intents.Scan.ACTION when
            // the deprecated intent is retired.
            Intent intent = new Intent(getIntent().getAction());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
            intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
            byte[] rawBytes = rawResult.getRawBytes();
            if (rawBytes != null && rawBytes.length > 0) {
                intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
            }
            Message message = Message.obtain(handler, R.id.return_scan_result);
            message.obj = intent;
            handler.sendMessage(message);
        } else if (source == Source.PRODUCT_SEARCH_LINK) {
            // Reformulate the URL which triggered us into a query, so that the
            // request goes to the same
            // TLD as the scan URL.
            Message message = Message.obtain(handler, R.id.launch_product_query);
            int end = sourceUrl.lastIndexOf("/scan");
            message.obj = sourceUrl.substring(0, end) + "?q=" + resultHandler.getDisplayContents().toString() + "&source=zxing";
            handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
        } else if (source == Source.ZXING_LINK) {
            // Replace each occurrence of RETURN_CODE_PLACEHOLDER in the
            // returnUrlTemplate
            // with the scanned code. This allows both queries and REST-style
            // URLs to work.
            Message message = Message.obtain(handler, R.id.launch_product_query);
            message.obj = returnUrlTemplate.replace(RETURN_CODE_PLACEHOLDER, resultHandler.getDisplayContents().toString());
            handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (isPermissionTrue) {
                CameraManager.get().CameraOpenDriver(surfaceHolder);
                // Creating the handler starts the preview, which can also throw a
                // RuntimeException.
                if (handler == null) {
                    handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
                }
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit(ioe.toString());
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializating camera", e);
            displayFrameworkBugMessageAndExit(e.toString());
        }
    }

    private void displayFrameworkBugMessageAndExit(String error_msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug) + "\n" + error_msg);
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    private void resetStatusView() {
        viewfinder_capture_preview.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinder_capture_preview.drawViewfinder();
    }


    @Override
    public void onBackPressed() {
        Log.e("krm0219", TAG + "  onBackPressed");
        removeBarcodeListInstance();
        Intent intent = getIntent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

          /*  // TODO TEST : 키보드 입력해서 다음으로 넘어가기
            if (opID.equals("karam.kim")) {

                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(edit_capture_type_number, InputMethodManager.SHOW_IMPLICIT);
            }*/
        } else {

            try {

                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(edit_capture_type_number, InputMethodManager.SHOW_IMPLICIT);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onTouch Exception : " + e.toString());
                e.printStackTrace();
            }
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
//			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
        }
    }

    int mScanStatusIndex = 4;

    String getScanStatusType(int index) {
        String[] arrStatusType = getResources().getStringArray(R.array.status_code);
        return arrStatusType[index];
    }


    /*
     * 바코드를 복수건 입력한후 확인버튼 클릭 이벤트메소드 2013-05-09 add by jmkang
     * Delivery Done
     */
    public void onConfirmButtonClick() {

        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {
            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        // 넘기는 데이터 재정의 스캔성공된 것들만 보낸다.
        DeliveryInfo info = null;
        BarcodeListData ba;
        String receiverName = "";
        boolean diffReceiverName = false;

        ArrayList<BarcodeListData> newBarcodeNoList = new ArrayList<>();
        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {
            ba = (BarcodeListData) scanBarcodeArrayList.get(i);

            if (ba.state.equals("SUCCESS")) {
                info = getDeliveryInfo(ba.barcode);

                try {
                    // 수취인성명이 틀린경우
                    if (!receiverName.equals("")) {
                        if (!receiverName.toUpperCase().equals(info.receiverName.toUpperCase())) {
                            diffReceiverName = true;
                        }
                    }
                } catch (Exception e) {

                    diffReceiverName = true;
                }

                receiverName = info.receiverName;
                newBarcodeNoList.add(ba);
            }
        }

        // 받는사람이 틀리다면 에러 메세지
        if (diffReceiverName) {
            Toast toast = Toast.makeText(this, R.string.msg_different_recipient_address, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        Log.e("krm0219", "SCAN > Delivery Done onConfirmButtonClick ");
        if (newBarcodeNoList.size() > 0) { // 사인화면으로 넘길데이터가 있다면
            Gson gson = new Gson();
            String jsonResult = gson.toJson(newBarcodeNoList);
            // 사인입력화면으로 인텐트
            Intent intentSign = new Intent(this, SigningDeliveryDoneActivity.class);
            intentSign.putExtra("title", mScanTitle);
            intentSign.putExtra("type", BarcodeType.TYPE_DELIVERY);
            intentSign.putExtra("result", jsonResult);
            intentSign.putExtra("data", newBarcodeNoList);
            //startActivity(intentSign);
            this.startActivityForResult(intentSign, REQUEST_SIGN_REQUEST);
            //finish();
        } else {

            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }
    }

    /*
     * 송장번호 규칙에 맞는지 체크한후 프리뷰영역에 이미지를 보여준다.
     * modified : 2016-09-09 eylee self-collection 복수 건 처리 add
     */
    public void onCaptureConfirmButtonClick(View sender) {

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
        if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE)) {

            Intent intentCamera = new Intent(this, CameraActivity.class);
            intentCamera.putExtra("barcode", scanBarcodeArrayList.get(0).getBarcode());
            this.startActivityForResult(intentCamera, REQUEST_CAMERA_REQUEST);
        } else {    // SELF COLLECTOR
            //복수건 가져다가 self-collection by 2016-09-09

            if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {
                Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }

            // 넘기는 데이터 재정의 스캔성공된 것들만 보낸다.
            BarcodeListData ba;

            ArrayList<BarcodeListData> newBarcodeNoList = new ArrayList<BarcodeListData>();
            for (int i = 0; i < scanBarcodeArrayList.size(); i++) {
                ba = (BarcodeListData) scanBarcodeArrayList.get(i);

                if (ba.state.equals("FAIL")) {
                    Toast toast = Toast.makeText(this, R.string.msg_invalid_scan, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    return;
                } else {
                    newBarcodeNoList.add(ba);
                }
            }

            if (newBarcodeNoList.size() > 0) { // 사인화면으로 넘길데이터가 있다면

                Gson gson = new Gson();
                String jsonResult = gson.toJson(newBarcodeNoList);
                // 사인입력화면으로 인텐트
                Intent intentSign = new Intent(this, SigningActivity.class);
                intentSign.putExtra("title", mScanTitle);
                intentSign.putExtra("type", BarcodeType.SELF_COLLECTION);
                intentSign.putExtra("result", jsonResult);
                intentSign.putExtra("data", newBarcodeNoList);
                intentSign.putExtra("nonq10qfs", String.valueOf(isNonQ10QFSOrder));    //09-12 add isNonQ10QFSOrder
                this.startActivityForResult(intentSign, REQUEST_SELF_COLLECTION);
            } else {

                Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
        }
    }

    //POD Upload 버튼
    public void onPodUpdateButtonClick(View sender) {
        //스캔한 건이 있으면  2단계 진행 후 업로드
        if (scanBarcodeArrayList != null && scanBarcodeArrayList.size() > 0) {
            Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_click_next_before_upload), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(context)) {
            AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
            return;
        }

        //서버에 올리기전 용량체크  내장메모리가 10메가 안남은경우
        if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
            AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
            return;
        }

        ArrayList<UploadData> songjanglist = new ArrayList<>();
        // 업로드 대상건 로컬 DB 조회
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

        String selectQuery = "SELECT invoice_no, stat FROM " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY + " WHERE reg_id= '" + opID + "'" + " and punchOut_stat <> 'S' ";
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


        if (songjanglist.size() > 0) {
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
                            for (String failinfo : resultList) {  //no songjang:Reason
                                result.append(failinfo);
                            }
                            Intent intent = getIntent();
                            intent.putExtra("result", result.toString());
                            intent.putExtra("type", getScanStatusType(mScanStatusIndex));
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }).build().execute();
        }
    }


    public void onUpdateButtonClick(View sender) {
        if (scanBarcodeArrayList == null || scanBarcodeArrayList.size() < 1) {
            Toast toast = Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER)) {

            if (!NetworkUtil.isNetworkAvailable(context)) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            //서버에 올리기전 용량체크  내장메모리가 10메가 안남은경우
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }


            new ManualDriverAssignHelper.Builder(this, opID, officeCode, deviceID, scanBarcodeArrayList)
                    .setOnDriverAssignEventListener(new ManualDriverAssignHelper.OnDriverAssignEventListener() {

                        @Override
                        public void onPostAssignResult(DriverAssignResult stdResult) {
                            // fail 된 번호만 다시 화면에 설정
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

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }).build().execute();

        } else if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {

            if (!NetworkUtil.isNetworkAvailable(context)) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            //서버에 올리기전 용량체크  내장메모리가 10메가 안남은경우
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            if (gpsEnable && gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " onUpdateButtonClick GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            new ManualChangeDelDriverHelper.Builder(this, opID, officeCode, deviceID, barcodeContrNoList, latitude, longitude)
                    .setOnChangeDelDriverEventListener(new OnChangeDelDriverEventListener() {

                        @Override
                        public void onPostAssignResult(DriverAssignResult stdResult) {
                            //  fail된 번호만 다시 화면에 설정

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

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }).build().execute();
        }
    }

    public void onResetButtonClick() {

        if (scanBarcodeArrayList != null && !scanBarcodeArrayList.isEmpty() && scanBarcodeArrayList.size() > 0) {

            scanBarcodeArrayList.clear();
            inputBarcodeNoListAdapter.notifyDataSetChanged();

            if (historyManager != null) {
                historyManager.clearHistory();
            }

            mScanCount = 0;
            text_capture_scan_count.setText(String.valueOf(mScanCount));

            if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                ArrayList<OutletPickupDoneResult.OutletPickupDoneTrackingNoItem> listItem = resultData.getTrackingNoList();

                for (int i = 0; i < listItem.size(); i++) {

                    BarcodeListData data = new BarcodeListData();
                    data.setState("FAIL");
                    data.setBarcode(listItem.get(i).getTrackingNo());

                    scanBarcodeArrayList.add(i, data);
                }

                inputBarcodeNoListAdapter.notifyDataSetChanged();
            }
        }


        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                || mScanType.equals(BarcodeType.PICKUP_CNR)
                || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {
            removeBarcodeListInstance();
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

    private void AlertShowNotCloseActivity(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 닫기
                    }
                });
        alert_internet_status.show();
    }


    int mScanCount = 0;

    private void addBarcodeNo(String strBarcodeNo, boolean isDuplicate, String where) {

        Log.e("krm0219", "addBarcodeNo called > " + where);
        Log.e("krm0219", "addBarcodeNo - " + strBarcodeNo + "  Duplicate : " + isDuplicate);

        strBarcodeNo = strBarcodeNo.replaceAll("\\r\\n|\\r|\\n", "");
        boolean isConn = NetworkUtil.isNetworkAvailable(context);

        if (mScanType.equals(BarcodeType.PICKUP_CNR) && isValidationPickupCnr == false) {  //2016-09-21 add type validation

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            // 2016-09-01 eylee 여기서 유효성 검사해서 네트워크 타기
            // 유효성 검사에 통과하면 여기서 소리 추가하면서 addBarcode
            // sqlite 에 cnr barcode scan no 가 있는지 확인하고 insert 하는 sqlite validation 부분 필요
            // validation 성공했을 때, editext 에 넣고 실패하면, alert 띄우고 editText 에 들어가지 않음
            // 성공하면 sqlite 에 insert
            ValidationPickupCNRRequestTask validationPickupCNRRequestTask = new ValidationPickupCNRRequestTask();
            validationPickupCNRRequestTask.execute(strBarcodeNo, String.valueOf(isDuplicate));

            beepManager.playBeepSoundAndVibrate(); // 소리추가
            return;
        }

        // 2016-09-20 eylee add scan Barcode No
        if (mScanType.equals(BarcodeType.SELF_COLLECTION)) {
            // duplicate 체크
            if (isDuplicate) {
                Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_duplicate_tracking_no), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
            if (!isInvoiceCodeRule(strBarcodeNo, mScanType)) {
                Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_invalid_scan), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return;
            }
            //2016-09-12 eylee nq 끼리만 self collector 가능하게 수정하기
            //직접 add btn 을 누르는 것 뿐만 아니라 스캔해서 들어오는 number 들 validation check
            //위해서 이 부분으로 이동이 필요함
            if (!scanBarcodeArrayList.isEmpty()) {
//			if(scanBarcodeArrayList.size() > 1){
                boolean tempIsNonQ10QFSOrder = isNonQ10QFSOrder;
                boolean tempValidation = isNonQ10QFSOrderForSelfCollection(strBarcodeNo);


                if (tempIsNonQ10QFSOrder != tempValidation) {
                    // alert 띄워줘야 함 type 이 다르다는 - 기존 Self - Collection 과 새로 NQ 가
                    Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_different_order_type), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    return;
                } else {
                    isNonQ10QFSOrder = tempValidation;
                }
            } else {
                isNonQ10QFSOrder = isNonQ10QFSOrderForSelfCollection(strBarcodeNo);
            }

        } // end of self collector


        if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) && !passedValidation) {

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            final String scanNo = strBarcodeNo;
            final boolean isDupl = isDuplicate;

            new OutletPickupScanValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo, mRoute)
                    .setOnPickupAddScanNoOneByOneUploadListener(new OutletPickupScanValidationCheckHelper.OnPickupAddScanNoOneByOneUploadListener() {

                        @Override
                        public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {
                            //유효성 검사 실패 시
                            if (result.getResultCode() < 0) {

                                Log.e("krm0219", "addBarcodeNo  Fail  ----------------");
                                beepManager2.playBeepSoundAndVibrate(); //실패 시 삐~~
                                passedValidation = false;
                                edit_capture_type_number.setText("");
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                deletePrevious(scanNo);
                                return;
                            } else {

                                if (isDupl) {
                                    Log.e("krm0219", "addBarcodeNo  Success  Duplicate ----------------");
                                    beepManager3.playBeepSoundAndVibrate();
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 20);
                                    toast.show();
                                    edit_capture_type_number.setText("");
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                    return;
                                } else {
                                    Log.e("krm0219", "addBarcodeNo  Success  ----------------");

                                    beepManager.playBeepSoundAndVibrate(); // 성공 시 딩동 소리
                                    passedValidation = true;
//
                                    //바인딩을 위해 재호출
                                    addBarcodeNo(scanNo, isDupl, "OutletPickupScanValidationCheckHelper");
                                    return;
                                }
                            }
                        }
                    }).build().execute();

            return;
        }

        if (mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) && !passedValidation) {

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            final String scanNo = strBarcodeNo;
            final boolean isDupl = isDuplicate;


            // ADD 버튼으로 픽업 번호와 연결 된 패킹 번호 추가
            new ManualPickupAddScanNoOneByOneUploadHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                    .setOnPickupAddScanNoOneByOneUploadListener(new ManualPickupAddScanNoOneByOneUploadHelper.OnPickupAddScanNoOneByOneUploadListener() {

                        @Override
                        public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {
                            //유효성 검사 실패 시
                            if (result.getResultCode() < 0) {
                                beepManager2.playBeepSoundAndVibrate(); //실패 시 삐~~
                                passedValidation = false;

                                edit_capture_type_number.setText("");
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                deletePrevious(scanNo);

                                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                return;
                            } else {
                                if (isDupl) {
                                    beepManager3.playBeepSoundAndVibrate();
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 20);
                                    toast.show();
                                    edit_capture_type_number.setText("");
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                    return;
                                } else {
                                    beepManager.playBeepSoundAndVibrate(); // 성공 시 딩동 소리
                                    passedValidation = true;
//
                                    //바인딩을 위해 재호출
                                    addBarcodeNo(scanNo, isDupl, "ManualPickupAddScanNoOneByOneUploadHelper");
                                    return;
                                }
                            }
                        }
                    }).build().execute();


            return;
        }

        if (mScanType.equals(BarcodeType.PICKUP_ADD_SCAN) && !passedValidation) {
            if (isDuplicate) {

                beepManager3.playBeepSoundAndVibrate(); //실패 시 삐~~
                edit_capture_type_number.setText("");

                Toast toast = Toast.makeText(this, context.getResources().getString(R.string.msg_duplicate_tracking_no), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                return;
            }

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            final String scanNo = strBarcodeNo;
            final boolean isDupl = isDuplicate;

            // ADD 버튼으로 픽업 번호와 연결 된 패킹 번호 추가
            new ManualPickupAddScanNoOneByOneUploadHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                    .setOnPickupAddScanNoOneByOneUploadListener(new ManualPickupAddScanNoOneByOneUploadHelper.OnPickupAddScanNoOneByOneUploadListener() {

                        @Override
                        public void onPickupAddScanNoOneByOneUploadResult(StdResult result) {
                            //유효성 검사 실패 시
                            if (result.getResultCode() < 0) {
                                beepManager2.playBeepSoundAndVibrate(); //실패 시 삐~~
                                passedValidation = false;

                                edit_capture_type_number.setText("");

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                deletePrevious(scanNo);

                                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                return;
                            } else {
                                if (isDupl) {
                                    beepManager3.playBeepSoundAndVibrate();
                                    edit_capture_type_number.setText("");
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                    return;
                                } else {
                                    beepManager.playBeepSoundAndVibrate(); // 성공 시 딩동 소리
                                    passedValidation = true;
//
                                    //바인딩을 위해 재호출
                                    addBarcodeNo(scanNo, isDupl, "ManualPickupAddScanNoOneByOneUploadHelper");
                                    return;
                                }
                            }
                        }
                    }).build().execute();

            return;
        }

        // TAKE BACK  Validation
        if (mScanType.equals(BarcodeType.PICKUP_TAKE_BACK) && !passedValidation) {

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            final String scanNo = strBarcodeNo;
            final boolean isDupl = isDuplicate;

            // Take Back Validation Check
            new ManualPickupTakeBackValidationCheckHelper.Builder(this, opID, pickupNo, strBarcodeNo)
                    .setOnPickupTakeBackValidationCheckListener(new ManualPickupTakeBackValidationCheckHelper.OnPickupTakeBackValidationCheckListener() {

                        @Override
                        public void onPickupTakeBackValidationCheckResult(StdResult result) {

                            if (result.getResultCode() < 0) {

                                beepManager2.playBeepSoundAndVibrate();
                                passedValidation = false;

                                edit_capture_type_number.setText("");
                                deletePrevious(scanNo);

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                return;
                            } else {

                                if (isDupl) {

                                    beepManager3.playBeepSoundAndVibrate();

                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 20);
                                    toast.show();

                                    edit_capture_type_number.setText("");
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                    return;
                                } else {
                                    // SUCCESS
                                    beepManager.playBeepSoundAndVibrate();
                                    passedValidation = true;

                                    //바인딩을 위해 재호출
                                    addBarcodeNo(scanNo, isDupl, "ManualPickupTakeBackValidationCheckHelper");
                                    return;
                                }
                            }
                        }
                    }).build().execute();
            return;
        }

        if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) && !passedValidation) {

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            final String scanNo = strBarcodeNo;
            final boolean isDupl = isDuplicate;


            // ADD 버튼으로 픽업 번호와 연결 된 패킹 번호 추가   // Validation Check
            new ManualDpc3OutValidationCheckHelper.Builder(this, opID, outletDriverYN, strBarcodeNo)
                    .setOnDpc3OutValidationCheckListener(new ManualDpc3OutValidationCheckHelper.OnDpc3OutValidationCheckListener() {

                        @Override
                        public void OnDpc3OutValidationCheckResult(StdResult result) {
                            //유효성 검사 실패 시
                            if (result.getResultCode() < 0) {
                                beepManager2.playBeepSoundAndVibrate(); //실패 시 삐~~
                                passedValidation = false;

                                edit_capture_type_number.setText("");
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                deletePrevious(scanNo);

                                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                return;
                            } else {

                                if (isDupl) {
                                    beepManager3.playBeepSoundAndVibrate();
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 20);
                                    toast.show();

                                    edit_capture_type_number.setText("");
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                    return;
                                } else {
                                    beepManager.playBeepSoundAndVibrate(); // 성공 시 딩동 소리
                                    passedValidation = true;
//
                                    //바인딩을 위해 재호출
                                    addBarcodeNo(scanNo, isDupl, "ManualDpc3OutValidationCheckHelper");
                                    return;
                                }
                            }
                        }

                        @Override
                        public void OnDpc3OutValidationCheckFailList(StdResult result) {
                            try {
                                Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {

                            }
                        }
                    }).build().execute();
            return;
        }


        if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER) && !passedValidation) {

            if (!isConn) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }
            final String scanNo = strBarcodeNo;
            final boolean isDupl = isDuplicate;


            // ADD 버튼으로 픽업 번호와 연결 된 패킹 번호 추가
            new ManualChangeDelDriverValidCheckHelper.Builder(this, opID, strBarcodeNo)
                    .setOnChangeDelDriverValidCheckListener(new ManualChangeDelDriverValidCheckHelper.OnChangeDelDriverValidCheckListener() {

                        @Override
                        public void OnChangeDelDriverValidCheckResult(ChgDelDriverResult result) {
                            //유효성 검사 실패 시
                            if (result.getResultCode() < 0) {
                                beepManager2.playBeepSoundAndVibrate(); //실패 시 삐~~
                                passedValidation = false;

                                edit_capture_type_number.setText("");
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                deletePrevious(scanNo);

                                imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                return;
                            } else {

                                if (isDupl) {
                                    beepManager3.playBeepSoundAndVibrate();
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 20);
                                    toast.show();

                                    edit_capture_type_number.setText("");
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                                    return;
                                } else {
                                    beepManager.playBeepSoundAndVibrate(); // 성공 시 딩동 소리
                                    passedValidation = true;
                                    chgDelDriverResultObj = result.getResultObject();
                                    //바인딩을 위해 재호출
                                    addBarcodeNo(scanNo, isDupl, "ManualChangeDelDriverValidCheckHelper");
                                    return;
                                }
                            }
                        }

                        @Override
                        public void OnChangeDelDriverValidCheckFailList(ChgDelDriverResult result) {
                        }
                    }).build().execute();

            return;
        }


        Log.e("krm0219", TAG + "  addBarcodeNo  HERE");
        //TODO  -  SCAN LIST 보여주기
        if (!isDuplicate) {

            BarcodeListData data = new BarcodeListData();
            data.setBarcode(strBarcodeNo.toUpperCase());

            if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {
                data.setBarcode(chgDelDriverResultObj.getTrackingNo() + "  |  " + chgDelDriverResultObj.getStatus() + "  |  " + chgDelDriverResultObj.getCurrentDriver());
            }
            data.setState("NONE");

            mScanCount++;

            text_capture_scan_count.setText(String.valueOf(mScanCount));

            // NOTIFICATION  -  스캔 시 최근 스캔한 바코드가 제일 위로 셋팅됨.
            // 2019.06  CnR, Add Scan 추가
            if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)
                    || mScanType.equals(BarcodeType.PICKUP_CNR)
                    || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL) || mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                    || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                data.setState("SUCCESS");
                scanBarcodeArrayList.add(0, data);
                inputBarcodeNoListAdapter.notifyDataSetChanged();

                list_capture_scan_barcode.setSelection(0);
                list_capture_scan_barcode.smoothScrollToPosition(0);

                if (mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {
                    barcodeList.add(chgDelDriverResultObj.getTrackingNo() + "  |  " + chgDelDriverResultObj.getStatus() + "  |  " + chgDelDriverResultObj.getCurrentDriver());
                    barcodeContrNoList.put(strBarcodeNo, chgDelDriverResultObj.getContrNo());
                } else {
                    barcodeList.add(strBarcodeNo);
                }
            } else if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

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
                    scanBarcodeArrayList.set(position, data);
                    inputBarcodeNoListAdapter.notifyDataSetChanged();

                    list_capture_scan_barcode.setSelection(0);
                    list_capture_scan_barcode.smoothScrollToPosition(0);
                    barcodeList.add(strBarcodeNo);
                } else {

                    mScanCount--;

                    text_capture_scan_count.setText(String.valueOf(mScanCount));

                    inputBarcodeNoListAdapter.notifyDataSetChanged();

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
            } else {
                // noti. 스캔 시 최근 스캔한 바코드가 아래로 추가됨.

                scanBarcodeArrayList.add(data);
                inputBarcodeNoListAdapter.notifyDataSetChanged();
            }


            if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE)) {

                if (isScanBarcode(strBarcodeNo)) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 20);
                    toast.show();

                    edit_capture_type_number.setText("");
                    scanBarcodeArrayList.clear();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
                    return;

                } else {

                    scanBarcodeArrayList.clear();
                    scanBarcodeArrayList.add(data);
                }
            }


            if (mScanType.equals(BarcodeType.PICKUP_CNR)) { // pickup C&R scan no 바코드 추가 2016-08-30

                isValidationPickupCnr = false;
            } else {

                if (!mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) && !mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {
                    updateInvoiceNO(mScanType, strBarcodeNo);
                }
            }
        } else {

            if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)
                    || mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                beepManager3.playBeepSoundAndVibrate();
                Toast toast = Toast.makeText(getApplicationContext(), R.string.msg_tracking_number_already_entered, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 20);
                toast.show();
            }
        }

        passedValidation = false; //초기화

        edit_capture_type_number.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit_capture_type_number.getWindowToken(), 0);
    }


    private void onBluetoothBarcodeAdd(String strBarcodeNo) {
        if (!TextUtils.isEmpty(strBarcodeNo)) {
            boolean isDuplicate = historyManager.addHistoryItem(new Result(strBarcodeNo, null, null, null), null);

            addBarcodeNo(strBarcodeNo, isDuplicate, "onBluetoothBarcodeAdd");
            if (!isDuplicate) {
                beepManager.playBeepSoundAndVibrate();  // 소리추가
            }
        }
    }


    /*
     * update delivery set stat = @stat , chg_id = localStorage.getItem('opId')
     * , chg_dt = datetime('now') where invoice_no = @invoice_no COLLATE NOCASE
     * and punchOut_stat <> 'S' and reg_id = localStorage.getItem('opId')
     */
    private void updateInvoiceNO(String scanType, String invoiceNo) {
        String opId = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String changeDataString = dateFormat.format(date);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

        ContentValues contentVal = new ContentValues();
        contentVal.put("stat", scanType);
        contentVal.put("chg_id", opId);
        contentVal.put("chg_dt", changeDataString);
        int updateCount = 0;


        // 복수건 배달완료 시점에서는 아무것도 안함 사인전 jmkang 2013-05-08
        if (mScanType.equals(BarcodeType.DELIVERY_DONE)) {

            ContentValues contentVal2 = new ContentValues();
            contentVal2.put("reg_id", opId); // 해당 배송번호를 가지고 자신의아이디만 없데이트

            updateCount = dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                    "invoice_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?",
                    new String[]{invoiceNo, opId});

        } else if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE) || mScanType.equals(BarcodeType.SELF_COLLECTION)
                || scanType.equals(BarcodeType.PICKUP_SCAN_ALL) || scanType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || scanType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || scanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            if (isInvoiceCodeRule(invoiceNo, mScanType)) {
                updateCount = 1;
            }
        } else {

            updateCount = dbHelper.update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?",
                    new String[]{invoiceNo, opId});
        }

        String message = String.format(" [ %s ] ", mScanTitle);
        String result = "NONE";

        String inputBarcode = scanBarcodeArrayList.get(scanBarcodeArrayList.size() - 1).getBarcode();

        if (updateCount < 1) {
            // 에러
            message += context.getResources().getString(R.string.text_not_assigned);
            result = "FAIL";
        } else {
            // 성공
            message += context.getResources().getString(R.string.text_success);
            result = "SUCCESS";

            // 단건처리일 때 각각의 상태에 따라 화면 이동 처리
            if (isSingleScan(mScanType)) {
                if (mScanType.equals(BarcodeType.DELIVERY_DONE)) {
                    // 사인 입력 화면
                    DeliveryInfo info = getDeliveryInfo(inputBarcode);

                    String strReceiverName = info.receiverName;
                    String strSenderName = info.senderName;

                    Intent intentSign = new Intent(this, SigningDeliveryDoneActivity.class);                // krm0219  2018.10.12
                    intentSign.putExtra("title", mScanTitle);
                    intentSign.putExtra("type", BarcodeType.TYPE_DELIVERY);
                    intentSign.putExtra("receiverName", strReceiverName);
                    intentSign.putExtra("senderName", strSenderName);
                    intentSign.putExtra("waybillNo", inputBarcode);
                    startActivity(intentSign);
                    finish();
                }
            }
        }

        if (!mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

            BarcodeListData data = new BarcodeListData();
            data.setBarcode(inputBarcode);
            data.setState(result);

            scanBarcodeArrayList.set(scanBarcodeArrayList.size() - 1, data);
            inputBarcodeNoListAdapter.notifyDataSetChanged();
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


    public DeliveryInfo getDeliveryInfo(String barcodeNo) {

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        DeliveryInfo info = new DeliveryInfo();

        if (cursor.moveToFirst()) {
            info.receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
            info.senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"));
        }

        if (cursor != null)
            cursor.close();

        return info;
    }

    /*
     * 시트스캔건수
     */
    public String getScanDeliveryCount() {

        String opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT count(*) as scan_cnt FROM " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY + " WHERE reg_id='" + opID + "'");

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("scan_cnt"));
        }

        cursor.close();

        return String.valueOf(count);
    }

    /*
     * 이미 바코드스캔조회 (Scan Barcode)
     */
    public boolean isScanBarcode(String barcodeNo) {

        boolean status = false;

        String opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT count(*) as scan_cnt FROM " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY + " WHERE invoice_no = '" + barcodeNo + "' and reg_id='" + opID + "'");

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("scan_cnt"));
            if (count > 0) {
                status = true;
            }
        }

        cursor.close();

        return status;
    }


    // 단건 여부 체크
    private boolean isSingleScan(String strType) {
        if (strType.equals(BarcodeType.DELIVERY_DONE)
                || strType.equals(BarcodeType.SELF_COLLECTION)) { // 복수건처리
            // 2013-05-09
            // 단건 처리 false
            return false;
        } else {
            // TYPE_OUT_FOR_DELIVERY AND TYPE_OUT_FOR_PICKUP AND TYPE_PICKED_UP
            // 이외 경우에 단건처리 true
            return true;
        }
    }

    // 센서 관련 객체
    SensorManager m_sensor_manager;
    Sensor m_light_sensor;

    // 정확도 변경시 호출되는 메소드. 센서의 경우 거의 호출되지 않는다.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // 측정한 값을 전달해주는 메소드.
    @Override
    public void onSensorChanged(SensorEvent event) {
    }


    /*
     * Qxpress송장번호 규칙(범용)
     * 운송장번호 규칙이 맞는지 체크
     * 10문자 안넘으면 false, 맨앞두글자가 KR,SG,QX,JP,CN이 아닐경우 false, 5,6번째가 숫자가 아닐경우 false, 영문숫자조합
     */
    public static boolean isInvoiceCodeRule(String invoiceNo, String mType) {

        boolean status = true;

        if (mType.equals(BarcodeType.PICKUP_SCAN_ALL) || mType.equals(BarcodeType.PICKUP_ADD_SCAN)
                || mType.equals(BarcodeType.OUTLET_PICKUP_SCAN) || mType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            return true;
        }

        // 2016-08-23 eylee C2C, RPC 번호 스캔할 수 있도록 기능확장
        if (!mType.equals(BarcodeType.SELF_COLLECTION)) {            // BarcodeType.TYPE_SCAN_CAPTURE
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
            if (!isStringDouble(sub_invoice_int)) {
                return false;
            }
        }

        return true;
    }


    /*
     * 숫자인지체크
     */
    public static boolean isStringDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * date : 2016-08-30  eylee
     * desc : pickup C&R scan next 버튼 클릭했을 때  //  AND, Pickup 관련
     */

    public void onNextButtonClick(View sender) {

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


        boolean isConnect = NetworkUtil.isNetworkAvailable(context);

        if (!isConnect) {
            if (mScanType.equals(BarcodeType.PICKUP_CNR)) {
                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
            } else {
                AlertShowNotCloseActivity(context.getResources().getString(R.string.msg_network_connect_error));
            }
            return;
        }

        //서버에 올리기전 용량체크  내장메모리가 10메가 안남은경우
        if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
            if (mScanType.equals(BarcodeType.PICKUP_CNR)) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
            } else {
                AlertShowNotCloseActivity(context.getResources().getString(R.string.msg_disk_size_error));
            }
            return;
        }


        String tempAmount = String.valueOf(scanBarcodeArrayList.size());    // scanned count
        String temp_WaybillNo_list = "";                                    // scanned barcode list

        for (int i = 0; i < scanBarcodeArrayList.size(); i++) {
            temp_WaybillNo_list += scanBarcodeArrayList.get(i).getBarcode();
            if (i != (scanBarcodeArrayList.size() - 1)) {
                temp_WaybillNo_list += ",";
            }
        }

        if (mScanType.equals(BarcodeType.PICKUP_CNR)) {

            removeBarcodeListInstance();

            Intent intent = new Intent(this, CnRPickupDoneActivity.class);
            intent.putExtra("title", context.getResources().getString(R.string.text_cnr_pickup_done));
            intent.putExtra("type", BarcodeType.PICKUP_CNR);
            intent.putExtra("receiverName", opName);
            intent.putExtra("senderName", pickupCNRRequestor);
            intent.putExtra("waybillNo", temp_WaybillNo_list);
            intent.putExtra("reqQty", tempAmount);
            this.startActivityForResult(intent, REQUEST_PICKUP_CNR_REQUEST);

        } else if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

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

            Log.e("krm0219", TAG + "  Scanned List : " + scanned_list);


            Intent intent = new Intent(this, OutletPickupDoneActivity.class);
            intent.putExtra("title", mTitle);
            intent.putExtra("type", BarcodeType.OUTLET_PICKUP_SCAN);
            intent.putExtra("pickup_no", mPickupNo);
            intent.putExtra("applicant", mApplicant);
            intent.putExtra("qty", mQty);
            intent.putExtra("route", mRoute);
            intent.putExtra("scanned_qty", scanned_qty);
            intent.putExtra("tracking_data", resultData);
            intent.putExtra("scanned_list", scanned_list);

            removeBarcodeListInstance();
            startActivityForResult(intent, CaptureActivity.REQUEST_OUTLET_PICKUP_SCAN);

        } else if (mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)) {

            // 2017-03-27 eylee
            Intent intent = new Intent(this, SigningPickupScanAllDoneActivity.class);
            intent.putExtra("title", context.getResources().getString(R.string.text_start_to_scan));
            intent.putExtra("type", BarcodeType.PICKUP_SCAN_ALL);
            intent.putExtra("receiverName", opName);
            intent.putExtra("pickup_no", pickupNo);
            intent.putExtra("applicant", pickupApplicantName);
            intent.putExtra("waybillNo", temp_WaybillNo_list);
            intent.putExtra("reqQty", tempAmount);

            removeBarcodeListInstance();
            this.startActivityForResult(intent, CaptureActivity.REQUEST_PICKUP_SCAN_ALL);

        } else if (mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)) {

            Intent intent = new Intent(this, PickupAddScanActivity.class);
            intent.putExtra("title", context.getResources().getString(R.string.text_title_add_pickup));
            intent.putExtra("pickupNo", pickupNo);
            intent.putExtra("scannedList", temp_WaybillNo_list);
            intent.putExtra("scannedQty", tempAmount);
            intent.putExtra("senderName", pickupApplicantName);
            this.startActivityForResult(intent, REQUEST_PICKUP_ADD_SCAN);

        } else if (mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

            Intent intent = new Intent(this, SigningPickupTakeBackActivity.class);

            intent.putExtra("title", context.getResources().getString(R.string.button_take_back));
            intent.putExtra("type", BarcodeType.PICKUP_TAKE_BACK);
            intent.putExtra("pickup_no", pickupNo);
            intent.putExtra("applicant", pickupApplicantName);
            intent.putExtra("total_qty", mQty);
            intent.putExtra("take_back_qty", tempAmount);
            intent.putExtra("waybillNo", temp_WaybillNo_list);

            removeBarcodeListInstance();
            this.startActivityForResult(intent, REQUEST_PICKUP_TAKE_BACK);
        }
    }

    class ValidationPickupCNRRequestTask extends AsyncTask<String, Integer, PickupCNRResult> {

        String scanBarcodeNoStr = "";
        String tempBarcodeNo = "";
        Boolean isDuplicate = false;

        @Override
        protected PickupCNRResult doInBackground(String... params) {

            PickupCNRResult pickupCNRResult;

            if (params != null) {

                scanBarcodeNoStr = params[0];
                isDuplicate = Boolean.valueOf(params[1]);
                tempBarcodeNo = params[0];

                if (!isDuplicate) {

                    pickupCNRResult = requestGetPickupCNRInfo(scanBarcodeNoStr);
                } else {

                    pickupCNRResult = new PickupCNRResult();
                    pickupCNRResult.setResultCode(-100);
                    pickupCNRResult.setResultMsg(context.getResources().getString(R.string.msg_tracking_number_already_entered));
                    PickupCNRResult.ResultObject pickupResultObject = new PickupCNRResult.ResultObject();
                    pickupResultObject.setPartnerRefNo(scanBarcodeNoStr);
                    pickupCNRResult.setResultObject(pickupResultObject);
                }
            } else {

                pickupCNRResult = new PickupCNRResult();
                pickupCNRResult.setResultCode(-200);
                pickupCNRResult.setResultMsg(context.getResources().getString(R.string.msg_internal_error));
            }

            return pickupCNRResult;
        }

        @Override
        protected void onPostExecute(PickupCNRResult result) {

            if (result != null) {

                int resultCode = result.getResultCode();
                String resultMsg = result.getResultMsg();
                PickupCNRResult.ResultObject resultObject = result.getResultObject();

                String temp_cnr_pickup_no = "";
                if (resultObject != null) {
                    temp_cnr_pickup_no = resultObject.getInvoiceNo();
                }

                if (resultCode == 0) { // 성공  pickup cnr barcode 가 유효한 값 객체도 가져오는

                    // 2016-09-03 eylee
                    // DB 에 pickup cnr no 가 있는지 먼저 확인 하고,
                    boolean isSQliteDuplicate = CNRDownloadDuplicateChk(resultObject.getContrNo(), resultObject.getInvoiceNo());
                    Log.e("krm0219", TAG + "  CnR DB Duplicate : " + isSQliteDuplicate);

                    if (!isSQliteDuplicate) {   //없으면 DB 저장

                        isValidationPickupCnr = true;
                        insertDevicePickupCNRData(resultObject);
                        pickupCNRRequestor = resultObject.getReqName();
                        addBarcodeNo(scanBarcodeNoStr, isDuplicate, "ValidationPickupCNRRequestTask1");
                    } else { // DB 에 있을 때

                        isValidationPickupCnr = true;
                        pickupCNRRequestor = selectDevicePickupCNRData(scanBarcodeNoStr);
                        //bug 수정 2016-09-08 added by eylee
                        deletePrevious(temp_cnr_pickup_no);
                        addBarcodeNo(scanBarcodeNoStr, isDuplicate, "ValidationPickupCNRRequestTask2");
                    }
                } else { // 실패  service 에서 메시지 떨굼  -> GetCnROrderCheck 받아다가 메시지 alert 띄워주기

                    edit_capture_type_number.setText("");
                    if (resultCode != -100) {
                        if (!temp_cnr_pickup_no.equals("")) {
                            deletePrevious(temp_cnr_pickup_no);
                        } else {
                            String CNR_PICKUP_NO2 = tempBarcodeNo;
                            deletePrevious(CNR_PICKUP_NO2);
                        }
                    }

                    if (!CaptureActivity.this.isFinishing()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                        builder.setTitle(context.getResources().getString(R.string.text_alert));
                        builder.setMessage(resultMsg);
                        builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    dialog.cancel();
                                } catch (Exception e) {

                                }
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            }
        }
    }

    private PickupCNRResult requestGetPickupCNRInfo(String scanBarcodeNoStr) {

        PickupCNRResult resultObj = null;

        try {

            GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
            HashMap<String, String> hmActionParam = new HashMap<>();

            hmActionParam.put("pickup_no", scanBarcodeNoStr);
            hmActionParam.put("opId", opID);
            hmActionParam.put("network_type", NetworkUtil.getNetworkType(getApplicationContext()));
            hmActionParam.put("app_id", DataUtil.appID);
            hmActionParam.put("nation_cd", DataUtil.nationCode);

            String methodName = "GetCnROrderCheck";
            Serializer serializer = new Persister();

            GMKT_HTTPResponseMessage response2 = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
            String resultString = response2.getResultString();
            Log.e("Server", methodName + "  Result : " + resultString);
            // {"ResultObject":{"contr_no":"55003355","partner_ref_no":"C2859SGSG","invoice_no":"C2859SGSG","stat":"P2","req_nm":"normal order","req_dt":"2019-08-2010:00-19:00","tel_no":"+65--","hp_no":"+65-8424-2354","zip_code":"048741","address":"11 PEKIN STREEThyemi3333","pickup_hopeday":"2019-08-20","pickup_hopetime":"10:00-19:00","sender_nm":"normal order","del_memo":"","driver_memo":"","fail_reason":"WA","qty":"1","cust_nm":"test191919","partner_id":"hyemi223","dr_assign_requestor":"","dr_assign_req_dt":"","dr_assign_stat":"","dr_req_no":"","failed_count":"0","route":"C2C","del_driver_id":null,"cust_no":"100054639"},"ResultCode":0,"ResultMsg":"Success"}

            resultObj = serializer.read(PickupCNRResult.class, resultString);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetCnROrderCheck Exception : " + e.toString());
        }

        return resultObj;
    }


    //픽업 데이타  DB 저장 by 2016-09-03 eylee
    private long insertDevicePickupCNRData(PickupCNRResult.ResultObject data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String regDataString = dateFormat.format(new Date());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        try {
            ContentValues contentVal = new ContentValues();
            contentVal.put("contr_no", data.getContrNo());
            contentVal.put("partner_ref_no", data.getPartnerRefNo());
            contentVal.put("invoice_no", data.getPartnerRefNo());  //invoice_no = partnerRefNo 사용
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
            contentVal.put("failed_count", data.getFailedCount());
            contentVal.put("rcv_request", data.getDelMemo());
            contentVal.put("sender_nm", "");
            contentVal.put("punchOut_stat", "N");
            contentVal.put("reg_id", "");
            contentVal.put("reg_dt", regDataString);
            contentVal.put("fail_reason", data.getFailReason());
            contentVal.put("secret_no_type", "");
            contentVal.put("secret_no", "");

            dbHelper.insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal);
        } catch (Exception e) {
            return 0;
        }

        return 1;
    }

    // pickup cnr duplicate check
    private boolean CNRDownloadDuplicateChk(String contr_no, String invoice_no) {

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        // 날짜기준 UTC
        String selectQuery = "SELECT  partner_ref_no, invoice_no, stat, rcv_nm, sender_nm "
                + " FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no= '" + invoice_no + "'" + " and contr_no= '" + contr_no + "'";

        Cursor cs = dbHelper.get(selectQuery);
        return cs.getCount() > 0;

    }

    public String selectDevicePickupCNRData(String scanBarcodeNoStr) {

        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        String requestor = "";
        // 날짜기준 UTC
        String scanCNRNo = scanBarcodeNoStr.trim().toUpperCase();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no = '" + scanCNRNo + "'";

        Cursor cs = dbHelper.get(selectQuery);
        if (cs.getCount() > 0) {
            if (cs.moveToFirst()) {
                do {
                    requestor = cs.getString(cs.getColumnIndex("req_nm"));
                } while (cs.moveToNext());
            }
        }

        return requestor;
    }

    //invalidation 일 때, SQLite  data 삭제
    public void deletePrevious(String text) {
        if (!text.equals("")) {
//			 Log.e("hello", "deletePrevious empty :: "+text);
            historyManager.deletePrevious(text);
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

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            Log.e("krm0219", TAG + "  onKey keyCode : " + keyCode);
            String tempStrScanNo = edit_capture_type_number.getText().toString().trim();

            if (!tempStrScanNo.equals("")) {
                boolean isDuplicate = historyManager.addHistoryItem(new Result(tempStrScanNo, null, null, null), null);
//	       		Toast.makeText(this, "DB에서 저장되어있는 스캔건 체크 :: " + Boolean.toString(isDuplicate), Toast.LENGTH_SHORT).show();

                // Scan Sheet의 경우 송장번호스캔은 1건만 가능하다.
                if (mScanType.equals(BarcodeType.TYPE_SCAN_CAPTURE)) {
                    mScanCount = 0;
                    isDuplicate = false;

                    beepManager.playBeepSoundAndVibrate(); // 소리추가
                }

                if (mScanType.equals(BarcodeType.SELF_COLLECTION) || mScanType.equals(BarcodeType.DELIVERY_DONE)) {
                    if (!isDuplicate) {
//						Toast.makeText(this, "DB에서 저장되어있는 스캔건 체크 :: " + Boolean.toString(isDuplicate), Toast.LENGTH_SHORT).show();
                        beepManager.playBeepSoundAndVibrate(); // 소리추가
                    } else {

                        beepManager3.playBeepSoundAndVibrate(); // 소리추가
                    }
//					beepManager.playBeepSoundAndVibrate(); // 소리추가
                }

                if (mScanType.equals(BarcodeType.PICKUP_CNR)) {

                    //This is the filter  2번 fired 해서 막음
                    if (event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;

                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey BarcodeType.PICKUP_CNR");
                } else if (mScanType.equals(BarcodeType.CONFIRM_MY_DELIVERY_ORDER) || mScanType.equals(BarcodeType.CHANGE_DELIVERY_DRIVER)) {
                    //This is the filter  2번 fired 해서 막음
                    Log.e("krm0219", TAG + "  onKey event getAction : " + event.getAction());

                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }

                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey BarcodeType.CONFIRM_MY_DELIVERY_ORDER");
                } else if (mScanType.equals(BarcodeType.OUTLET_PICKUP_SCAN)) {

                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey BarcodeType.OUTLET_PICKUP_SCAN");
                } else if (mScanType.equals(BarcodeType.PICKUP_SCAN_ALL)) {

                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey BarcodeType.PICKUP_SCAN_ALL");
                } else if (mScanType.equals(BarcodeType.PICKUP_ADD_SCAN)) {

                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey BarcodeType.PICKUP_ADD_SCAN");
                } else if (mScanType.equals(BarcodeType.PICKUP_TAKE_BACK)) {

                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey BarcodeType.PICKUP_TAKE_BACK");
                } else {
                    addBarcodeNo(tempStrScanNo, isDuplicate, "onKey");
                } // end of else

            } //  end of if(!tempStriScanNo.equals("")) scan null이 아니면
            return true;
//	        edit_capture_type_number.setOnKeyListener(this);

        }
//		else{
//        	 Toast toast1 = Toast.makeText(getApplicationContext(),	"else hi "+keyCode, Toast.LENGTH_SHORT);
//			toast1.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
//	       	toast1.show();
//        }
        return false;
    }
}