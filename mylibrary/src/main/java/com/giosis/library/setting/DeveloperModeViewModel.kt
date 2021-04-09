package com.giosis.library.setting

import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.R
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences


class DeveloperModeViewModel : BaseViewModel() {

    private val _serverUrl = MutableLiveData<String>()
    val serverUrl: MutableLiveData<String>
        get() = _serverUrl

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


    private val _xRouteUrl = MutableLiveData<String>()
    val xRouteUrl: MutableLiveData<String>
        get() = _xRouteUrl

    private val _xRouteUrlReal = MutableLiveData(DataUtil.XROUTE_SERVER_REAL)
    val xRouteUrlReal: MutableLiveData<String> = _xRouteUrlReal

    private val _xRouteUrlStaging = MutableLiveData(DataUtil.XROUTE_SERVER_STAGING)
    val xRouteUrlStaging: MutableLiveData<String> = _xRouteUrlStaging

    private val _xRouteCheckedId = MutableLiveData<Int>()
    val xRouteCheckedId: MutableLiveData<Int>
        get() = _xRouteCheckedId


    init {

        _serverUrl.value = Preferences.serverURL
        _xRouteUrl.value = Preferences.xRouteServerURL


        when (Preferences.serverURL) {
            DataUtil.SERVER_REAL -> {

                _checkedId.value = R.id.rb_developer_server_url_real
            }
            DataUtil.SERVER_STAGING -> {

                _checkedId.value = R.id.rb_developer_server_url_staging
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
    }


    fun changeServer(url: String) {

        Preferences.serverURL = url
        _serverUrl.value = url
    }

    fun changeXRouteServer(url: String) {

        Preferences.xRouteServerURL = url
        _xRouteUrl.value = url
    }
}