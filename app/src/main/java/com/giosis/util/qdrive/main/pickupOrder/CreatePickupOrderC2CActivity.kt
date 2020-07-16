package com.giosis.util.qdrive.main.pickupOrder


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.top_title.*


class CreatePickupOrderC2CActivity : AppCompatActivity() {

    companion object {
        const val PAR_BY_DRIVER = "par_by_driver"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pickup_c2c)

        layout_top_change_pickup.visibility = View.VISIBLE

        if (intent.getBooleanExtra(PAR_BY_DRIVER, false)) {
            text_top_title.text = resources.getString(R.string.text_pay_by_driver)
        } else {
            text_top_title.text = resources.getString(R.string.text_pay_by_customer)
        }


        layout_top_back.setOnClickListener { finish() }

    }

}