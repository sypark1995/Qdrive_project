package com.giosis.util.qdrive.singapore.barcodescanner

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ItemCaptureScannedBinding
import com.giosis.util.qdrive.singapore.list.BarcodeData
import com.giosis.util.qdrive.singapore.util.BarcodeType
import java.util.*

class ScannedBarcodeAdapter(var items: ArrayList<BarcodeData>?, private var scanType: String) :
    RecyclerView.Adapter<ScannedBarcodeAdapter.ViewHolder>() {

    private var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mContext = parent.context
        val binding =
            ItemCaptureScannedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemCaptureScannedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val barcodeNumber = items!![position].barcode
        val barcodeState = items!![position].state

        if (barcodeNumber != null) {

            holder.binding.textBarcode.text = barcodeNumber

            if (barcodeState == "SUCCESS") {

                holder.binding.layoutItem.setBackgroundResource(R.drawable.bg_round_10_cccccc)
                holder.binding.imgBarcode.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode)
                holder.binding.btnState.visibility = View.VISIBLE
                holder.binding.btnState.setBackgroundResource(R.drawable.qdrive_btn_icon_big_on)

                mContext?.let {

                    holder.binding.textBarcode.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        it.resources.getDimension(R.dimen.text_size_36px)
                    )
                    holder.binding.textBarcode.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_303030
                        )
                    )
                }

                if (scanType == BarcodeType.CHANGE_DELIVERY_DRIVER) {

                    mContext?.let {

                        holder.binding.textBarcode.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            it.resources.getDimension(R.dimen.text_size_24px)
                        )
                    }
                    holder.binding.btnState.visibility = View.GONE
                } else if (scanType == BarcodeType.OUTLET_PICKUP_SCAN) {

                    holder.binding.layoutItem.setBackgroundResource(R.drawable.bg_round_5_ffcc00)
                }
            } else if (barcodeState == "FAIL") {

                holder.binding.layoutItem.setBackgroundResource(R.drawable.bg_round_10_dedede)
                holder.binding.imgBarcode.setBackgroundResource(R.drawable.qdrive_btn_icon_barcode_off)
                holder.binding.btnState.setBackgroundResource(R.drawable.qdrive_btn_icon_big_off)

                mContext?.let {

                    holder.binding.textBarcode.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_909090
                        )
                    )
                }

                if (scanType == BarcodeType.OUTLET_PICKUP_SCAN) {

                    holder.binding.btnState.visibility = View.INVISIBLE
                } else {

                    holder.binding.btnState.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        if (items != null) {
            if (0 < items!!.size) {
                return items!!.size
            }
        }
        return 0
    }
}