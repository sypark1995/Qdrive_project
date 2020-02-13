package com.giosis.util.qdrive.qdelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.SharedPreferencesHelper
import kotlinx.android.synthetic.main.activity_qdelivery_step2.*
import kotlinx.android.synthetic.main.activity_qdelivery_step2.img_qd_step2_address_search
import kotlinx.android.synthetic.main.activity_qdelivery_step2_edit_info.*
import kotlinx.android.synthetic.main.top_title.*


class QDeliveryStep2Activity : AppCompatActivity() {

    lateinit var context: Context

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qdelivery_step2)

        text_top_title.setText(R.string.text_request_qdelivery)
        layout_top_back.setOnClickListener {

            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        }

        //
        context = applicationContext
        val opId = SharedPreferencesHelper.getSigninOpID(context)
        val qDeliveryData = intent.getSerializableExtra(DataUtil.qDeliveryData) as QDeliveryData

        text_qd_step2_driver_name.text = qDeliveryData.senderName
        text_qd_step2_driver_id.text = "($opId)"
        text_qd_step2_driver_address.text = "(${qDeliveryData.senderZipCode})" + qDeliveryData.senderAddress1 + qDeliveryData.senderAddress2

        text_qd_step2_receipt_country.text = qDeliveryData.toNation + "(${qDeliveryData.toNationCode})"


        layout_qd_step2_edit.setOnClickListener {

            val intent = Intent(this, QDeliveryStep2EditInfoActivity::class.java)
            intent.putExtra(DataUtil.qDeliveryData, qDeliveryData)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        img_qd_step2_address_search.setOnClickListener {

            val searchAddressDialog = SearchAddressDialog(this)

            searchAddressDialog.setCanceledOnTouchOutside(false)
            searchAddressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            searchAddressDialog.setDialogListener { zipCode, address ->

                qDeliveryData.receiptZipCode = zipCode
                qDeliveryData.receiptAddress1 = address

                edit_qd_step2_sender_address1.setText("(${qDeliveryData.receiptZipCode})" + qDeliveryData.receiptAddress1)
            }

            searchAddressDialog.show()
            searchAddressDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }


        btn_qd_step2_next.setOnClickListener {

            // TODO

            val intent = Intent(this, QDeliveryStep3Activity::class.java)
            intent.putExtra(DataUtil.qDeliveryData, qDeliveryData)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }
}
