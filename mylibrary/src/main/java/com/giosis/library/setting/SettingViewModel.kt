package com.giosis.library.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.setting.bluetooth.PrinterSettingActivity
import com.giosis.library.util.Preferences
import com.giosis.library.util.SingleLiveEvent

// TODO_kjyoo : 상단에 연필버튼 누르면 user info 수정으로 이동
class SettingViewModel : BaseViewModel() {

    private val _name = MutableLiveData<String>()
    val name: MutableLiveData<String>
        get() = _name

    private val _id = MutableLiveData<String>()
    val id: MutableLiveData<String>
        get() = _id

    private val _email = MutableLiveData<String>()
    val email: MutableLiveData<String>
        get() = _email

    private val _officeName = MutableLiveData<String>()
    val officeName: MutableLiveData<String>
        get() = _officeName

    private val _visibleChangeLanguage = MutableLiveData<Boolean>()
    val visibleChangeLanguage: MutableLiveData<Boolean>
        get() = _visibleChangeLanguage

    private val _visibleSetLocker = MutableLiveData<Boolean>()
    val visibleSetLocker: MutableLiveData<Boolean>
        get() = _visibleSetLocker

    private val _deleteAlert = SingleLiveEvent<Boolean>()
    val deleteAlert: LiveData<Boolean>
        get() = _deleteAlert

    private val _version = MutableLiveData<String>()
    val version: MutableLiveData<String>
        get() = _version


    init {
        _name.value = Preferences.userName
        _id.value = Preferences.userId
        _email.value = Preferences.userEmail
        _officeName.value = Preferences.officeName

        _visibleChangeLanguage.value = Preferences.userNation != "SG"
        _visibleSetLocker.value = false

        val outletDriverYN = Preferences.outletDriver
        val lockerStatus = Preferences.lockerStatus

        if (outletDriverYN == "Y") {
            if (lockerStatus.contains("no pin")
                    || lockerStatus.contains("active")
                    || lockerStatus.contains("expired")) {
                _visibleSetLocker.value = true
            }
        }

        if (Preferences.userId.equals("karam.kim", ignoreCase = true)) {
            _visibleSetLocker.value = true
        }

        if (Preferences.serverURL.contains("test")) {
            version.value = Preferences.appVersion + "_ test"
        } else if (Preferences.serverURL.contains("staging")) {
            version.value = Preferences.appVersion + " _ staging"
        } else {
            version.value = Preferences.appVersion
        }

    }

    fun changePassword() {
        startActivity(ChangePwdActivity::class.java)
    }

    fun deleteData() {
        _deleteAlert.value = true
    }

    fun goNotiActivity() {
        startActivity(NoticeActivity::class.java)
    }

    fun printerSetting() {
        startActivity(PrinterSettingActivity::class.java)
    }

    fun scanSetting() {
        startActivity(ScanSettingActivity::class.java)
    }

    fun settingLocker() {
        startActivity(LockerUserInfoActivity::class.java)
    }

    fun goDeveloperActivity() {
        startActivity(DeveloperModeActivity::class.java)
    }

    fun languageSetting() {
        startActivity(LanguageSettingActivity::class.java)

    }
}