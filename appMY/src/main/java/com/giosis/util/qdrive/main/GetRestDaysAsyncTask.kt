package com.giosis.util.qdrive.main

import android.content.ContentValues
import android.os.AsyncTask
import android.util.Log
import com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL
import com.giosis.util.qdrive.international.onEventListner
import com.giosis.util.qdrive.util.Custom_JsonParser
import com.giosis.util.qdrive.util.DatabaseHelper
import org.json.JSONObject

class GetRestDaysAsyncTask(private val nation: String, private val year: Int, private val eventListener: onEventListner?) : AsyncTask<Void, Void, String>() {


    override fun doInBackground(vararg params: Void?): String? {

        val databaseHelper = DatabaseHelper.getInstance()

        try {

            val job = JSONObject()
            job.accumulate("svc_nation_cd", nation)
            job.accumulate("year", year)
            job.accumulate("app_id", "QDRIVE")
            job.accumulate("nation_cd", nation)

            val methodName = "GetRestDays"
            val jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job)
            // {"ResultObject":[{"rest_dt":"2020-01-01","title":"New Years day"},{"rest_dt":"2020-01-25","title":"Chinese New Year"},{"rest_dt":"2020-01-26","title":"Chinese New Year day2"},{"rest_dt":"2020-01-27","title":"Chinese New Year replacement"},{"rest_dt":"2020-02-08","title":"Thaipusam"},{"rest_dt":"2020-05-01","title":"Labour Day"},{"rest_dt":"2020-05-07","title":"Wesak Day"},{"rest_dt":"2020-05-11","title":"Nuzul Al-Quran Holiday"},{"rest_dt":"2020-05-25","title":"Hari Raya Aidilfitri"},{"rest_dt":"2020-05-26","title":"Hari Raya Aidilfitri replacment"},{"rest_dt":"2020-06-06","title":"Agong\u0027s Birthday"},{"rest_dt":"2020-07-31","title":"Hari Raya Haji"},{"rest_dt":"2020-08-20","title":"Awal Muharram"},{"rest_dt":"2020-08-31","title":"Merdeka Day"},{"rest_dt":"2020-09-16","title":"Malaysia Day"},{"rest_dt":"2020-10-29","title":"Prophet Muhammad\u0027s Birthday"},{"rest_dt":"2020-11-14","title":"Deepavali"},{"rest_dt":"2020-12-11","title":"Sultan of Selangor\u0027s Birthday"},{"rest_dt":"2020-12-25","title":"Christmas"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

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

            return jsonObject.getString("ResultCode")
        } catch (e: Exception) {

        }
        return null
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result == "0") {

            eventListener?.onSuccess()
        } else {

            eventListener?.onFailure()
        }
    }
}