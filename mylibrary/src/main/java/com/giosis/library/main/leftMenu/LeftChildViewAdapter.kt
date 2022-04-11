package com.giosis.library.main.leftMenu


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.main.MainActivity

class LeftChildViewAdapter(val item: ArrayList<SubMenuItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_nav_list_child, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val textNavListSubTitle: TextView = view.findViewById(R.id.text_nav_list_sub_title)

        fun bind(item: SubMenuItem) {
            textNavListSubTitle.text = view.context.resources.getString(item.title)

            view.setOnClickListener {

                (view.context as MainActivity).leftMenuGone()

                val intent = Intent(view.context, item.className)
                intent.putExtras(Bundle().apply(item.extras))
                (view.context as MainActivity).startActivity(intent)

                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(item[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return if (item.size > 0) {
            item.size
        } else 0
    }

}