package com.giosis.library.message

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.giosis.library.R
import com.giosis.library.message.MessageListResult.MessageList
import com.giosis.library.server.Custom_JsonParser
import com.giosis.library.util.DataUtil
import com.giosis.library.util.DisplayUtil
import com.giosis.library.util.NetworkUtil
import com.giosis.library.util.Preferences
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_message_list.*
import kotlinx.android.synthetic.main.fragment_message_list.view.*
import org.json.JSONObject
import java.util.*

/**
 * @author krm0219
 */
class AdminMessageListFragment : Fragment() {
    var TAG = "AdminMessageListFragment"


    var messageListAdapter: MessageListAdapter? = null

    // 5 min refresh
    lateinit var handler: AsyncHandler
    private lateinit var adminThread: AdminThread

    var oldResultString: String = ""
    var newResultString: String = ""

    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_message_list, container, false)
        view.layout_message_list_bottom.visibility = View.GONE
        return view
    }


    override fun onResume() {
        super.onResume()

        if (!NetworkUtil.isNetworkAvailable(activity)) {
            try {
                showDialog(resources.getString(R.string.text_warning), resources.getString(R.string.msg_network_connect_error))
            } catch (e: Exception) {
            }
            return
        } else {

            handler = AsyncHandler()
            adminThread = AdminThread()
            adminThread.start()
        }
    }

    inner class AsyncHandler : Handler() {
        override fun handleMessage(msg: Message) {
            try {

                if (activity != null && !activity!!.isFinishing) {

                    val adminMessageListAsyncTask = AdminMessageListAsyncTask()
                    adminMessageListAsyncTask.execute()
                } else {

                    Log.e("Message", "$TAG  getActivity().isFinishing()")
                }
            } catch (e: Exception) {

                Log.e("Exception", "$TAG  AsyncHandler Exception : $e")
            }
        }
    }


    // NOTIFICATION.   AdminThread
    inner class AdminThread : Thread() {
        override fun run() {
            super.run()

            while (!currentThread().isInterrupted) {

                try {

                    val message = handler.obtainMessage()
                    message.what = SEND_ADMIN_START
                    handler.sendMessage(message)

                    sleep(5 * 60 * 1000.toLong())
                } catch (e: InterruptedException) {

                    Log.e("Exception", "$TAG  AdminThread Exception : $e")
                    currentThread().interrupt()
                    e.printStackTrace()
                }
            }

            Log.e("Message", "$TAG  AdminThread while break")
        }
    }


    override fun onStop() {
        super.onStop()

        try {

            adminThread.interrupt()
        } catch (e: Exception) {

            Thread.currentThread().interrupt()
        }
    }


    //NOTIFICATION.  AdminMessageListAsyncTask
    inner class AdminMessageListAsyncTask() : AsyncTask<Void?, Void?, MessageListResult?>() {

        var progressDialog = ProgressDialog(activity)

        override fun onPreExecute() {
            super.onPreExecute()

            oldResultString = newResultString

            if (newResultString == "") {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progressDialog.setMessage(resources.getString(R.string.text_please_wait))
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun doInBackground(vararg params: Void?): MessageListResult? {

            var resultObj: MessageListResult?

            try {

                val job = JSONObject()
                job.accumulate("qdriver_id", Preferences.userId)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", Preferences.userNation)

                val methodName = "GetQdriverMessageListFromMessenger"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                newResultString = jsonString

                resultObj = Gson().fromJson(jsonString, MessageListResult::class.java)
            } catch (e: Exception) {

                Log.e("Exception", "$TAG  GetQdriverMessageListFromMessenger Json Exception : $e")
                resultObj = null
            }

            return resultObj
        }

        override fun onPostExecute(result: MessageListResult?) {
            super.onPostExecute(result)

            DisplayUtil.dismissProgressDialog(progressDialog)

            try {

                if (oldResultString != "" && oldResultString.equals(newResultString, ignoreCase = true)) {
                    Log.e("Message", "$TAG  AdminMessageListAsyncTask  EQUAL")
                } else {

                    if (result != null) {

                        messageList = result.resultObject as ArrayList<MessageList>

                        if (messageList.size > 0) {

                            list_message_list.visibility = View.VISIBLE
                            text_message_list_empty.visibility = View.GONE

                            messageListAdapter = MessageListAdapter(activity, "A", messageList)
                            list_message_list.adapter = messageListAdapter

                            var count = 0

                            for (i in messageList.indices) {
                                if (messageList[i].getRead_yn() == "N") {
                                    count++
                                }
                            }

                            (activity as MessageListActivity?)!!.setAdminNewImage(count)
                        } else {

                            list_message_list.visibility = View.GONE
                            text_message_list_empty.visibility = View.VISIBLE

                            text_message_list_empty.text = resources.getString(R.string.text_empty)
                        }
                    }
                }
            } catch (e: Exception) {

                list_message_list.visibility = View.GONE
                text_message_list_empty.visibility = View.VISIBLE
                text_message_list_empty.text = resources.getString(R.string.text_error)
                Toast.makeText(activity, resources.getString(R.string.text_error) + "!! " + resources.getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show()
                Log.e("Exception", "$TAG  AdminMessageListAsyncTask Exception : $e")
            }
        }
    }

    private fun showDialog(title: String?, msg: String?) {

        val alert = AlertDialog.Builder(activity)
        alert.setTitle(title)
        alert.setMessage(msg)
        alert.setPositiveButton(resources.getString(R.string.button_close)
        ) { dialog, _ ->

            dialog.dismiss()
            requireActivity().finish()
        }
        alert.show()
    }


    companion object {

        private lateinit var messageList: ArrayList<MessageList>
        const val SEND_ADMIN_START = 200
    }
}