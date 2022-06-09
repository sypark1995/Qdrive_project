package com.giosis.util.qdrive.singapore.barcodescanner


import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ItemCaptureScannedBinding
import com.giosis.util.qdrive.singapore.list.BarcodeData
import com.giosis.util.qdrive.singapore.util.BarcodeType
import java.util.*


class ScannedBarcodeAdapter(var items: ArrayList<BarcodeData>, private var scanType: String) :
    RecyclerView.Adapter<ScannedBarcodeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding =
            ItemCaptureScannedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemCaptureScannedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val barcodeNumber = items[position].barcode
        val barcodeState = items[position].state

        if (barcodeNumber != null) {

            holder.binding.textBarcode.text = barcodeNumber

            if (barcodeState == "SUCCESS") {

                holder.binding.layoutItem.setBackgroundResource(R.drawable.bg_round_10_cccccc)
                holder.binding.imgBarcode.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode)
                holder.binding.btnState.visibility = View.VISIBLE
                holder.binding.btnState.setBackgroundResource(R.drawable.qdrive_btn_icon_big_on)

                holder.binding.textBarcode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                holder.binding.textBarcode.setTextColor(Color.parseColor("#303030"))

                if (scanType == BarcodeType.CHANGE_DELIVERY_DRIVER) {
                    holder.binding.textBarcode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    holder.binding.btnState.visibility = View.GONE

                } else if (scanType == BarcodeType.OUTLET_PICKUP_SCAN) {
                    holder.binding.layoutItem.setBackgroundResource(R.drawable.bg_round_5_ffcc00)
                }

            } else if (barcodeState == "FAIL") {

                holder.binding.layoutItem.setBackgroundResource(R.drawable.bg_round_10_dedede)
                holder.binding.imgBarcode.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode_off)
                holder.binding.btnState.setBackgroundResource(R.drawable.qdrive_btn_icon_big_off)

                holder.binding.textBarcode.setTextColor(Color.parseColor(("#909090")))

                if (scanType == BarcodeType.OUTLET_PICKUP_SCAN) {
                    holder.binding.btnState.visibility = View.INVISIBLE
                } else {
                    holder.binding.btnState.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}