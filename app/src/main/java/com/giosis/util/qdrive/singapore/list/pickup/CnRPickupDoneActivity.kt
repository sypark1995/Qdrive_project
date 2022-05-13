package com.giosis.util.qdrive.singapore.list.pickup

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.gps.LocationModel
import com.giosis.util.qdrive.singapore.list.BarcodeData
import com.giosis.util.qdrive.singapore.database.DatabaseHelper.Companion.getInstance
import com.giosis.util.qdrive.singapore.util.*
import kotlinx.android.synthetic.main.activity_pickup_done.*
import kotlinx.android.synthetic.main.top_title.*
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

    var tag = "CnRPickupDoneActivity"

    private var mStrWaybillNo: String = ""
    private var mType = BarcodeType.PICKUP_CNR

    private var pickupNoList: ArrayList<BarcodeData>? = null

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    private var locationModel = LocationModel()

    var isPermissionTrue = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_done)

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

        pickupNoList = ArrayList()
        var pickupBarcodeData: BarcodeData

        val mWaybillList = mStrWaybillNo.split(",".toRegex()).toTypedArray()

        for (s in mWaybillList) {

            val barcode = s.trim()
            pickupBarcodeData = BarcodeData()
            pickupBarcodeData.barcode = barcode
            pickupBarcodeData.state = mType
            pickupNoList!!.add(pickupBarcodeData)

            // 위, 경도
            if (strReqQty.equals("1")) {

                val cs: Cursor =
                    getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcode + "'"]

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

        var barcodeMsg: String? = ""
        val songJangListSize = pickupNoList!!.size

        for (i in 0 until songJangListSize) {
            barcodeMsg += if (barcodeMsg == "") pickupNoList!![i].barcode else ", " + pickupNoList!![i].barcode
        }

        val qtyFormat =
            String.format(resources.getString(R.string.text_total_qty_count), songJangListSize)

        text_sign_p_tracking_no.text = qtyFormat
        text_sign_p_tracking_no_more.visibility = View.VISIBLE
        text_sign_p_tracking_no_more.text = barcodeMsg

        text_top_title.text = resources.getString(R.string.text_cnr_pickup_done)
        text_sign_p_requester.text = strReqQty
        text_sign_p_request_qty.text = strSenderName

        val checker = PermissionChecker(this@CnRPickupDoneActivity)

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this@CnRPickupDoneActivity,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
            overridePendingTransition(0, 0)
        } else {
            isPermissionTrue = true
        }
    }

    override fun onResume() {
        super.onResume()

        if (isPermissionTrue) {

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

    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", "$tag   onActivityResult  PERMISSIONS_GRANTED")
                isPermissionTrue = true
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

            DataUtil.logEvent("button_click", tag, "SetPickupUploadData")

            CnRPickupUploadHelper.Builder(
                this@CnRPickupDoneActivity,
                Preferences.userId,
                Preferences.officeCode,
                Preferences.deviceUUID,
                pickupNoList,
                sign_view_sign_p_applicant_signature,
                sign_view_sign_p_collector_signature,
                MemoryStatus.availableInternalMemorySize,
                locationModel
            ).setOnServerEventListener(object : OnServerEventListener {
                override fun onPostResult() {

                    DataUtil.inProgressListPosition = 0
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                override fun onPostFailList() {}
            }).build().execute()

        } catch (e: Exception) {

            Log.e("Exception", "$tag  Exception : $e")
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

}