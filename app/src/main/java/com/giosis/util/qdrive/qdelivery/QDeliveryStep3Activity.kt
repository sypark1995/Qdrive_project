package com.giosis.util.qdrive.qdelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.DataUtil
import kotlinx.android.synthetic.main.activity_qdelivery_step3.*
import kotlinx.android.synthetic.main.top_title.*
import java.text.SimpleDateFormat
import java.util.*


class QDeliveryStep3Activity : AppCompatActivity() {

    lateinit var context: Context


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qdelivery_step3)

        text_top_title.setText(R.string.text_request_qdelivery)
        layout_top_back.setOnClickListener {

            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        }


        //
        context = applicationContext
        val qDeliveryData = intent.getSerializableExtra(DataUtil.qDeliveryData) as QDeliveryData


        val calendar = Calendar.getInstance()
        val format1 = SimpleDateFormat("yyyy / MM / dd / EEE", Locale.ENGLISH)
        val formatted = format1.format(calendar.time)

        Log.e("krm0219", " DATE : $formatted")

        // TODO.  server 전달 형식 체크!
        qDeliveryData.requestDate = formatted
        qDeliveryData.requestTime = "10:00~19:00"

        text_qd_step3_date.text = qDeliveryData.requestDate
        text_qd_step3_time.text = qDeliveryData.requestTime



        btn_qd_step3_next.setOnClickListener {

            val pickupMemo = edit_qd_step3_memo.text.toString()

            if (pickupMemo.isNotEmpty()) {

                qDeliveryData.pickupMemo = pickupMemo
            }

            val intent = Intent(this, QDeliveryStep4Activity::class.java)
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