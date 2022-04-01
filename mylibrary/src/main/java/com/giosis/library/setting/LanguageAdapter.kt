package com.giosis.library.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import kotlinx.android.synthetic.main.item_language_setting.view.*

class LanguageAdapter(var language: String, val items: List<String>, val codes: List<String>) :
    RecyclerView.Adapter<LanguageAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language_setting, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val item = items[position]
        val code = codes[position]
        val listener = View.OnClickListener {
            language = code
            notifyDataSetChanged()
        }

        val isCheck = language == code

        holder.apply {
            bind(listener, item, isCheck)
            itemView.tag = item
        }
    }

    fun getLanguageCode(): String {
        return language
    }

    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {

        private var view: View = v

        fun bind(listener: View.OnClickListener, item: String, isCheck: Boolean) {

            view.text_language_setting_language.text = item
            view.setOnClickListener(listener)

            if (isCheck) {
                view.img_language_setting_checked.setBackgroundResource(R.drawable.icon_checked)
            } else {
                view.img_language_setting_checked.background = null
            }

        }
    }
}