package com.giosis.library.setting.bluetooth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.giosis.library.R
import java.util.*

class PrinterAvailableListAdapter internal constructor(private val context: Context,
                                                       private val availableDevicesArrayList: ArrayList<PrinterDeviceItem>?,
                                                       private val listener: PairedAdapterListener)
    : BaseAdapter() {

    private val TAG = "NewDeviceListBaseAdapter"

    override fun getCount(): Int {
        return if (availableDevicesArrayList != null && availableDevicesArrayList.size > 0) {
            availableDevicesArrayList.size
        } else 0
    }

    override fun getItem(position: Int): Any {
        return availableDevicesArrayList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_setting_printer_available_device, null)
        } else {
            convertView
        }

        val text_printer_other_item_device_name = view.findViewById<TextView>(R.id.text_printer_other_item_device_name)
        val text_printer_other_item_device_address = view.findViewById<TextView>(R.id.text_printer_other_item_device_address)
        val btn_printer_other_item_connect = view.findViewById<Button>(R.id.btn_printer_other_item_connect)

        text_printer_other_item_device_name.text = availableDevicesArrayList!![position].deviceNm
        text_printer_other_item_device_address.text = availableDevicesArrayList[position].deviceAddress

        val _address = availableDevicesArrayList[position].deviceAddress
        btn_printer_other_item_connect.setOnClickListener {

            try {
                val device = listener.getRemoteDevice(_address)
                device?.createBond()
            } catch (e: Exception) {
                Toast.makeText(context, "Pairing Exception : $e", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}