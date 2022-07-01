package com.giosis.util.qdrive.singapore.message

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ActivityMessageDetailBinding
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.CommonActivity
import com.giosis.util.qdrive.singapore.util.FirebaseEvent
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import com.giosis.util.qdrive.singapore.util.NetworkUtil
import com.giosis.util.qdrive.singapore.util.Preferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*


class AdminMessageListDetailActivity : CommonActivity() {
    var TAG = "AdminMessageListDetailActivity"

    private val binding by lazy {
        ActivityMessageDetailBinding.inflate(layoutInflater)
    }

    val progressBar by lazy {
        ProgressDialog(this@AdminMessageListDetailActivity)
    }

    val handler = Handler(Looper.getMainLooper())
    private val task = object : Runnable {
        override fun run() {
            callServer()
            handler.postDelayed(this, 5 * 60 * 1000)
        }
    }

    private var senderID: String = ""
    private var oldResultString: String = ""
    private var newResultString: String = ""
    private var messageList: ArrayList<MessageDetailResult> = ArrayList()
    private var messageDetailAdapter: MessageDetailAdapter? = null

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseEvent.createEvent(this, TAG)

        senderID = intent.getStringExtra("sender_id").toString()

        binding.layoutTopTitle.textTopTitle.text = senderID
        binding.textMessageTitle.visibility = View.GONE

        binding.editMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                binding.layoutSend.setBackgroundResource(R.drawable.btn_send_qpost)
                binding.editMessage.hint = ""
            }
        })

        binding.editMessage.setOnTouchListener { _, _ ->

            binding.layoutSend.setBackgroundResource(R.drawable.btn_send_qpost)
            binding.editMessage.hint = ""
            false
        }

        //
        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            finish()
        }

        binding.layoutSend.setOnClickListener {

            if (!NetworkUtil.isNetworkAvailable(this@AdminMessageListDetailActivity)) {

                showDialog(
                    resources.getString(R.string.text_warning),
                    resources.getString(R.string.msg_network_connect_error)
                )
            } else {
                sendChatMessage()
            }
        }

        binding.listDetailMessage.adapter?.let { binding.listDetailMessage.smoothScrollToPosition(it.itemCount) }
    }


    override fun onResume() {
        super.onResume()

        if (!NetworkUtil.isNetworkAvailable(this@AdminMessageListDetailActivity)) {

            showDialog(
                resources.getString(R.string.text_warning),
                resources.getString(R.string.msg_network_connect_error)
            )
            return
        } else {

            handler.post(task)
        }
    }

    fun callServer() {

        try {
            if (!isFinishing) {

                oldResultString = newResultString

                if (newResultString.isEmpty())
                    progressBar.visibility = View.VISIBLE

                Log.e(TAG, "callServer ---- GetQdriverMessageDetailFromMessenger")
                RetrofitClient.instanceDynamic().requestGetMessageDetailFromAdmin(senderID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (oldResultString != "" && oldResultString.equals(
                                newResultString,
                                ignoreCase = true
                            )
                        ) {

                            Log.e("Message", " GetQdriverMessageDetailFromMessenger    EQUAL")
                        } else {

                            if (it.resultObject != null) {

                                newResultString = it.toString()
                                messageList = Gson().fromJson(
                                    it.resultObject,
                                    object : TypeToken<ArrayList<MessageDetailResult>>() {}.type
                                )

                                if (0 < messageList.size) {

                                    for (i in messageList.indices) {

                                        var dateString = messageList[i].send_date
                                        val dateSplitArray =
                                            dateString.split(":".toRegex()).toTypedArray()
                                        dateString = dateSplitArray[0] + ":" + dateSplitArray[1]
                                        messageList[i].send_date = dateString
                                    }

                                    messageDetailAdapter = MessageDetailAdapter(
                                        this@AdminMessageListDetailActivity,
                                        messageList,
                                        "A"
                                    )
                                    binding.listDetailMessage.adapter = messageDetailAdapter
                                    binding.listDetailMessage.smoothScrollToPosition(messageList.size - 1)
                                } else {

                                    Toast.makeText(
                                        this@AdminMessageListDetailActivity,
                                        resources.getString(R.string.text_empty),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        progressBar.visibility = View.GONE
                    }, {

                        Toast.makeText(
                            this@AdminMessageListDetailActivity,
                            resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "  GetQdriverMessageListFromMessenger Exception : $it")
                    })
            }
        } catch (e: Exception) {

        }
    }

    override fun onStop() {
        super.onStop()
        try {
            handler.removeCallbacks(task)
        } catch (e: Exception) {
            Log.e(TAG, "onStop Exception $e")
        }
    }

    private fun sendChatMessage() {

        val sendMessage = binding.editMessage.text.toString().trim { it <= ' ' }

        if (sendMessage == "") {
            Toast.makeText(
                this@AdminMessageListDetailActivity,
                resources.getString(R.string.msg_enter_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        RetrofitClient.instanceDynamic().requestSendQdriveToMessengerMessage(sendMessage, senderID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    if (it.resultObject != null) {

                        val result = Gson().fromJson<MessageSendResult>(
                            it.resultObject,
                            object : TypeToken<MessageSendResult>() {}.type
                        )

                        if (result != null) {

                            binding.layoutSend.setBackgroundResource(R.drawable.btn_send_qpost)
                            binding.editMessage.setHint(R.string.msg_qpost_edit_text_hint)
                            binding.editMessage.setText("")

                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                            val today = simpleDateFormat.format(Calendar.getInstance().time)

                            val item = MessageDetailResult()
                            item.message = sendMessage
                            item.sender_id = senderID
                            item.receive_id = Preferences.userId
                            item.send_date = today
                            item.align = "right"

                            messageList.add(item)
                            messageDetailAdapter!!.notifyDataSetChanged()
                            binding.listDetailMessage.smoothScrollToPosition(messageList.size - 1)
                            Log.e(
                                "Message",
                                "SendQdriveToMessengerMessage Size : " + messageList.size
                            )
                        } else {

                            Toast.makeText(
                                this@AdminMessageListDetailActivity,
                                "${resources.getString(R.string.msg_send_message_error)} ${
                                    resources.getString(R.string.msg_please_try_again)
                                }",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Message", "SendQdriveToMessengerMessage  result null")
                        }
                    }
                } catch (e: Exception) {

                    Toast.makeText(
                        this@AdminMessageListDetailActivity,
                        "${resources.getString(R.string.msg_send_message_error)} ${
                            resources.getString(R.string.msg_please_try_again)
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "SendQdriveToMessengerMessage Exception : $e")
                }

                progressBar.visibility = View.GONE
            }, {

                Toast.makeText(
                    this@AdminMessageListDetailActivity,
                    resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, " GetQdriverMessageListFromMessenger Exception : $it")
            })
    }

    private fun showDialog(title: String?, msg: String?) {

        try {
            val alert = AlertDialog.Builder(this@AdminMessageListDetailActivity)
            alert.setTitle(title)
            alert.setMessage(msg)
            alert.setPositiveButton(resources.getString(R.string.button_close)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            alert.show()
        } catch (ignore: Exception) {
        }
    }
}