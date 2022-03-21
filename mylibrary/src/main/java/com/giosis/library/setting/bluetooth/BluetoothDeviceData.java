package com.giosis.library.setting.bluetooth;

import java.util.UUID;


public class BluetoothDeviceData {

    // paired & unpaired
    static final int CONN_STATE_PAIRED = 25;
    static final int CONN_STATE_UNPAIRED = 26;

    // rename
    static final int REQUEST_RENAME_PAIR_DEVICE = 10005;

    // Printer Setting 에서 [Connected Device] 주소
    public static String connectedPrinterAddress = null;

    // connect
    static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String ACTION_CONNECT_STATE = "action_connect_state";
    static final int CONN_STATE_CONNECTED = 23;

    // disconnect
    static final int CONN_STATE_DISCONNECT = 20;

    static final String STATE = "state";
    static final String DEVICE_ID = "address";
    static final String DEVICE_NAME = "name";
}