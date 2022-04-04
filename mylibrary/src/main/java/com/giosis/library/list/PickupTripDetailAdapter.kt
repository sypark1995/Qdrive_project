package com.giosis.library.list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.databinding.ItemTripDetailBinding
import com.giosis.library.util.DataUtil

class PickupTripDetailAdapter(
    private val list: ArrayList<RowItem>,
    private val listener: BluetoothListener
) : RecyclerView.Adapter<PickupTripDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTripDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemTripDetailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        list[position].let { item ->

            if (item.ref_pickup_no == "") {
                holder.binding.textTrackingNo.text = item.shipping

            } else {
                holder.binding.textTrackingNo.text = item.ref_pickup_no
            }

            if (item.shipping[0] == 'P') {
                holder.binding.layoutCnrPrint.visibility = View.GONE
            } else {
                holder.binding.layoutCnrPrint.visibility = View.VISIBLE
            }

            holder.binding.textAddress.text = item.address

            holder.binding.layoutCnrPrint.setOnClickListener {

                Log.e("List", "Pickup Trip Clicked >> ${item.shipping}")
                DataUtil.logEvent("button_click", "ListActivity", "Print_CNR");
                listener.isConnectPortablePrint(item.shipping)
            }
        }

        // 리스트에 최대 3개만 노출 > 주소 길이때문에 아이템 높이가 달라지기 때문에 화면 그려지고 높이값 구해서 전달
        holder.binding.layoutItem.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {

                    holder.binding.layoutItem.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val height = holder.binding.layoutItem.height

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