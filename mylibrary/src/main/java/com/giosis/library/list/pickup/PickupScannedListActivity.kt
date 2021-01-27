package com.giosis.library.list.pickup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.list.ListTodayDoneFragment
import com.giosis.library.server.data.PickupScannedListResult
import com.giosis.library.util.BarcodeType
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.Preferences
import kotlinx.android.synthetic.main.activity_pickup_scanned_list.*
import kotlinx.android.synthetic.main.top_title.*
import java.util.*

/**
 * @author eylee 2017-03-14
 * @editor krm0219 2019.02
 * LIST - TODAY DONE > 'Add Scan' Button
 * LIST - TODAY DONE > 'Take Back' Button
 */
class PickupScannedListActivity : CommonActivity() {

    var tag = "TodayDonePickupScanListActivity"


    private var itemArrayList: ArrayList<PickupScannedListItem>? = null
    private var pickupScannedListAdapter: PickupScannedListAdapter? = null


    var pickupNo: String = ""
    var applicant: String = ""
    private var buttonType: String = ""
    var scannedQty: String = "0"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_scanned_list)


        pickupNo = intent.getStringExtra("pickupNo").toString()
        applicant = intent.getStringExtra("applicant").toString()
        buttonType = intent.getStringExtra("buttonType").toString()


        text_top_title.text = resources.getString(R.string.text_today_done_scan_list)
        text_add_scan_pickup_no.text = pickupNo

        if (buttonType == BarcodeType.PICKUP_ADD_SCAN) {

            btn_add_scan_add.setText(R.string.button_add_scan_list)
        } else if (buttonType == BarcodeType.PICKUP_TAKE_BACK) {

            btn_add_scan_add.setText(R.string.button_take_back)
        }


        layout_top_back.setOnClickListener {

            val intent = intent
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }

        btn_add_scan_add.setOnClickListener {

            if (buttonType == BarcodeType.PICKUP_ADD_SCAN) {

                clickAddScan()
            } else if (buttonType == BarcodeType.PICKUP_TAKE_BACK) {

                clickTakeBack()
            }
        }


        PickupScannedListDownloadHelper.Builder(this@PickupScannedListActivity, Preferences.userId, pickupNo)
                .setOnServerEventListener { result: PickupScannedListResult -> setScannedList(result) }.build().execute()
    }


    private fun setScannedList(result: PickupScannedListResult) {

        scannedQty = result.resultObject!!.size.toString()
        text_add_scan_scanned_qty!!.text = scannedQty

        itemArrayList = ArrayList()

        for (scanPackingList in result.resultObject!!) {
            val item = PickupScannedListItem()
            item.trackingNo = scanPackingList.packingNo
            item.scannedDate = scanPackingList.regDt
            itemArrayList!!.add(item)
        }

        pickupScannedListAdapter = PickupScannedListAdapter(itemArrayList)
        list_add_scan_scanned_list!!.adapter = pickupScannedListAdapter
        setListViewHeightBasedOnChildren(list_add_scan_scanned_list)
    }


    private fun clickAddScan() {


        try {

            val intent = Intent(this@PickupScannedListActivity, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
            intent.putExtra("title", resources.getString(R.string.button_add_scan_list))
            intent.putExtra("type", BarcodeType.PICKUP_ADD_SCAN)
            intent.putExtra("pickup_no", pickupNo)
            intent.putExtra("applicant", applicant)
            startActivityForResult(intent, ListTodayDoneFragment.REQUEST_ADD_SCAN)
        } catch (e: Exception) {

            Log.e("Exception", "$tag  Exception :  $e")
            Toast.makeText(this@PickupScannedListActivity, "Exception $e", Toast.LENGTH_SHORT).show()
        }
    }


    private fun clickTakeBack() {

        try {

            val intent = Intent(this@PickupScannedListActivity, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
            intent.putExtra("title", resources.getString(R.string.button_take_back))
            intent.putExtra("type", BarcodeType.PICKUP_TAKE_BACK)
            intent.putExtra("pickup_no", pickupNo)
            intent.putExtra("applicant", applicant)
            intent.putExtra("scanned_qty", scannedQty)
            startActivityForResult(intent, ListTodayDoneFragment.REQUEST_TAKE_BACK)
        } catch (e: Exception) {

            Log.e("Exception", "$tag  Exception :  $e")
            Toast.makeText(this@PickupScannedListActivity, "Exception $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ListTodayDoneFragment.REQUEST_ADD_SCAN || requestCode == ListTodayDoneFragment.REQUEST_TAKE_BACK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    companion object {

        fun setListViewHeightBasedOnChildren(listView: ListView?) {
            val listAdapter = listView!!.adapter ?: return
            var totalHeight = 0
            val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)
            for (i in 0 until listAdapter.count) {
                val listItem = listAdapter.getView(i, null, listView)
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                totalHeight += listItem.measuredHeight
            }

            val params = listView.layoutParams
            params.height = totalHeight
            listView.layoutParams = params
            listView.requestLayout()
        }
    }
}