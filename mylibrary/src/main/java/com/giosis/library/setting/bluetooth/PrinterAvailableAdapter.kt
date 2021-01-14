package com.giosis.library.setting.bluetooth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import java.util.*

class PrinterAvailableAdapter(private val dataList: ArrayList<PrinterDeviceItem>, private val listener: PairedAdapterListener)
    : RecyclerView.Adapter<PrinterAvailableAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_setting_printer_available_device, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text_printer_other_item_device_name.text = dataList[position].deviceNm
        holder.text_printer_other_item_device_address.text = dataList[position].deviceAddress

        holder.btn_printer_other_item_connect.setOnClickListener {

            try {
                val _address = dataList[position].deviceAddress

                val device = listener.getRemoteDevice(_address)
                device?.createBond()
            } catch (e: Exception) {
                Toast.makeText(it.context, "Pairing Exception : $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val text_printer_other_item_device_name = view.findViewById<TextView>(R.id.text_printer_other_item_device_name)
        val text_printer_other_item_device_address = view.findViewById<TextView>(R.id.text_printer_other_item_device_address)
        val btn_printer_other_item_connect = view.findViewById<Button>(R.id.btn_printer_other_item_connect)

    }
}