package com.giosis.util.qdrive.singapore.list.delivery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giosis.util.qdrive.singapore.databinding.OutletQrcodeItemBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OutletTrackingNoAdapter3(
    var trackingNoList: ArrayList<OutletDeliveryItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        val hashMap = HashMap<String, OutletDeliveryItem>()

        // 같은 jobID 1개만 처리 .
        for (item in trackingNoList) {
            if (!item.jobID.isNullOrEmpty()) {
                if (!hashMap.contains(item.jobID)) {
                    hashMap[item.jobID!!] = item
                }
            }
        }
        val dataList = ArrayList(hashMap.values)

        trackingNoList.clear()
        trackingNoList.addAll(dataList)

        Collections.sort(trackingNoList, CompareNameAsc())
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            OutletQrcodeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: OutletQrcodeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val data = trackingNoList[position]

            binding.textSignDOutletQrcodeDate.text =
                data.jobID!!.substring(2, 6) +
                        "-" + data.jobID!!.substring(6, 8) +
                        "-" + data.jobID!!.substring(8, 10)

            binding.textSignDOutletQrcodeJobId.text = data.jobID!!
            binding.textSignDOutletQrcodeVendorCode.text = data.vendorCode!!

            Glide.with(itemView)
                .load(data.qrCode)
                .into(binding.imgSignDOutletQrcode)

            binding.btnSignDOutletReload.setOnClickListener {

                if (data.qrCode == null) {
                    binding.layoutSignDOutletQrcodeLoad.visibility = View.GONE
                    binding.layoutSignDOutletQrcodeReload.visibility = View.VISIBLE
                } else {
                    binding.layoutSignDOutletQrcodeLoad.visibility = View.VISIBLE
                    binding.layoutSignDOutletQrcodeReload.visibility = View.GONE

                    Glide.with(itemView)
                        .load(data.qrCode)
                        .into(binding.imgSignDOutletQrcode)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    override fun getItemCount(): Int {
        return trackingNoList.size
    }

    // 리스트 정렬. 1순위 Job ID / 2순위 Tracking No
    class CompareNameAsc : Comparator<OutletDeliveryItem> {
        override fun compare(o1: OutletDeliveryItem, o2: OutletDeliveryItem): Int {
            return if (o1.jobID == o2.jobID) {
                o1.trackingNo!!.compareTo(o2.trackingNo!!)
            } else {
                o1.jobID!!.compareTo(o2.jobID!!)
            }
        }
    }

}