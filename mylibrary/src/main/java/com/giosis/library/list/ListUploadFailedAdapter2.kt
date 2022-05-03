package com.giosis.library.list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.giosis.library.R
import com.giosis.library.UploadData
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.database.DatabaseHelper.Companion.getInstance
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.main.DeviceDataUploadHelper
import com.giosis.library.util.BarcodeType
import com.giosis.library.util.DataUtil
import com.giosis.library.util.OnServerEventListener
import com.giosis.library.util.Preferences.deviceUUID
import com.giosis.library.util.Preferences.officeCode
import com.giosis.library.util.Preferences.userId
import java.io.File
import java.util.*

class ListUploadFailedAdapter2(
    rowItem: ArrayList<RowItemNotUpload>?,
    listener: AdapterInterface
) : BaseExpandableListAdapter() {

    private val TAG = "UploadFailedAdapter"
    private var gpsTrackerManager: GPSTrackerManager? = null
    private var gpsEnable = false
    private val mCountListener: AdapterInterface
    private val rowItem: ArrayList<RowItemNotUpload> = ArrayList()
    private var originalRowItem: ArrayList<RowItemNotUpload>
    fun setGpsTrackerManager(gpsTrackerManager: GPSTrackerManager?) {
        this.gpsTrackerManager = gpsTrackerManager
        gpsEnable = this.gpsTrackerManager!!.enableGPSSetting()
    }

    interface AdapterInterface {
        fun getFailedCountRefresh()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var view = convertView
        if (view == null) {
            val mInflater =
                parent.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = mInflater.inflate(R.layout.list_group_item, null)
        }
        view?.let {
            val layoutListItemCardView =
                view.findViewById<LinearLayout>(R.id.layout_list_item_card_view) // background change
            val textListItemDDay = view.findViewById<TextView>(R.id.text_list_item_d_day)
            val textListItemUploadFailedState =
                view.findViewById<TextView>(R.id.text_list_item_upload_failed_state)
            val imgListItemSecureDelivery =
                view.findViewById<ImageView>(R.id.img_list_item_secure_delivery)
            val imgListItemStationIcon =
                view.findViewById<ImageView>(R.id.img_list_item_station_icon)
            val textListItemTrackingNo =
                view.findViewById<TextView>(R.id.text_list_item_tracking_no)
            val textListItemPickupState =
                view.findViewById<TextView>(R.id.text_list_item_pickup_state)
            val imgListItemUpIcon = view.findViewById<ImageView>(R.id.img_list_item_up_icon)
            val textListItemAddress = view.findViewById<TextView>(R.id.text_list_item_address)
            val layoutListItemMenuIcon =
                view.findViewById<FrameLayout>(R.id.layout_list_item_menu_icon)
            val textListItemReceiptName =
                view.findViewById<TextView>(R.id.text_list_item_receipt_name)
            val layoutListItemDeliveryOutletInfo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_delivery_outlet_info)
            val layoutListItemPickupInfo =
                view.findViewById<RelativeLayout>(R.id.layout_list_item_pickup_info)
            val layoutListItemRequest =
                view.findViewById<LinearLayout>(R.id.layout_list_item_request)
            val textListItemRequest = view.findViewById<TextView>(R.id.text_list_item_request)
            val layoutListItemDriverMemo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_driver_memo)
            if (isExpanded) {
                layoutListItemCardView.setBackgroundResource(R.drawable.bg_top_round_10_ffffff)
                imgListItemUpIcon.visibility = View.VISIBLE
            } else {
                layoutListItemCardView.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow)
                imgListItemUpIcon.visibility = View.GONE
            }
            if (groupPosition == 0) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 0, 0)
                layoutListItemCardView.layoutParams = lp
            } else {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 20, 0, 0)
                layoutListItemCardView.layoutParams = lp
            }
            val rowPosition = rowItem[groupPosition]
            var status = ""
            when (rowPosition.stat) {
                BarcodeType.DELIVERY_FAIL -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_d_failed)
                BarcodeType.DELIVERY_DONE -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_delivered)
                BarcodeType.PICKUP_FAIL -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_p_failed)
                BarcodeType.PICKUP_CANCEL -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_p_cancelled)
                BarcodeType.PICKUP_DONE -> status =
                    textListItemUploadFailedState.context.resources.getString(R.string.text_p_done)
            }
            textListItemDDay.visibility = View.GONE
            textListItemUploadFailedState.visibility = View.VISIBLE
            textListItemUploadFailedState.setTextColor(Color.parseColor("#FF0000"))
            textListItemUploadFailedState.text = status
            imgListItemSecureDelivery.visibility = View.GONE
            imgListItemStationIcon.visibility = View.GONE
            textListItemTrackingNo.text = rowPosition.shipping
            textListItemPickupState.visibility = View.GONE
            textListItemAddress.text = rowPosition.address
            textListItemReceiptName.text = rowPosition.name
            layoutListItemDeliveryOutletInfo.visibility = View.GONE
            layoutListItemPickupInfo.visibility = View.GONE
            layoutListItemDriverMemo.visibility = View.GONE
            if (rowPosition.request.isEmpty()) {
                layoutListItemRequest.visibility = View.GONE
            } else {
                layoutListItemRequest.visibility = View.VISIBLE
                textListItemRequest.text = rowPosition.request
            }
            layoutListItemMenuIcon.tag = rowPosition.shipping // 퀵메뉴 아이콘에 shipping no
            layoutListItemMenuIcon.setOnClickListener { v: View ->
                val popup =
                    PopupMenu(v.context, layoutListItemMenuIcon)
                popup.menuInflater.inflate(R.menu.quickmenu_failed, popup.menu)
                popup.setOnMenuItemClickListener {
                    val cs3 =
                        getInstance()["SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + layoutListItemMenuIcon.tag
                            .toString() + "' LIMIT 1"]
                    cs3.moveToFirst()
                    val address = cs3.getString(cs3.getColumnIndex("address"))
                    val uri =
                        Uri.parse("http://maps.google.co.in/maps?q=$address")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    v.context.startActivity(intent)
                    true
                }
                popup.show()
            }
        }

        return view!!
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var view = convertView
        if (view == null) {
            val inflater =
                parent.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_list_upload_failed_child, null)
        }
        view?.let {

            val layoutListItemChildTelephone =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_telephone)
            val textListItemChildTelephoneNumber =
                view.findViewById<TextView>(R.id.text_list_item_child_telephone_number)
            val layoutListItemChildMobile =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_mobile)
            val textListItemChildMobileNumber =
                view.findViewById<TextView>(R.id.text_list_item_child_mobile_number)
            val imgListItemChildSms =
                view.findViewById<ImageView>(R.id.img_list_item_child_sms)
            val imgListItemChildLive10 =
                view.findViewById<ImageView>(R.id.img_list_item_child_live10)
            val layoutListItemChildFailedReason =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_failed_reason)
            val textListItemChildFailedReason =
                view.findViewById<TextView>(R.id.text_list_item_child_failed_reason)
            val layoutListItemChildMemo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_memo)
            val textListItemChildMemo =
                view.findViewById<TextView>(R.id.text_list_item_child_memo)
            val layoutListItemChildRequester =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_requester)
            val textListItemChildRequester =
                view.findViewById<TextView>(R.id.text_list_item_child_requester)
            val imgListItemChildRequesterSign =
                view.findViewById<ImageView>(R.id.img_list_item_child_requester_sign)
            val layoutListItemChildDriver =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_driver)
            val imgListItemChildDriverSign =
                view.findViewById<ImageView>(R.id.img_list_item_child_driver_sign)
            val btnListItemChildUpload =
                view.findViewById<Button>(R.id.btn_list_item_child_upload)
            val child = getChild(groupPosition, childPosition) as ChildItemNotUpload
            val trackingNo = rowItem[groupPosition].shipping
            val receiver = rowItem[groupPosition].name
            if (child.secretNoType == "T") {     // Qtalk 안심번호 타입 T - Qnumber 사용
                layoutListItemChildTelephone.visibility = View.GONE
                layoutListItemChildMobile.visibility = View.GONE
                imgListItemChildLive10.visibility = View.VISIBLE
            } else if (child.secretNoType == "P") {  // Phone 안심번호 - 핸드폰만 활성화
                layoutListItemChildTelephone.visibility = View.GONE
                layoutListItemChildMobile.visibility = View.VISIBLE
                textListItemChildMobileNumber.text = child.hp
                imgListItemChildLive10.visibility = View.GONE
            } else {          //안심번호 사용안함
                imgListItemChildLive10.visibility = View.GONE
                if (child.tel != null && child.tel!!.length > 5) {
                    layoutListItemChildTelephone.visibility = View.VISIBLE
                    val content = SpannableString(child.tel)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textListItemChildTelephoneNumber.text = content
                } else {
                    layoutListItemChildTelephone.visibility = View.GONE
                }
                if (child.hp != null && child.hp!!.length > 5) {
                    layoutListItemChildMobile.visibility = View.VISIBLE
                    val content = SpannableString(child.hp)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textListItemChildMobileNumber.text = content
                } else {
                    layoutListItemChildMobile.visibility = View.GONE
                }
            }

            //  Reason
            if (child.statReason != null && !child.statReason!!.contains(" ") && child.statReason!!.isNotEmpty()) {
                layoutListItemChildFailedReason.visibility = View.VISIBLE
                when (child.stat) {
                    BarcodeType.DELIVERY_FAIL -> {
                        val reasonText = DataUtil.getDeliveryFailedMsg(child.statReason)
                        textListItemChildFailedReason.text = reasonText
                    }
                    BarcodeType.PICKUP_FAIL -> {
                        val reasonText = DataUtil.getPickupFailedMsg(child.statReason)
                        textListItemChildFailedReason.text = reasonText
                    }
                }
            } else {
                layoutListItemChildFailedReason.visibility = View.GONE
            }

            // 메모
            if (child.stat != BarcodeType.PICKUP_DONE) {
                if (child.statMsg!!.isNotEmpty()) {
                    layoutListItemChildMemo.visibility = View.VISIBLE
                    textListItemChildMemo.text = child.statMsg
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
            when (child.stat) {
                BarcodeType.DELIVERY_DONE -> {
                    // Delivery   sign 1개
                    var dirPath =
                        Environment.getExternalStorageDirectory().toString() + deliverySign
                    var filePath = "$dirPath/$trackingNo.png"
                    var imgFile = File(filePath)
                    if (imgFile.exists()) {
                        DataUtil.FirebaseSelectEvents("DELIVERY_DONE", "original")
                        myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        textListItemChildRequester.text =
                            textListItemChildRequester.context.resources.getString(R.string.text_receiver)
                        imgListItemChildRequesterSign.setImageBitmap(myBitmap)
                        layoutListItemChildDriver.visibility = View.GONE
                    } else {
                        dirPath =
                            Environment.getExternalStorageDirectory().toString() + deliverySign
                        filePath = dirPath + "/" + trackingNo + "_1.png"
                        imgFile = File(filePath)
                        if (imgFile.exists()) {
                            DataUtil.FirebaseSelectEvents("DELIVERY_DONE", "original")
                            myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            textListItemChildRequester.text =
                                textListItemChildRequester.context.resources.getString(R.string.text_receiver)
                            imgListItemChildRequesterSign.setImageBitmap(myBitmap)
                            layoutListItemChildDriver.visibility = View.GONE
                        }
                    }
                }
                BarcodeType.PICKUP_DONE, BarcodeType.PICKUP_CANCEL -> {
                    val dirPath =
                        Environment.getExternalStorageDirectory().toString() + "/" + pickupSign
                    val dirPath2 =
                        Environment.getExternalStorageDirectory()
                            .toString() + "/" + pickupDriverSign
                    val filePath = "$dirPath/$trackingNo.png"
                    val filePath2 = "$dirPath2/$trackingNo.png"
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
            textListItemChildTelephoneNumber.setOnClickListener { v: View ->
                val callUri = Uri.parse("tel:" + child.tel)
                val intent = Intent(Intent.ACTION_DIAL, callUri)
                v.context.startActivity(intent)
            }
            textListItemChildMobileNumber.setOnClickListener { v: View ->
                val callUri = Uri.parse("tel:" + child.hp)
                val intent = Intent(Intent.ACTION_DIAL, callUri)
                v.context.startActivity(intent)
            }
            imgListItemChildSms.setOnClickListener { v: View ->
                val smsBody = String.format(
                    v.context.resources.getString(R.string.msg_delivery_start_sms), receiver
                )
                val smsUri = Uri.parse("sms:" + child.hp)
                val intent = Intent(Intent.ACTION_SENDTO, smsUri)
                intent.putExtra("sms_body", smsBody)
                v.context.startActivity(intent)
            }
            imgListItemChildLive10.setOnClickListener { v: View ->
                val alert =
                    AlertDialog.Builder(v.context)
                val msg = String.format(
                    v.context.resources.getString(R.string.msg_delivery_start_sms), receiver
                )
                alert.setTitle(v.context.resources.getString(R.string.text_qpost_message))
                val input = EditText(v.context)
                input.setText(msg)
                alert.setView(input)
                alert.setPositiveButton(
                    v.context.resources.getString(R.string.button_send)
                ) { _: DialogInterface?, _: Int ->
                    val value = input.text.toString()
                    // Qtalk sms 전송
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data =
                        Uri.parse("qtalk://link?qnumber=" + child.secretNo + "&msg=" + value + "&link=&execurl=")
                    v.context.startActivity(intent)
                }
                alert.setNegativeButton(
                    v.context.resources.getString(R.string.button_cancel)
                ) { _: DialogInterface?, _: Int -> }
                alert.show()
            }
            btnListItemChildUpload.setOnClickListener { v: View ->
                val songjanglist = ArrayList<UploadData>()
                // 업로드 대상건 로컬 DB 조회
                val selectQuery = "SELECT invoice_no" +
                        " , stat " +
                        " , ifnull(rcv_type, '')  as rcv_type" +
                        " , ifnull(fail_reason, '')  as fail_reason" +
                        " , ifnull(driver_memo, '') as driver_memo" +
                        " , ifnull(real_qty, '') as real_qty" +
                        " , ifnull(retry_dt , '') as retry_dt" +
                        " , type " +
                        " FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                        " WHERE reg_id= '" + userId + "'" +
                        " and invoice_no = '" + trackingNo + "'" +
                        " and punchOut_stat <> 'S' "
                val cs = getInstance()[selectQuery]
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
                    Log.e(
                        "Location",
                        "$TAG btn_list_item_upload  GPSTrackerManager : $latitude  $longitude  "
                    )
                }
                if (songjanglist.size > 0) {
                    DataUtil.logEvent(
                        "button_click",
                        TAG,
                        "SetDeliveryUploadData / SetPickupUploadData"
                    )
                    DeviceDataUploadHelper.Builder(
                        v.context,
                        userId,
                        officeCode,
                        deviceUUID,
                        songjanglist,
                        "QL",
                        latitude,
                        longitude
                    ).setOnServerEventListener(object : OnServerEventListener {
                        override fun onPostResult() {
                            try {
                                rowItem.removeAt(groupPosition)
                            } catch (ignored: Exception) {
                            }
                            DataUtil.uploadFailedListPosition = 0
                            originalRowItem = rowItem
                            notifyDataSetChanged()
                            mCountListener.getFailedCountRefresh()
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
        return view!!
    }

    override fun getGroupCount(): Int {
        return rowItem.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val chList = rowItem[groupPosition].items
        return chList!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return rowItem[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val chList = rowItem[groupPosition].items
        return chList!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    //Search
    fun filterData(query: String) {
        var query = query
        query = query.uppercase(Locale.getDefault())
        rowItem.clear()
        if (query.isEmpty()) {
            rowItem.addAll(originalRowItem)
        } else {
            val newList = ArrayList<RowItemNotUpload>()
            for (rowItem in originalRowItem) {
                //이름 or 송장번호 조회
                if (rowItem.name.uppercase(Locale.getDefault())
                        .contains(query) || rowItem.shipping.uppercase(Locale.getDefault())
                        .contains(query)
                ) {
                    newList.add(rowItem)
                }
            }
            if (newList.size > 0) {
                rowItem.addAll(newList)
            }
        }
        notifyDataSetChanged()
    }

    fun setSorting(sortedItems: ArrayList<RowItemNotUpload>) {
        rowItem.clear()
        rowItem.addAll(sortedItems)
        originalRowItem = sortedItems
        notifyDataSetChanged()
    }

    init {
        this.rowItem.addAll(rowItem!!)
        originalRowItem = ArrayList()
        originalRowItem.addAll(rowItem)
        mCountListener = listener
    }
}