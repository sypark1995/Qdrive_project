package com.giosis.library.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import com.giosis.library.R
import com.giosis.library.util.BarcodeType
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.Preferences.userId
import kotlinx.android.synthetic.main.activity_pickup_scan_list.*
import kotlinx.android.synthetic.main.top_title.*
import java.util.*

/**
 * @author eylee 2017-03-14
 * @editor LIST > TODAY DONE > 'ADD SCAN' Button
 * LIST > TODAY DONE > 'Take Back' Button
 */
class TodayDonePickupScanListActivity : CommonActivity() {
    var TAG = "TodayDonePickupScanListActivity"

    var itemArrayList: ArrayList<PickupScanListItem>? = null
    var todayDonePickupScanListAdapter: TodayDonePickupScanListAdapter? = null
    var button_type: String? = null
    var pickup_no: String? = ""
    var applicant: String? = ""
    var scanned_qty: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_scan_list)

        val title = intent.getStringExtra("title")
        pickup_no = intent.getStringExtra("pickup_no")
        applicant = intent.getStringExtra("applicant")
        button_type = intent.getStringExtra("button_type")

        text_top_title.text = title
        text_add_scan_pickup_no.text = pickup_no

        if (button_type == "Add Scan") {
            btn_add_scan_add.setText(R.string.button_add_scan_list)
        } else if (button_type == "Take Back") {
            btn_add_scan_add.setText(R.string.button_take_back)
        }

        layout_top_back.setOnClickListener {
            val intent = intent
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        btn_add_scan_add.setOnClickListener {
            if (button_type == "Add Scan") {
                click_add_scan()
            } else if (button_type == "Take Back") {
                click_take_back()
            }
        }

        TodayScanPackingListDownloadHelper.Builder(this, userId, pickup_no)
                .setOnScanPackingListDownloadEventListener { result: PickupPackingListResult -> setScannedList(result) }.build().execute()
    }

    private fun setScannedList(result: PickupPackingListResult) {
        scanned_qty = Integer.toString(result.resultObject.size)
        text_add_scan_scanned_qty!!.text = scanned_qty
        itemArrayList = ArrayList()
        for (scanPackingList in result.resultObject) {
            val item = PickupScanListItem()
            item.tracking_no = scanPackingList.packingNo
            item.scanned_date = scanPackingList.regDt
            itemArrayList!!.add(item)
        }
        todayDonePickupScanListAdapter = TodayDonePickupScanListAdapter(itemArrayList)
        list_add_scan_scanned_list!!.adapter = todayDonePickupScanListAdapter

        setListViewHeightBasedOnChildren(list_add_scan_scanned_list)
    }

    // packing list 추가
    private fun click_add_scan() {
        val intent = Intent(this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
        intent.putExtra("title", "ADD Scan List")
        intent.putExtra("type", BarcodeType.PICKUP_ADD_SCAN)
        intent.putExtra("pickup_no", pickup_no)
        intent.putExtra("applicant", applicant)
        startActivityForResult(intent, ListTodayDoneFragment.REQUEST_ADD_SCAN)
    }

    private fun click_take_back() {
        val intent = Intent(this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"))
        intent.putExtra("title", "Take Back List")
        intent.putExtra("type", BarcodeType.PICKUP_TAKE_BACK)
        intent.putExtra("pickup_no", pickup_no)
        intent.putExtra("applicant", applicant)
        intent.putExtra("scanned_qty", scanned_qty)
        startActivityForResult(intent, ListTodayDoneFragment.REQUEST_TAKE_BACK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ListTodayDoneFragment.REQUEST_ADD_SCAN
                || requestCode == ListTodayDoneFragment.REQUEST_TAKE_BACK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object { // TODO_kjyoo 이상함...

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