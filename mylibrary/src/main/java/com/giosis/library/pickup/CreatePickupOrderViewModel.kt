package com.giosis.library.pickup

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.giosis.library.ActivityRequestCode
import com.giosis.library.BaseViewModel
import com.giosis.library.R

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

    val _phoneNo = MutableLiveData<String>()
    val phoneNo: MutableLiveData<String>
        get() = _phoneNo


    fun setVisiblePickupLayout() {
        _visiblePickupLayout.value = !((_visiblePickupLayout.value)!!)
    }


    fun idSearchClick() {
        if (_orderType.value == 0) {

            if (_sellerId.value.isNullOrEmpty()) {
                toastString.postValue(R.string.enter_seller_id)
            } else {
                Log.e("TAG", "api 호 " + sellerId.value)
                // TODO
            }
        }
    }

    fun pickupSearchClick() {
        if (_pickupNo.value.isNullOrEmpty()) {
            toastString.postValue(R.string.enter_pickup_no)
        } else {
            // TODO
        }
    }

    fun addressLayout() {
        startActivity(AddressDialogActivity::class.java, null, ActivityRequestCode.ADDRESS_REQUEST.ordinal)
    }
}