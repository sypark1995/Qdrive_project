package com.giosis.library.list

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
import androidx.fragment.app.Fragment
import com.giosis.library.R
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.main.PickupAssignResult
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class ListTodayDoneFragment2(var bluetoothListener: BluetoothListener) : Fragment(),
    SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    var TAG = "ListTodayDoneFragment"
    private var searchViewList: SearchView? = null
    private lateinit var editListSearchView: EditText
    private var layoutListSort: FrameLayout? = null
    private var exlistCardList: ExpandableListView? = null
    private var mCountCallback: OnTodayDoneCountListener? = null
    private var rowItems = ArrayList<RowItem>()
    private var childItems = ArrayList<ChildItem>()
    private lateinit var adapter: ListTodayDoneAdapter2

    //리스트 카운트를 갱신하기 위한 인터페이스
    interface OnTodayDoneCountListener {
        fun onTodayDoneCountRefresh(count: Int)
    }

    //부모 Activity와 통신을 하기 위한 연결
    override fun onAttach(context: Context) {
        super.onAttach(context)
        var activity: Activity? = null
        if (context is Activity) {
            activity = context
        }
        mCountCallback = try {
            activity as OnTodayDoneCountListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnCountListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inprogress, container, false)
        searchViewList = view.findViewById(R.id.searchview_list)
        layoutListSort = view.findViewById(R.id.layout_list_sort)
        exlistCardList = view.findViewById(R.id.exlist_card_list)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Search
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchViewList!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchViewList!!.queryHint = resources.getString(R.string.text_search)
        searchViewList!!.setOnQueryTextListener(this)
        searchViewList!!.setOnCloseListener(this)
        val id = searchViewList!!.context.resources.getIdentifier(
            "android:id/search_src_text",
            null,
            null
        )
        editListSearchView = searchViewList!!.findViewById(id)
        editListSearchView.setTextColor(Color.parseColor("#8F8F8F"))
        editListSearchView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_26px)
        )
        editListSearchView.setHintTextColor(Color.parseColor("#8F8F8F"))
        layoutListSort!!.visibility = View.GONE
        exlistCardList!!.setOnGroupExpandListener { groupPosition: Int ->
            val groupCount = adapter.groupCount

            // 한 그룹을 클릭하면 나머지 그룹들은 닫힌다.
            for (i in 0 until groupCount) {
                if (i != groupPosition) exlistCardList!!.collapseGroup(i)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editListSearchView.windowToken, 0)
            editListSearchView.setText("")
            editListSearchView.clearFocus()
        } catch (e: Exception) {
            Log.e("Exception", "search init  Exception : $e")
        }
        try {
            RetrofitClient.instanceDynamic().requestGetTodayPickupDoneList(
                Preferences.userId, "", "",
                DataUtil.appID, Preferences.userNation
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.e(
                        "Server",
                        " TodayDone requestGetTodayPickupDoneList  result  " + it.resultCode
                    )
                    if (it.resultCode == 0) {
                        val list =
                            Gson().fromJson<ArrayList<PickupAssignResult.QSignPickupList>>(
                                it.resultObject,
                                object :
                                    TypeToken<ArrayList<PickupAssignResult.QSignPickupList?>?>() {}.type
                            )
                        if (isAdded) {
                            rowItems = ArrayList()
                            for (pickupInfo in list) {
                                childItems = ArrayList()
                                val child = ChildItem()
                                child.hp = pickupInfo.hpNo
                                child.tel = pickupInfo.telNo
                                child.stat = pickupInfo.stat
                                child.statMsg = pickupInfo.driverMemo
                                child.statReason = pickupInfo.failReason
                                child.secretNoType = pickupInfo.secretNoType
                                child.secretNo = pickupInfo.secretNo
                                childItems.add(child)
                                val rowitem = RowItem(
                                    pickupInfo.contrNo,
                                    "D+0",
                                    pickupInfo.invoiceNo,
                                    pickupInfo.reqName,
                                    "(" + pickupInfo.zipCode + ") " + pickupInfo.address,
                                    pickupInfo.delMemo,
                                    "P",
                                    pickupInfo.route,
                                    "",  //cs.getString(cs.getColumnIndex("sender_nm")),
                                    pickupInfo.pickupHopeDay,
                                    pickupInfo.qty,
                                    "",  // cs.getString(cs.getColumnIndex("self_memo")),
                                    0.0,  // cs.getDouble(cs.getColumnIndex("lat")),
                                    0.0,  //cs.getDouble(cs.getColumnIndex("lng")),
                                    pickupInfo.stat,  //cs.getString(cs.getColumnIndex("stat")),
                                    pickupInfo.custNo,  //cs.getString(cs.getColumnIndex("cust_no")),
                                    pickupInfo.partnerID,  //cs.getString(cs.getColumnIndex("partner_id"))
                                    "",
                                    "",
                                    "", ""
                                )
                                rowitem.items = childItems
                                rowItems.add(rowitem)
                            }
                            adapter = ListTodayDoneAdapter2(rowItems, bluetoothListener)
                            exlistCardList!!.setAdapter(adapter)
                            adapter.setSorting(rowItems)
                            val groupCount = adapter.groupCount
                            for (i in 0 until groupCount) {
                                exlistCardList!!.collapseGroup(i)
                            }

                            //카운트 전달
                            mCountCallback!!.onTodayDoneCountRefresh(groupCount)
                        }
                    }
                }) {
                    if (activity != null && isAdded) {
                        Log.e(RetrofitClient.errorTag, "$TAG - $it")
                        Toast.makeText(
                            activity,
                            getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "  onActivityResult > $resultCode / $requestCode")
        if (requestCode == REQUEST_ADD_SCAN || requestCode == REQUEST_TAKE_BACK) {
            onResume()
        }
    }

    override fun onClose(): Boolean {
        adapter.filterData("")
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        try {
            adapter.filterData(query)
        } catch (e: Exception) {
            Log.e("Exception", "$TAG  onQueryTextSubmit  Exception : $e")
        }
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

    override fun onPause() {
        super.onPause()
        bluetoothListener.clearBluetoothAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothListener.clearBluetoothAdapter()
    }

    companion object {
        const val REQUEST_ADD_SCAN = 30
        const val REQUEST_TAKE_BACK = 31
    }
}