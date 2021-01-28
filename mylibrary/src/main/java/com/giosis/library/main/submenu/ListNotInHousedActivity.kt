package com.giosis.library.main.submenu

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.server.Custom_JsonParser
import com.giosis.library.util.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_not_in_housed.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONObject

/**
 * @author krm0219  2018.07.26
 */
class ListNotInHousedActivity : CommonActivity() {
    var tag = "ListNotInHousedActivity"


    lateinit var listNotInHousedAdapter: ListNotInHousedAdapter

    lateinit var opID: String
    lateinit var officeCode: String
    lateinit var deviceID: String
    lateinit var networkType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_in_housed)


        //
        opID = Preferences.userId
        officeCode = Preferences.officeCode
        deviceID = Preferences.deviceUUID
        networkType = NetworkUtil.getNetworkType(this@ListNotInHousedActivity)

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

            val asyncTask = NotInHousedServerDownloadAsyncTask()
            asyncTask.execute()
        } else {

            Toast.makeText(this@ListNotInHousedActivity, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show()
        }
    }

    private inner class NotInHousedServerDownloadAsyncTask() : AsyncTask<Void?, Void?, NotInHousedResult?>() {
        var progressDialog = ProgressDialog(this@ListNotInHousedActivity)

        override fun onPreExecute() {

            super.onPreExecute()
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage(resources.getString(R.string.text_please_wait))
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): NotInHousedResult? {

            val resultObj: NotInHousedResult?

            resultObj = try {

                val job = JSONObject()
                job.accumulate("opId", opID)
                job.accumulate("officeCd", officeCode)
                job.accumulate("device_id", deviceID)
                job.accumulate("network_type", networkType)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", Preferences.userNation)

                val methodName = "GetOutStandingInhousedPickupList"

                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                Gson().fromJson(jsonString, NotInHousedResult::class.java)
            } catch (e: Exception) {

                Log.e("Exception", "$tag  GetOutStandingInhousedPickupList Json Exception : $e")
                null
            }

            return resultObj
        }

        override fun onPostExecute(result: NotInHousedResult?) {
            super.onPostExecute(result)

            DisplayUtil.dismissProgressDialog(progressDialog)

            try {

                if (result!!.resultObject!!.isEmpty()) {

                    text_not_in_housed_empty!!.text = resources.getString(R.string.text_empty)
                    text_not_in_housed_empty!!.visibility = View.VISIBLE
                    exlist_not_in_housed_list!!.visibility = View.GONE
                } else {

                    text_not_in_housed_empty!!.visibility = View.GONE
                    exlist_not_in_housed_list!!.visibility = View.VISIBLE

                    listNotInHousedAdapter = ListNotInHousedAdapter(result)
                    exlist_not_in_housed_list!!.setAdapter(listNotInHousedAdapter)
                }
            } catch (e: Exception) {

                Log.e("Exception", "$tag  onPostExecute Exception : $e")
                text_not_in_housed_empty!!.text = resources.getString(R.string.msg_please_try_again)
                text_not_in_housed_empty!!.visibility = View.VISIBLE
                exlist_not_in_housed_list!!.visibility = View.GONE
            }
        }
    }
}