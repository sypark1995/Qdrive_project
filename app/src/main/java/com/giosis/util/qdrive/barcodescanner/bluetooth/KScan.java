/*
 * KScan.java
 *
 * ?��your company here>, 2003-2008
 * Confidential and proprietary.
 */

package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Calendar;


public class KScan {

    private static final String TAG = "KScan";
    public Handler mHandler;
    public Handler mSettingHandler;

    public Context mContext;
    public Context mSettingContext;
    public Context mSystemContext;
    //private int buffer_write = 0;
    //private byte[] buffer = new byte[1024];

    private CommandThread _command_thread = null;

    private int rbuffer_offset, bbuffer_offset, barcode_length;

    private byte[] latitudestring = new byte[30];
    private byte[] longitudestring = new byte[30];
    private byte[] altitudestring = new byte[30];

    private boolean bIsSouth = false;
    private boolean bIsWest = false;

    public boolean bIsFileAvailable = false;

    public Writer outfile;

    private float latitudedecimal = 0, longitudedecimal = 0, altitudedecimal = 0;
    private float latitudedecimal_backup = 0, longitudedecimal_backup = 0;
    //private int count = 0;

    private int sleep_timeout;

    private AES mAES = null;

    public boolean IsMenuAlertStarted = false;

    private int returnTarget = 0;
    //
//
    public static int COMMAND_GET_NUMBER = 1;
    public static int COMMAND_GET_STRING = 2;
    public static int COMMAND_GET_NUMBER_EX = 3;

    //
    public static int STORED_BARCODE = 1;
    public static int MEMORY_LEFT = 2;
    public static int SLEEP_TIMEOUT = 3;
    public static int BEEP_SOUND = 4;
    public static int HIGH_BEEP_VOLUME = 5;
    public static int MENU_BARCODE = 6;
    public static int AUTO_ERASE = 7;
    public static int DATA_FORMAT = 8;
    public static int TRACK_TERMINATOR = 9;
    public static int ENCRYPT_DATA = 10;
    public static int TRACK_SELECT = 11;
    public static int HID_AUTOLOCK = 12;
    public static int HID_KEYBOARD = 13;
    public static int HID_INIT_DELAY = 14;
    public static int HID_CHAR_DELAY = 15;
    public static int HID_CTRL_CHAR = 16;
    public static int CONNECT_DEVICE = 17;
    public static int BT_OPTIONS = 18;
    public static int POWER_ON_TIME = 19;
    public static int POWER_OFF_TIME = 20;
    public static int WEDGE_STORE = 21;
    public static int BARCODE_FORMAT = 22;
    public static int TERMINATOR = 23;
    public static int CHECK_DUPLICATED = 24;
    public static int AIM_ID = 25;
    public static int START_POSITION = 26;
    public static int NO_OF_CHARS = 27;
    public static int ACTION = 28;
    public static int PREFIX = 29;
    public static int SUFFIX = 30;
    public static int BARCODE_OPTION = 31;
    public static int SECURITY = 32;
    public static int SCAN_TIMEOUT = 33;
    public static int MIN_LENGTH = 34;
    public static int AUTO_TRIGGER = 35;
    public static int REREAD_DELAY = 36;
    public static int SYMBOL_OPTION = 37;
    public static int BEEP_ON_ERROR = 38;
    public static int DATE_TIME = 39;

    ProgressDialog dialog;

    public KScan(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mSettingHandler = handler;

        KTSyncData.writePtr = 0;
        KTSyncData.state = 0;
        if (_command_thread == null) {
            _command_thread = new CommandThread();
            _command_thread.wPtr = 0;
            _command_thread.rPtr = 0;
            _command_thread.start();
        }

        mAES = new AES();

    }

    private Handler dialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "dialogHandler");
            if ((dialog != null) && (!KTSyncData.bForceTerminate)) {
                dialog.dismiss();
                IsMenuAlertStarted = false;
            }
        }
    };


    public void openDialog(Context context, String message, int timeout, boolean alert) {

        Log.d(TAG, "openDialog");
        if (alert) {
            if (IsMenuAlertStarted) return;
            IsMenuAlertStarted = true;
            Log.d(TAG, "openDialog: alert started");
        }
        if (KTSyncData.bForceTerminate)
            dialog = ProgressDialog.show(mContext, message, "Please wait...", true);
        else dialog = ProgressDialog.show(context, message, "Please wait...", true);
        // Close it after 2 seconds
        dialogHandler.sendEmptyMessageDelayed(0, timeout);
    }


    public void callHandler() {
        Log.d(TAG, "callHandler " + returnTarget);

        if (returnTarget == 0) return;

        dialog.dismiss();

        if (returnTarget == CaptureActivityTemp.MESSAGE_SETTING)
            mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_SETTING, -1, -1, -1).sendToTarget();
        else
            mSettingHandler.obtainMessage(returnTarget, -1, -1, -1).sendToTarget();

        returnTarget = 0;
    }

    public synchronized void HandleInputData(byte ch) {
        //String tmp = String.format("%x", ch);
        //Log.d(TAG, "HandleInputData " + KTDemoData.state + " : " + tmp);
        KTSyncData.RxBuffer[KTSyncData.writePtr++] = ch;
/*    	
    	if ( ch == 0x0a ) {
    		// Send the obtained bytes to the UI Activity
    		mHandler.obtainMessage(aKTSync.MESSAGE_DISPLAY, KTSyncData.writePtr, -1, KTSyncData.RxBuffer)
    				.sendToTarget();
    		KTSyncData.writePtr = 0;
    	}
*/

        switch (KTSyncData.state) {
            case 0:     //wedge state - barcode only mode
                if (ch == 0x03) {
                    KTSyncData.state = 10;
                    KTSyncData.writePtr = 0;
                } else {
                    if (ch == 0x0a) {
                        mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_DISPLAY, KTSyncData.writePtr, -1, KTSyncData.RxBuffer)
                                .sendToTarget();
                        KTSyncData.writePtr = 0;
                    } else
                        KTSyncData.writePtr = 0;
                }
                break;
            case 1: // Get numbers
                if ((ch == '@') && (KTSyncData.writePtr == 5)) GetNumbers();
                break;
            case 2: // Get strings
                if (ch == '@') GetStrings();
                break;
            case 3: // Get numbers for KDC300
                if ((ch == 0x40) && (KTSyncData.writePtr == 9)) GetNumbersEx();
                break;
            case 10:    //wedge - packet data mode
                if (KTSyncData.writePtr == 3) {
                    KTSyncData.total = MakeInteger(0, 3);
                    KTSyncData.state = 11;
                }
                break;
            case 11:
                if (KTSyncData.writePtr == KTSyncData.total) {
                    SynchronizeData();
                }
                break;
            case 12:
                if (ch == '@') SynchronizeData();
                break;
            case 20:    //Synchronize one by one
                if (ch == 0x03) KTSyncData.state = 21;
                KTSyncData.writePtr = 0;
                break;
            case 21:
                if (KTSyncData.writePtr == 3) {
                    KTSyncData.total = MakeInteger(0, 3);
                    //if ( ! KTSyncData.bIsKDC300 )   {
                    //    KTSyncData.total++;
                    //}
                    KTSyncData.state = 22;
                }
                break;
            case 22:
                if (KTSyncData.writePtr == KTSyncData.total) SynchronizeData();
                break;
            case 255:
                if ((ch == '@') || (ch == '!')) InitialVariables();
                break;
            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------------------------
//
//  Synchronize Data
//
//----------------------------------------------------------------------------------------------------------------
    public void MakeFilename() {

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String tempstring;
        tempstring = String.format("_%4d%02d%02d_%02d%02d%02d.txt", year, month, day, hour, min, second);

        String temp = new String(KTSyncData.SerialNumber, 4, 6);
        KTSyncData.filename = temp + tempstring;

    }

    public boolean OpenFile() {
        outfile = null;
        bIsFileAvailable = true;
        Log.d(TAG, "Open File");
        try {
            Log.d(TAG, "" + Environment.getExternalStorageDirectory());

            File root = new File(Environment.getExternalStorageDirectory(), "myData");

            if (!root.exists()) root.mkdirs();

            MakeFilename();

            File syncfile = new File(root, KTSyncData.filename);
            outfile = new BufferedWriter(new FileWriter(syncfile));

            Log.d(TAG, "File created:");

        } catch (Exception e) {
            bIsFileAvailable = false;
            Log.d(TAG, "Exception:" + e);
        }
        return bIsFileAvailable;
    }

    public void InitialVariables() {
        KTSyncData.state = 0;
        KTSyncData.writePtr = 0;
        KTSyncData.bIsCommandDone = true;
    }

    public int uByteToInt(byte ch) {
        return (ch & 0xFF);
    }

    public int MakeInteger(int offset, int index) {
        int number = 0;

        for (int i = 0; i < index; i++)
            number += (uByteToInt(KTSyncData.RxBuffer[offset + i]) << (index - i - 1) * 8);

        return number;
    }

    public void GetNumbers() {

        KTSyncData.LongNumbers = MakeInteger(0, 4);

        //Dialog.alert(Integer.toHexString(KTSyncData.LongNumbers));

        InitialVariables();
    }

    public void GetNumbersEx() {
        KTSyncData.LongNumbers = MakeInteger(0, 4);
        KTSyncData.LongNumbersEx = MakeInteger(4, 4);

        //Dialog.alert(Integer.toHexString(KTSyncData.LongNumbers) + "/" + Integer.toHexString(KTSyncData.LongNumbersEx));      
        InitialVariables();
    }

    public void GetStrings() {
        int i;
        int j = 0;
        for (i = 0; i < KTSyncData.writePtr - 1; i++) {
            if (KTSyncData.RxBuffer[i] != 0x00)
                KTSyncData.StringData[j++] = KTSyncData.RxBuffer[i];
        }
        KTSyncData.StringData[j] = 0;
        KTSyncData.StringLength = j;
        //Dialog.alert(new String(KTSyncData.StringData));
        InitialVariables();

    }

    public boolean IsCheckSumOK() {
        int sum = 0;

        for (int i = 0; i < KTSyncData.total; i++) sum += KTSyncData.RxBuffer[i];

        if (sum != 0) return false;
        return true;
    }

    private void DetermineBarcodeLength() {
        if (!KTSyncData.bIsTwoBytesCount) {
            barcode_length = (MakeInteger(rbuffer_offset, 1) - 5);    //(int)(KTSyncData.RxBuffer[rbuffer_offset++]) - 5;
            rbuffer_offset += 1;
        } else {
            barcode_length = (MakeInteger(rbuffer_offset, 2) - 7);
            rbuffer_offset += 2;
        }
    }

    private void DetermineBarcodeType() {
        if (KTSyncData.bIsKDC300)
            KTSyncData.BarcodeType = KTSyncData.RxBuffer[rbuffer_offset++] & 0x3f;
        else {
            KTSyncData.BarcodeType = KTSyncData.RxBuffer[rbuffer_offset++] & 0x1f;
            if ((KTSyncData.BarcodeType == 20) || (KTSyncData.BarcodeType == 21))
                KTSyncData.BarcodeType -= 10;
        }
    }

    private void GetDataDelimiter() {
        switch (KTSyncData.DataDelimiter) {
            case 1:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = 0x09;
                break;
            case 2:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = 0x20;
                break;
            case 3:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) ',';
                break;
            case 4:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) ';';
                break;
            default:
                break;
        }
    }

    private void GetRecordDelimiter() {
        switch (KTSyncData.RecordDelimiter) {
            case 1:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '\n';
                break;
            case 2:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '\r';
                break;
            case 3:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '\t';
                break;
            case 4:
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '\r';
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '\n';
                break;
            default:
                break;
        }
    }

    private void GetSerialNumber(boolean attach) {
        if (attach) {
            //if ( KTSyncData.AttachSerialNumber == 2  )   GetDataDelimiter();
            for (int i = 0; i < 10; i++)
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = KTSyncData.SerialNumber[i];
            if (KTSyncData.AttachSerialNumber == true) GetDataDelimiter();
        }
    }

    private void GetBarcodeTypeString(boolean attach) {
        if (attach) {
            byte[] temp;
            if (KTSyncData.bIsKDC300)
                temp = KTSyncData.BarcodeType300[KTSyncData.BarcodeType].getBytes();
            else temp = KTSyncData.BarcodeTypeName[KTSyncData.BarcodeType].getBytes();
            KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '[';
            for (int i = 0; i < temp.length; i++)
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = temp[i];
            KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) ']';
            GetDataDelimiter();
        }
    }

    private void GetBarcodeTimeStamp(boolean attach) {
        if (attach) {
            GetDataDelimiter();
            //int datedata = ((KTSyncData.RxBuffer[rbuffer_offset] << 8) + KTSyncData.RxBuffer[rbuffer_offset+1]);
            //int timedata = ((KTSyncData.RxBuffer[rbuffer_offset+2] << 8) + KTSyncData.RxBuffer[rbuffer_offset+3]);            
            int datedata = MakeInteger(rbuffer_offset, 2);
            int timedata = MakeInteger(rbuffer_offset + 2, 2);

            int year = (((datedata & 0xfc00) >> 10) + 2000);
            int month = (((datedata & 0x3c0) >> 6));
            int day = (((datedata & 0x3e) >> 1));

            int hour = (((timedata & 0xf000) >> 12));
            if ((datedata & 0x1) != 0) hour += 12;
            int minute = (((timedata & 0xfc0) >> 6));
            int second = ((timedata & 0x3f));
            /*
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);            
                                    
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy:MM:dd/HH:mm:ss");
            
            String tempstring = dateformat.format(calendar, new StringBuffer(), null).toString();
         */
            String tempstring;
            tempstring = String.format("%d:%02d:%02d/%02d:%02d:%02d", year, month, day, hour, minute, second);

            byte[] temp = tempstring.getBytes();
            for (int i = 0; i < temp.length; i++)
                KTSyncData.BarcodeBuffer[bbuffer_offset++] = temp[i];
        }
    }

    private float ConvertStringToFloat(String string) {

        float floatnumber = Float.valueOf(string.trim()).floatValue();
        int decimal = (int) floatnumber / 100;

        return decimal + (floatnumber - decimal * 100) / 60;
    }

    private void CalcurateLocation() {
        latitudedecimal = ConvertStringToFloat(new String(latitudestring));
        longitudedecimal = ConvertStringToFloat(new String(longitudestring));

        if (bIsSouth) latitudedecimal = -(latitudedecimal);
        if (bIsWest) longitudedecimal = -(longitudedecimal);

    }

    private void GetBarcodeData() {
        Log.d(TAG, "" + barcode_length);

        for (int i = 0; i < barcode_length; i++)
            KTSyncData.BarcodeBuffer[bbuffer_offset++] = KTSyncData.RxBuffer[rbuffer_offset++];

        if (!KTSyncData.bIsGPSSupported) return;

        KTSyncData.BarcodeBuffer[bbuffer_offset] = 0;

        String tmp = new String(KTSyncData.BarcodeBuffer);
        String gps = new String("<G|P/S]");
        int offset = tmp.indexOf(gps);

        if (offset == -1) return;    //it is not GPS data

        bbuffer_offset = offset;

        if (!KTSyncData.AttachLocation) return;

        gps = tmp.substring(offset);
        byte[] temp = gps.getBytes();

        int i = 7;
        int cnt = 0;

        while (true) {
            if (temp[i] == ',') break;
            latitudestring[cnt++] = temp[i++];
        }
        latitudestring[cnt] = 0;
        i++;

        if (temp[i++] == 'N') bIsSouth = false;
        else bIsSouth = true;

        i++;
        cnt = 0;

        while (true) {
            if (temp[i] == ',') break;
            longitudestring[cnt++] = temp[i++];
        }
        longitudestring[cnt] = 0;
        i++;

        if (temp[i++] == 'W') bIsWest = true;
        else bIsWest = false;

        CalcurateLocation();

        GetDataDelimiter();

        if (temp[i++] == ';') {   //Process Altitude data

            cnt = 0;

            while (true) {
                if (temp[i] == ',') break;
                altitudestring[cnt++] = temp[i++];
            }
            altitudestring[cnt] = 0;

            tmp = new String(altitudestring);
            gps = String.format("%f,%f,%.1f", latitudedecimal, longitudedecimal,
                    Float.valueOf(tmp.trim()).floatValue());
        } else
            gps = String.format("%f,%f", latitudedecimal, longitudedecimal);

        temp = gps.getBytes();
        for (i = 0; i < gps.length(); i++) KTSyncData.BarcodeBuffer[bbuffer_offset++] = temp[i];
        KTSyncData.BarcodeBuffer[bbuffer_offset] = 0;

    }


    public void SendBarcodeData() {
        mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_DISPLAY, bbuffer_offset, -1, KTSyncData.BarcodeBuffer)
                .sendToTarget();
    }

    public void GetQuantity() {
        if (KTSyncData.RxBuffer[KTSyncData.total - 3] != (byte) 0xff) return;

        GetDataDelimiter();

        String tmp = String.format("%d", (KTSyncData.RxBuffer[KTSyncData.total - 2] & (byte) 0x7f));
        byte[] temp = tmp.getBytes();

        for (int i = 0; i < temp.length; i++) KTSyncData.BarcodeBuffer[bbuffer_offset++] = temp[i];
    }

    public void SynchronizeMSRData() {
        byte ch = KTSyncData.RxBuffer[rbuffer_offset++];

        GetBarcodeData();

        KTSyncData.BarcodeBuffer[bbuffer_offset] = 0;

        if (ch == (byte) 0xFE) bbuffer_offset = mAES.DecryptData(bbuffer_offset);

        GetBarcodeTimeStamp(KTSyncData.AttachTimestamp == true);

        GetRecordDelimiter();

        SendBarcodeData();

        InitialVariables();
    }

    public void SynchronizeData() {
        rbuffer_offset = 3;
        bbuffer_offset = 0;

        Log.d(TAG, "Synchronization has started");

        DetermineBarcodeLength();

        if ((KTSyncData.RxBuffer[rbuffer_offset] & 0x7f) == (byte) 0x7E) {
            SynchronizeMSRData();
            return;
        }

        if (KTSyncData.RxBuffer[KTSyncData.total - 3] == (byte) 0xff) {   //Application Data
            Log.d(TAG, "Application data");
            barcode_length -= 2;
            if (!KTSyncData.SyncNonCompliant) {
                if ((KTSyncData.RxBuffer[KTSyncData.total - 2] & (byte) 0x80) != 0) {
                    Log.d(TAG, "Non compliant application data");
                    return;
                }
            }
        }

        DetermineBarcodeType();

        GetSerialNumber(KTSyncData.AttachSerialNumber == true);
        GetBarcodeTypeString(KTSyncData.AttachType == true);
        GetBarcodeData();
        GetBarcodeTimeStamp(KTSyncData.AttachTimestamp == true);

        if (KTSyncData.AttachQuantity) GetQuantity();

        GetRecordDelimiter();

        SendBarcodeData();

        InitialVariables();
    }

    //----------------------------------------------------------------------------------------------------------------
//
//  KDC Commands
//
//----------------------------------------------------------------------------------------------------------------  
    public void StartCommandThread(int state, byte command) {
        if (_command_thread == null) {
            _command_thread = new CommandThread();
            _command_thread.wPtr = 0;
            _command_thread.rPtr = 0;
            _command_thread.start();
            Sleep(100);
        }

        _command_thread.cmdArray[_command_thread.wPtr] = command;
        _command_thread.stateArray[_command_thread.wPtr] = state;
        _command_thread.wPtr++;
        //if ( _command_thread.wPtr == 256 )  _command_thread.wPtr = 0;
    }

    public void SendCommandGetResult(int cmdType, int cmdIndex, int resultType) {
        Log.d(TAG, "KScan: SendCommandGetNumber " + cmdIndex);

        _command_thread.cmdArray[_command_thread.wPtr] = (byte) cmdType;
        _command_thread.stateArray[_command_thread.wPtr] = resultType;
        _command_thread.wPtr++;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) cmdIndex;

        Sleep(100);
        //if ( _command_thread.wPtr == 256 )  _command_thread.wPtr = 0;  

    }

    public void SendCommandWithValue(String cmd, int value) {

        openDialog(mSettingContext, "Storing", 30000, true);

        String command = String.format("%X#", value);
        command = cmd + command;

        _command_thread.stateArray[_command_thread.wPtr] = 255;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) 12;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) command.length();

        byte[] temp = command.getBytes();
        for (int i = 0; i < command.length(); i++)
            _command_thread.cmdArray[_command_thread.wPtr++] = temp[i];

        Log.d(TAG, "SendCommandWithValue: " + command + " " + command.length());
        Sleep(100);
/*
//        if ( ! IsMenuAlertStarted ) {
//            IsMenuAlertStarted = true;
//            [appDelegate DisplayAlert:true:@"Please Wait...":nil:false];
//        }
*/
    }

    public void SendCommandWithValueEx(String cmd, int value1, int value2) {

        openDialog(mSettingContext, "Storing", 30000, true);

        String command = String.format("%X#%X#", value1, value2);
        command = cmd + command;

        _command_thread.stateArray[_command_thread.wPtr] = 255;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) 12;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) command.length();

        byte[] temp = command.getBytes();
        for (int i = 0; i < command.length(); i++)
            _command_thread.cmdArray[_command_thread.wPtr++] = temp[i];

        Log.d(TAG, "SendCommandWithValueEx: " + command + " " + command.length());
        Sleep(100);
/*
//        if ( ! IsMenuAlertStarted ) {
//            IsMenuAlertStarted = true;
//            [appDelegate DisplayAlert:true:@"Please Wait...":nil:false];
//        }
*/
    }

    public void SendCommandFixData(String cmd, String fix) {
        openDialog(mSettingContext, "Storing", 30000, true);

        String command = String.format("%X#", fix.length());
        command = cmd + command + fix;

        _command_thread.stateArray[_command_thread.wPtr] = 255;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) 12;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) command.length();

        byte[] temp = command.getBytes();
        for (int i = 0; i < command.length(); i++)
            _command_thread.cmdArray[_command_thread.wPtr++] = temp[i];

        Log.d(TAG, "SendCommandFixData: " + command + " " + command.length());
        Sleep(100);
/*
//        if ( ! IsMenuAlertStarted ) {
//            IsMenuAlertStarted = true;
//            [appDelegate DisplayAlert:true:@"Please Wait...":nil:false];
//        }
*/
    }

    public void NormalSynchronize() {
        StartCommandThread(255, (byte) 'W');
        StartCommandThread(1, (byte) 'N');
        StartCommandThread(255, (byte) 0x01);  //Sync Started
        StartCommandThread(0, (byte) 'p');
        //StartCommandThread(255, (byte)0x00);  //Sync finished
    }

    public void GetSerialNumber() {
        StartCommandThread(2, (byte) 'M');
    }

    public void GetFWVersion() {
        StartCommandThread(2, (byte) 'V');
    }

    public void GetFWBuild() {
        StartCommandThread(2, (byte) 'v');
    }

    public void SetWedgeMode(int WedgeMode) {
        KTSyncData.WedgeMode = WedgeMode;
        StartCommandThread(255, (byte) 'W');
        StartCommandThread(255, (byte) 'w');
    }

    public void Sleep(int timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }
    }

    public void AutoPowerOff(boolean enable) {
        if (enable) StartCommandThread(255, (byte) 0x02);
        else StartCommandThread(255, (byte) 0x03);
    }

    public void SetSleepTimeout(int timeout) {
        sleep_timeout = timeout;
        StartCommandThread(255, (byte) 0x06);
    }

    public void LockUnlockScanButton(boolean lock) {
        KTSyncData.bLockScanButton = lock;
        StartCommandThread(255, (byte) 'W');
        StartCommandThread(255, (byte) 0x07);
        if (lock) {
            StartCommandThread(255, (byte) 0x04);
            SetSleepTimeout(2);
        } else {
            StartCommandThread(255, (byte) 0x05);
            SetSleepTimeout(30);
        }
    }

    public synchronized void DeviceConnected(boolean connected) {
        Log.d(TAG, "KDC is connected");
        Sleep(3000);
        KTSyncData.bIsConnected = true;
        GetSerialNumber();
        Sleep(100);
        GetFWVersion();
        Sleep(100);
        GetFWBuild();
        Sleep(100);
        SetWedgeMode(1);
        Sleep(100);
        if (KTSyncData.AutoConnect) AutoPowerOff(false);
        else AutoPowerOff(true);
    }

    //------------------------------------------------------------------------------------------------------------
    public void WakeupCommand() {
        _command_thread.wPtr = 0;
        _command_thread.rPtr = 0;

        StartCommandThread(255, (byte) 'W');
    }

    public void FinishCommand() {
        Log.d(TAG, "FinishCommand");

        StartCommandThread(255, (byte) 10);
    }

    public void ClearMemory() {
        openDialog(mSystemContext, "Erasing", 10000, true);

        WakeupCommand();

        StartCommandThread(255, (byte) 'E');

        FinishCommand();
    }

    public void SyncClock() {
        openDialog(mSystemContext, "Programming", 10000, true);

        WakeupCommand();

        Calendar c = Calendar.getInstance();

        _command_thread.stateArray[_command_thread.wPtr] = 255;
        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) 12;
        _command_thread.cmdArray[_command_thread.wPtr++] = 7;


        _command_thread.cmdArray[_command_thread.wPtr++] = (byte) 'C';
        KTSyncData.DateTime[0] = (byte) (c.get(Calendar.YEAR) - 2000);
        KTSyncData.DateTime[1] = (byte) (c.get(Calendar.MONTH) + 1);
        KTSyncData.DateTime[2] = (byte) (c.get(Calendar.DAY_OF_MONTH));
        KTSyncData.DateTime[3] = (byte) (c.get(Calendar.HOUR));
        KTSyncData.DateTime[4] = (byte) (c.get(Calendar.MINUTE));
        KTSyncData.DateTime[5] = (byte) (c.get(Calendar.SECOND));

        for (int i = 0; i < 6; i++)
            _command_thread.cmdArray[_command_thread.wPtr++] = KTSyncData.DateTime[i];

        Sleep(100);
        FinishCommand();
    }


    public void FactoryDefault() {
        openDialog(mSystemContext, "Setting", 30000, true);
        WakeupCommand();

        StartCommandThread(255, (byte) 'F');

        FinishCommand();
    }

    public void ChangeBTProfile() {
        Log.d(TAG, "ChangeBTProfile");

        WakeupCommand();
        SendCommandWithValue("bTc", KTSyncData.ConnectDevice);
        FinishCommand();
    }


    public void GetSymbolOption() {
        Log.d(TAG, "GetSymbolOption");

        returnTarget = Settings.MESSAGE_BARCODE;
        WakeupCommand();

        openDialog(mSettingContext, "Loading", 10000, false);

        if (KTSyncData.bIsKDC300) SendCommandGetResult(11, SYMBOL_OPTION, COMMAND_GET_NUMBER_EX);
        else SendCommandGetResult(11, SYMBOL_OPTION, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }

    public void GetBarcodeOption() {
        Log.d(TAG, "GetBarcodeOption");

        returnTarget = Settings.MESSAGE_OPTION;
        WakeupCommand();

        openDialog(mSettingContext, "Loading", 10000, false);

        if (KTSyncData.bIsKDC300) SendCommandGetResult(11, BARCODE_OPTION, COMMAND_GET_NUMBER_EX);
        else SendCommandGetResult(11, BARCODE_OPTION, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }

    public void GetScanOptions() {
        Log.d(TAG, "GetDataProcess");

        returnTarget = Settings.MESSAGE_SCANOPTION;
        WakeupCommand();

        openDialog(mSettingContext, "Loading", 10000, false);

        if (!KTSyncData.bIsKDC300) {
            //Scan Angle & Filter    
            SendCommandGetResult(11, BARCODE_OPTION, COMMAND_GET_NUMBER);
            //Security level    
            SendCommandGetResult(11, SECURITY, COMMAND_GET_NUMBER);
        }
        //Reading timeout
        SendCommandGetResult(11, SCAN_TIMEOUT, COMMAND_GET_NUMBER);
        //Minimum length    
        SendCommandGetResult(11, MIN_LENGTH, COMMAND_GET_NUMBER);
        //Auto Trigger   
        SendCommandGetResult(11, AUTO_TRIGGER, COMMAND_GET_NUMBER);
        //Reread delay     		
        SendCommandGetResult(11, REREAD_DELAY, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }

    public void GetDataProcess() {
        Log.d(TAG, "GetDataProcess");

        returnTarget = Settings.MESSAGE_DATAPROCESS;
        WakeupCommand();
        openDialog(mSettingContext, "Loading", 10000, false);
        //Wedge/store
        SendCommandGetResult(11, WEDGE_STORE, COMMAND_GET_NUMBER);
        //Data format
        SendCommandGetResult(11, BARCODE_FORMAT, COMMAND_GET_NUMBER);
        //Terminator
        SendCommandGetResult(11, TERMINATOR, COMMAND_GET_STRING);
        //Check duplicated data
        SendCommandGetResult(11, CHECK_DUPLICATED, COMMAND_GET_NUMBER);
        //AIM ID
        SendCommandGetResult(11, AIM_ID, COMMAND_GET_NUMBER);
        //Start position
        SendCommandGetResult(11, START_POSITION, COMMAND_GET_NUMBER);
        //No of chars
        SendCommandGetResult(11, NO_OF_CHARS, COMMAND_GET_NUMBER);
        //Action
        SendCommandGetResult(11, ACTION, COMMAND_GET_NUMBER);
        //Prefix
        SendCommandGetResult(11, PREFIX, COMMAND_GET_STRING);
        //Suffix
        SendCommandGetResult(11, SUFFIX, COMMAND_GET_STRING);
        StartCommandThread(255, (byte) 10);
    }

    public void GetBluetoothOptions() {
        returnTarget = Settings.MESSAGE_BLUETOOTH;
        WakeupCommand();
        openDialog(mSettingContext, "Loading", 10000, false);

        //connect device
        SendCommandGetResult(11, CONNECT_DEVICE, COMMAND_GET_NUMBER);
        //bluetooth options
        SendCommandGetResult(11, BT_OPTIONS, COMMAND_GET_NUMBER);
        //power on time
        SendCommandGetResult(11, POWER_ON_TIME, COMMAND_GET_NUMBER);
        //power off time
        SendCommandGetResult(11, POWER_OFF_TIME, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }

    public void GetHIDSettings() {
        Log.d(TAG, "GetHIDSettings");

        returnTarget = Settings.MESSAGE_HID;

        WakeupCommand();
        openDialog(mSettingContext, "Loading", 10000, false);
//Autolock
        SendCommandGetResult(11, HID_AUTOLOCK, COMMAND_GET_NUMBER);
//keyboard  
        SendCommandGetResult(11, HID_KEYBOARD, COMMAND_GET_NUMBER);
//Init delay 
        SendCommandGetResult(11, HID_INIT_DELAY, COMMAND_GET_NUMBER);
//Char delay 
        SendCommandGetResult(11, HID_CHAR_DELAY, COMMAND_GET_NUMBER);
//Control char    	  	 
        SendCommandGetResult(11, HID_CTRL_CHAR, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }

    public void GetMSRSettings() {
        Log.d(TAG, "GetMSRSettings");

        returnTarget = Settings.MESSAGE_MSR;

        WakeupCommand();
        openDialog(mSettingContext, "Loading", 10000, false);
//Data Format
        SendCommandGetResult(11, DATA_FORMAT, COMMAND_GET_NUMBER);
//Track terminator  
        SendCommandGetResult(11, TRACK_TERMINATOR, COMMAND_GET_NUMBER);
//Encrypt Data 
        SendCommandGetResult(11, ENCRYPT_DATA, COMMAND_GET_NUMBER);
//Beep on error 
        SendCommandGetResult(11, BEEP_ON_ERROR, COMMAND_GET_NUMBER);
//Track select    	  	 
        SendCommandGetResult(11, TRACK_SELECT, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }

    public void GetSystemSettings() {
        Log.d(TAG, "GetSystemSettings");

        returnTarget = Settings.MESSAGE_SYSTEM;

        WakeupCommand();
        openDialog(mSettingContext, "Loading", 10000, false);
//Sleep timeout    
        SendCommandGetResult(11, SLEEP_TIMEOUT, COMMAND_GET_NUMBER);
//Get date/time    
        SendCommandGetResult(11, DATE_TIME, COMMAND_GET_STRING);
//Beep sound    
        SendCommandGetResult(11, BEEP_SOUND, COMMAND_GET_NUMBER);
//High beep volume    
        SendCommandGetResult(11, HIGH_BEEP_VOLUME, COMMAND_GET_NUMBER);
//Menu barcode
        if (KTSyncData.bIsKDC300) SendCommandGetResult(11, MENU_BARCODE, COMMAND_GET_NUMBER);
//Auto erase   
        SendCommandGetResult(11, AUTO_ERASE, COMMAND_GET_NUMBER);
        StartCommandThread(255, (byte) 10);
    }
//----------------------------------------------------------------------------------------------------------------
//
//  KDC Commands Thread
//
//----------------------------------------------------------------------------------------------------------------      

    private class CommandThread extends Thread {
        private String command;
        private char wPtr = 0, rPtr = 0, i, length;
        private byte[] cmdArray = new byte[256 + 2];
        private int[] stateArray = new int[256 + 2];

        @Override
        public void run() {
            while (true) {
                if (KTSyncData.bIsConnected) {
                    if (wPtr != rPtr) {
                        KTSyncData.writePtr = 0;
                        KTSyncData.state = stateArray[rPtr];
                        switch (cmdArray[rPtr++]) {
                            case 10:
                                if (IsMenuAlertStarted) {
                                    IsMenuAlertStarted = false;
                                    if (dialog != null) dialog.dismiss();
                                    wPtr = rPtr = 0;
                                    if (KTSyncData.bForceTerminate) {
                                        Sleep(1000);
                                        KTSyncData.bForceTerminate = false;
                                        mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_EXIT, -1, -1, -1).sendToTarget();
                                    }
                                } else {
                                    Log.d(TAG, "Command = 10-2");
                                    callHandler();
                                }
                                InitialVariables();
                                break;
                            case 11:
                                switch (cmdArray[rPtr++]) {
                                    case 1:
                                        SendCommand("GnS0");
                                        KTSyncData.StoredBarcode = KTSyncData.LongNumbers;
                                        break;
                                    case 2:
                                        SendCommand("GnS1");
                                        KTSyncData.MemoryLeft = KTSyncData.LongNumbers;
                                        break;
                                    case 3:
                                        SendCommand("GnTG");
                                        KTSyncData.SleepTimeout = KTSyncData.LongNumbers;
                                        break;
                                    case 4:
                                        SendCommand("Gb2");
                                        KTSyncData.KDCSettings &= (~KTSyncData.BEEPSOUND_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.BEEPSOUND_MASK;
                                        break;
                                    case 5:
                                        SendCommand("Gb3");
                                        KTSyncData.KDCSettings &= (~KTSyncData.BEEPVOLUME_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.BEEPVOLUME_MASK;
                                        break;
                                    case 6:
                                        SendCommand("GnBG");
                                        KTSyncData.KDCSettings &= (~KTSyncData.MENUBARCODE_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.MENUBARCODE_MASK;
                                        break;
                                    case 7:
                                        SendCommand("GnEG");
                                        KTSyncData.KDCSettings &= (~KTSyncData.AUTOERASE_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.AUTOERASE_MASK;
                                        break;
                                    case 8:
                                        SendCommand("GnMDG");
                                        KTSyncData.MSRFormat = KTSyncData.LongNumbers;
                                        break;
                                    case 9:
                                        SendCommand("GnMSG");
                                        KTSyncData.TrackTerminator = KTSyncData.LongNumbers;
                                        break;
                                    case 10:
                                        SendCommand("GnMEG");    //Encrypt Data
                                        KTSyncData.KDCSettings &= (~KTSyncData.ENCRYPT_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.ENCRYPT_MASK;
                                        break;
                                    case 11:
                                        SendCommand("GnMTG");    //Encrypt Data
                                        KTSyncData.KDCSettings &= (~KTSyncData.TRACKS_MASK);
                                        KTSyncData.LongNumbers &= (KTSyncData.TRACKS_MASK >> 4);
                                        KTSyncData.LongNumbers <<= 4;
                                        KTSyncData.KDCSettings |= KTSyncData.LongNumbers;
                                        break;
                                    case 12:
                                        SendCommand("GHTG");
                                        KTSyncData.AutoLock = KTSyncData.LongNumbers;
                                        break;
                                    case 13:
                                        SendCommand("GHKG");
                                        KTSyncData.Keyboard = KTSyncData.LongNumbers;
                                        break;
                                    case 14:
                                        SendCommand("GndBG");
                                        KTSyncData.InitDelay = KTSyncData.LongNumbers;
                                        break;
                                    case 15:
                                        SendCommand("GndCG");
                                        KTSyncData.CharDelay = KTSyncData.LongNumbers;
                                        break;
                                    case 16:
                                        SendCommand("GnCG");
                                        KTSyncData.CtrlChar = KTSyncData.LongNumbers;
                                        break;
                                    case 17:
                                        SendCommand("bTcG");
                                        KTSyncData.ConnectDevice = KTSyncData.LongNumbers;
                                        break;
                                    case 18:
                                        SendCommand("bT0");
                                        KTSyncData.KDCSettings &= (~KTSyncData.BLUETOOTH_MASK);
                                        KTSyncData.LongNumbers &= KTSyncData.BLUETOOTH_MASK;
                                        KTSyncData.KDCSettings |= KTSyncData.LongNumbers;
                                        break;
                                    case 19:
                                        SendCommand("bTO0");
                                        KTSyncData.PowerOnTime = KTSyncData.LongNumbers;
                                        break;
                                    case 20:
                                        SendCommand("bT70");
                                        KTSyncData.PowerOffTime = KTSyncData.LongNumbers - 1;
                                        if (KTSyncData.PowerOffTime >= 30)
                                            KTSyncData.PowerOffTime = 5;
                                        break;
                                    case 21:
                                        SendCommand("u");
                                        KTSyncData.WedgeStore = KTSyncData.LongNumbers;
                                        break;
                                    case 22:
                                        SendCommand("GnF");
                                        KTSyncData.BarcodeFormat = KTSyncData.LongNumbers;
                                        break;
                                    case 23:
                                        SendCommand("GTG");
                                        KTSyncData.Terminator = KTSyncData.StringData[0] - '0';
                                        break;
                                    case 24:
                                        SendCommand("GnDG");
                                        KTSyncData.KDCSettings &= (~KTSyncData.DUPLICATED_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.DUPLICATED_MASK;
                                        break;
                                    case 25:
                                        SendCommand("GEGA");
                                        KTSyncData.AIM_ID = KTSyncData.LongNumbers;
                                        break;
                                    case 26:
                                        SendCommand("GEGO");
                                        KTSyncData.StartPosition = KTSyncData.LongNumbers;
                                        break;
                                    case 27:
                                        SendCommand("GEGL");
                                        KTSyncData.NoOfChars = KTSyncData.LongNumbers;
                                        break;
                                    case 28:
                                        SendCommand("GEGT");
                                        KTSyncData.Action = KTSyncData.LongNumbers;
                                        break;
                                    case 29:
                                        SendCommand("GEGP");
                                        byte[] temp = new byte[KTSyncData.StringLength];
                                        for (int i = 0; i < KTSyncData.StringLength; i++)
                                            temp[i] = KTSyncData.StringData[i];
                                        KTSyncData.Prefix = new String(temp);
                                        break;
                                    case 30:
                                        SendCommand("GEGS");
                                        byte[] temp1 = new byte[KTSyncData.StringLength];
                                        for (int i = 0; i < KTSyncData.StringLength; i++)
                                            temp1[i] = KTSyncData.StringData[i];
                                        KTSyncData.Suffix = new String(temp1);
                                        break;
                                    case 31:
                                        SendCommand("o");
                                        KTSyncData.Options = KTSyncData.LongNumbers;
                                        if (KTSyncData.bIsKDC300)
                                            KTSyncData.OptionsEx = KTSyncData.LongNumbersEx;
                                        break;
                                    case 32:
                                        SendCommand("z");
                                        KTSyncData.Security = KTSyncData.LongNumbers - 1;
                                        break;
                                    case 33:
                                        SendCommand("t");
                                        KTSyncData.Timeout = (KTSyncData.LongNumbers / 1000) - 1;
                                        break;
                                    case 34:
                                        SendCommand("l");
                                        KTSyncData.Minlength = KTSyncData.LongNumbers;
                                        break;
                                    case 35:
                                        SendCommand("GtGM");
                                        KTSyncData.KDCSettings &= (~KTSyncData.AUTO_TRIGGER_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.AUTO_TRIGGER_MASK;
                                        break;
                                    case 36:
                                        SendCommand("GtGD");
                                        KTSyncData.RereadDelay = KTSyncData.LongNumbers;
                                        break;
                                    case 37:
                                        SendCommand("s");
                                        KTSyncData.Symbologies = KTSyncData.LongNumbers;
                                        if (KTSyncData.bIsKDC300)
                                            KTSyncData.SymbologiesEx = KTSyncData.LongNumbersEx;
                                        break;
                                    case 38:
                                        SendCommand("GnMBG");    //Beep on error
                                        KTSyncData.KDCSettings &= (~KTSyncData.BEEPONERROR_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.BEEPONERROR_MASK;
                                        break;
                                    case 39:
                                        SendCommand("c");
                                        for (int i = 0; i < 6; i++)
                                            KTSyncData.DateTime[i] = KTSyncData.StringData[i];
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case 12:
                                length = (char) cmdArray[rPtr++];
                                byte[] temp = new byte[length];
                                for (i = 0; i < length; i++) temp[i] = cmdArray[rPtr++];
                                SendCommand(new String(temp));
                                break;

                            case 0x00:  // GS0
                                SendCommand("GS0");

                                if (KTSyncData.EraseMemory) {
                                    StartCommandThread(255, (byte) 'E');
                                }
                                break;
                            case 0x01:  // GS1
                                SendCommand("GS1");
                                break;
                            case 0x02:    //bT51	Bluetooth auto power off enable
                                if (!KTSyncData.bIsSLEDConnected) SendCommand("bT51");
                                else InitialVariables();
                                break;
                            case 0x03:    //bT50	Bluetooth auto power off disable
                                SendCommand("bT50");
                                break;
                            case 0x04:
                                SendCommand("GMB0GM1;0#GMTScan button  Locked!!!    \r");
                                break;
                            case 0x05:
                                SendCommand("GMB1GM1;0#GMTScan button  Unlocked!!!  \r");
                                break;
                            case 0x06:
                                command = "GNs" + Integer.toHexString(sleep_timeout) + "#";
                                SendCommand(command);
                                break;
                            case 0x07:    //Lock/unlock
                                if (KTSyncData.bLockScanButton) SendCommand("GNS0");
                                else SendCommand("GNS1");
                                break;
                            case 'l':
                                SendCommand("l");
                                KTSyncData.Minlength = KTSyncData.LongNumbers;
                                KTSyncData.bIsReadyForMenu = true;
                                break;
                            case 'E':
                                SendCommand("E");
                                break;
                            case 'F':
                                SendCommand("F");
                                break;
                            case 'L':
                                SendCommandEx("L", KTSyncData.Minlength);
                                break;

                            case 'M':
                                SendCommand("M");
                                CopySerialNumber();
                                break;
                            case 'N':
                                SendCommand("N");
                                break;

                            case 'O':
                                if (KTSyncData.bIsKDC300) {
                                    SendCommandEx("O", KTSyncData.Options, KTSyncData.OptionsEx);
                                    break;
                                } else {
                                    SendCommandEx("O", KTSyncData.Options);
                                    break;
                                }
                            case 'o':
                                if (KTSyncData.bIsKDC300) KTSyncData.state = 3;
                                else KTSyncData.state = 1;
                                SendCommand("o");
                                KTSyncData.Options = KTSyncData.LongNumbers;
                                if (KTSyncData.bIsKDC300)
                                    KTSyncData.OptionsEx = KTSyncData.LongNumbersEx;
                                break;
                            case 'p':
                                SynchronizeRecordByRecord(KTSyncData.LongNumbers);
                                StartCommandThread(255, (byte) 0x00);  //Sync finished
                                break;
                            case 'S':
                                if (KTSyncData.bIsKDC300) {
                                    SendCommandEx("S", KTSyncData.Symbologies, KTSyncData.SymbologiesEx);
                                    break;
                                } else {
                                    SendCommandEx("S", KTSyncData.Symbologies);
                                    break;
                                }
                            case 's':
                                if (KTSyncData.bIsKDC300) KTSyncData.state = 3;
                                else KTSyncData.state = 1;
                                SendCommand("s");
                                KTSyncData.Symbologies = KTSyncData.LongNumbers;
                                if (KTSyncData.bIsKDC300)
                                    KTSyncData.SymbologiesEx = KTSyncData.LongNumbersEx;
                                KTSyncData.bIsReadyForMenu = true;
                                break;
                            case 'T':
                                SendCommandEx("T", KTSyncData.Timeout);
                                break;
                            case 't':
                                SendCommand("t");
                                KTSyncData.Timeout = KTSyncData.LongNumbers;
                                break;
                            case 'V':
                                SendCommand("IV");
                                CopyFWVersion();
                                break;
                            case 'v':
                                SendCommand("Iv");
                                CopyFWBuild();
                                break;
                            case 'W':
                                WakeupKDC();
                                break;
                            case 'w':
                                SendCommandEx("w", KTSyncData.WedgeMode);
                                //if ( KTSyncData.WedgeMode == 0) KTSyncData.bIsReadyToClose = true;
                                if (KTSyncData.bIsSLEDConnected) InitializeSLED();
                                break;
                            case 'Z':
                                if (!KTSyncData.bIsKDC300) SendCommandEx("Z", KTSyncData.Security);
                                break;
                            case 'z':
                                if (!KTSyncData.bIsKDC300) {
                                    SendCommand("z");
                                    KTSyncData.Security = KTSyncData.LongNumbers;
                                }
                                break;
                            default:
                                break;
                        }
                    } else {    //wPtr != rPtr
                        Sleep(100);
                    }
                } else {    //Is Connected
                    _command_thread = null;
                    return;
                }
            }
        }

        public void Sleep(int timeout) {
            long endTime = System.currentTimeMillis() + timeout;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
        }

        public void InitializeSLED() {
            int i;

            Log.d(TAG, "SLED is connected");

            KTSyncData.state = 255;
            SendCommand("GnMDS1#");

            KTSyncData.state = 2;
            SendCommand("bT9");
            for (i = 0; i < 12; i++) KTSyncData.MACAddress[i] = KTSyncData.StringData[i];
            KTSyncData.MACAddress[i] = (byte) 0x00;


            KTSyncData.state = 2;
            SendCommand("bTV");
            for (i = 0; i < 5; i++) KTSyncData.BTVersion[i] = KTSyncData.StringData[i + 1];
            KTSyncData.BTVersion[i] = (byte) 0x00;

        }

        public void CopySerialNumber() {
            int i;

            for (i = 0; i < 10; i++) KTSyncData.SerialNumber[i] = KTSyncData.StringData[i];
            KTSyncData.SerialNumber[i] = (byte) 0x00;
        }

        public void CopyFWBuild() {
            int i;

            for (i = 0; i < 12; i++) KTSyncData.FWBuild[i] = KTSyncData.StringData[i];
            KTSyncData.FWBuild[i] = (byte) 0x00;
        }

        public void CopyFWVersion() {
            int i;

            for (i = 0; i < 10; i++) KTSyncData.FWVersion[i] = KTSyncData.StringData[i];
            KTSyncData.FWVersion[i] = (byte) 0x00;
            KTSyncData.bIsKDC300 = false;
            KTSyncData.bIsTwoBytesCount = false;

            if (KTSyncData.FWVersion[5] == '3') {
                KTSyncData.bIsKDC300 = true;
                KTSyncData.bIsTwoBytesCount = true;
            }
            KTSyncData.bIsGPSSupported = false;
            if ((KTSyncData.FWVersion[5] == '2') && (KTSyncData.FWVersion[6] == '5'))
                KTSyncData.bIsGPSSupported = true;

            KTSyncData.bIsSLEDConnected = false;
            if (KTSyncData.FWVersion[5] == '4') {
                KTSyncData.bIsSLEDConnected = true;
                if (KTSyncData.FWVersion[6] == '2') {
                    KTSyncData.bIsKDC300 = true;
                    KTSyncData.bIsTwoBytesCount = true;
                }
            }

            if (KTSyncData.FWVersion[8] == 'H' || KTSyncData.FWVersion[8] == 'J') {
                KTSyncData.LockUnlock = true;
            }
            if (KTSyncData.FWVersion[8] == 'C' && KTSyncData.FWVersion[9] == 'J') {
                KTSyncData.LockUnlock = true;
            }
        }

        public void writeData(String command, int offset, int length) {
            byte[] buffer = command.getBytes();

            mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_SEND, length, -1, buffer)
                    .sendToTarget();

        }

        public void SendCommand(String cmd) {
            Log.d(TAG, "SendCommand: " + cmd + ":" + Integer.toString(wPtr) + ":" + Integer.toString(rPtr));

            KTSyncData.bIsCommandDone = false;
            writeData(cmd, 0, cmd.length());

            if (KTSyncData.bForceTerminate) Sleep(2000);
            else {
                //Log.d(TAG, "SendCommand:Starting ");
                int loopcnt = 100;
                while (loopcnt-- != 0) {
                    if (KTSyncData.bIsCommandDone) break;
                    if (!KTSyncData.bIsConnected) return;
                    Sleep(10);
                }
            }
            Log.d(TAG, "SendCommand:Done ");
        }

        public void SendCommandEx(String cmd, int numbers) {
            KTSyncData.bIsCommandDone = false;
            command = cmd + Integer.toHexString(numbers) + "#";
            writeData(command, 0, command.length());
            while (!KTSyncData.bIsCommandDone) Sleep(10);

        }

        public void SendCommandEx(String cmd, int numbers, int numbersEx) {
            KTSyncData.bIsCommandDone = false;
            command = cmd + Integer.toHexString(numbers) + "#" + Integer.toHexString(numbersEx) + "#";
            writeData(command, 0, command.length());
            while (!KTSyncData.bIsCommandDone) Sleep(10);

        }


        public void WakeupKDC() {
            KTSyncData.state = 255;
            KTSyncData.bIsCommandDone = false;
            while (!KTSyncData.bIsCommandDone) {
                Log.d(TAG, "WakeupKDC");
                writeData("W", 0, 1);
                Sleep(150);
            }
        }

        public void SynchronizeRecordByRecord(int noofrecord) {
            KTSyncData.bIsSynchronizeOn = true;
            for (int i = 0; i < noofrecord; i++) {

                //count = i;
                KTSyncData.state = 20;
                KTSyncData.bIsSyncFinished = false;
                command = "p" + Integer.toHexString(i) + "#";
                writeData(command, 0, command.length());

                //while( ! KTSyncData.bIsSyncFinished )   Sleep(100);
                int loopcnt = 3;
                while (true) {
                    if (KTSyncData.bIsSyncFinished) break;
                    if (loopcnt-- == 0) {
                        i--;
                        KTSyncData.state = 20;
                        break;
                    }
                    Sleep(100);
                }
            }
            KTSyncData.bIsSynchronizeOn = false;
        }
    }
} 
