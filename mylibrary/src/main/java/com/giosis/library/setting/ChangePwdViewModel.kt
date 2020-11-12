package com.giosis.library.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import com.giosis.library.util.SingleLiveEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.regex.Pattern

class ChangePwdViewModel : BaseViewModel() {

    private val _confirmPwd = MutableLiveData<String>()
    val confirmPwd: MutableLiveData<String>
        get() = _confirmPwd

    private val _oldPwd = MutableLiveData<String>()
    val oldPwd: MutableLiveData<String>
        get() = _oldPwd

    private val _newPwd = MutableLiveData<String>()
    val newPwd: MutableLiveData<String>
        get() = _newPwd

    private val _checkAlert = SingleLiveEvent<Any>()
    val checkAlert: LiveData<Any>
        get() = _checkAlert

    private val _errorAlert = SingleLiveEvent<Int>()
    val errorAlert: LiveData<Int>
        get() = _errorAlert

    fun onClickConfirm() {
        _checkAlert.call()
    }

    fun alertOkClick() {

        val oldPassword = _oldPwd.value.toString().trim()
        val newPassword = _newPwd.value.toString().trim()
        val confirmPassword = _confirmPwd.value.toString().trim()

        val isValid = isValidPassword(oldPassword, newPassword, confirmPassword)

        if (isValid) {
            // TODO kjyoo
            val userAgent = Preferences.userAgent
            val id = Preferences.userId
            val appID = DataUtil.appID
            val nationCode = Preferences.userNation

            RetrofitClient.instanceDynamic().requestChangePwd(
                    id, oldPassword, newPassword, appID, nationCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                    }, {

                    })

//                    .enqueue(object : Callback<APIModel> {
//
//                override fun onFailure(call: Call<APIModel>, t: Throwable) {
////                        progressBar.visibility = View.GONE
//
//                }
//
//                override fun onResponse(call: Call<APIModel>, response: Response<APIModel>) {
//
//                    if (response.isSuccessful) {
//                        if (response.body() != null && response.body()!!.resultCode == 0) {
////                                val loginData = Gson().fromJson(response.body()!!.resultObject, LoginInfo::class.java)
//// TODO kjyoo
//                        }
//                    }
//
////                        progressBar.visibility = View.GONE
//                }
//            })
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