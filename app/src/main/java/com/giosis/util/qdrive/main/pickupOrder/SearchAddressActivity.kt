package com.giosis.util.qdrive.main.pickupOrder


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.dialog_search_address.*


class SearchAddressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_search_address)

        btn_search_address_close.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        layout_search_address_search.setOnClickListener {

        }

    }
}