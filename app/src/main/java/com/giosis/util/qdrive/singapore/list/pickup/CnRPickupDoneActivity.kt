package com.giosis.util.qdrive.singapore.list.pickup

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.gps.LocationModel
import com.giosis.util.qdrive.singapore.list.BarcodeData
import com.giosis.util.qdrive.singapore.server.ImageUpload
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import kotlinx.android.synthetic.main.activity_pickup_done.*
import kotlinx.android.synthetic.main.top_title.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * SCAN > CNR DONE
 */

class CnRPickupDoneActivity : CommonActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
        )
    }

    var TAG = "CnRPickupDoneActivity"

    private val progressBar by lazy {
        ProgressDialog(this@CnRPickupDoneActivity)
    }

    private var mStrWaybillNo: String = ""
    private var mType = BarcodeType.PICKUP_CNR

    private var pickupNoList = ArrayList<BarcodeData>()

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    private var locationModel = LocationModel()


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_done)

        FirebaseEvent.createEvent(this, TAG)

        layout_top_back.setOnClickListener {
            cancelSigning()
        }

        layout_sign_p_applicant_eraser.setOnClickListener {
            sign_view_sign_p_applicant_signature!!.clearText()
        }

        layout_sign_p_collector_eraser.setOnClickListener {
            sign_view_sign_p_collector_signature!!.clearText()
        }

        btn_sign_p_save.setOnClickListener {
            saveServerUploadSign()
        }

        //
        val strSenderName = intent.getStringExtra("senderName")
        mStrWaybillNo = intent.getStringExtra("scannedList").toString()
        val strReqQty = intent.getStringExtra("scannedQty")


        text_sign_p_tracking_no_title.setText(R.string.text_pickup_no)
        text_sign_p_requester_title.setText(R.string.text_parcel_qty1)
        text_sign_p_request_qty_title.setText(R.string.text_applicant)

        var pickupBarcodeData: BarcodeData

        val mWaybillList = mStrWaybillNo.split(",".toRegex()).toTypedArray()

        for (s in mWaybillList) {

            val barcode = s.trim()
            pickupBarcodeData = BarcodeData()
            pickupBarcodeData.barcode = barcode
            pickupBarcodeData.state = mType
            pickupNoList.add(pickupBarcodeData)

            // 위, 경도
            if (strReqQty.equals("1")) {

                val cs: Cursor =
                    DatabaseHelper.getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcode + "'"]

                if (cs.moveToFirst()) {

                    val parcelLat: Double = cs.getDouble(cs.getColumnIndex("lat"))
                    val parcelLng: Double = cs.getDouble(cs.getColumnIndex("lng"))
                    val zipCode: String = cs.getString(cs.getColumnIndex("zip_code"))
                    val state: String = cs.getString(cs.getColumnIndex("state"))
                    val city: String = cs.getString(cs.getColumnIndex("city"))
                    val street: String = cs.getString(cs.getColumnIndex("street"))

                    locationModel.setParcelLocation(
                        parcelLat,
                        parcelLng,
                        zipCode,
                        state,
                        city,
                        street
                    )
                }
            }
        }

        val invoiceList = ArrayList<String>()

        for (item in pickupNoList) {
            invoiceList.add(item.barcode!!)
        }

        val qtyFormat =
            String.format(resources.getString(R.string.text_total_qty_count), invoiceList.size)

        text_sign_p_tracking_no.text = qtyFormat
        text_sign_p_tracking_no_more.visibility = View.VISIBLE
        text_sign_p_tracking_no_more.text = TextUtils.join(",", invoiceList)

        text_top_title.text = resources.getString(R.string.text_cnr_pickup_done)
        text_sign_p_requester.text = strReqQty
        text_sign_p_request_qty.text = strSenderName

        val checker = PermissionChecker(this@CnRPickupDoneActivity)

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(*PERMISSIONS)) {
            PermissionActivity.startActivityForResult(
                this@CnRPickupDoneActivity,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
            overridePendingTransition(0, 0)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                gpsTrackerManager = GPSTrackerManager(this@CnRPickupDoneActivity)
                gpsTrackerManager?.let {
                    gpsEnable = it.enableGPSSetting()
                }

                if (gpsEnable && gpsTrackerManager != null) {
                    gpsTrackerManager!!.gpsTrackerStart()
                } else {
                    DataUtil.enableLocationSettings(this@CnRPickupDoneActivity)
                }
            }
        }
    }


    private fun saveServerUploadSign() {
        try {
            if (!NetworkUtil.isNetworkAvailable(this@CnRPickupDoneActivity)) {
                DisplayUtil.AlertDialog(
                    this@CnRPickupDoneActivity,
                    resources.getString(R.string.msg_network_connect_error)
                )
                return
            }

            var latitude = 0.0
            var longitude = 0.0

            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }

            locationModel.setDriverLocation(latitude, longitude)

            if (!sign_view_sign_p_applicant_signature!!.isTouch) {
                Toast.makeText(
                    this.applicationContext,
                    resources.getString(R.string.msg_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (!sign_view_sign_p_collector_signature!!.isTouch) {
                Toast.makeText(
                    this.applicationContext,
                    resources.getString(R.string.msg_collector_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (MemoryStatus.availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && MemoryStatus.availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {
                DisplayUtil.AlertDialog(
                    this@CnRPickupDoneActivity,
                    resources.getString(R.string.msg_disk_size_error)
                )
                return
            }

            if (!NetworkUtil.isNetworkAvailable(this@CnRPickupDoneActivity)) {
                DisplayUtil.AlertDialog(
                    this@CnRPickupDoneActivity,
                    resources.getString(R.string.msg_network_connect_error_saved)
                )
                return
            }

            FirebaseEvent.clickEvent(this, TAG, "SetPickupUploadData ")

            lifecycleScope.launch {
                progressBar.visibility = View.VISIBLE

                var resultMsg = ""

                for (item in pickupNoList) {

                    val bitmap1 = QDataUtil.getBitmapString(
                        this@CnRPickupDoneActivity,
                        sign_view_sign_p_applicant_signature,
                        ImageUpload.QXPOP,
                        "qdriver/sign",
                        item.barcode!!
                    )

                    val bitmap2 = QDataUtil.getBitmapString(
                        this@CnRPickupDoneActivity,
                        sign_view_sign_p_collector_signature,
                        ImageUpload.QXPOP,
                        "qdriver/sign",
                        item.barcode!!
                    )

                    if (bitmap1 == "" || bitmap2 == "") {
                        resultDialog(resources.getString(R.string.msg_upload_fail_image))
                        return@launch
                    }

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val date = Date()

                    val contentVal = ContentValues()
                    contentVal.put("stat", BarcodeType.PICKUP_DONE)
                    contentVal.put("real_qty", "1")
                    contentVal.put("chg_dt", dateFormat.format(date))
                    contentVal.put("fail_reason", "")
                    contentVal.put("retry_dt", "")
                    contentVal.put("driver_memo", "")
                    contentVal.put("reg_id", Preferences.userId)

                    //todo_sypark 디비 업데이트시 Preferences.userId 가지고 들어감
                    DatabaseHelper.getInstance().update(
                        DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                        contentVal,
                        "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                        arrayOf(item.barcode!!, Preferences.userId)
                    )

                    try {
                        val response = RetrofitClient.instanceDynamic().pickupUploadData(
                            NetworkUtil.getNetworkType(this@CnRPickupDoneActivity),
                            item.barcode!!,
                            bitmap1,
                            bitmap2,
                            latitude,
                            longitude
                        )

                        if (response.resultCode == 0) {
                            val contentVal2 = ContentValues()
                            contentVal2.put("punchOut_stat", "S")

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal2,
                                "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                                arrayOf(item.barcode!!, Preferences.userId)
                            )
                        }

                        resultMsg = response.resultMsg.toString()

                    } catch (e: java.lang.Exception) {
                        resultMsg = "SetPickupUploadData api error $e"
                        break
                    }
                }
                progressBar.visibility = View.GONE
                if (resultMsg.isNotEmpty()) {
                    resultDialog(resultMsg)
                }
            }

        } catch (e: Exception) {

            Log.e("Exception", "$TAG  Exception : $e")
            Toast.makeText(
                this@CnRPickupDoneActivity,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onBackPressed() {
        cancelSigning()
    }

    private fun cancelSigning() {
        AlertDialog.Builder(this@CnRPickupDoneActivity)
            .setMessage(R.string.msg_delivered_sign_cancel)
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            .setNegativeButton(R.string.button_cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    private fun resultDialog(msg: String) {
        if (!isFinishing) {
            AlertDialog.Builder(this@CnRPickupDoneActivity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.text_upload_result))
                .setMessage(msg)
                .setPositiveButton(resources.getString(R.string.button_ok)) { dialog, _ ->

                    dialog.dismiss()

                    setResult(Activity.RESULT_OK)
                    finish()
                }.show()
        }
    }

}