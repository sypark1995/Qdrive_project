package com.giosis.util.qdrive.portableprinter.bluetooth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.gprinter.io.BluetoothPort;
import com.gprinter.io.PortManager;

import java.io.IOException;
import java.util.Vector;

/**
 * @author eylee 2018-03-26
 * <p>
 * receiver 에서 이 클래를 ArrayList 에 담아두고 사용
 */
public class PrinterConnManager {
    private String TAG = "PrinterConnManager";

    private int id;
    private PortManager mPort;
    private CONN_METHOD connMethod;
    private String macAddress;

    private boolean isOpenPort = false;


    private final int CONN_STATE_DISCONNECT = 500;
    private final int CONN_STATE_FAILED = CONN_STATE_DISCONNECT << 2;


    private final int READ_DATA = 10000;
    private final String READ_DATA_CNT = "read_data_cnt";
    private final String READ_BUFFER_ARRAY = "read_buffer_array";

    public final String ACTION_QUERY_PRINTER_STATE = "action_query_printer_state";
    private byte[] sendCommand;
    private byte[] tsc = {0x1b, '!', '?'};
    private PrinterCommand currentPrinterCommand;

    private final int TSC_STATE_PAPER_ERR = 0x04;
    private final int TSC_STATE_COVER_OPEN = 0x01;
    private final int TSC_STATE_ERR_OCCURS = 0x80;


    public enum CONN_METHOD {
        BLUETOOTH("BLUETOOTH");

        private String name;

        CONN_METHOD(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    //    （ESC（EscCommand.java）、TSC（LabelCommand.java））
    public enum PrinterCommand {
        /**
         * ESC指令
         */
        ESC,
        /**
         * TSC指令
         */
        TSC
    }

    PrinterConnManager(CONN_METHOD connMethod, String macAddress) {
        this.connMethod = connMethod;
        this.macAddress = macAddress;
    }


    synchronized void openPort() {

        isOpenPort = false;

        try {

            if (connMethod == CONN_METHOD.BLUETOOTH) {

                mPort = new BluetoothPort(macAddress);
                isOpenPort = mPort.openPort();
            }
        } catch (Exception e) {

            isOpenPort = false;
        }

        Log.e("print", TAG + "  openPort : " + isOpenPort);

        if (isOpenPort) {

            queryCommand();
        } else {

            sendStateBroadcast(CONN_STATE_FAILED);
        }
    }

    private void queryCommand() {

        //开启读取打印机返回数据线程
        PrinterReader reader = new PrinterReader();
        reader.start();

        //查询打印机所使用指令
        queryPrinterCommand();
    }

    private void queryPrinterCommand() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("print", TAG + "  queryPrinterCommand  : " + currentPrinterCommand);

                if (currentPrinterCommand == null || currentPrinterCommand == PrinterCommand.TSC) {

                    //发送TSC查询打印机状态指令
                    sendCommand = tsc;
                    Vector<Byte> data2 = new Vector(tsc.length);
                    for (byte b : tsc) {
                        data2.add(b);
                    }
                    sendDataImmediately(data2);
                }
            }
        }, "Thread");

        thread.start();
    }

    public void sendDataImmediately(final Vector<Byte> data) {

        if (this.mPort == null) {
            return;
        }

        try {

            Log.e("print", TAG + "  sendDataImmediately size : " + data.size());
            this.mPort.writeDataImmediately(data, 0, data.size());
            if (data.size() > 10) {  // 첫번째 포트 (리스트 프린트 버튼에서 커넥션 여는게 아닌 프린트 할 때 불려지는 함수 - 자동 트리거)
                //자동 트리거하는 함수 없앤것
                GPrinterData.TEMP_TRACKING_NO = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void closePort() {

        if (this.mPort != null) {

            this.mPort.closePort();
            isOpenPort = false;
            currentPrinterCommand = null;
        }

        sendStateBroadcast(CONN_STATE_DISCONNECT);
    }

    private void sendStateBroadcast(int state) {

        Intent intent = new Intent(GPrinterData.ACTION_CONN_STATE);
        intent.putExtra("state", state);
        intent.putExtra("id", id);
        MyApplication.getContext().sendBroadcast(intent);
    }


    public boolean getConnState() {
        return isOpenPort;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public PrinterCommand getCurrentPrinterCommand() {
        return currentPrinterCommand;
    }


    class PrinterReader extends Thread {

        private boolean isRun;
        private byte[] buffer = new byte[100];

        PrinterReader() {

            isRun = true;
        }

        @Override
        public void run() {
            try {

                while (isRun) {

                    int len = readDataImmediately(buffer);
                    Log.e("print", TAG + "  PrinterReader length : " + len);

                    if (0 < len) {

                        Message msg = Message.obtain();
                        msg.what = READ_DATA;
                        Bundle bundle = new Bundle();
                        bundle.putInt(READ_DATA_CNT, len);
                        bundle.putByteArray(READ_BUFFER_ARRAY, buffer);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {

                Log.e("print", TAG + "  Exception : " + e.toString());
                closePort();
            }
        }
    }

    private int readDataImmediately(byte[] buffer) throws IOException {
        return this.mPort.readData(buffer);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == READ_DATA) {

                int cnt = msg.getData().getInt(READ_DATA_CNT);
                byte[] buffer = msg.getData().getByteArray(READ_BUFFER_ARRAY);
                //这里只对查询状态返回值做处理，其它返回值可参考编程手册来解析
                if (buffer == null) {
                    return;
                }

                String status = "Printer connected normal";

                if (sendCommand == tsc) {
                    //设置当前打印机模式为TSC模式   // 현재 프린터 모드를 TSC 모드로 설정
                    if (currentPrinterCommand == null) {

                        currentPrinterCommand = PrinterCommand.TSC;
                        //    sendStateBroadcast(CONN_STATE_CONNECTED);
                    } else {

                        if (cnt == 1) {//查询打印机实时状态
                            if ((buffer[0] & TSC_STATE_PAPER_ERR) > 0) {//缺纸
                                status += " " + "Printer out of paper";
                            }
                            if ((buffer[0] & TSC_STATE_COVER_OPEN) > 0) {//开盖
                                status += " " + "Printer open cover";
                            }
                            if ((buffer[0] & TSC_STATE_ERR_OCCURS) > 0) {//打印机报错
                                status += " " + "Printer error";
                            }
                            Toast.makeText(MyApplication.getContext(), status, Toast.LENGTH_SHORT).show();
                        } else {//打印机状态查询

                            Intent intent = new Intent(ACTION_QUERY_PRINTER_STATE);
                            intent.putExtra("id", id);
                            MyApplication.getContext().sendBroadcast(intent);
                        }
                    }
                }
            }
        }
    };
}