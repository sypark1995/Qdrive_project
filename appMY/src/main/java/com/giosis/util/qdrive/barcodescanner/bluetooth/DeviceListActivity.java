package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;

import java.util.Set;


public class DeviceListActivity extends Activity {
    private String TAG = "DeviceListActivity";


    TextView text_capture_bluetooth_paired_devices_title;
    ListView list_capture_bluetooth_paired_devices;
    TextView text_capture_bluetooth_other_devices_title;
    ListView list_capture_bluetooth_other_devices;

    Button btn_capture_bluetooth_scan_devices;
    Button btn_capture_bluetooth_cancel;


    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDeviceArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);


        text_capture_bluetooth_paired_devices_title = findViewById(R.id.text_capture_bluetooth_paired_devices_title);
        list_capture_bluetooth_paired_devices = findViewById(R.id.list_capture_bluetooth_paired_devices);
        text_capture_bluetooth_other_devices_title = findViewById(R.id.text_capture_bluetooth_other_devices_title);
        list_capture_bluetooth_other_devices = findViewById(R.id.list_capture_bluetooth_other_devices);

        btn_capture_bluetooth_scan_devices = findViewById(R.id.btn_capture_bluetooth_scan_devices);
        btn_capture_bluetooth_cancel = findViewById(R.id.btn_capture_bluetooth_cancel);

        setTitle(R.string.select_device);


        btn_capture_bluetooth_scan_devices.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                doDiscovery();
            }
        });

        btn_capture_bluetooth_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothAdapter.isDiscovering()) {

                    bluetoothAdapter.cancelDiscovery();
                }

                finish();
            }
        });


        pairedDeviceArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        newDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        list_capture_bluetooth_paired_devices.setAdapter(pairedDeviceArrayAdapter);
        list_capture_bluetooth_paired_devices.setOnItemClickListener(mDeviceClickListener);

        list_capture_bluetooth_other_devices.setAdapter(newDevicesArrayAdapter);
        list_capture_bluetooth_other_devices.setOnItemClickListener(mDeviceClickListener);


        // Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        // get Device List
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e(TAG, "Bonded Device Size : " + pairedDevices.size());

        if (0 < pairedDevices.size()) {

            int kdcDeviceCount = 0;

            for (BluetoothDevice device : pairedDevices) {

                if (device.getName() != null && device.getName().contains("KDC")) {
                    pairedDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    kdcDeviceCount++;
                }
            }

            if (kdcDeviceCount < 1) {

                pairedDeviceArrayAdapter.add(getResources().getText(R.string.none_paired).toString());
            }
        } else {

            pairedDeviceArrayAdapter.add(getResources().getText(R.string.none_paired).toString());
        }
    }


    private void doDiscovery() {

        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        newDevicesArrayAdapter.clear();
        newDevicesArrayAdapter.notifyDataSetChanged();


        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }


    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            if (address.contains(":")) {

                bluetoothAdapter.cancelDiscovery();

                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        unregisterReceiver(mReceiver);
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // 블루투스 기기 검색됨.
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // 찾아진 디바이스가 페어링 되어 있지 않을 경우  >  [Other Available Devices] 리스트에 표시
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (device.getName() != null && device.getName().contains("KDC")) {
                        newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(MyApplication.localeManager.setLocale(base));
    }
}