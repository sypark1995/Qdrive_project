package com.giosis.util.qdrive.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;

import java.lang.reflect.Method;


public class BluetoothDeviceHandler extends Handler {
    private String TAG = "BluetoothDeviceHandler";

    private Context context;

    BluetoothDeviceHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {

            // [Available Devices] > [Paired Devices] 이동
            case BluetoothDeviceData.CONN_STATE_PAIRED: {
                Log.e("print", TAG + "  BOND_STATE_CHANGED > PAIRED");

                Bundle bundle = msg.getData();
                String macAddress = bundle.getString(BluetoothDeviceData.DEVICE_ID);
                BluetoothDevice device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(macAddress);
                Toast.makeText(context, device.getName() + "  " + context.getResources().getString(R.string.msg_is_paired), Toast.LENGTH_SHORT).show();

                PrinterSettingActivity.pairedItems.add(new PrinterDeviceItem(device.getName(), device.getAddress(), true, false));
                BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                // 2019.11 - 갱신화면 안보임 수정
                PrinterSettingActivity.notnullPairedDevices();


                PrinterSettingActivity.newDeviceItems.clear();

                if (BluetoothDeviceData.mBluetoothAdapter.isDiscovering()) {
                    BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();
                }
                BluetoothDeviceData.mBluetoothAdapter.startDiscovery();

            }
            break;

            // [Paired Devices] 에서 삭제
            case BluetoothDeviceData.CONN_STATE_UNPAIRED: {
                Log.e("print", TAG + "  BOND_STATE_CHANGED > UNPAIRED");

                Bundle bundle = msg.getData();
                String macAddress = bundle.getString(BluetoothDeviceData.DEVICE_ID);

                BluetoothDevice device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(macAddress);

                String deviceName = device.getName();
                try {
                    Method method = device.getClass().getMethod("getAliasName");
                    if (method != null) {
                        deviceName = (String) method.invoke(device);
                    }
                } catch (Exception e) {

                    deviceName = device.getName();
                }

                Toast.makeText(context, deviceName + "  " + context.getResources().getString(R.string.msg_is_unpaired), Toast.LENGTH_SHORT).show();


                int position = -1;
                for (int i = 0; i < PrinterSettingActivity.pairedItems.size(); i++) {
                    if ((PrinterSettingActivity.pairedItems.get(i).getDeviceAddress()).equals(macAddress)) {
                        position = i;
                        break;
                    }
                }

                if (-1 < position) {

                    PrinterSettingActivity.pairedItems.remove(position);
                    BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
                }

                if (PrinterSettingActivity.pairedItems.size() == 0) {

                    PrinterSettingActivity.nullPairedDevices();
                }


                PrinterSettingActivity.newDeviceItems.clear();

                if (BluetoothDeviceData.mBluetoothAdapter.isDiscovering()) {
                    BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();
                }
                BluetoothDeviceData.mBluetoothAdapter.startDiscovery();
            }
            break;

            default:
                break;
        }
    }
}