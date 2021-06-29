package com.giosis.library.main.submenu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.NetworkUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_not_in_housed.*
import kotlinx.android.synthetic.main.top_title.*
import java.util.*

/**
 * @author krm0219  2018.07.26
 */
class ListNotInHousedActivity : CommonActivity() {
    var tag = "ListNotInHousedActivity"


    lateinit var listNotInHousedAdapter: ListNotInHousedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_in_housed)


        text_top_title.setText(R.string.navi_sub_not_in_housed)
        layout_top_back.setOnClickListener { finish() }

        exlist_not_in_housed_list.setOnGroupExpandListener { groupPosition: Int ->

            val groupCount = listNotInHousedAdapter.groupCount

            for (i in 0 until groupCount) {

                if (i != groupPosition) exlist_not_in_housed_list.collapseGroup(i)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (NetworkUtil.isNetworkAvailable(this@ListNotInHousedActivity)) {

            progressBar.visibility = View.VISIBLE
            RetrofitClient.instanceDynamic().requestGetOutStandingPickupList(NetworkUtil.getNetworkType(this@ListNotInHousedActivity))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        val list = Gson().fromJson<ArrayList<NotInHousedResult>>(it.resultObject,
                                object : TypeToken<ArrayList<NotInHousedResult>?>() {}.type)

                        if (list.isEmpty()) {

                            text_not_in_housed_empty.text = resources.getString(R.string.text_empty)
                            text_not_in_housed_empty.visibility = View.VISIBLE
                            exlist_not_in_housed_list.visibility = View.GONE
                        } else {

                            text_not_in_housed_empty.visibility = View.GONE
                            exlist_not_in_housed_list.visibility = View.VISIBLE

                            listNotInHousedAdapter = ListNotInHousedAdapter(list)
                            exlist_not_in_housed_list.setAdapter(listNotInHousedAdapter)
                        }

                        progressBar.visibility = View.GONE
                    }, {

                        progressBar.visibility = View.GONE

                        text_not_in_housed_empty.text = resources.getString(R.string.msg_please_try_again)
                        text_not_in_housed_empty.visibility = View.VISIBLE
                        exlist_not_in_housed_list.visibility = View.GONE
                    })
        } else {

            Toast.makeText(this@ListNotInHousedActivity, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show()
        }
    }
}