package com.giosis.library.setting.bluetooth

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.giosis.library.R
import java.util.*

class PrinterPairedListAdapter internal constructor(private val context: Context,
                                                    private val pairedDevicesArrayList: ArrayList<PrinterDeviceItem>?,
                                                    private val listener: PairedAdapterListener)
    : BaseAdapter() {

    private val TAG = "PairedListBaseAdapter"

    override fun getCount(): Int {
        return if (pairedDevicesArrayList != null && pairedDevicesArrayList.size > 0) {
            pairedDevicesArrayList.size
        } else 0
    }

    override fun getItem(position: Int): Any {
        return pairedDevicesArrayList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_setting_printer_paired_device, null)
        } else {
            convertView
        }

        val text_printer_my_item_device_name = view.findViewById<TextView>(R.id.text_printer_my_item_device_name)
        val btn_printer_my_item_modify = view.findViewById<Button>(R.id.btn_printer_my_item_modify)
        val btn_printer_my_item_delete = view.findViewById<Button>(R.id.btn_printer_my_item_delete)
        val btn_printer_my_item_connect = view.findViewById<Button>(R.id.btn_printer_my_item_connect)

        val _name = pairedDevicesArrayList!![position].deviceNm
        val _address = pairedDevicesArrayList[position].deviceAddress

        text_printer_my_item_device_name.text = _name
        if (pairedDevicesArrayList[position].isFound) {
            text_printer_my_item_device_name.setTextColor(context.resources.getColor(R.color.color_303030))
            btn_printer_my_item_connect.visibility = View.VISIBLE
        } else {
            text_printer_my_item_device_name.setTextColor(context.resources.getColor(R.color.color_d4d3d3))
            btn_printer_my_item_connect.visibility = View.GONE
        }

        btn_printer_my_item_modify.setOnClickListener {
            listener.cancelDiscovery()
            modifyDevice(_address)
        }

        btn_printer_my_item_delete.setOnClickListener {
            listener.cancelDiscovery()

            AlertDialog.Builder(context).setTitle(context.resources.getString(R.string.text_alert))
                    .setMessage(context.resources.getString(R.string.msg_sure_disconnect_bluetooth) + " " + _name + "?")
                    .setPositiveButton(context.resources.getString(R.string.button_ok)) { dialog, which ->

                        // TODO_kjyoo :  Delete 버튼 누르면, Available Devices 리스트에 나타나야함
                        deleteDevice(_address)
                    }
                    .setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialog, which -> }.show()
        }

        btn_printer_my_item_connect.setOnClickListener {
            listener.cancelDiscovery()

            val returnValue = listener.connectDevice(position, _address, _name)

            if (!returnValue) {
                pairedDevicesArrayList[position].isFound = false
                notifyDataSetChanged()

                Toast.makeText(context, context.resources.getString(R.string.msg_connection_failed), Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun modifyDevice(address: String) {
        val intent = Intent(context, ModifyDeviceInfoActivity::class.java)
        intent.putExtra(BluetoothDeviceData.DEVICE_ID, address)
        intent.putExtra("device", listener.getRemoteDevice(address))
        (context as Activity).startActivityForResult(intent, BluetoothDeviceData.REQUEST_RENAME_PAIR_DEVICE)
    }

    private fun deleteDevice(address: String) {
        val device = listener.getRemoteDevice(address)

        try {
            if (device != null) {
                val method = device.javaClass.getMethod("removeBond", null)
                method.invoke(device, null as Array<Any?>?)
            }
        } catch (e: java.lang.Exception) {
            Log.e("Exception", "$TAG  UnPairing Exception : $e")
        }
    }

}