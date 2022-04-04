package com.giosis.util.qdrive.international

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.giosis.library.BuildConfig
import com.giosis.library.setting.bluetooth.BluetoothDeviceData
import com.giosis.library.util.LocaleManager.Companion.getInstance

class IntroActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Live10 설치 여부 확인
        val intent = packageManager.getLaunchIntentForPackage("net.giosis.qpost")

        if (intent == null && !BuildConfig.DEBUG) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.msg_live10_installed))
                .setCancelable(false).setPositiveButton(
                    resources.getString(R.string.button_ok)
                ) { dialog, which ->
                    val qtalkIntent = Intent(Intent.ACTION_VIEW)
                    qtalkIntent.data = Uri.parse("market://details?id=net.giosis.qpost")
                    startActivity(qtalkIntent)
                    finish()
                }.show()

        } else {

            // Bluetooth Print 연결 초기화 (앱 종료시 못했을 경우를 대비  : MainActivity onDestroy 호출 안되는 경우 있음 !)
            BluetoothDeviceData.connectedPrinterAddress = null
            Handler().postDelayed({
                val intent = Intent()
                intent.setClass(applicationContext, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 500)
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(getInstance(base).setLocale(base))
    }
}