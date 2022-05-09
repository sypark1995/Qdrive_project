package com.giosis.library.list

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.giosis.library.R
import com.giosis.library.barcodescanner.CaptureActivity1
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.list.delivery.DeliveryDoneActivity
import com.giosis.library.list.delivery.DeliveryFailedActivity
import com.giosis.library.list.delivery.QuickReturnFailedActivity
import com.giosis.library.list.delivery.QuickReturnedActivity
import com.giosis.library.list.pickup.OutletPickupStep1Activity
import com.giosis.library.list.pickup.PickupFailedActivity
import com.giosis.library.list.pickup.PickupZeroQtyActivity
import com.giosis.library.message.CustomerMessageListDetailActivity
import com.giosis.library.util.BarcodeType
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import java.util.*
import kotlin.collections.ArrayList

class ListInProgressAdapter(bluetoothListener: BluetoothListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "ListInProgressAdapter"
    var bluetoothListener: BluetoothListener
    private var expandedPos = -1
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun selectItem(v: View, selectedPos: Int, height: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    var itemList = ArrayList<RowItem>()
        set(value) {
            itemList.clear()
            itemList.addAll(value)
        }

    private var originalRowItem = ArrayList<RowItem>()

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
            if (adapterPosition == 0) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 0, 0)
                cardView.layoutParams = lp
            } else {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 24, 0, 0)
                cardView.layoutParams = lp
            }

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

            // 참조 픽업번호가 있으면 해당 번호로 표시, 없으면 기존 픽업번호 (Ref. Pickup No)
            if (data.type == "P") {        // Pickup
                if (data.ref_pickup_no != "") {
                    textTrackingNo.text = data.ref_pickup_no
                } else {
                    textTrackingNo.text = data.shipping
                }
            } else {        // Delivery
                textTrackingNo.text = data.shipping
            }

            textAddress.text = data.address
            menuIcon.tag = data.shipping
            textReceiptName.text = data.name

            //픽업
            if (data.type == BarcodeType.TYPE_PICKUP) {
                textTrackingNo.setTextColor(Color.parseColor("#363BE7"))
                imgSecureDelivery.visibility = View.GONE
                textEconomy.visibility = View.GONE
                textHighAmount.visibility = View.GONE

                when (data.stat) {
                    BarcodeType.PICKUP_REASSIGN -> {
                        textPickupState.visibility = View.VISIBLE
                        textPickupState.text =
                            itemView.context.resources.getString(R.string.text_pickup_reassigned)
                    }

                    BarcodeType.PICKUP_FAIL -> {
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

            } else {       //배송
                textTrackingNo.setTextColor(Color.parseColor("#32BD87"))
                if (data.secure_delivery_yn != null && data.secure_delivery_yn == "Y") {
                    imgSecureDelivery.visibility = View.VISIBLE
                } else {
                    imgSecureDelivery.visibility = View.GONE
                }

                if (data.stat == BarcodeType.DELIVERY_FAIL) {
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

            //요청
            if (data.request == null || data.request!!.isEmpty()) {
                layoutRequest.visibility = View.GONE
            } else {
                layoutRequest.visibility = View.VISIBLE
                textRequest.text = data.request
            }

            //드라이버 셀프 메모
            if (data.selfMemo == null || data.selfMemo!!.isEmpty()) {
                layoutDriverMemo.visibility = View.GONE
            } else {
                layoutDriverMemo.visibility = View.VISIBLE
                textDriverMemo.text = data.selfMemo
            }

            //  Outlet
            if (data.outlet_company == "7E" || data.outlet_company == "FL") {
                if (data.type == BarcodeType.TYPE_PICKUP) {
                    textPickupState.text =
                        itemView.context.resources.getString(R.string.text_retrieve)
                } else if (data.type == BarcodeType.TYPE_DELIVERY) {
                    textPickupState.text =
                        itemView.context.resources.getString(R.string.text_delivery)
                }
                textPickupState.visibility = View.VISIBLE
                layoutRequest.visibility = View.GONE
            }

            //우측 메뉴 아이콘 클릭 이벤트  Quick Menu
            menuIcon.setOnClickListener { v: View ->
                val popup =
                    PopupMenu(v.context, menuIcon)
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
            if (data.items?.get(0)?.secretNoType == "T") {    // Qtalk 안심번호 타입 T - Qnumber 사용
                layoutTelephone.visibility = View.GONE
                layoutMobile.visibility = View.GONE
                imgLive10.visibility = View.VISIBLE
            } else if (data.items?.get(0)?.secretNoType == "P") {  // Phone 안심번호 - 핸드폰만 활성화
                layoutTelephone.visibility = View.GONE
                layoutMobile.visibility = View.VISIBLE
                imgLive10.visibility = View.GONE
                val content = SpannableString(data.items?.get(0)?.hp)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                textMobileNumber.text = content
            } else {          //안심번호 사용안함
                if (data.items?.get(0)?.tel != null && data.items?.get(0)?.tel!!.length > 5) {
                    layoutTelephone.visibility = View.VISIBLE
                    val content = SpannableString(data.items?.get(0)?.tel)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textTelephoneNumber.text = content
                } else {
                    layoutTelephone.visibility = View.GONE
                }

                if (data.items?.get(0)?.hp != null && data.items?.get(0)?.hp!!.length > 5) {
                    layoutMobile.visibility = View.VISIBLE
                    val content = SpannableString(data.items?.get(0)?.hp)
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
                notifyDataSetChanged()
                childLayout.measure(0, 0)
                listener?.selectItem(it, expandedPos, it.height + childLayout.measuredHeight)
            }

            try {
                val orderType = data.order_type_etc
                //    Log.e(TAG, "Order Type ETC : " + rowItem.get(groupPosition).getOrder_type_etc());
                if (orderType != null && orderType.equals("DPC", ignoreCase = true)) {
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
            if (data.items?.get(0)?.statReason != null && data.items?.get(0)?.statReason!!.isNotEmpty()) {
                val reasonText: String?
                when (data.items?.get(0)?.stat) {
                    BarcodeType.DELIVERY_FAIL -> {
                        reasonText = DataUtil.getDeliveryFailedMsg(data.items?.get(0)?.statReason)
                        layoutFailed.visibility = View.VISIBLE
                        textFailedReason.text = reasonText
                    }
                    BarcodeType.PICKUP_FAIL -> {
                        reasonText = DataUtil.getPickupFailedMsg(data.items?.get(0)?.statReason)
                        layoutFailed.visibility = View.VISIBLE
                        textFailedReason.text = reasonText
                    }
                    else -> {
                        layoutFailed.visibility = View.GONE
                    }
                }
            } else {
                layoutFailed.visibility = View.GONE
            }

            if (data.type == BarcodeType.TYPE_DELIVERY) {
                textParcelAmountTitle.text =
                    itemView.context.resources.getString(R.string.text_parcel_amount)
                var parcelAmount = data.parcel_amount
                if (parcelAmount == null) {
                    parcelAmount = "0.00"
                } else if (parcelAmount == "" || parcelAmount.lowercase(Locale.getDefault()) == "null") {
                    parcelAmount = "0.00"
                }
                textParcelAmount.text = parcelAmount

                var parcelAmountUnit = data.currency
                if (parcelAmountUnit == null) {
                    parcelAmountUnit = "SGD"
                } else if (parcelAmountUnit == "" || parcelAmountUnit.lowercase(Locale.getDefault()) == "null") {
                    parcelAmountUnit = "SGD"
                }

                val currencyUnit: String = when (parcelAmountUnit) {
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
                    //  VisitLog 시 화물이 DPC3-Out 처리됨... 7E 화물은 DPC2-Out 까지만 처리되야 함..
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
                    itemView.context.resources.getString(R.string.text_name)
                textParcelAmount.text = data.name
                textParcelAmountUnit.visibility = View.GONE
                layoutChildDeliveryButtons.visibility = View.GONE
                layoutQuickButtons.visibility = View.GONE

                //tracking_no 에 따라서 layout 선택하기
                val isNotCNR = isPickupNotCNR(data.shipping)

                //TEST.  CNR
                /* isNotCNR = true;*/

                if (isNotCNR) { // true 이면 cnr      // C&R  주문건
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

                } else {    //  일반 Pickup
                    layoutPickupButtons.visibility = View.VISIBLE
                    layoutCnrButtons.visibility = View.GONE
                    layoutOutletPickup.visibility = View.GONE
                }

                if (Preferences.userNation == "SG") {
                    // Trip
                    if (data.isPrimaryKey) {
                        layoutButtons2.visibility = View.VISIBLE
                    } else {
                        layoutButtons2.visibility = View.GONE
                    }
                } else {
                    layoutButtons2.visibility = View.GONE
                }


            }

            if (Preferences.userNation == "SG") {
                btnDetailButton.visibility = View.VISIBLE
                btnDetailButton.setOnClickListener { v: View ->
                    val tripDataArrayList =
                        data.tripSubDataArrayList
                    val dialog = PickupTripDetailDialog(
                        v.context,
                        tripDataArrayList!!, bluetoothListener
                    )
                    dialog.show()
                    val window = dialog.window
                    window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    window.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            } else {
                btnDetailButton.visibility = View.GONE
            }

            textTelephoneNumber.setOnClickListener { v: View ->
                val callUri = Uri.parse("tel:" + data.items?.get(0)?.tel)
                val intent = Intent(Intent.ACTION_DIAL, callUri)
                v.context.startActivity(intent)
            }

            textMobileNumber.setOnClickListener { v: View ->
                val callUri = Uri.parse("tel:" + data.items?.get(0)?.hp)
                val intent = Intent(Intent.ACTION_DIAL, callUri)
                v.context.startActivity(intent)
            }

            imgSms.setOnClickListener { v: View ->
                try {
                    val smsBody = String.format(
                        v.context.resources.getString(R.string.msg_delivery_start_sms),
                        data.name
                    )
                    val smsUri = Uri.parse("sms:" + data.items?.get(0)?.hp)
                    val intent = Intent(Intent.ACTION_SENDTO, smsUri)
                    intent.putExtra("sms_body", smsBody)
                    v.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        v.context,
                        v.context.resources.getString(R.string.msg_send_sms_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            imgLive10.setOnClickListener { v: View ->
                val pQlpsCustNo = data.custNo
                val pDeliveryType = data.type //P,D
                val pOrderType = data.route // RPC, C2C, GIO
                val pTrackingNo = data.shipping
                val pSellerId = data.partnerID
                val sendLive10Message = SendLive10Message(v.context)
                sendLive10Message.dialogSelectOption(
                    v.context,
                    pQlpsCustNo!!, pDeliveryType, pOrderType, pTrackingNo, pSellerId!!
                )
            }

            imgQpost.setOnClickListener { view: View ->
                val intent = Intent(
                    view.context,
                    CustomerMessageListDetailActivity::class.java
                )
                intent.putExtra("tracking_no", data.shipping)
                view.context.startActivity(intent)
            }

            imgDriverMemo.setOnClickListener { v: View ->
                val alert = AlertDialog.Builder(v.context)
                val msg = data.selfMemo
                val shipping = data.shipping
                alert.setTitle(v.context.resources.getString(R.string.text_driver_memo1))
                alert.setMessage(shipping)

                // Set an EditText view to get user input
                val input = EditText(v.context)
                input.setText(msg)
                input.setTextColor(Color.BLACK)
                alert.setView(input)
                alert.setPositiveButton(
                    v.context.resources.getString(R.string.button_ok)
                ) { _: DialogInterface?, _: Int ->
                    val selfMemo = input.text.toString()
                    val contentVal = ContentValues()
                    contentVal.put("self_memo", selfMemo)
                    DatabaseHelper.getInstance().update(
                        DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                        "invoice_no= ? COLLATE NOCASE ", arrayOf(data.shipping)
                    )
                    data.selfMemo = selfMemo
                    notifyDataSetChanged()
                }
                    .setNegativeButton(
                        v.context.resources.getString(R.string.button_cancel)
                    ) { _: DialogInterface?, _: Int -> }
                alert.show()
            }

            btnDelivered.setOnClickListener { v: View ->
                if (data.route.contains("7E") || data.route.contains("FL")) {
                    val intent = Intent(v.context, DeliveryDoneActivity::class.java)
                    intent.putExtra("parcel", data)
                    intent.putExtra("route", data.route)    //todo_sypark 합치기
                    v.context.startActivity(intent)
                } else {
                    val intent = Intent(v.context, DeliveryDoneActivity::class.java)
                    intent.putExtra("parcel", data)
                    v.context.startActivity(intent)
                }
            }

            btnDeliveryFailed.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    DeliveryFailedActivity::class.java
                )
                intent.putExtra("trackingNo", data.shipping)
                intent.putExtra("receiverName", data.name)
                intent.putExtra("senderName", data.sender)
                v.context.startActivity(intent)
            }

            btnPickupScan.setOnClickListener { v: View ->
                val intent = Intent(v.context, CaptureActivity1::class.java)
                intent.putExtra(
                    "title",
                    v.context.resources.getString(R.string.text_start_to_scan)
                )
                intent.putExtra("type", BarcodeType.PICKUP_SCAN_ALL)
                intent.putExtra("pickup_no", data.shipping)
                intent.putExtra("applicant", data.name)
                v.context.startActivity(intent)
            }

            btnPickupZeroQty.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    PickupZeroQtyActivity::class.java
                )
                intent.putExtra(
                    "title",
                    v.context.resources.getString(R.string.text_zero_qty)
                )
                intent.putExtra("pickupNo", data.shipping)
                intent.putExtra("applicant", data.name)
                v.context.startActivity(intent)
            }

            btnPickupVisitLog.setOnClickListener { v: View ->
                val intent = Intent(v.context, PickupFailedActivity::class.java)
                intent.putExtra("type", BarcodeType.TYPE_PICKUP)
                intent.putExtra("reqQty", data.qty)
                intent.putExtra("applicant", data.name)
                intent.putExtra("pickupNo", data.shipping)
                v.context.startActivity(intent)
            }

            // NOTIFICATION.  Outlet Pickup Done
            btnOutletPickupScan.setOnClickListener { view: View ->
                val intent = Intent(
                    view.context,
                    OutletPickupStep1Activity::class.java
                )
                intent.putExtra(
                    "title",
                    view.context.resources.getString(R.string.text_outlet_pickup_done)
                )
                intent.putExtra("pickup_no", data.shipping)
                intent.putExtra("applicant", data.name)
                intent.putExtra("qty", data.qty)
                intent.putExtra("route", data.route)
                view.context.startActivity(intent)
            }

            btnQuickDelivered.setOnClickListener {
                val intent = Intent(
                    it.context,
                    QuickReturnedActivity::class.java
                )
                intent.putExtra(
                    "title",
                    it.context.resources.getString(R.string.text_signature)
                )
                intent.putExtra("waybillNo", data.shipping)
                it.context.startActivity(intent)
            }

            btnQuickFailed.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    QuickReturnFailedActivity::class.java
                )
                intent.putExtra(
                    "title",
                    v.context.resources.getString(R.string.text_visit_log)
                )
                intent.putExtra("waybillNo", data.shipping)
                v.context.startActivity(intent)
            }

            btnCnrFailed.setOnClickListener { v: View ->
                val intent = Intent(v.context, PickupFailedActivity::class.java)
                intent.putExtra("type", BarcodeType.TYPE_CNR)
                intent.putExtra("reqQty", data.qty)
                intent.putExtra("applicant", data.name)
                intent.putExtra("pickupNo", data.shipping)
                v.context.startActivity(intent)
            }

            btnChildCnrPrint.setOnClickListener {
                DataUtil.logEvent("button_click", "ListActivity", "Print_CNR")
                bluetoothListener.isConnectPortablePrint(data.shipping)
            }
        }
    }

    //eylee pickup C&R number check
    private fun isPickupNotCNR(trackingNo: String): Boolean {
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
                scanNoFirst != "P" -> { // cnr 일 때, true
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
                    //이름 or 송장번호 조회
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

    init {
        originalRowItem = ArrayList()
        originalRowItem.addAll(itemList)
        this.bluetoothListener = bluetoothListener
    }
}