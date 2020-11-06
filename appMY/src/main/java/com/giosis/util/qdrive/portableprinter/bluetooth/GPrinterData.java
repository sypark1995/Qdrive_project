package com.giosis.util.qdrive.portableprinter.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;

import java.util.ArrayList;

/**
 * @author eylee 2018-04-03
 * 공통 변수 관리하는 클래스
 * <p></p>
 * @editor krm0219
 */
@SuppressLint("StaticFieldLeak")
public class GPrinterData {

    // Bluetooth 지원 여부 확인
    public static BluetoothAdapter mBluetoothAdapter;

    // Print 연결 BroadCaseReceiver  &&  Handler
    public static BroadcastReceiver printerReceiver = null;
    public static GPrinterHandler gPrinterHandler = null;


    // Bluetooth 비활성화시, 연결시키는 intent requestCode
    public static final int REQUEST_ENABLE_BT = 532;
                
    // 연결된 Print List                   >> List Size 여러개 가능한지 확인하기
    public static ArrayList<PrinterConnManager> printerConnManagerList = null;

    public static boolean CONNECTED_PRINT = false;
    public static String TRACKING_NO = "";
    public static String TEMP_TRACKING_NO = "";

    static final int PRINTER_CONNECT_SUCCESS = 983;
    public static final int PRINTER_COMMAND_ERROR = 333;
    static final int NONE_PRINTER = 987;


    public static final String ACTION_CONN_STATE = "action_connect_state";
    static final int CONN_STATE_DISCONNECT = 0x90;
    static final int CONN_PRINT_DONE = CONN_STATE_DISCONNECT << 4;
}
