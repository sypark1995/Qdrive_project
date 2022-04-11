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

//                when (item) {
//                    view.resources.getString(R.string.text_start_delivery_for_outlet),
//                    view.resources.getString(R.string.navi_sub_confirm_delivery) -> {
//                        val intent = Intent(view.context, CaptureActivity1::class.java)
//                        intent.putExtra(
//                            "title",
//                            view.context.resources.getString(R.string.text_title_driver_assign)
//                        )
//                        intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                    view.resources.getString(R.string.navi_sub_delivery_done) -> {
//                        val intent = Intent(view.context, CaptureActivity1::class.java)
//                        intent.putExtra(
//                            "title",
//                            view.context.resources.getString(R.string.text_delivered)
//                        )
//                        intent.putExtra("type", BarcodeType.DELIVERY_DONE)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                    view.resources.getString(R.string.navi_sub_pickup) -> {
//                        val intent = Intent(view.context, CaptureActivity1::class.java)
//                        intent.putExtra(
//                            "title",
//                            view.context.resources.getString(R.string.text_title_scan_pickup_cnr)
//                        )
//                        intent.putExtra("type", BarcodeType.PICKUP_CNR)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                    view.resources.getString(R.string.navi_sub_self) -> {
//                        val intent = Intent(view.context, CaptureActivity1::class.java)
//                        intent.putExtra(
//                            "title",
//                            view.context.resources.getString(R.string.navi_sub_self)
//                        )
//                        intent.putExtra("type", BarcodeType.SELF_COLLECTION)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                    view.resources.getString(R.string.navi_sub_in_progress),
//                    view.resources.getString(R.string.navi_sub_upload_fail),
//                    view.resources.getString(R.string.navi_sub_today_done) -> {
//                        val intent = Intent(view.context, ListActivity::class.java)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                    view.resources.getString(R.string.navi_sub_not_in_housed) -> {
//                        val intent = Intent(view.context, ListNotInHousedActivity::class.java)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                    view.resources.getString(R.string.text_outlet_order_status) -> {
//                        val intent = Intent(view.context, OutletOrderStatusActivity::class.java)
//                        (view.context as AppBaseActivity).startActivity(intent)
//                    }
//                }
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