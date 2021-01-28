package com.giosis.library.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.giosis.library.R
import java.util.*

class TodayDonePickupScanListAdapter(val context: Context, private val mItems: ArrayList<PickupScanListItem>?) : BaseAdapter() {

    override fun getCount(): Int {
        return if (mItems != null && mItems.size > 0) {
            mItems.size
        } else 0
    }


    override fun getItem(position: Int): Any {
        return mItems!![position]
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_today_done_pickup_scanned_list, null)
        } else {
            convertView
        }

        val tracking_no = view.findViewById<TextView>(R.id.text_scan_list_item_tracking_no)
        val item_date = view.findViewById<TextView>(R.id.text_scan_list_item_date)

        tracking_no.text = mItems!![position].tracking_no
        item_date.text = mItems[position].scanned_date
        return view
    }
}