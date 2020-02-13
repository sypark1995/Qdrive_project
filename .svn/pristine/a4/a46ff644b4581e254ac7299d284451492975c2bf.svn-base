package com.giosis.util.qdrive.settings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.UUID;

/**
 * @author eylee 2018-04-03
 * 공통 변수 관리하는 클래스
 * <p></p>
 * @editor krm0219
 */
public class BluetoothDeviceData {

    // Bluetooth 지원 여부 확인
    public static BluetoothAdapter mBluetoothAdapter;

    static BroadcastReceiver bluetoothDeviceReceiver = null;
    static BluetoothDeviceHandler bluetoothDeviceHandler = null;

    static PrinterConnectedListAdapter printerConnectedListAdapter;
    static PrinterPairedListAdapter printerPairedListAdapter;
    static PrinterAvailableListAdapter printerAvailableListAdapter;

    static ProgressBar availableProgress;
    static ImageView availableRefresh;


    // Printer Setting 에서 [Connected Device] 주소
    public static String connectedPrinterAddress = null;

    // paired & unpaired
    static final int CONN_STATE_PAIRED = 25;
    static final int CONN_STATE_UNPAIRED = 26;


    // rename
    static final int REQUEST_RENAME_PAIR_DEVICE = 10005;

    // connect
    public static BluetoothSocket socket = null;
    static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String ACTION_CONNECT_STATE = "action_connect_state";
    static final int CONN_STATE_CONNECTED = 23;


    // disconnect
    static final int CONN_STATE_DISCONNECT = 20;


    static final String STATE = "state";
    static final String DEVICE_ID = "address";
    static final String DEVICE_NAME = "name";
}