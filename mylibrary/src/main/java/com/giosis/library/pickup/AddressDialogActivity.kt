package com.giosis.library.pickup

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityAddressDialogBinding

class AddressDialogActivity : BaseActivity<ActivityAddressDialogBinding, AddressDialogViewModel>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_address_dialog
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): AddressDialogViewModel {
        return ViewModelProvider(this).get(AddressDialogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG", "oncreate()")
    }


}