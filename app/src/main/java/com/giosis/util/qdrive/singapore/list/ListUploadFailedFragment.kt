package com.giosis.util.qdrive.singapore.list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.UploadData
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.main.DeviceDataUploadHelper
import com.giosis.util.qdrive.singapore.util.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * In-Progress Tab Fragment
 */
class ListUploadFailedFragment : Fragment(), SearchView.OnQueryTextListener,
    SearchView.OnCloseListener, ListUploadFailedAdapter.OnItemClickListener {

    var TAG = "ListUploadFailedFragment"
    private var gpsTrackerManager: GPSTrackerManager? = null
    private var gpsEnable = false
    private var checker: PermissionChecker? = null
    private var isPermissionTrue = false
    private var searchViewList: SearchView? = null
    private var layoutListSort: FrameLayout? = null
    private var spinnerListSort: Spinner? = null
    private var exListCardList: RecyclerView? = null
    var mFailedCountCallback: OnFailedCountListener? = null
    private var orderby = "zip_code desc"
    private lateinit var adapter: ListUploadFailedAdapter

    private var rowItems = ArrayList<RowItemNotUpload>()

    //    리스트 카운트를 갱신하기 위한 인터페이스
    interface OnFailedCountListener {
        fun onFailedCountRefresh(count: Int)
    }

    //부모 Activity와 통신을 하기 위한 연결
    override fun onAttach(context: Context) {
        super.onAttach(context)
        var activity: Activity? = null
        if (context is Activity) {
            activity = context
        }
        mFailedCountCallback = try {
            activity as OnFailedCountListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnFailedCountListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checker = PermissionChecker(requireActivity())
        if (checker!!.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                activity,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
            requireActivity().overridePendingTransition(0, 0)
        } else {
            isPermissionTrue = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inprogress, container, false)

        searchViewList = view.findViewById(R.id.search_view)
        layoutListSort = view.findViewById(R.id.layout_list_sort)
        spinnerListSort = view.findViewById(R.id.spinner_list_sort)
        exListCardList = view.findViewById(R.id.exlist_card_list)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Search
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchViewList!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchViewList!!.queryHint = resources.getString(R.string.text_search)
        searchViewList!!.setOnQueryTextListener(this)
        searchViewList!!.setOnCloseListener(this)
        val id = searchViewList!!.context.resources.getIdentifier(
            "android:id/search_src_text",
            null,
            null
        )
        val editListSearchView = searchViewList!!.findViewById<EditText>(id)
        editListSearchView.setTextColor(Color.parseColor("#8F8F8F"))
        editListSearchView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_26px)
        )
        editListSearchView.setHintTextColor(Color.parseColor("#8F8F8F"))
        layoutListSort!!.setOnClickListener { spinnerListSort!!.performClick() }
        val spinnerList: ArrayList<String> = ArrayList(
            listOf(
                resources.getString(R.string.text_sort_postal_code_asc),
                resources.getString(R.string.text_sort_postal_code_desc),
                resources.getString(R.string.text_sort_tracking_no_asc),
                resources.getString(R.string.text_sort_tracking_no_desc),
                resources.getString(R.string.text_sort_name_asc),
                resources.getString(R.string.text_sort_name_desc)
            )
        )
        val orderbyQuery = arrayOf(
            "zip_code asc",
            "zip_code desc",
            "invoice_no asc",
            "invoice_no desc",
            "rcv_nm asc",
            "rcv_nm desc"
        )
        val adapterSpinner: ArrayAdapter<String> = ArrayAdapter(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item,
        )
        adapterSpinner.addAll(spinnerList)

        spinnerListSort!!.prompt = "Sort by" // 스피너 제목
        spinnerListSort!!.adapter = adapterSpinner
        spinnerListSort!!.setSelection(1) //Zipcode desc
        spinnerListSort!!.post {
            spinnerListSort!!.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id1: Long
                ) {
                    orderby = orderbyQuery[position]
                    onResume()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        rowItems = ArrayList()
        adapter = ListUploadFailedAdapter(this)
        adapter.rowItem = rowItems
        exListCardList!!.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        val cs2 =
            DatabaseHelper.getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat <> 'S' and chg_dt is not null and reg_id='" + Preferences.userId + "' order by " + orderby]

        rowItems = ArrayList()
        var childItems: ArrayList<ChildItemNotUpload>

        if (cs2.moveToFirst()) {
            do {
                childItems = ArrayList()
                val child = ChildItemNotUpload()
                child.hp = cs2.getString(cs2.getColumnIndex("hp_no"))
                child.tel = cs2.getString(cs2.getColumnIndex("tel_no"))
                child.stat = cs2.getString(cs2.getColumnIndex("stat"))
                child.statMsg = cs2.getString(cs2.getColumnIndex("driver_memo"))
                child.statReason = cs2.getString(cs2.getColumnIndex("fail_reason"))
                child.receiveType = cs2.getString(cs2.getColumnIndex("rcv_type"))
                child.realQty = cs2.getString(cs2.getColumnIndex("real_qty"))
                child.retryDay = cs2.getString(cs2.getColumnIndex("retry_dt"))
                child.secretNoType = cs2.getString(cs2.getColumnIndex("secret_no_type"))
                child.secretNo = cs2.getString(cs2.getColumnIndex("secret_no"))
                childItems.add(child)

                //배송 타입
                val deliveryType = cs2.getString(cs2.getColumnIndex("type"))
                var rcvName: String? = ""
                if (deliveryType == "D") {
                    rcvName = cs2.getString(cs2.getColumnIndex("rcv_nm")) //구매자
                } else if (deliveryType == "P") {
                    rcvName = cs2.getString(cs2.getColumnIndex("req_nm")) //픽업 요청 셀러
                }
                val rowitem = RowItemNotUpload(
                    cs2.getString(cs2.getColumnIndex("stat")),
                    cs2.getString(cs2.getColumnIndex("invoice_no")),
                    rcvName!!,
                    "(" + cs2.getString(cs2.getColumnIndex("zip_code")) + ") "
                            + cs2.getString(cs2.getColumnIndex("address")),
                    cs2.getString(cs2.getColumnIndex("rcv_request")),
                    deliveryType,
                    cs2.getString(cs2.getColumnIndex("route")),
                    cs2.getString(cs2.getColumnIndex("sender_nm"))
                )
                rowitem.items = childItems
                rowItems.add(rowitem)
            } while (cs2.moveToNext())
        }

        adapter.setSorting(rowItems)

        //카운트 갱신
        mFailedCountCallback!!.onFailedCountRefresh(rowItems.size)
        if (isPermissionTrue) {
            gpsTrackerManager = GPSTrackerManager(requireActivity())
            gpsEnable = gpsTrackerManager!!.enableGPSSetting()
            if (gpsEnable && gpsTrackerManager != null) {
                gpsTrackerManager!!.gpsTrackerStart()
                adapter.setGpsTrackerManager(gpsTrackerManager)
            } else {
                DataUtil.enableLocationSettings(requireActivity())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (gpsTrackerManager != null) {
            gpsTrackerManager!!.stopFusedProviderService()
            gpsTrackerManager = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                isPermissionTrue = true
                Log.e("Permission", "$TAG   onActivityResult  PERMISSIONS_GRANTED")
            }
        }
    }

    override fun onClose(): Boolean {
        adapter.filterData("")
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        adapter.filterData(query)
        return false
    }

    override fun onQueryTextChange(query: String): Boolean {
        try {
            adapter.filterData(query)
        } catch (e: Exception) {
            Log.e("Exception", "$TAG  onQueryTextChange Exception : $e")
        }
        return false
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            PermissionChecker.READ_EXTERNAL_STORAGE,
            PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_FINE_LOCATION,
            PermissionChecker.ACCESS_COARSE_LOCATION
        )
    }

    override fun itemMenuIconClick(view: View) {
        val popup =
            PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.quickmenu_failed, popup.menu)
        popup.setOnMenuItemClickListener {
            val cs3 =
                DatabaseHelper.getInstance()["SELECT address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + view.tag
                    .toString() + "' LIMIT 1"]
            cs3.moveToFirst()
            val address = cs3.getString(cs3.getColumnIndex("address"))
            val uri =
                Uri.parse("http://maps.google.co.in/maps?q=$address")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            view.context.startActivity(intent)
            true
        }
        popup.show()
    }

    override fun telePhoneNumberClicked(data: RowItemNotUpload) {
        val callUri = Uri.parse("tel:" + data.items?.get(0)?.tel)
        val intent = Intent(Intent.ACTION_DIAL, callUri)
        startActivity(intent)
    }

    override fun mobileNumberClicked(data: RowItemNotUpload) {
        val callUri = Uri.parse("tel:" + data.items?.get(0)?.hp)
        val intent = Intent(Intent.ACTION_DIAL, callUri)
        startActivity(intent)
    }

    override fun smsClicked(data: RowItemNotUpload) {
        val smsBody = String.format(
            resources.getString(R.string.msg_delivery_start_sms), data.name
        )
        val smsUri = Uri.parse("sms:" + data.items?.get(0)?.hp)
        val intent = Intent(Intent.ACTION_SENDTO, smsUri)
        intent.putExtra("sms_body", smsBody)
        startActivity(intent)
    }

    override fun live10Clicked(data: RowItemNotUpload) {
        val alert =
            AlertDialog.Builder(requireContext())
        val msg = String.format(
            resources.getString(R.string.msg_delivery_start_sms), data.name
        )
        alert.setTitle(resources.getString(R.string.text_qpost_message))
        val input = EditText(requireContext())
        input.setText(msg)
        alert.setView(input)
        alert.setPositiveButton(
            resources.getString(R.string.button_send)
        ) { _: DialogInterface?, _: Int ->
            val value = input.text.toString()
            // Qtalk sms 전송
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data =
                Uri.parse("qtalk://link?qnumber=" + data.items?.get(0)?.secretNo + "&msg=" + value + "&link=&execurl=")
            startActivity(intent)
        }
        alert.setNegativeButton(
            resources.getString(R.string.button_cancel)
        ) { _: DialogInterface?, _: Int -> }
        alert.show()
    }

}