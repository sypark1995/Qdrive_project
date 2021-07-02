package com.giosis.library.main.submenu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.databinding.ActivityNotInHousedBinding
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.NotInHousedResult
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.NetworkUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

/**
 * @author krm0219  2018.07.26
 */
class ListNotInHousedActivity : CommonActivity() {
    var tag = "ListNotInHousedActivity"

    private val binding by lazy {
        ActivityNotInHousedBinding.inflate(layoutInflater)
    }

    lateinit var listNotInHousedAdapter: ListNotInHousedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.layoutTopTitle.textTopTitle.setText(R.string.navi_sub_not_in_housed)
        binding.layoutTopTitle.layoutTopBack.setOnClickListener { finish() }

        binding.exlistList.setOnGroupExpandListener { groupPosition: Int ->

            val groupCount = listNotInHousedAdapter.groupCount

            for (i in 0 until groupCount) {

                if (i != groupPosition) binding.exlistList.collapseGroup(i)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (NetworkUtil.isNetworkAvailable(this@ListNotInHousedActivity)) {

            binding.progressBar.visibility = View.VISIBLE
            RetrofitClient.instanceDynamic().requestGetOutStandingPickupList(NetworkUtil.getNetworkType(this@ListNotInHousedActivity))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        val list = Gson().fromJson<ArrayList<NotInHousedResult>>(it.resultObject,
                                object : TypeToken<ArrayList<NotInHousedResult>>() {}.type)

                        if (list.isEmpty()) {

                            binding.textEmpty.text = resources.getString(R.string.text_empty)
                            binding.textEmpty.visibility = View.VISIBLE
                            binding.exlistList.visibility = View.GONE
                        } else {

                            binding.textEmpty.visibility = View.GONE
                            binding.exlistList.visibility = View.VISIBLE

                            listNotInHousedAdapter = ListNotInHousedAdapter(list)
                            binding.exlistList.setAdapter(listNotInHousedAdapter)
                        }

                        binding.progressBar.visibility = View.GONE
                    }, {

                        binding.progressBar.visibility = View.GONE

                        binding.textEmpty.text = resources.getString(R.string.msg_please_try_again)
                        binding.textEmpty.visibility = View.VISIBLE
                        binding.exlistList.visibility = View.GONE
                    })
        } else {

            Toast.makeText(this@ListNotInHousedActivity, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show()
        }
    }
}