package com.giosis.library.message

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.message.MessageDetailResult.MessageDetailList
import com.giosis.library.server.Custom_JsonParser
import com.giosis.library.util.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_message_detail.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author krm0219
 */
class AdminMessageListDetailActivity : CommonActivity() {
    var tag = "AdminMessageListDetailActivity"

    var messageDetailAdapter: MessageDetailAdapter? = null
    var handler: AsyncHandler? = null
    private var adminThread: AdminThread? = null

    var senderID: String? = null

    var oldResultString: String? = null
    var newResultString: String? = null
    var sendMessage: String = ""

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        layout_top_back.setOnClickListener(clickListener)
        layout_message_detail_send.setOnClickListener(clickListener)
        list_message_detail_message.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL // message 입력 후, List 최하단으로 이동

        //
        senderID = intent.getStringExtra("sender_id")

        text_top_title.text = senderID
        text_message_detail_title.visibility = View.GONE

        edit_message_detail_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                layout_message_detail_send.setBackgroundResource(R.drawable.btn_send_qpost)
                edit_message_detail_input.hint = ""
            }
        })

        edit_message_detail_input.setOnTouchListener { _, _ ->

            layout_message_detail_send.setBackgroundResource(R.drawable.btn_send_qpost)
            edit_message_detail_input.hint = ""
            false
        }
    }


    override fun onResume() {
        super.onResume()

        if (!NetworkUtil.isNetworkAvailable(this@AdminMessageListDetailActivity)) {
            try {
                showDialog(resources.getString(R.string.text_warning), resources.getString(R.string.msg_network_connect_error))
            } catch (e: Exception) {
            }
            return
        } else {

            handler = AsyncHandler()
            adminThread = AdminThread()
            adminThread!!.start()
        }
    }


    inner class AsyncHandler : Handler() {
        override fun handleMessage(msg: Message) {
            try {
                if (!isFinishing) {
                    val adminMessageDetailAsyncTask = AdminMessageDetailAsyncTask(senderID)
                    adminMessageDetailAsyncTask.execute()
                }
            } catch (e: Exception) {
                Log.e("Exception", "$tag  AsyncHandler Exception : $e")
            }
        }
    }


    // NOTIFICATION.  AdminThread
    inner class AdminThread : Thread() {
        override fun run() {
            super.run()

            while (!currentThread().isInterrupted) {

                try {

                    val message = handler!!.obtainMessage()
                    message.what = SEND_ADMIN_START
                    handler!!.sendMessage(message)

                    sleep(5 * 60 * 1000.toLong())
                } catch (e: InterruptedException) {

                    Log.e("Exception", "$tag  AdminThread Exception : $e")
                    currentThread().interrupt()
                    e.printStackTrace()
                }
            }

            Log.e("Message", "$tag  AdminThread while break")
        }
    }


    override fun onStop() {
        super.onStop()
        try {
            adminThread!!.interrupt()
        } catch (e: Exception) {
            Thread.currentThread().interrupt()
        }
    }


    //NOTIFICATION.
    private inner class AdminMessageDetailAsyncTask(var sender_id: String?) : AsyncTask<Void?, Void?, MessageDetailResult?>() {

        var progressDialog = ProgressDialog(this@AdminMessageListDetailActivity)

        override fun onPreExecute() {
            super.onPreExecute()

            oldResultString = newResultString

            if (newResultString == null) {

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progressDialog.setMessage(resources.getString(R.string.text_please_wait))
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun doInBackground(vararg params: Void?): MessageDetailResult? {

            var resultObj: MessageDetailResult?

            try {

                val job = JSONObject()
                job.accumulate("qdriver_id", Preferences.userId)
                job.accumulate("senderID", sender_id)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", Preferences.userNation)

                val methodName = "GetQdriverMessageDetailFromMessenger"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                newResultString = jsonString

                resultObj = Gson().fromJson(jsonString, MessageDetailResult::class.java)
            } catch (e: Exception) {

                Log.e("Exception", "$tag  GetQdriverMessageDetailFromMessenger Json Exception : $e")
                resultObj = null
            }

            return resultObj
        }

        override fun onPostExecute(result: MessageDetailResult?) {
            super.onPostExecute(result)

            DisplayUtil.dismissProgressDialog(progressDialog)

            try {

                if (oldResultString != null && oldResultString.equals(newResultString, ignoreCase = true)) {

                    Log.e("Message", "$tag  GetQdriverMessageDetailFromMessenger  EQUAL")
                } else {

                    if (result != null) {

                        messageDetailList = result.resultObject as ArrayList<MessageDetailList>
                        if (messageDetailList!!.size > 0) {

                            for (i in messageDetailList!!.indices) {        // 초 second 제거
                                var dateString = messageDetailList!![i].getSend_date()
                                val dateSplitArray = dateString.split(":".toRegex()).toTypedArray()
                                dateString = dateSplitArray[0] + ":" + dateSplitArray[1]
                                messageDetailList!![i].setSend_date(dateString)
                            }

                            messageDetailAdapter = MessageDetailAdapter(this@AdminMessageListDetailActivity, messageDetailList, "A")
                            list_message_detail_message.adapter = messageDetailAdapter
                            list_message_detail_message.setSelection(list_message_detail_message.count - 1)
                        } else {

                            Toast.makeText(this@AdminMessageListDetailActivity, resources.getString(R.string.text_empty), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {

                Toast.makeText(this@AdminMessageListDetailActivity, resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show()
                Log.e("Exception", "$tag GetQdriverMessageDetailFromMessenger Exception : $e")
            }
        }
    }


    private fun sendChatMessage() {

        sendMessage = edit_message_detail_input.text.toString().trim { it <= ' ' }

        if (sendMessage == "") {
            Toast.makeText(this@AdminMessageListDetailActivity, resources.getString(R.string.msg_enter_message), Toast.LENGTH_SHORT).show()
            return
        }

        val sendMessageAdminAsyncTask = SendMessageAdminAsyncTask(sendMessage, senderID)
        sendMessageAdminAsyncTask.execute()
    }

    //NOTIFICATION.
    private inner class SendMessageAdminAsyncTask(var contents: String, var sender_id: String?) : AsyncTask<Void?, Void?, MessageSendResult?>() {

        var progressDialog = ProgressDialog(this@AdminMessageListDetailActivity)

        override fun onPreExecute() {
            super.onPreExecute()

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage(resources.getString(R.string.text_send_message))
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): MessageSendResult? {

            val resultObj: MessageSendResult?

            resultObj = try {

                val job = JSONObject()
                job.accumulate("contents", contents)
                job.accumulate("driver_id", Preferences.userId)
                job.accumulate("sender_id", sender_id)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", Preferences.userNation)

                val methodName = "SendQdriveToMessengerMessage"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                Gson().fromJson(jsonString, MessageSendResult::class.java)
            } catch (e: Exception) {

                Log.e("Exception", "$tag  SendQdriveToMessengerMessage Json Exception : $e")
                null
            }

            return resultObj
        }

        override fun onPostExecute(result: MessageSendResult?) {

            super.onPostExecute(result)
            DisplayUtil.dismissProgressDialog(progressDialog)

            try {
                if (result != null) {

                    layout_message_detail_send.setBackgroundResource(R.drawable.btn_send_qpost)
                    edit_message_detail_input.setHint(R.string.msg_qpost_edit_text_hint)
                    edit_message_detail_input.setText("")

                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    val today = simpleDateFormat.format(Calendar.getInstance().time)
                    val item = MessageDetailList()

                    item.setMessage(sendMessage)
                    item.setSender_id(sender_id)
                    item.setReceive_id(Preferences.userId)
                    item.setSend_date(today)
                    item.setAlign("right")

                    messageDetailList!!.add(item)
                    messageDetailAdapter!!.notifyDataSetChanged()
                    Log.e("Message", "SendQdriveToMessengerMessage Size : " + messageDetailList!!.size)
                } else {

                    Toast.makeText(this@AdminMessageListDetailActivity, "${resources.getString(R.string.msg_send_message_error)} ${resources.getString(R.string.msg_please_try_again)}", Toast.LENGTH_SHORT).show()
                    Log.e("Message", "SendQdriveToMessengerMessage  result null")
                }
            } catch (e: Exception) {

                Toast.makeText(this@AdminMessageListDetailActivity, "${resources.getString(R.string.msg_send_message_error)} ${resources.getString(R.string.msg_please_try_again)}", Toast.LENGTH_SHORT).show()
                Log.e("Exception", "$tag SendQdriveToMessengerMessage Exception : $e")
            }
        }

        init {
            Log.e("Message", "$tag  SendQdriveToMessengerMessage  $contents  ${Preferences.userId}  $sender_id")
        }
    }

    var clickListener = View.OnClickListener { view ->

        val id = view.id

        if (id == R.id.layout_top_back) {

            finish()
        } else if (id == R.id.layout_message_detail_send) {

            if (!NetworkUtil.isNetworkAvailable(this@AdminMessageListDetailActivity)) {
                try {
                    showDialog(resources.getString(R.string.text_warning), resources.getString(R.string.msg_network_connect_error))
                } catch (e: Exception) {
                }
                return@OnClickListener
            } else {
                sendChatMessage()
            }
        }
    }


    private fun showDialog(title: String?, msg: String?) {

        val alert = AlertDialog.Builder(this@AdminMessageListDetailActivity)
        alert.setTitle(title)
        alert.setMessage(msg)
        alert.setPositiveButton(resources.getString(R.string.button_close)
        ) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        alert.show()
    }


    companion object {
        private var messageDetailList: ArrayList<MessageDetailList>? = null
        const val SEND_ADMIN_START = 200
    }
}