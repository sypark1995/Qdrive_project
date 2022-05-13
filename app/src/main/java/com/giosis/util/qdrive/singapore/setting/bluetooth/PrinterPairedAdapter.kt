package com.giosis.util.qdrive.singapore.setting.bluetooth

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import java.util.*

class PrinterPairedAdapter(private val dataList: ArrayList<PrinterDeviceItem>, private val listener: PairedAdapterListener)
    : RecyclerView.Adapter<PrinterPairedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_setting_printer_paired_device, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val _name = dataList[position].deviceNm
        val _address = dataList[position].deviceAddress

        holder.text_printer_my_item_device_name.text = _name
        if (dataList[position].isFound) {
            holder.text_printer_my_item_device_name.setTextColor(holder.text_printer_my_item_device_name.context.resources.getColor(R.color.color_303030))
            holder.btn_printer_my_item_connect.visibility = View.VISIBLE
        } else {
            holder.text_printer_my_item_device_name.setTextColor(holder.text_printer_my_item_device_name.context.resources.getColor(R.color.color_d4d3d3))
            holder.btn_printer_my_item_connect.visibility = View.GONE
        }

        holder.btn_printer_my_item_modify.setOnClickListener {
            listener.cancelDiscovery()
            val intent = Intent(it.context, ModifyDeviceInfoActivity::class.java)
            val device = listener.getRemoteDevice(_address)
            intent.putExtra("device", device)
            (it.context as Activity).startActivityForResult(intent,
                BluetoothDeviceData.REQUEST_RENAME_PAIR_DEVICE
            )
        }

        holder.btn_printer_my_item_delete.setOnClickListener {
            listener.cancelDiscovery()

            AlertDialog.Builder(it.context).setTitle(it.context.resources.getString(R.string.text_alert))
                    .setMessage(it.context.resources.getString(R.string.msg_sure_disconnect_bluetooth) + " " + _name + "?")
                    .setPositiveButton(it.context.resources.getString(R.string.button_ok)) { dialog, which ->

                        val device = listener.getRemoteDevice(_address)

                        try {
                            if (device != null) {
                                device::class.java.getMethod("removeBond").invoke(device)
                            }
                        } catch (e: java.lang.Exception) {
                            Log.e("Exception", "UnPairing Exception : $e")
                        }

                    }
                    .setNegativeButton(it.context.resources.getString(R.string.button_cancel)) { dialog, which -> }.show()
        }

        holder.btn_printer_my_item_connect.setOnClickListener {
            listener.cancelDiscovery()

            val returnValue = listener.connectDevice(position, _address, _name)

            if (!returnValue) {
                dataList[position].isFound = false
                notifyDataSetChanged()

                Toast.makeText(it.context, it.context.resources.getString(R.string.msg_connection_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val text_printer_my_item_device_name = view.findViewById<TextView>(R.id.text_printer_my_item_device_name)
        val btn_printer_my_item_modify = view.findViewById<Button>(R.id.btn_printer_my_item_modify)
        val btn_printer_my_item_delete = view.findViewById<Button>(R.id.btn_printer_my_item_delete)
        val btn_printer_my_item_connect = view.findViewById<Button>(R.id.btn_printer_my_item_connect)

    }


}