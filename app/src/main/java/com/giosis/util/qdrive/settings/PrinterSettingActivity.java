package com.giosis.util.qdrive.settings;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.portableprinter.bluetooth.GPrinterData;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * @editor krm0219
 * <p>
 * 1. 프린터 페어링 (Available Devices > Paired Devices => 여러개 가능)
 * 2. 프린터 연결 (Paired Devices > Connected Device => 1개 가능)
 * <p>
 */
public class PrinterSettingActivity extends AppCompatActivity {
    private String TAG = "PrinterSettingActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    static ListView list_setting_printer_connected_device;
    static LinearLayout layout_setting_printer_no_connected_device;
    static ListView list_setting_printer_paired_device;
    static LinearLayout layout_setting_printer_no_paired_device;
    static ListView list_setting_printer_available_device;
    static LinearLayout layout_setting_printer_no_available_device;


    private Context context;

    public static ArrayList<PrinterDeviceItem> connectedItem = new ArrayList<>();
    public static ArrayList<PrinterDeviceItem> pairedItems = new ArrayList<>();
    public static ArrayList<PrinterDeviceItem> newDeviceItems = new ArrayList<>();

    public int REQUEST_ENABLE_BT = 10001;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setting);

        //
        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_top_title.setText(R.string.text_title_printer_setting);
        layout_top_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


        list_setting_printer_connected_device = findViewById(R.id.list_setting_printer_connected_device);
        layout_setting_printer_no_connected_device = findViewById(R.id.layout_setting_printer_no_connected_device);
        list_setting_printer_paired_device = findViewById(R.id.list_setting_printer_paired_device);
        layout_setting_printer_no_paired_device = findViewById(R.id.layout_setting_printer_no_paired_device);
        list_setting_printer_available_device = findViewById(R.id.list_setting_printer_available_device);
        layout_setting_printer_no_available_device = findViewById(R.id.layout_setting_printer_no_available_device);

        BluetoothDeviceData.availableProgress = findViewById(R.id.progress_available_devices);
        BluetoothDeviceData.availableRefresh = findViewById(R.id.img_available_refresh);

        int color = getResources().getColor(R.color.color_4fb648);
        BluetoothDeviceData.availableProgress.setIndeterminate(true);
        BluetoothDeviceData.availableProgress.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
       /* if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {

            BluetoothDeviceData.availableProgress.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP);
        } else {
            BluetoothDeviceData.availableProgress.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }*/


        //
        context = getApplicationContext();
        String action_type = getIntent().getStringExtra("action");

        if (action_type != null && action_type.equals("before_list")) {

            if (GPrinterData.mBluetoothAdapter != null) {

                GPrinterData.mBluetoothAdapter.cancelDiscovery();
                GPrinterData.mBluetoothAdapter = null;
            }

            if (GPrinterData.printerConnManagerList != null) {

                for (int i = 0; i < GPrinterData.printerConnManagerList.size(); i++) {
                    GPrinterData.printerConnManagerList.get(i).closePort();
                }
                GPrinterData.printerConnManagerList = null;
            }

            if (GPrinterData.printerReceiver != null) {
                context.unregisterReceiver(GPrinterData.printerReceiver);
                GPrinterData.printerReceiver = null;
            }

            if (GPrinterData.gPrinterHandler != null)
                GPrinterData.gPrinterHandler = null;
        }


        BluetoothDeviceData.availableRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                discoveryDevice();
            }
        });


        //
        PermissionChecker checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(PERMISSIONS)) {

            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, PERMISSIONS);
            overridePendingTransition(0, 0);
        } else {

            checkBluetoothState();
        }
    }

    private void checkBluetoothState() {

        // Bluetooth 지원 여부 확인
        BluetoothDeviceData.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth 지원하지 않음
        if (BluetoothDeviceData.mBluetoothAdapter == null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_SHORT).show();
        } else {

            // Bluetooth 지원 && 비활성화 상태
            if (!BluetoothDeviceData.mBluetoothAdapter.isEnabled()) {

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            } else {

                // Bluetooth 지원 && 활성화 상태
                registerReceiverNHandler();
            }
        }
    }

    public void registerReceiverNHandler() {

        if (BluetoothDeviceData.bluetoothDeviceReceiver == null) {

            BluetoothDeviceData.bluetoothDeviceReceiver = new BluetoothDeviceBroadcastReceiver(context);

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDeviceData.ACTION_CONNECT_STATE);        // "action_connect_state"
            filter.addAction(BluetoothDevice.ACTION_FOUND);                 // 기기 검색됨
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);    // 기기 검색 시작
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);   // 기기 검색 종료
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);      // 연결 끊김 확인
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);       // 기기 Pairing 요구
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);    // 기기 Pairing 상태 변화
            registerReceiver(BluetoothDeviceData.bluetoothDeviceReceiver, filter);
        }

        if (BluetoothDeviceData.bluetoothDeviceHandler == null) {
            BluetoothDeviceData.bluetoothDeviceHandler = new BluetoothDeviceHandler(context);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        connectedItem = new ArrayList<>();
        pairedItems = new ArrayList<>();
        newDeviceItems = new ArrayList<>();

        BluetoothDeviceData.printerConnectedListAdapter = new PrinterConnectedListAdapter(PrinterSettingActivity.this, connectedItem);
        BluetoothDeviceData.printerPairedListAdapter = new PrinterPairedListAdapter(PrinterSettingActivity.this, pairedItems);
        BluetoothDeviceData.printerAvailableListAdapter = new PrinterAvailableListAdapter(PrinterSettingActivity.this, newDeviceItems);

        list_setting_printer_paired_device.setAdapter(BluetoothDeviceData.printerPairedListAdapter);
        list_setting_printer_connected_device.setAdapter(BluetoothDeviceData.printerConnectedListAdapter);
        list_setting_printer_available_device.setAdapter(BluetoothDeviceData.printerAvailableListAdapter);

        if (BluetoothDeviceData.mBluetoothAdapter != null) {
            discoveryDevice();
            getDeviceList();
        }

    }

    private void discoveryDevice() {

        // startDiscovery()를 호출하여 디바이스 검색을 시작합니다.
        // 만약 이미 검색중이라면 cancelDiscovery()를 호출하여 검색을 멈춘 후 다시 검색해야 합니다.
        if (BluetoothDeviceData.mBluetoothAdapter.isDiscovering()) {
            BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();
        }

        BluetoothDeviceData.mBluetoothAdapter.startDiscovery();
    }

    private void getDeviceList() {

        Set<BluetoothDevice> pairedDevices = BluetoothDeviceData.mBluetoothAdapter.getBondedDevices();
        Log.e("print", TAG + "  getDeviceList  " + BluetoothDeviceData.connectedPrinterAddress + " / " + pairedDevices.size());

        if (0 < pairedDevices.size()) {

            for (BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                try {
                    Method method = device.getClass().getMethod("getAliasName");
                    if (method != null) {
                        deviceName = (String) method.invoke(device);
                    }

                    if (deviceName == null || deviceName.equals(""))
                        deviceName = device.getName();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (BluetoothDeviceData.connectedPrinterAddress != null && BluetoothDeviceData.connectedPrinterAddress.equals(deviceAddress)) {

                    Log.e("print", TAG + "  connected Device : " + device.getName() + " / " + device.getAddress());
                    if (BluetoothDeviceData.socket != null) {

                        PrinterSettingActivity.connectedItem.add(new PrinterDeviceItem(deviceName, deviceAddress, true, true));
                        BluetoothDeviceData.printerConnectedListAdapter.notifyDataSetChanged();
                    } else {

                        try {

                            BluetoothDeviceData.socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothDeviceData.MY_UUID_INSECURE);
                            BluetoothDeviceData.socket.connect();

                            Intent intent = new Intent(BluetoothDeviceData.ACTION_CONNECT_STATE);
                            intent.putExtra(BluetoothDeviceData.STATE, BluetoothDeviceData.CONN_STATE_CONNECTED);
                            intent.putExtra(BluetoothDeviceData.DEVICE_ID, deviceAddress);
                            intent.putExtra(BluetoothDeviceData.DEVICE_NAME, deviceName);
                            MyApplication.getContext().sendBroadcast(intent);
                        } catch (Exception e) {

                            Toast.makeText(context, "Connect Error.\n" + "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {

                    Log.e("print", TAG + "  paired Device : " + device.getName() + " / " + device.getAddress());
                    PrinterSettingActivity.pairedItems.add(new PrinterDeviceItem(deviceName, deviceAddress, false, false));
                    BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                }

                if (PrinterSettingActivity.pairedItems.size() == 0) {

                    nullPairedDevices();
                } else {

                    notnullPairedDevices();
                }

                if (PrinterSettingActivity.connectedItem.size() == 0) {

                    nullConnectedDevice();
                } else {

                    notnullConnectedDevice();
                }
            }
        }
    }


    public static void nullAvailableDevices() {

        list_setting_printer_available_device.setVisibility(View.GONE);
        layout_setting_printer_no_available_device.setVisibility(View.VISIBLE);
    }

    public static void notnullAvailableDevices() {

        list_setting_printer_available_device.setVisibility(View.VISIBLE);
        layout_setting_printer_no_available_device.setVisibility(View.GONE);
    }

    public static void nullPairedDevices() {

        list_setting_printer_paired_device.setVisibility(View.GONE);
        layout_setting_printer_no_paired_device.setVisibility(View.VISIBLE);
    }

    public static void notnullPairedDevices() {

        list_setting_printer_paired_device.setVisibility(View.VISIBLE);
        layout_setting_printer_no_paired_device.setVisibility(View.GONE);
    }

    public static void nullConnectedDevice() {

        list_setting_printer_connected_device.setVisibility(View.GONE);
        layout_setting_printer_no_connected_device.setVisibility(View.VISIBLE);
    }

    public static void notnullConnectedDevice() {

        list_setting_printer_connected_device.setVisibility(View.VISIBLE);
        layout_setting_printer_no_connected_device.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {

                // k. 블루투스 승인 요청 'YES'
                Toast.makeText(context, context.getResources().getString(R.string.msg_bluetooth_enabled), Toast.LENGTH_SHORT).show();
                checkBluetoothState();
            } else {

                // k. 블루투스 승인 요청 'NO'
                Toast.makeText(context, context.getResources().getString(R.string.msg_bluetooth_not_enabled), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BluetoothDeviceData.REQUEST_RENAME_PAIR_DEVICE) {

            if (resultCode == RESULT_OK) {

                Toast.makeText(context, context.getResources().getString(R.string.msg_bluetooth_device_rename), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                checkBluetoothState();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {

            BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();

            if (BluetoothDeviceData.socket != null) {

                BluetoothDeviceData.socket.close();
                BluetoothDeviceData.socket = null;
            }

            unregisterReceiver(BluetoothDeviceData.bluetoothDeviceReceiver);
            BluetoothDeviceData.bluetoothDeviceReceiver = null;
            BluetoothDeviceData.bluetoothDeviceHandler = null;

            Log.e("print", TAG + "  stopBluetoothService");
        } catch (Exception e) {

            Log.e("Exception", TAG + "  stopBluetoothService  Exception : " + e.toString());
        }
    }
}