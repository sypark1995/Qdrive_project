package com.giosis.util.qdrive.singapore

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.singapore.setting.bluetooth.BluetoothDeviceData

class IntroActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Live10 설치 여부 확인
        val intent = packageManager.getLaunchIntentForPackage("net.giosis.qpost")

        if (intent == null && !BuildConfig.DEBUG) {

            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.msg_live10_installed))
                .setCancelable(false)
                .setPositiveButton(
                    resources.getString(R.string.button_ok)
                ) { _: DialogInterface?, _: Int ->

                    // 구글 마켓으로 이동
                    val intent1 = Intent(Intent.ACTION_VIEW)
                    intent1.data = Uri.parse("market://details?id=net.giosis.qpost")

                    try {
                        startActivity(intent1)
                    } catch (e: ActivityNotFoundException) {

                        // Play Store 없는 핸드폰의 경우
                        val webIntent = Intent(Intent.ACTION_VIEW)
                        webIntent.data =
                            Uri.parse("https://play.google.com/store/apps/details?id=net.giosis.qpost")

                        if (webIntent.resolveActivity(packageManager) != null) {
                            startActivity(webIntent)
                        }
                    }

                    finish()
                }.show()
        } else {

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
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}