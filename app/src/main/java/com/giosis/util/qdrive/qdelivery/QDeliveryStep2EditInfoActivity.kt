package com.giosis.util.qdrive.qdelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.DataUtil
import kotlinx.android.synthetic.main.activity_qdelivery_step2_edit_info.*
import kotlinx.android.synthetic.main.top_title.*


class QDeliveryStep2EditInfoActivity : AppCompatActivity() {

    lateinit var context: Context

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qdelivery_step2_edit_info)

        text_top_title.setText(R.string.text_request_qdelivery)
        layout_top_back.setOnClickListener {


            finish()
            overridePendingTransition(0, 0)
        }

        //
        context = applicationContext
        val qDeliveryData = intent.getSerializableExtra(DataUtil.qDeliveryData) as QDeliveryData


        edit_qd_step2_sender_name.setText(qDeliveryData.senderName)
        text_qd_step2_sender_country.text = qDeliveryData.fromNation + "(${qDeliveryData.fromNationCode})"
        edit_qd_step2_sender_contact_no.setText(qDeliveryData.senderContactNo)
        edit_qd_step2_sender_address1.setText("(${qDeliveryData.senderZipCode})" + qDeliveryData.senderAddress1)
        edit_qd_step2_sender_address2.setText(qDeliveryData.senderAddress2)


        test_address1.setText("(${qDeliveryData.senderZipCode})" + qDeliveryData.senderAddress1)
        test_address2.setText(qDeliveryData.senderAddress2)


/*        edit_qd_step2_sender_address1.viewTreeObserver.addOnGlobalLayoutListener { Log.e("krm0219", "Width : " + edit_qd_step2_sender_address1.width) }
        val realSize = Rect()

        edit_qd_step2_sender_address1.paint.getTextBounds(edit_qd_step2_sender_address1.text.toString(), 0, edit_qd_step2_sender_address1.text.length, realSize)
        Log.e("krm0219", "Real Width : " + realSize.width())*/


        img_qd_step2_address_search.setOnClickListener {

            val searchAddressDialog = AddressDialog(this)

            searchAddressDialog.setCanceledOnTouchOutside(false)
            searchAddressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val listener = object : AddressDialog.SearchListener {
                override fun onSubmitClicked(zipCode: String, address: String) {

                    qDeliveryData.receiptZipCode = zipCode
                    qDeliveryData.receiptAddress1 = address

                    edit_qd_step2_sender_address1.setText("(${qDeliveryData.receiptZipCode})" + qDeliveryData.receiptAddress1)
                }
            }

            searchAddressDialog.setDialogListener(listener)
            searchAddressDialog.show()
            searchAddressDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }


        layout_qd_step2_save.setOnClickListener {

            val senderName = edit_qd_step2_sender_name.text.toString().trim()
            val senderContactNo = edit_qd_step2_sender_contact_no.text.toString().trim()
            val senderAddress = edit_qd_step2_sender_address2.text.toString().trim()

            when {
                senderName.isEmpty() -> {

                    Toast.makeText(context, "Name 필수!!", Toast.LENGTH_SHORT).show()
                }
                senderContactNo.isEmpty() -> {

                    Toast.makeText(context, "Contact No. 필수!!", Toast.LENGTH_SHORT).show()
                }
                senderAddress.isEmpty() -> {

                    Toast.makeText(context, "Back Address 필수!!", Toast.LENGTH_SHORT).show()
                }
                else -> {

                    qDeliveryData.senderName = senderName
                    qDeliveryData.senderContactNo = senderContactNo
                    qDeliveryData.senderAddress2 = senderAddress

                    val intent = Intent(this, QDeliveryStep2Activity::class.java)
                    intent.putExtra(DataUtil.qDeliveryData, qDeliveryData)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
        }
    }
}