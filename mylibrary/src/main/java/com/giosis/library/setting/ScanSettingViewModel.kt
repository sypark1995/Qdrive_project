package com.giosis.library.setting

import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.util.Preferences

class ScanSettingViewModel : BaseViewModel() {

    private val _vibration = MutableLiveData<String>()
    val vibration: MutableLiveData<String>
        get() = _vibration

    init {
        _vibration.value = Preferences.scanVibration
    }

    fun onClickOn() {
        _vibration.value = "ON"
    }

    fun onClickOff() {
        _vibration.value = "OFF"
    }
}