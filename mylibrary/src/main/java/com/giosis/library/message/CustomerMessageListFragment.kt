package com.giosis.library.message


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.*


// todo_kjyoo 1분에 한번씩 돌면서 먼짓하는지??
class CustomerMessageListFragment : Fragment() {

    var TAG = "CustomerMessageListFragment"
    lateinit var binding: FragmentMessageListBinding

    // 1 min refresh
    val handler = Handler(Looper.getMainLooper())

    private val task = object : Runnable {
        override fun run() {
            callServer()
            handler.postDelayed(this, 60 * 1000)
        }
    }

    private var currentPage = 1
    private var totalPage = 1
    private var oldResultString: String = ""
    private var newResultString: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMessageListBinding.inflate(inflater, container, false)

        binding.layoutPrev.setOnClickListener {

            if (!NetworkUtil.isNetworkAvailable(activity)) {
                showDialog(
                    resources.getString(R.string.text_warning),
                    resources.getString(R.string.msg_network_connect_error)
                )
            } else {
                if (currentPage > 1) {

                    currentPage -= 1
                    callServer()
                } else {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.text_first_page),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.layoutNext.setOnClickListener {

            if (!NetworkUtil.isNetworkAvailable(activity)) {
                showDialog(
                    resources.getString(R.string.text_warning),
                    resources.getString(R.string.msg_network_connect_error)
                )
            } else {
                if (totalPage >= currentPage + 1) {

                    currentPage += 1
                    callServer()
                } else {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.text_last_page),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (!NetworkUtil.isNetworkAvailable(activity)) {

            showDialog(
                resources.getString(R.string.text_warning),
                resources.getString(R.string.msg_network_connect_error)
            )
        } else {

            handler.post(task)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun callServer() {
        Log.e(TAG, "callServer ---- GetMessageListFromCustomer")

        if (activity != null && !requireActivity().isFinishing) {

            oldResultString = newResultString

            if (newResultString.isEmpty())
                binding.progressBar.visibility = View.VISIBLE

            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.DATE, -14) // 최근 2주

            val yDate = cal.time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val startDate = dateFormat.format(yDate).toString() + " 00:00:00"
            val endDate = dateFormat.format(Date()).toString() + " 23:59:59"

            RetrofitClient.instanceDynamic().requestGetMessageListFromCustomer(
                currentPage.toString(),
                "15",
                startDate,
                endDate,
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (oldResultString != ""
                        && oldResultString.equals(newResultString, ignoreCase = true)
                    ) {
                        Log.e(TAG, "  GetQdriverMessageList  EQUAL")

                    } else {

                        if (it.resultObject != null) {

                            newResultString = it.toString()
                            val list = Gson().fromJson<ArrayList<MessageListResult>>(
                                it.resultObject,
                                object : TypeToken<ArrayList<MessageListResult>>() {}.type
                            )

                            if (0 < list.size) {

                                binding.textEmpty.visibility = View.GONE

                                binding.layoutBottom.visibility = View.VISIBLE
                                binding.recyclerMessages.visibility = View.VISIBLE
                                binding.recyclerMessages.adapter = MessageListAdapter("C", list)

                                val decoration = DividerItemDecoration(activity, VERTICAL)
                                binding.recyclerMessages.addItemDecoration(decoration)


                                totalPage = list[0].total_page_size
                                binding.textCurrentPage.text = currentPage.toString()
                                binding.textTotalPage.text = totalPage.toString()

                                var count = 0

                                for (i in list.indices) {
                                    if (list[i].read_yn == "N") {
                                        count++
                                    }
                                }
                                (activity as MessageListActivity).setCustomerNewImage(count)

                            } else {
                                binding.recyclerMessages.visibility = View.GONE
                                binding.layoutBottom.visibility = View.GONE
                                binding.textEmpty.visibility = View.VISIBLE
                                binding.textEmpty.text = resources.getString(R.string.text_empty)
                            }
                        }
                    }

                    binding.progressBar.visibility = View.GONE
                }, {

                    binding.recyclerMessages.visibility = View.GONE
                    binding.layoutBottom.visibility = View.GONE
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = resources.getString(R.string.text_error)

                    binding.progressBar.visibility = View.GONE
                    Log.e("Exception", "$TAG  GetQdriverMessageList Exception : $it")
                })
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


    private fun showDialog(title: String, msg: String) {

        try {
            val alertBuilder = AlertDialog.Builder(activity)
            alertBuilder.setTitle(title)
            alertBuilder.setMessage(msg)
            alertBuilder.setPositiveButton(resources.getString(R.string.button_close)) { dialog: DialogInterface, _ ->

                dialog.dismiss()
                requireActivity().finish()
            }
            alertBuilder.show()
        } catch (ignore: Exception) {
        }
    }
}