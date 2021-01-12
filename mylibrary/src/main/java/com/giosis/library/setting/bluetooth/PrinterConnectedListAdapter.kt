package com.giosis.library.setting.bluetooth

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

class PrinterConnectedListAdapter internal constructor(private val context: Context,
                                                       private val connectedDeviceArrayList: ArrayList<PrinterDeviceItem>?,
                                                       private val listener: PairedAdapterListener)
    : BaseAdapter() {

    private val TAG = "ConnectedListBaseAdapter"
    override fun getCount(): Int {
        return if (connectedDeviceArrayList != null && connectedDeviceArrayList.size > 0) {
            connectedDeviceArrayList.size
        } else 0
    }

    override fun getItem(position: Int): Any {
        return connectedDeviceArrayList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_setting_printer_connected_device, null)
        } else {
            convertView
        }

        val text_printer_connected_item_device_name = view.findViewById<TextView>(R.id.text_printer_connected_item_device_name)
        val btn_printer_connected_item_disconnect = view.findViewById<Button>(R.id.btn_printer_connected_item_disconnect)
        val _address = connectedDeviceArrayList!![position].deviceAddress
        val _deviceName = connectedDeviceArrayList[position].deviceNm
        val device = listener.getRemoteDevice(_address)
        var deviceName = _deviceName
        try {
            if (device != null) {
                val method = device.javaClass.getMethod("getAliasName")
                if (method != null) {
                    deviceName = method.invoke(device) as String
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        text_printer_connected_item_device_name.text = deviceName
        btn_printer_connected_item_disconnect.setOnClickListener {
            try {
                AlertDialog.Builder(context).setTitle(context.resources.getString(R.string.text_alert))
                        .setMessage(context.resources.getString(R.string.msg_sure_disconnect_bluetooth) + " " + _deviceName + "?")
                        .setPositiveButton(context.resources.getString(R.string.button_ok)) { dialog, which ->
                            listener.cancelDiscovery()
                            disconnectDevice(_address, _deviceName)
                        }
                        .setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialog, which -> }.show()
            } catch (e: Exception) {
                Log.e("Exception", "$TAG  disconnect Dialog Exception : $e")
            }
        }
        return view
    }

    private fun disconnectDevice(address: String, name: String) {
        try {
            listener.closeSocket()

            val intent = Intent(BluetoothDeviceData.ACTION_CONNECT_STATE)
            intent.putExtra(BluetoothDeviceData.STATE, BluetoothDeviceData.CONN_STATE_DISCONNECT)
            intent.putExtra(BluetoothDeviceData.DEVICE_ID, address)
            intent.putExtra(BluetoothDeviceData.DEVICE_NAME, name)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Disconnect Error.\nException : $e", Toast.LENGTH_SHORT).show()
        }
    }
}