package com.giosis.util.qdrive.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;

import java.util.ArrayList;


public class PrinterAvailableListAdapter extends BaseAdapter {
    private String TAG = "NewDeviceListBaseAdapter";

    private Context context;
    private ArrayList<PrinterDeviceItem> availableDevicesArrayList;


    PrinterAvailableListAdapter(Context mContext, ArrayList<PrinterDeviceItem> newDeviceArrayList) {

        this.context = mContext;
        this.availableDevicesArrayList = newDeviceArrayList;
    }

    @Override
    public int getCount() {

        if (availableDevicesArrayList != null && availableDevicesArrayList.size() > 0) {
            return availableDevicesArrayList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return availableDevicesArrayList.get(position);
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
            view = inflater.inflate(R.layout.item_setting_printer_available_device, null);
        } else {

            view = convertView;
        }

        TextView text_printer_other_item_device_name = view.findViewById(R.id.text_printer_other_item_device_name);
        TextView text_printer_other_item_device_address = view.findViewById(R.id.text_printer_other_item_device_address);
        Button btn_printer_other_item_connect = view.findViewById(R.id.btn_printer_other_item_connect);


        //
        text_printer_other_item_device_name.setText(availableDevicesArrayList.get(position).getDeviceNm());
        text_printer_other_item_device_address.setText(availableDevicesArrayList.get(position).getDeviceAddress());

        final String _address = availableDevicesArrayList.get(position).getDeviceAddress();

        btn_printer_other_item_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    BluetoothDevice device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(_address);
                    device.createBond();
                } catch (Exception e) {

                    Toast.makeText(context, "Pairing Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}