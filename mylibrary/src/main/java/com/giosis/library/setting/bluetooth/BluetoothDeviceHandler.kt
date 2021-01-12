package com.giosis.library.setting.bluetooth

import android.content.Context
import android.os.Handler

// 엑티비티 않으로 이동 사용하지 않음
class BluetoothDeviceHandler internal constructor(private val context: Context) : Handler() {

    private val TAG = "BluetoothDeviceHandler"

//    override fun handleMessage(msg: Message) {
//        super.handleMessage(msg)
//        when (msg.what) {
//            BluetoothDeviceData.CONN_STATE_PAIRED -> {
//                Log.e("print", "$TAG  BOND_STATE_CHANGED > PAIRED")
//                val bundle = msg.data
//                val macAddress = bundle.getString(BluetoothDeviceData.DEVICE_ID)
//                val device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(macAddress)
//                Toast.makeText(context, device.name + "  " + context.resources.getString(R.string.msg_is_paired), Toast.LENGTH_SHORT).show()
//                PrinterSettingActivity.pairedItems.add(PrinterDeviceItem(device.name, device.address, true, false))
//                BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged()
//                // 2019.11 - 갱신화면 안보임 수정
//                PrinterSettingActivity.notnullPairedDevices()
//                PrinterSettingActivity.newDeviceItems.clear()
//                if (BluetoothDeviceData.mBluetoothAdapter.isDiscovering) {
//                    BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery()
//                }
//                BluetoothDeviceData.mBluetoothAdapter.startDiscovery()
//            }
//            BluetoothDeviceData.CONN_STATE_UNPAIRED -> {
//                Log.e("print", "$TAG  BOND_STATE_CHANGED > UNPAIRED")
//                val bundle = msg.data
//                val macAddress = bundle.getString(BluetoothDeviceData.DEVICE_ID)
//                val device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(macAddress)
//                var deviceName = device.name
//                try {
//                    val method = device.javaClass.getMethod("getAliasName")
//                    if (method != null) {
//                        deviceName = method.invoke(device) as String
//                    }
//                } catch (e: Exception) {
//                    deviceName = device.name
//                }
//                Toast.makeText(context, deviceName + "  " + context.resources.getString(R.string.msg_is_unpaired), Toast.LENGTH_SHORT).show()
//                var position = -1
//                var i = 0
//                while (i < PrinterSettingActivity.pairedItems.size) {
//                    if (PrinterSettingActivity.pairedItems[i].deviceAddress == macAddress) {
//                        position = i
//                        break
//                    }
//                    i++
//                }
//                if (-1 < position) {
//                    PrinterSettingActivity.pairedItems.removeAt(position)
//                    BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged()
//                }
//                if (PrinterSettingActivity.pairedItems.size == 0) {
//                    PrinterSettingActivity.nullPairedDevices()
//                }
//                PrinterSettingActivity.newDeviceItems.clear()
//                if (BluetoothDeviceData.mBluetoothAdapter.isDiscovering) {
//                    BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery()
//                }
//                BluetoothDeviceData.mBluetoothAdapter.startDiscovery()
//            }
//            else -> {
//            }
//        }
//    }
}