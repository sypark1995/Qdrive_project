package com.giosis.util.qdrive.singapore.list.delivery

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.OutletQrcodeItemBinding
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*


class Outlet7ETrackingNoAdapter(
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

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(position: Int) {
            val data = trackingNoList[position]

            itemView.setOnClickListener {
                if (binding.qrImg.tag == false) {
                    Glide.with(itemView)
                        .load(data.qrCode)
                        .error(R.drawable.qdrive_btn_icon_failed)
                        .into(binding.qrImg)
                }
            }

            Glide.with(itemView)
                .load(data.qrCode)
                .error(R.drawable.qdrive_btn_icon_failed)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.qrImg.tag = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.qrImg.tag = true
                        return false
                    }
                })
                .into(binding.qrImg)

            binding.textQrcodeDate.text =
                data.jobID!!.substring(2, 6) +
                        "-" + data.jobID!!.substring(6, 8) +
                        "-" + data.jobID!!.substring(8, 10)

            binding.textJobId.text = data.jobID!!
            binding.textVendorCode.text = data.vendorCode!!

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
