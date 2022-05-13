package com.giosis.util.qdrive.singapore.setting

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.giosis.util.qdrive.singapore.BaseViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.Preferences


class DeveloperModeViewModel : BaseViewModel() {

    // Server
    private val _inputUrl = MutableLiveData<String>()
    val inputUrl: MutableLiveData<String>
        get() = _inputUrl

    private val _urlReal = MutableLiveData(DataUtil.SERVER_REAL)
    val urlReal: MutableLiveData<String> = _urlReal

    private val _urlStaging = MutableLiveData(DataUtil.SERVER_STAGING)
    val urlStaging: MutableLiveData<String> = _urlStaging

    private val _urlTest = MutableLiveData(DataUtil.SERVER_TEST)
    val urlTest: MutableLiveData<String> = _urlTest

    private val _urlLocal = MutableLiveData(DataUtil.SERVER_LOCAL)
    val urlLocal: MutableLiveData<String> = _urlLocal

    private val _checkedId = MutableLiveData<Int>()
    val checkedId: MutableLiveData<Int>
        get() = _checkedId

    // xRoute Server
    private val _xRouteUrlReal = MutableLiveData(DataUtil.XROUTE_SERVER_REAL)
    val xRouteUrlReal: MutableLiveData<String> = _xRouteUrlReal

    private val _xRouteUrlStaging = MutableLiveData(DataUtil.XROUTE_SERVER_STAGING)
    val xRouteUrlStaging: MutableLiveData<String> = _xRouteUrlStaging

    private val _xRouteCheckedId = MutableLiveData<Int>()
    val xRouteCheckedId: MutableLiveData<Int>
        get() = _xRouteCheckedId

    // GPS
    private val _gpsVisible = MutableLiveData<Int>()
    val gpsVisible: MutableLiveData<Int>
        get() = _gpsVisible

    private val _gpsCheckedId = MutableLiveData<Int>()
    val gpsCheckedId: MutableLiveData<Int>
        get() = _gpsCheckedId

    private val _gpsInputVisible = MutableLiveData<Int>()
    val gpsInputVisible: MutableLiveData<Int>
        get() = _gpsInputVisible

    private val _gpsLatitude = MutableLiveData<String>()
    val gpsLatitude: MutableLiveData<String>
        get() = _gpsLatitude

    private val _gpsLongitude = MutableLiveData<String>()
    val gpsLongitude: MutableLiveData<String>
        get() = _gpsLongitude


    init {

        _gpsVisible.value = View.GONE
        _gpsInputVisible.value = View.GONE

        _inputUrl.value = Preferences.serverURL
        when (Preferences.serverURL) {
            DataUtil.SERVER_REAL -> {

                _checkedId.value = R.id.rb_developer_server_url_real
            }
            DataUtil.SERVER_STAGING -> {

                _checkedId.value = R.id.rb_developer_server_url_staging

                if (Preferences.userNation != "SG")
                    _gpsVisible.value = View.VISIBLE
            }
            DataUtil.SERVER_TEST -> {

                _checkedId.value = R.id.rb_developer_server_url_test
            }
            DataUtil.SERVER_LOCAL -> {

                _checkedId.value = R.id.rb_developer_server_url_local
            }
        }


        when (Preferences.xRouteServerURL) {
            DataUtil.XROUTE_SERVER_REAL -> {

                _xRouteCheckedId.value = R.id.rb_developer_xroute_url_real
            }
            DataUtil.XROUTE_SERVER_STAGING -> {

                _xRouteCheckedId.value = R.id.rb_developer_xroute_url_staging
            }
        }

        when (Preferences.gpsMode) {
            "REAL" -> {

                _gpsCheckedId.value = R.id.rb_developer_gps_real
            }
            "TEST" -> {

                _gpsCheckedId.value = R.id.rb_developer_gps_test
                _gpsInputVisible.value = View.VISIBLE
                setGpsValue()
            }
        }
    }


    fun changeServer(url: String) {

        Preferences.serverURL = url
        _inputUrl.value = url

        if (url == DataUtil.SERVER_STAGING && Preferences.userNation != "SG") {

            _gpsVisible.value = View.VISIBLE
        } else {

            _gpsVisible.value = View.GONE
        }
    }

    fun changeXRouteServer(url: String) {

        Preferences.xRouteServerURL = url
    }

    fun changeGPS(str: String) {

        Preferences.gpsMode = str

        if (str == "REAL") {

            _gpsInputVisible.value = View.GONE
            Preferences.gpsTestValue = "0, 0"
        } else {

            _gpsInputVisible.value = View.VISIBLE
            setGpsValue()
        }
    }

    private fun setGpsValue() {

        val gpsValue = Preferences.gpsTestValue.split(",")
        _gpsLatitude.value = gpsValue[0]
        _gpsLongitude.value = gpsValue[1]
    }

    fun gpsValueSave() {

        Log.e("GPS", "Latitude ${_gpsLatitude.value}, ${_gpsLongitude.value}")
        Preferences.gpsTestValue = "${_gpsLatitude.value}, ${_gpsLongitude.value}"
    }
}