package com.giosis.library.setting.bluetooth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// 엑티비티 않으로 이동 사용하지 않음
class BluetoothDeviceBroadcastReceiver(var context: Context)
    : BroadcastReceiver() {

    var TAG = "BluetoothDeviceBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
//        val action = intent.action
//        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//
//        if (action != null && action != BluetoothDevice.ACTION_FOUND) Log.e("print", "$TAG  onReceive action : $action")
//
//        if (action != null) {
//            when (action) {
//                BluetoothDevice.ACTION_FOUND -> {
//                    // 불루트스 기기 검색됨.
//
//                    // 찾아진 디바이스가 페어링 되어 있지 않을 경우  >  [Available Devices] 리스트에 표시
//                    if (device != null && device.bondState != BluetoothDevice.BOND_BONDED) {
//                        if (device.name != null && device.name != "") {
//
//                            // NOTIFICATION.  2019.11 - Print 기계만 선택되도록 'Major' 추가   (Q80 프린트의 경우 IMAGING, UNCATEGORIZED 두가지로 검색됨)
//                            if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING) {
//                                Log.e("print", TAG + "  ACTION_FOUND Not Pairing  " + device.name + "  " + device.address +
//                                        " / " + device.bluetoothClass.deviceClass + " / " + device.bluetoothClass.majorDeviceClass)
//
//                                // 동일한 주소가 이미 리스트에 들어 있는지 확인! 한번만 리스트에 넣기 위한 코드
//                                var position = -1
//                                var i = 0
//                                while (i < PrinterSettingActivity.newDeviceItems.size) {
//                                    if (PrinterSettingActivity.newDeviceItems[i].deviceAddress == device.address) {
//                                        position = i
//                                        break
//                                    }
//                                    i++
//                                }
//                                if (position < 0) {
//                                    PrinterSettingActivity.newDeviceItems.add(PrinterDeviceItem(device.name, device.address, false, false))
//                                    BluetoothDeviceData.printerAvailableListAdapter.notifyDataSetChanged()
//                                }
//                                //
//                                if (PrinterSettingActivity.newDeviceItems.size == 0) {
//                                    PrinterSettingActivity.nullAvailableDevices()
//                                } else {
//                                    PrinterSettingActivity.notnullAvailableDevices()
//                                }
//                            }
//                        }
//                    } else if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
//                        // 찾아진 디바이스가 페어링 되어 있는 경우    Data update
//                        var pairedPosition = -1
//                        var connectedPosition = -1
//                        run {
//                            var i = 0
//                            while (i < PrinterSettingActivity.pairedItems.size) {
//                                if (PrinterSettingActivity.pairedItems[i].deviceAddress == device.address) {
//                                    pairedPosition = i
//                                    break
//                                }
//                                i++
//                            }
//                        }
//                        var i = 0
//                        while (i < PrinterSettingActivity.connectedItem.size) {
//                            if (PrinterSettingActivity.connectedItem[i].deviceAddress == device.address) {
//                                connectedPosition = i
//                                break
//                            }
//                            i++
//                        }
//                        Log.e("print", TAG + "  ACTION_FOUND Pairing  " + pairedPosition + " / " + connectedPosition + " / "
//                                + device.name + " / " + device.address +
//                                " / " + device.bluetoothClass.deviceClass + " / " + device.bluetoothClass.majorDeviceClass)
//                        if (0 <= pairedPosition) {
//                            PrinterSettingActivity.pairedItems[pairedPosition].isFound = true
//                            BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged()
//                        } else if (0 <= connectedPosition) {
//                            PrinterSettingActivity.connectedItem[connectedPosition].isFound = true
//                            PrinterSettingActivity.connectedItem[connectedPosition].isConnected = true
//                            BluetoothDeviceData.printerConnectedListAdapter.notifyDataSetChanged()
//                        }
//                    }
//                }
//                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
//                    BluetoothDeviceData.availableProgress.visibility = View.VISIBLE
//                    BluetoothDeviceData.availableRefresh.visibility = View.GONE
//                }
//                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
//                    // 블루투스 기기 검색 종료
//                    BluetoothDeviceData.availableProgress.visibility = View.GONE
//                    BluetoothDeviceData.availableRefresh.visibility = View.VISIBLE
//                    if (PrinterSettingActivity.newDeviceItems.size == 0) {
//                        PrinterSettingActivity.nullAvailableDevices()
//                    } else {
//                        PrinterSettingActivity.notnullAvailableDevices()
//                    }
//                    if (PrinterSettingActivity.pairedItems.size == 0) {
//                        PrinterSettingActivity.nullPairedDevices()
//                    } else {
//                        PrinterSettingActivity.notnullPairedDevices()
//                    }
//                    if (PrinterSettingActivity.connectedItem.size == 0) {
//                        PrinterSettingActivity.nullConnectedDevice()
//                    } else {
//                        PrinterSettingActivity.notnullConnectedDevice()
//                    }
//                }
//                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
//                    // 블루투스 기기 연결 끊어짐.
//
//                    // [Connected Device] 연결이 끊어졌을 때 (전원 OFF)
//                    if (BluetoothDeviceData.socket != null) {
//                        Toast.makeText(context, device!!.name + " " + context.resources.getString(R.string.msg_is_disconnected), Toast.LENGTH_SHORT).show()
//                        try {
//                            BluetoothDeviceData.socket.close()
//                            BluetoothDeviceData.socket = null
//                            BluetoothDeviceData.connectedPrinterAddress = null
//                            PrinterSettingActivity.connectedItem.clear()
//                            PrinterSettingActivity.nullConnectedDevice()
//
//                            // [Paired Devices] 추가
//                            PrinterSettingActivity.pairedItems.add(PrinterDeviceItem(device.name, device.address, false, false))
//                            BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged()
//                            PrinterSettingActivity.notnullPairedDevices()
//                        } catch (e: Exception) {
//                            Log.e("Exception", "$TAG  ACTION_ACL_DISCONNECTED  Exception : $e")
//                        }
//                    } else if (BluetoothDeviceData.connectedPrinterAddress != null) {
//                        // [Connected Device] 연결이 끊어졌을 때 ('Disconnect')
//                        Toast.makeText(context, device!!.name + " " + context.resources.getString(R.string.msg_is_disconnected), Toast.LENGTH_SHORT).show()
//                        Log.e("print", "$TAG  Disconnect Button click")
//                        BluetoothDeviceData.connectedPrinterAddress = null
//
//                        // [Connected Device] 에서 삭제
//                        PrinterSettingActivity.connectedItem.clear()
//                        PrinterSettingActivity.nullConnectedDevice()
//
//                        // [Paired Devices] 추가
//                        PrinterSettingActivity.pairedItems.add(PrinterDeviceItem(device.name, device.address, true, false))
//                        BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged()
//                        PrinterSettingActivity.notnullPairedDevices()
//                    }
//                }
//                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
//                    // 기기 Pairing 요구
//                    Toast.makeText(context, context.resources.getString(R.string.msg_pairing_requested), Toast.LENGTH_SHORT).show()
//                }
//                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
//                    // 기기 Pairing 상태 변화
//                    Log.e("print", TAG + "  ACTION_BOND_STATE_CHANGED  : " + device!!.bondState)
//                    val bundle = Bundle()
//                    bundle.putString(BluetoothDeviceData.DEVICE_ID, device.address)
//                    if (device.bondState == BluetoothDevice.BOND_BONDED) {
//                        val message = BluetoothDeviceData.bluetoothDeviceHandler.obtainMessage(BluetoothDeviceData.CONN_STATE_PAIRED)
//                        message.data = bundle
//                        BluetoothDeviceData.bluetoothDeviceHandler.sendMessage(message)
//                    } else if (device.bondState == BluetoothDevice.BOND_NONE) {
//                        val message = BluetoothDeviceData.bluetoothDeviceHandler.obtainMessage(BluetoothDeviceData.CONN_STATE_UNPAIRED)
//                        message.data = bundle
//                        BluetoothDeviceData.bluetoothDeviceHandler.sendMessage(message)
//                    }
//                }
//                BluetoothDeviceData.ACTION_CONNECT_STATE -> {
//                    val state = intent.getIntExtra(BluetoothDeviceData.STATE, -1)
//                    val deviceAddress = intent.getStringExtra(BluetoothDeviceData.DEVICE_ID)
//                    var deviceName = intent.getStringExtra(BluetoothDeviceData.DEVICE_NAME)
//                    try {
//                        val method = device!!.javaClass.getMethod("getAliasName")
//                        if (method != null) {
//                            deviceName = method.invoke(device) as String
//                        }
//                    } catch (e: Exception) {
//                    }
//                    when (state) {
//                        BluetoothDeviceData.CONN_STATE_CONNECTED -> {
//                            // 'Connect'
//                            Log.e("print", "$TAG  CONN_STATE_CONNECTED  Connect : $deviceName")
//                            Toast.makeText(context, deviceName + " " + context.resources.getString(R.string.msg_is_connected), Toast.LENGTH_SHORT).show()
//
//                            // [Paired Devices] 에서 삭제
//                            if (PrinterSettingActivity.pairedItems != null && 0 < PrinterSettingActivity.pairedItems.size) {
//                                var position = -1
//                                var i = 0
//                                while (i < PrinterSettingActivity.pairedItems.size) {
//                                    if (PrinterSettingActivity.pairedItems[i].deviceAddress == deviceAddress) {
//                                        position = i
//                                        break
//                                    }
//                                    i++
//                                }
//                                if (-1 < position) {
//                                    PrinterSettingActivity.pairedItems.removeAt(position)
//                                    BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged()
//                                }
//                            }
//                            if (PrinterSettingActivity.pairedItems.size == 0) {
//                                PrinterSettingActivity.nullPairedDevices()
//                            } else {
//                                PrinterSettingActivity.notnullPairedDevices()
//                            }
//
//                            // [Connected Device] 추가
//                            PrinterSettingActivity.connectedItem.add(PrinterDeviceItem(deviceName!!, deviceAddress!!, true, true))
//                            BluetoothDeviceData.printerConnectedListAdapter.notifyDataSetChanged()
//                            BluetoothDeviceData.connectedPrinterAddress = deviceAddress
//                            PrinterSettingActivity.notnullConnectedDevice()
//                        }
//                        BluetoothDeviceData.CONN_STATE_DISCONNECT -> {
//                            // 'Disconnect'
//                            Log.e("print", "$TAG  CONN_STATE_CONNECTED  Disconnect : $deviceName")
//                            Toast.makeText(context, deviceName + " " + context.resources.getString(R.string.msg_is_disconnecting), Toast.LENGTH_LONG).show()
//                        }
//                        else -> {
//                        }
//                    }
//                }
//                else -> {
//                }
//            }
//        }
    }
}