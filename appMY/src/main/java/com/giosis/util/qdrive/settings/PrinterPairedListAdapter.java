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

import com.giosis.util.qdrive.international.R;

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

        if (pairedDevicesArrayList != null && 0 < pairedDevicesArrayList.size()) {
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
        final String deviceName = pairedDevicesArrayList.get(position).getDeviceNm();
        final String deviceAddress = pairedDevicesArrayList.get(position).getDeviceAddress();

        text_printer_my_item_device_name.setText(deviceName);

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
                modifyDevice(deviceAddress);
            }
        });


        btn_printer_my_item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothDeviceData.mBluetoothAdapter.cancelDiscovery();

                new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.text_alert))
                        .setMessage(context.getResources().getString(R.string.msg_sure_disconnect_bluetooth) + " " + deviceName + "?")
                        .setPositiveButton(context.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteDevice(deviceAddress);
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
                connectDevice(position, deviceAddress, deviceName);
            }
        });

        return view;
    }


    private void modifyDevice(String address) {

        Intent intent = new Intent(context, ModifyDeviceInfoActivity.class);
        intent.putExtra("address", address);
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

        // NOTIFICATION.
        // 2019.11 - 1개의 프린터만 [Connected Devices]로 올릴 수 있도록 수정
        // List - 'Print Label'을 누를때, 마지막에 연결된 프린터로 프린트 될 수 있도록...

        // 2020.06 - 해당 화면에서 실제 socket 연결이 무의미하므로 연결할 Device Address 정보만 저장하는 것으로 수정
        // socket 연결해도 해당 화면을 나가면 연결이 끊기게 되고, List 화면에서 Device Address 찾아서 또 연결하는 구조

        //  [Connected Device] 가 있을 때
        if (BluetoothDeviceData.connectedPrinterAddress != null) {

            Toast.makeText(context, context.getResources().getString(R.string.msg_only_one_device_connected), Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(context, name + " " + context.getResources().getString(R.string.msg_is_connected), Toast.LENGTH_SHORT).show();
            Log.e("print", TAG + "  Paired Data > " + position + " / " + name + " / " + address);

            // paired list 에서 삭제하고, connected list 에 넣기
            PrinterSettingActivity.pairedItems.remove(position);
            notifyDataSetChanged();

            if (PrinterSettingActivity.pairedItems.size() == 0) {

                PrinterSettingActivity.nullPairedDevices();
            } else {

                PrinterSettingActivity.notnullPairedDevices();
            }

            // [Connected Device] 추가
            PrinterSettingActivity.connectedItem.add(new PrinterDeviceItem(name, address, true, true));
            BluetoothDeviceData.printerConnectedListAdapter.notifyDataSetChanged();

            BluetoothDeviceData.connectedPrinterAddress = address;
            PrinterSettingActivity.notnullConnectedDevice();
        }
    }
}