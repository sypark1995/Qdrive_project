package com.giosis.util.qdrive.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.international.R
import kotlinx.android.synthetic.main.language_setting_item.view.*

class LanguageAdapter(val context: Context, var language: String, val items: List<String>, val codes: List<String>) : RecyclerView.Adapter<LanguageAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(context).inflate(R.layout.language_setting_item, parent, false)
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