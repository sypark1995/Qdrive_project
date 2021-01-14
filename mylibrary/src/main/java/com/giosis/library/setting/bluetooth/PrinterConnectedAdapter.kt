package com.giosis.library.setting.bluetooth

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import java.util.*

class PrinterConnectedAdapter(private val dataList: ArrayList<PrinterDeviceItem>, private val listener: PairedAdapterListener)
    : RecyclerView.Adapter<PrinterConnectedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_setting_printer_connected_device, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val _address = dataList[position].deviceAddress
        val _deviceName = dataList[position].deviceNm
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

        holder.text_printer_connected_item_device_name.text = deviceName
        holder.btn_printer_connected_item_disconnect.setOnClickListener {
            try {
                AlertDialog.Builder(it.context)
                        .setTitle(it.context.resources.getString(R.string.text_alert))
                        .setMessage(it.context.resources.getString(R.string.msg_sure_disconnect_bluetooth) + " " + _deviceName + "?")
                        .setPositiveButton(it.context.resources.getString(R.string.button_ok)) { dialog, which ->
                            listener.cancelDiscovery()
                            disconnectDevice(it.context, _address, _deviceName)
                        }
                        .setNegativeButton(it.context.resources.getString(R.string.button_cancel)) { dialog, which ->

                        }.show()
            } catch (e: Exception) {
                Log.e("Exception", " disconnect Dialog Exception : $e")
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun disconnectDevice(context: Context, address: String, name: String) {
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


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text_printer_connected_item_device_name = view.findViewById<TextView>(R.id.text_printer_connected_item_device_name)
        val btn_printer_connected_item_disconnect = view.findViewById<Button>(R.id.btn_printer_connected_item_disconnect)
    }
}