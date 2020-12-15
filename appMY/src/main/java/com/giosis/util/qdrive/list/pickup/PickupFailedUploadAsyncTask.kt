package com.giosis.util.qdrive.list.pickup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.giosis.library.server.ImageUpload
import com.giosis.util.qdrive.barcodescanner.StdResult
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.international.onEventListner
import com.giosis.util.qdrive.util.*
import kotlinx.android.synthetic.main.custom_progress_dialog.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
class PickupFailedUploadAsyncTask(val activity: Activity,
                                  val pickupNo: String, val failedReason: String, val retryDay: String,
                                  val memo: String, val imageView: ImageView,
                                  val diskSize: Long, val latitude: Double, val longitude: Double,
                                  val eventListener: onEventListner?) : AsyncTask<Void, Int, StdResult>() {


    lateinit var dialog: Dialog
    var status = 0

    override fun onPreExecute() {
        super.onPreExecute()

        dialog = DisplayUtil.showProgressDialog(activity)
        dialog.text_custom_progress_dialog_title.text = activity.resources.getString(R.string.text_set_transfer)
        dialog.progress_custom_progress_dialog.max = 1
        dialog.text_custom_progress_dialog_status.text = "0/${dialog.progress_custom_progress_dialog.max}"
        dialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    override fun doInBackground(vararg params: Void?): StdResult? {

        DataUtil.captureSign("/QdrivePickup", pickupNo, imageView)


        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date()

        val contentValues = ContentValues()
        contentValues.put("stat", "PF")
        contentValues.put("real_qty", "0")
        contentValues.put("rcv_type", "VL")
        contentValues.put("chg_id", MyApplication.preferences.userId)
        contentValues.put("chg_dt", dateFormat.format(date))
        contentValues.put("fail_reason", failedReason)
        contentValues.put("driver_memo", memo)
        contentValues.put("retry_dt", retryDay)

        DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentValues,
                "invoice_no=? COLLATE NOCASE and reg_id=?", arrayOf(pickupNo, MyApplication.preferences.userId))


        val result = StdResult()

        if (!NetworkUtil.isNetworkAvailable(activity)) {

            result.resultCode = -16
            result.resultMsg = activity.resources.getString(R.string.msg_network_connect_error_saved)
            return result
        }

        try {

            imageView.buildDrawingCache()
            val captureBitmap = imageView.getDrawingCache()
            val bitmapString = DataUtil.bitmapToString(captureBitmap, ImageUpload.QXPOP, "qdriver/sign", pickupNo)

            if (bitmapString == "") {
                result.resultCode = -100
                result.resultMsg = activity.resources.getString(R.string.msg_upload_fail_image)
                return result
            }


            val job = JSONObject()
            job.accumulate("stat", "PF")
            job.accumulate("rcv_type", "VL")
            job.accumulate("opId", MyApplication.preferences.userId)
            job.accumulate("officeCd", MyApplication.preferences.officeCode)
            job.accumulate("device_id", MyApplication.preferences.deviceUUID)
            job.accumulate("network_type", NetworkUtil.getNetworkType(activity))
            job.accumulate("disk_size", diskSize)
            job.accumulate("chg_id", MyApplication.preferences.userId)
            job.accumulate("deliv_msg", "(by Qdrive RalTime-Upload")
            job.accumulate("no_songjang", pickupNo)
            job.accumulate("real_qty", "0")
            job.accumulate("fail_reason", failedReason)
            job.accumulate("retry_day", retryDay)
            job.accumulate("remark", memo)
            job.accumulate("fileData", bitmapString)
            job.accumulate("fileData2", "")
            job.accumulate("lat", latitude)
            job.accumulate("lon", longitude)
            job.accumulate("app_id", DataUtil.appID)
            job.accumulate("nation_cd", DataUtil.nationCode)


            val methodName = "SetPickupUploadData"
            val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)

            val jsonObject = JSONObject(jsonString)
            result.resultCode = jsonObject.getInt("ResultCode")
            result.resultMsg = jsonObject.getString("ResultMsg")

            if (jsonObject.getInt("ResultCode") == 0) {

                val contentValues2 = ContentValues()
                contentValues2.put("punchOut_stat", "S")

                DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentValues2,
                        "invoice_no=? COLLATE NOCASE and reg_id=?", arrayOf(pickupNo, MyApplication.preferences.userId))
            }

            publishProgress(1)
        } catch (e: Exception) {

            Log.e("Exception", "  PickupFailedUploadAsyncTask  Exception : $e")

            val msg = String.format(activity.resources.getString(R.string.text_exception), e.toString())
            result.resultCode = -15
            result.resultMsg = msg
        }

        return result
    }


    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        status += values[0]!!
        dialog.progress_custom_progress_dialog.progress = status
        dialog.text_custom_progress_dialog_status.text = "$status/${dialog.progress_custom_progress_dialog.max}"
    }

    override fun onPostExecute(result: StdResult) {
        super.onPostExecute(result)

        dialog.dismiss()


        if (result.resultCode == 0) {

            val msg = String.format(activity.resources.getString(R.string.text_upload_success_count), 1)
            DisplayUtil.serverResultDialog(activity, msg, eventListener)
        } else if (result.resultCode < 0) {        //  실패

            if (result.resultCode == -14) {

                result.resultMsg = activity.resources.getString(R.string.msg_upload_fail_14)
                val msg = String.format(activity.resources.getString(R.string.text_upload_fail_count), 0, 1, result.resultMsg)
                DisplayUtil.serverResultDialog(activity, msg, eventListener)
            } else if (result.resultCode == -15) {

                result.resultMsg = activity.resources.getString(R.string.msg_upload_fail_15)
                val msg = String.format(activity.resources.getString(R.string.text_upload_fail_count), 0, 1, result.resultMsg)
                DisplayUtil.serverResultDialog(activity, msg, eventListener)
            } else {

                DisplayUtil.serverResultDialog(activity, result.resultMsg, eventListener)
            }
        }
    }
}