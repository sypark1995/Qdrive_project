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

import com.giosis.util.qdrive.singapore.R;

import java.util.Set;


public class DeviceListActivity extends Activity {
    private static final String TAG = "DeviceListActivity";


    TextView text_capture_bluetooth_paired_devices_title;
    ListView list_capture_bluetooth_paired_devices;
    TextView text_capture_bluetooth_other_devices_title;
    ListView list_capture_bluetooth_other_devices;

    Button btn_capture_bluetooth_scan_devices;
    Button btn_capture_bluetooth_cancel;


    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.dialog_device_list);


        text_capture_bluetooth_paired_devices_title = findViewById(R.id.text_capture_bluetooth_paired_devices_title);
        list_capture_bluetooth_paired_devices = findViewById(R.id.list_capture_bluetooth_paired_devices);
        text_capture_bluetooth_other_devices_title = findViewById(R.id.text_capture_bluetooth_other_devices_title);
        list_capture_bluetooth_other_devices = findViewById(R.id.list_capture_bluetooth_other_devices);

        btn_capture_bluetooth_scan_devices = findViewById(R.id.btn_capture_bluetooth_scan_devices);
        btn_capture_bluetooth_cancel = findViewById(R.id.btn_capture_bluetooth_cancel);


        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
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

                if (mBtAdapter.isDiscovering()) {

                    mBtAdapter.cancelDiscovery();
                }

                finish();
            }
        });


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.item_device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.item_device_name);

        // Find and set up the ListView for paired devices
        list_capture_bluetooth_paired_devices.setAdapter(mPairedDevicesArrayAdapter);
        list_capture_bluetooth_paired_devices.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        list_capture_bluetooth_other_devices.setAdapter(mNewDevicesArrayAdapter);
        list_capture_bluetooth_other_devices.setOnItemClickListener(mDeviceClickListener);


        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        Log.e("krm0219", TAG + "  Paired Device Size : " + pairedDevices.size());


        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {

            text_capture_bluetooth_paired_devices_title.setVisibility(View.VISIBLE);

            int kdcDeviceCount = 0;
            for (BluetoothDevice device : pairedDevices) {
                Log.e("krm0219", "device name : " + device.getName());

                if (device.getName() != null && device.getName().contains("KDC")) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    kdcDeviceCount++;
                }
            }

            if (kdcDeviceCount < 1) {
                String noDevices = getResources().getText(R.string.none_paired).toString();
                mPairedDevicesArrayAdapter.add(noDevices);
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        text_capture_bluetooth_other_devices_title.setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mNewDevicesArrayAdapter.clear();
        mNewDevicesArrayAdapter.notifyDataSetChanged();

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            if (address != null && address.contains(":")) {
                // Cancel discovery because it's costly and we're about to connect
                mBtAdapter.cancelDiscovery();

                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    Log.e("krm0219", "mReceiver  device name : " + device.getName());

                    if (device.getName() != null && device.getName().contains("KDC")) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
