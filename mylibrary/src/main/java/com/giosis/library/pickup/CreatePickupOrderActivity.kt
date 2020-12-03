package com.giosis.library.pickup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.ActivityRequestCode
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityCreatePickupOrderBinding
import com.giosis.library.util.Preferences
import com.giosis.library.util.dialog.CustomDialog
import kotlinx.android.synthetic.main.activity_create_pickup_order.*
import kotlinx.android.synthetic.main.top_title.*


class CreatePickupOrderActivity : BaseActivity<ActivityCreatePickupOrderBinding, CreatePickupOrderViewModel>() {
    val tag = "CreatePickupOrder"

    override fun getLayoutId(): Int {
        return R.layout.activity_create_pickup_order
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): CreatePickupOrderViewModel {
        return ViewModelProvider(this).get(CreatePickupOrderViewModel::class.java)
    }

    private val dialog by lazy { CustomDialog(this@CreatePickupOrderActivity, R.layout.custom_dialog) }
    private val confirmDialog by lazy { CustomDialog(this@CreatePickupOrderActivity) }


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

                    edit_seller_id.setText("")
                    edit_seller_id.isEnabled = true
                    edit_seller_id.background = resources.getDrawable(R.drawable.back_1_e1e1e1)
                    layout_seller_id_search.background = resources.getDrawable(R.drawable.back_round_3_border_4fb648)

                } else {

                    edit_seller_id.setText(Preferences.userId)
                    edit_seller_id.isEnabled = false
                    edit_seller_id.background = resources.getDrawable(R.drawable.back_1_e1e1e1_desable)
                    layout_seller_id_search.background = resources.getDrawable(R.drawable.back_round_3_border_4fb648_disable)
                }

                mViewModel.orderType.value = position
                mViewModel.zipCode.value = ""
                mViewModel.addressFront.value = ""
                mViewModel.addressLast.value = ""
                mViewModel.phoneNo.value = ""
                mViewModel.remarks.value = ""
            }
        }

        val pickupNo = "Pickup No."

        val str = getString(R.string.search_pickup_no).replace("%s", pickupNo)
        val index = str.indexOf(pickupNo)
        val sp = SpannableString(str)
        sp.setSpan(UnderlineSpan(), index, index + pickupNo.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text_pickup_no.text = sp


        mViewModel.visiblePickupLayout.observe(this) {
            if (it) {
                pickup_image.setImageDrawable(resources.getDrawable(R.drawable.pickup_up_icon))
            } else {
                pickup_image.setImageDrawable(resources.getDrawable(R.drawable.pickup_down_icon))
            }
        }

        mViewModel.checkAlert.observe(this) {
            if (it != null) {
                dialog.bindingData = it
                dialog.visibility = View.VISIBLE
            } else {
                dialog.visibility = View.GONE
            }
        }

        mViewModel.confirmAlert.observe(this) {
            if (it != null) {
                confirmDialog.bindingData = it
                confirmDialog.visibility = View.VISIBLE
            } else {
                confirmDialog.visibility = View.GONE
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (ActivityRequestCode.values()[requestCode]) {
            ActivityRequestCode.ADDRESS_REQUEST -> {

                val zipCode = data!!.extras!!.getString("zipCode")
                val frontAddress = data.extras!!.getString("frontAddress")
                Log.e(tag, "zonActivityResult data - $zipCode  /  $frontAddress")

                mViewModel.zipCode.value = zipCode
                mViewModel.addressFront.value = frontAddress
            }
            else -> {
            }
        }
    }

}
