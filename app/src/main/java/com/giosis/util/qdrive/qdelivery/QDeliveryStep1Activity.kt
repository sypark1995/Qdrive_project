package com.giosis.util.qdrive.qdelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Spinner
import com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.Custom_JsonParser
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.DisplayUtil
import com.giosis.util.qdrive.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_qdelivery_step1.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Field


class QDeliveryStep1Activity : AppCompatActivity() {

    lateinit var context: Context

    private lateinit var arrivalCountryArrayAdapter: ArrayAdapter<String>
    private val countryResultArrayList = mutableListOf<CountryResult.Country>()
    private var countryArrayList = ArrayList<String>()

    private var selectedArrivalCountry = ""


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qdelivery_step1)

        text_top_title.setText(R.string.text_request_qdelivery)
        layout_top_back.setOnClickListener {

            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        }


        //
        context = applicationContext
        val qDeliveryData = intent.getSerializableExtra(DataUtil.qDeliveryData) as QDeliveryData


        text_qd_step1_departure_country.text = qDeliveryData.fromNation

        layout_qd_step1_arrival_country.setOnClickListener {

            spinner_qd_step1_arrival_country.performClick()
        }

        btn_qd_step1_next.setOnClickListener {

            /*
            *    if (selectedArrivalCountry.equalsIgnoreCase("select")) {

                        Toast.makeText(QDeliveryStep1Activity.this, "No Pass", Toast.LENGTH_SHORT).show();
                    } else {*/

            try {

                qDeliveryData.toNation = text_qd_step1_arrival_country.text as String

                for (i in 0 until countryResultArrayList.size) {

                    if (text_qd_step1_arrival_country.text === countryResultArrayList[i]._country) {

                        qDeliveryData.toNationCode = countryResultArrayList[i]._countryCode
                        break
                    }
                }
            } catch (e: java.lang.Exception) {

                qDeliveryData.toNation = "Select"
                qDeliveryData.toNationCode = "+00"
            }


            val intent = Intent(this, QDeliveryStep2Activity::class.java)
            intent.putExtra(DataUtil.qDeliveryData, qDeliveryData)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }


        spinner_qd_step1_arrival_country.prompt = "Select"
        arrivalCountryArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countryArrayList)
        arrivalCountryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_qd_step1_arrival_country.adapter = arrivalCountryArrayAdapter

        spinner_qd_step1_arrival_country.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedArrivalCountry = parent?.getItemAtPosition(position).toString()
                text_qd_step1_arrival_country.text = selectedArrivalCountry

                if (selectedArrivalCountry.equals("select", false)) {

                    btn_qd_step1_next.setBackgroundResource(R.drawable.back_round_30_cccccc)
                    btn_qd_step1_next.setTextColor(resources.getColor(R.color.color_303030))
                } else {

                    btn_qd_step1_next.setBackgroundResource(R.drawable.back_round_30_4fb648)
                    btn_qd_step1_next.setTextColor(resources.getColor(R.color.white))
                }
            }
        }

        // Spinner 높이 지정
        try {

            val popup: Field = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val popupWindow = popup.get(spinner_qd_step1_arrival_country) as ListPopupWindow
            popupWindow.height = DisplayUtil.dpTopx(context, 200f)
        } catch (e: java.lang.Exception) { // silently fail...
        }

        val arrivalCountryAsyncTask = ArrivalCountryAsyncTask()
        arrivalCountryAsyncTask.execute()
    }

    @SuppressLint("StaticFieldLeak")
    inner class ArrivalCountryAsyncTask : AsyncTask<Void, Void, CountryResult>() {

        override fun onPreExecute() {
            super.onPreExecute()

            progress_qd_step1.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): CountryResult {

            val result = CountryResult()

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.resultCode = -16
                result.resultMsg = context.resources.getString(R.string.msg_network_connect_error_saved)

                return result
            }

            try {
                val job = JSONObject()
                job.accumulate("gubun", "LIST")
                job.accumulate("kind", "QSIGN")
                job.accumulate("page_no", 1)
                job.accumulate("page_size", 30)
                job.accumulate("nid", 0)
                job.accumulate("svc_nation_cd", "SG")
                job.accumulate("opId", "karam.kim")
                job.accumulate("officeCd", "0000")
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", DataUtil.nationCode)
                val methodName = "GetNoticeData"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job)
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                val jsonObject = JSONObject(jsonString)
                var resultArray = jsonObject.getJSONArray("ResultObject")

                // TEST.
                val testString = "[{\"country\":\"Korea\",\"countryCode\":\"+82\"}, {\"country\":\"Singapore\",\"countryCode\":\"+65\"}, " +
                        "{\"country\":\"Japan\",\"countryCode\":\"+12\"}, {\"country\":\"China\",\"countryCode\":\"+34\"}, " +
                        "{\"country\":\"Malaysia\",\"countryCode\":\"+56\"}, {\"country\":\"Indonesia\",\"countryCode\":\"+78\"}]"
                resultArray = JSONArray(testString)

                val resultCode = jsonObject.getInt("ResultCode")
                val resultMsg = jsonObject.getString("ResultMsg")
                result.resultCode = resultCode
                result.resultMsg = resultMsg


                val countryArrayList = ArrayList<CountryResult.Country>()

                for (i in 0 until resultArray.length()) {

                    val item = resultArray.getJSONObject(i)

                    val countryData = CountryResult.Country(item.getString("country"), item.getString("countryCode"))
                    countryArrayList.add(countryData)
                }

                result.resultObject = countryArrayList
            } catch (e: Exception) {

                Log.e("krm0219", "Exception : $e")
                result.resultCode = -15
                result.resultMsg = java.lang.String.format(context.resources.getString(R.string.text_exception), e.toString())
            }

            return result
        }

        override fun onPostExecute(result: CountryResult?) {
            super.onPostExecute(result)

            progress_qd_step1.visibility = View.GONE

            if (result != null) {
                if (result.resultCode == 0) {

                    countryResultArrayList.clear()
                    countryArrayList.clear()


                    for (item in result.resultObject) {

                        val country = item._country

                        Log.e("krm0219", "Country : $country")
                        countryResultArrayList.add(item)
                        countryArrayList.add(country)
                    }

                    arrivalCountryArrayAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }
}