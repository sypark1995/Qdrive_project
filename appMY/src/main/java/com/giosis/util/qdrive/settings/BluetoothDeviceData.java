package com.giosis.util.qdrive.settings;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author eylee 2018-04-03
 * Printer Setting
 * 공통 변수 관리하는 클래스
 * @editor krm0219
 */

@SuppressLint("StaticFieldLeak")
public class BluetoothDeviceData {

    // Bluetooth 지원 여부 확인
    public static BluetoothAdapter mBluetoothAdapter;

    static BroadcastReceiver bluetoothDeviceReceiver = null;
    static BluetoothDeviceHandler bluetoothDeviceHandler = null;

    // Printer Setting 에서 [Connected Device] 주소
    public static String connectedPrinterAddress = null;


    static PrinterConnectedListAdapter printerConnectedListAdapter;
    static PrinterPairedListAdapter printerPairedListAdapter;
    static PrinterAvailableListAdapter printerAvailableListAdapter;

    static ProgressBar availableProgress;
    static ImageView availableRefresh;


    // paired & unpaired
    static final int CONN_STATE_PAIRED = 25;
    static final int CONN_STATE_UNPAIRED = 26;

    // rename
    static final int REQUEST_RENAME_PAIR_DEVICE = 10005;

    static String getDeviceAliasName(BluetoothDevice device) {

        String aliasName = device.getName();

        try {

            Method method = device.getClass().getMethod("getAliasName");

            if (method != null) {

                aliasName = (String) method.invoke(device);
            }
        } catch (Exception e) {

            Log.e("Exception", "getDeviceAliasName  Exception : " + e.toString());
        }

        return aliasName;
    }

    static void setDeviceAliasName(BluetoothDevice device, String aliasName) {

        try {

            Method method = device.getClass().getMethod("setAlias", String.class);

            if (method != null) {

                method.invoke(device, aliasName);
            }
        } catch (Exception e) {

            Log.e("Exception", "setDeviceAliasName  Exception : " + e.toString());
        }
    }


    // connect
    public static BluetoothSocket socket = null;
    static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String ACTION_CONNECT_STATE = "action_connect_state";

    // connect & disconnect
    static final int CONN_STATE_CONNECTED = 23;
    static final int CONN_STATE_DISCONNECT = 20;

    static final String STATE = "state";
    static final String DEVICE_ID = "address";
    static final String DEVICE_NAME = "name";
}
