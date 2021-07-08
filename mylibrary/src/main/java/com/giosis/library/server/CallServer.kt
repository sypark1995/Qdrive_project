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

                    callback.onServerResult(it)
                }, {

                    Log.e(RetrofitClient.errorTag, it.toString())
                    callback.onServerError(R.string.msg_please_try_again)
                })
    }
}