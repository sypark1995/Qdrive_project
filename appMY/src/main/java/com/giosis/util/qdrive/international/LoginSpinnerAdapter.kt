package com.giosis.util.qdrive.international

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class LoginSpinnerAdapter(val context: Context, private val nationList: ArrayList<LoginNation>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View = LayoutInflater.from(context).inflate(R.layout.item_login_nation, null)

        val img_login_spinner_item = view.findViewById<ImageView>(R.id.img_login_spinner_item)
        val text_login_spinner_item = view.findViewById<TextView>(R.id.text_login_spinner_item)


        val nation = nationList[position]

        val resourceId = context.resources.getIdentifier(nation.nationImg, "drawable", context.packageName)
        img_login_spinner_item.setBackgroundResource(resourceId)
        text_login_spinner_item.text = nation.nation

        return view
    }

    override fun getItem(position: Int): Any {
        return nationList[position]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return nationList.size
    }
}