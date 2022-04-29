package com.giosis.library.list

import android.annotation.SuppressLint
import android.app.Activity
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
import com.giosis.library.R
import com.giosis.library.barcodescanner.CaptureActivity1
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.database.DatabaseHelper.Companion.getInstance
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

class ListInProgressAdapter2(rowItems: ArrayList<RowItem>?, bluetoothListener: BluetoothListener) :
    BaseExpandableListAdapter() {

    private val TAG = "ListInProgressAdapter"
    private var onMoveUpListener: OnMoveUpListener? = null
    private val rowItem: ArrayList<RowItem> = ArrayList()
    private val originalRowItem: ArrayList<RowItem>
    var bluetoothListener: BluetoothListener

    fun setOnMoveUpListener(listener: OnMoveUpListener?) {
        onMoveUpListener = listener
    }

    interface OnMoveUpListener {
        fun onMoveUp(pos: Int)
    }

    override fun getGroupCount(): Int {
        return rowItem.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return rowItem[groupPosition].items!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return rowItem[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return rowItem[groupPosition].items!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var view: View? = convertView

        if (view == null) {
            val mInflater =
                parent.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = mInflater.inflate(R.layout.list_group_item, null)
        }
        view?.let {
            val cardView =
                view.findViewById<LinearLayout>(R.id.layout_list_item_card_view) // background change
            val textDday = view.findViewById<TextView>(R.id.text_list_item_d_day)
            val imgSecureDelivery =
                view.findViewById<ImageView>(R.id.img_list_item_secure_delivery)
            val imgStationIcon =
                view.findViewById<ImageView>(R.id.img_list_item_station_icon)
            val textTrackingNo =
                view.findViewById<TextView>(R.id.text_list_item_tracking_no)
            val textPickupState =
                view.findViewById<TextView>(R.id.text_list_item_pickup_state)
            val textEconomy = view.findViewById<TextView>(R.id.text_list_item_economy)
            val textHighAmount =
                view.findViewById<TextView>(R.id.text_list_item_high_amount)
            val imgItemUpIcon = view.findViewById<ImageView>(R.id.img_list_item_up_icon)
            val textAddress = view.findViewById<TextView>(R.id.text_list_item_address)
            val menuIcon =
                view.findViewById<FrameLayout>(R.id.layout_list_item_menu_icon)
            val deliveryInfo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_delivery_info)
            val textReceiptName =
                view.findViewById<TextView>(R.id.text_list_item_receipt_name)
            val deliveryOutletInfo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_delivery_outlet_info)
            val textParcelQty =
                view.findViewById<TextView>(R.id.text_list_item_parcel_qty)
            val textPickupInfo =
                view.findViewById<RelativeLayout>(R.id.layout_list_item_pickup_info)
            val textDesiredDate =
                view.findViewById<TextView>(R.id.text_list_item_desired_date)
            val textQty = view.findViewById<TextView>(R.id.text_list_item_qty)
            val layoutRequest =
                view.findViewById<LinearLayout>(R.id.layout_list_item_request)
            val textRequest = view.findViewById<TextView>(R.id.text_list_item_request)
            val layoutDriverMemo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_driver_memo)
            val textDriverMemo =
                view.findViewById<TextView>(R.id.text_list_item_driver_memo)
            if (isExpanded) {
                cardView.setBackgroundResource(R.drawable.bg_top_round_10_ffffff)
                imgItemUpIcon.visibility = View.VISIBLE
            } else {
                cardView.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow)
                imgItemUpIcon.visibility = View.GONE
            }
            if (groupPosition == 0) {
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
            val rowPos = rowItem[groupPosition]
            //   Log.i(TAG, "  Route : " + row_pos.getRoute() + " / Stat : " + row_pos.getStat() + " / Number : " + row_pos.getShipping());
            textDday.text = rowPos.delay
            if (rowPos.delay == "D+0" || rowPos.delay == "D+1") {
                textDday.setTextColor(Color.parseColor("#303030"))
            } else {
                textDday.setTextColor(Color.parseColor("#FF0000"))
            }

            //
            when (rowPos.outlet_company) {
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

            // 2019.10 - 참조 픽업번호가 있으면 해당 번호로 표시, 없으면 기존 픽업번호 (Ref. Pickup No)
            if (rowPos.type == "P") {        // Pickup
                if (rowPos.ref_pickup_no != "") {
                    textTrackingNo.text = rowPos.ref_pickup_no
                } else {
                    textTrackingNo.text = rowPos.shipping
                }
            } else {        // Delivery
                textTrackingNo.text = rowPos.shipping
            }
            textAddress.text = rowPos.address
            menuIcon.tag = rowPos.shipping
            textReceiptName.text = rowPos.name

            //픽업
            if (rowPos.type == BarcodeType.TYPE_PICKUP) {
                textTrackingNo.setTextColor(Color.parseColor("#363BE7"))
                imgSecureDelivery.visibility = View.GONE
                textEconomy.visibility = View.GONE
                textHighAmount.visibility = View.GONE
                when (rowPos.stat) {
                    BarcodeType.PICKUP_REASSIGN -> {
                        textPickupState.visibility = View.VISIBLE
                        textPickupState.text =
                            parent.context.resources.getString(R.string.text_pickup_reassigned)
                    }
                    BarcodeType.PICKUP_FAIL -> {
                        textPickupState.visibility = View.VISIBLE
                        textPickupState.text =
                            parent.context.resources.getString(R.string.text_pickup_failed)
                    }
                    else -> {
                        textPickupState.visibility = View.GONE
                    }
                }
                if (rowPos.outlet_store_name != null) {

                    // 2019.04
                    if (rowPos.outlet_company == "7E") {
                        textTrackingNo.text = rowPos.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + rowPos.outlet_store_name + " (" + rowPos.outlet_store_code + ")"
                    } else {
                        textTrackingNo.text = rowPos.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + rowPos.outlet_store_name
                    }
                }
                deliveryInfo.visibility = View.GONE
                deliveryOutletInfo.visibility = View.GONE
                textPickupInfo.visibility = View.VISIBLE
                textDesiredDate.text = rowPos.desiredDate
                textQty.text = rowPos.qty
                if (rowPos.route.equals("RPC", ignoreCase = true)) {
                    textDesiredDate.text =
                        rowPos.desiredDate + " / " + rowPos.desired_time
                }
            } else {       //배송
                textTrackingNo.setTextColor(Color.parseColor("#32BD87"))
                if (rowPos.secure_delivery_yn != null && rowPos.secure_delivery_yn == "Y") {
                    imgSecureDelivery.visibility = View.VISIBLE
                } else {
                    imgSecureDelivery.visibility = View.GONE
                }
                if (rowPos.stat == BarcodeType.DELIVERY_FAIL) {
                    textPickupState.visibility = View.VISIBLE
                    textPickupState.text =
                        parent.context.resources.getString(R.string.text_failed)
                } else {
                    textPickupState.visibility = View.GONE
                }

                // 2021.04  High amount
                if (rowPos.high_amount_yn == "Y") {
                    textHighAmount.visibility = View.VISIBLE
                } else {
                    textHighAmount.visibility = View.GONE
                }

                // 2021.09 Economy
                if (rowPos.orderType == "ECO") {
                    textEconomy.visibility = View.VISIBLE
                } else {
                    textEconomy.visibility = View.GONE
                }
                deliveryInfo.visibility = View.VISIBLE
                textPickupInfo.visibility = View.GONE
                if (rowPos.outlet_store_name != null) {

                    //text_list_item_tracking_no.setText(row_pos.getOutlet_company().replace("FL", "LA") + " " + row_pos.getOutlet_store_name());
                    deliveryOutletInfo.visibility = View.VISIBLE
                    textParcelQty.text = String.format("%d", rowPos.outlet_qty)

                    // 2019.04
                    if (rowPos.outlet_company == "7E") {
                        textTrackingNo.text = rowPos.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + rowPos.outlet_store_name + " (" + rowPos.outlet_store_code + ")"
                    } else {
                        textTrackingNo.text = rowPos.outlet_company!!.replace(
                            "FL",
                            "LA"
                        ) + " " + rowPos.outlet_store_name
                    }
                    deliveryInfo.visibility = View.GONE
                } else {
                    deliveryOutletInfo.visibility = View.GONE
                }
            }

            //요청
            if (rowPos.request == null || rowPos.request!!.isEmpty()) {
                layoutRequest.visibility = View.GONE
            } else {
                layoutRequest.visibility = View.VISIBLE
                textRequest.text = rowPos.request
            }

            //드라이버 셀프 메모
            if (rowPos.selfMemo == null || rowPos.selfMemo!!.isEmpty()) {
                layoutDriverMemo.visibility = View.GONE
            } else {
                layoutDriverMemo.visibility = View.VISIBLE
                textDriverMemo.text = rowPos.selfMemo
            }


            //  2019.04 Outlet
            if (rowPos.outlet_company == "7E" || rowPos.outlet_company == "FL") {
                if (rowPos.type == BarcodeType.TYPE_PICKUP) {
                    textPickupState.text =
                        parent.context.resources.getString(R.string.text_retrieve)
                } else if (rowPos.type == BarcodeType.TYPE_DELIVERY) {
                    textPickupState.text =
                        parent.context.resources.getString(R.string.text_delivery)
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
                            getInstance()["SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + menuIcon.tag
                                .toString() + "' LIMIT 1"]
                        cs.moveToFirst()
                        val address = cs.getString(cs.getColumnIndex("address"))
                        val uri =
                            Uri.parse("http://maps.google.co.in/maps?q=$address")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        v.context.startActivity(intent)
                    } else if (itemId == R.id.menu_up) {
                        if (0 < groupPosition) {
                            val upItem = rowItem.removeAt(groupPosition)
                            rowItem.add(groupPosition - 1, upItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(rowItem)
                            notifyDataSetChanged()
                            if (onMoveUpListener != null) {
                                onMoveUpListener!!.onMoveUp(groupPosition - 1)
                            }
                        }
                    } else if (itemId == R.id.menu_down) {
                        if (groupPosition < rowItem.size - 1) {
                            val downItem = rowItem.removeAt(groupPosition)
                            rowItem.add(groupPosition + 1, downItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(rowItem)
                            notifyDataSetChanged()
                            if (onMoveUpListener != null) {
                                onMoveUpListener!!.onMoveUp(groupPosition + 1)
                            }
                        }
                    }
                    true
                }
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
            val layoutInflater =
                parent.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.item_list_child, null)
        }
        view?.let {
            val layoutFailed =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_failed)
            val textFailedReason =
                view.findViewById<TextView>(R.id.text_list_item_child_failed_reason)
            val layoutParcelAmount =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_parcel_amount)
            val textParcelAmountTitle =
                view.findViewById<TextView>(R.id.text_list_item_child_parcel_amount_title)
            val textParcelAmount =
                view.findViewById<TextView>(R.id.text_list_item_child_parcel_amount)
            val textParcelAmountUnit =
                view.findViewById<TextView>(R.id.text_list_item_child_parcel_amount_unit)
            val layoutTelephone =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_telephone)
            val textTelephoneNumber =
                view.findViewById<TextView>(R.id.text_list_item_child_telephone_number)
            val layoutMobile =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_mobile)
            val textMobileNumber =
                view.findViewById<TextView>(R.id.text_list_item_child_mobile_number)
            val imgSms =
                view.findViewById<ImageView>(R.id.img_list_item_child_sms)
            val imgLive10 =
                view.findViewById<ImageView>(R.id.img_list_item_child_live10)
            val imgQpost =
                view.findViewById<ImageView>(R.id.img_list_item_child_qpost)
            val imgDriverMemo =
                view.findViewById<ImageView>(R.id.img_list_item_child_driver_memo)
            val layoutChildDeliveryButtons =
                view.findViewById<RelativeLayout>(R.id.layout_list_item_child_delivery_buttons)
            val btnDelivered =
                view.findViewById<Button>(R.id.btn_list_item_child_delivered)
            val btnDeliveryFailed =
                view.findViewById<Button>(R.id.btn_list_item_child_delivery_failed)
            val layoutQuickButtons =
                view.findViewById<RelativeLayout>(R.id.layout_list_item_child_quick_buttons)
            val btnQuickDelivered =
                view.findViewById<Button>(R.id.btn_list_item_child_quick_delivered)
            val btnQuickFailed =
                view.findViewById<Button>(R.id.btn_list_item_child_quick_failed)
            val layoutPickupButtons =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_pickup_buttons)
            val btnPickupScan =
                view.findViewById<Button>(R.id.btn_list_item_child_pickup_scan)
            val btnPickupZeroQty =
                view.findViewById<Button>(R.id.btn_list_item_child_pickup_zero_qty)
            val btnPickupVisitLog =
                view.findViewById<Button>(R.id.btn_list_item_child_pickup_visit_log)
            val layoutCnrButtons =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_cnr_buttons)
            val btnCnrFailed =
                view.findViewById<Button>(R.id.btn_list_item_child_cnr_failed)
            val btnChildCnrPrint =
                view.findViewById<Button>(R.id.btn_list_item_child_cnr_print)
            val layoutOutletPickup =
                view.findViewById<RelativeLayout>(R.id.layout_list_item_child_outlet_pickup)
            val btnOutletPickupScan =
                view.findViewById<Button>(R.id.btn_list_item_child_outlet_pickup_scan)
            val layoutButtons2 =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_buttons2)
            val btnDetailButton =
                view.findViewById<Button>(R.id.btn_list_item_child_detail_button)
            val groupItem = rowItem[groupPosition]
            val child = getChild(groupPosition, childPosition) as ChildItem
            val trackingNo = rowItem[groupPosition].shipping
            val receiver = rowItem[groupPosition].name
            val sender = rowItem[groupPosition].sender
            val requester = rowItem[groupPosition].name
            val route = rowItem[groupPosition].route
            val qty = rowItem[groupPosition].qty
            if (child.secretNoType == "T") {    // Qtalk 안심번호 타입 T - Qnumber 사용
                layoutTelephone.visibility = View.GONE
                layoutMobile.visibility = View.GONE
                imgLive10.visibility = View.VISIBLE
            } else if (child.secretNoType == "P") {  // Phone 안심번호 - 핸드폰만 활성화
                layoutTelephone.visibility = View.GONE
                layoutMobile.visibility = View.VISIBLE
                imgLive10.visibility = View.GONE
                val content = SpannableString(child.hp)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                textMobileNumber.text = content
            } else {          //안심번호 사용안함
                if (child.tel != null && child.tel!!.length > 5) {
                    layoutTelephone.visibility = View.VISIBLE
                    val content = SpannableString(child.tel)
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    textTelephoneNumber.text = content
                } else {
                    layoutTelephone.visibility = View.GONE
                }
                if (child.hp != null && child.hp!!.length > 5) {
                    layoutMobile.visibility = View.VISIBLE
                    val content = SpannableString(child.hp)
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
            try {
                val orderType = rowItem[groupPosition].order_type_etc
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

            // 2018.12.26  Delivery / Pickup  Fail Reason
            if (child.statReason != null && child.statReason!!.isNotEmpty()) {
                val reasonText: String?
                when (child.stat) {
                    BarcodeType.DELIVERY_FAIL -> {
                        reasonText = DataUtil.getDeliveryFailedMsg(child.statReason)
                        layoutFailed.visibility = View.VISIBLE
                        textFailedReason.text = reasonText
                    }
                    BarcodeType.PICKUP_FAIL -> {
                        reasonText = DataUtil.getPickupFailedMsg(child.statReason)
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


            //
            if (rowItem[groupPosition].type == BarcodeType.TYPE_DELIVERY) {
                textParcelAmountTitle.text =
                    parent.context.resources.getString(R.string.text_parcel_amount)
                var parcelAmount = rowItem[groupPosition].parcel_amount
                if (parcelAmount == null) {
                    parcelAmount = "0.00"
                } else if (parcelAmount == "" || parcelAmount.lowercase(Locale.getDefault()) == "null") {
                    parcelAmount = "0.00"
                }
                textParcelAmount.text = parcelAmount
                var parcelAmountUnit = rowItem[groupPosition].currency
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
                if (rowItem[groupPosition].route == "QXQ") {
                    layoutChildDeliveryButtons.visibility = View.GONE
                    layoutQuickButtons.visibility = View.VISIBLE
                } else {
                    layoutChildDeliveryButtons.visibility = View.VISIBLE
                    layoutQuickButtons.visibility = View.GONE
                }
                layoutPickupButtons.visibility = View.GONE
                layoutCnrButtons.visibility = View.GONE
                layoutOutletPickup.visibility = View.GONE
                if (rowItem[groupPosition].outlet_company == "7E" || rowItem[groupPosition].outlet_company == "FL") {
                    layoutParcelAmount.visibility = View.GONE
                    // k. 2018.10.24   VisitLog 시 화물이 DPC3-Out 처리됨... 7E 화물은 DPC2-Out 까지만 처리되야 함..
                    btnDeliveryFailed.visibility = View.GONE

                    // 2019.04
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
                    parent.context.resources.getString(R.string.text_name)
                textParcelAmount.text = rowItem[groupPosition].name
                textParcelAmountUnit.visibility = View.GONE
                layoutChildDeliveryButtons.visibility = View.GONE
                layoutQuickButtons.visibility = View.GONE

                //tracking_no 에 따라서 layout 선택하기  by 2016-09-23
                val isNotCNR = isPickupNotCNR(trackingNo)

                /* //TEST.  CNR
                isNotCNR = true;*/if (isNotCNR) { // true 이면 cnr      // C&R  주문건
                    layoutPickupButtons.visibility = View.GONE
                    layoutCnrButtons.visibility = View.VISIBLE
                    layoutOutletPickup.visibility = View.GONE
                } else if (rowItem[groupPosition].outlet_company == "7E" || rowItem[groupPosition].outlet_company == "FL") {       // 7E, FL
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
                if ("SG" == Preferences.userNation) {
                    // Trip
                    if (rowItem[groupPosition].isPrimaryKey) {
                        layoutButtons2.visibility = View.VISIBLE
                    } else {
                        layoutButtons2.visibility = View.GONE
                    }
                } else {
                    layoutButtons2.visibility = View.GONE
                }
            }
            if ("SG" == Preferences.userNation) {
                btnDetailButton.visibility = View.VISIBLE
                btnDetailButton.setOnClickListener { v: View ->
                    val tripDataArrayList =
                        groupItem.tripSubDataArrayList
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
                val callUri = Uri.parse("tel:" + child.tel)
                val intent = Intent(Intent.ACTION_DIAL, callUri)
                v.context.startActivity(intent)
            }
            textMobileNumber.setOnClickListener { v: View ->
                val callUri = Uri.parse("tel:" + child.hp)
                val intent = Intent(Intent.ACTION_DIAL, callUri)
                v.context.startActivity(intent)
            }
            imgSms.setOnClickListener { v: View ->
                try {
                    val smsBody = String.format(
                        v.context.resources.getString(R.string.msg_delivery_start_sms),
                        receiver
                    )
                    val smsUri = Uri.parse("sms:" + child.hp)
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
                val pQlpsCustNo = groupItem.custNo
                val pDeliveryType = groupItem.type //P,D
                val pOrderType = groupItem.route // RPC, C2C, GIO
                val pTrackingNo = groupItem.shipping
                val pSellerId = groupItem.partnerID
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
                intent.putExtra("tracking_no", trackingNo)
                view.context.startActivity(intent)
            }
            imgDriverMemo.setOnClickListener { v: View ->
                val alert =
                    AlertDialog.Builder(v.context)
                val msg = groupItem.selfMemo
                val shipping = groupItem.shipping
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
                    getInstance().update(
                        DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                        "invoice_no= ? COLLATE NOCASE ", arrayOf(trackingNo)
                    )
                    groupItem.selfMemo = selfMemo
                    notifyDataSetChanged()
                }
                    .setNegativeButton(
                        v.context.resources.getString(R.string.button_cancel)
                    ) { _: DialogInterface?, _: Int -> }
                alert.show()
            }
            btnDelivered.setOnClickListener { v: View ->
                if (route.contains("7E") || route.contains("FL")) {
                    val intent = Intent(v.context, DeliveryDoneActivity::class.java)
                    intent.putExtra("parcel", rowItem[groupPosition])
                    intent.putExtra("route", route)
                    v.context.startActivity(intent)
                } else {
                    val intent = Intent(v.context, DeliveryDoneActivity::class.java)
                    intent.putExtra("parcel", rowItem[groupPosition])
                    v.context.startActivity(intent)
                }
            }
            btnDeliveryFailed.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    DeliveryFailedActivity::class.java
                )
                intent.putExtra("trackingNo", trackingNo)
                intent.putExtra("receiverName", receiver)
                intent.putExtra("senderName", sender)
                v.context.startActivity(intent)
            }
            btnPickupScan.setOnClickListener { v: View ->
                val intent = Intent(v.context, CaptureActivity1::class.java)
                intent.putExtra(
                    "title",
                    v.context.resources.getString(R.string.text_start_to_scan)
                )
                intent.putExtra("type", BarcodeType.PICKUP_SCAN_ALL)
                intent.putExtra("pickup_no", trackingNo)
                intent.putExtra("applicant", requester)
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
                intent.putExtra("pickupNo", trackingNo)
                intent.putExtra("applicant", requester)
                v.context.startActivity(intent)
            }
            btnPickupVisitLog.setOnClickListener { v: View ->
                val intent = Intent(v.context, PickupFailedActivity::class.java)
                intent.putExtra("type", BarcodeType.TYPE_PICKUP)
                intent.putExtra("reqQty", qty)
                intent.putExtra("applicant", requester)
                intent.putExtra("pickupNo", trackingNo)
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
                intent.putExtra("pickup_no", trackingNo)
                intent.putExtra("applicant", requester)
                intent.putExtra("qty", qty)
                intent.putExtra("route", route)
                view.context.startActivity(intent)
            }
            btnQuickDelivered.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    QuickReturnedActivity::class.java
                )
                intent.putExtra(
                    "title",
                    v.context.resources.getString(R.string.text_signature)
                )
                intent.putExtra("waybillNo", trackingNo)
                v.context.startActivity(intent)
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
                intent.putExtra("waybillNo", trackingNo)
                v.context.startActivity(intent)
            }
            btnCnrFailed.setOnClickListener { v: View ->
                val intent = Intent(v.context, PickupFailedActivity::class.java)
                intent.putExtra("type", BarcodeType.TYPE_CNR)
                intent.putExtra("reqQty", qty)
                intent.putExtra("applicant", requester)
                intent.putExtra("pickupNo", trackingNo)
                v.context.startActivity(intent)
            }
            btnChildCnrPrint.setOnClickListener {
                DataUtil.logEvent("button_click", "ListActivity", "Print_CNR")
                bluetoothListener.isConnectPortablePrint(trackingNo)
            }
        }

        return view!!
    }

    // 2016-09-23 eylee pickup C&R number check
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

    //Search
    fun filterData(query: String) {
        var query = query
        try {
            query = query.uppercase(Locale.getDefault())
            rowItem.clear()
            if (query.isEmpty()) {
                rowItem.addAll(originalRowItem)
            } else {
                val newList = ArrayList<RowItem>()
                for (rowItem in originalRowItem) {
                    //이름 or 송장번호 조회
                    if (rowItem.name.uppercase(Locale.getDefault())
                            .contains(query) || rowItem.shipping.uppercase(Locale.getDefault())
                            .contains(query)
                    ) {
                        newList.add(rowItem)
                    }
                }
                if (0 < newList.size) {
                    rowItem.addAll(newList)
                }
            }
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e(TAG, "filterData  Exception  $e")
        }
    }

    fun setSorting(sortedItems: ArrayList<RowItem>?) {
        rowItem.clear()
        rowItem.addAll(sortedItems!!)
        originalRowItem.clear()
        originalRowItem.addAll(sortedItems)
        notifyDataSetChanged()
    }

    init {
        rowItem.addAll(rowItems!!)
        originalRowItem = ArrayList()
        originalRowItem.addAll(rowItems)
        this.bluetoothListener = bluetoothListener
    }
}