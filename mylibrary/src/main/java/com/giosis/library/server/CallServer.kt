package com.giosis.library.server

import android.util.Log
import com.giosis.library.R
import com.giosis.library.server.data.FailedCodeResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers


object CallServer {


    const val PFC = "PFC"
    const val DFC = "DFC"

    interface GetFailedCodeCallback {
        fun onServerResult(value: FailedCodeResult)
        fun onServerError(value: Int)
    }

    fun getFailedCode(commonCode: String, nationCode: String, callback: GetFailedCodeCallback) {

        RetrofitClient.instanceDynamic().requestGetFailedCode(
                commonCode, nationCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    Log.e("krm0219", "result  ${it.resultCode}")
                    callback.onServerResult(it)
                }, {

                    Log.e(RetrofitClient.TAG, it.message.toString())
                    callback.onServerError(R.string.msg_please_try_again)
                })
    }

    interface GetFailedCodeCallback1 {
        fun onServerResult(value: APIModel)
    }

    fun getNotice(callback: GetFailedCodeCallback1) {
        Log.e("krm0219", "getFailedCode2")
        RetrofitClient.instanceDynamic().requestGetNoticeData("0", "List", 1, 5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {

                        Log.e("krm0219", "getFailedCode3")
                        callback.onServerResult(it)

                    } catch (e: Exception) {

                        Log.e("Exception", "requestGetNoticeData  $e")
                    }
                }, {

                })
    }
}