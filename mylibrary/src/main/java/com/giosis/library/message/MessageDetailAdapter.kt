package com.giosis.library.message

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.databinding.ItemMessageDetailBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessageDetailAdapter(
    var context: Context,
    var items: ArrayList<MessageDetailResult>?,
    var calledFragment: String
) : RecyclerView.Adapter<MessageDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMessageDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemMessageDetailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items!![position]

        if (item.align.equals("LEFT", ignoreCase = true)) {
            /* from customer, admin > to driver */

            holder.binding.layoutReceiveMessage.visibility = View.VISIBLE
            holder.binding.layoutSendMessage.visibility = View.GONE

            if (item.sender_id == "") {
                holder.binding.textReceiverId.text = "null"
            } else {
                holder.binding.textReceiverId.text = item.sender_id
            }
            holder.binding.textReceiveDate.text = item.send_date


            // Customer(Admin) ID   visible/gone
            if (position == 0) {
                holder.binding.textReceiverId.visibility = View.VISIBLE
            } else {  // 1 이상일 때, 나의 이전  ALIGN  비교

                val prevAlign = items!![position - 1].align
                val thisAlign = items!![position].align

                if (thisAlign != prevAlign) {   // right > left

                    holder.binding.textReceiverId.visibility = View.VISIBLE
                } else {

                    val prevDate = items!![position - 1].send_date
                    val thisDate = items!![position].send_date
                    val diffTime = diffTime(prevDate, thisDate)
                    Log.e(
                        "Message",
                        diffTime.toString() + " " + prevDate + "  " + thisDate + "  " + items!![position].message
                    )

                    if (1 <= diffTime) {
                        holder.binding.textReceiverId.visibility = View.VISIBLE
                    } else {
                        holder.binding.textReceiverId.visibility = View.GONE
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= 24) {
                holder.binding.textReceiveMessage.text =
                    Html.fromHtml(item.message, Html.FROM_HTML_MODE_LEGACY)
            } else {
                holder.binding.textReceiveMessage.text = Html.fromHtml(item.message)
            }
        } else if (item.align.equals("RIGHT", ignoreCase = true)) {
            /* from driver > to customer */

            holder.binding.layoutReceiveMessage.visibility = View.GONE
            holder.binding.layoutSendMessage.visibility = View.VISIBLE
            holder.binding.textSendDate.text = item.send_date

            if (Build.VERSION.SDK_INT >= 24) {
                holder.binding.textSendMessage.text =
                    Html.fromHtml(item.message, Html.FROM_HTML_MODE_LEGACY)
            } else {
                holder.binding.textSendMessage.text = Html.fromHtml(item.message)
            }
        }


        //  연속으로 메세지 보낸거 확인 > layout param 바꾸기 / Date  visible/gone
        if (position < items!!.size - 1) {

            val thisAlign = items!![position].align
            val nextAlign = items!![position + 1].align
            val thisDate = items!![position].send_date
            val nextDate = items!![position + 1].send_date
            val diffTime = diffTime(thisDate, nextDate)

            if (nextAlign.equals(thisAlign, ignoreCase = true)) {  // left > left  // right > right
                if (1 <= diffTime) {

                    holder.binding.layoutMessageDetail.setPadding(
                        dpTopx(15f),
                        dpTopx(5f),
                        dpTopx(15f),
                        dpTopx(10f)
                    )
                    holder.binding.textReceiveDate.visibility = View.VISIBLE
                    holder.binding.textSendDate.visibility = View.VISIBLE
                } else {        // 1분 미만으로 동일한 사람이 입력!

                    holder.binding.layoutMessageDetail.setPadding(
                        dpTopx(15f),
                        dpTopx(5f),
                        dpTopx(15f),
                        dpTopx(5f)
                    )
                    holder.binding.textReceiveDate.visibility = View.GONE
                    holder.binding.textSendDate.visibility = View.GONE
                }
            } else {        // left > right  // right > left

                holder.binding.layoutMessageDetail.setPadding(
                    dpTopx(15f),
                    dpTopx(5f),
                    dpTopx(15f),
                    dpTopx(10f)
                )
                holder.binding.textReceiveDate.visibility = View.VISIBLE
                holder.binding.textSendDate.visibility = View.VISIBLE
            }
        } else {        // last

            holder.binding.layoutMessageDetail.setPadding(
                dpTopx(15f),
                dpTopx(5f),
                dpTopx(15f),
                dpTopx(10f)
            )
            holder.binding.textReceiveDate.visibility = View.VISIBLE
            holder.binding.textSendDate.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {

        if (items != null) {

            if (0 < items!!.size) {
                return items!!.size
            }
        }

        return 0
    }


    private fun dpTopx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    @SuppressLint("SimpleDateFormat")
    private fun diffTime(old_date_string: String, date_string: String): Long {

        var diffTime: Long

        try {
            var dateFormat: DateFormat? = null

            if (calledFragment.equals("C", ignoreCase = true)) {

                dateFormat = SimpleDateFormat("yyyy-MM-dd a HH:mm")
            } else if (calledFragment.equals("A", ignoreCase = true)) {

                dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            }

            val oldDate = dateFormat!!.parse(old_date_string)
            val date = dateFormat.parse(date_string)
            val oldDateTime = oldDate.time
            val dateTime = date.time

            diffTime = (dateTime - oldDateTime) / 60000
        } catch (e: Exception) {

            e.printStackTrace()
            diffTime = 100
        }

        return diffTime
    }
}