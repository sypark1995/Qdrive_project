package com.giosis.util.qdrive.singapore.list

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.UploadData
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.main.DeviceDataUploadHelper
import com.giosis.util.qdrive.singapore.util.*
import java.io.File
import java.util.*

class ListUploadFailedAdapter(private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun itemMenuIconClick(view: View)
        fun telePhoneNumberClicked(data: RowItemNotUpload)
        fun mobileNumberClicked(data: RowItemNotUpload)
        fun smsClicked(data: RowItemNotUpload)
        fun live10Clicked(data: RowItemNotUpload)
    }

    private val TAG = "UploadFailedAdapter"
    private var gpsTrackerManager: GPSTrackerManager? = null
    private var gpsEnable = false

    var rowItem = ArrayList<RowItemNotUpload>()
        set(value) {
            rowItem.clear()
            rowItem.addAll(value)
        }

    private var originalRowItem: ArrayList<RowItemNotUpload>
    fun setGpsTrackerManager(gpsTrackerManager: GPSTrackerManager?) {
        this.gpsTrackerManager = gpsTrackerManager
        gpsEnable = this.gpsTrackerManager!!.enableGPSSetting()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_upload_failed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position, itemClickListener)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val layoutListItemCardView: LinearLayout =
            view.findViewById(R.id.layout_list_item_card_view) // background change
        private val layoutFailedChildLayout: LinearLayout =
            view.findViewById(R.id.layout_failed_child_layout)
        private val textListItemDDay: TextView = view.findViewById(R.id.text_list_item_d_day)
        private val textListItemUploadFailedState: TextView =
            view.findViewById(R.id.text_list_item_upload_failed_state)
        private val imgListItemSecureDelivery: ImageView =
            view.findViewById(R.id.img_list_item_secure_delivery)
        private val imgListItemStationIcon: ImageView =
            view.findViewById(R.id.img_list_item_station_icon)
        private val textListItemTrackingNo: TextView =
            view.findViewById(R.id.text_list_item_tracking_no)
        private val textListItemPickupState: TextView =
            view.findViewById(R.id.text_list_item_pickup_state)
        private val imgListItemUpIcon: ImageView = view.findViewById(R.id.img_list_item_up_icon)
        private val textListItemAddress: TextView = view.findViewById(R.id.text_list_item_address)
        private val layoutListItemMenuIcon: FrameLayout =
            view.findViewById(R.id.layout_list_item_menu_icon)
        private val textListItemReceiptName: TextView =
            view.findViewById(R.id.text_list_item_receipt_name)
        private val layoutListItemDeliveryOutletInfo: LinearLayout =
            view.findViewById(R.id.layout_list_item_delivery_outlet_info)
        private val layoutListItemPickupInfo: RelativeLayout =
            view.findViewById(R.id.layout_list_item_pickup_info)
        private val layoutListItemRequest: LinearLayout =
            view.findViewById(R.id.layout_list_item_request)
        private val textListItemRequest: TextView = view.findViewById(R.id.text_list_item_request)
        private val layoutListItemDriverMemo: LinearLayout =
            view.findViewById(R.id.layout_list_item_driver_memo)

        private val layoutListItemChildTelephone: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_telephone)
        private val textListItemChildTelephoneNumber: TextView =
            view.findViewById(R.id.text_list_item_child_telephone_number)
        private val layoutListItemChildMobile: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_mobile)
        private val textListItemChildMobileNumber: TextView =
            view.findViewById(R.id.text_list_item_child_mobile_number)
        private val imgListItemChildSms: ImageView =
            view.findViewById(R.id.img_list_item_child_sms)
        private val imgListItemChildLive10: ImageView =
            view.findViewById(R.id.img_list_item_child_live10)
        private val layoutListItemChildFailedReason: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_failed_reason)
        private val textListItemChildFailedReason: TextView =
            view.findViewById(R.id.text_list_item_child_failed_reason)
        private val layoutListItemChildMemo: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_memo)
        private val textListItemChildMemo: TextView =
            view.findViewById(R.id.text_list_item_child_memo)
        private val layoutListItemChildRequester: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_requester)
        private val textListItemChildRequester: TextView =
            view.findViewById(R.id.text_list_item_child_requester)
        private val imgListItemChildRequesterSign: ImageView =
            view.findViewById(R.id.img_list_item_child_requester_sign)
        private val layoutListItemChildDriver: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_driver)
        private val imgListItemChildDriverSign: ImageView =
            view.findViewById(R.id.img_list_item_child_driver_sign)
        private val btnListItemChildUpload: Button =
            view.findViewById(R.id.btn_list_item_child_upload)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(position: Int, listener: OnItemClickListener) {
            val data = rowItem[position]
            layoutListItemCardView.setOnClickListener {
                data.isClicked = !data.isClicked
                if (data.isClicked) {
                    layoutListItemCardView.setBackgroundResource(R.drawable.bg_top_round_10_ffffff)
                    imgListItemUpIcon.visibility = View.GONE
                    layoutFailedChildLayout.visibility = View.VISIBLE
                } else {
                    layoutListItemCardView.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow)
                    imgListItemUpIcon.visibility = View.VISIBLE
                    layoutFailedChildLayout.visibility = View.GONE
                }
                notifyDataSetChanged()
            }

            var status = ""

            when (data.stat) {
                StatueType.DELIVERY_FAIL -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_d_failed)
                StatueType.DELIVERY_DONE -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_delivered)
                StatueType.PICKUP_FAIL -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_p_failed)
                StatueType.PICKUP_CANCEL -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_p_cancelled)
                StatueType.PICKUP_DONE -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_p_done)
            }

            textListItemUploadFailedState.text = status
            textListItemDDay.visibility = View.GONE
            textListItemUploadFailedState.visibility = View.VISIBLE
            textListItemUploadFailedState.setTextColor(Color.parseColor("#FF0000"))
            imgListItemSecureDelivery.visibility = View.GONE
            imgListItemStationIcon.visibility = View.GONE
            textListItemTrackingNo.text = data.shipping
            textListItemPickupState.visibility = View.GONE
            textListItemAddress.text = data.address
            textListItemReceiptName.text = data.name
            layoutListItemDeliveryOutletInfo.visibility = View.GONE
            layoutListItemPickupInfo.visibility = View.GONE
            layoutListItemDriverMemo.visibility = View.GONE

            if (data.request.isEmpty()) {
                layoutListItemRequest.visibility = View.GONE
            } else {
                layoutListItemRequest.visibility = View.VISIBLE
                textListItemRequest.text = data.request
            }

            layoutListItemMenuIcon.tag = data.shipping // ????????? ???????????? shipping no
            layoutListItemMenuIcon.setOnClickListener {
                listener.itemMenuIconClick(it)
            }

            if (data.items?.get(0)?.secretNoType == "T") {     // Qtalk ???????????? ?????? T - Qnumber ??????
                layoutListItemChildTelephone.visibility = View.GONE
                layoutListItemChildMobile.visibility = View.GONE
                imgListItemChildLive10.visibility = View.VISIBLE
            } else if (data.items?.get(0)?.secretNoType == "P") {  // Phone ???????????? - ???????????? ?????????
                layoutListItemChildTelephone.visibility = View.GONE
                layoutListItemChildMobile.visibility = View.VISIBLE
                textListItemChildMobileNumber.text = data.items?.get(0)?.hp
                imgListItemChildLive10.visibility = View.GONE
            } else {    //???????????? ????????????
                imgListItemChildLive10.visibility = View.GONE
                if (data.items?.get(0)?.tel != null && data.items?.get(0)?.tel!!.length > 5) {
                    layoutListItemChildTelephone.visibility = View.VISIBLE
                    val content = SpannableString(data.items?.get(0)?.tel)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textListItemChildTelephoneNumber.text = content
                } else {
                    layoutListItemChildTelephone.visibility = View.GONE
                }
                if (data.items?.get(0)?.hp != null && data.items?.get(0)?.hp!!.length > 5) {
                    layoutListItemChildMobile.visibility = View.VISIBLE
                    val content = SpannableString(data.items?.get(0)?.hp)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textListItemChildMobileNumber.text = content
                } else {
                    layoutListItemChildMobile.visibility = View.GONE
                }
            }

            //  Reason
            if (data.items?.get(0)?.statReason != null
                && data.items?.get(0)?.statReason!! != " "
                && data.items?.get(0)?.statReason!!.isNotEmpty()
            ) {
                layoutListItemChildFailedReason.visibility = View.VISIBLE
                when (data.items?.get(0)?.stat) {
                    StatueType.DELIVERY_FAIL -> {
                        val reasonText =
                            DataUtil.getDeliveryFailedMsg(data.items?.get(0)?.statReason)
                        textListItemChildFailedReason.text = reasonText
                    }
                    StatueType.PICKUP_FAIL -> {
                        val reasonText = DataUtil.getPickupFailedMsg(data.items?.get(0)?.statReason)
                        textListItemChildFailedReason.text = reasonText
                    }
                }
            } else {
                layoutListItemChildFailedReason.visibility = View.GONE
            }

            // ??????
            if (data.items?.get(0)?.stat != StatueType.PICKUP_DONE) {
                if (data.items?.get(0)?.statMsg!!.isNotEmpty()) {
                    layoutListItemChildMemo.visibility = View.VISIBLE
                    textListItemChildMemo.text = data.items?.get(0)?.statMsg
                } else {
                    layoutListItemChildMemo.visibility = View.GONE
                }
            } else {
                layoutListItemChildMemo.visibility = View.GONE
            }

            val pickupSign = "/QdrivePickup"
            val pickupDriverSign = "/QdriveCollector"
            val deliverySign = "/Qdrive"
            val myBitmap: Bitmap

            when (data.items?.get(0)?.stat) {
                StatueType.DELIVERY_DONE -> {
                    // Delivery   sign 1???
                    var dirPath =
                        itemView.context.getExternalFilesDir(null)!!.absolutePath + deliverySign
                    var filePath = "$dirPath/${data.shipping}.png"
                    var imgFile = File(filePath)

                    if (imgFile.exists()) {
                        myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        textListItemChildRequester.text =
                            textListItemChildRequester.context.resources.getString(R.string.text_receiver)
                        imgListItemChildRequesterSign.setImageBitmap(myBitmap)
                        layoutListItemChildDriver.visibility = View.GONE
                    } else {
                        dirPath =
                            itemView.context.getExternalFilesDir(null)!!.absolutePath + deliverySign
                        filePath = dirPath + "/" + data.shipping + "_1.png"
                        imgFile = File(filePath)
                        if (imgFile.exists()) {
                            myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            textListItemChildRequester.text =
                                textListItemChildRequester.context.resources.getString(R.string.text_receiver)
                            imgListItemChildRequesterSign.setImageBitmap(myBitmap)
                            layoutListItemChildDriver.visibility = View.GONE
                        }
                    }
                }
                StatueType.PICKUP_DONE, StatueType.PICKUP_CANCEL -> {
                    val dirPath =
                        itemView.context.getExternalFilesDir(null)!!.absolutePath + "/" + pickupSign
                    val dirPath2 =
                        itemView.context.getExternalFilesDir(null)!!.absolutePath + "/" + pickupDriverSign
                    val filePath = "$dirPath/${data.shipping}.png"
                    val filePath2 = "$dirPath2/${data.shipping}.png"
                    val imgFile = File(filePath)
                    val imgFile2 = File(filePath2)

                    layoutListItemChildDriver.visibility = View.VISIBLE

                    if (imgFile.exists()) {
                        myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        textListItemChildRequester.text =
                            textListItemChildRequester.context.resources.getString(R.string.text_requestor)
                        imgListItemChildRequesterSign.setImageBitmap(myBitmap)
                    }

                    if (imgFile2.exists()) {
                        val myBitmap2 = BitmapFactory.decodeFile(imgFile2.absolutePath)
                        imgListItemChildDriverSign.setImageBitmap(myBitmap2)
                    }
                }
                else -> {
                    layoutListItemChildRequester.visibility = View.GONE
                    layoutListItemChildDriver.visibility = View.GONE
                }
            }

            textListItemChildTelephoneNumber.setOnClickListener {
                listener.telePhoneNumberClicked(data)
            }

            textListItemChildMobileNumber.setOnClickListener {
                listener.mobileNumberClicked(data)
            }

            imgListItemChildSms.setOnClickListener {
                listener.smsClicked(data)
            }

            imgListItemChildLive10.setOnClickListener {
                listener.live10Clicked(data)
            }

            btnListItemChildUpload.setOnClickListener { v: View ->
                val songjanglist = ArrayList<UploadData>()
                // ????????? ????????? ?????? DB ??????
                val selectQuery = "SELECT invoice_no" +
                        " , stat " +
                        " , ifnull(rcv_type, '')  as rcv_type" +
                        " , ifnull(fail_reason, '')  as fail_reason" +
                        " , ifnull(driver_memo, '') as driver_memo" +
                        " , ifnull(real_qty, '') as real_qty" +
                        " , ifnull(retry_dt , '') as retry_dt" +
                        " , type " +
                        " FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                        " WHERE reg_id= '" + Preferences.userId + "'" +
                        " and invoice_no = '" + data.shipping + "'" +
                        " and punchOut_stat <> 'S' "
                val cs = DatabaseHelper.getInstance()[selectQuery]
                if (cs.moveToFirst()) {
                    do {
                        val data = UploadData()
                        data.noSongjang = cs.getString(cs.getColumnIndex("invoice_no"))
                        data.stat = cs.getString(cs.getColumnIndex("stat"))
                        data.receiveType = cs.getString(cs.getColumnIndex("rcv_type"))
                        data.failReason = cs.getString(cs.getColumnIndex("fail_reason"))
                        data.driverMemo = cs.getString(cs.getColumnIndex("driver_memo"))
                        data.realQty = cs.getString(cs.getColumnIndex("real_qty"))
                        data.retryDay = cs.getString(cs.getColumnIndex("retry_dt"))
                        data.type = cs.getString(cs.getColumnIndex("type"))
                        songjanglist.add(data)
                    } while (cs.moveToNext())
                }
                var latitude = 0.0
                var longitude = 0.0
                if (gpsEnable && gpsTrackerManager != null) {
                    latitude = gpsTrackerManager!!.latitude
                    longitude = gpsTrackerManager!!.longitude
                }

                if (songjanglist.size > 0) {

                    FirebaseEvent.clickEvent(
                        v.context,
                        TAG,
                        "SetDeliveryUploadData / SetPickupUploadData"
                    )

                    DeviceDataUploadHelper.Builder(
                        v.context,
                        Preferences.userId,
                        Preferences.officeCode,
                        Preferences.deviceUUID,
                        songjanglist,
                        "QL",
                        latitude,
                        longitude
                    ).setOnServerEventListener(object : OnServerEventListener {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onPostResult() {
                            try {
                                rowItem.removeAt(adapterPosition)
                            } catch (ignored: Exception) {
                            }
                            originalRowItem = rowItem
                            notifyDataSetChanged()
                        }

                        override fun onPostFailList() {}
                    }).build().execute()

                } else {
                    AlertDialog.Builder(v.context)
                        .setMessage(v.context.resources.getString(R.string.text_data_error))
                        .setTitle(
                            "[" + v.context.resources.getString(R.string.button_upload) + "]"
                        )
                        .setCancelable(false)
                        .setPositiveButton(
                            v.context.resources.getString(R.string.button_ok)
                        ) { _: DialogInterface?, _: Int -> }
                        .show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return rowItem.size
    }

    //Search
    @SuppressLint("NotifyDataSetChanged")
    fun filterData(query: String) {
        var query = query
        query = query.uppercase(Locale.getDefault())
        rowItem.clear()
        if (query.isEmpty()) {
            rowItem.addAll(originalRowItem)
        } else {
            val newList = ArrayList<RowItemNotUpload>()
            newList.addAll(
                originalRowItem.filter {
                    it.name.uppercase(Locale.getDefault())
                        .contains(query) || it.shipping.uppercase(Locale.getDefault())
                        .contains(query)
                }
            )
            if (newList.size > 0) {
                rowItem.addAll(newList)
            }
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSorting(sortedItems: ArrayList<RowItemNotUpload>) {
        rowItem.clear()
        rowItem.addAll(sortedItems)
        originalRowItem = sortedItems
        notifyDataSetChanged()
    }

    init {
        this.rowItem.addAll(rowItem)
        originalRowItem = ArrayList()
        originalRowItem.addAll(rowItem)
    }
}