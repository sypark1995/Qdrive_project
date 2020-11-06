package com.giosis.util.qdrive.settings;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.international.R;

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

        if (connectedDeviceArrayList != null && 0 < connectedDeviceArrayList.size()) {
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
        String deviceName = BluetoothDeviceData.getDeviceAliasName(device);

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

        Toast.makeText(context, name + " " + context.getResources().getString(R.string.msg_is_disconnecting), Toast.LENGTH_SHORT).show();

        BluetoothDeviceData.connectedPrinterAddress = null;
        PrinterSettingActivity.connectedItem.clear();
        notifyDataSetChanged();
        PrinterSettingActivity.nullConnectedDevice();

        PrinterSettingActivity.pairedItems.add(new PrinterDeviceItem(name, address, true, false));
        BluetoothDeviceData.printerPairedListAdapter.notifyDataSetChanged();
        PrinterSettingActivity.notnullPairedDevices();
    }
}