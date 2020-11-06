package com.giosis.util.qdrive.portableprinter.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.giosis.util.qdrive.settings.BluetoothDeviceData;

/**
 * @author eylee 2018-04-03
 * @editor krm0219  2019.11
 */
public class GPrinterHandler extends Handler {
    private String TAG = "GPrinterHandler";

    private Context context;
    private Activity activity;
    private OnGPrinterReadyListener listener;


    public GPrinterHandler(Context context, Activity activity, OnGPrinterReadyListener listener) {

        this.context = context;
        this.activity = activity;
        this.listener = listener;
    }

    public interface OnGPrinterReadyListener {
        void onStartGprinter(String tracking_no, String mac_addr);
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        // 제일처음은 983 / 그 다음부터는 333 (currentPrinterCommand 셋팅될때까지...)
        Log.e("print_list", TAG + "  handleMessage : " + msg.what);

        switch (msg.what) {

            case GPrinterData.PRINTER_COMMAND_ERROR: {   // 333

                if (!GPrinterData.TRACKING_NO.equals("")) {

                    listener.onStartGprinter(GPrinterData.TRACKING_NO, "");
                    break;
                }

                Toast.makeText(context, "Please select the correct printer instructions", Toast.LENGTH_SHORT).show();
            }
            break;

            case GPrinterData.PRINTER_CONNECT_SUCCESS: {  // 983

                Bundle bundle = msg.getData();
                String tracking_no = bundle.getString("tracking_no");
                String macAddress = bundle.getString("address");

                listener.onStartGprinter(tracking_no, macAddress);

            }
            break;

            case GPrinterData.NONE_PRINTER: {   // 987

                if (GPrinterData.mBluetoothAdapter != null) {
                    GPrinterData.mBluetoothAdapter.cancelDiscovery();
                }

                if (GPrinterData.printerConnManagerList != null) {
                    for (int i = 0; i < GPrinterData.printerConnManagerList.size(); i++) {
                        GPrinterData.printerConnManagerList.get(i).closePort();
                    }
                    GPrinterData.printerConnManagerList = null;
                }

                try {

                    if (GPrinterData.printerReceiver != null) {
                        activity.unregisterReceiver(GPrinterData.printerReceiver);
                        GPrinterData.printerReceiver = null;
                    }
                } catch (Exception e) {
                    Log.w("new", "hi, GPrinterHandler unregisterReceiver Exception :: " + e.toString());
                }


                Toast.makeText(context, "Please check the printer status again.", Toast.LENGTH_SHORT).show();
                BluetoothDeviceData.connectedPrinterAddress = null;

                if (!GPrinterData.TRACKING_NO.equals("")) {

                    listener.onStartGprinter(GPrinterData.TRACKING_NO, "");
                    break;
                }
            }
            break;

            default:
                break;
        }
    }
}