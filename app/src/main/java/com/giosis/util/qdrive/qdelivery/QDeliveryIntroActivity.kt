package com.giosis.util.qdrive.qdelivery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.SharedPreferencesHelper
import kotlinx.android.synthetic.main.activity_qdelivery_intro.*

class QDeliveryIntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qdelivery_intro)


        btn_qd_intro_back.setOnClickListener {

            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        }

        layout_qd_intro_request_qdelivery.setOnClickListener {

            val qDeliveryData = QDeliveryData()

            qDeliveryData.senderId = SharedPreferencesHelper.getSigninOpID(applicationContext)
            qDeliveryData.senderName = SharedPreferencesHelper.getSigninOpName(applicationContext)
            qDeliveryData.fromNation = applicationContext.resources.getString(R.string.app_nation)
            qDeliveryData.fromNationCode = applicationContext.resources.getString(R.string.app_nation_code)
            qDeliveryData.senderZipCode = "757322"
            qDeliveryData.senderAddress1 = "HARVEST @ WOODLANDS 280 WOODLANDS INDUSTRIAL PARK E5 "
            qDeliveryData.senderAddress2 = "#10-54 (Use Lift Lobby 1)"


            val intent = Intent(this, QDeliveryStep1Activity::class.java)
            intent.putExtra(DataUtil.qDeliveryData, qDeliveryData)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }

        layout_qd_intro_my_qdelivery.setOnClickListener {

            val intent = Intent(this, MyQDeliveryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }
}
