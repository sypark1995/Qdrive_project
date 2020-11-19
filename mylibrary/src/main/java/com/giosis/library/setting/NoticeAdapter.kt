package com.giosis.library.setting

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.server.data.NoticeResult
import kotlinx.android.synthetic.main.item_notice.view.*

class NoticeAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var noticeItems: ArrayList<NoticeResult.NoticeItem> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false)
        return NoticeHolder(view)
    }

    override fun getItemCount(): Int {
        return noticeItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = noticeItems[position]

        val viewHolder = holder as NoticeHolder
        viewHolder.bind(item)
    }

    fun setItems(listData: ArrayList<NoticeResult.NoticeItem>) {

        this.noticeItems = listData
        notifyDataSetChanged()
    }


    inner class NoticeHolder(v: View) : RecyclerView.ViewHolder(v) {

        private var view: View = v

        fun bind(item: NoticeResult.NoticeItem) {

            view.text_notice_list_item_title.text = item.title
            view.text_notice_list_item_date.text = item.shortDate

            view.layout_notice_list_item.setOnClickListener {

                val intent = Intent(context, NoticeDetailActivity::class.java)
                intent.putExtra("notice_no", item.seqNo)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
}