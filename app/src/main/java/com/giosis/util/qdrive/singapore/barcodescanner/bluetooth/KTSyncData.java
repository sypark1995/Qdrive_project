/*
 * KTSyncData.java
 *
 * �?your company here>, 2003-2008
 * Confidential and proprietary.
 */

package com.giosis.util.qdrive.singapore.barcodescanner.bluetooth;

/**
 *
 */
public class KTSyncData {
    public static final int BEEPSOUND_MASK = 0x80000000;
    public static final int BEEPVOLUME_MASK = 0x40000000;
    public static final int MENUBARCODE_MASK = 0x20000000;
    public static final int AUTOERASE_MASK = 0x10000000;
    public static final int TRACKS_MASK = 0x00000070;
    public static final int ENCRYPT_MASK = 0x00000080;
    public static final int BEEPONERROR_MASK = 0x00000100;
    public static final int BLUETOOTH_MASK = 0x000000FD;
    public static final int DUPLICATED_MASK = 0000000002;
    public static final int AUTO_TRIGGER_MASK = 0x00001000;
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
    public static boolean bIsReadyForMenu = false;
//
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


    public static boolean AutoConnect = false;
    public static boolean AttachTimestamp = false;
    public static boolean AttachType = false;
    public static boolean AttachSerialNumber = false;
    public static boolean AttachLocation = false;
    public static boolean SyncNonCompliant = true;
    public static boolean AttachQuantity = false;
    public static int DataDelimiter = 0;
    public static int RecordDelimiter = 0;


    public static boolean EraseMemory = false;
    public static boolean bIsGPSSupported = false;
    public static String[] BarcodeTypeName = new String[]
            {
                    "EAN 13",
                    "EAN 8",
                    "UPCA",
                    "UPCE",
                    "Code 39",
                    "ITF-14",                               // Unused
                    "Code 128",
                    "I2of5",
                    "CodaBar",
                    "UCC/EAN-128",
                    "Code 93",
                    "Code 35",
                    "Unknown",
                    "Unknown",
                    "Bookland EAN",
                    "Unknown",
            };
    public static String[] BarcodeType300 = new String[]
            {
                    "Code 32",      //0 0x00000001
                    "Trioptic",       //1 0x00000002
                    "Korea Post",     //2 0x00000004
                    "Aus. Post",      //3 0x00000008
                    "British Post",   //4 0x00000010
                    "Canada Post",    //5 0x00000020
                    "EAN-8",          //6 0x00000040
                    "UPC-E",          //7 0x00000080
                    "UCC/EAN-128",    //8 0x00000100
                    "Japan Post",     //9 0x00000200
                    "KIX Post",       //10 0x00000400
                    "Planet Code",    //11 0x00000800
                    "OCR",            //12 0x00001000
                    "Postnet",        //13 0x00002000
                    "China Post",     //14 0x00004000
                    "Micro PDF417",   //15 0x00008000
                    "TLC 39",         //16 0x00010000
                    "PosiCode",       //17 0x00020000
                    "Codabar",        //18 0x00040000
                    "Code 39",        //19 0x00080000
                    "UPC-A",          //20 0x00100000
                    "EAN-13",         //21 0x00200000
                    "I2of5",          //22 0x00400000
                    "IATA",           //23 0x00800000
                    "MSI",            //24 0x01000000
                    "Code 11",        //25 0x02000000
                    "Code 93",        //26 0x04000000
                    "Code 128",       //27 0x08000000
                    "Code 49",        //28 0x10000000
                    "Matrix2of5",     //29 0x20000000
                    "Plessey",        //30 0x40000000
                    "Code 16K",       //31 0x80000000
////////////////////////////////////////////////////////////////////////
                    "Codablock F",    //32 0x00000001
                    "PDF417",         //33 0x00000002
                    "QR/Micro QR",    //34 0x00000004
                    "Telepen",        //35 0x00000008
                    "VeriCode",       //36 0x00000010
                    "Data Matrix",    //37 0x00000020
                    "MaxiCode",       //38 0x00000040
                    "EAN/UCC",        //39 0x00000080
                    "RSS",            //40 0x00000100
                    "Aztec Code",     //41 0x00000200
                    "No Read",        //42
                    "HanXin Code",    //43
                    "Unknown"         //44
            };
    //
    public static int StoredBarcode = 0;
    public static int MemoryLeft = 0;
    public static int SleepTimeout = 2;
    //
    public static int MSRFormat = 0;
    public static int TrackTerminator = 6;
    //
    public static int ConnectDevice = 0;
    //public static int 	BluetoothOptions = 0;
    public static int PowerOnTime = 0;
    public static int PowerOffTime = 5;
    public static int AutoLock = 0;
    public static int Keyboard = 0;
    public static int InitDelay = 0;
    public static int CharDelay = 0;
    public static int CtrlChar = 0;
    public static int WedgeStore = 1;
    public static int BarcodeFormat = 0;
    public static int Terminator = 5;
    public static int AIM_ID = 0;
    public static int StartPosition = 1;
    public static int NoOfChars = 0;
    public static int Action = 1;
    public static String Prefix;
    public static String Suffix;
    public static int RereadDelay;
    public static int KDCSettings = 0;
}