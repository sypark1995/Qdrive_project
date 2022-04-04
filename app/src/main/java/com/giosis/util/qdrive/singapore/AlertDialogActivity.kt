package com.giosis.util.qdrive.singapore

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.app.NotificationManagerCompat
import com.giosis.library.main.MainActivity
import com.giosis.util.qdrive.singapore.databinding.DialogPushAlertBinding
import com.giosis.library.util.DataUtil

class AlertDialogActivity : Activity() {

    private val binding by lazy {
        DialogPushAlertBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        setFinishOnTouchOutside(false)

        val bun = intent.extras

        if (bun != null) {
            val notiTitle = if (bun.getString(FCMIntentService.TITLE) != null) {
                bun.getString(FCMIntentService.TITLE)
            } else {
                ""
            }

            val notiMessage = if (bun.getString(FCMIntentService.MESSAGE) != null) {
                bun.getString(FCMIntentService.MESSAGE)
            } else {
                ""
            }

            val actionKey = if (bun.getString(FCMIntentService.ACTION_KEY) != null) {
                bun.getString(FCMIntentService.ACTION_KEY)
            } else {
                ""
            }

            val actionValue = if (bun.getString(FCMIntentService.ACTION_VALUE) != null) {
                bun.getString(FCMIntentService.ACTION_VALUE)
            } else {
                ""
            }


            if (actionKey == FCMIntentService.LZD_PICK) {
                binding.titleText.text = "Pickup no : $actionValue"
                binding.messageText.text = resources.getString(R.string.text_lazada_order) + "\n" + resources.getString(R.string.text_lazada_content)

            } else {
                if (notiTitle!!.isEmpty()) {
                    binding.titleText.visibility = View.GONE

                } else {
                    binding.titleText.visibility = View.VISIBLE
                    binding.titleText.text = notiTitle
                }

                binding.messageText.text = notiMessage
            }

            binding.okBtn.setOnClickListener {

                if (actionKey == FCMIntentService.SevenEle_TAKEBACK
                    || actionKey == FCMIntentService.FL_TAKEBACK
                    || actionKey == FCMIntentService.LZD_PICK
                ) {
                    val intent = Intent(this@AlertDialogActivity, MainActivity::class.java)
                    intent.putExtra(FCMIntentService.DOWNLOAD, "Y")
                    startActivity(intent)
                    overridePendingTransition(0, 0)

                } else if (actionKey == FCMIntentService.LAE) {      // 2019.02

                    if (actionValue!!.isEmpty()) {
                        try {
                            // ok 버튼 눌러서 이동하면 Notification 지우기
                            NotificationManagerCompat.from(application)
                                .cancel(actionValue.substring(0, 9).toInt())

                        } catch (e: Exception) {

                        }
                    }

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DataUtil.locker_pin_url))
                    startActivity(intent)

                } else if (actionKey == "LOGOUT") {
                    val intent = Intent(this@AlertDialogActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                finish()
            }
        }

    }

    override fun onBackPressed() {
        //back key 막기
    }

}