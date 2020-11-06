package com.giosis.util.qdrive.settings

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.giosis.util.qdrive.barcodescanner.ManualHelper
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.settings.NoticeResults.NoticeItem
import com.giosis.util.qdrive.util.Custom_JsonParser
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.NetworkUtil
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_notice.layout_notice
import kotlinx.android.synthetic.main.activity_notice1.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONObject

class NoticeActivity : CommonActivity() {

    val tag = "NoticeActivity"
    private val context = MyApplication.getContext()
    private val userId = MyApplication.preferences.userId
    private val officeCode = MyApplication.preferences.officeCode

    private val progressBar = ProgressBar(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice1)


        text_top_title.text = context.resources.getString(R.string.text_title_notice)

        layout_top_back.setOnClickListener {

            finish()
        }


        progressBar.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout_notice.addView(progressBar)


        val noticeAsyncTask = NoticeAsyncTask()
        noticeAsyncTask.execute()
    }


    @SuppressLint("StaticFieldLeak")
    inner class NoticeAsyncTask : AsyncTask<Void, Void, NoticeResults>() {

        @SuppressLint("WrongThread")
        override fun doInBackground(vararg p0: Void?): NoticeResults {


            val result = NoticeResults()

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.resultCode = "-16"
                result.resultMsg = context.resources.getString(R.string.msg_network_connect_error_saved)

                return result
            }


            try {

                val job = JSONObject()
                job.accumulate("opId", userId)
                job.accumulate("officeCd", officeCode)
                job.accumulate("gubun", "LIST")
                job.accumulate("kind", "QSIGN")
                job.accumulate("page_no", 1)
                job.accumulate("page_size", 30)
                job.accumulate("nid", 0)
                job.accumulate("svc_nation_cd", DataUtil.nationCode)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", DataUtil.nationCode)

                Log.e(tag, "$userId  /  $officeCode")

                val methodName = "GetNoticeData"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(ManualHelper.MOBILE_SERVER_URL, methodName, job)
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                val jsonObject = JSONObject(jsonString)
                result.resultCode = jsonObject.getString("ResultCode")
                result.resultMsg = jsonObject.getString("ResultMsg")

                val jsonArray = jsonObject.getJSONArray("ResultObject")
                val noticeList = arrayListOf<NoticeItem>()

                for (i in 0 until jsonArray.length()) {

                    val resultObject = jsonArray.getJSONObject(i)
                    val noticeItem = NoticeItem()

                    noticeItem.seqNo = resultObject.getString("nid")
                    noticeItem.title = resultObject.getString("title")
                    noticeItem.date = resultObject.getString("reg_dt_short")
                    noticeList.add(noticeItem)
                }

                result.resultObject = noticeList
            } catch (e: Exception) {

                Log.e("krm0219", "Exception : $e")
                result.resultCode = "-15"
                result.resultMsg = java.lang.String.format(context.resources.getString(R.string.text_exception), e.toString())

            }

            return result
        }

        override fun onPostExecute(result: NoticeResults) {
            super.onPostExecute(result)

            progressBar.visibility = View.GONE

            if (result.resultCode == "0") {

                recycler_notice.visibility = View.VISIBLE
                layout_notice_reload.visibility = View.GONE

                val adapter = NoticeAdapter(context, result.resultObject)
                recycler_notice.adapter = adapter

                // RecyclerView 구분선 색상 지정
                val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                dividerItemDecoration.setDrawable(context.resources.getDrawable(R.drawable.bg_rect_ebebeb))
                recycler_notice.addItemDecoration(dividerItemDecoration)
            } else {

                recycler_notice.visibility = View.GONE
                layout_notice_reload.visibility = View.VISIBLE
            }
        }
    }
}