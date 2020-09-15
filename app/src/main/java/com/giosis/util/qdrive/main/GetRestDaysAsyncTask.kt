package com.giosis.util.qdrive.main

import android.content.ContentValues
import android.util.Log
import com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL
import com.giosis.util.qdrive.util.Custom_JsonParser
import com.giosis.util.qdrive.util.DatabaseHelper
import gmkt.inc.android.common.util.bitmap.AsyncTask
import org.json.JSONObject

class GetRestDaysAsyncTask(private val nation: String, private val year: Int) : AsyncTask<Void, Void, Void>() {


    override fun doInBackground(vararg params: Void?): Void? {

        val databaseHelper = DatabaseHelper.getInstance()

        try {

            val job = JSONObject()
            job.accumulate("svc_nation_cd", nation)
            job.accumulate("year", year)
            job.accumulate("app_id", "QDRIVE")
            job.accumulate("nation_cd", nation)

            val methodName = "GetRestDays"

            val jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job)
            // {"ResultObject":[{"rest_dt":"2020-01-01","title":"New Year\u0027s Day"},{"rest_dt":"2020-05-01","title":"Labour Day"},{"rest_dt":"2020-08-09","title":"National Day"},{"rest_dt":"2020-12-25","title":"Christmas"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

            val jsonObject = JSONObject(jsonString)
            val resultObject = jsonObject.getJSONArray("ResultObject")


            var insert = 0
            for (i in 0 until resultObject.length()) {

                val title = resultObject.getJSONObject(i).getString("title")
                val restDate = resultObject.getJSONObject(i).getString("rest_dt")


                val contentValues = ContentValues()
                contentValues.put("title", title)
                contentValues.put("rest_dt", restDate)


                insert = databaseHelper.insert(DatabaseHelper.DB_TABLE_REST_DAYS, contentValues).toInt()
            }

            Log.e("krm0219", "Rest Day   DB Insert: $insert")

        } catch (e: Exception) {

        }
        return null
    }
}