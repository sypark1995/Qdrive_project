package com.giosis.util.qdrive.settings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;

import java.lang.reflect.Method;

public class BluetoothDeviceBroadcastReceiver extends BroadcastReceiver {
    String TAG = "BluetoothDeviceBroadcastReceiver";

    Context context;

    public BluetoothDeviceBroadcastReceiver(Context context) {

        this.context = context;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (action != null && !action.equals(BluetoothDevice.ACTION_FOUND))
            Log.e("print", TAG + "  onReceive action : " + action);

        if (action != null) {
            switch (action) {
                case BluetoothDevice.ACTION_FOUND: {     // 불루트스 기기 검색됨.

                    // 찾아진 디바이스가 페어링 되어 있지 않을 경우  >  [Available Devices] 리스트에 표시
                    if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (device.getName() != null && !device.getName().equals("")) {

                            // NOTIFICATION.  2019.11 - Print 기계만 선택되도록 'Major' 추가   (Q80 프린트의 경우 IMAGING, UNCATEGORIZED 두가지로 검색됨)
                            if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {

                                Log.e("print", TAG + "  ACTION_FOUND Not Pairing  " + device.getName() + "  " + device.getAddress() +
                                        " / " + device.getBluetoothClass().getDeviceClass() + " / " + device.getBluetoothClass().getMajorDeviceClass());

                                // 동일한 주소가 이미 리스트에 들어 있는지 확인! 한번만 리스트에 넣기 위한 코드
                                int position = -1;

                                for (int i = 0; i < PrinterSettingActivity.newDeviceItems.size(); i++) {
                                    if ((PrinterSettingActivity.newDeviceItems.get(i).getDeviceAddress()).equals(device.getAddress())) {
                                        position = i;
                                        break;
                                    }
                                }

                                if (position < 0) {

                                    PrinterSettingActivity.newDeviceItems.add(new PrinterDeviceItem(device.getName(), device.getAddress(), false, false));
                                    BluetoothDeviceData.printerAvailableListAdapter.notifyDataSetChanged();
                                }
                                //

                                if (PrinterSettingActivity.newDeviceItems.size() == 0) {

                                    PrinterSettingActivity.nullAvailableDevices();
                                } else {

                                    PrinterSettingActivity.notnullAvailableDevices();
                                }
                            }
                        }
                    } else if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        // 찾아진 디바이스가 페어링 되어 있는 경우    Data update

                        int pairedPosition = -1;
                        int connectedPosition = -1;

                        for (int i = 0; i < PrinterSettingActivity.pairedItems.size(); i++) {
                            if ((PrinterSettingActivity.pairedItems.get(i).getDeviceAddress()).equals(device.getAddress())) {
                                pairedPosition = i;
                                break;
                            }
                        }

                        for (int i = 0; i < PrinterSettingActivity.connectedItem.size(); i++) {
                            if ((PrinterSettingActivity.connectedItem.get(i).getDeviceAddress()).equals(device.getAddress())) {
                                connectedPosition = i;
                                break;
                            }
                        }

                        Log.e("print", TAG + "  ACTION_FOUND Pairing  " + pairedPosition + " / " + connectedPosition + " / "
                                + device.getName() + " / " + device.getAddress() +
                                " / " + device.getBluetoothClass().getDeviceClass() + " / " + device.getBluetoothClass().getMajorDeviceClass());

                        if (0 <= pairedPosition) {

                            PrinterSettingActivity.pairedItems.get(pairedPosition).setFound(true);
                            BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                        } else if (0 <= connectedPosition) {

                            PrinterSettingActivity.connectedItem.get(connectedPosition).setFound(true);
                            PrinterSettingActivity.connectedItem.get(connectedPosition).setConnected(true);
                            BluetoothDeviceData.printerConnectedListAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED: {

                    BluetoothDeviceData.availableProgress.setVisibility(View.VISIBLE);
                    BluetoothDeviceData.availableRefresh.setVisibility(View.GONE);
                }
                break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {   // 블루투스 기기 검색 종료

                    BluetoothDeviceData.availableProgress.setVisibility(View.GONE);
                    BluetoothDeviceData.availableRefresh.setVisibility(View.VISIBLE);

                    if (PrinterSettingActivity.newDeviceItems.size() == 0) {
                        PrinterSettingActivity.nullAvailableDevices();
                    } else {
                        PrinterSettingActivity.notnullAvailableDevices();
                    }

                    if (PrinterSettingActivity.pairedItems.size() == 0) {
                        PrinterSettingActivity.nullPairedDevices();
                    } else {
                        PrinterSettingActivity.notnullPairedDevices();
                    }

                    if (PrinterSettingActivity.connectedItem.size() == 0) {
                        PrinterSettingActivity.nullConnectedDevice();
                    } else {
                        PrinterSettingActivity.notnullConnectedDevice();
                    }
                }
                break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED: {      // 블루투스 기기 연결 끊어짐.

                    // [Connected Device] 연결이 끊어졌을 때 (전원 OFF)
                    if (BluetoothDeviceData.socket != null) {

                        Toast.makeText(context, device.getName() + " " + context.getResources().getString(R.string.msg_is_disconnected), Toast.LENGTH_SHORT).show();

                        try {

                            BluetoothDeviceData.socket.close();
                            BluetoothDeviceData.socket = null;
                            BluetoothDeviceData.connectedPrinterAddress = null;

                            PrinterSettingActivity.connectedItem.clear();
                            PrinterSettingActivity.nullConnectedDevice();

                            // [Paired Devices] 추가
                            PrinterSettingActivity.pairedItems.add(new PrinterDeviceItem(device.getName(), device.getAddress(), false, false));
                            BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                            PrinterSettingActivity.notnullPairedDevices();
                        } catch (Exception e) {

                            Log.e("Exception", TAG + "  ACTION_ACL_DISCONNECTED  Exception : " + e.toString());
                        }
                    } else if (BluetoothDeviceData.connectedPrinterAddress != null) {
                        // [Connected Device] 연결이 끊어졌을 때 ('Disconnect')

                        Toast.makeText(context, device.getName() + " " + context.getResources().getString(R.string.msg_is_disconnected), Toast.LENGTH_SHORT).show();

                        Log.e("print", TAG + "  Disconnect Button click");
                        BluetoothDeviceData.connectedPrinterAddress = null;

                        // [Connected Device] 에서 삭제
                        PrinterSettingActivity.connectedItem.clear();
                        PrinterSettingActivity.nullConnectedDevice();

                        // [Paired Devices] 추가
                        PrinterSettingActivity.pairedItems.add(new PrinterDeviceItem(device.getName(), device.getAddress(), true, false));
                        BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                        PrinterSettingActivity.notnullPairedDevices();
                    }
                }
                break;

                case BluetoothDevice.ACTION_PAIRING_REQUEST: {      // 기기 Pairing 요구

                    Toast.makeText(context, context.getResources().getString(R.string.msg_pairing_requested), Toast.LENGTH_SHORT).show();
                }
                break;

                // NOTIFICATION. [Available Devices] 에서 'Connect' 버튼 (11>12)  //  [Paired Devices] 에서 'Delete' 버튼 (10)
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {    // 기기 Pairing 상태 변화
                    Log.e("print", TAG + "  ACTION_BOND_STATE_CHANGED  : " + device.getBondState());

                    Bundle bundle = new Bundle();
                    bundle.putString(BluetoothDeviceData.DEVICE_ID, device.getAddress());

                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

                        Message message = BluetoothDeviceData.bluetoothDeviceHandler.obtainMessage(BluetoothDeviceData.CONN_STATE_PAIRED);
                        message.setData(bundle);
                        BluetoothDeviceData.bluetoothDeviceHandler.sendMessage(message);
                    } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {

                        Message message = BluetoothDeviceData.bluetoothDeviceHandler.obtainMessage(BluetoothDeviceData.CONN_STATE_UNPAIRED);
                        message.setData(bundle);
                        BluetoothDeviceData.bluetoothDeviceHandler.sendMessage(message);
                    }
                }
                break;

                // NOTIFICATION.  [Paired Devices] 'Connect' 버튼  //  [Connected Device] 'Disconnect' 버튼
                case BluetoothDeviceData.ACTION_CONNECT_STATE: {

                    int state = intent.getIntExtra(BluetoothDeviceData.STATE, -1);
                    String deviceAddress = intent.getStringExtra(BluetoothDeviceData.DEVICE_ID);
                    String deviceName = intent.getStringExtra(BluetoothDeviceData.DEVICE_NAME);

                    try {
                        Method method = device.getClass().getMethod("getAliasName");
                        if (method != null) {
                            deviceName = (String) method.invoke(device);
                        }
                    } catch (Exception e) {

                    }

                    //    Log.e("print", TAG + "  ACTION_CONN_STATE  state : " + state);
                    switch (state) {

                        case BluetoothDeviceData.CONN_STATE_CONNECTED: { // 'Connect'
                            Log.e("print", TAG + "  CONN_STATE_CONNECTED  Connect : " + deviceName);

                            Toast.makeText(context, deviceName + " " + context.getResources().getString(R.string.msg_is_connected), Toast.LENGTH_SHORT).show();

                            // [Paired Devices] 에서 삭제
                            if (PrinterSettingActivity.pairedItems != null && 0 < PrinterSettingActivity.pairedItems.size()) {

                                int position = -1;

                                for (int i = 0; i < PrinterSettingActivity.pairedItems.size(); i++) {
                                    if ((PrinterSettingActivity.pairedItems.get(i).getDeviceAddress()).equals(deviceAddress)) {
                                        position = i;
                                        break;
                                    }
                                }

                                if (-1 < position) {

                                    PrinterSettingActivity.pairedItems.remove(position);
                                    BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                                }
                            }

                            if (PrinterSettingActivity.pairedItems.size() == 0) {

                                PrinterSettingActivity.nullPairedDevices();
                            } else {

                                PrinterSettingActivity.notnullPairedDevices();
                            }

                            // [Connected Device] 추가
                            PrinterSettingActivity.connectedItem.add(new PrinterDeviceItem(deviceName, deviceAddress, true, true));
                            BluetoothDeviceData.printerConnectedListAdapter.notifyDataSetChanged();

                            BluetoothDeviceData.connectedPrinterAddress = deviceAddress;
                            PrinterSettingActivity.notnullConnectedDevice();
                        }
                        break;

                        case BluetoothDeviceData.CONN_STATE_DISCONNECT: {  // 'Disconnect'
                            Log.e("print", TAG + "  CONN_STATE_CONNECTED  Disconnect : " + deviceName);

                            Toast.makeText(context, deviceName + " " + context.getResources().getString(R.string.msg_is_disconnecting), Toast.LENGTH_LONG).show();
                        }
                        break;

                        default:
                            break;
                    }
                }
                break;

                default:
                    break;
            }
        }
    }
}