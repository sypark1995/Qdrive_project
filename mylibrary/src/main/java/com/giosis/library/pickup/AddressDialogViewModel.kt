package com.giosis.library.pickup

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.ListViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.AddressResult
import com.giosis.library.util.SingleLiveEvent
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class AddressDialogViewModel : ListViewModel<AddressResult.AddressResultObject.AddressItem>() {


    private val _searchText = MutableLiveData<String>()
    val searchText: MutableLiveData<String>
        get() = _searchText

    private val _errorMsg = SingleLiveEvent<Any>()
    val errorMsg: LiveData<Any>
        get() = _errorMsg


    fun clickSearch() {

        if (_searchText.value.isNullOrEmpty()) {

            toastString.postValue(R.string.msg_please_input_data)
        } else {

            Log.e("TAG", "api 호출 " + _searchText.value)
            callServer(_searchText.value.toString().trim())
        }
    }


    private fun callServer(data: String) {

        progressVisible.value = true

        RetrofitClient.instanceTestServer().requestGetAddressInfo(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {

                        val result1 = Gson().fromJson(it.resultObject.toString(), AddressResult.AddressResultObject::class.java)
                        val result = result1.resultRows!!

                        Log.e("krm0219", "Server  ${result.size}  ${result[0].zipCode}  ${result[0].frontAddress}")
                        setItemList(result)
                        notifyChange1()
                        //    notifyChange()
                    } catch (e: Exception) {

                        Log.e("Exception", "requestGetAddressInfo  $e")
                        _errorMsg.value = e.toString()
                    }

                    progressVisible.value = false
                }, {

                    Log.e("krm0219", it.message.toString())
                    progressVisible.value = false
                    _errorMsg.value = R.string.msg_network_connect_error
                })
    }


    fun clickItem(pos: Int) {

        val bundle = Bundle()
        bundle.putString("zipCode", getItem(pos).zipCode)
        bundle.putString("frontAddress", getItem(pos).frontAddress)
        finish(bundle)
    }
}