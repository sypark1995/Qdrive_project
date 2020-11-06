/*
 * KTSyncData.java
 *
 * �?your company here>, 2003-2008
 * Confidential and proprietary.
 */

package com.giosis.util.qdrive.barcodescanner.bluetooth;

import android.annotation.SuppressLint;

public class KTSyncData {

    @SuppressLint("StaticFieldLeak")
    public static KScan mKScan = null;


    public static BluetoothChatService mChatService = null;
    public static boolean LockUnlock = false;
    public static boolean bLockScanButton = false;

    public static boolean bForceTerminate = false;
    //
    public static boolean bIsSLEDConnected = false;
    //
    //
    public static boolean bIsRunning = false;
    //
    public static boolean bIsConnected = false;

    //Command related
    public static int state = 0;
    public static boolean bIsCommandDone = true;
    public static int LongNumbers = 0;
    public static int LongNumbersEx = 0;
    public static byte[] StringData = new byte[256];
    public static int StringLength;
    //Buffer related
    public static int total = 0;
    public static int writePtr = 0;
    public static byte[] RxBuffer = new byte[4 * 1024];
//

    public static boolean bIsSynchronizeOn = false;
    public static boolean bIsSyncFinished = false;
    //
    public static boolean bIsKDC300 = false;
    public static boolean bIsTwoBytesCount = false;
    public static byte[] SerialNumber = new byte[15];
    public static byte[] FWVersion = new byte[15];
    public static byte[] MACAddress = new byte[15];
    public static byte[] BTVersion = new byte[15];
    public static byte[] FWBuild = new byte[15];
    public static byte[] DateTime = new byte[10];
    //
    public static int BarcodeType;
    public static byte[] BarcodeBuffer = new byte[2048 * 2];
    //
    public static int Options;
    public static int Symbologies;
    public static int OptionsEx;
    public static int SymbologiesEx;
    public static int Timeout = 2000;
    public static int Security = 1;
    public static int Minlength = 2;
    public static int WedgeMode = 1;
    //


    public static boolean SyncNonCompliant = true;

    static boolean bIsGPSSupported = false;

    //
    static int PowerOffTime = 5;
    static int KDCSettings = 0;

    static final int BEEPSOUND_MASK = 0x80000000;
    static final int BEEPVOLUME_MASK = 0x40000000;
    static final int MENUBARCODE_MASK = 0x20000000;
    static final int AUTOERASE_MASK = 0x10000000;
    static final int TRACKS_MASK = 0x00000070;
    static final int ENCRYPT_MASK = 0x00000080;
    static final int BEEPONERROR_MASK = 0x00000100;
    static final int BLUETOOTH_MASK = 0x000000FD;
    static final int DUPLICATED_MASK = 0000000002;
    static final int AUTO_TRIGGER_MASK = 0x00001000;
}