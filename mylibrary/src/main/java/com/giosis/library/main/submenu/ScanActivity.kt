package com.giosis.library.main.submenu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.giosis.library.R
import com.giosis.library.util.BarcodeType
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.Preferences
import com.giosis.library.util.Preferences.outletDriver
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.top_title.*

/**
 * @author krm0219
 */
class ScanActivity : CommonActivity() {
    var TAG = "ScanActivity"

    var context: Context? = null
    lateinit var officeName: String
    var outletDriverYN = "N"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)


        text_top_title.setText(R.string.text_title_delivery_scan)

        context = applicationContext
        officeName = Preferences.officeName

        outletDriverYN = try {
            outletDriver
        } catch (e: Exception) {
            "N"
        }


        val msg: String
        if (outletDriverYN == "Y") {

            msg = String.format(context!!.resources.getString(R.string.msg_delivery_scan2), context!!.resources.getString(R.string.text_start_delivery_for_outlet))
            text_scan_confirm_my_delivery_order.setText(R.string.text_start_delivery_for_outlet)
        } else {

            msg = String.format(context!!.resources.getString(R.string.msg_delivery_scan2), context!!.resources.getString(R.string.button_confirm_my_delivery_order))
            text_scan_confirm_my_delivery_order.setText(R.string.button_confirm_my_delivery_order)
        }
        text_scan_delivery_scan_msg.text = msg


        if (officeName.contains("Qxpress SG")) {
            layout_scan_self_collection_shown.visibility = View.VISIBLE
        } else {
            layout_scan_self_collection_shown.visibility = View.GONE
        }


        layout_top_back.setOnClickListener(clickListener)
        layout_scan_confirm_my_delivery_order.setOnClickListener(clickListener)
        layout_scan_delivery_done.setOnClickListener(clickListener)
        layout_scan_pickup_cnr.setOnClickListener(clickListener)
        layout_scan_self_collection.setOnClickListener(clickListener)
    }

    var clickListener = View.OnClickListener { v ->

        when (v.id) {
            R.id.layout_top_back -> {

                finish()
            }
            R.id.layout_scan_confirm_my_delivery_order -> {

                try {

                    val intent = Intent(this@ScanActivity, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
                    intent.putExtra("title", context!!.resources.getString(R.string.text_title_driver_assign))
                    intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
                    startActivity(intent)
                } catch (e: Exception) {

                    Log.e("Exception", "$TAG  Exception  $e")
                }
            }
            R.id.layout_scan_delivery_done -> {

                try {

                    val intent = Intent(this@ScanActivity, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
                    intent.putExtra("title", context!!.resources.getString(R.string.text_delivered))
                    intent.putExtra("type", BarcodeType.DELIVERY_DONE)
                    startActivity(intent)
                } catch (e: Exception) {

                    Log.e("Exception", "$TAG  Exception  $e")
                }
            }
            R.id.layout_scan_pickup_cnr -> {

                try {

                    val intent = Intent(this@ScanActivity, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
                    intent.putExtra("title", context!!.resources.getString(R.string.text_title_scan_pickup_cnr))
                    intent.putExtra("type", BarcodeType.PICKUP_CNR)
                    startActivity(intent)
                } catch (e: Exception) {

                    Log.e("Exception", "$TAG  Exception  $e")
                }
            }
            R.id.layout_scan_self_collection -> {

                try {

                    val intent = Intent(this@ScanActivity, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
                    intent.putExtra("title", context!!.resources.getString(R.string.navi_sub_self))
                    intent.putExtra("type", BarcodeType.SELF_COLLECTION)
                    startActivity(intent)
                } catch (e: Exception) {

                    Log.e("Exception", "$TAG  Exception  $e")
                }
            }
        }
    }
}