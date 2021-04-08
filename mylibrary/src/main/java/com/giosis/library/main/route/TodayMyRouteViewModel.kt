package com.giosis.library.main.route

import android.database.Cursor
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.util.DatabaseHelper
import com.giosis.library.util.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// ViewModel에서 Context가 필요한 경우 AndroidViewModel 클래스를 상속받아
// Application 객체를 넘길 것을 권장 !!
// Context를 갖고 있으면 메모리 누수의 원인이 된다

class TodayMyRouteViewModel : BaseViewModel() {

    private val _routeType = MutableLiveData<Int>()
    val routeType: MutableLiveData<Int>
        get() = _routeType

    private val _PACount = MutableLiveData<Int>()
    val PACount: LiveData<Int>
        get() = _PACount

    private val _P2Count = MutableLiveData<Int>()
    val P2Count: LiveData<Int>
        get() = _P2Count

    private val _P3Count = MutableLiveData<Int>()
    val P3Count: LiveData<Int>
        get() = _P3Count

    private val _PFCount = MutableLiveData<Int>()
    val PFCount: LiveData<Int>
        get() = _PFCount

    private val _D3Count = MutableLiveData<Int>()
    val D3Count: LiveData<Int>
        get() = _D3Count

    private val _D4Count = MutableLiveData<Int>()
    val D4Count: LiveData<Int>
        get() = _D4Count

    private val _DFCount = MutableLiveData<Int>()
    val DFCount: LiveData<Int>
        get() = _DFCount


    private val _routeData = MutableLiveData<RouteData>()
    val routeData: MutableLiveData<RouteData>
        get() = _routeData


    init {
        // Pickup / Delivery 각 타입에 맞는 route 우선 보여줌.
        if (Preferences.pickupDriver != "Y") {

            _routeType.value = 1
        }
    }


    fun getCount() {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val selectQuery = (
                        "select   ifnull(sum(case when stat='P1' or stat='PA' or stat='RE' then 1 else 0 end), 0) as pa_count," +
                                "ifnull(sum(case when stat='P2' then 1 else 0 end) ,0) as p2_count, " +
                                "ifnull(sum(case when stat='P3' then 1 else 0 end), 0) as p3_count, " +
                                "ifnull(sum(case when stat='PF' then 1 else 0 end), 0) as pf_count, " +
                                "ifnull(sum(case when stat='D3' then 1 else 0 end), 0) as d3_count, " +
                                "ifnull(sum(case when stat='D4' then 1 else 0 end), 0) as d4_count, " +
                                "ifnull(sum(case when stat='DX' then 1 else 0 end), 0) as df_count " +
                                "from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " where reg_id='" + Preferences.userId + "'")

                val cs: Cursor = DatabaseHelper.getInstance()[selectQuery]

                if (cs.moveToFirst()) {

                    if (_routeType.value == 0) {

                        _PACount.postValue(cs.getInt(cs.getColumnIndex("pa_count")))
                        _P2Count.postValue(cs.getInt(cs.getColumnIndex("p2_count")))
                        _P3Count.postValue(cs.getInt(cs.getColumnIndex("p3_count")))
                        _PFCount.postValue(cs.getInt(cs.getColumnIndex("pf_count")))
                    } else {

                        _D3Count.postValue(cs.getInt(cs.getColumnIndex("d3_count")))
                        _D4Count.postValue(cs.getInt(cs.getColumnIndex("d4_count")))
                        _DFCount.postValue(cs.getInt(cs.getColumnIndex("df_count")))
                    }
                }
            } catch (e: Exception) {

                Log.e("Exception", "getCount() Exception : $e")
            }
        }
    }


    // route 받을 API 호출
    fun clickRun() {

        //  _routeData.value =
    }

    /*
    * fun getInvoice() {

        val invoice_list: ArrayList<String> = ArrayList(0)

        val query: String =
            String.format(IntegrationTable.INTEGRATION_LIST.SELECT_INVOICE_MY_ROUTE)
        val result: DbResult = DataBaseManager.getInstance().getExecuteQueryResult(query)
        if (result.isSucceed) {
            if (result.data.row.isEmpty()) {
                mListItem.clear()
                showNoJobOrder(getString(R.string.text_no_job_order))
            } else if ((0.0 == LocationService.mLatitude)
                || (0.0 == LocationService.mLongitude)
            ) {
                showNoJobOrder(getString(R.string.msg_location_info_checking))
            } else {

                for (row in result.data.row) {
                    if (isCheck) {
                        invoice_list.add(row[IntegrationTable.COLUMN.INVOICE_NO]!!.s)
                    } else {
                        val stat = row[IntegrationTable.COLUMN.STAT]!!.s
                        if (stat != "DX") {
                            invoice_list.add(row[IntegrationTable.COLUMN.INVOICE_NO]!!.s)
                        }
                    }
                }

                val invoice = invoice_list.toString().replace("[", "")
                    .replace("]", "")
                    .replace(" ", "")

                var lat: String = LocationService.mLatitude.toString()
                var lng: String = LocationService.mLongitude.toString()

                if (BuildConfig.DEBUG) {
                    lat = "1.353095"
                    lng = "103.942726"
                }

                wait_login_api(true)
                RetrofitClient.instanceXRoute().requestGetTripList(lat, lng, invoice)
                    .enqueue(object : Callback<APIModel> {
                        override fun onFailure(call: Call<APIModel>, t: Throwable) {
                            setRunStat(true)
                            wait_login_api(false)
                        }

                        @SuppressLint("SimpleDateFormat")
                        override fun onResponse(
                            call: Call<APIModel>,
                            response: Response<APIModel>,
                        ) {
                            try {
                                if (response.isSuccessful && response.body() != null) {
                                    if (response.body()!!.errCode == "0000") {
                                        val trapData = Gson().fromJson(
                                            response.body()!!.data,
                                            GetTripData::class.java
                                        )

                                        val totalTimeMin = (trapData.duration.toDouble() / 60).toInt()

                                        text_my_route_info_hour.text = totalTimeMin.div(60).toString()
                                        text_my_route_info_min.text = String.format("%02d", totalTimeMin.rem(60))
                                        text_my_route_info_distance.text = String.format("%.1f", trapData.distance.toDouble() / 1000)
                                        text_my_route_info_trip.text = trapData.tripCnt

                                        if (trapData.tripItems.isEmpty()) {
                                            showNoJobOrder(getString(R.string.text_no_job_order))
                                        } else {

                                            val cal = Calendar.getInstance()
                                            for ((index, trip) in trapData.tripItems.withIndex()) {

                                                if (index < trapData.maps.size) {
                                                    trip.map = trapData.maps[index]
                                                }

                                                val timeMin: Int =
                                                    (trip.nextTripDuration.toDouble() / 60).toInt()

                                                if (0 == index) {
                                                    cal.time = Date()
                                                } else {
                                                    cal.time =
                                                        SimpleDateFormat("yyyy-MM-dd HH:mm").parse(
                                                            trapData.tripItems[index - 1].nextTripDate
                                                        )!!
                                                }
                                                cal.add(Calendar.MINUTE, timeMin)

                                                if (index == trapData.tripItems.size - 1) {

                                                    trip.nextTripDate = ""
                                                    trip.nextTripTime = ""
                                                    trip.nextTripDistance = ""

                                                } else {

                                                    trip.nextTripDate =
                                                        SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                            .format(cal.time)
                                                    trip.nextTripTime =
                                                        SimpleDateFormat("HH:mm").format(cal.time)
                                                    trip.nextTripDistance = String.format(
                                                        "%.1f",
                                                        trip.nextTripDistance.toDouble() / 1000
                                                    )
                                                }

                                                trip.orderType = getOrderType(trip.orderType)

                                                if (index == 0) {
                                                    trip.estimatedTime = nowTime()
                                                } else {
                                                    trip.estimatedTime =
                                                        trapData.tripItems[index - 1].nextTripTime
                                                }

                                            }

                                            layout_my_route_info_summary.visibility = View.VISIBLE
                                            list_my_route_job.visibility = View.VISIBLE

                                            mListItem.clear()
                                            mListItem.addAll(trapData.tripItems)

                                            listAdapter.notifyDataSetChanged()
                                        }
                                    } else {
                                        QUtil.showAlert(
                                            this@TodayMyRouteActivity,
                                            R.string.alert_title_information,
                                            "Today's my route failed.\n${response.body()!!.desc}"
                                        )
                                    }
                                }
                            } catch (e: Exception) {

                            }

                            setRunStat(true)
                            wait_login_api(false)

                        }
                    })
            }
        }
    }
    * */

    fun onClickItem(item: Route) {

        // TODO     start navigation Button
        Log.e("Route", "HERE onClickItem")
    }
}