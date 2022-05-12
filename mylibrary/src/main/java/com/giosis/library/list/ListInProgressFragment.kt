package com.giosis.library.list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.main.PickupAssignResult
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class ListInProgressFragment(var bluetoothListener: BluetoothListener) : Fragment(),
    SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    var TAG = "ListInProgressFragment"
    private var selectedSort: String = ""
    private var check = 0
    private val orderbyQuery = arrayOf(
        "zip_code asc",
        "zip_code desc",
        "invoice_no asc",
        "invoice_no desc",
        "rcv_nm asc",
        "rcv_nm desc"
    )

    private val adapter by lazy {
        ListInProgressAdapter(bluetoothListener)
    }

    private var rowItems = ArrayList<RowItem>()

    // 2020.07  ByTrip 정렬기능 추가
    private var pickupSortCondition = "R" // R : Request(기본) / T : Trip (묶음배송)
    private var tripArrayList = ArrayList<RowItem>()

    private var fragmentListener: OnInProgressFragmentListener? = null
    private var inputMethodManager: InputMethodManager? = null
    private var layoutListPickupSortCondition: ConstraintLayout? = null

    private var progressInProgress: ProgressBar? = null
    private var searchView: SearchView? = null
    private lateinit var editListSearchView: EditText
    private var layoutListSort: FrameLayout? = null
    private var spinnerListSort: NDSpinner? = null
    private var exlistCardList: RecyclerView? = null

    interface OnInProgressFragmentListener {
        fun onCountRefresh(count: Int)
        fun onTodayDoneCountRefresh(count: Int)
    }

    //부모 Activity 와 통신을 하기 위한 연결
    override fun onAttach(context: Context) {
        super.onAttach(context)
        var activity: Activity? = null
        if (context is Activity) {
            activity = context
        }
        try {
            fragmentListener = activity as OnInProgressFragmentListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnCountListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val checker = PermissionChecker(
            activity
        )
        if (checker.lacksPermissions(*PERMISSIONS)) {
            PermissionActivity.startActivityForResult(
                activity,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
            requireActivity().overridePendingTransition(0, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = inflater.inflate(R.layout.fragment_inprogress, container, false)

        layoutListPickupSortCondition =
            view.findViewById(R.id.layout_list_pickup_sort_condition)
        val btnListPickupSortRequest = view.findViewById<Button>(R.id.btn_list_pickup_sort_request)
        val btnListPickupSortTrip = view.findViewById<Button>(R.id.btn_list_pickup_sort_trip)
        progressInProgress = view.findViewById(R.id.progress_in_progress)
        searchView = view.findViewById(R.id.search_view)
        layoutListSort = view.findViewById(R.id.layout_list_sort)
        spinnerListSort = view.findViewById(R.id.spinner_list_sort)
        exlistCardList = view.findViewById(R.id.exlist_card_list)

        btnListPickupSortRequest.setOnClickListener {
            DataUtil.inProgressListPosition = 0
            if (pickupSortCondition != "R") {
                btnListPickupSortRequest.setBackgroundResource(R.drawable.bg_round_4_ffffff)
                btnListPickupSortRequest.setTextColor(Color.parseColor("#4e4e4e"))
                btnListPickupSortTrip.setBackgroundResource(R.color.transparent)
                btnListPickupSortTrip.setTextColor(Color.parseColor("#8F8F8F"))
                pickupSortCondition = "R"
                onResume()
            }
        }

        btnListPickupSortTrip.setOnClickListener {
            DataUtil.inProgressListPosition = 0
            if (pickupSortCondition != "T") {
                btnListPickupSortRequest.setBackgroundResource(R.color.transparent)
                btnListPickupSortRequest.setTextColor(Color.parseColor("#8F8F8F"))
                btnListPickupSortTrip.setBackgroundResource(R.drawable.bg_round_4_ffffff)
                btnListPickupSortTrip.setTextColor(Color.parseColor("#4e4e4e"))
                pickupSortCondition = "T"
                onResume()
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Search
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView!!.queryHint = resources.getString(R.string.text_search)
        searchView!!.setOnQueryTextListener(this)
        searchView!!.setOnCloseListener(this)

        val id = searchView!!.resources.getIdentifier("android:id/search_src_text", null, null)
        editListSearchView = searchView!!.findViewById(id)
        editListSearchView.setTextColor(Color.parseColor("#8F8F8F"))
        editListSearchView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_26px)
        )

        editListSearchView.setHintTextColor(Color.parseColor("#8F8F8F"))
        layoutListSort!!.setOnClickListener { spinnerListSort!!.performClick() }
        val sortArrayList: ArrayList<String> = ArrayList<String>(
            listOf(
                resources.getString(R.string.text_sort_postal_code_asc),
                resources.getString(R.string.text_sort_postal_code_desc),
                resources.getString(R.string.text_sort_tracking_no_asc),
                resources.getString(R.string.text_sort_tracking_no_desc),
                resources.getString(R.string.text_sort_name_asc),
                resources.getString(R.string.text_sort_name_desc)
            )
        )
        val sortArrayAdapter =
            ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                sortArrayList
            )
        spinnerListSort!!.adapter = sortArrayAdapter
        try {
            spinnerListSort!!.setSelection(Preferences.sortIndex)
            selectedSort = orderbyQuery[Preferences.sortIndex]
        } catch (e: Exception) {
            Log.e("Exception", "$TAG  Spinner Exception : $e")
            Preferences.sortIndex = 0
            spinnerListSort!!.setSelection(0)
            selectedSort = orderbyQuery[0]
        }
        spinnerListSort!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if (++check > 1) {
                    Preferences.sortIndex = position
                    selectedSort = orderbyQuery[position]
                    onResume()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        adapter.setOnItemClickListener(object : ListInProgressAdapter.OnItemClickListener {
            override fun selectItem(v: View, selectedPos: Int, height: Int) {
                exlistCardList!!.smoothSnapToPosition(selectedPos)
            }
        })
    }

    fun RecyclerView.smoothSnapToPosition(
        position: Int,
        snapMode: Int = LinearSmoothScroller.SNAP_TO_START
    ) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    override fun onResume() {
        super.onResume()

        // NOTIFICATION
        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y") {
            layoutListPickupSortCondition!!.visibility = View.VISIBLE
            if (pickupSortCondition == "T") {
                sortByTrip()
            }
        }
        try {
            inputMethodManager!!.hideSoftInputFromWindow(editListSearchView.windowToken, 0)
            editListSearchView.setText("")
            editListSearchView.clearFocus()
        } catch (e: Exception) {
            Log.e("Exception", "search init  Exception : $e")
        }

        // 일반 정렬
        getNormalList()
    }

    private fun getNormalList() {
        exlistCardList!!.visibility = View.VISIBLE

        // 정렬
        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y" && pickupSortCondition == "T") {
            Collections.sort(tripArrayList, CompareRowItem(selectedSort))
            rowItems = tripArrayList
        } else {
            Log.e(TAG, "getSortList  $selectedSort")
            rowItems = getSortList(selectedSort)
            Log.e(TAG, "getSortList  Finish")
        }

        adapter.itemList = rowItems

        exlistCardList!!.adapter = adapter
        adapter.setSorting(rowItems)

        // LIST 들어갈 때 TODAY DONE Count 표시하기 위함.
        // ViewPage 특성상 TODAY DONE 페이지는 처음에 호출되지 않아서 0 으로 표시되어있음.
        try {
            RetrofitClient.instanceDynamic().requestGetTodayPickupDoneList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    //     Log.i("Server", " requestGetTodayPickupDoneList  result  " + it.getResultCode());
                    if (it.resultCode == 0) {
                        val list =
                            Gson().fromJson<ArrayList<PickupAssignResult.QSignPickupList>>(
                                it.resultObject,
                                object :
                                    TypeToken<ArrayList<PickupAssignResult.QSignPickupList?>?>() {}.type
                            )
                        if (isAdded) {
                            fragmentListener!!.onTodayDoneCountRefresh(list?.size ?: 0)
                        }
                    }
                }
                ) {
                    Log.e(
                        RetrofitClient.errorTag,
                        "$TAG - $it"
                    )
                }
        } catch (e: java.lang.Exception) {
            Log.e("Exception", "getTodayPickupDone API Exception : $e")
        }
        fragmentListener!!.onCountRefresh(rowItems.size)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e(TAG, "   onActivityResult  PERMISSIONS_GRANTED")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        bluetoothListener.clearBluetoothAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothListener.clearBluetoothAdapter()
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
        adapter.filterData(query)
        return false
    }

    // NOTIFICATION. 기본 정렬
    private fun getSortList(orderby: String?): ArrayList<RowItem> {
        val resultArrayList = ArrayList<RowItem>()
        val cs = DatabaseHelper.getInstance()[("SELECT * FROM "
                + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and reg_id='" + Preferences.userId + "' order by " + orderby)]
        if (cs.moveToFirst()) {
            do {
                val childItems = ArrayList<ChildItem>()
                val child = ChildItem()
                child.hp = cs.getString(cs.getColumnIndex("hp_no"))
                child.tel = cs.getString(cs.getColumnIndex("tel_no"))
                child.stat = cs.getString(cs.getColumnIndex("stat"))
                child.statMsg = cs.getString(cs.getColumnIndex("driver_memo"))
                child.statReason = cs.getString(cs.getColumnIndex("fail_reason"))
                child.secretNoType = cs.getString(cs.getColumnIndex("secret_no_type"))
                child.secretNo = cs.getString(cs.getColumnIndex("secret_no"))
                childItems.add(child)
                var delay: Long = 0
                if (cs.getString(cs.getColumnIndex("delivery_dt")) != null && cs.getString(
                        cs.getColumnIndex(
                            "delivery_dt"
                        )
                    ) != ""
                ) {
                    try {
                        delay = diffOfDate(cs.getString(cs.getColumnIndex("delivery_dt")))
                    } catch (e: Exception) {
                        Log.e("Exception", "$TAG  diffOfDate Exception : $e")
                    }
                }

                // Route
                val routeType = cs.getString(cs.getColumnIndex("route"))
                //배송 타입
                val deliveryType = cs.getString(cs.getColumnIndex("type"))
                var rcvName: String? = ""
                if (deliveryType == "D") {
                    rcvName = cs.getString(cs.getColumnIndex("rcv_nm")) //구매자
                } else if (deliveryType == "P") {
                    rcvName = cs.getString(cs.getColumnIndex("req_nm")) //픽업 요청 셀러
                }
                val rowItem = RowItem(
                    cs.getString(cs.getColumnIndex("contr_no")),
                    "D+$delay",
                    cs.getString(cs.getColumnIndex("invoice_no")),
                    rcvName!!,
                    "(" + cs.getString(cs.getColumnIndex("zip_code")) + ") "
                            + cs.getString(cs.getColumnIndex("address")),
                    cs.getString(cs.getColumnIndex("rcv_request")),
                    deliveryType,
                    routeType,
                    cs.getString(cs.getColumnIndex("sender_nm")),
                    cs.getString(cs.getColumnIndex("desired_date")),
                    cs.getString(cs.getColumnIndex("req_qty")),
                    cs.getString(cs.getColumnIndex("self_memo")),
                    cs.getDouble(cs.getColumnIndex("lat")),
                    cs.getDouble(cs.getColumnIndex("lng")),
                    cs.getString(cs.getColumnIndex("stat")),
                    cs.getString(cs.getColumnIndex("cust_no")),
                    cs.getString(cs.getColumnIndex("partner_id")),
                    cs.getString(cs.getColumnIndex("secure_delivery_yn")),
                    cs.getString(cs.getColumnIndex("parcel_amount")),
                    cs.getString(cs.getColumnIndex("currency")),
                    cs.getString(cs.getColumnIndex("high_amount_yn"))
                )
                rowItem.zip_code = cs.getString(cs.getColumnIndex("zip_code"))
                rowItem.state = cs.getString(cs.getColumnIndex("state"))
                rowItem.city = cs.getString(cs.getColumnIndex("city"))
                rowItem.street = cs.getString(cs.getColumnIndex("street"))
                //

                // NOTIFICATION.  2019.10  invoice와 같은지 체크! 같으면 저장 x
                if ((deliveryType == "P")) {
                    if ((cs.getString(cs.getColumnIndex("invoice_no")) == cs.getString(
                            cs.getColumnIndex(
                                "partner_ref_no"
                            )
                        ))
                    ) {
                        rowItem.ref_pickup_no = ""
                    } else {
                        rowItem.ref_pickup_no = cs.getString(cs.getColumnIndex("partner_ref_no"))
                    }
                }
                if ((deliveryType == "D")) {
                    rowItem.order_type_etc = cs.getString(cs.getColumnIndex("order_type_etc"))
                    rowItem.orderType = cs.getString(cs.getColumnIndex("order_type"))
                }
                if ((routeType == "RPC")) {
                    rowItem.desired_time = cs.getString(cs.getColumnIndex("desired_time"))
                }
                rowItem.items = childItems

                // k. Outlet Delivery 경우 같은 지점은 하나만 나오도록 수정
                if (0 < resultArrayList.size) {
                    var isRegisteredRoute = false
                    for (i in resultArrayList.indices) {
                        if (deliveryType.equals("D", ignoreCase = true)) {
                            if (routeType.contains("7E") || routeType.contains("FL")) {

                                // ex. 7E 001 name1, 7E 002 name2  / ex. FL FLA10001 mrtA, FL FLS10001 mrtB
                                val routeSplit = routeType.split(" ").toTypedArray()
                                if (1 < routeSplit.size) {
                                    val routeNumber = routeSplit[0] + " " + routeSplit[1]
                                    if ((resultArrayList[i].type == "D") && resultArrayList[i].route.contains(
                                            routeNumber
                                        )
                                    ) {
                                        isRegisteredRoute = true
                                    }
                                }
                            }
                        }
                    }
                    if (!isRegisteredRoute) {
                        resultArrayList.add(rowItem)
                    }
                } else {
                    resultArrayList.add(rowItem)
                }
            } while (cs.moveToNext())
        }

        // k. Outlet 정보 추가
        for (i in resultArrayList.indices) {
            if (resultArrayList[i].type.equals("D", ignoreCase = true)) {
                resultArrayList[i].outlet_company = resultArrayList[i].route
                if (resultArrayList[i].route.contains("7E") || resultArrayList[i].route.contains("FL")) {
                    val routeSplit = resultArrayList[i].route.split(" ").toTypedArray()
                    if (1 < routeSplit.size) {
                        val routeNumber = routeSplit[0] + " " + routeSplit[1]
                        val cursor = DatabaseHelper.getInstance()[("SELECT count(*) FROM "
                                + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and " +
                                "reg_id='" + Preferences.userId + "' and route LIKE '%" + routeNumber + "%'")]
                        cursor.moveToFirst()
                        val count = cursor.getInt(0)
                        val sb = StringBuilder()
                        for (j in 2 until routeSplit.size) {
                            sb.append(routeSplit[j])
                            sb.append(" ")
                        }
                        resultArrayList[i].outlet_company = routeSplit[0]
                        resultArrayList[i].outlet_store_code = routeSplit[1]
                        resultArrayList[i].outlet_store_name = sb.toString().trim { it <= ' ' }
                        resultArrayList[i].outlet_qty = count
                    }
                }
            } else {        // Pickup
                resultArrayList[i].outlet_company = resultArrayList[i].route
                if (resultArrayList[i].route.contains("7E") || resultArrayList[i].route.contains("FL")) {
                    val routeSplit = resultArrayList[i].route.split(" ").toTypedArray()
                    if (1 < routeSplit.size) {
                        val sb = StringBuilder()
                        for (j in 2 until routeSplit.size) {
                            sb.append(routeSplit[j])
                            sb.append(" ")
                        }
                        resultArrayList[i].outlet_company = routeSplit[0]
                        resultArrayList[i].outlet_store_code = routeSplit[1]
                        resultArrayList[i].outlet_store_name = sb.toString().trim { it <= ' ' }
                    }
                }
            }
        }
        return resultArrayList
    }

    // NOTIFICATION. By Trip 정렬기능 추가
    // 픽업 우편번호 & 픽업지 핸드폰 번호 동일
    // P 번호로만 묶인 경우는 제외
    // C or R 번호로만 묶이거나, C or R + P 번호와 묶인 경우에만 해당
    private fun sortByTrip() {
        tripArrayList = ArrayList()
        val tempArrayList = getSortList(orderbyQuery[0])
        Collections.sort(tempArrayList, TripMultiComparator())
        var tripNo = 1
        var count = 0
        // Trip 묶음 번호
        while (count < tempArrayList.size - 1) {
            val item = tempArrayList[count]
            val nextItem = tempArrayList[count + 1]
            if ((item.zip_code == nextItem.zip_code) && (item.items!![0].hp == nextItem.items!![0].hp)) {
                // 픽업 우편번호 & 픽업지 핸드폰 번호 동일
                if (item.shipping[0] == 'P' && nextItem.shipping[0] == 'P') {
                    // P번호 끼리는 묶일 수 없음
                    item.tripNo = tripNo++
                } else {
                    item.tripNo = tripNo
                }
            } else {

                // 우편번호 다름 || 핸드폰번호 다름
                item.tripNo = tripNo++
            }

            // 마지막 List 값
            if ((count + 1) == tempArrayList.size - 1) {
                nextItem.tripNo = tripNo
            }
            count++
        }
        count = 0
        var tripCount = 0
        var primaryPosition = 0
        var tripSubDataArrayList = ArrayList<RowItem>()

        // 리스트에 표시될 대표 번호로 구성된 ArrayList 생성
        while (count < tempArrayList.size - 1) {
            val item = tempArrayList[count]
            val nextItem = tempArrayList[count + 1]
            if (item.tripNo == nextItem.tripNo) {
                when {
                    tripCount == 0 -> {
                        primaryPosition = count
                        tripSubDataArrayList = ArrayList()
                        tripSubDataArrayList.add(item)
                    }
                    item.shipping[0] == 'P' -> {
                        primaryPosition = count
                        tripSubDataArrayList.add(0, item)
                    }
                    else -> {
                        tripSubDataArrayList.add(item)
                    }
                }
                tripCount++
            } else if ((item.tripNo != nextItem.tripNo) && tripCount != 0) {

                // 마지막 순서의 Trip Data
                if (item.shipping[0] == 'P') {
                    primaryPosition = count
                    tripSubDataArrayList.add(0, item)
                } else {
                    tripSubDataArrayList.add(item)
                }
                tempArrayList[primaryPosition].isPrimaryKey = true
                tempArrayList[primaryPosition].tripSubDataArrayList = tripSubDataArrayList
                tripArrayList.add(tempArrayList[primaryPosition])
                tripCount = 0
            } else {
                tripArrayList.add(item)
            }

            // 마지막 List 값
            if ((count + 1) == tempArrayList.size - 1) {
                if (item.tripNo == nextItem.tripNo) {
                    if (nextItem.shipping[0] == 'P') {
                        primaryPosition = count + 1
                        tripSubDataArrayList.add(0, nextItem)
                    } else {
                        tripSubDataArrayList.add(nextItem)
                    }
                    tempArrayList[primaryPosition].isPrimaryKey = true
                    tempArrayList[primaryPosition].tripSubDataArrayList = tripSubDataArrayList
                    tripArrayList.add(tempArrayList[primaryPosition])
                } else {
                    tripArrayList.add(nextItem)
                }
            }
            count++
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun diffOfDate(begin: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val beginDate = formatter.parse(begin)
        val endDate = Date()
        val diff = endDate.time - beginDate.time
        return diff / (24 * 60 * 60 * 1000)
    }

    inner class TripMultiComparator : Comparator<RowItem> {
        override fun compare(o1: RowItem, o2: RowItem): Int {
            // 우편번호 > 핸드폰번호 > 송장번호
            return if ((o1.zip_code == o2.zip_code)) {
                if ((o1.items!![0].hp == o2.items!![0].hp)) {
                    o1.shipping.compareTo(o2.shipping)
                } else {
                    o1.items!![0].hp!!.compareTo((o2.items!![0].hp)!!)
                }
            } else {
                o1.zip_code!!.compareTo((o2.zip_code)!!)
            }
        }
    }

    inner class CompareRowItem(private var orderBy: String) :
        Comparator<RowItem> {
        override fun compare(o1: RowItem, o2: RowItem): Int {
            when (orderBy) {
                orderbyQuery[0] -> {
                    return o1.zip_code!!.compareTo((o2.zip_code)!!)
                }
                orderbyQuery[1] -> {
                    return o2.zip_code!!.compareTo((o1.zip_code)!!)
                }
                orderbyQuery[2] -> {
                    return o1.shipping.compareTo(o2.shipping)
                }
                orderbyQuery[3] -> {
                    return o2.shipping.compareTo(o1.shipping)
                }
                orderbyQuery[4] -> {
                    return o1.name.compareTo(o2.name)
                }
                orderbyQuery[5] -> {
                    return o2.name.compareTo(o1.name)
                }
                else -> return o1.zip_code!!.compareTo((o2.zip_code)!!)
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            PermissionChecker.ACCESS_FINE_LOCATION,
            PermissionChecker.ACCESS_COARSE_LOCATION
        )
    }
}