package com.giosis.library.setting.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.library.R
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.PermissionActivity
import com.giosis.library.util.PermissionChecker
import kotlinx.android.synthetic.main.activity_printer_setting.*
import kotlinx.android.synthetic.main.top_title.*
import java.util.*

/**
 * @editor krm0219
 *
 *
 * 1. 프린터 페어링 (Available Devices > Paired Devices => 여러개 가능)
 * 2. 프린터 연결 (Paired Devices > Connected Device => 1개 가능)
 *
 *
 */

class PrinterSettingActivity : CommonActivity() {
    private val TAG = "PrinterSettingActivity"

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION)
    }

    // connect
    var socket: BluetoothSocket? = null

    var connectedItem = ArrayList<PrinterDeviceItem>()
    var pairedItems = ArrayList<PrinterDeviceItem>()
    var newDeviceItems = ArrayList<PrinterDeviceItem>()

    var REQUEST_ENABLE_BT = 10001
    var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_setting)

        text_top_title.setText(R.string.text_title_printer_setting)
        layout_top_back.setOnClickListener {
            finish()
        }

        val color = resources.getColor(R.color.color_4fb648)
        progress_available_devices.isIndeterminate = true
        progress_available_devices.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)

        /* if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
            BluetoothDeviceData.availableProgress.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP);
        } else {
            BluetoothDeviceData.availableProgress.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }*/

        img_available_refresh.setOnClickListener {
            discoveryDevice()
        }

        val checker = PermissionChecker(this)

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(*PERMISSIONS)) {
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {
            checkBluetoothState()
        }
    }

    private fun checkBluetoothState() {

        // Bluetooth 지원 여부 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Bluetooth 지원하지 않음
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, resources.getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_SHORT).show()
        } else {

            // Bluetooth 지원 && 비활성화 상태
            if (!mBluetoothAdapter!!.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BT)
            } else {

                // Bluetooth 지원 && 활성화 상태
                registerReceiverNHandler()
            }
        }
    }

    private var bluetoothDeviceReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {
                val action = intent.action
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                if (action != null && action != BluetoothDevice.ACTION_FOUND) {
                    Log.e("print", "$TAG  onReceive action : $action")
                }

                when (action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // 불루트스 기기 검색됨.

                        // 찾아진 디바이스가 페어링 되어 있지 않을 경우  >  [Available Devices] 리스트에 표시
                        if (device != null && device.bondState != BluetoothDevice.BOND_BONDED) {
                            if (device.name != null && device.name != "") {

                                // NOTIFICATION.  2019.11 - Print 기계만 선택되도록 'Major' 추가   (Q80 프린트의 경우 IMAGING, UNCATEGORIZED 두가지로 검색됨)
                                if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING) {
                                    Log.e("print", TAG + "  ACTION_FOUND Not Pairing  " + device.name + "  " + device.address +
                                            " / " + device.bluetoothClass.deviceClass + " / " + device.bluetoothClass.majorDeviceClass)

                                    // 동일한 주소가 이미 리스트에 들어 있는지 확인! 한번만 리스트에 넣기 위한 코드
                                    var position = -1
                                    var i = 0
                                    while (i < newDeviceItems.size) {
                                        if (newDeviceItems[i].deviceAddress == device.address) {
                                            position = i
                                            break
                                        }
                                        i++
                                    }
                                    if (position < 0) {
                                        newDeviceItems.add(PrinterDeviceItem(device.name, device.address, false, false))
                                        printerAvailableListAdapter?.notifyDataSetChanged()
                                    }

                                    if (newDeviceItems.size == 0) {
                                        nullAvailableDevices()
                                    } else {
                                        notnullAvailableDevices()
                                    }
                                }
                            }
                        } else if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
                            // 찾아진 디바이스가 페어링 되어 있는 경우    Data update
                            var pairedPosition = -1
                            var connectedPosition = -1
                            run {
                                var i = 0
                                while (i < pairedItems.size) {
                                    if (pairedItems[i].deviceAddress == device.address) {
                                        pairedPosition = i
                                        break
                                    }
                                    i++
                                }
                            }
                            var i = 0
                            while (i < connectedItem.size) {
                                if (connectedItem[i].deviceAddress == device.address) {
                                    connectedPosition = i
                                    break
                                }
                                i++
                            }

                            Log.e("print", TAG + "  ACTION_FOUND Pairing  " + pairedPosition + " / " + connectedPosition + " / "
                                    + device.name + " / " + device.address +
                                    " / " + device.bluetoothClass.deviceClass + " / " + device.bluetoothClass.majorDeviceClass)

                            if (0 <= pairedPosition) {
                                pairedItems[pairedPosition].isFound = true
                                printerPairedListAdapter?.notifyDataSetChanged()

                            } else if (0 <= connectedPosition) {
                                connectedItem[connectedPosition].isFound = true
                                connectedItem[connectedPosition].isConnected = true
                                printerConnectedListAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        progress_available_devices.visibility = View.VISIBLE
                        img_available_refresh.visibility = View.GONE
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        // 블루투스 기기 검색 종료
                        progress_available_devices.visibility = View.GONE
                        img_available_refresh.visibility = View.VISIBLE

                        if (newDeviceItems.size == 0) {
                            nullAvailableDevices()
                        } else {
                            notnullAvailableDevices()
                        }

                        if (pairedItems.size == 0) {
                            nullPairedDevices()
                        } else {
                            notnullPairedDevices()
                        }

                        if (connectedItem.size == 0) {
                            nullConnectedDevice()
                        } else {
                            notnullConnectedDevice()
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        // 블루투스 기기 연결 끊어짐.

                        // [Connected Device] 연결이 끊어졌을 때 (전원 OFF)
                        if (socket != null) {

                            Toast.makeText(context, device!!.name + " " + resources.getString(R.string.msg_is_disconnected), Toast.LENGTH_SHORT)
                                    .show()

                            try {
                                socket!!.close()
                                socket = null
                               BluetoothDeviceData.connectedPrinterAddress = null
                                connectedItem.clear()
                                nullConnectedDevice()

                                // [Paired Devices] 추가
                                pairedItems.add(PrinterDeviceItem(device.name, device.address, false, false))
                                printerPairedListAdapter?.notifyDataSetChanged()
                                notnullPairedDevices()
                            } catch (e: Exception) {
                                Log.e("Exception", "$TAG  ACTION_ACL_DISCONNECTED  Exception : $e")
                            }
                        } else if (BluetoothDeviceData.connectedPrinterAddress != null) {
                            // [Connected Device] 연결이 끊어졌을 때 ('Disconnect')
                            Toast.makeText(context, device!!.name + " " + resources.getString(R.string.msg_is_disconnected), Toast.LENGTH_SHORT).show()
                            Log.e("print", "$TAG  Disconnect Button click")
                            BluetoothDeviceData.connectedPrinterAddress = null

                            // [Connected Device] 에서 삭제
                            connectedItem.clear()
                            nullConnectedDevice()

                            // [Paired Devices] 추가
                            pairedItems.add(PrinterDeviceItem(device.name, device.address, true, false))
                            printerPairedListAdapter?.notifyDataSetChanged()
                            notnullPairedDevices()
                        }
                    }
                    BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                        // 기기 Pairing 요구
                        Toast.makeText(context, resources.getString(R.string.msg_pairing_requested), Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        // 기기 Pairing 상태 변화
                        Log.e("print", TAG + "  ACTION_BOND_STATE_CHANGED  : " + device!!.bondState)
                        val bundle = Bundle()
                        bundle.putString(BluetoothDeviceData.DEVICE_ID, device.address)
                        if (device.bondState == BluetoothDevice.BOND_BONDED) {
                            val message = bluetoothDeviceHandler.obtainMessage(BluetoothDeviceData.CONN_STATE_PAIRED)
                            message.data = bundle
                            bluetoothDeviceHandler.sendMessage(message)
                        } else if (device.bondState == BluetoothDevice.BOND_NONE) {
                            val message = bluetoothDeviceHandler.obtainMessage(BluetoothDeviceData.CONN_STATE_UNPAIRED)
                            message.data = bundle
                            bluetoothDeviceHandler.sendMessage(message)
                        }
                    }
                    BluetoothDeviceData.ACTION_CONNECT_STATE -> {
                        val state = intent.getIntExtra(BluetoothDeviceData.STATE, -1)
                        val deviceAddress = intent.getStringExtra(BluetoothDeviceData.DEVICE_ID)
                        var deviceName = intent.getStringExtra(BluetoothDeviceData.DEVICE_NAME)
                        try {
                            val method = device!!.javaClass.getMethod("getAliasName")
                            if (method != null) {
                                deviceName = method.invoke(device) as String
                            }
                        } catch (e: Exception) {
                        }
                        when (state) {
                            BluetoothDeviceData.CONN_STATE_CONNECTED -> {
                                // 'Connect'
                                Log.e("print", "$TAG  CONN_STATE_CONNECTED  Connect : $deviceName")
                                Toast.makeText(context, deviceName + " " + resources.getString(R.string.msg_is_connected), Toast.LENGTH_SHORT).show()

                                // [Paired Devices] 에서 삭제
                                if (pairedItems != null && 0 < pairedItems.size) {
                                    var position = -1
                                    var i = 0
                                    while (i < pairedItems.size) {
                                        if (pairedItems[i].deviceAddress == deviceAddress) {
                                            position = i
                                            break
                                        }
                                        i++
                                    }
                                    if (-1 < position) {
                                        pairedItems.removeAt(position)
                                        printerPairedListAdapter?.notifyDataSetChanged()
                                    }
                                }
                                if (pairedItems.size == 0) {
                                    nullPairedDevices()
                                } else {
                                    notnullPairedDevices()
                                }

                                // [Connected Device] 추가
                                connectedItem.add(PrinterDeviceItem(deviceName!!, deviceAddress!!, true, true))
                                printerConnectedListAdapter?.notifyDataSetChanged()
                                BluetoothDeviceData.connectedPrinterAddress = deviceAddress
                                notnullConnectedDevice()
                            }

                            BluetoothDeviceData.CONN_STATE_DISCONNECT -> {
                                // 'Disconnect'
                                Log.e("print", "$TAG  CONN_STATE_CONNECTED  Disconnect : $deviceName")
                                Toast.makeText(context, deviceName + " " + resources.getString(R.string.msg_is_disconnecting), Toast.LENGTH_LONG).show()
                            }
                            else -> {
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }


    private var bluetoothDeviceHandler = Handler(Looper.getMainLooper()) {

        when (it.what) {

            BluetoothDeviceData.CONN_STATE_PAIRED -> {
                Log.e("print", "$TAG  BOND_STATE_CHANGED > PAIRED")
                val bundle = it.data
                val macAddress = bundle.getString(BluetoothDeviceData.DEVICE_ID)

                if (mBluetoothAdapter != null) {
                    val device = mBluetoothAdapter!!.getRemoteDevice(macAddress)
                    Toast.makeText(this, device.name + "  " + resources.getString(R.string.msg_is_paired), Toast.LENGTH_SHORT).show()
                    pairedItems.add(PrinterDeviceItem(device.name, device.address, true, false))
                    printerPairedListAdapter?.notifyDataSetChanged()

                    // 2019.11 - 갱신화면 안보임 수정
                    notnullPairedDevices()
                    newDeviceItems.clear()
                    if (mBluetoothAdapter!!.isDiscovering) {
                        mBluetoothAdapter!!.cancelDiscovery()
                    }
                    mBluetoothAdapter!!.startDiscovery()
                }
                true

            }

            BluetoothDeviceData.CONN_STATE_UNPAIRED -> {
                Log.e("print", "$TAG  BOND_STATE_CHANGED > UNPAIRED")
                val bundle = it.data
                val macAddress = bundle.getString(BluetoothDeviceData.DEVICE_ID)

                if (mBluetoothAdapter != null) {
                    val device = mBluetoothAdapter!!.getRemoteDevice(macAddress)
                    var deviceName = device.name
                    try {
                        val method = device.javaClass.getMethod("getAliasName")
                        deviceName = method.invoke(device) as String
                    } catch (e: Exception) {
                        deviceName = device.name
                    }
                    Toast.makeText(this, deviceName + "  " + resources.getString(R.string.msg_is_unpaired), Toast.LENGTH_SHORT).show()
                }

                var position = -1
                var i = 0
                while (i < pairedItems.size) {
                    if (pairedItems[i].deviceAddress == macAddress) {
                        position = i
                        break
                    }
                    i++
                }
                if (-1 < position) {
                    pairedItems.removeAt(position)
                    printerPairedListAdapter?.notifyDataSetChanged()
                }
                if (pairedItems.size == 0) {
                    nullPairedDevices()
                }
                newDeviceItems.clear()

                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter!!.isDiscovering) {
                        mBluetoothAdapter!!.cancelDiscovery()
                    }
                    mBluetoothAdapter!!.startDiscovery()
                }
                true
            }
            else -> {
                true
            }
        }

    }

    private fun registerReceiverNHandler() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDeviceData.ACTION_CONNECT_STATE) // "action_connect_state"
        filter.addAction(BluetoothDevice.ACTION_FOUND) // 기기 검색됨
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) // 기기 검색 시작
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) // 기기 검색 종료
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) // 연결 끊김 확인
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST) // 기기 Pairing 요구
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) // 기기 Pairing 상태 변화
        registerReceiver(bluetoothDeviceReceiver, filter)
    }

    var printerConnectedListAdapter: PrinterConnectedListAdapter? = null
    var printerPairedListAdapter: PrinterPairedListAdapter? = null
    var printerAvailableListAdapter: PrinterAvailableListAdapter? = null

    override fun onResume() {
        super.onResume()
        connectedItem = ArrayList()
        pairedItems = ArrayList()
        newDeviceItems = ArrayList()

        val listener: PairedAdapterListener = object : PairedAdapterListener {

            override fun getRemoteDevice(address: String): BluetoothDevice? {
                return if (mBluetoothAdapter != null) {
                    mBluetoothAdapter!!.getRemoteDevice(address)
                } else {
                    null
                }
            }

            override fun getConnectedPrinterAddress(): String? {
                return BluetoothDeviceData.connectedPrinterAddress
            }

            override fun closeSocket() {
                socket?.close()
                socket = null
            }

            override fun getSocket(): BluetoothSocket? {
                return socket
            }

            override fun cancelDiscovery() {
                mBluetoothAdapter?.cancelDiscovery()
            }

            override fun connectDevice(position: Int, address: String, name: String): Boolean {

                // 2019.11 - 1개의 프린터만 [Connected Devices]로 올릴 수 있도록 수정
                // List - 'Print Label'을 누를때, 마지막에 연결된 프린터로 프린트 될 수 있도록...

                //  [Connected Device] 가 있을 때
                if (socket != null) {
                    Toast.makeText(this@PrinterSettingActivity, resources.getString(R.string.msg_only_one_device_connected), Toast.LENGTH_SHORT).show()

                } else if (BluetoothDeviceData.connectedPrinterAddress != null) {
                    Toast.makeText(this@PrinterSettingActivity, resources.getString(R.string.msg_disconnecting_old_device), Toast.LENGTH_SHORT).show()

                } else {

                    val device = mBluetoothAdapter?.getRemoteDevice(address)
                    try {

                        if (device != null) {
                            socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothDeviceData.MY_UUID_INSECURE)
                            socket?.connect()

                            val intent = Intent(BluetoothDeviceData.ACTION_CONNECT_STATE)
                            intent.putExtra(BluetoothDeviceData.STATE, BluetoothDeviceData.CONN_STATE_CONNECTED)
                            intent.putExtra(BluetoothDeviceData.DEVICE_ID, address)
                            intent.putExtra(BluetoothDeviceData.DEVICE_NAME, name)
                            this@PrinterSettingActivity.sendBroadcast(intent)
                        }

                    } catch (e: Exception) {
                        try {
                            socket?.close()
                            socket = null

                        } catch (ee: Exception) {
                            Log.e("Exception", "Connect Exception2 : $e")
                        }
                        Log.e("Exception", "Connect Exception : $e")

                        return false
                    }
                }

                return true
            }

        }

        printerConnectedListAdapter = PrinterConnectedListAdapter(this@PrinterSettingActivity, connectedItem, listener)
        printerPairedListAdapter = PrinterPairedListAdapter(this@PrinterSettingActivity, pairedItems, listener)
        printerAvailableListAdapter = PrinterAvailableListAdapter(this@PrinterSettingActivity, newDeviceItems, listener)

        list_setting_printer_paired_device.adapter = printerPairedListAdapter
        list_setting_printer_connected_device.adapter = printerConnectedListAdapter
        list_setting_printer_available_device.adapter = printerAvailableListAdapter

        if (mBluetoothAdapter != null) {
            discoveryDevice()
            deviceList
        }
    }

    private fun discoveryDevice() {

        // startDiscovery()를 호출하여 디바이스 검색을 시작합니다.
        // 만약 이미 검색중이라면 cancelDiscovery()를 호출하여 검색을 멈춘 후 다시 검색해야 합니다.
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
        mBluetoothAdapter!!.startDiscovery()

    }

    private val deviceList: Unit
        private get() {
            val pairedDevices = mBluetoothAdapter!!.bondedDevices
            Log.e("print", TAG + "  getDeviceList  " + BluetoothDeviceData.connectedPrinterAddress + " / " + pairedDevices.size)
            if (0 < pairedDevices.size) {
                for (device in pairedDevices) {
                    var deviceName = device.name
                    val deviceAddress = device.address
                    try {
                        val method = device.javaClass.getMethod("getAliasName")
                        if (method != null) {
                            deviceName = method.invoke(device) as String
                        }
                        if (deviceName == null || deviceName == "") deviceName = device.name
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (BluetoothDeviceData.connectedPrinterAddress != null && BluetoothDeviceData.connectedPrinterAddress == deviceAddress) {

                        Log.e("print", TAG + "  connected Device : " + device.name + " / " + device.address)

                        if (socket != null) {
                            connectedItem.add(PrinterDeviceItem(deviceName!!, deviceAddress, true, true))
                            printerConnectedListAdapter!!.notifyDataSetChanged()

                        } else {
                            try {
                                socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothDeviceData.MY_UUID_INSECURE)
                                socket?.connect()

                                val intent = Intent(BluetoothDeviceData.ACTION_CONNECT_STATE)
                                intent.putExtra(BluetoothDeviceData.STATE, BluetoothDeviceData.CONN_STATE_CONNECTED)
                                intent.putExtra(BluetoothDeviceData.DEVICE_ID, deviceAddress)
                                intent.putExtra(BluetoothDeviceData.DEVICE_NAME, deviceName)
                                sendBroadcast(intent)

                            } catch (e: Exception) {
                                Toast.makeText(this, "Connect Error.\nException : $e", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("print", TAG + "  paired Device : " + device.name + " / " + device.address)
                        pairedItems.add(PrinterDeviceItem(deviceName!!, deviceAddress, false, false))
                        printerPairedListAdapter!!.notifyDataSetChanged()
                    }
                }

                if (pairedItems.size == 0) {
                    nullPairedDevices()
                } else {
                    notnullPairedDevices()
                }
                if (connectedItem.size == 0) {
                    nullConnectedDevice()
                } else {
                    notnullConnectedDevice()
                }
            }
        }

    fun nullAvailableDevices() {
        list_setting_printer_available_device.visibility = View.GONE
        layout_setting_printer_no_available_device.visibility = View.VISIBLE
    }

    fun notnullAvailableDevices() {
        list_setting_printer_available_device.visibility = View.VISIBLE
        layout_setting_printer_no_available_device.visibility = View.GONE
    }

    fun nullPairedDevices() {
        list_setting_printer_paired_device.visibility = View.GONE
        layout_setting_printer_no_paired_device.visibility = View.VISIBLE
    }

    fun notnullPairedDevices() {
        list_setting_printer_paired_device.visibility = View.VISIBLE
        layout_setting_printer_no_paired_device.visibility = View.GONE
    }

    fun nullConnectedDevice() {
        list_setting_printer_connected_device.visibility = View.GONE
        layout_setting_printer_no_connected_device.visibility = View.VISIBLE
    }

    fun notnullConnectedDevice() {
        list_setting_printer_connected_device.visibility = View.VISIBLE
        layout_setting_printer_no_connected_device.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {

                // k. 블루투스 승인 요청 'YES'
                Toast.makeText(this, resources.getString(R.string.msg_bluetooth_enabled), Toast.LENGTH_SHORT).show()
                checkBluetoothState()
            } else {

                // k. 블루투스 승인 요청 'NO'
                Toast.makeText(this, resources.getString(R.string.msg_bluetooth_not_enabled), Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == BluetoothDeviceData.REQUEST_RENAME_PAIR_DEVICE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, resources.getString(R.string.msg_bluetooth_device_rename), Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", "$TAG   onActivityResult  PERMISSIONS_GRANTED")
                checkBluetoothState()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mBluetoothAdapter?.cancelDiscovery()

            socket?.close()
            socket = null

            unregisterReceiver(bluetoothDeviceReceiver)

            Log.e("print", "$TAG  stopBluetoothService")
        } catch (e: Exception) {
            Log.e("Exception", "$TAG  stopBluetoothService  Exception : $e")
        }
    }

}