package com.giosis.library.message

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.databinding.ItemMessageListBinding
import java.util.*

class MessageListAdapter(private var called: String, private val mItems: ArrayList<MessageListResult>?) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemMessageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemMessageListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (mItems!![position].read_yn == "Y") {
            holder.binding.textMessage.setTextColor(Color.parseColor("#444444"))
        } else {
            holder.binding.textMessage.setTextColor(Color.parseColor("#f22020"))
        }

        if (called == "C") {     // Customer
            holder.binding.textTitle.text = mItems[position].tracking_no
        } else if (called == "A") {  // Admin
            holder.binding.textTitle.text = mItems[position].sender_id
        }


        if (Build.VERSION.SDK_INT >= 24) {

            holder.binding.textMessage.text = Html.fromHtml(mItems[position].message, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.binding.textMessage.text = Html.fromHtml(mItems[position].message)
        }

        holder.binding.textDate.text = mItems[position].getTime(called)


        holder.binding.layoutMessageItem.setOnClickListener {
            if (called == "C") {     // Customer

                val intent = Intent(it.context, CustomerMessageListDetailActivity::class.java)
                intent.putExtra("question_no", mItems[position].question_seq_no)
                intent.putExtra("tracking_no", mItems[position].tracking_no)
                it.context.startActivity(intent)
            } else if (called == "A") {  // Admin

                val intent = Intent(it.context, AdminMessageListDetailActivity::class.java)
                intent.putExtra("sender_id", mItems[position].sender_id)
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (mItems != null && mItems.size > 0) {
            mItems.size
        } else 0
    }
}