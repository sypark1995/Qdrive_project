package com.giosis.library.setting

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import com.giosis.library.ListViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.NoticeResult
import com.giosis.library.util.Preferences
import com.giosis.library.util.SingleLiveEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class NoticeViewModel : ListViewModel<NoticeResult.NoticeItem>() {

    private val _errorMsg = SingleLiveEvent<Any>()
    val errorMsg: LiveData<Any>
        get() = _errorMsg


    fun callServer() {

        Log.e("krm0219", "Nation  ${Preferences.userNation}")
        progressVisible.value = true

        RetrofitClient.instanceDynamic().requestGetNoticeData("0", "List", 1, 30)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {
                        val result = Gson().fromJson<ArrayList<NoticeResult.NoticeItem>>(
                                it.resultObject.toString(), object : TypeToken<ArrayList<NoticeResult.NoticeItem>>() {}.type
                        )
                        Log.e("krm0219", it.resultObject.toString())

                        setItemList(result)
                        notifyChange()

                    } catch (e: Exception) {

                        Log.e("Exception", "requestGetNoticeData  $e")
                        _errorMsg.value = e.toString()
                    }

                    progressVisible.value = false
                }, {

                    progressVisible.value = false
                    _errorMsg.value = R.string.msg_network_connect_error
                })
    }

    fun clickItem(pos: Int) {
        val bundle = Bundle()
        bundle.putString("notice_no", getItem(pos).seqNo)
        startActivity(NoticeDetailActivity::class.java, bundle)
    }
}