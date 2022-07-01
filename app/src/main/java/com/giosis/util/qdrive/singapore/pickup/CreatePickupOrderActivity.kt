package com.giosis.util.qdrive.singapore.pickup


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.giosis.util.qdrive.singapore.ActivityRequestCode
import com.giosis.util.qdrive.singapore.BR
import com.giosis.util.qdrive.singapore.ViewModelActivity
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ActivityCreatePickupOrderBinding
import com.giosis.util.qdrive.singapore.util.FirebaseEvent
import com.giosis.util.qdrive.singapore.util.Preferences
import com.giosis.util.qdrive.singapore.util.dialog.CustomDialog
import kotlinx.android.synthetic.main.activity_create_pickup_order.*
import kotlinx.android.synthetic.main.top_title.*


class CreatePickupOrderActivity :
    ViewModelActivity<ActivityCreatePickupOrderBinding, CreatePickupOrderViewModel>() {
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

    private val dialog by lazy {
        CustomDialog(
            this@CreatePickupOrderActivity,
            R.layout.custom_dialog
        )
    }

    private val confirmDialog by lazy { CustomDialog(this@CreatePickupOrderActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseEvent.createEvent(this, tag)

        text_top_title.text = resources.getString(R.string.text_create_pickup_order)

        layout_top_back.setOnClickListener {
            finish()
        }

        val orderList = listOf(
            resources.getString(R.string.pickup_order_seller),
            resources.getString(R.string.pickup_order_general)
        )

        val adapter = OrderTypeAdapter(orderList)
        select_spinner.adapter = adapter

        select_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {

                    edit_seller_id.setText("")
                    edit_seller_id.isEnabled = true

                    edit_seller_id.background = ContextCompat.getDrawable(
                        this@CreatePickupOrderActivity,
                        R.drawable.border_e6e6e6
                    )

                    layout_seller_id_search.background =
                        ContextCompat.getDrawable(
                            this@CreatePickupOrderActivity,
                            R.drawable.back_round_3_border_4fb648
                        )
                } else {

                    edit_seller_id.setText(Preferences.userId)
                    edit_seller_id.isEnabled = false
                    edit_seller_id.background =
                        ContextCompat.getDrawable(
                            this@CreatePickupOrderActivity,
                            R.drawable.back_1_e1e1e1_desable
                        )

                    layout_seller_id_search.background =
                        ContextCompat.getDrawable(
                            this@CreatePickupOrderActivity,
                            R.drawable.back_round_3_border_4fb648_disable
                        )
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
        sp.setSpan(
            UnderlineSpan(),
            index,
            index + pickupNo.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        text_pickup_no.text = sp

        mViewModel.visiblePickupLayout.observe(this) {
            if (it) {
                pickup_image.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@CreatePickupOrderActivity,
                        R.drawable.icon_round_arrow_up
                    )
                )
            } else {
                pickup_image.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@CreatePickupOrderActivity,
                        R.drawable.icon_round_arrow_down
                    )
                )
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
