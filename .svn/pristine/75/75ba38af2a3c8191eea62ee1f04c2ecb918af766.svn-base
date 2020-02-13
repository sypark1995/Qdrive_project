package com.giosis.util.qdrive.settings;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class PrinterConnectedListAdapter extends BaseAdapter {
    private String TAG = "ConnectedListBaseAdapter";

    private Context context;
    private ArrayList<PrinterDeviceItem> connectedDeviceArrayList;

    PrinterConnectedListAdapter(Context context, ArrayList<PrinterDeviceItem> connectedItemArrayList) {

        this.context = context;
        this.connectedDeviceArrayList = connectedItemArrayList;
    }

    @Override
    public int getCount() {

        if (connectedDeviceArrayList != null && connectedDeviceArrayList.size() > 0) {
            return connectedDeviceArrayList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return connectedDeviceArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_setting_printer_connected_device, null);
        } else {

            view = convertView;
        }


        TextView text_printer_connected_item_device_name = view.findViewById(R.id.text_printer_connected_item_device_name);
        Button btn_printer_connected_item_disconnect = view.findViewById(R.id.btn_printer_connected_item_disconnect);

        final String _address = connectedDeviceArrayList.get(position).getDeviceAddress();
        final String _deviceName = connectedDeviceArrayList.get(position).getDeviceNm();

        BluetoothDevice device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(_address);
        String deviceName = _deviceName;

        try {
            Method method = device.getClass().getMethod("getAliasName");
            if (method != null) {
                deviceName = (String) method.invoke(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        text_printer_connected_item_device_name.setText(deviceName);


        btn_printer_connected_item_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_alert))
                            .setMessage(context.getResources().getString(R.string.msg_sure_disconnect_bluetooth) + " " + _deviceName + "?")
                            .setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();
                                    disconnectDevice(_address, _deviceName);
                                }
                            })
                            .setNegativeButton(context.getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                } catch (Exception e) {

                    Log.e("Exception", TAG + "  disconnect Dialog Exception : " + e.toString());
                }
            }
        });

        return view;
    }

    private void disconnectDevice(String address, String name) {

        try {

            BluetoothDeviceData.socket.close();
            BluetoothDeviceData.socket = null;

            Intent intent = new Intent(BluetoothDeviceData.ACTION_CONNECT_STATE);
            intent.putExtra(BluetoothDeviceData.STATE, BluetoothDeviceData.CONN_STATE_DISCONNECT);
            intent.putExtra(BluetoothDeviceData.DEVICE_ID, address);
            intent.putExtra(BluetoothDeviceData.DEVICE_NAME, name);
            MyApplication.getContext().sendBroadcast(intent);
        } catch (Exception e) {

            Toast.makeText(context, "Disconnect Error.\n" + "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}