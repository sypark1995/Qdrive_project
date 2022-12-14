package com.giosis.util.qdrive.singapore.pickup

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.util.qdrive.singapore.ListViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.data.AddressResult
import com.giosis.util.qdrive.singapore.util.SingleLiveEvent
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class AddressDialogViewModel : ListViewModel<AddressResult.AddressItem>() {

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
            callServer(_searchText.value.toString().trim())
        }
    }


    private fun callServer(data: String) {

        progressVisible.value = true

        RetrofitClient.instanceDynamic().requestGetAddressInfo(data)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    val result1 =
                        Gson().fromJson(it.resultObject.toString(), AddressResult::class.java)

                    val result = result1.resultRows!!

                    if (result.size == 0) {
                        clearList()
                        _errorMsg.value = R.string.msg_no_results
                    } else {
                        setItemList(result)
                    }

                    notifyChange()

                } catch (e: Exception) {
                    Log.e("Exception", "requestGetAddressInfo  $e")
                    _errorMsg.value = e.toString()
                }

                progressVisible.value = false
            }, {

                Log.e("Exception", it.message.toString())
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