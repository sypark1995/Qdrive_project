package com.giosis.util.qdrive.singapore.list

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothListener
import com.giosis.util.qdrive.singapore.databinding.DialogPickupTripDetailBinding

class PickupTripDetailDialog(
    context: Context,
    private val list: ArrayList<RowItem>,
    private val listener: BluetoothListener
) : Dialog(context), PickupTripDetailAdapter.GetViewHeightListener {

    val binding by lazy {
        DialogPickupTripDetailBinding.inflate(layoutInflater)
    }

    private var totalHeight = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        val msg = String.format(
            context.resources.getString(R.string.text_trip_detail_requests),
            list.size
        )
        binding.textCountTitle.text = msg

        binding.listTripDetail.layoutManager = LinearLayoutManager(context)

        val tripDetailAdapter = PickupTripDetailAdapter(list, listener)
        binding.listTripDetail.adapter = tripDetailAdapter
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
            val params = binding.listTripDetail.layoutParams
            params.height = totalHeight
            binding.listTripDetail.layoutParams = params
        }
    }
}