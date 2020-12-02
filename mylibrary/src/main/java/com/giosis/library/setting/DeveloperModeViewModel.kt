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


    init {

        _serverUrl.value = Preferences.serverURL

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
    }


    fun changeServer(url: String) {

        Preferences.serverURL = url
        _serverUrl.value = url
    }
}