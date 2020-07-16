package com.giosis.util.qdrive.main.pickupOrder

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.activity_create_pickup_b2c.*
import kotlinx.android.synthetic.main.top_title.*

class CreatePickupOrderB2CActivity : AppCompatActivity() {

    companion object {
        const val QOO10_SELLER = "qoo10_seller"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pickup_b2c)

        layout_top_change_pickup.visibility = View.VISIBLE

        if (intent.getBooleanExtra(QOO10_SELLER, false)) {
            text_top_title.text = resources.getString(R.string.text_by_qoo10_seller_id)
            layout_b2c_qoo10_seller.visibility = View.VISIBLE
            layout_b2c_manual_register.visibility = View.GONE
        } else {
            text_top_title.text = resources.getString(R.string.text_manual_register)
            layout_b2c_qoo10_seller.visibility = View.GONE
            layout_b2c_manual_register.visibility = View.VISIBLE
        }

        layout_top_back.setOnClickListener { finish() }

        layout_b2c_address_search.setOnClickListener {
            val intent = Intent(this, SearchAddressActivity::class.java)
            startActivity(intent)
        }


    }


}