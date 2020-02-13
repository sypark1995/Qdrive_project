package com.giosis.util.qdrive.qdelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.giosis.util.qdrive.barcodescanner.ManualHelper
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.Custom_JsonParser
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.DisplayUtil
import com.giosis.util.qdrive.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_qdelivery_step4.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Field
import java.util.*


class QDeliveryStep4Activity : AppCompatActivity() {

    lateinit var context: Context

    var sizeArrayList = ArrayList<String>()
    lateinit var sizeArrayAdapter: ArrayAdapter<String>

    val packingCostResultArrayList = ArrayList<PackingCostResult.PackingCost>()
    var weightArrayList = ArrayList<String>()
    lateinit var weightArrayAdapter: ArrayAdapter<String>

    var selectedSize = ""
    var selectedWeight = ""


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qdelivery_step4)

        text_top_title.setText(R.string.text_request_qdelivery)
        layout_top_back.setOnClickListener {

            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        }


        //
        context = applicationContext
        val qDeliveryData = intent.getSerializableExtra(DataUtil.qDeliveryData) as QDeliveryData



        spinner_qd_step4_size.prompt = "Select"
        sizeArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizeArrayList)
        sizeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_qd_step4_size.adapter = sizeArrayAdapter

        spinner_qd_step4_weight.prompt = "Select"
        weightArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weightArrayList)
        weightArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_qd_step4_weight.adapter = weightArrayAdapter


        spinner_qd_step4_size.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedSize = parent?.getItemAtPosition(position).toString()
                text_qd_step4_size.text = selectedSize
            }
        }


        spinner_qd_step4_weight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedWeight = parent?.getItemAtPosition(position).toString()
                text_qd_step4_weight.text = selectedWeight

                text_qd_step4_shipping_cost.text = packingCostResultArrayList[position]._cost
            }
        }

        // Spinner 높이 지정
        try {

            val popup: Field = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val popupWindow = popup.get(spinner_qd_step4_weight) as ListPopupWindow
            popupWindow.height = DisplayUtil.dpTopx(context, 200f)
        } catch (e: java.lang.Exception) { // silently fail...
        }

        val packingCostAsyncTask = PackingCostAsyncTask()
        packingCostAsyncTask.execute()


        layout_qd_step4_size.setOnClickListener {

            spinner_qd_step4_size.performClick()
        }

        layout_qd_step4_weight.setOnClickListener {

            spinner_qd_step4_weight.performClick()
        }

        btn_qd_step4_order_place.setOnClickListener {

            val itemName = edit_qd_step4_item_name.text.toString().trim()
            val itemPrice = edit_qd_step4_item_price.text.toString().trim()
            val itemDefinition = edit_qd_step4_item_definition.text.toString().trim()

            if (itemName.isEmpty()) {

                Toast.makeText(this, "item Name 필수!", Toast.LENGTH_SHORT).show()
            } else if (itemPrice.isEmpty()) {

                Toast.makeText(this, "item Price 필수!", Toast.LENGTH_SHORT).show()
            } else {

                qDeliveryData.itemName = itemName
                qDeliveryData.itemPrice = itemPrice

                if (itemDefinition.isNotEmpty()) {

                    qDeliveryData.itemDefinition = itemDefinition
                }

                qDeliveryData.itemSize = text_qd_step4_size.text as String
                qDeliveryData.itemWeight = text_qd_step4_weight.text as String
                qDeliveryData.estimatePrice = text_qd_step4_shipping_cost.text as String

                Log.e("krm0219", "ITEM $itemName")
                // TODO
                val intent = Intent(this, QDeliveryStep5Activity::class.java)
                intent.putExtra(DataUtil.qDeliveryData, qDeliveryData)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class PackingCostAsyncTask : AsyncTask<Void, Void, PackingCostResult>() {

        override fun onPreExecute() {
            super.onPreExecute()

            progress_qd_step4.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): PackingCostResult {

            val result = PackingCostResult()

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
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(ManualHelper.MOBILE_SERVER_URL, methodName, job)
                // {"ResultObject":[{"total_cnt":null,"nid":"158577","kind":"","title":"hello","link":"","priority":"","reg_dt_short":"Apr 3","reg_dt_long":"Apr 3, 2:02 PM","rownum":null,"contents":"","nextnid":"158578","prevnid":"158575"}],"ResultCode":0,"ResultMsg":"SUCCESS"}

                val jsonObject = JSONObject(jsonString)
                var resultArray = jsonObject.getJSONArray("ResultObject")

                // TEST.
                val testString = "[{\"weight\":\"0Kg ~ 5Kg\",\"cost\":\"3.88\"}, {\"weight\":\"5Kg ~ 10Kg\",\"cost\":\"5.08\"}, " +
                        "{\"weight\":\"10Kg ~ 15Kg\",\"cost\":\"7.28\"}, {\"weight\":\"15Kg ~ 20Kg\",\"cost\":\"9.48\"}, " +
                        "{\"weight\":\"20Kg ~ 25Kg\",\"cost\":\"11.68\"}, {\"weight\":\"25Kg ~ 30Kg\",\"cost\":\"13.88\"}]"
                resultArray = JSONArray(testString)

                val resultCode = jsonObject.getInt("ResultCode")
                val resultMsg = jsonObject.getString("ResultMsg")
                result.resultCode = resultCode
                result.resultMsg = resultMsg

                val resultSize = "0cm ~ 160cm"
                result.packingSize = resultSize

                for (i in 0 until resultArray.length()) {

                    val item = resultArray.getJSONObject(i)

                    val packingCostData = PackingCostResult.PackingCost(item.getString("weight"), item.getString("cost"))
                    packingCostResultArrayList.add(packingCostData)
                }

                result.resultObject = packingCostResultArrayList
            } catch (e: Exception) {

                Log.e("krm0219", "Exception : $e")
                result.resultCode = -15
                result.resultMsg = java.lang.String.format(context.resources.getString(R.string.text_exception), e.toString())
            }

            return result
        }

        override fun onPostExecute(result: PackingCostResult?) {
            super.onPostExecute(result)

            progress_qd_step4.visibility = View.GONE

            if (result != null) {
                if (result.resultCode == 0) {

                    sizeArrayList.clear()
                    weightArrayList.clear()

                    sizeArrayList.add(result.packingSize)
                    sizeArrayAdapter.notifyDataSetChanged()

                    for (item in result.resultObject) {

                        weightArrayList.add(item._weight)
                    }
                    weightArrayAdapter.notifyDataSetChanged()
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