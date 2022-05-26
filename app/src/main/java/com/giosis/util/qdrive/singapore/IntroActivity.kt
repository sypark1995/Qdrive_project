package com.giosis.util.qdrive.singapore

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.setting.bluetooth.BluetoothDeviceData

class IntroActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Bluetooth Print 연결 초기화 (앱 종료시 못했을 경우를 대비  : MainActivity onDestroy 호출 안되는 경우 있음 !)
        BluetoothDeviceData.connectedPrinterAddress = null

        Handler().postDelayed({
            val intent12 = Intent()
            intent12.setClass(this@IntroActivity, LoginActivity::class.java)
            intent12.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent12)
            finish()
        }, 500)
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}