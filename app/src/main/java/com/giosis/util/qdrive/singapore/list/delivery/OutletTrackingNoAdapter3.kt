package com.giosis.util.qdrive.singapore.list.delivery

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giosis.util.qdrive.singapore.databinding.OutletQrcodeItemBinding
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class OutletTrackingNoAdapter3(
    var trackingNoList: ArrayList<OutletDeliveryDoneListItem>,
    var route: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
//            if (data.trackingNo == "1") {
            binding.textSignDOutletQrcodeDate.text =
                data.jobID!!.substring(2, 6) + "-" + data.jobID!!.substring(
                    6,
                    8
                ) + "-" + data.jobID!!.substring(8, 10)
            binding.textSignDOutletQrcodeJobId.text = data.jobID!!
            binding.textSignDOutletQrcodeVendorCode.text = data.vendorCode!!
            trustAllHosts()
            Glide.with(itemView)
                .load(data.qrCode)
                .into(binding.imgSignDOutletQrcode)
            binding.btnSignDOutletReload.setOnClickListener {
                trustAllHosts()

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
//            }
            setIsRecyclable(false)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    override fun getItemCount(): Int {
        Log.e(">>>",trackingNoList.size.toString())
        return trackingNoList.size
    }

    private fun resetListItem() {
        val qrcodeListItem = ArrayList<OutletDeliveryDoneListItem>()

        for (i in trackingNoList.indices) {
            if (i == 0) {
                val item = OutletDeliveryDoneListItem()
                item.trackingNo = "1"
                item.jobID = trackingNoList[0].jobID
                item.vendorCode = trackingNoList[0].vendorCode
                item.qrCode = trackingNoList[0].qrCode
                qrcodeListItem.add(item)
            }
            if (i + 1 < trackingNoList.size) {
                if (trackingNoList[i].jobID != trackingNoList[i + 1].jobID) {
                    val item = OutletDeliveryDoneListItem()
                    item.trackingNo = "1"
                    item.jobID = trackingNoList[i + 1].jobID
                    item.vendorCode = trackingNoList[i + 1].vendorCode
                    item.qrCode = trackingNoList[i + 1].qrCode
                    qrcodeListItem.add(item)
                }
            }
        }
        for (i in qrcodeListItem.indices) {
            trackingNoList.add(qrcodeListItem[i])
        }
        Collections.sort(trackingNoList, CompareNameAsc())
    }

    // 리스트 정렬. 1순위 Job ID / 2순위 Tracking No
    class CompareNameAsc :
        Comparator<OutletDeliveryDoneListItem> {
        override fun compare(o1: OutletDeliveryDoneListItem, o2: OutletDeliveryDoneListItem): Int {
            return if (o1.jobID == o2.jobID) {
                o1.trackingNo!!.compareTo(o2.trackingNo!!)
            } else {
                o1.jobID!!.compareTo(o2.jobID!!)
            }
        }
    }

    private fun trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }
        })

        // Install the all-trusting trust manager
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Federated Locker - Tracking No Sort
    inner class CompareTrackingNoAsc :
        Comparator<OutletDeliveryDoneListItem> {
        override fun compare(o1: OutletDeliveryDoneListItem, o2: OutletDeliveryDoneListItem): Int {
            return o1.trackingNo!!.compareTo(o2.trackingNo!!)
        }
    }

    init {
        if (route.contains("7E")) {
            Collections.sort(trackingNoList, CompareNameAsc())
            resetListItem()
        } else if (route.contains("FL")) {
            Collections.sort(trackingNoList, CompareTrackingNoAsc())
        }
    }
}