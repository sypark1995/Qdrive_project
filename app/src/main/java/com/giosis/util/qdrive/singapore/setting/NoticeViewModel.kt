package com.giosis.util.qdrive.singapore.setting

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import com.giosis.util.qdrive.singapore.ListViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.data.NoticeResult
import com.giosis.util.qdrive.singapore.util.SingleLiveEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class NoticeViewModel : ListViewModel<NoticeResult.NoticeItem>() {

    private val _errorMsg = SingleLiveEvent<Any>()
    val errorMsg: LiveData<Any>
        get() = _errorMsg

    fun callServer() {

        progressVisible.value = true

        RetrofitClient.instanceDynamic().requestGetNoticeData("0", "List", 1, 30)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    val result = Gson().fromJson<ArrayList<NoticeResult.NoticeItem>>(
                        it.resultObject.toString(),
                        object : TypeToken<ArrayList<NoticeResult.NoticeItem>>() {}.type
                    )

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