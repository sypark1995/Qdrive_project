package com.giosis.library.message

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.giosis.library.R
import com.giosis.library.databinding.FragmentMessageListBinding
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.NetworkUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

/**
 * @author krm0219
 */
class AdminMessageListFragment : Fragment() {

    lateinit var binding: FragmentMessageListBinding

    // 5 min refresh
    lateinit var handler: AsyncHandler
    private lateinit var adminThread: AdminThread

    var oldResultString: String = ""
    var newResultString: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentMessageListBinding.inflate(inflater, container, false)
        binding.layoutBottom.visibility = View.GONE
        return binding.root
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

    inner class AsyncHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            try {

                if (activity != null && !activity!!.isFinishing) {

                    oldResultString = newResultString

                    if (newResultString.isEmpty())
                        binding.progressBar.visibility = View.VISIBLE

                    RetrofitClient.instanceDynamic().requestGetMessageListFromAdmin()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({

                                if (oldResultString != "" && oldResultString.equals(newResultString, ignoreCase = true)) {

                                    Log.e(TAG, "$TAG  GetQdriverMessageListFromMessenger  EQUAL")
                                } else {
                                    if (it.resultObject != null) {

                                        newResultString = it.toString()
                                        val list = Gson().fromJson<ArrayList<MessageListResult>>(it.resultObject,
                                                object : TypeToken<ArrayList<MessageListResult>>() {}.type)

                                        if (0 < list.size) {

                                            binding.textEmpty.visibility = View.GONE

                                            binding.recyclerMessages.visibility = View.VISIBLE
                                            binding.recyclerMessages.adapter = MessageListAdapter("A", list)

                                            val decoration = DividerItemDecoration(activity, VERTICAL)
                                            binding.recyclerMessages.addItemDecoration(decoration)


                                            var count = 0

                                            for (i in list.indices) {
                                                if (list[i].read_yn == "N") {
                                                    count++
                                                }
                                            }

                                            (activity as MessageListActivity).setAdminNewImage(count)
                                        } else {

                                            binding.recyclerMessages.visibility = View.GONE
                                            binding.textEmpty.visibility = View.VISIBLE
                                            binding.textEmpty.text = resources.getString(R.string.text_empty)
                                        }
                                    }
                                }

                                binding.progressBar.visibility = View.GONE
                            }, {

                                binding.recyclerMessages.visibility = View.GONE
                                binding.textEmpty.visibility = View.VISIBLE
                                binding.textEmpty.text = resources.getString(R.string.text_error)
                                binding.progressBar.visibility = View.GONE

                                Log.e("Exception", "$TAG  GetQdriverMessageListFromMessenger Exception : $it")
                            })
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

        var TAG = "AdminMessageListFragment"
        const val SEND_ADMIN_START = 200
    }
}