package com.giosis.util.qdrive.settings

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.international.R
import kotlinx.android.synthetic.main.notice_item.view.*

class NoticeAdapter(val context: Context, val items: ArrayList<NoticeResults.NoticeItem>) : RecyclerView.Adapter<NoticeAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(context).inflate(R.layout.notice_item, parent, false)
        return Holder(view)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val item = items[position]
        val listener = View.OnTouchListener { view, motionEvent ->

            when (motionEvent.action) {

                MotionEvent.ACTION_DOWN -> {

                    view.layout_notice_list_item.setBackgroundColor(context.resources.getColor(R.color.color_f6f6f6))
                }

                MotionEvent.ACTION_UP -> {

                    view.layout_notice_list_item.setBackgroundColor(context.resources.getColor(R.color.white))

                    val intent = Intent(context, NoticeDetailActivity::class.java)
                    intent.putExtra("notice_no", item.seqNo)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }

                MotionEvent.ACTION_CANCEL -> {

                    view.layout_notice_list_item.setBackgroundColor(context.resources.getColor(R.color.white))
                }
            }

            true
        }


        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }


    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {

        private var view: View = v

        fun bind(listener: View.OnTouchListener, item: NoticeResults.NoticeItem) {

            view.text_notice_list_item_title.text = item.title
            view.text_notice_list_item_date.text = item.date
            view.setOnTouchListener(listener)
        }
    }
}