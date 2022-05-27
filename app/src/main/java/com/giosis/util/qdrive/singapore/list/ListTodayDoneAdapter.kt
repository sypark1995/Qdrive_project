package com.giosis.util.qdrive.singapore.list

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
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothListener
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.FirebaseEvent
import com.giosis.util.qdrive.singapore.util.NetworkUtil
import java.util.*
import kotlin.collections.ArrayList

class ListTodayDoneAdapter(bluetoothListener: BluetoothListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG = "ListTodayDoneAdapter3"
    var rowItem = ArrayList<RowItem>()
        set(value) {
            rowItem.clear()
            rowItem.addAll(value)
        }
    var bluetoothListener: BluetoothListener
    private var originalRowItem = ArrayList<RowItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_today_done, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutListItemCardView: LinearLayout =
            view.findViewById(R.id.layout_list_item_card_view) // background change
        private val imgListItemUpIcon: ImageView =
            view.findViewById(R.id.img_list_item_up_icon)
        private val layoutChildUploadDone: RelativeLayout = view.findViewById(R.id.layout_child_upload_done)
        private val textListItemDDay: TextView = view.findViewById(R.id.text_list_item_d_day)
        private val imgListItemSecureDelivery: ImageView =
            view.findViewById(R.id.img_list_item_secure_delivery)
        private val imgListItemStationIcon: ImageView =
            view.findViewById(R.id.img_list_item_station_icon)
        private val textListItemTrackingNo: TextView =
            view.findViewById(R.id.text_list_item_tracking_no)
        private val textListItemPickupState: TextView =
            view.findViewById(R.id.text_list_item_pickup_state)
        private val textListItemAddress: TextView =
            view.findViewById(R.id.text_list_item_address)
        private val layoutListItemMenuIcon: FrameLayout =
            view.findViewById(R.id.layout_list_item_menu_icon)
        private val textListItemReceiptName: TextView =
            view.findViewById(R.id.text_list_item_receipt_name)
        private val layoutListItemDeliveryOutletInfo: LinearLayout =
            view.findViewById(R.id.layout_list_item_delivery_outlet_info)
        private val textListItemDesiredDateTitle: TextView =
            view.findViewById(R.id.text_list_item_desired_date_title)
        private val textListItemDesiredDate: TextView =
            view.findViewById(R.id.text_list_item_desired_date)
        private val textListItemQtyTitle: TextView =
            view.findViewById(R.id.text_list_item_qty_title)
        private val textListItemQty: TextView = view.findViewById(R.id.text_list_item_qty)
        private val layoutListItemRequest: LinearLayout =
            view.findViewById(R.id.layout_list_item_request)
        private val layoutListItemDriverMemo: LinearLayout =
            view.findViewById(R.id.layout_list_item_driver_memo)
        private val textListItemDriverMemo: TextView =
            view.findViewById(R.id.text_list_item_driver_memo)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(position: Int) {
            val data = rowItem[position]
            textListItemDDay.text = data.delay

            if (data.delay == "D+0" || data.delay == "D+1") {
                textListItemDDay.setTextColor(Color.parseColor("#303030"))
            } else {
                textListItemDDay.setTextColor(Color.parseColor("#FF0000"))
            }

            imgListItemSecureDelivery.visibility = View.GONE
            imgListItemStationIcon.visibility = View.GONE

            layoutListItemCardView.setOnClickListener {
                data.isClicked = !data.isClicked

                if (data.isClicked) {
                    layoutListItemCardView.setBackgroundResource(R.drawable.bg_top_round_10_ffffff)
                    imgListItemUpIcon.visibility = View.GONE
                    layoutChildUploadDone.visibility = View.VISIBLE
                } else {
                    layoutListItemCardView.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow)
                    imgListItemUpIcon.visibility = View.VISIBLE
                    layoutChildUploadDone.visibility = View.GONE
                }
                notifyDataSetChanged()
            }
            if (data.route.contains("7E")) {
                imgListItemStationIcon.visibility = View.VISIBLE
            } else {
                imgListItemStationIcon.visibility = View.GONE
            }

            //드라이버 셀프 메모
            if (data.selfMemo == null || data.selfMemo!!.isEmpty()) {
                layoutListItemDriverMemo.visibility = View.GONE
            } else {
                layoutListItemDriverMemo.visibility = View.VISIBLE
                textListItemDriverMemo.text = data.selfMemo
            }

            //픽업
            if (data.type == "P") {
                textListItemTrackingNo.setTextColor(Color.parseColor("#363BE7"))
                when (data.stat) {
                    "RE" -> {
                        textListItemPickupState.visibility = View.VISIBLE
                        textListItemPickupState.text =
                            itemView.context.resources.getString(R.string.text_pickup_reassigned)
                    }
                    "PF" -> {
                        textListItemPickupState.visibility = View.VISIBLE
                        textListItemPickupState.text =
                            itemView.context.resources.getString(R.string.text_pickup_failed)
                    }
                    else -> {
                        textListItemPickupState.visibility = View.GONE
                    }
                }
            }


            textListItemTrackingNo.text = data.shipping
            textListItemAddress.text = data.address
            layoutListItemMenuIcon.tag = data.shipping
            textListItemReceiptName.text = data.name
            layoutListItemDeliveryOutletInfo.visibility = View.GONE
            textListItemDesiredDateTitle.text =
                itemView.context.resources.getString(R.string.text_scanned_qty)
            textListItemDesiredDate.text = data.qty
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
                        val mapAddress = data.address
                        val splitIndex = mapAddress.indexOf(")")
                        val splitAddress = mapAddress.substring(splitIndex + 1)
                        if (splitAddress != "") {
                            val uri =
                                Uri.parse("http://maps.google.co.in/maps?q=" + splitAddress.trim { it <= ' ' })
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            v.context.startActivity(intent)
                        }
                    } else if (itemId == R.id.menu_up) {
                        if (adapterPosition > 0) {
                            val upItem = rowItem.removeAt(adapterPosition)
                            rowItem.add(adapterPosition - 1, upItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(rowItem)
                            notifyDataSetChanged()
                        }
                    } else if (itemId == R.id.menu_down) {
                        if (adapterPosition < rowItem.size - 1) {
                            val downItem = rowItem.removeAt(adapterPosition)
                            rowItem.add(adapterPosition + 1, downItem)
                            originalRowItem.clear()
                            originalRowItem.addAll(rowItem)
                            notifyDataSetChanged()
                        }
                    }
                    true
                }
            }

            val layoutListItemChildDonePickup =
                itemView.findViewById<LinearLayout>(R.id.layout_list_item_child_done_pickup)
            val btnListItemChildDoneAddScan =
                itemView.findViewById<Button>(R.id.btn_list_item_child_done_add_scan)
            val btnListItemChildDoneTakeBack =
                itemView.findViewById<Button>(R.id.btn_list_item_child_done_take_back)
            val btnListItemChildDonePrintLabel =
                itemView.findViewById<Button>(R.id.btn_list_item_child_done_print_label)
            var isAbleScanAddPage = true
            if (!NetworkUtil.isNetworkAvailable(itemView.context)) {
                isAbleScanAddPage = false
                alertShow(
                    itemView.context,
                    itemView.context.resources.getString(R.string.msg_network_connect_error)
                )
            }

            if ((data.route == "RPC" || data.route == "C2C") && isAbleScanAddPage) {
                layoutListItemChildDonePickup.visibility = View.GONE
                btnListItemChildDonePrintLabel.visibility = View.VISIBLE
            } else {
                layoutListItemChildDonePickup.visibility = View.VISIBLE
                btnListItemChildDonePrintLabel.visibility = View.GONE
                Log.e(TAG, "Scanned Qty :  ${data.qty}")
                if (data.qty == "0") {
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
                intent.putExtra("pickup_no", data.shipping)
                intent.putExtra("applicant", data.name)
                intent.putExtra("button_type", "Add Scan")
                (v.context as Activity).startActivityForResult(
                    intent,
                    ListTodayDoneFragment.REQUEST_ADD_SCAN
                )
            }

            btnListItemChildDonePrintLabel.setOnClickListener {
                FirebaseEvent.clickEvent(it.context,TAG,"btnListItemChildDonePrintLabel click")
                bluetoothListener.isConnectPortablePrint(data.shipping)
            }

            // 2019.02 - Take Back
            btnListItemChildDoneTakeBack.setOnClickListener { v: View ->
                val intent = Intent(
                    v.context,
                    TodayDonePickupScanListActivity::class.java
                )
                intent.putExtra("pickup_no", data.shipping)
                intent.putExtra("applicant", data.name)
                intent.putExtra("button_type", "Take Back")
                (v.context as Activity).startActivityForResult(
                    intent,
                    ListTodayDoneFragment.REQUEST_TAKE_BACK
                )
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

    @SuppressLint("NotifyDataSetChanged")
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
        originalRowItem = ArrayList()
        originalRowItem.addAll(rowItem)
        this.bluetoothListener = bluetoothListener
    }
}