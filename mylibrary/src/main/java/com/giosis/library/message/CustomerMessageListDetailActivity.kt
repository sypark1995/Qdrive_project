package com.giosis.library.message

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.giosis.library.R
import com.giosis.library.databinding.ActivityMessageDetailBinding
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.*
import com.giosis.library.util.dialog.ProgressDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author krm0219
 */
class CustomerMessageListDetailActivity : CommonActivity() {
    var TAG = "CustomerMessageListDetailActivity"

    private val binding by lazy {
        ActivityMessageDetailBinding.inflate(layoutInflater)
    }

    val progressBar by lazy {
        ProgressDialog(this@CustomerMessageListDetailActivity)
    }

    var messageDetailAdapter: MessageDetailAdapter? = null
    var handler: AsyncHandler? = null
    var customerThread: CustomerThread? = null

    var oldResultString: String = ""
    var newResultString: String = ""
    var messageList: ArrayList<MessageDetailResult> = ArrayList()

    var questionNo: String = "0"
    var trackingNo: String = ""
    private var sendTitle: String = ""
    private var sendMessage: String = ""


    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //
        questionNo = intent.getIntExtra("question_no", 0).toString() // 최초 0
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
                try {
                    showDialog(resources.getString(R.string.text_warning), resources.getString(R.string.msg_network_connect_error))
                } catch (ignored: Exception) {
                }
            } else {
                sendChatMessage()
            }
        }

        binding.listDetailMessage.adapter?.let { binding.listDetailMessage.smoothScrollToPosition(it.itemCount) }
    }


    override fun onResume() {
        super.onResume()

        if (!NetworkUtil.isNetworkAvailable(this)) {
            try {
                showDialog(resources.getString(R.string.text_warning), resources.getString(R.string.msg_network_connect_error))
            } catch (ignored: Exception) {
            }
        } else if (questionNo == "0") {

            Log.e(TAG, "in LIST")

            progressBar.visibility = View.VISIBLE
            RetrofitClient.instanceDynamic().requestGetMessageToQPostOnPickupMenu(trackingNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        progressBar.visibility = View.GONE

                        if (it.resultObject != null) {

                            val list = Gson().fromJson<ArrayList<MessageQuestionNumberResult>>(it.resultObject, object : TypeToken<ArrayList<MessageQuestionNumberResult>>() {}.type)

                            questionNo = "0"

                            if (list != null && list.isNotEmpty()) {
                                if (0 < list[0].questionNo)
                                    questionNo = list[0].questionNo.toString()
                            }

                            handler = AsyncHandler()
                            customerThread = CustomerThread()
                            customerThread!!.start()
                        }
                    }, {

                        progressBar.visibility = View.GONE
                        Toast.makeText(this@CustomerMessageListDetailActivity, resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show()
                        Log.e("Exception", "$TAG  GetMessageToQPostOnPickupMenu Exception : $it")
                    })
        } else {

            handler = AsyncHandler()
            customerThread = CustomerThread()
            customerThread!!.start()
        }
    }


    inner class AsyncHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            try {
                if (!isFinishing) {

                    oldResultString = newResultString

                    if (newResultString.isEmpty())
                        progressBar.visibility = View.VISIBLE

                    RetrofitClient.instanceDynamic().requestGetQdriverMessageDetail(questionNo)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({

                                if (oldResultString != "" && oldResultString.equals(newResultString, ignoreCase = true)) {

                                    Log.e("Message", " GetQdriverMessageDetail    EQUAL")
                                } else {

                                    if (it.resultObject?.isJsonNull == false && it.resultObject != null) {

                                        newResultString = it.toString()
                                        messageList = Gson().fromJson(it.resultObject, object : TypeToken<ArrayList<MessageDetailResult>>() {}.type)
                                        if (0 < messageList.size) {

                                            for (i in messageList.indices) {

                                                var dateString = messageList[i].send_date
                                                val dateSplitArray = dateString.split(":".toRegex()).toTypedArray()
                                                dateString = dateSplitArray[0] + ":" + dateSplitArray[1]
                                                messageList[i].send_date = dateString
                                            }

                                            binding.textMessageTitle.text = messageList[0].title
                                            messageDetailAdapter = MessageDetailAdapter(this@CustomerMessageListDetailActivity, messageList, "C")
                                            binding.listDetailMessage.adapter = messageDetailAdapter
                                        } else {

                                            binding.textMessageTitle.text = resources.getString(R.string.text_qxpress_driver)
                                            messageList = ArrayList()
                                            messageDetailAdapter = MessageDetailAdapter(this@CustomerMessageListDetailActivity, messageList, "C")
                                            binding.listDetailMessage.adapter = messageDetailAdapter
                                        }
                                    } else {

                                        binding.textMessageTitle.text = resources.getString(R.string.text_qxpress_driver)
                                        messageList = ArrayList()
                                        messageDetailAdapter = MessageDetailAdapter(this@CustomerMessageListDetailActivity, messageList, "C")
                                        binding.listDetailMessage.adapter = messageDetailAdapter
                                    }
                                }

                                progressBar.visibility = View.GONE
                            }, {

                                progressBar.visibility = View.GONE
                                Toast.makeText(this@CustomerMessageListDetailActivity, resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show()
                                Log.e("Exception", "$TAG GetQdriverMessageDetail Exception : $it")
                            })
                }
            } catch (e: Exception) {
                Log.e("krm0219", "$TAG  AsyncHandler Exception : $e")
            }
        }
    }


    // NOTIFICATION.   CustomerThread
    inner class CustomerThread : Thread() {
        override fun run() {
            super.run()

            while (!currentThread().isInterrupted) {
                try {

                    val message = handler!!.obtainMessage()
                    message.what = SEND_CUTOMER_START
                    handler!!.sendMessage(message)

                    sleep((60 * 1000).toLong())
                } catch (e: InterruptedException) {

                    Log.e("Exception", "$TAG  CustomerThread Exception : $e")
                    currentThread().interrupt()
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            customerThread!!.interrupt()
        } catch (e: Exception) {
            Thread.currentThread().interrupt()
        }
    }


    private fun sendChatMessage() {

        sendTitle = binding.textMessageTitle.text.toString().trim { it <= ' ' }
        sendMessage = binding.editMessage.text.toString().trim { it <= ' ' }

        if (sendMessage == "") {
            Toast.makeText(this@CustomerMessageListDetailActivity, resources.getString(R.string.msg_enter_message), Toast.LENGTH_SHORT).show()
            return
        }


        progressBar.visibility = View.VISIBLE
        RetrofitClient.instanceDynamic().requestSendQdriverMessage(trackingNo, sendTitle, sendMessage, questionNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {
                        if (it.resultObject != null) {

                            val result = Gson().fromJson<MessageSendResult>(it.resultObject,
                                    object : TypeToken<MessageSendResult>() {}.type)

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
                                    Toast.makeText(this@CustomerMessageListDetailActivity, resources.getString(R.string.msg_send_message_error) +
                                            " : " + result.resultMsg, Toast.LENGTH_SHORT).show()
                                    Log.e("Message", "SendQdriverMessage  ResultCode : " + result.resultCode)
                                }
                            } else {

                                Toast.makeText(this@CustomerMessageListDetailActivity, "${resources.getString(R.string.msg_send_message_error)} ${resources.getString(R.string.msg_please_try_again)}", Toast.LENGTH_SHORT).show()
                                Log.e("Message", "SendQdriverMessage  result null")
                            }
                        }
                    } catch (e: Exception) {

                        Toast.makeText(this@CustomerMessageListDetailActivity, "${resources.getString(R.string.msg_send_message_error)} ${resources.getString(R.string.msg_please_try_again)}", Toast.LENGTH_SHORT).show()
                        Log.e("Exception", "$TAG SendQdriverMessage Exception : $e")
                    }

                    progressBar.visibility = View.GONE
                }, {

                    progressBar.visibility = View.GONE
                    Toast.makeText(this@CustomerMessageListDetailActivity, resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show()
                    Log.e("Exception", "${AdminMessageListFragment.TAG}  GetQdriverMessageListFromMessenger Exception : $it")
                })
    }


    private fun showDialog(title: String?, msg: String?) {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(resources.getString(R.string.button_close)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            finish()
        }
        alertBuilder.show()
    }

    companion object {
        const val SEND_CUTOMER_START = 100
    }
}