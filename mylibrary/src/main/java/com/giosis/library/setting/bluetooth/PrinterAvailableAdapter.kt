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

class PrinterAvailableAdapter(
    private val dataList: ArrayList<PrinterDeviceItem>,
    private val listener: PairedAdapterListener
) : RecyclerView.Adapter<PrinterAvailableAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.printer_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceName.text = dataList[position].deviceNm
        holder.deviceAddress.text = dataList[position].deviceAddress

        holder.connectBtn.setOnClickListener {

            try {
                val address = dataList[position].deviceAddress
                val device = listener.getRemoteDevice(address)
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

        val deviceName: TextView = view.findViewById(R.id.device_name_text)
        val deviceAddress: TextView = view.findViewById(R.id.device_address_text)
        val connectBtn: Button = view.findViewById(R.id.connect_btn)

    }
}