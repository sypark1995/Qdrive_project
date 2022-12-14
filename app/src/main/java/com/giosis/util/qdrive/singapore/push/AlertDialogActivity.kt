package com.giosis.util.qdrive.singapore.push

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.app.NotificationManagerCompat
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.DialogPushAlertBinding
import com.giosis.util.qdrive.singapore.main.MainActivity
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.LocaleManager.Companion.getInstance

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
            val notiTitle = if (bun.getString(PushData.TITLE) != null) {
                bun.getString(PushData.TITLE)
            } else {
                ""
            }

            val notiMessage = if (bun.getString(PushData.MESSAGE) != null) {
                bun.getString(PushData.MESSAGE)
            } else {
                ""
            }

            val actionKey = if (bun.getString(PushData.ACTION_KEY) != null) {
                bun.getString(PushData.ACTION_KEY)
            } else {
                ""
            }

            val actionValue = if (bun.getString(PushData.ACTION_VALUE) != null) {
                bun.getString(PushData.ACTION_VALUE)
            } else {
                ""
            }

            val activityName = if (bun.getString(PushData.ACTIVITY_NAME) != null) {
                bun.getString(PushData.ACTIVITY_NAME)
            } else {
                ""
            }

            if (actionKey == PushData.LZD_PICK) {
                binding.titleText.text = "Pickup no : $actionValue"
                binding.messageText.text =
                    resources.getString(R.string.text_lazada_order) + "\n" + resources.getString(
                        R.string.text_lazada_content
                    )

            } else if(actionKey == PushData.LOGOUT) {
                binding.titleText.visibility = View.GONE
                binding.messageText.text = resources.getString(R.string.text_logout)
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

                if (actionKey == PushData.SevenEle_TAKEBACK
                    || actionKey == PushData.FL_TAKEBACK
                    || actionKey == PushData.LZD_PICK
                ) {
                    if (activityName!!.contains("CaptureActivity")) {
                        finish()
                    } else {
                        if (actionValue!!.isEmpty()) {
                            try {
                                // ok ?????? ????????? ???????????? Notification ?????????
                                NotificationManagerCompat.from(application)
                                    .cancel(actionValue.substring(0, 9).toInt())

                            } catch (e: Exception) {

                            }
                        }
                        val intent = Intent(this@AlertDialogActivity, MainActivity::class.java)
                        intent.putExtra(PushData.DOWNLOAD, "Y")
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }

                } else if (actionKey == PushData.Locker_EXPIRED) {      // 2019.02

                    if (actionValue!!.isEmpty()) {
                        try {
                            // ok ?????? ????????? ???????????? Notification ?????????
                            NotificationManagerCompat.from(application)
                                .cancel(actionValue.substring(0, 9).toInt())

                        } catch (e: Exception) {

                        }
                    }

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DataUtil.locker_pin_url))
                    startActivity(intent)

                } else if (actionKey == PushData.LOGOUT) {
                    val intent = Intent(this@AlertDialogActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                finish()
            }
        }

    }

    override fun onBackPressed() {
        //back key ??????
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(getInstance(base!!).setLocale(base))
    }
}