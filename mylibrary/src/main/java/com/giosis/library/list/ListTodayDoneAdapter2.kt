package com.giosis.library.list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.giosis.library.R
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.util.DataUtil
import com.giosis.library.util.NetworkUtil
import java.util.*
import kotlin.collections.ArrayList

class ListTodayDoneAdapter2(
    rowItem: ArrayList<RowItem>?,
    bluetoothListener: BluetoothListener
) :
    BaseExpandableListAdapter() {
    var TAG = "CustomTodayDoneExpandableAdapter"
    private val rowItem: ArrayList<RowItem> = ArrayList()
    private val originalRowItem: ArrayList<RowItem>
    var bluetoothListener: BluetoothListener
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
            val imgListItemUpIcon =
                view.findViewById<ImageView>(R.id.img_list_item_up_icon)
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
            val textListItemDDay = view.findViewById<TextView>(R.id.text_list_item_d_day)
            val imgListItemSecureDelivery =
                view.findViewById<ImageView>(R.id.img_list_item_secure_delivery)
            val imgListItemStationIcon =
                view.findViewById<ImageView>(R.id.img_list_item_station_icon)
            val textListItemTrackingNo =
                view.findViewById<TextView>(R.id.text_list_item_tracking_no)
            val textListItemPickupState =
                view.findViewById<TextView>(R.id.text_list_item_pickup_state)
            val textListItemAddress =
                view.findViewById<TextView>(R.id.text_list_item_address)
            val layoutListItemMenuIcon =
                view.findViewById<FrameLayout>(R.id.layout_list_item_menu_icon)
            val textListItemReceiptName =
                view.findViewById<TextView>(R.id.text_list_item_receipt_name)
            val layoutListItemDeliveryOutletInfo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_delivery_outlet_info)
            val textListItemDesiredDateTitle =
                view.findViewById<TextView>(R.id.text_list_item_desired_date_title)
            val textListItemDesiredDate =
                view.findViewById<TextView>(R.id.text_list_item_desired_date)
            val textListItemQtyTitle =
                view.findViewById<TextView>(R.id.text_list_item_qty_title)
            val textListItemQty = view.findViewById<TextView>(R.id.text_list_item_qty)
            val layoutListItemRequest =
                view.findViewById<LinearLayout>(R.id.layout_list_item_request)
            val layoutListItemDriverMemo =
                view.findViewById<LinearLayout>(R.id.layout_list_item_driver_memo)
            val textListItemDriverMemo =
                view.findViewById<TextView>(R.id.text_list_item_driver_memo)
            val rowPos = rowItem[groupPosition]
            textListItemDDay.text = rowPos.delay
            if (rowPos.delay == "D+0" || rowPos.delay == "D+1") {
                textListItemDDay.setTextColor(Color.parseColor("#303030"))
            } else {
                textListItemDDay.setTextColor(Color.parseColor("#FF0000"))
            }

            //
            imgListItemSecureDelivery.visibility = View.GONE
            imgListItemStationIcon.visibility = View.GONE
            if (rowPos.route.contains("7E")) {
                imgListItemStationIcon.visibility = View.VISIBLE
            } else {
                imgListItemStationIcon.visibility = View.GONE
            }

            //드라이버 셀프 메모
            if (rowPos.selfMemo == null || rowPos.selfMemo!!.isEmpty()) {
                layoutListItemDriverMemo.visibility = View.GONE
            } else {
                layoutListItemDriverMemo.visibility = View.VISIBLE
                textListItemDriverMemo.text = rowPos.selfMemo
            }

            //픽업
            if (rowPos.type == "P") {
                textListItemTrackingNo.setTextColor(Color.parseColor("#363BE7"))
                when (rowPos.stat) {
                    "RE" -> {
                        textListItemPickupState.visibility = View.VISIBLE
                        textListItemPickupState.text =
                            view.context.resources.getString(R.string.text_pickup_reassigned)
                    }
                    "PF" -> {
                        textListItemPickupState.visibility = View.VISIBLE
                        textListItemPickupState.text =
                            view.context.resources.getString(R.string.text_pickup_failed)
                    }
                    else -> {
                        textListItemPickupState.visibility = View.GONE
                    }
                }
            }
            textListItemTrackingNo.text = rowPos.shipping
            textListItemAddress.text = rowPos.address
            layoutListItemMenuIcon.tag = rowPos.shipping
            textListItemReceiptName.text = rowPos.name
            layoutListItemDeliveryOutletInfo.visibility = View.GONE
            textListItemDesiredDateTitle.text =
                view.context.resources.getString(R.string.text_scanned_qty)
            textListItemDesiredDate.text = rowPos.qty
            textListItemQtyTitle.visibility = View.GONE
            textListItemQty.visibility = View.GONE
            layoutListItemRequest.visibility = View.GONE

            //우측 메뉴 아이콘 클릭 이벤트  Quick Menu
            layoutListItemMenuIcon.setOnClickListener { v: View ->
                val popup =
                    PopupMenu(v.context, layoutListItemMenuIcon)
                popup.menuInflater.inflate(R.menu.quickmenu_pickup, popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    val itemId = item.itemId
                    if (itemId == R.id.menu_one) {
                        val mapAddress = rowPos.address
                        val splitIndex = mapAddress.indexOf(")")
                        val splitAddress = mapAddress.substring(splitIndex + 1)
                        if (splitAddress != "") {
                            val uri =
                                Uri.parse("http://maps.google.co.in/maps?q=" + splitAddress.trim { it <= ' ' })
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            v.context.startActivity(intent)
                        }
                    } else if (itemId == R.id.menu_up) {
                        if (groupPosition > 0) {
                            val upItem = rowItem.removeAt(groupPosition)
                            rowItem.add(groupPosition - 1, upItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(rowItem)
                            notifyDataSetChanged()
                        }
                    } else if (itemId == R.id.menu_down) {
                        if (groupPosition < rowItem.size - 1) {
                            val downItem = rowItem.removeAt(groupPosition)
                            rowItem.add(groupPosition + 1, downItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(rowItem)
                            notifyDataSetChanged()
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
            val inflater =
                parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_list_today_done_child, null)
        }
        view?.let {
            val layoutListItemChildDonePickup =
                view.findViewById<LinearLayout>(R.id.layout_list_item_child_done_pickup)
            val btnListItemChildDoneAddScan =
                view.findViewById<Button>(R.id.btn_list_item_child_done_add_scan)
            val btnListItemChildDoneTakeBack =
                view.findViewById<Button>(R.id.btn_list_item_child_done_take_back)
            val btnListItemChildDonePrintLabel =
                view.findViewById<Button>(R.id.btn_list_item_child_done_print_label)
            val trackingNo = rowItem[groupPosition].shipping
            val route = rowItem[groupPosition].route
            val scannedQty = rowItem[groupPosition].qty
            val applicant = rowItem[groupPosition].name
            var isAbleScanAddPage = true
            if (!NetworkUtil.isNetworkAvailable(view.context)) {
                isAbleScanAddPage = false
                alertShow(
                    view.context,
                    view.context.resources.getString(R.string.msg_network_connect_error)
                )
            }
            if ((route == "RPC" || route == "C2C") && isAbleScanAddPage) {
                layoutListItemChildDonePickup.visibility = View.GONE
                btnListItemChildDonePrintLabel.visibility = View.VISIBLE
            } else {
                layoutListItemChildDonePickup.visibility = View.VISIBLE
                btnListItemChildDonePrintLabel.visibility = View.GONE
                Log.e(TAG, "Scanned Qty :  $scannedQty")
                if (scannedQty == "0") {
                    btnListItemChildDoneTakeBack.visibility = View.GONE
                } else {
                    btnListItemChildDoneTakeBack.visibility = View.VISIBLE
                }
            }
            btnListItemChildDoneAddScan.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    TodayDonePickupScanListActivity::class.java
                )
                intent.putExtra("pickup_no", trackingNo)
                intent.putExtra("applicant", applicant)
                intent.putExtra("button_type", "Add Scan")
                (v.context as Activity).startActivityForResult(
                    intent,
                    ListTodayDoneFragment2.REQUEST_ADD_SCAN
                )
            }
            btnListItemChildDonePrintLabel.setOnClickListener {
                DataUtil.logEvent("button_click", "ListActivity", "Print_CNR")
                bluetoothListener.isConnectPortablePrint(trackingNo)
            }

            // 2019.02 - Take Back
            btnListItemChildDoneTakeBack.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    TodayDonePickupScanListActivity::class.java
                )
                intent.putExtra("pickup_no", trackingNo)
                intent.putExtra("applicant", applicant)
                intent.putExtra("button_type", "Take Back")
                (v.context as Activity).startActivityForResult(
                    intent,
                    ListTodayDoneFragment2.REQUEST_TAKE_BACK
                )
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
            val newList = ArrayList<RowItem>()
            for (rowItem in originalRowItem) {
                //이름 or 송장번호 조회
                if (rowItem.name.uppercase(Locale.getDefault()).contains(query) || rowItem.shipping.uppercase(
                        Locale.getDefault()
                    )
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

    fun setSorting(sortedItems: ArrayList<RowItem>?) {
        rowItem.clear()
        rowItem.addAll(sortedItems!!)
        originalRowItem.clear()
        originalRowItem.addAll(sortedItems)
        notifyDataSetChanged()
    }

    private fun alertShow(context: Context, msg: String) {
        val alertInternetStatus = AlertDialog.Builder(context)
        alertInternetStatus.setTitle(context.resources.getString(R.string.text_warning))
        alertInternetStatus.setMessage(msg)
        alertInternetStatus.setPositiveButton(
            context.resources.getString(R.string.button_close)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss() // 닫기
        }
        alertInternetStatus.show()
    }

    init {
        this.rowItem.addAll(rowItem!!)
        originalRowItem = ArrayList()
        originalRowItem.addAll(rowItem)
        this.bluetoothListener = bluetoothListener
    }
}