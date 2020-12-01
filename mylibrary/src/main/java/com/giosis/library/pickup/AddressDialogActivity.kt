package com.giosis.library.pickup

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityAddressDialogBinding
import kotlinx.android.synthetic.main.activity_address_dialog.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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

    val adapter: AddressAdapter by lazy {
        AddressAdapter(mViewModel)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btn_search_address_close.setOnClickListener {

            setResult(Activity.RESULT_CANCELED)
            finish()
        }


        recycler_search_address.adapter = adapter

        edit_search_address.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                mViewModel.searchText.value = v!!.text.toString()
                mViewModel.clickSearch()
            }

            true
        }


        getViewModel().errorMsg.observe(this) {

            if (it is String) {

                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            } else if (it is Int) {

                Toast.makeText(this, resources.getString(it), Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()

        mViewModel.searchText.value = "048616"
        mViewModel.clickSearch()
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun addressNoti(event: String?) {
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}