package com.giosis.library.pickup

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityCreatePickupOrderBinding
import kotlinx.android.synthetic.main.activity_create_pickup_order.*
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

        val orderList = listOf(resources.getString(R.string.pickup_order_seller), resources.getString(R.string.pickup_order_general))

        val adapter = OrderTypeAdapter(orderList)
        select_spinner.adapter = adapter

        select_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    edit_seller_id.isEnabled = true
                    edit_seller_id.background = resources.getDrawable(R.drawable.back_1_e1e1e1)
                    layout_seller_id_search.background = resources.getDrawable(R.drawable.back_round_3_border_4fb648)
                } else {
                    edit_seller_id.isEnabled = false
                    edit_seller_id.background = resources.getDrawable(R.drawable.back_1_e1e1e1_desable)
                    layout_seller_id_search.background = resources.getDrawable(R.drawable.back_round_3_border_4fb648_disable)
                }

                mViewModel._orderType.value = position
            }
        }


        val pickupNo = "Pickup No."

        val str = getString(R.string.search_pickup_no).replace("%s", pickupNo)
        val index = str.indexOf(pickupNo)
        val sp = SpannableString(str)
        sp.setSpan(UnderlineSpan(), index, index + pickupNo.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text_pickup_no.text = sp
    }

}
