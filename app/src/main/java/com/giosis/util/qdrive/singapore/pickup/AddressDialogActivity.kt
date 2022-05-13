package com.giosis.util.qdrive.singapore.pickup


import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.giosis.util.qdrive.singapore.BR
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.ViewModelActivity
import com.giosis.util.qdrive.singapore.databinding.ActivityAddressDialogBinding
import kotlinx.android.synthetic.main.activity_address_dialog.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AddressDialogActivity :
    ViewModelActivity<ActivityAddressDialogBinding, AddressDialogViewModel>() {

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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun noti(event: String?) {
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}