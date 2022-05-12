package com.giosis.util.qdrive.international

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bumptech.glide.Glide

class LoginSpinnerAdapter(val context: Context, private val nationList: ArrayList<LoginNation>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View = LayoutInflater.from(context).inflate(R.layout.item_login_nation, null)

        val textItem = view.findViewById<TextView>(R.id.text_login_spinner_item)


        val nation = nationList[position]

        Glide.with(context).load(nation.nation_img_url).error(R.drawable.login_icon_sg).into(view.findViewById(R.id.img_login_spinner_item))
        textItem.text = nation.nation_nm

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