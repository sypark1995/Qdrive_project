package com.giosis.library.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import com.giosis.library.util.SingleLiveEvent
import com.giosis.library.util.dialog.DialogUiConfig
import com.giosis.library.util.dialog.DialogViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.regex.Pattern

class ChangePwdViewModel : BaseViewModel() {

    private val _oldPwd = MutableLiveData<String>()
    val oldPwd: MutableLiveData<String>
        get() = _oldPwd

    private val _newPwd = MutableLiveData<String>()
    val newPwd: MutableLiveData<String>
        get() = _newPwd

    private val _confirmPwd = MutableLiveData<String>()
    val confirmPwd: MutableLiveData<String>
        get() = _confirmPwd


    private val _checkAlert = MutableLiveData<Pair<DialogUiConfig, DialogViewModel>>()
    val checkAlert: LiveData<Pair<DialogUiConfig, DialogViewModel>>
        get() = _checkAlert

    private val _errorAlert = SingleLiveEvent<Int>()
    val errorAlert: LiveData<Int>
        get() = _errorAlert

    private val _resultAlert = SingleLiveEvent<Any>()
    val resultAlert: LiveData<Any>
        get() = _resultAlert


    fun onClickConfirm() {
        val text = DialogUiConfig(
                title = R.string.text_title_change_password,
                message = R.string.msg_want_change_password
        )

        val listener = DialogViewModel(
                positiveClick = {

                    alertOkClick()
                    _checkAlert.value = null
                },
                negativeClick = {
                    _checkAlert.value = null
                }
        )

        _checkAlert.value = Pair(text, listener)
    }


    private fun alertOkClick() {

        val oldPassword = _oldPwd.value.toString().trim()
        val newPassword = _newPwd.value.toString().trim()
        val confirmPassword = _confirmPwd.value.toString().trim()

        val isValid = isValidPassword(oldPassword, newPassword, confirmPassword)

        if (isValid) {
            // TODO kjyoo
            val id = Preferences.userId
            val appID = DataUtil.appID
            val nationCode = Preferences.userNation

            progressVisible.value = true

            RetrofitClient.instanceDynamic().requestChangePwd(
                    id, oldPassword, newPassword, appID, nationCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        progressVisible.value = false
                        Log.e(RetrofitClient.TAG, "${it.resultCode} / ${it.resultMsg}")

                        if (it.resultCode == 0) {

                            Preferences.userPw = newPassword
                        }

                        _resultAlert.value = it
                    }, {

                        progressVisible.value = false
                        _errorAlert.value = R.string.msg_network_connect_error
                    })
        }
    }


    private fun isValidPassword(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        var isValid = false

        if (oldPassword.isNotEmpty()) {      // 현재 패스워드 입력

            if (11 <= newPassword.length) {     // 새로운 패스워드 11자리 이상 입력

                val passwordPattern = "((?=.*\\d)(?=.*[A-Za-z])(?=.*[!@#$%]).{11,20})"
                val pattern = Pattern.compile(passwordPattern)
                val matcher = pattern.matcher(newPassword)
                val patternValid = matcher.matches()

                if (patternValid) {  // 비밀번호 유효성

                    if (newPassword == confirmPassword) {    // 확인 비밀번호 일치
                        isValid = true

                    } else {
                        // 확인 비밀번호 불일치
                        _errorAlert.value = R.string.msg_same_password_error
                    }

                } else {
                    // 비밀번호 유효성 틀림
                    _errorAlert.value = R.string.msg_password_symbols_error
                }
            } else {
                // 새로운 패스워드 11자리 이상 입력하지 않음
                _errorAlert.value = R.string.msg_password_length_error
            }
        } else {
            // 현재 패스워드 입력하지 않음
            _errorAlert.value = R.string.msg_empty_password_error
        }

        return isValid
    }
}