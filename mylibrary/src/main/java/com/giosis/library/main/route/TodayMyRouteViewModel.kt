package com.giosis.library.main.route

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BuildConfig
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.Event
import com.giosis.library.util.Preferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


// ViewModel에서 Context가 필요한 경우 AndroidViewModel 클래스를 상속받아
// Application 객체를 넘길 것을 권장 !!
// Context를 갖고 있으면 메모리 누수의 원인이 된다
class TodayMyRouteViewModel(application: Application) : AndroidViewModel(application) {

    // Location
    private val gpsTrackerManager = GPSTrackerManager(application)
    fun getGpsManager() = gpsTrackerManager

    private val _permissionCheck = MutableLiveData<Boolean>()
    val permissionCheck: MutableLiveData<Boolean>
        get() = _permissionCheck

    private val _latitude = MutableLiveData<Double>()
    val latitude: MutableLiveData<Double>
        get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: MutableLiveData<Double>
        get() = _longitude

    //
    private val _routeType = MutableLiveData<String>()
    val routeType: MutableLiveData<String>
        get() = _routeType

    private val _spinnerPosition = MutableLiveData<Int>()
    val spinnerPosition: MutableLiveData<Int>
        get() = _spinnerPosition

    private val _PACount = MutableLiveData<String>()
    val PACount: LiveData<String>
        get() = _PACount

    private val _P2Count = MutableLiveData<String>()
    val P2Count: LiveData<String>
        get() = _P2Count

    private val _P3Count = MutableLiveData<String>()
    val P3Count: LiveData<String>
        get() = _P3Count

    private val _PFCount = MutableLiveData<String>()
    val PFCount: LiveData<String>
        get() = _PFCount

    private val _D3Count = MutableLiveData<String>()
    val D3Count: LiveData<String>
        get() = _D3Count

    private val _D4Count = MutableLiveData<String>()
    val D4Count: LiveData<String>
        get() = _D4Count

    private val _DFCount = MutableLiveData<String>()
    val DFCount: LiveData<String>
        get() = _DFCount

    private val _trackingList = MutableLiveData<List<String>>()
    val trackingList: MutableLiveData<List<String>>
        get() = _trackingList

    //
    private val _routeData = MutableLiveData<RouteData>()
    val routeData: MutableLiveData<RouteData>
        get() = _routeData

    private val _progress = MutableLiveData<Int>()
    val progress: MutableLiveData<Int>
        get() = _progress

    private val _resultVisible = MutableLiveData<Int>()
    val resultVisible: MutableLiveData<Int>
        get() = _resultVisible

    private val _showToast = MutableLiveData<Event<String>>()
    val showToast: MutableLiveData<Event<String>>
        get() = _showToast

    //
    private val _googleMap = MutableLiveData<String>()
    val googleMap: MutableLiveData<String>
        get() = _googleMap

    init {
        // Pickup / Delivery 각 타입에 맞는 route 우선 보여줌.
        if (Preferences.pickupDriver == "Y") {

            _spinnerPosition.value = 0
            _routeType.value = "P"
        } else {

            _spinnerPosition.value = 1
            _routeType.value = "D"
        }

        _progress.value = View.GONE
        _resultVisible.value = View.GONE
    }


    fun getCount(position: Int) {

        _progress.value = View.VISIBLE
        _resultVisible.value = View.GONE

        if (position == 0) {

            _routeType.value = "P"
            RetrofitClient.instanceDynamic().requestGetMyPickupRoute()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (it.resultCode == 0) {

                            val result = Gson().fromJson<List<TrackingModel>>(
                                    it.resultObject, object : TypeToken<List<TrackingModel>>() {}.type
                            )


                            val list = ArrayList<String>()
                            var assignedCount = 0
                            var confirmedCount = 0
                            var doneCount = 0
                            var failedCount = 0

                            for (data in result) {

                                when (data.stat) {
                                    "P3" -> {
                                        doneCount++
                                    }
                                    "PF" -> {
                                        failedCount++
                                    }
                                    "P2" -> {
                                        confirmedCount++
                                        list.add(data.trackingNo)
                                    }
                                    else -> {
                                        assignedCount++
                                        list.add(data.trackingNo)
                                    }
                                }
                            }

                            Log.e("route", "Count $confirmedCount / $doneCount / $failedCount")
                            _PACount.postValue(assignedCount.toString())
                            _P2Count.postValue(confirmedCount.toString())
                            _P3Count.postValue(doneCount.toString())
                            _PFCount.postValue(failedCount.toString())
                            _trackingList.postValue(list)
                        } else {

                            showToast.value = Event(it.resultMsg)
                        }

                        _progress.value = View.GONE
                    }, {

                        _progress.value = View.GONE
                        _resultVisible.value = View.GONE
                        showToast.value = Event(it.message)
                    })
        } else {

            _routeType.value = "D"
            RetrofitClient.instanceDynamic().requestGetMyDeliveryRoute()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (it.resultCode == 0) {

                            val result = Gson().fromJson<List<TrackingModel>>(
                                    it.resultObject, object : TypeToken<List<TrackingModel>>() {}.type
                            )


                            val list = ArrayList<String>()
                            var progressCount = 0
                            var doneCount = 0
                            var failedCount = 0

                            for (data in result) {

                                when (data.stat) {
                                    "D4" -> {
                                        doneCount++
                                    }
                                    "DX" -> {
                                        failedCount++
                                    }
                                    else -> {
                                        progressCount++
                                        list.add(data.trackingNo)
                                    }
                                }
                            }

                            Log.e("route", "Count $progressCount / $doneCount / $failedCount")
                            _D3Count.postValue(progressCount.toString())
                            _D4Count.postValue(doneCount.toString())
                            _DFCount.postValue(failedCount.toString())
                            _trackingList.postValue(list)
                        } else {

                            showToast.value = Event(it.resultMsg)
                        }

                        _progress.value = View.GONE
                    }, {

                        _progress.value = View.GONE
                        _resultVisible.value = View.GONE
                        showToast.value = Event(it.message)
                    })
        }
    }


    // route 받을 API 호출
    @SuppressLint("SimpleDateFormat")
    fun clickRun() {

        _progress.value = View.VISIBLE

        var lat = gpsTrackerManager.latitude.toString()
        var lng = gpsTrackerManager.longitude.toString()
        Log.e("route", "Lat_Lng   $lat / $lng")

        if (BuildConfig.DEBUG) {
            lat = "1.353095"
            lng = "103.942726"
        }

        val list = _trackingList.value.toString()
                .replace("[", "")
                .replace("]", "")
        Log.e("route", "$lat - $lng / (${_trackingList.value?.size})$list / ${_routeType.value}")

        RetrofitClient.instanceXRoute().requestGetTripList(lat, lng, list, _routeType.value!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (it.errCode == "0000") {      // 성공

                        // TODO_route
                        Log.e("route", " requestGetTripList result ${it.errCode} / ${it.data} ")

                        if (it.data != null) {

                            val routeData = Gson().fromJson(it.data, RouteData::class.java)

                            val calendar = Calendar.getInstance()
                            for ((index, route) in routeData.routeList.withIndex()) {

                                val timeMin: Int = (route.next_trip_duration.toDouble() / 60).toInt()

                                if (0 == index) {
                                    calendar.time = Date()
                                } else {
                                    calendar.time = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(routeData.routeList[index - 1].nextTripDate)!!
                                }
                                calendar.add(Calendar.MINUTE, timeMin)

                                if (index == routeData.routeList.size - 1) {

                                    route.nextTripDate = ""
                                    route.nextTripTime = ""
                                } else {

                                    route.nextTripDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.time)
                                    route.nextTripTime = SimpleDateFormat("HH:mm").format(calendar.time)
                                }

                                if (index == 0) {
                                    route.estimatedTime = SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)
                                } else {
                                    route.estimatedTime = routeData.routeList[index - 1].nextTripTime
                                }
                            }

                            _routeData.postValue(routeData)
                            _resultVisible.value = View.VISIBLE
                        } else {

                            showToast.value = Event(it.desc)
                            _resultVisible.value = View.GONE
                        }
                    } else {    // 실패

                        showToast.value = Event(it.desc)
                        _resultVisible.value = View.GONE
                    }

                    _progress.value = View.GONE
                }, {

                    _progress.value = View.GONE
                    _resultVisible.value = View.GONE
                    showToast.value = Event(it.message)
                })
    }

    fun onClickItem() {

        // TODO     start navigation Button
        _googleMap.value = routeData.value?.maps?.get(0)
        Log.e("Route", "HERE onClickItem   ${_googleMap.value}")
    }


    fun setPermission(check: Boolean) {

        _permissionCheck.value = check
    }
}
