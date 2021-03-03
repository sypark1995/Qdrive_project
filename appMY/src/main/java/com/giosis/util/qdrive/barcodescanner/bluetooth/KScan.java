package com.giosis.util.qdrive.barcodescanner.bluetooth;

import android.os.Handler;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp;

public class KScan {

    private static final String TAG = "KScan";


    public Handler mHandler;
    public Handler mSettingHandler;

    private CommandThread _command_thread = null;

    private int rbuffer_offset, bbuffer_offset, barcode_length;


    private int sleep_timeout;

    private AES mAES = null;

    private int returnTarget = 0;
    //


    public KScan(Handler handler) {

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


    public void callHandler() {
        Log.e("capture", "callHandler " + returnTarget);

        if (returnTarget == 0) return;

        if (returnTarget == CaptureActivityTemp.MESSAGE_SETTING)
            mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_SETTING, -1, -1, -1).sendToTarget();
        else
            mSettingHandler.obtainMessage(returnTarget, -1, -1, -1).sendToTarget();

        returnTarget = 0;
    }

    public synchronized void HandleInputData(byte ch) {

        KTSyncData.RxBuffer[KTSyncData.writePtr++] = ch;

        Log.e("krm0219", "KScan state  > " + KTSyncData.state);


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

        KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) ';';
    }

    private void GetRecordDelimiter() {

        KTSyncData.BarcodeBuffer[bbuffer_offset++] = (byte) '\n';
    }

    // NOTIFICATION.   Scan
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
    }


    public void SendBarcodeData() {
        mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_DISPLAY, bbuffer_offset, -1, KTSyncData.BarcodeBuffer).sendToTarget();
    }


    public void SynchronizeMSRData() {
        byte ch = KTSyncData.RxBuffer[rbuffer_offset++];

        GetBarcodeData();

        KTSyncData.BarcodeBuffer[bbuffer_offset] = 0;

        if (ch == (byte) 0xFE) bbuffer_offset = mAES.DecryptData(bbuffer_offset);


        GetRecordDelimiter();

        SendBarcodeData();

        InitialVariables();
    }

    public void SynchronizeData() {
        rbuffer_offset = 3;
        bbuffer_offset = 0;

        DetermineBarcodeLength();

        if ((KTSyncData.RxBuffer[rbuffer_offset] & 0x7f) == (byte) 0x7E) {
            SynchronizeMSRData();
            return;
        }

        if (KTSyncData.RxBuffer[KTSyncData.total - 3] == (byte) 0xff) {   //Application Data
            Log.d(TAG, "Application data");
            barcode_length -= 2;

            Log.e("km0219", "KScan  > " + KTSyncData.SyncNonCompliant);


            if (!KTSyncData.SyncNonCompliant) {
                if ((KTSyncData.RxBuffer[KTSyncData.total - 2] & (byte) 0x80) != 0) {
                    Log.d(TAG, "Non compliant application data");
                    return;
                }
            }
        }

        DetermineBarcodeType();
        GetBarcodeData();

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

    public void AutoPowerOff() {

        StartCommandThread(255, (byte) 0x02);
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


    public synchronized void DeviceConnected() {

        Sleep(3000);
        KTSyncData.bIsConnected = true;
        GetSerialNumber();      //   StartCommandThread(2, (byte) 'M');
        Sleep(100);
        GetFWVersion();         //   StartCommandThread(2, (byte) 'V');
        Sleep(100);
        GetFWBuild();            //   StartCommandThread(2, (byte) 'v');
        Sleep(100);
        SetWedgeMode(1);        /*
         KTSyncData.WedgeMode = WedgeMode;
        StartCommandThread(255, (byte) 'W');
        StartCommandThread(255, (byte) 'w');
        */
        Sleep(100);
        AutoPowerOff();         //  StartCommandThread(255, (byte) 0x02);
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

                        Log.e("capture", TAG + "  DATA : " + cmdArray[rPtr]);
                        switch (cmdArray[rPtr++]) {
                            case 10:

                                Log.d(TAG, "Command = 10-2");
                                callHandler();

                                InitialVariables();
                                break;
                            case 11:
                                switch (cmdArray[rPtr++]) {
                                    case 1:
                                        SendCommand("GnS0");
                                        break;
                                    case 2:
                                        SendCommand("GnS1");
                                        break;
                                    case 3:
                                        SendCommand("GnTG");
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
                                        break;
                                    case 9:
                                        SendCommand("GnMSG");
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
                                        break;
                                    case 13:
                                        SendCommand("GHKG");
                                        break;
                                    case 14:
                                        SendCommand("GndBG");
                                        break;
                                    case 15:
                                        SendCommand("GndCG");
                                        break;
                                    case 16:
                                        SendCommand("GnCG");
                                        break;
                                    case 17:
                                        SendCommand("bTcG");
                                        break;
                                    case 18:
                                        SendCommand("bT0");
                                        KTSyncData.KDCSettings &= (~KTSyncData.BLUETOOTH_MASK);
                                        KTSyncData.LongNumbers &= KTSyncData.BLUETOOTH_MASK;
                                        KTSyncData.KDCSettings |= KTSyncData.LongNumbers;
                                        break;
                                    case 19:
                                        SendCommand("bTO0");
                                        break;
                                    case 20:
                                        SendCommand("bT70");
                                        KTSyncData.PowerOffTime = KTSyncData.LongNumbers - 1;
                                        if (KTSyncData.PowerOffTime >= 30)
                                            KTSyncData.PowerOffTime = 5;
                                        break;
                                    case 21:
                                        SendCommand("u");
                                        break;
                                    case 22:
                                        SendCommand("GnF");
                                        break;
                                    case 23:
                                        SendCommand("GTG");
                                        break;
                                    case 24:
                                        SendCommand("GnDG");
                                        KTSyncData.KDCSettings &= (~KTSyncData.DUPLICATED_MASK);
                                        if (KTSyncData.LongNumbers == 1)
                                            KTSyncData.KDCSettings |= KTSyncData.DUPLICATED_MASK;
                                        break;
                                    case 25:
                                        SendCommand("GEGA");
                                        break;
                                    case 26:
                                        SendCommand("GEGO");
                                        break;
                                    case 27:
                                        SendCommand("GEGL");
                                        break;
                                    case 28:
                                        SendCommand("GEGT");
                                        break;
                                    case 29:
                                        SendCommand("GEGP");
                                        byte[] temp = new byte[KTSyncData.StringLength];
                                        for (int i = 0; i < KTSyncData.StringLength; i++)
                                            temp[i] = KTSyncData.StringData[i];
                                        break;
                                    case 30:
                                        SendCommand("GEGS");
                                        byte[] temp1 = new byte[KTSyncData.StringLength];
                                        for (int i = 0; i < KTSyncData.StringLength; i++)
                                            temp1[i] = KTSyncData.StringData[i];
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
                            default: {
                                Log.e("capture", TAG + "  default...");
                            }
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

            mHandler.obtainMessage(CaptureActivityTemp.MESSAGE_SEND, length, -1, buffer).sendToTarget();
        }

        public void SendCommand(String cmd) {
            Log.d(TAG, "SendCommand: " + cmd + ":" + wPtr + ":" + rPtr);

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


        void WakeupKDC() {

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
