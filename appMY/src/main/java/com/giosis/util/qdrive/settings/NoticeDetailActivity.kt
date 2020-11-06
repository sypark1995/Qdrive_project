package com.giosis.util.qdrive.settings

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.giosis.util.qdrive.barcodescanner.ManualHelper
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.util.Custom_JsonParser
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.NetworkUtil
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_notice_detail.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONObject

class NoticeDetailActivity : CommonActivity() {

    val tag = "NoticeDetailActivity"
    private val context = MyApplication.getContext()
    private val userId = MyApplication.preferences.userId
    private val officeCode = MyApplication.preferences.officeCode

    private val progressBar = ProgressBar(context)

    lateinit var seqNo: String
    lateinit var seqNextNo: String
    lateinit var seqPrevNo: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)


        text_top_title.text = context.resources.getString(R.string.text_title_notice)

        progressBar.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout_notice_progress.addView(progressBar)
        layout_notice_progress.visibility = View.VISIBLE


        layout_top_back.setOnClickListener {

            finish()
        }

        layout_notice_detail_prev.setOnClickListener {

            seqNo = seqPrevNo
            val noticeDetailAsyncTask = NoticeDetailAsyncTask(seqNo)
            noticeDetailAsyncTask.execute()
        }

        layout_notice_detail_next.setOnClickListener {

            seqNo = seqNextNo
            val noticeDetailAsyncTask = NoticeDetailAsyncTask(seqNo)
            noticeDetailAsyncTask.execute()
        }

        btn_notice_detail_reload.setOnClickListener {

            val noticeDetailAsyncTask = NoticeDetailAsyncTask(seqNo)
            noticeDetailAsyncTask.execute()
        }


        seqNo = intent.getStringExtra("notice_no")
        val noticeDetailAsyncTask = NoticeDetailAsyncTask(seqNo)
        noticeDetailAsyncTask.execute()
    }


    @SuppressLint("StaticFieldLeak")
    inner class NoticeDetailAsyncTask(private val seqNo: String) : AsyncTask<Void, Void, NoticeResults>() {

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
                job.accumulate("gubun", "DETAIL")
                job.accumulate("kind", "QSIGN")
                job.accumulate("page_no", 0)
                job.accumulate("page_size", 0)
                job.accumulate("nid", seqNo)
                job.accumulate("svc_nation_cd", DataUtil.nationCode)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", DataUtil.nationCode)

                Log.e(tag, "$userId  /  $officeCode / $seqNo")

                val methodName = "GetNoticeData"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(ManualHelper.MOBILE_SERVER_URL, methodName, job)
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                val jsonObject = JSONObject(jsonString)
                result.resultCode = jsonObject.getString("ResultCode")
                result.resultMsg = jsonObject.getString("ResultMsg")

                val jsonArray = jsonObject.getJSONArray("ResultObject")
                val resultObject = jsonArray.getJSONObject(0)

                val noticeList = arrayListOf<NoticeResults.NoticeItem>()
                val noticeItem = NoticeResults.NoticeItem()

                noticeItem.seqNo = resultObject.getString("nid")
                noticeItem.date = resultObject.getString("reg_dt_long")
                noticeItem.content = resultObject.getString("title")
                noticeItem.prevNo = resultObject.getString("prevnid")
                noticeItem.nextNo = resultObject.getString("nextnid")
                noticeList.add(noticeItem)

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

                layout_notice_detail.visibility = View.VISIBLE
                layout_notice_detail_reload.visibility = View.GONE

                text_notice_detail_content.text = result.resultObject[0].content
                text_notice_detail_date.text = result.resultObject[0].date


                if (result.resultObject[0].nextNo.isEmpty()) {

                    layout_notice_detail_next.visibility = View.GONE
                } else {

                    layout_notice_detail_next.visibility = View.VISIBLE
                    seqNextNo = result.resultObject[0].nextNo
                }

                if (result.resultObject[0].prevNo.isEmpty()) {

                    layout_notice_detail_prev.visibility = View.GONE
                } else {

                    layout_notice_detail_prev.visibility = View.VISIBLE
                    seqPrevNo = result.resultObject[0].prevNo
                }
            } else {

                layout_notice_detail.visibility = View.GONE
                layout_notice_detail_reload.visibility = View.VISIBLE
            }
        }
    }
}