package com.giosis.util.qdrive.portableprinter.bluetooth;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.giosis.library.setting.bluetooth.BluetoothDeviceData;

/**
 * @author eylee 2018-04-03
 * <p>
 * 리스트 페이지에서 Print Label 버튼을 눌렀을 때 이 receiver를 등록
 * 앱을 종료할 때 destroy 메서드에서 receiver 해제
 * <p>
 * @editor krm0219
 */

public class GPrinterBroadcastReceiver extends BroadcastReceiver {
    String TAG = "GPrinterBroadcastReceiver";

    Context context;
    Activity activity;

    public GPrinterBroadcastReceiver(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (action != null && !action.equals(BluetoothDevice.ACTION_FOUND))
            Log.e("print", TAG + "  onReceive action : " + action);

        switch (action) {
            case BluetoothDevice.ACTION_FOUND:  // 불루트스 기기 검색됨.

               /* if (device != null && device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {

                    Log.e("print", TAG + "  ACTION_FOUND   " + device.getName() + "  " + device.getAddress() +
                            " / " + device.getBluetoothClass().getDeviceClass() + " / " + device.getBluetoothClass().getMajorDeviceClass());
                }*/

                if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (BluetoothDeviceData.connectedPrinterAddress.equals(device.getAddress())) {

                        Log.e("print", TAG + "  FOUND Device name " + device.getName() + " / " + device.getAddress());
                        // 프린트 버튼을 눌렀을 때, 디바이스를 찾아서 커넥션이 이루어진다음에  printerConnManagerList 에 소켓 섹션을 담아서 저장한 다음에 SDK 포트로  열린다면 그 커넥션을 저장해 놓고
                        // 포트가 열리지 않는다면 커넥션 제거
                        // 포트가 열린다면 디바이스 맥어드레스 저장해 놓기 -> 블루투스 세팅 화면에서 커넥션 열어 한 소스처럼 관리
                        if (GPrinterData.printerConnManagerList != null) {

                            GPrinterData.printerConnManagerList.add(new PrinterConnManager(PrinterConnManager.CONN_METHOD.BLUETOOTH, device.getAddress()));
                            int size = GPrinterData.printerConnManagerList.size();

                            if (0 < size) {

                                GPrinterData.printerConnManagerList.get(size - 1).openPort();

                                if (!GPrinterData.printerConnManagerList.get(size - 1).getConnState()) {  // 포트가  열리지 않았다면

                                    Log.e("print", TAG + "  connState  " + size);
                                    GPrinterData.printerConnManagerList.remove((size - 1));
                                }
                            }
                        }
                    }
                }
                break;


            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:        // 블루투스 기기 검색 종료

                Log.e("print", TAG + "  ACTION_DISCOVERY_FINISHED  " + GPrinterData.printerConnManagerList.size());
                if (GPrinterData.printerConnManagerList != null && 0 < GPrinterData.printerConnManagerList.size()) {

                    PrinterConnManager connManager = GPrinterData.printerConnManagerList.get(0);
                    String printAddr = connManager.getMacAddress();

                    //여기서 만약 첫번째 버튼을 누른 상태로 프린터가 되야 한다면    // ??
                    if (GPrinterData.isGPrint && !GPrinterData.TRACKING_NO.equals("")) {
                        if (GPrinterData.gPrinterHandler != null) {

                            Message message = GPrinterData.gPrinterHandler.obtainMessage(GPrinterData.DOUBLE_PRINTER);
                            Bundle bundle = new Bundle();
                            bundle.putString("tracking_no", GPrinterData.TRACKING_NO);
                            bundle.putString("address", printAddr);
                            message.setData(bundle);

                            //먼저 초기화
                            GPrinterData.TRACKING_NO = "";
                            GPrinterData.isGPrint = false;
                            GPrinterData.gPrinterHandler.sendMessage(message);
                        }
                    }
                } else {

                    if (GPrinterData.gPrinterHandler != null) {

                        GPrinterData.gPrinterHandler.obtainMessage(GPrinterData.NONE_PRINTER).sendToTarget();
                    } else {

                        GPrinterData.TEMP_TRACKING_NO = "";
                        if (GPrinterData.mBluetoothAdapter != null) {
                            GPrinterData.mBluetoothAdapter.cancelDiscovery();
                            GPrinterData.mBluetoothAdapter = null;
                        }

                        if (GPrinterData.printerConnManagerList != null) {
                            for (int i = 0; i < GPrinterData.printerConnManagerList.size(); i++) {
                                GPrinterData.printerConnManagerList.get(i).closePort();
                            }
                            GPrinterData.printerConnManagerList = null;
                        }

                        if (GPrinterData.gPrinterHandler != null) {
                            GPrinterData.gPrinterHandler = null;
                        }

                        try {

                            if (GPrinterData.printerReceiver != null) {
                                activity.unregisterReceiver(GPrinterData.printerReceiver);
                                GPrinterData.printerReceiver = null;
                            }
                        } catch (Exception e) {

                            Log.e("Exception", TAG + "  unRegisterReceiver Exception : " + e.toString());
                        }
                    }
                }
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:   // 블루투스 기기 연결 끊어짐.

                if (device != null) {

                    Log.e("print", TAG + "  ACTION_ACL_DISCONNECTED  " + device.getName() + " / " + GPrinterData.printerConnManagerList.size());

                    if (GPrinterData.printerConnManagerList != null && 0 < GPrinterData.printerConnManagerList.size()) {

                        int size = GPrinterData.printerConnManagerList.size();

                        if (device.getAddress().equals(GPrinterData.printerConnManagerList.get(size - 1).getMacAddress())) {
                            GPrinterData.printerConnManagerList.remove((size - 1));
                        }

                        Toast.makeText(context, "device disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case GPrinterData.ACTION_CONN_STATE:   // 블루투스의 연결 상태 변경        // 사용여부 확인

                int state = intent.getIntExtra("state", -1);
                Log.e("print", TAG + "  action_connect_state  state : " + state);

                switch (state) {
                    case GPrinterData.CONN_STATE_DISCONNECT:        // 144

                        Toast.makeText(context, "CONN_STATE_DISCONNECT", Toast.LENGTH_SHORT).show();
                        break;

                    case GPrinterData.CONN_PRINT_DONE:              // 2304

                        Toast.makeText(context, "Print Done!", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }
}