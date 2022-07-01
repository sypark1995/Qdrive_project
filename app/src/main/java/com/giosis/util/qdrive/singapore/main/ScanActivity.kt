package com.giosis.util.qdrive.singapore.main


import android.content.Intent
import android.os.Bundle
import android.view.View
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.barcodescanner.CaptureActivity
import com.giosis.util.qdrive.singapore.barcodescanner.CaptureActivity1
import com.giosis.util.qdrive.singapore.barcodescanner.CaptureType
import com.giosis.util.qdrive.singapore.databinding.ActivityScanBinding
import com.giosis.util.qdrive.singapore.util.*


class ScanActivity : CommonActivity() {
    var TAG = "ScanActivity"

    private val binding by lazy {
        ActivityScanBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseEvent.createEvent(this, TAG)

        binding.layoutTopTitle.textTopTitle.setText(R.string.text_title_delivery_scan)

        val msg: String
        if (Preferences.outletDriver == "Y") {
            msg = String.format(
                resources.getString(R.string.msg_delivery_scan2),
                resources.getString(R.string.text_start_delivery_for_outlet)
            )
            binding.textConfirmMyDeliveryOrder.setText(R.string.text_start_delivery_for_outlet)
        } else {
            msg = String.format(
                resources.getString(R.string.msg_delivery_scan2),
                resources.getString(R.string.button_confirm_my_delivery_order)
            )
            binding.textConfirmMyDeliveryOrder.setText(R.string.button_confirm_my_delivery_order)
        }

        binding.textDeliveryScanMsg.text = msg
        if (Preferences.userNation == Common.SG) {
            binding.textPickupScan.text = resources.getText(R.string.navi_sub_pickup_sg)
        } else {
            binding.textPickupScan.text = resources.getText(R.string.navi_sub_pickup)
        }

        if (Preferences.officeName.contains("Qxpress SG")) {
            binding.layoutSelfCollectionShown.visibility = View.VISIBLE
        } else {
            binding.layoutSelfCollectionShown.visibility = View.GONE
        }

        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            finish()
        }

        binding.layoutConfirmMyDeliveryOrder.setOnClickListener {
            val intent = Intent(this@ScanActivity, CaptureActivity1::class.java)
            intent.putExtra("title", resources.getString(R.string.text_title_driver_assign))
            intent.putExtra("type", CaptureType.CONFIRM_MY_DELIVERY_ORDER)
            startActivity(intent)
        }

        binding.layoutDeliveryDone.setOnClickListener {

            val intent = Intent(this@ScanActivity, CaptureActivity1::class.java)
            intent.putExtra("title", resources.getString(R.string.text_delivered))
            intent.putExtra("type", CaptureType.DELIVERY_DONE)
            startActivity(intent)
        }

        binding.layoutPickupCnr.setOnClickListener {

            val intent = Intent(this@ScanActivity, CaptureActivity1::class.java)
            intent.putExtra("title", resources.getString(R.string.text_title_scan_pickup_cnr))
            intent.putExtra("type", CaptureType.PICKUP_CNR)
            startActivity(intent)
        }

        binding.layoutSelfCollection.setOnClickListener {

            val intent = Intent(this@ScanActivity, CaptureActivity1::class.java)
            intent.putExtra("title", resources.getString(R.string.navi_sub_self))
            intent.putExtra("type", CaptureType.SELF_COLLECTION)
            startActivity(intent)
        }
    }
}