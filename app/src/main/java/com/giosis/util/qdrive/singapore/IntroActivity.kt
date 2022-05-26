package com.giosis.util.qdrive.singapore

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.setting.bluetooth.BluetoothDeviceData

class IntroActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Bluetooth Print 연결 초기화 (앱 종료시 못했을 경우를 대비  : MainActivity onDestroy 호출 안되는 경우 있음 !)
        BluetoothDeviceData.connectedPrinterAddress = null

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent()
            intent.setClass(this@IntroActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }, 500)
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}