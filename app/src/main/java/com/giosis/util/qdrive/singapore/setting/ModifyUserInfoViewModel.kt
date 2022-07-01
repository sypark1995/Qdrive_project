package com.giosis.util.qdrive.singapore.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.util.qdrive.singapore.BaseViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.Preferences
import com.giosis.util.qdrive.singapore.util.SingleLiveEvent
import com.giosis.util.qdrive.singapore.util.dialog.DialogUiConfig
import com.giosis.util.qdrive.singapore.util.dialog.DialogViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.regex.Matcher
import java.util.regex.Pattern


class ModifyUserInfoViewModel : BaseViewModel() {

    private val _driverId = MutableLiveData<String>()
    val driverId: MutableLiveData<String>
        get() = _driverId

    private val _name = MutableLiveData<String>()
    val name: MutableLiveData<String>
        get() = _name

    private val _email = MutableLiveData<String>()
    val email: MutableLiveData<String>
        get() = _email

    private val _checkAlert = MutableLiveData<Pair<DialogUiConfig, DialogViewModel>>()
    val checkAlert: LiveData<Pair<DialogUiConfig, DialogViewModel>>
        get() = _checkAlert

    private val _errorAlert = SingleLiveEvent<Int>()
    val errorAlert: LiveData<Int>
        get() = _errorAlert

    private val _resultAlert = SingleLiveEvent<Any>()
    val resultAlert: LiveData<Any>
        get() = _resultAlert

    init {
        _driverId.value = Preferences.userId
        _name.value = Preferences.userName
        _email.value = Preferences.userEmail
    }

    fun onClickConfirm() {

        val text = DialogUiConfig(
            title = R.string.text_modify_my_info,
            message = R.string.msg_want_change_info
        )

        val listener = DialogViewModel(
            positiveClick = {
                modifyUserInfo()
                _checkAlert.value = null
            },
            negativeClick = {
                _checkAlert.value = null
            }
        )

        _checkAlert.value = Pair(text, listener)
    }


    private fun modifyUserInfo() {

        val name = _name.value.toString().trim()
        val email = _email.value.toString().trim()

        val isValid = isValidData(name, email)

        if (isValid) {

            progressVisible.value = true

            RetrofitClient.instanceDynamic().requestChangeMyInfo(name, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    progressVisible.value = false
                    Log.e(RetrofitClient.TAG, "${it.resultCode} / ${it.resultMsg}")

                    if (it.resultCode == 0) {

                        Preferences.userName = name
                        Preferences.userEmail = email
                    }

                    _resultAlert.value = it
                }, {

                    progressVisible.value = false
                    _errorAlert.value = R.string.msg_network_connect_error
                })
        }
    }


    private fun isValidData(name: String, email: String): Boolean {

        val isValid: Boolean

        if (name.trim().length >= 6) {
            if (email.isNotEmpty()) {

                val emailPattern =
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
                val pattern: Pattern = Pattern.compile(emailPattern)
                val matcher: Matcher = pattern.matcher(email)
                val isEmail: Boolean = matcher.matches()

                if (isEmail) {

                    isValid = true
                } else {

                    isValid = false
                    _errorAlert.value = R.string.msg_email_format_error
                }
            } else {
                isValid = true
            }
        } else {

            isValid = false
            _errorAlert.value = R.string.msg_full_name_info
        }

        return isValid
    }
}