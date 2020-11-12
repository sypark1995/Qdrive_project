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

    private val _checkAlert = SingleLiveEvent<Any>()
    val checkAlert: LiveData<Any>
        get() = _checkAlert

    private val _errorAlert = SingleLiveEvent<Int>()
    val errorAlert: LiveData<Int>
        get() = _errorAlert

    private val _successAlert = SingleLiveEvent<Any>()
    val successAlert: LiveData<Any>
        get() = _successAlert


    init {

        _driverId.value = Preferences.userId
        _name.value = Preferences.userName
        _email.value = Preferences.userEmail
    }


    fun onClickConfirm() {
        _checkAlert.call()
    }

    fun modifyUserInfo() {

        val name = _name.value.toString().trim()
        val email = _email.value.toString().trim()

        val isValid = isValidData(name, email)

        if (isValid) {

            val id = Preferences.userId
            val appID = DataUtil.appID
            val nationCode = Preferences.userNation

            RetrofitClient.instanceDynamic().requestChangeMyInfo(
                    id, name, email, appID, nationCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
//                        progressBar.visibility = View.GONE
//                        if (response.isSuccessful) {
//                            if (response.body() != null) {
//
//                                _successAlert.value = response.body()
//                                Log.e(RetrofitClient.TAG, "${response.body()!!.resultCode} / ${response.body()!!.resultMsg}")
//
//                                if (response.body()!!.resultCode == 0) {
//
//                                    Preferences.userName = name
//                                    Preferences.userEmail = email
//                                }
//                            }
//                        } else {
//
//                            TODO()
//                        }
//
//                        progressBar.visibility = View.GONE
                    }, {
//                        progressBar.visibility = View.GONE
                    })

        }
    }


    private fun isValidData(name: String, email: String): Boolean {

        val isValid: Boolean

        if (name.trim().length >= 6) {
            if (email.isNotEmpty()) {

                val emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
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