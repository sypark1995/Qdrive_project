package com.giosis.util.qdrive.list

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.item_trip_detail.view.*

class PickupTripDetailAdapter(private val context: Context, private val list: ArrayList<RowItem>, private val adapter: CustomExpandableAdapter)
    : RecyclerView.Adapter<PickupTripDetailAdapter.ViewHolder>() {


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_trip_detail, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        list[position].let { item ->

            if (item.ref_pickup_no == "") {

                holder.view.text_trip_detail_tracking_no.text = item.shipping
            } else {

                holder.view.text_trip_detail_tracking_no.text = item.ref_pickup_no
            }


            if (item.shipping[0] == 'P') {

                holder.view.layout_trip_detail_cnr_print.visibility = View.GONE
            } else {

                holder.view.layout_trip_detail_cnr_print.visibility = View.VISIBLE
            }


            holder.view.text_trip_detail_address.text = item.address


            holder.view.layout_trip_detail_cnr_print.setOnClickListener {

                Log.e("trip", "CNR Print ${item.shipping}")
                adapter.isConnectPortablePrint(item.shipping)
            }
        }


        // 리스트에 최대 3개만 노출 > 주소 길이때문에 아이템 높이가 달라지기 때문에 화면 그려지고 높이값 구해서 전달
        holder.view.layout_trip_detail_item.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {

                        holder.view.layout_trip_detail_item.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        val height = holder.view.layout_trip_detail_item.height

                        if (position < 3) {

                            //    Log.e("trip", "OnGlobalLayoutListener height  $position / $height")
                            viewHeightListener.getViewHeight(position, height)
                        }
                    }
                })
    }


    lateinit var viewHeightListener: GetViewHeightListener

    interface GetViewHeightListener {
        fun getViewHeight(position: Int, height: Int)
    }

    fun setGetViewHeightListener(listener: GetViewHeightListener) {
        this.viewHeightListener = listener
    }

    override fun getItemCount(): Int {

        return list.size
    }
}