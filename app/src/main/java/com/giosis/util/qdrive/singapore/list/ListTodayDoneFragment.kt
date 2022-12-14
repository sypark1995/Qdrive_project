package com.giosis.util.qdrive.singapore.list

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothListener
import com.giosis.util.qdrive.singapore.main.PickupAssignResult
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.Preferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class ListTodayDoneFragment(var bluetoothListener: BluetoothListener) : Fragment(),
    SearchView.OnQueryTextListener, SearchView.OnCloseListener,
    ListTodayDoneAdapter.OnItemClickListener {

    var TAG = "ListTodayDoneFragment"
    private var searchViewList: SearchView? = null
    private lateinit var editListSearchView: EditText
    private var layoutListSort: FrameLayout? = null
    private var exlistCardList: RecyclerView? = null
    private var mCountCallback: OnTodayDoneCountListener? = null
    private var rowItems = ArrayList<RowItem>()
    private lateinit var adapter: ListTodayDoneAdapter

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
        searchViewList = view.findViewById(R.id.search_view)
        layoutListSort = view.findViewById(R.id.layout_list_sort)
        exlistCardList = view.findViewById(R.id.exlist_card_list)
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
        editListSearchView = searchViewList!!.findViewById(id)
        editListSearchView.setTextColor(Color.parseColor("#8F8F8F"))
        editListSearchView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_26px)
        )
        editListSearchView.setHintTextColor(Color.parseColor("#8F8F8F"))
        layoutListSort!!.visibility = View.GONE
        adapter = ListTodayDoneAdapter(bluetoothListener, this)
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
                                val child = ChildItem()
                                child.hp = pickupInfo.hpNo
                                child.tel = pickupInfo.telNo
                                child.stat = pickupInfo.stat
                                child.statMsg = pickupInfo.driverMemo
                                child.statReason = pickupInfo.failReason
                                child.secretNoType = pickupInfo.secretNoType
                                child.secretNo = pickupInfo.secretNo

                                val rowItem = RowItem(
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

                                rowItem.childItems = child
                                rowItems.add(rowItem)
                            }

                            adapter.rowItem = rowItems
                            exlistCardList!!.adapter = adapter

                            adapter.setSorting(rowItems)
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

    private val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
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

    override fun addScanClicked(data: RowItem) {
        val intent = Intent(
            activity,
            TodayDonePickupScanListActivity::class.java
        )
        intent.putExtra("pickup_no", data.shipping)
        intent.putExtra("applicant", data.name)
        intent.putExtra("button_type", "Add Scan")
        resultLauncher.launch(intent)
    }

    override fun takeBackClicked(data: RowItem) {
        val intent = Intent(
            activity,
            TodayDonePickupScanListActivity::class.java
        )
        intent.putExtra("pickup_no", data.shipping)
        intent.putExtra("applicant", data.name)
        intent.putExtra("button_type", "Take Back")
        resultLauncher.launch(intent)
    }

    override fun itemMenuIconClicked(view: View, data: RowItem) {
        val popup =
            PopupMenu(activity, view)
        popup.menuInflater.inflate(R.menu.quickmenu_pickup, popup.menu)
        popup.show()
        popup.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.menu_one) {
                val mapAddress = data.address
                val splitIndex = mapAddress.indexOf(")")
                val splitAddress = mapAddress.substring(splitIndex + 1)
                if (splitAddress != "") {
                    val uri =
                        Uri.parse("http://maps.google.co.in/maps?q=" + splitAddress.trim { it <= ' ' })
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        }
    }
}