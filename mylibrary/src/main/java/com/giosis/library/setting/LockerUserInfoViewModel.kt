package com.giosis.library.setting

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.data.LockerUserInfoResult
import com.giosis.library.util.Preferences
import com.giosis.library.util.SingleLiveEvent
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*


class LockerUserInfoViewModel : BaseViewModel() {

    private val _userKey = MutableLiveData<String>()
    val userKey: MutableLiveData<String>
        get() = _userKey

    private val _status = MutableLiveData<String>()
    val status: MutableLiveData<String>
        get() = _status

    private val _mobile = MutableLiveData<String>()
    val mobile: MutableLiveData<String>
        get() = _mobile

    private val _expiryDate = MutableLiveData<String>()
    val expiryDate: MutableLiveData<String>
        get() = _expiryDate

    private val _barcodeImg = MutableLiveData<Bitmap?>()
    val barcodeImg: MutableLiveData<Bitmap?>
        get() = _barcodeImg

    private val _errorAlert = SingleLiveEvent<Int>()
    val errorAlert: LiveData<Int>
        get() = _errorAlert

    private val _resultAlert = SingleLiveEvent<Any>()
    val resultAlert: LiveData<Any>
        get() = _resultAlert


    fun callServer() {

        progressVisible.value = true
        val id = Preferences.userId

        RetrofitClient.instanceDynamic().requestGetLockerUserInfo(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it != null && it.resultCode == 0) {

                    progressVisible.value = false

                    try {
                        val result = Gson().fromJson(
                            it.resultObject.toString(),
                            LockerUserInfoResult.LockerResultObject::class.java
                        )

                        _userKey.value = result.resultRows!![0].user_key.toString()
                        _status.value = result.resultRows!![0].user_status.toString()
                        _mobile.value = result.resultRows!![0].user_mobile.toString()

                        val date = result.resultRows!![0].user_expiry_date.toString()

                        try {

                            val oldFormat = SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.KOREA)
                            val newFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                            val oldDate = oldFormat.parse(date)

                            _expiryDate.value = newFormat.format(oldDate).toString()
                        } catch (e: Exception) {

                            _expiryDate.value = date
                        }

                        callBarcodeServer()
                    } catch (e: Exception) {

                        Log.e("Exception", "requestGetLockerUserInfo  $e")

                        progressVisible.value = false
                        _errorAlert.value = R.string.msg_download_locker_info_error
                    }
                } else {

                    progressVisible.value = false
                    _errorAlert.value = R.string.msg_download_locker_info_error
                }
            }, {

                progressVisible.value = false
                _errorAlert.value = R.string.msg_network_connect_error
            })
    }


    private fun callBarcodeServer() {

        progressVisible.value = true
        val key = _userKey.value.toString()
        var bitmap: Bitmap? = null


        RetrofitClient.instanceBarcode().requestGetBarcode(key)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                progressVisible.value = false

                try {

                    bitmap = BitmapFactory.decodeStream(it.byteStream())
                    _barcodeImg.value = bitmap
                } catch (e: Exception) {

                    Log.e("Exception", "requestGetBarcode  $e")
                    _barcodeImg.value = null
                }
            }, {

                progressVisible.value = false
                Log.e("Exception", "requestGetBarcode  Error")
                _barcodeImg.value = null
            })
    }
}

