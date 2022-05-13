package com.giosis.util.qdrive.singapore.barcodescanner

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.DialogDeviceList1Binding

// TODO_kjyoo 이건 왜 Activity를 상속받 ??
class DeviceListActivity1 : Activity() {

    companion object {
        private const val TAG = "DeviceListActivity"

        // Return Intent extra
        var EXTRA_DEVICE_ADDRESS = "device_address"
        var BLUETOOTH_SCANNER = "KDC"
    }

    private val binding by lazy {
        DialogDeviceList1Binding.inflate(layoutInflater)
    }

    private val mBtAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(binding.root)

        // Set result CANCELED incase the user backs out
        setResult(RESULT_CANCELED)
        setTitle(R.string.select_device)


        binding.btnScanDevices.setOnClickListener { doDiscovery() }

        binding.btnCancel.setOnClickListener {
            if (mBtAdapter!!.isDiscovering) {
                mBtAdapter!!.cancelDiscovery()
            }

            finish()
        }


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.item_device_name)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.item_device_name)

        // Find and set up the ListView for paired devices
        binding.listPairedDevices.adapter = mPairedDevicesArrayAdapter
        binding.listPairedDevices.onItemClickListener = mDeviceClickListener
        // Find and set up the ListView for newly discovered devices
        binding.listOtherDevices.adapter = mNewDevicesArrayAdapter
        binding.listOtherDevices.onItemClickListener = mDeviceClickListener


        // Register for broadcasts when a device is discovered
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)   // 기기 검색
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)     // 기기 검색 종료
        this.registerReceiver(mReceiver, filter)

        // Get a set of currently paired devices
        val pairedDevices = mBtAdapter.bondedDevices
        Log.e(TAG, "  Paired Device Size : " + pairedDevices.size)

        // If there are paired devices, add each one to the ArrayAdapter
        if (0 < pairedDevices.size) {

            binding.textPairedDevices.visibility = View.VISIBLE

            var deviceCount = 0
            for (device in pairedDevices) {
                Log.e("krm0219", "device name : " + device.name)

                if (device.name != null && device.name.contains(BLUETOOTH_SCANNER)) {
                    mPairedDevicesArrayAdapter!!.add("${device.name}\n${device.address}")
                    deviceCount++
                }
            }

            if (deviceCount < 1) {

                mPairedDevicesArrayAdapter!!.add(resources.getText(R.string.none_paired).toString())
            }
        } else {

            mPairedDevicesArrayAdapter!!.add(resources.getText(R.string.none_paired).toString())
        }
    }


    /**
     * Start device discover with the BluetoothAdapter
     */
    private fun doDiscovery() {

        // Indicate scanning in the title
        binding.progressBar.visibility = View.VISIBLE
        //   setProgressBarIndeterminateVisibility(true)
        setTitle(R.string.scanning)

        // Turn on sub-title for new devices
        binding.textOtherDevices.visibility = View.VISIBLE

        // If we're already discovering, stop it
        if (mBtAdapter!!.isDiscovering) {
            mBtAdapter!!.cancelDiscovery()
        }
        mNewDevicesArrayAdapter!!.clear()
        mNewDevicesArrayAdapter!!.notifyDataSetChanged()

        // Request discover from BluetoothAdapter
        mBtAdapter!!.startDiscovery()
    }


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                // When discovery finds a device

                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                    Log.e("krm0219", "mReceiver  device name : " + device.name)

                    if (device.name != null && device.name.contains(BLUETOOTH_SCANNER)) {
                        mNewDevicesArrayAdapter!!.add(" ${device.name}\n${device.address}")
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {

                // When discovery is finished, change the Activity title
                binding.progressBar.visibility = View.GONE
                //    setProgressBarIndeterminateVisibility(false)
                setTitle(R.string.select_device)

                if (mNewDevicesArrayAdapter!!.count == 0) {

                    mNewDevicesArrayAdapter!!.add(resources.getText(R.string.none_found).toString())
                }
            }
        }
    }


    // The on-click listener for all devices in the ListViews
    private val mDeviceClickListener = OnItemClickListener { _, v, _, _ ->

        // Get the device MAC address, which is the last 17 chars in the View
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)

        if (address.contains(":")) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter!!.cancelDiscovery()

            // Create the result Intent and include the MAC address
            // Set result and finish this Activity
            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)
            setResult(RESULT_OK, intent)
            finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }
        // Unregister broadcast listeners
        unregisterReceiver(mReceiver)
    }

}