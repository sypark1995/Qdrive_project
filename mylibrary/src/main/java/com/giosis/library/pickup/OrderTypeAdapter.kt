package com.giosis.library.pickup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.giosis.library.R


class OrderTypeAdapter(val list: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
                ?: LayoutInflater.from(parent!!.context).inflate(R.layout.order_item_layout, parent, false)

        val text: String = list[position]
        (view!!.findViewById(R.id.spinnerText) as TextView).text = text

        return view
    }


}