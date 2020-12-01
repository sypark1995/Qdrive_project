package com.giosis.library.pickup

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.giosis.library.ActivityRequestCode
import com.giosis.library.BaseViewModel
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.CustomSellerInfo
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class CreatePickupOrderViewModel : BaseViewModel() {

    private val _visiblePickupLayout = MutableLiveData(false)
    val visiblePickupLayout: MutableLiveData<Boolean>
        get() = _visiblePickupLayout

    val _orderType = MutableLiveData(0)
    val orderType: MutableLiveData<Int>
        get() = _orderType

    private val _sellerId = MutableLiveData<String>()
    val sellerId: MutableLiveData<String>
        get() = _sellerId

    private val _pickupNo = MutableLiveData<String>()
    val pickupNo: MutableLiveData<String>
        get() = _pickupNo

    private val _zipCode = MutableLiveData<String>()
    val zipCode: MutableLiveData<String>
        get() = _zipCode

    private val _addressFront = MutableLiveData<String>()
    val addressFront: MutableLiveData<String>
        get() = _addressFront

    private val _addressLast = MutableLiveData<String>()
    val addressLast: MutableLiveData<String>
        get() = _addressLast

    val _phoneNo = MutableLiveData<String>()
    val phoneNo: MutableLiveData<String>
        get() = _phoneNo

    val _remarks = MutableLiveData("")
    val remarks: MutableLiveData<String>
        get() = _remarks

    fun setVisiblePickupLayout() {
        _visiblePickupLayout.value = !((_visiblePickupLayout.value)!!)
    }


    fun idSearchClick() {
        if (_orderType.value == 0) {

            if (_sellerId.value.isNullOrEmpty()) {
                toastString.postValue(R.string.enter_seller_id)
            } else {
                RetrofitClient.instanceDynamic().requestGetCustomSellerInfo("SellerID", _sellerId.value!!).subscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            try {
                                if (it.resultCode == 0) {
                                    val info: CustomSellerInfo = Gson().fromJson(it.resultObject, CustomSellerInfo::class.java)

                                    if (!info.resultRows.isNullOrEmpty()) {
                                        custNo = info.resultRows!![0].cust_no
                                        _zipCode.value = info.resultRows!![0].zip_code
                                        _addressFront.value = info.resultRows!![0].addr_front
                                        _addressLast.value = info.resultRows!![0].addr_last

                                        if (info.resultRows!![0].hp_no.contains("-")) {
                                            val phoneSplit = info.resultRows!![0].hp_no.split("-")
                                            _phoneNo.value = phoneSplit[1] + "-" + phoneSplit[2]
                                        } else {
                                            _phoneNo.value = info.resultRows!![0].hp_no
                                        }
                                    }
                                } else {
                                    toastString.value = it.resultMsg
                                }
                            } catch (e: Exception) {
                                e.stackTrace
                            }
                        }, {

                        })
            }
        }
    }

    fun pickupSearchClick() {
        // sample pickup No
        // C3548661SGSG
        // C3507265SGSG
        // C3507262SGSG
        if (_pickupNo.value.isNullOrEmpty()) {
            toastString.postValue(R.string.enter_pickup_no)
        } else {
            RetrofitClient.instanceDynamic().requestGetCustomSellerInfo("PickupNo", _pickupNo.value!!).subscribeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        try {
                            if (it.resultCode == 0) {
                                val info: CustomSellerInfo = Gson().fromJson(it.resultObject, CustomSellerInfo::class.java)

                                if (!info.resultRows.isNullOrEmpty()) {
                                    custNo = info.resultRows!![0].cust_no
                                    _zipCode.value = info.resultRows!![0].zip_code
                                    _addressFront.value = info.resultRows!![0].addr_front
                                    _addressLast.value = info.resultRows!![0].addr_last

                                    if (info.resultRows!![0].hp_no.contains("-")) {
                                        val phoneSplit = info.resultRows!![0].hp_no.split("-")
                                        _phoneNo.value = phoneSplit[1] + "-" + phoneSplit[2]
                                    } else {
                                        _phoneNo.value = info.resultRows!![0].hp_no
                                    }
                                }
                            } else {
                                toastString.value = it.resultMsg
                            }
                        } catch (e: Exception) {
                            e.stackTrace
                        }
                    }, {

                    })
        }
    }

    fun addressLayout() {
        startActivity(AddressDialogActivity::class.java, null, ActivityRequestCode.ADDRESS_REQUEST.ordinal)
    }

    private var custNo = ""

    fun clickRegister() {

        if (_sellerId.value.isNullOrEmpty() && _pickupNo.value.isNullOrEmpty()) {
            toastString.value = R.string.enter_seller_id
        } else {
            if (_zipCode.value.isNullOrEmpty() || _addressLast.value.isNullOrEmpty()) {
                toastString.value = R.string.enter_address
            } else {
                if (_phoneNo.value.isNullOrEmpty()) {
                    toastString.value = R.string.enter_phone_no
                } else {

                    // TODO 다이얼로그 추가 하기!!!!

                    RetrofitClient.instanceDynamic().requestSetSelfPickupOrder(
                            custNo = custNo,
                            zipcode = _zipCode.value!!,
                            addr1 = _addressFront.value!!,
                            addr2 = _addressLast.value!!,
                            mobileNo = "+65-" + _phoneNo.value,
                            requestMemo = _remarks.value!!
                    ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (it.resultCode == 0) {
                                    Log.e("TAG", "adfasdfasdfsa")
                                }
                            }, {

                            })
                }
            }

        }
    }
}