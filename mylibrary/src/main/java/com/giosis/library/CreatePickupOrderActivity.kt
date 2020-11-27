package com.giosis.library

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.databinding.ActivityCreatePickupOrderBinding
import kotlinx.android.synthetic.main.top_title.*

class CreatePickupOrderActivity : BaseActivity<ActivityCreatePickupOrderBinding, CreatePickupOrderViewModel>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_create_pickup_order
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): CreatePickupOrderViewModel {
        return ViewModelProvider(this).get(CreatePickupOrderViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_create_pickup_order)

        layout_top_back.setOnClickListener {
            finish()
        }
    }

}