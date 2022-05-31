package com.giosis.util.qdrive.singapore.list.delivery

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.util.DisplayUtil
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class OutletTrackingNoAdapter2(
    var trackingNoList: ArrayList<OutletDeliveryDoneListItem>?,
    var route: String
) : BaseAdapter() {
    var TAG = "OutletTrackingNoAdapter"

    private fun resetListItem() {
        val qrcodeListItem = ArrayList<OutletDeliveryDoneListItem>()
        for (i in trackingNoList!!.indices) {
            if (i == 0) {
                val item = OutletDeliveryDoneListItem()
                item.trackingNo = "1"
                item.jobID = trackingNoList!![0].jobID
                item.vendorCode = trackingNoList!![0].vendorCode
                item.qrCode = trackingNoList!![0].qrCode
                qrcodeListItem.add(item)
            }
            if (i + 1 < trackingNoList!!.size) {
                if (trackingNoList!![i].jobID != trackingNoList!![i + 1].jobID) {
                    val item = OutletDeliveryDoneListItem()
                    item.trackingNo = "1"
                    item.jobID = trackingNoList!![i + 1].jobID
                    item.vendorCode = trackingNoList!![i + 1].vendorCode
                    item.qrCode = trackingNoList!![i + 1].qrCode
                    qrcodeListItem.add(item)
                }
            }
        }
        for (i in qrcodeListItem.indices) {
            trackingNoList!!.add(qrcodeListItem[i])
        }
        Collections.sort(trackingNoList!!, CompareNameAsc())
    }

    override fun getCount(): Int {
        return if (trackingNoList != null) {
            trackingNoList!!.size
        } else 0
    }

    override fun getItem(position: Int): Any {
        return trackingNoList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, viewGroup: ViewGroup): View {
        val item = trackingNoList!![position]
        var view: View? = null
        if (convertView == null) {
            val inflater =
                view!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            if (item.trackingNo == "1") {
                view = inflater.inflate(R.layout.outlet_qrcode_item, null)
                val layout_sign_d_outlet_qrcode_load =
                    view.findViewById<RelativeLayout>(R.id.layout_sign_d_outlet_qrcode_load)
                val text_sign_d_outlet_qrcode_date =
                    view.findViewById<TextView>(R.id.text_sign_d_outlet_qrcode_date)
                val text_sign_d_outlet_qrcode_job_id =
                    view.findViewById<TextView>(R.id.text_sign_d_outlet_qrcode_job_id)
                val text_sign_d_outlet_qrcode_vendor_code =
                    view.findViewById<TextView>(R.id.text_sign_d_outlet_qrcode_vendor_code)
                val img_sign_d_outlet_qrcode =
                    view.findViewById<ImageView>(R.id.img_sign_d_outlet_qrcode)
                val layout_sign_d_outlet_qrcode_reload =
                    view.findViewById<LinearLayout>(R.id.layout_sign_d_outlet_qrcode_reload)
                val btn_sign_d_outlet_reload =
                    view.findViewById<Button>(R.id.btn_sign_d_outlet_reload)
                btn_sign_d_outlet_reload.setOnClickListener {
                    val qrCodeAsyncTask =
                        QRCodeAsyncTask(
                            layout_sign_d_outlet_qrcode_load,
                            layout_sign_d_outlet_qrcode_reload,
                            img_sign_d_outlet_qrcode,
                            item.qrCode!!
                        )
                    qrCodeAsyncTask.execute()
                }
                text_sign_d_outlet_qrcode_date.text =
                    item.jobID!!.substring(2, 6) + "-" + item.jobID!!.substring(
                        6,
                        8
                    ) + "-" + item.jobID!!.substring(8, 10)
                text_sign_d_outlet_qrcode_job_id.text = item.jobID
                text_sign_d_outlet_qrcode_vendor_code.text = item.vendorCode
                val qrCodeAsyncTask = QRCodeAsyncTask(
                    layout_sign_d_outlet_qrcode_load,
                    layout_sign_d_outlet_qrcode_reload,
                    img_sign_d_outlet_qrcode,
                    item.qrCode!!
                )
                qrCodeAsyncTask.execute()
            } else {
                view = inflater.inflate(R.layout.item_outlet_tracking_no, null)
                val layout_sign_d_outlet_item_tracking_no =
                    view.findViewById<RelativeLayout>(R.id.layout_sign_d_outlet_item_tracking_no)
                val text_sign_d_outlet_item_tracking_no =
                    view.findViewById<TextView>(R.id.text_sign_d_outlet_item_tracking_no)
                val text_sign_d_outlet_item_receiver =
                    view.findViewById<TextView>(R.id.text_sign_d_outlet_item_receiver)
                text_sign_d_outlet_item_tracking_no.text = item.trackingNo
                text_sign_d_outlet_item_receiver.text = item.receiverName
                if (route.contains("FL")) {
                    if (trackingNoList!!.size == 1) {
                        layout_sign_d_outlet_item_tracking_no.setPadding(
                            0,
                            dpTopx(view.context, 20f),
                            0,
                            dpTopx(view.context, 20f)
                        )
                    } else {
                        if (position == 0) {
                            layout_sign_d_outlet_item_tracking_no.setPadding(
                                0,
                                dpTopx(view.context, 20f),
                                0,
                                dpTopx(view.context, 7f)
                            )
                        } else if (position == trackingNoList!!.size - 1) {
                            layout_sign_d_outlet_item_tracking_no.setPadding(
                                0,
                                dpTopx(view.context, 7f),
                                0,
                                dpTopx(view.context, 20f)
                            )
                        } else {
                            layout_sign_d_outlet_item_tracking_no.setPadding(
                                0,
                                dpTopx(view.context, 7f),
                                0,
                                dpTopx(view.context, 7f)
                            )
                        }
                    }
                }
            }
        }
        return view!!
    }

    // Federated Locker - Tracking No Sort
    internal inner class CompareTrackingNoAsc :
        Comparator<OutletDeliveryDoneListItem> {
        override fun compare(o1: OutletDeliveryDoneListItem, o2: OutletDeliveryDoneListItem): Int {
            return o1.trackingNo!!.compareTo(o2.trackingNo!!)
        }
    }

    private fun dpTopx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    // 리스트 정렬. 1순위 Job ID / 2순위 Tracking No
    internal inner class CompareNameAsc :
        Comparator<OutletDeliveryDoneListItem> {
        override fun compare(o1: OutletDeliveryDoneListItem, o2: OutletDeliveryDoneListItem): Int {
            return if (o1.jobID == o2.jobID) {
                o1.trackingNo!!.compareTo(o2.trackingNo!!)
            } else {
                o1.jobID!!.compareTo(o2.jobID!!)
            }
        }
    }

    inner class QRCodeAsyncTask(
        var layout_sign_d_outlet_qrcode_load: RelativeLayout,
        var layout_sign_d_outlet_qrcode_reload: LinearLayout,
        var img_sign_d_outlet_qrcode: ImageView,
        var imgUrl: String
    ) :
        AsyncTask<Void?, Void?, Bitmap?>() {
        var progressDialog: ProgressDialog = ProgressDialog(img_sign_d_outlet_qrcode.context)
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.show()
        }


        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            DisplayUtil.dismissProgressDialog(progressDialog)
            if (bitmap != null) {
                layout_sign_d_outlet_qrcode_load.visibility = View.VISIBLE
                layout_sign_d_outlet_qrcode_reload.visibility = View.GONE
                img_sign_d_outlet_qrcode.setImageBitmap(bitmap)
            } else {
                layout_sign_d_outlet_qrcode_load.visibility = View.GONE
                layout_sign_d_outlet_qrcode_reload.visibility = View.VISIBLE
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

        init {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage(img_sign_d_outlet_qrcode.context.resources.getString(R.string.text_please_wait))
            progressDialog.setCancelable(false)
        }

        override fun doInBackground(vararg params: Void?): Bitmap? {
            return try {
                val url = URL(imgUrl)
                trustAllHosts()
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e("Exception", "$TAG   QRCode to Bitmap Exception : $e")
                e.printStackTrace()
                null
            }
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