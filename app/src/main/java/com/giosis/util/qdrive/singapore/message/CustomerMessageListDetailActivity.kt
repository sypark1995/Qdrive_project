package com.giosis.util.qdrive.singapore.message

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
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
import kotlin.collections.ArrayList


class CustomerMessageListDetailActivity : CommonActivity() {
    var TAG = "CustomerMessageListDetailActivity"

    private val binding by lazy {
        ActivityMessageDetailBinding.inflate(layoutInflater)
    }

    val progressBar by lazy {
        ProgressDialog(this@CustomerMessageListDetailActivity)
    }

    val handler = Handler(Looper.getMainLooper())
    private val task = object : Runnable {
        override fun run() {
            callServer()
            handler.postDelayed(this, 60 * 1000)
        }
    }

    private var oldResultString: String = ""
    private var newResultString: String = ""
    private var messageList: ArrayList<MessageDetailResult> = ArrayList()
    private var messageDetailAdapter: MessageDetailAdapter? = null

    private var questionNo: String = "0"
    var trackingNo = ""
    private var sendTitle: String = ""
    private var sendMessage: String = ""


    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseEvent.createEvent(this, TAG)

        //
        questionNo = intent.getIntExtra("question_no", 0).toString() // ?????? 0
        trackingNo = intent.getStringExtra("tracking_no").toString()
        Log.e(TAG, "$TAG  $questionNo / $trackingNo")

        binding.layoutTopTitle.textTopTitle.text = trackingNo

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

            if (!NetworkUtil.isNetworkAvailable(this@CustomerMessageListDetailActivity)) {

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

        if (!NetworkUtil.isNetworkAvailable(this@CustomerMessageListDetailActivity)) {

            showDialog(
                resources.getString(R.string.text_warning),
                resources.getString(R.string.msg_network_connect_error)
            )
        } else if (questionNo == "0") {

            Log.e(TAG, "in LIST")

            progressBar.visibility = View.VISIBLE
            RetrofitClient.instanceDynamic().requestGetMessageToQPostOnPickupMenu(trackingNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    progressBar.visibility = View.GONE

                    if (it.resultObject != null) {

                        val list = Gson().fromJson<ArrayList<MessageQuestionNumberResult>>(
                            it.resultObject,
                            object : TypeToken<ArrayList<MessageQuestionNumberResult>>() {}.type
                        )

                        questionNo = "0"

                        if (list != null && list.isNotEmpty()) {
                            if (0 < list[0].questionNo)
                                questionNo = list[0].questionNo.toString()
                        }

                        handler.post(task)
                    }
                }, {

                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@CustomerMessageListDetailActivity,
                        resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Exception", "$TAG  GetMessageToQPostOnPickupMenu Exception : $it")
                })
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

                Log.e(TAG, "callServer ---- GetQdriverMessageDetail")
                RetrofitClient.instanceDynamic().requestGetQdriverMessageDetail(questionNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (oldResultString != "" && oldResultString.equals(
                                newResultString,
                                ignoreCase = true
                            )
                        ) {

                            Log.e(TAG, " GetQdriverMessageDetail    EQUAL")
                        } else {

                            if (it.resultObject?.isJsonNull == false && it.resultObject != null) {

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

                                    binding.textMessageTitle.text = messageList[0].title
                                    messageDetailAdapter = MessageDetailAdapter(
                                        this@CustomerMessageListDetailActivity,
                                        messageList,
                                        "C"
                                    )
                                    binding.listDetailMessage.adapter = messageDetailAdapter
                                } else {

                                    binding.textMessageTitle.text =
                                        resources.getString(R.string.text_qxpress_driver)
                                    messageList = ArrayList()
                                    messageDetailAdapter = MessageDetailAdapter(
                                        this@CustomerMessageListDetailActivity,
                                        messageList,
                                        "C"
                                    )
                                    binding.listDetailMessage.adapter = messageDetailAdapter
                                }
                            } else {

                                binding.textMessageTitle.text =
                                    resources.getString(R.string.text_qxpress_driver)
                                messageList = ArrayList()
                                messageDetailAdapter = MessageDetailAdapter(
                                    this@CustomerMessageListDetailActivity,
                                    messageList,
                                    "C"
                                )
                                binding.listDetailMessage.adapter = messageDetailAdapter
                            }
                        }

                        progressBar.visibility = View.GONE
                    }, {

                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@CustomerMessageListDetailActivity,
                            resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Exception", "$TAG GetQdriverMessageDetail Exception : $it")
                    })
            }
        } catch (e: Exception) {
            Log.e("Exception", "$TAG  AsyncHandler Exception : $e")
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

        sendTitle = binding.textMessageTitle.text.toString().trim { it <= ' ' }
        sendMessage = binding.editMessage.text.toString().trim { it <= ' ' }

        if (sendMessage == "") {
            Toast.makeText(
                this@CustomerMessageListDetailActivity,
                resources.getString(R.string.msg_enter_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        progressBar.visibility = View.VISIBLE
        RetrofitClient.instanceDynamic()
            .requestSendQdriverMessage(trackingNo, sendTitle, sendMessage, questionNo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {
                    if (it.resultObject != null) {

                        val result = Gson().fromJson<MessageSendResult>(it.resultObject,
                            object : TypeToken<MessageSendResult>() {}.type
                        )

                        if (result != null) {
                            if (result.resultCode == "0") {

                                binding.layoutSend.setBackgroundResource(R.color.color_ebebeb)
                                binding.editMessage.setHint(R.string.msg_qpost_edit_text_hint)
                                binding.editMessage.setText("")

                                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                                val today = simpleDateFormat.format(Calendar.getInstance().time)

                                val item = MessageDetailResult()
                                item.tracking_no = trackingNo
                                item.question_seq_no = questionNo
                                item.title = sendTitle
                                item.message = sendMessage
                                item.sender_id = Preferences.userId
                                item.send_date = today
                                item.align = "right"

                                messageList.add(item)
                                messageDetailAdapter!!.notifyDataSetChanged()
                            } else {
                                Toast.makeText(
                                    this@CustomerMessageListDetailActivity,
                                    resources.getString(R.string.msg_send_message_error) +
                                            " : " + result.resultMsg,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "Message",
                                    "SendQdriverMessage  ResultCode : " + result.resultCode
                                )
                            }
                        } else {

                            Toast.makeText(
                                this@CustomerMessageListDetailActivity,
                                "${resources.getString(R.string.msg_send_message_error)} ${
                                    resources.getString(R.string.msg_please_try_again)
                                }",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Message", "SendQdriverMessage  result null")
                        }
                    }
                } catch (e: Exception) {

                    Toast.makeText(
                        this@CustomerMessageListDetailActivity,
                        "${resources.getString(R.string.msg_send_message_error)} ${
                            resources.getString(R.string.msg_please_try_again)
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Exception", "$TAG SendQdriverMessage Exception : $e")
                }

                progressBar.visibility = View.GONE
            }, {

                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@CustomerMessageListDetailActivity,
                    resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Exception", "$TAG  GetQdriverMessageListFromMessenger Exception : $it")
            })
    }


    private fun showDialog(title: String, msg: String) {

        try {
            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle(title)
            alertBuilder.setMessage(msg)
            alertBuilder.setPositiveButton(resources.getString(R.string.button_close)) { dialog: DialogInterface, _ ->
                dialog.dismiss()
                finish()
            }
            alertBuilder.show()
        } catch (ignore: Exception) {

        }
    }
}