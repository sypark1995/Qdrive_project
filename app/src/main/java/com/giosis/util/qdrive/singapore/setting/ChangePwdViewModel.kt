package com.giosis.util.qdrive.singapore.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.util.qdrive.singapore.BaseViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.Preferences
import com.giosis.util.qdrive.singapore.util.SingleLiveEvent
import com.giosis.util.qdrive.singapore.util.dialog.DialogUiConfig
import com.giosis.util.qdrive.singapore.util.dialog.DialogViewModel
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
            val id = Preferences.userId
            val appID = DataUtil.appID
            val nationCode = Preferences.userNation

            progressVisible.value = true

            RetrofitClient.instanceDynamic().requestChangePwd(
                id, oldPassword, newPassword, appID, nationCode
            ).subscribeOn(Schedulers.io())
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


    private fun isValidPassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        var isValid = false

        if (oldPassword.isNotEmpty()) {      // ?????? ???????????? ??????

            if (11 <= newPassword.length) {     // ????????? ???????????? 11?????? ?????? ??????

                val passwordPattern = "((?=.*\\d)(?=.*[A-Za-z])(?=.*[!@#$%]).{11,20})"
                val pattern = Pattern.compile(passwordPattern)
                val matcher = pattern.matcher(newPassword)
                val patternValid = matcher.matches()

                if (patternValid) {  // ???????????? ?????????

                    if (newPassword == confirmPassword) {    // ?????? ???????????? ??????
                        isValid = true

                    } else {
                        // ?????? ???????????? ?????????
                        _errorAlert.value = R.string.msg_same_password_error
                    }

                } else {
                    // ???????????? ????????? ??????
                    _errorAlert.value = R.string.msg_password_symbols_error
                }
            } else {
                // ????????? ???????????? 11?????? ?????? ???????????? ??????
                _errorAlert.value = R.string.msg_password_length_error
            }
        } else {
            // ?????? ???????????? ???????????? ??????
            _errorAlert.value = R.string.msg_empty_password_error
        }

        return isValid
    }
}