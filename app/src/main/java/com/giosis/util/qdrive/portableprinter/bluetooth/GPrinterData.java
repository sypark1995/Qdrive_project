package com.giosis.util.qdrive.portableprinter.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;

import java.util.ArrayList;

/**
 * @author eylee 2018-04-03
 * 공통 변수 관리하는 클래스
 *
 * <p></p>
 * @editor krm0219
 */
public class GPrinterData {

    // Bluetooth 비활성화시, 연결시키는 intent requestCode
    public static final int REQUEST_ENABLE_BT = 532;

    // BluetoothAdapter
    public static BluetoothAdapter mBluetoothAdapter;

    // 연결된 Print List
    public static ArrayList<PrinterConnManager> printerConnManagerList = null;

    // Print 연결 BroadCaseReceiver
    public static BroadcastReceiver printerReceiver = null;
    // Print 연결 Handler
    public static GPrinterHandler gPrinterHandler = null;


    public static Boolean isGPrint = false;
    public static String TRACKING_NO = "";
    public static String TEMP_TRACKING_NO = "";

    static final int DOUBLE_PRINTER = 983;
    public static final int PRINTER_COMMAND_ERROR = 333;
    static final int NONE_PRINTER = 987;


    public static final String ACTION_CONN_STATE = "action_connect_state";
    static final int CONN_STATE_DISCONNECT = 0x90;
    static final int CONN_PRINT_DONE = CONN_STATE_DISCONNECT << 4;
}