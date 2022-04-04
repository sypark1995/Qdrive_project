package com.giosis.library.main

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.giosis.library.R
import com.giosis.library.UploadData
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.QSignDeliveryList
import com.giosis.library.server.data.QSignPickupList
import com.giosis.library.server.data.RestDaysResult
import com.giosis.library.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object MainActivityServer {
    const val TAG = "MainDownload"

    const val PFC = "PFC"
    const val DFC = "DFC"

    fun download(context: Context) {

        val progressDialog = ProgressDialog(context)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setMessage(context.resources.getString(R.string.text_downloading))
        progressDialog.setCancelable(false)

        progressDialog.show()

        (context as MainActivity).lifecycleScope.launch {
            // 2020.12  Failed Code 가져오기
            getDFCFailedCode()
            getPFCFailedCode()
        }

        context.lifecycleScope.launch(Dispatchers.IO) {
            //  2020.02 휴무일 가져오기
            val delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_REST_DAYS, "")
            Log.i(TAG, "DELETE  DB_TABLE_REST_DAYS  Count : $delete")

            getRestDay(Calendar.getInstance()[Calendar.YEAR])
            getRestDay(Calendar.getInstance()[Calendar.YEAR] + 1)
        }

        context.lifecycleScope.launch(Dispatchers.IO) {

            try {
                DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "")
            } catch (e: java.lang.Exception) {
                try {
                    val params = Bundle()
                    params.putString("Activity", TAG)
                    params.putString("message", " DB.delete > $e")
                    DataUtil.mFirebaseAnalytics.logEvent("error_exception", params)
                } catch (ignored: java.lang.Exception) {
                }
            }

            var pickupList = ArrayList<QSignPickupList>()
            var deliveryList = ArrayList<QSignDeliveryList>()
            var outletDeliveryList = ArrayList<QSignDeliveryList>()
            var errorMsg = ""

            withContext(Dispatchers.Default) {
                val network = NetworkUtil.getNetworkType(context)
                launch {
                    try {
                        val response =
                            RetrofitClient.instanceDynamic().requestGetPickupList(network)

                        if (response.resultCode >= 0) {
                            pickupList = Gson().fromJson(
                                response.resultObject,
                                object : TypeToken<ArrayList<QSignPickupList>>() {}.type
                            )

                        } else {
                            errorMsg += response.resultMsg
                        }

                    } catch (e: java.lang.Exception) {

                    }
                }

                launch {
                    try {
                        val response =
                            RetrofitClient.instanceDynamic().requestGetDeliveryList(network)

                        if (response.resultCode >= 0) {
                            deliveryList = Gson().fromJson(
                                response.resultObject,
                                object : TypeToken<ArrayList<QSignDeliveryList>>() {}.type
                            )
                        } else {
                            errorMsg += response.resultMsg
                        }
                    } catch (e: java.lang.Exception) {

                    }
                }

                launch {
                    if (Preferences.userNation == "SG") {
                        val response =
                            RetrofitClient.instanceDynamic().requestGetDeliveryOutlet(network)

                        if (response.resultCode >= 0) {
                            outletDeliveryList = Gson().fromJson(
                                response.resultObject,
                                object : TypeToken<ArrayList<QSignDeliveryList>>() {}.type
                            )
                        } else {

                            errorMsg += response.resultMsg
                        }
                    }
                }
            }

            val totalItem = pickupList.size + deliveryList.size + outletDeliveryList.size

            Log.e(TAG, "end api $errorMsg total cnt = $totalItem")

            progressDialog.max = totalItem
            var current = 0

            if (pickupList.size > 0) {
                withContext(Dispatchers.IO) {
                    for (item in pickupList) {
                        insertDevicePickupData(item)
                        progressDialog.progress = ++current
                    }
                }
            }

            if (deliveryList.size > 0) {
                withContext(Dispatchers.IO) {
                    for (item in deliveryList) {
                        insertDeviceDeliveryData(item)
                        progressDialog.progress = ++current
                    }
                }
            }

            if (outletDeliveryList.size > 0) {
                withContext(Dispatchers.IO) {
                    for (item in outletDeliveryList) {
                        insertDeviceDeliveryData(item)
                        progressDialog.progress = ++current
                    }
                }
            }

            if (pickupList.size + deliveryList.size + outletDeliveryList.size == 0) {
                errorMsg += context.getResources().getString(R.string.msg_no_data_to_download)
            }

            if (errorMsg.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    progressDialog.hide()

                    AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.text_download_result))
                        .setMessage(errorMsg)
                        .setCancelable(true)
                        .setPositiveButton(
                            context.getResources().getString(R.string.button_ok)
                        ) { dialog1: DialogInterface?, which: Int ->
                            if (dialog1 != null) {
                                if (!(context as Activity).isFinishing) {
                                    dialog1.dismiss()
                                }
                            }
                        }
                        .create()
                        .show()
                }
            }

            getLocalCount(context)

            withContext(Dispatchers.Main) {
                progressDialog.hide()
            }
        }
    }

    suspend fun getDFCFailedCode() {
        try {
            val result = RetrofitClient.instanceDynamic().requestGetFailedCode(DFC)

            if (result.resultCode == 10) {
                val json = Gson().toJson(result.resultObject)
                Log.i(TAG, "D  getFailedCode  $json")
                Preferences.dFailedCode = json
            }
        } catch (e: Exception) {

        }

    }

    suspend fun getPFCFailedCode() {
        try {
            val result = RetrofitClient.instanceDynamic().requestGetFailedCode(PFC)

            if (result.resultCode == 10) {
                val json = Gson().toJson(result.resultObject)
                Log.i(TAG, "P  getFailedCode  $json")
                Preferences.pFailedCode = json
            }
        } catch (e: Exception) {

        }
    }

    private suspend fun getRestDay(year: Int) {
        try {
            val response2 = RetrofitClient.instanceDynamic().requestGetRestDays(year)

            if (response2.resultCode == 0 && response2.resultObject != null) {
                val list = Gson().fromJson<ArrayList<RestDaysResult>>(
                    response2.resultObject,
                    object : TypeToken<ArrayList<RestDaysResult?>?>() {}.type
                )

                insertRestDays(list)
            }
        } catch (e: java.lang.Exception) {

        }
    }

    private fun insertRestDays(list: ArrayList<RestDaysResult>?) {
        var insert: Long = 0
        if (list != null) {
            for (data in list) {
                val contentValues = ContentValues().apply {
                    put("title", data.title)
                    put("rest_dt", data.rest_dt)
                }

                insert = DatabaseHelper.getInstance()
                    .insert(DatabaseHelper.DB_TABLE_REST_DAYS, contentValues)
            }
        }
        Log.i(TAG, "insertRestDays  DB Insert: $insert")
    }

    private fun insertDevicePickupData(data: QSignPickupList): Long {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val regDataString = dateFormat.format(Date())

        val contentVal = ContentValues().apply {
            put("contr_no", data.contrNo)

            // NOTIFICATION.  19/10 - Ref.Pickup No가 존재하면 리스트에서 해당 번호로 표시
            if (data.ref_pickup_no != "") {
                Log.e(TAG, "  Ref. Pickup No >> " + data.ref_pickup_no + " / " + data.partnerRefNo)
                put("partner_ref_no", data.ref_pickup_no)
            } else {
                put("partner_ref_no", data.partnerRefNo)
            }

            put("invoice_no", data.partnerRefNo) //invoice_no = partnerRefNo 사용
            put("stat", data.stat)
            put("tel_no", data.telNo)
            put("hp_no", data.hpNo)
            put("zip_code", data.zipCode)
            put("address", data.address)
            put("route", data.route)
            put("type", BarcodeType.TYPE_PICKUP)
            put("desired_date", data.pickupHopeDay)
            put("req_qty", data.qty)
            put("req_nm", data.reqName)
            put("rcv_request", data.delMemo)
            put("sender_nm", "")
            put("punchOut_stat", "N")
            put("reg_id", Preferences.userId)
            put("reg_dt", regDataString)
            put("fail_reason", data.failReason)
            put("secret_no_type", data.secretNoType)
            put("secret_no", data.secretNo)
            put("cust_no", data.custNo)
            put("partner_id", data.partnerID)

            if (data.route == "RPC") {
                put("desired_time", data.pickupHopeTime)
            }

            // 2020.06 위, 경도 저장
            val latLng = GeoCodeUtil.getLatLng(data.lat_lng)
            put("lat", latLng[0])
            put("lng", latLng[1])

            put("state", data.state)
            put("city", data.city)
            put("street", data.street)
        }

        return DatabaseHelper.getInstance()
            .insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal)
    }

    private fun insertDeviceDeliveryData(data: QSignDeliveryList): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val regDataString = dateFormat.format(Date())

        val contentVal = ContentValues().apply {
            put("contr_no", data.contr_no)
            put("partner_ref_no", data.partner_ref_no)
            put("invoice_no", data.invoice_no)
            put("stat", data.stat)
            put("rcv_nm", data.rcv_nm)
            put("sender_nm", data.sender_nm)
            put("tel_no", data.tel_no)
            put("hp_no", data.hp_no)
            put("zip_code", data.zip_code)
            put("address", data.address)
            put("rcv_request", data.del_memo)
            put("delivery_dt", data.delivery_first_date)
            put("type", BarcodeType.TYPE_DELIVERY)
            put("route", data.route)
            put("reg_id", Preferences.userId)
            put("reg_dt", regDataString)
            put("punchOut_stat", "N")
            put("driver_memo", data.driver_memo)
            put("fail_reason", data.fail_reason)
            put("secret_no_type", data.secret_no_type)
            put("secret_no", data.secret_no)
            put("secure_delivery_yn", data.secure_delivery_yn)
            put("parcel_amount", data.parcel_amount)
            put("currency", data.currency)
            put("order_type_etc", data.order_type_etc)

            // 2020.06 위, 경도 저장
            val latLng = GeoCodeUtil.getLatLng(data.lat_lng)
            put("lat", latLng[0])
            put("lng", latLng[1])

            // 2021.04  High Value
            put("high_amount_yn", data.high_amount_yn)
            // 2021.09 Economy

            put("order_type", data.order_type)
            put("state", data.state)
            put("city", data.city)
            put("street", data.street)
        }

        return DatabaseHelper.getInstance()
            .insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal)
    }

    class CountData {
        var inProgressCnt = 0
        var todayUploadedCnt = 0
        var uploadFailedCnt = 0
        var inProgressDeliveryCnt = 0
        var inProgressPickupCnt = 0
        var inProgressRpcCnt = 0
    }

    fun getLocalCount(activity: MainActivity) {

        activity.lifecycleScope.launch(Dispatchers.IO) {

            try {
                val selectQuery =
                    "select ifnull(sum(case when chg_dt is null then 1 else 0 end), 0) as InprogressCnt " + //In-Progress
                            " , ifnull(sum(case when punchOut_stat = 'S' and strftime('%Y-%m-%d', chg_dt) = date('now') then 1 else 0 end) ,0) as TodayUploadedCnt " +// Uploaded Today
                            " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is not null  then 1 else 0 end), 0) as UploadFailedCnt " + //Upload Failed
                            " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and type = 'D' then 1 else 0 end), 0) as InprogressDeliveryCnt " +//Delivery
                            " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and type = 'P' and route <> 'RPC' then 1 else 0 end), 0) as InprogressPickupCnt " + //Pickup
                            " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and route = 'RPC' then 1 else 0 end), 0) as InprogressRpcCnt " + //RPC
                            " , datetime(max(reg_dt), 'localtime') as PI_Time " +
                            " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                            " where reg_id= '" + Preferences.userId + "'"

                val cs = DatabaseHelper.getInstance().get(selectQuery)

                if (cs.moveToFirst()) {

                    val count = CountData().apply {
                        inProgressCnt = cs.getInt(cs.getColumnIndex("InprogressCnt"))
                        todayUploadedCnt = cs.getInt(cs.getColumnIndex("TodayUploadedCnt"))
                        uploadFailedCnt = cs.getInt(cs.getColumnIndex("UploadFailedCnt"))
                        inProgressDeliveryCnt =
                            cs.getInt(cs.getColumnIndex("InprogressDeliveryCnt"))
                        inProgressPickupCnt = cs.getInt(cs.getColumnIndex("InprogressPickupCnt"))
                        inProgressRpcCnt = cs.getInt(cs.getColumnIndex("InprogressRpcCnt"))
                    }

                    withContext(Dispatchers.Main) {
                        activity.text_home_total_qty.text =
                            (count.inProgressPickupCnt + count.todayUploadedCnt + count.uploadFailedCnt).toString()

                        activity.uploadFailedCount = count.uploadFailedCnt.toString()

                        activity.text_home_in_progress_count.text = count.inProgressCnt.toString()
                        activity.text_home_delivery_count.text =
                            count.inProgressDeliveryCnt.toString()
                        activity.text_home_pickup_count.text = count.inProgressPickupCnt.toString()
                        activity.text_home_rpc_count.text = count.inProgressRpcCnt.toString()

                        //파트너 Office Header - RPC Change Driver 버튼 설정
                        if (Preferences.default == "Y") {
                            if (count.inProgressRpcCnt.toString() != "0") {
                                activity.btn_home_assign_pickup_driver.visibility = View.VISIBLE
                            } else {
                                activity.btn_home_assign_pickup_driver.visibility = View.GONE
                            }
                        }
                        Log.e(TAG, "getLocalCount finish")
                    }
                }

            } catch (e: java.lang.Exception) {

            }

        }
    }

    fun setDestroyUserInfo(context: Context) {
        Log.e(TAG, "setDestroyUserInfo start ")
        CoroutineScope(Dispatchers.IO).launch {
            Log.e(TAG, "requestSetAppUserInfo2 start ")
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = dateFormat.format(Date())

                val response = RetrofitClient.instanceDynamic().requestSetAppUserInfo2(
                    "killapp", NetworkUtil.getNetworkType(context), "", date,
                )

                Log.e(TAG, "requestSetAppUserInfo2 called ${response.resultCode}")

                if (response.resultCode < 0) {
                    withContext(Dispatchers.Main) {
                        AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.text_upload_result))
                            .setMessage(response.resultMsg)
                            .setCancelable(true)
                            .setPositiveButton(
                                context.getResources().getString(R.string.button_ok)
                            ) { dialog1: DialogInterface?, which: Int ->
                                if (dialog1 != null) {
                                    if (!(context as Activity).isFinishing) {
                                        dialog1.dismiss()
                                    }
                                }
                            }
                            .create()
                            .show()
                    }
                }
            } catch (e: java.lang.Exception) {

            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.e(TAG, "requestSetDriverPerformanceLog start ")
            var accuracy = 0.0
            var latitude = 0.0
            var longitude = 0.0

            try {
                if ((context as MainActivity).gpsEnable && (context).gpsTrackerManager != null) {
                    latitude = (context).gpsTrackerManager.latitude
                    longitude = (context).gpsTrackerManager.longitude
                    accuracy = (context).gpsTrackerManager.accuracy
                }
            } catch (e: java.lang.Exception) {

            }

            val channel = if (Preferences.userNation == "SG") {
                "QDRIVE"
            } else {
                "QDRIVE_V2"
            }

            val response = RetrofitClient.instanceDynamic().requestSetDriverPerformanceLog(
                channel, latitude.toString(), longitude.toString(), accuracy.toString()
            )

            Log.e(TAG, "requestSetDriverPerformanceLog called ${response.resultCode}")

            if (response.resultCode < 0) {

                var resultMsg = response.resultMsg
                if (response.resultCode != -16) {
                    resultMsg = String.format(
                        context.getResources().getString(R.string.text_upload_failed_msg),
                        response.resultMsg
                    )
                }

                AlertDialog.Builder(context)
                    .setTitle(context.getResources().getString(R.string.text_upload_result))
                    .setMessage(resultMsg)
                    .setCancelable(true)
                    .setPositiveButton(
                        context.getResources().getString(R.string.button_ok)
                    ) { dialog1: DialogInterface?, which: Int ->
                        if (dialog1 != null) {
                            if (!(context as Activity).isFinishing) {
                                dialog1.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
            }
        }
        Log.e(TAG, "setDestroyUserInfo end ")
    }

    fun upload(context: Context) {
        (context as MainActivity).lifecycleScope.launch(Dispatchers.IO) {

            val songjangList = ArrayList<UploadData>()

            // 업로드 대상건 로컬 DB 조회
            val selectQuery = "select invoice_no" +
                    " , stat " +
                    " , ifnull(rcv_type, '')  as rcv_type" +
                    " , ifnull(fail_reason, '')  as fail_reason" +
                    " , ifnull(driver_memo, '') as driver_memo" +
                    " , ifnull(real_qty, '') as real_qty" +
                    " , ifnull(retry_dt , '') as retry_dt" +
                    " , type " +
                    " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                    " where reg_id= '" + Preferences.userId + "'" +
                    " and punchOut_stat <> 'S' and chg_dt is not null"

            val cs: Cursor = DatabaseHelper.getInstance().get(selectQuery)

            if (cs.moveToFirst()) {
                do {
                    val data = UploadData().apply {
                        noSongjang = cs.getString(cs.getColumnIndex("invoice_no"))
                        stat = cs.getString(cs.getColumnIndex("stat"))
                        receiveType = cs.getString(cs.getColumnIndex("rcv_type"))
                        failReason = cs.getString(cs.getColumnIndex("fail_reason"))
                        driverMemo = cs.getString(cs.getColumnIndex("driver_memo"))
                        realQty = cs.getString(cs.getColumnIndex("real_qty"))
                        retryDay = cs.getString(cs.getColumnIndex("retry_dt"))
                        type = cs.getString(cs.getColumnIndex("type"))
                    }

                    songjangList.add(data)
                } while (cs.moveToNext())
            }

            var latitude = 0.0
            var longitude = 0.0
            if ((context).gpsEnable && (context).gpsTrackerManager != null) {
                latitude = (context).gpsTrackerManager.latitude
                longitude = (context).gpsTrackerManager.longitude
            }

            for (item in songjangList) {
                if (item.noSongjang.isNotEmpty()) {
                    if (item.type == BarcodeType.TYPE_DELIVERY) {

                    } else if (item.type == BarcodeType.TYPE_PICKUP) {

                        var fileData = ""
                        var fileData2 = ""

                        // TODO_kjyoo upload

//                        val dirPath = Environment.getExternalStorageDirectory()
//                            .toString() + "/QdrivePickup"
//                        val filePath = dirPath + "/" + item.noSongjang + ".png"
//                        val imgFile = File(filePath)
//
//                        if (imgFile.exists()) {
//                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
//                            fileData = DataUtil.bitmapToString(
//                                context,
//                                myBitmap,
//                                ImageUpload.QXPOD,
//                                "qdriver/sign",
//                                item.noSongjang
//                            )
//
//                            if (fileData == "") {
//                                result.setResultCode(-100)
//                                result.setResultMsg(
//                                    context.getResources()
//                                        .getString(R.string.msg_upload_fail_image)
//                                )
//                                return result
//                            }
//                        } else {
//
//                        }
//
//                        if (item.stat == BarcodeType.PICKUP_FAILED) {
//
//                        } else {
//
//
//                        }

                        val result = RetrofitClient.instanceDynamic().requestSetPickupUploadData(
                            item.receiveType,
                            item.stat,
                            NetworkUtil.getNetworkType(context),
                            item.noSongjang,
                            fileData,
                            fileData2,
                            item.driverMemo,
                            latitude,
                            longitude,
                            item.realQty,
                            item.failReason,
                            item.retryDay,
                        )

                        if (result.resultCode == 0) {
                            val contentVal = ContentValues().apply {
                                put("punchOut_stat", "S")
                            }

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal,
                                "invoice_no=? COLLATE NOCASE and reg_id=?",
                                arrayOf(item.noSongjang, Preferences.userId)
                            )
                        }
                    }
                }
            }
        }
    }

}