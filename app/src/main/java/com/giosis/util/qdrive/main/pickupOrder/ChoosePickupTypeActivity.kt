package com.giosis.util.qdrive.main.pickupOrder


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.activity_create_pickup_intro.*
import kotlinx.android.synthetic.main.dialog_pickup_type.*


class ChoosePickupTypeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pickup_intro)

        btn_create_pickup_intro_back.setOnClickListener { finish() }
        btn_pickup_type_close.visibility = View.GONE


        layout_pickup_type_by_qoo10.setOnClickListener {
            val intent = Intent(this, CreatePickupOrderB2CActivity::class.java)
            intent.putExtra(CreatePickupOrderB2CActivity.QOO10_SELLER, true)
            startActivity(intent)
        }

        layout_pickup_type_manual_register.setOnClickListener {
            val intent = Intent(this, CreatePickupOrderB2CActivity::class.java)
            intent.putExtra(CreatePickupOrderB2CActivity.QOO10_SELLER, false)
            startActivity(intent)
        }

        layout_pickup_type_pay_driver.setOnClickListener {
            val intent = Intent(this, CreatePickupOrderC2CActivity::class.java)
            intent.putExtra(CreatePickupOrderC2CActivity.PAR_BY_DRIVER, true)
            startActivity(intent)
        }

        layout_pickup_type_pay_customer.setOnClickListener {
            val intent = Intent(this, CreatePickupOrderC2CActivity::class.java)
            intent.putExtra(CreatePickupOrderC2CActivity.PAR_BY_DRIVER, false)
            startActivity(intent)
        }
    }

}