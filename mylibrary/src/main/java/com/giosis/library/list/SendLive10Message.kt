package com.giosis.library.list

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.util.Log
import com.giosis.library.R
import com.giosis.library.barcodescanner.StdResult
import com.giosis.library.server.Custom_JsonParser
import com.giosis.library.util.DataUtil
import com.giosis.library.util.NetworkUtil
import com.giosis.library.util.Preferences
import org.json.JSONObject

class SendLive10Message(val mContext: Context) {

    // Qtalk 메시지 선택 창
    fun dialogSelectOption(mContext: Context, qlps_cust_no: String, delivery_type: String,
                           order_type: String, tracking_no: String, seller_id: String) {

        val Pickup_items = arrayOf(
                mContext.resources.getString(R.string.msg_qpost_pickup1),
                mContext.resources.getString(R.string.msg_qpost_pickup2)
        )
        val Delivery_items = arrayOf(
                mContext.resources.getString(R.string.msg_qpost_delivery1),
                mContext.resources.getString(R.string.msg_qpost_delivery2)
        )
        val delivery_qtalk_message_array = arrayOf(
                mContext.resources.getString(R.string.msg_qpost_delivery_1),
                mContext.resources.getString(R.string.msg_qpost_delivery_2)
        )
        val pickup_qtalk_message_array = arrayOf(
                mContext.resources.getString(R.string.msg_qpost_pickup_1),
                mContext.resources.getString(R.string.msg_qpost_pickup_2)
        )

        val ab = AlertDialog.Builder(mContext)
        ab.setTitle(mContext.resources.getString(R.string.text_qpost_auto_message))
        ab.setSingleChoiceItems(if (delivery_type == "P") Pickup_items else Delivery_items, -1
        ) { dialog: DialogInterface, whichButton: Int ->
            val lv = (dialog as AlertDialog).listView
            lv.tag = whichButton
        }
                .setPositiveButton(mContext.resources.getString(R.string.button_ok)) { dialog: DialogInterface, whichButton: Int ->
                    //
                    val lv = (dialog as AlertDialog).listView
                    val selected = lv.tag as Int

                    var msg = if (delivery_type == "P")
                        pickup_qtalk_message_array[selected]
                    else
                        delivery_qtalk_message_array[selected]

                    msg = String.format(msg, seller_id, tracking_no, Preferences.userName)
                    val qtalkParams = arrayOf(qlps_cust_no, delivery_type, order_type, tracking_no, "SG", msg, Preferences.userId)

                    val sendLive10MessageTask = SendLive10MessageTask(mContext)
                    sendLive10MessageTask.execute(*qtalkParams)

                }.setNegativeButton(mContext.resources.getString(R.string.button_cancel)
                ) { dialog: DialogInterface?, whichButton: Int ->

                }
        ab.show()
    }

    class SendLive10MessageTask(val mContext: Context) : AsyncTask<String?, Void?, StdResult>() {

        override fun doInBackground(vararg params: String?): StdResult? {
            return sendLive10Msg(params[0], params[1], params[2], params[3], params[4], params[5], params[6])
        }

        override fun onPostExecute(result: StdResult?) {
            if (result != null) {
                val resultCode = result.resultCode
                val resultMsg = result.resultMsg
                if (resultCode != 0) {
                    val builder = AlertDialog.Builder(mContext)
                    builder.setTitle(mContext.resources.getString(R.string.text_alert))
                    builder.setMessage(resultMsg)
                    builder.setPositiveButton(mContext.resources.getString(R.string.button_ok)) { dialog, which ->
                        dialog.cancel()
                    }
                    val alertDialog = builder.create()
                    alertDialog.show()
                }
            }
        }

        private fun sendLive10Msg(qlps_cust_no: String?, delivery_type: String?, order_type: String?,
                                  tracking_no: String?, svc_nation_cd: String?, msg: String?, qsign_id: String?): StdResult? {

            val stdResult = StdResult()
            if (!NetworkUtil.isNetworkAvailable(mContext)) {
                stdResult.resultCode = -16
                stdResult.resultMsg = mContext.resources.getString(R.string.msg_network_connect_error_saved)
                return stdResult
            }
            try {
                val job = JSONObject()
                job.accumulate("qlps_cust_no", qlps_cust_no)
                job.accumulate("delivery_type", delivery_type)
                job.accumulate("order_type", order_type)
                job.accumulate("tracking_no", tracking_no)
                job.accumulate("svc_nation_cd", svc_nation_cd)
                job.accumulate("msg", msg)
                job.accumulate("qsign_id", qsign_id)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", Preferences.userNation)

                Log.e("Server", "SendLive10Message  DATA : " + qlps_cust_no + " / " + delivery_type + " / " + order_type + " / " + tracking_no
                        + " / " + svc_nation_cd + " / " + msg + " / " + qsign_id)

                val methodName = "SetSendQtalkMessagebyQsign"
                val jsonString: String = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                // {"ResultCode":-99,"ResultMsg":"Cannot send a content-body with this verb-type."}
                // {"ResultCode":-10,"ResultMsg":"The buyer is not Qtalk user."}

                val jsonObject = JSONObject(jsonString)
                stdResult.resultCode = jsonObject.getInt("ResultCode")
                stdResult.resultMsg = jsonObject.getString("ResultMsg")

            } catch (e: Exception) {
                Log.e("Exception", " SendLive10Message SetSendQtalkMessagebyQsign Exception : $e")
                val msg1: String = java.lang.String.format(mContext.resources.getString(R.string.text_exception), e.toString())
                stdResult.resultCode = -15
                stdResult.resultMsg = msg1
            }
            return stdResult
        }
    }
}