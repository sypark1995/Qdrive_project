package com.giosis.library.list.pickup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.giosis.library.R
import java.util.*

class PickupScannedListAdapter(private val itemArrayList: ArrayList<PickupScannedListItem>?) : BaseAdapter() {

    private lateinit var text_scan_list_item_tracking_no: TextView
    private lateinit var text_scan_list_item_date: TextView

    override fun getCount(): Int {

        return if (itemArrayList != null && itemArrayList.size > 0) {
            itemArrayList.size
        } else 0
    }

    override fun getItem(position: Int): Any {
        return itemArrayList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_pickup_scanned_list, null)
        } else {
            convertView
        }

        text_scan_list_item_tracking_no = view.findViewById(R.id.text_scan_list_item_tracking_no)
        text_scan_list_item_date = view.findViewById(R.id.text_scan_list_item_date)

        text_scan_list_item_tracking_no.text = itemArrayList!![position].trackingNo
        text_scan_list_item_date.text = itemArrayList[position].scannedDate
        return view
    }

}