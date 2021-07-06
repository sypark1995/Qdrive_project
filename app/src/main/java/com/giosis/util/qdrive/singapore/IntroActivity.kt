package com.giosis.util.qdrive.singapore

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler

class IntroActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Live10 설치 여부 확인
        val intent = packageManager.getLaunchIntentForPackage("net.giosis.qpost")
        if (intent == null && !BuildConfig.DEBUG) {

            AlertDialog.Builder(this)
                    .setMessage(resources.getString(R.string.msg_live10_installed))
                    .setCancelable(false)
                    .setPositiveButton(resources.getString(R.string.button_ok)
                    ) { _: DialogInterface?, _: Int ->

                        // 구글 마켓으로 이동
                        val intent1 = Intent(Intent.ACTION_VIEW)
                        intent1.data = Uri.parse("market://details?id=net.giosis.qpost")
                        startActivity(intent1)
                        finish()
                    }.show()
        } else {

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