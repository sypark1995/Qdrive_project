package com.giosis.library.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.BaseViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.NoticeResult
import com.giosis.library.util.SingleLiveEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class NoticeDetailViewModel : BaseViewModel() {

    private val _seqNo = MutableLiveData<String>()
    var seqNo: MutableLiveData<String>
        get() = _seqNo
        set(value) {

            _seqNo.value = value.toString()
        }


    private val _errorAlert = SingleLiveEvent<Int>()
    val errorAlert: LiveData<Int>
        get() = _errorAlert


    private val _resultAlert = SingleLiveEvent<Any>()
    val resultAlert: LiveData<Any>
        get() = _resultAlert


    fun setSeqNo(value: String) {

        _seqNo.value = value
    }


    fun callServer() {

        val noticeNo = _seqNo.value.toString()

        progressVisible.value = true

        RetrofitClient.instanceDynamic().requestGetNoticeData(noticeNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    progressVisible.value = false

                    try {

                        val result = Gson().fromJson<List<NoticeResult.NoticeItem>>(
                                it.resultObject.toString(), object : TypeToken<List<NoticeResult.NoticeItem>>() {}.type
                        )

                        Log.e("Server", it.resultObject.toString())
                        Log.e("Server", "${result[0].title}  /  ${result[0].date}")

                        _content.value = result[0].title
                        _date.value = result[0].date
                        _prevNo.value = result[0].prevNo
                        _nextNo.value = result[0].nextNo

                        _resultAlert.value = result
                    } catch (e: Exception) {

                        _resultAlert.value = e.toString()
                    }
                }, {

                    progressVisible.value = false
                    _resultAlert.value = R.string.msg_network_connect_error
                })
    }


    private val _content = MutableLiveData<String>()
    val content: MutableLiveData<String>
        get() = _content

    private val _date = MutableLiveData<String>()
    val date: MutableLiveData<String>
        get() = _date

    private val _prevNo = MutableLiveData<String>()
    val prevNo: MutableLiveData<String>
        get() = _prevNo


    private val _nextNo = MutableLiveData<String>()
    val nextNo: MutableLiveData<String>
        get() = _nextNo


    fun onClickPrev() {

        _seqNo.value = _prevNo.value
        callServer()
    }

    fun onClickNext() {

        _seqNo.value = _nextNo.value
        callServer()
    }
}