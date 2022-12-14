package com.giosis.util.qdrive.singapore.list

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothListener
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.FirebaseEvent
import com.giosis.util.qdrive.singapore.util.Preferences
import com.giosis.util.qdrive.singapore.util.StatueType
import java.util.*

class ListInProgressAdapter(bluetoothListener: BluetoothListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "ListInProgressAdapter"
    var bluetoothListener: BluetoothListener
    private var expandedPos = -1
    private var listener: OnItemClickListener? = null

    private var originalRowItem = ArrayList<RowItem>()

    var itemList = ArrayList<RowItem>()
        set(value) {
            itemList.clear()
            itemList.addAll(value)
        }

    init {
        originalRowItem = ArrayList()
        originalRowItem.addAll(itemList)
        this.bluetoothListener = bluetoothListener
    }

    interface OnItemClickListener {
        fun selectItem(v: View, selectedPos: Int, height: Int)
        fun detailClicked(data: RowItem)
        fun telephoneNumberClicked(data: RowItem)
        fun mobileNumberClicked(data: RowItem)
        fun imgSmsClicked(data: RowItem)
        fun live10Clicked(data: RowItem)
        fun qPostClicked(data: RowItem)
        fun driverMemoClicked(data: RowItem)
        fun deliveredClicked(data: RowItem)
        fun deliveryFailedClicked(data: RowItem)
        fun pickupScanClicked(data: RowItem)
        fun pickupZeroQtyClicked(data: RowItem)
        fun pickupVisitLogClicked(data: RowItem)
        fun outletPickupScanClicked(data: RowItem)
        fun quickDeliveredClicked(data: RowItem)
        fun quickFailedClicked(data: RowItem)
        fun cnrFailedClicked(data: RowItem)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_in_progress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: LinearLayout =
            view.findViewById(R.id.layout_list_item_card_view) // background change
        private val textDday = view.findViewById<TextView>(R.id.text_list_item_d_day)
        private val imgSecureDelivery: ImageView =
            view.findViewById(R.id.img_list_item_secure_delivery)
        private val imgStationIcon: ImageView = view.findViewById(R.id.img_list_item_station_icon)
        private val textTrackingNo: TextView = view.findViewById(R.id.text_list_item_tracking_no)
        private val textPickupState: TextView = view.findViewById(R.id.text_list_item_pickup_state)
        private val textEconomy: TextView = view.findViewById(R.id.text_list_item_economy)
        private val textHighAmount: TextView = view.findViewById(R.id.text_list_item_high_amount)
        private val imgItemUpIcon: ImageView = view.findViewById(R.id.img_list_item_up_icon)
        private val textAddress: TextView = view.findViewById(R.id.text_list_item_address)
        private val menuIcon: FrameLayout = view.findViewById(R.id.layout_list_item_menu_icon)
        private val deliveryInfo: LinearLayout =
            view.findViewById(R.id.layout_list_item_delivery_info)
        private val textReceiptName: TextView = view.findViewById(R.id.text_list_item_receipt_name)
        private val deliveryOutletInfo: LinearLayout =
            view.findViewById(R.id.layout_list_item_delivery_outlet_info)
        private val textParcelQty: TextView = view.findViewById(R.id.text_list_item_parcel_qty)
        private val textPickupInfo: RelativeLayout =
            view.findViewById(R.id.layout_list_item_pickup_info)
        private val textDesiredDate: TextView = view.findViewById(R.id.text_list_item_desired_date)
        private val textQty: TextView = view.findViewById(R.id.text_list_item_qty)
        private val layoutRequest: LinearLayout = view.findViewById(R.id.layout_list_item_request)
        private val textRequest: TextView = view.findViewById(R.id.text_list_item_request)
        private val layoutDriverMemo: LinearLayout =
            view.findViewById(R.id.layout_list_item_driver_memo)
        private val textDriverMemo: TextView = view.findViewById(R.id.text_list_item_driver_memo)

        private val childLayout: LinearLayout = view.findViewById(R.id.layout_child_view)
        private val layoutFailed: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_failed)
        private val textFailedReason: TextView =
            view.findViewById(R.id.text_list_item_child_failed_reason)
        private val layoutParcelAmount: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_parcel_amount)
        private val textParcelAmountTitle: TextView =
            view.findViewById(R.id.text_list_item_child_parcel_amount_title)
        private val textParcelAmount: TextView =
            view.findViewById(R.id.text_list_item_child_parcel_amount)
        private val textParcelAmountUnit: TextView =
            view.findViewById(R.id.text_list_item_child_parcel_amount_unit)
        private val layoutTelephone: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_telephone)
        private val textTelephoneNumber: TextView =
            view.findViewById(R.id.text_list_item_child_telephone_number)
        private val layoutMobile: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_mobile)
        private val textMobileNumber: TextView =
            view.findViewById(R.id.text_list_item_child_mobile_number)
        private val imgSms: ImageView = view.findViewById(R.id.img_list_item_child_sms)
        private val imgLive10: ImageView = view.findViewById(R.id.img_list_item_child_live10)
        private val imgQpost: ImageView = view.findViewById(R.id.img_list_item_child_qpost)
        private val imgDriverMemo: ImageView =
            view.findViewById(R.id.img_list_item_child_driver_memo)
        private val layoutChildDeliveryButtons: RelativeLayout =
            view.findViewById(R.id.layout_list_item_child_delivery_buttons)
        private val btnDelivered: Button = view.findViewById(R.id.btn_list_item_child_delivered)
        private val btnDeliveryFailed: Button =
            view.findViewById(R.id.btn_list_item_child_delivery_failed)
        private val layoutQuickButtons: RelativeLayout =
            view.findViewById(R.id.layout_list_item_child_quick_buttons)
        private val btnQuickDelivered: Button =
            view.findViewById(R.id.btn_list_item_child_quick_delivered)
        private val btnQuickFailed: Button =
            view.findViewById(R.id.btn_list_item_child_quick_failed)
        private val layoutPickupButtons: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_pickup_buttons)
        private val btnPickupScan: Button = view.findViewById(R.id.btn_list_item_child_pickup_scan)
        private val btnPickupZeroQty: Button =
            view.findViewById(R.id.btn_list_item_child_pickup_zero_qty)
        private val btnPickupVisitLog: Button =
            view.findViewById(R.id.btn_list_item_child_pickup_visit_log)
        private val layoutCnrButtons: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_cnr_buttons)
        private val btnCnrFailed: Button = view.findViewById(R.id.btn_list_item_child_cnr_failed)
        private val btnChildCnrPrint: Button = view.findViewById(R.id.btn_list_item_child_cnr_print)
        private val layoutOutletPickup: RelativeLayout =
            view.findViewById(R.id.layout_list_item_child_outlet_pickup)
        private val btnOutletPickupScan: Button =
            view.findViewById(R.id.btn_list_item_child_outlet_pickup_scan)
        private val layoutButtons2: LinearLayout =
            view.findViewById(R.id.layout_list_item_child_buttons2)
        private val btnDetailButton: Button =
            view.findViewById(R.id.btn_list_item_child_detail_button)


        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(position: Int) {
            val data = itemList[position]

            textDday.text = data.delay
            if (data.delay == "D+0" || data.delay == "D+1") {
                textDday.setTextColor(Color.parseColor("#303030"))
            } else {
                textDday.setTextColor(Color.parseColor("#FF0000"))
            }

            when (data.outlet_company) {
                "7E" -> {
                    imgStationIcon.visibility = View.VISIBLE
                    imgStationIcon.setBackgroundResource(R.drawable.qdrive_btn_icon_seven)
                }
                "FL" -> {
                    imgStationIcon.visibility = View.VISIBLE
                    imgStationIcon.setBackgroundResource(R.drawable.qdrive_btn_icon_locker)
                }
                else -> {
                    imgStationIcon.visibility = View.GONE
                }
            }

            textTrackingNo.text = data.shipping
            textAddress.text = data.address
            menuIcon.tag = data.shipping
            textReceiptName.text = data.name

            //??????
            if (data.type == StatueType.TYPE_PICKUP) {
                textTrackingNo.setTextColor(Color.parseColor("#363BE7"))
                imgSecureDelivery.visibility = View.GONE
                textEconomy.visibility = View.GONE
                textHighAmount.visibility = View.GONE

                when (data.stat) {
                    StatueType.PICKUP_REASSIGN -> {
                        textPickupState.visibility = View.VISIBLE
                        textPickupState.text =
                            itemView.context.resources.getString(R.string.text_pickup_reassigned)
                    }

                    StatueType.PICKUP_FAIL -> {
                        textPickupState.visibility = View.VISIBLE
                        textPickupState.text =
                            itemView.context.resources.getString(R.string.text_pickup_failed)
                    }
                    else -> {
                        textPickupState.visibility = View.GONE
                    }
                }

                if (data.outlet_store_name != null) {
                    if (data.outlet_company == "7E") {
                        textTrackingNo.text = data.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + data.outlet_store_name + " (" + data.outlet_store_code + ")"
                    } else {
                        textTrackingNo.text = data.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + data.outlet_store_name
                    }
                }

                deliveryInfo.visibility = View.GONE
                deliveryOutletInfo.visibility = View.GONE
                textPickupInfo.visibility = View.VISIBLE
                textDesiredDate.text = data.desiredDate
                textQty.text = data.qty

                if (data.route.equals("RPC", ignoreCase = true)) {
                    textDesiredDate.text = data.desiredDate + " / " + data.desired_time
                }

            } else {       //??????
                textTrackingNo.setTextColor(Color.parseColor("#32BD87"))
                if (data.secure_delivery_yn != null && data.secure_delivery_yn == "Y") {
                    imgSecureDelivery.visibility = View.VISIBLE
                } else {
                    imgSecureDelivery.visibility = View.GONE
                }

                if (data.stat == StatueType.DELIVERY_FAIL) {
                    textPickupState.visibility = View.VISIBLE
                    textPickupState.text =
                        itemView.context.resources.getString(R.string.text_failed)
                } else {
                    textPickupState.visibility = View.GONE
                }

                // High amount
                if (data.high_amount_yn == "Y") {
                    textHighAmount.visibility = View.VISIBLE
                } else {
                    textHighAmount.visibility = View.GONE
                }

                // Economy
                if (data.orderType == "ECO") {
                    textEconomy.visibility = View.VISIBLE
                } else {
                    textEconomy.visibility = View.GONE
                }
                deliveryInfo.visibility = View.VISIBLE
                textPickupInfo.visibility = View.GONE

                if (data.outlet_store_name != null) {

                    //text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name());
                    deliveryOutletInfo.visibility = View.VISIBLE
                    textParcelQty.text = String.format("%d", data.outlet_qty)

                    if (data.outlet_company == "7E") {
                        textTrackingNo.text = data.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + data.outlet_store_name + " (" + data.outlet_store_code + ")"
                    } else {
                        textTrackingNo.text = data.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + data.outlet_store_name
                    }
                    deliveryInfo.visibility = View.GONE

                } else {
                    deliveryOutletInfo.visibility = View.GONE
                }
            }

            //??????
            if (data.request == null || data.request!!.isEmpty()) {
                layoutRequest.visibility = View.GONE
            } else {
                layoutRequest.visibility = View.VISIBLE
                textRequest.text = data.request
            }

            //???????????? ?????? ??????
            if (data.selfMemo == null || data.selfMemo!!.isEmpty()) {
                layoutDriverMemo.visibility = View.GONE
            } else {
                layoutDriverMemo.visibility = View.VISIBLE
                textDriverMemo.text = data.selfMemo
            }

            //  Outlet
            if (data.outlet_company == "7E" || data.outlet_company == "FL") {
                if (data.type == StatueType.TYPE_PICKUP) {
                    textPickupState.text =
                        itemView.context.resources.getString(R.string.text_retrieve)
                } else if (data.type == StatueType.TYPE_DELIVERY) {
                    textPickupState.text =
                        itemView.context.resources.getString(R.string.text_delivery)
                }
                textPickupState.visibility = View.VISIBLE
                layoutRequest.visibility = View.GONE
            }

            //?????? ?????? ????????? ?????? ?????????  Quick Menu
            menuIcon.setOnClickListener { v: View ->
                val popup = PopupMenu(v.context, menuIcon)
                popup.menuInflater.inflate(R.menu.quickmenu, popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    val itemId = item.itemId
                    if (itemId == R.id.menu_one) {
                        val cs =
                            DatabaseHelper.getInstance()["SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + menuIcon.tag
                                .toString() + "' LIMIT 1"]
                        cs.moveToFirst()
                        val address = cs.getString(cs.getColumnIndex("address"))
                        val uri =
                            Uri.parse("http://maps.google.co.in/maps?q=$address")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        v.context.startActivity(intent)

                    } else if (itemId == R.id.menu_up) {
                        if (0 < adapterPosition) {
                            val upItem = itemList.removeAt(adapterPosition)
                            itemList.add(adapterPosition - 1, upItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(itemList)
                            notifyDataSetChanged()
                        }

                    } else if (itemId == R.id.menu_down) {
                        if (adapterPosition < itemList.size - 1) {
                            val downItem = itemList.removeAt(adapterPosition)
                            itemList.add(adapterPosition + 1, downItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(itemList)
                            notifyDataSetChanged()
                        }
                    }
                    true
                }
            }

            if (data.childItems.secretNoType == "T") {    // Qtalk ???????????? ?????? T - Qnumber ??????
                layoutTelephone.visibility = View.GONE
                layoutMobile.visibility = View.GONE
                imgLive10.visibility = View.VISIBLE

            } else if (data.childItems.secretNoType == "P") {  // Phone ???????????? - ???????????? ?????????
                layoutTelephone.visibility = View.GONE
                layoutMobile.visibility = View.VISIBLE
                imgLive10.visibility = View.GONE
                val content = SpannableString(data.childItems.hp)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                textMobileNumber.text = content

            } else {          //???????????? ????????????
                if (data.childItems.tel != null && data.childItems.tel!!.length > 5) {
                    layoutTelephone.visibility = View.VISIBLE
                    val content = SpannableString(data.childItems.tel)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textTelephoneNumber.text = content
                } else {
                    layoutTelephone.visibility = View.GONE
                }

                if (data.childItems.hp != null && data.childItems.hp!!.length > 5) {
                    layoutMobile.visibility = View.VISIBLE
                    val content = SpannableString(data.childItems.hp)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textMobileNumber.text = content
                } else {
                    layoutMobile.visibility = View.GONE
                }
            }

            if (Preferences.authNo.contains("137")) {
                imgLive10.visibility = View.VISIBLE
            } else {
                imgLive10.visibility = View.GONE
            }

            cardView.setOnClickListener {

                expandedPos = adapterPosition
                listener?.selectItem(it, adapterPosition, it.height)
                notifyItemChanged(adapterPosition)
            }

            try {
                //    Log.e(TAG, "Order Type ETC : " + rowItem.get(groupPosition).getOrder_type_etc());
                if (data.order_type_etc != null && data.order_type_etc.equals(
                        "DPC",
                        ignoreCase = true
                    )
                ) {
                    imgQpost.visibility = View.VISIBLE
                } else {
                    imgQpost.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Order Type ETC Exception : $e")
                imgQpost.visibility = View.GONE
            }

            if (expandedPos == adapterPosition) {
                childLayout.visibility = View.VISIBLE
                cardView.setBackgroundResource(R.drawable.bg_top_round_10_ffffff)
                imgItemUpIcon.visibility = View.VISIBLE
            } else {
                childLayout.visibility = View.GONE
                cardView.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow)
                imgItemUpIcon.visibility = View.GONE
            }

            //  Delivery / Pickup  Fail Reason
            if (data.childItems.statReason != null && data.childItems.statReason!!.isNotEmpty()) {

                when (data.childItems.stat) {
                    StatueType.DELIVERY_FAIL -> {
                        layoutFailed.visibility = View.VISIBLE
                        textFailedReason.text =
                            DataUtil.getDeliveryFailedMsg(data.childItems.statReason)
                    }
                    StatueType.PICKUP_FAIL -> {
                        layoutFailed.visibility = View.VISIBLE
                        textFailedReason.text =
                            DataUtil.getPickupFailedMsg(data.childItems.statReason)
                    }
                    else -> {
                        layoutFailed.visibility = View.GONE
                    }
                }

            } else {
                layoutFailed.visibility = View.GONE
            }

            if (data.type == StatueType.TYPE_DELIVERY) {
                textParcelAmountTitle.text =
                    textParcelAmountTitle.context.resources.getString(R.string.text_parcel_amount)

                var parcelAmount = data.parcel_amount
                if (data.parcel_amount == null
                    || data.parcel_amount == ""
                    || data.parcel_amount.lowercase(Locale.getDefault()) == "null"
                ) {
                    parcelAmount = "0.00"
                }
                textParcelAmount.text = parcelAmount

                var parcelAmountUnit = data.currency
                if (parcelAmountUnit == null
                    || parcelAmountUnit == ""
                    || parcelAmountUnit.lowercase(Locale.getDefault()) == "null"
                ) {
                    parcelAmountUnit = "SGD"
                }

                val currencyUnit = when (parcelAmountUnit) {
                    "SGD" -> {
                        "S$"
                    }
                    "USD" -> {
                        "$"
                    }
                    else -> {
                        parcelAmountUnit
                    }
                }

                textParcelAmountUnit.visibility = View.VISIBLE
                textParcelAmountUnit.text = currencyUnit

                if (data.route == "QXQ") {
                    layoutChildDeliveryButtons.visibility = View.GONE
                    layoutQuickButtons.visibility = View.VISIBLE
                } else {
                    layoutChildDeliveryButtons.visibility = View.VISIBLE
                    layoutQuickButtons.visibility = View.GONE
                }

                layoutPickupButtons.visibility = View.GONE
                layoutCnrButtons.visibility = View.GONE
                layoutOutletPickup.visibility = View.GONE

                if (data.outlet_company == "7E" || data.outlet_company == "FL") {
                    layoutParcelAmount.visibility = View.GONE
                    //  VisitLog ??? ????????? DPC3-Out ?????????... 7E ????????? DPC2-Out ????????? ???????????? ???..
                    btnDeliveryFailed.visibility = View.GONE

                    layoutTelephone.visibility = View.GONE
                    layoutMobile.visibility = View.GONE
                    imgSms.visibility = View.GONE
                    imgLive10.visibility = View.GONE
                    imgQpost.visibility = View.GONE
                } else {
                    layoutParcelAmount.visibility = View.VISIBLE
                    btnDeliveryFailed.visibility = View.VISIBLE
                }

                layoutButtons2.visibility = View.GONE

            } else {            // Pickup
                textParcelAmountTitle.text =
                    textParcelAmountTitle.context.resources.getString(R.string.text_name)
                textParcelAmount.text = data.name
                textParcelAmountUnit.visibility = View.GONE
                layoutChildDeliveryButtons.visibility = View.GONE
                layoutQuickButtons.visibility = View.GONE

                //tracking_no ??? ????????? layout ????????????
                val isCNR = isPickupCNR(data.shipping)

                if (isCNR) { // true ?????? cnr      // C&R  ?????????
                    layoutPickupButtons.visibility = View.GONE
                    layoutCnrButtons.visibility = View.VISIBLE
                    layoutOutletPickup.visibility = View.GONE

                } else if (data.outlet_company == "7E" || data.outlet_company == "FL") {       // 7E, FL
                    layoutPickupButtons.visibility = View.GONE
                    layoutCnrButtons.visibility = View.GONE
                    layoutOutletPickup.visibility = View.VISIBLE

                    layoutTelephone.visibility = View.GONE
                    layoutMobile.visibility = View.GONE
                    imgSms.visibility = View.GONE
                    imgLive10.visibility = View.GONE
                    imgQpost.visibility = View.GONE

                } else {    //  ?????? Pickup
                    layoutPickupButtons.visibility = View.VISIBLE
                    layoutCnrButtons.visibility = View.GONE
                    layoutOutletPickup.visibility = View.GONE
                }

                if (Preferences.userNation == "SG") {
                    // Trip
                    if (data.isPrimaryKey) {
                        layoutButtons2.visibility = View.VISIBLE

                        btnDetailButton.setOnClickListener {
                            listener?.detailClicked(data)
                        }

                    } else {
                        layoutButtons2.visibility = View.GONE
                    }

                } else {
                    layoutButtons2.visibility = View.GONE
                }

            }


            textTelephoneNumber.setOnClickListener {
                listener?.telephoneNumberClicked(data)
            }

            textMobileNumber.setOnClickListener {
                listener?.mobileNumberClicked(data)
            }

            imgSms.setOnClickListener {
                listener?.imgSmsClicked(data)
            }

            imgLive10.setOnClickListener {
                listener?.live10Clicked(data)
            }

            imgQpost.setOnClickListener {
                listener?.qPostClicked(data)
            }
            imgDriverMemo.setOnClickListener {
                listener?.driverMemoClicked(data)
            }
            btnDelivered.setOnClickListener {
                listener?.deliveredClicked(data)
            }
            btnDeliveryFailed.setOnClickListener {
                listener?.deliveryFailedClicked(data)
            }
            btnPickupScan.setOnClickListener {
                listener?.pickupScanClicked(data)
            }
            btnPickupZeroQty.setOnClickListener {
                listener?.pickupZeroQtyClicked(data)
            }
            btnPickupVisitLog.setOnClickListener {
                listener?.pickupVisitLogClicked(data)
            }
            // NOTIFICATION.  Outlet Pickup Done
            btnOutletPickupScan.setOnClickListener {
                listener?.outletPickupScanClicked(data)
            }
            btnQuickDelivered.setOnClickListener {
                listener?.quickDeliveredClicked(data)
            }
            btnQuickFailed.setOnClickListener {
                listener?.quickFailedClicked(data)
            }
            btnCnrFailed.setOnClickListener {
                listener?.cnrFailedClicked(data)
            }

            btnChildCnrPrint.setOnClickListener {
                FirebaseEvent.clickEvent(it.context, TAG, "btnChildCnrPrint clickevent")
                bluetoothListener.isConnectPortablePrint(data.shipping)
            }
        }
    }

    //eylee pickup C&R number check
    private fun isPickupCNR(trackingNo: String): Boolean {
        var isCNR = false
        if (trackingNo != "") {
            val scanNoFirst = trackingNo.substring(0, 1).uppercase(Locale.getDefault())
            val scanNoTwo = trackingNo.substring(0, 2).uppercase(Locale.getDefault())
            when {
                scanNoTwo == "FL" -> {
                    return false
                }
                scanNoFirst == "7" -> {
                    return false
                }
                scanNoFirst != "P" -> { // cnr ??? ???, true
                    isCNR = true
                }
            }
        }
        return isCNR
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    //Search
    @SuppressLint("NotifyDataSetChanged")
    fun filterData(query: String) {

        try {
            val queryUpper = query.uppercase(Locale.getDefault())
            itemList.clear()

            if (queryUpper.isEmpty()) {
                itemList.addAll(originalRowItem)

            } else {
                val newList = ArrayList<RowItem>()
                for (rowItem in originalRowItem) {
                    //?????? or ???????????? ??????
                    if (rowItem.name.uppercase(Locale.getDefault()).contains(queryUpper)
                        || rowItem.shipping.uppercase(Locale.getDefault()).contains(queryUpper)
                    ) {
                        newList.add(rowItem)
                    }
                }

                if (0 < newList.size) {
                    itemList.addAll(newList)
                }
            }
            notifyDataSetChanged()

        } catch (e: Exception) {
            Log.e(TAG, "filterData  Exception  $e")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSorting(sortedItems: ArrayList<RowItem>?) {
        itemList.clear()
        itemList.addAll(sortedItems!!)
        originalRowItem.clear()
        originalRowItem.addAll(sortedItems)
        notifyDataSetChanged()
    }


}