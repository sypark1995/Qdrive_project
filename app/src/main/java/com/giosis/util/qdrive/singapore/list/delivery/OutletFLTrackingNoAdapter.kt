package com.giosis.util.qdrive.singapore.list.delivery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.databinding.ItemOutletTrackingNoBinding
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class OutletFLTrackingNoAdapter(
    var trackingNoList: ArrayList<OutletDeliveryItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        Collections.sort(trackingNoList, CompareTrackingNoAsc())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ItemOutletTrackingNoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemOutletTrackingNoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val data = trackingNoList[position]

            binding.textSignDOutletItemTrackingNo.text = data.trackingNo
            binding.textSignDOutletItemReceiver.text = data.receiverName
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    override fun getItemCount(): Int {
        return trackingNoList.size
    }


    // Federated Locker - Tracking No Sort
    class CompareTrackingNoAsc : Comparator<OutletDeliveryItem> {
        override fun compare(o1: OutletDeliveryItem, o2: OutletDeliveryItem): Int {
            return o1.trackingNo!!.compareTo(o2.trackingNo!!)
        }
    }
}