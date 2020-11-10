package com.giosis.library.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel

class ChangePwdViewModel :BaseViewModel(){

    private val _isLightTheme = MutableLiveData<Boolean>()
    val isLightTheme: LiveData<Boolean> = _isLightTheme

    fun onclick(){

    }
}