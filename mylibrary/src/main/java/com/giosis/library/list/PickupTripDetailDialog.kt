package com.giosis.library.list

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.giosis.library.R
import com.giosis.library.bluetooth.BluetoothListener
import kotlinx.android.synthetic.main.dialog_pickup_trip_detail.*


class PickupTripDetailDialog(context: Context, private val list: ArrayList<RowItem>, private val listener: BluetoothListener)
    : Dialog(context), PickupTripDetailAdapter.GetViewHeightListener {

    private var totalHeight = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_pickup_trip_detail)

        btn_trip_detail_close.setOnClickListener {
            dismiss()
        }

        val msg = String.format(context.resources.getString(R.string.text_trip_detail_requests), list.size)
        text_trip_detail_count_title.text = msg

        list_dialog_trip_detail.layoutManager = LinearLayoutManager(context)

        val tripDetailAdapter = PickupTripDetailAdapter(context, list, listener)
        list_dialog_trip_detail.adapter = tripDetailAdapter
        tripDetailAdapter.setGetViewHeightListener(this)
    }


    override fun getViewHeight(position: Int, height: Int) {

        var maxCount = 3
        if (list.size < 3) {
            maxCount = list.size
        }

        totalHeight += height

        if (position == (maxCount - 1)) {

            //    Log.e("trip", "Final Height $height  ->  $totalHeight")
            val params = list_dialog_trip_detail.layoutParams
            params.height = totalHeight
            list_dialog_trip_detail.layoutParams = params
        }
    }
}