package com.giosis.util.qdrive.settings;

import android.app.Activity;
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


public class PrinterPairedListAdapter extends BaseAdapter {
    private String TAG = "PairedListBaseAdapter";


    private Context context;
    private ArrayList<PrinterDeviceItem> pairedDevicesArrayList;


    PrinterPairedListAdapter(Context mContext, ArrayList<PrinterDeviceItem> pairedItems) {

        this.context = mContext;
        this.pairedDevicesArrayList = pairedItems;
    }


    @Override
    public int getCount() {

        if (pairedDevicesArrayList != null && pairedDevicesArrayList.size() > 0) {
            return pairedDevicesArrayList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return pairedDevicesArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_setting_printer_paired_device, null);
        } else {

            view = convertView;
        }

        TextView text_printer_my_item_device_name = view.findViewById(R.id.text_printer_my_item_device_name);
        Button btn_printer_my_item_modify = view.findViewById(R.id.btn_printer_my_item_modify);
        Button btn_printer_my_item_delete = view.findViewById(R.id.btn_printer_my_item_delete);
        Button btn_printer_my_item_connect = view.findViewById(R.id.btn_printer_my_item_connect);


        //
        final String _name = pairedDevicesArrayList.get(position).getDeviceNm();
        final String _address = pairedDevicesArrayList.get(position).getDeviceAddress();

        text_printer_my_item_device_name.setText(_name);

        if (pairedDevicesArrayList.get(position).isFound()) {

            text_printer_my_item_device_name.setTextColor(context.getResources().getColor(R.color.color_303030));
            btn_printer_my_item_connect.setVisibility(View.VISIBLE);
        } else {

            text_printer_my_item_device_name.setTextColor(context.getResources().getColor(R.color.color_d4d3d3));
            btn_printer_my_item_connect.setVisibility(View.GONE);
        }


        btn_printer_my_item_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();
                modifyDevice(_address);
            }
        });


        btn_printer_my_item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();

                new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_alert))
                        .setMessage(context.getResources().getString(R.string.msg_sure_disconnect_bluetooth) + " " + _name + "?")
                        .setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteDevice(_address);
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

        btn_printer_my_item_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();
                connectDevice(position, _address, _name);
            }
        });

        return view;
    }


    private void modifyDevice(String address) {

        Intent intent = new Intent(context, ModifyDeviceInfoActivity.class);
        intent.putExtra(BluetoothDeviceData.DEVICE_ID, address);
        ((Activity) context).startActivityForResult(intent, BluetoothDeviceData.REQUEST_RENAME_PAIR_DEVICE);
    }

    private void deleteDevice(String address) {

        BluetoothDevice device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(address);

        try {

            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("Exception", TAG + "  UnPairing Exception : " + e.toString());
        }
    }

    private void connectDevice(int position, String address, String name) {

        // 2019.11 - 1개의 프린터만 [Connected Devices]로 올릴 수 있도록 수정
        // List - 'Print Label'을 누를때, 마지막에 연결된 프린터로 프린트 될 수 있도록...

        //  [Connected Device] 가 있을 때
        if (BluetoothDeviceData.socket != null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_only_one_device_connected), Toast.LENGTH_SHORT).show();
        } else if (BluetoothDeviceData.connectedPrinterAddress != null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_disconnecting_old_device), Toast.LENGTH_SHORT).show();
        } else {

            BluetoothDevice device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(address);

            try {

                BluetoothDeviceData.socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothDeviceData.MY_UUID_INSECURE);
                BluetoothDeviceData.socket.connect();

                Intent intent = new Intent(BluetoothDeviceData.ACTION_CONNECT_STATE);
                intent.putExtra(BluetoothDeviceData.STATE, BluetoothDeviceData.CONN_STATE_CONNECTED);
                intent.putExtra(BluetoothDeviceData.DEVICE_ID, address);
                intent.putExtra(BluetoothDeviceData.DEVICE_NAME, name);
                MyApplication.getContext().sendBroadcast(intent);
            } catch (Exception e) {
                try {

                    if (BluetoothDeviceData.socket != null) {

                        BluetoothDeviceData.socket.close();
                        BluetoothDeviceData.socket = null;
                    }

                    pairedDevicesArrayList.get(position).setFound(false);
                    notifyDataSetChanged();
                } catch (Exception ee) {

                    Log.e("Exception", "Connect Exception2 : " + e.toString());
                }

                Log.e("Exception", "Connect Exception : " + e.toString());
                Toast.makeText(context, context.getResources().getString(R.string.msg_connection_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}