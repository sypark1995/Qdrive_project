package com.giosis.util.qdrive.list.pickup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.giosis.util.qdrive.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.singapore.OnServerEventListener
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.*
import kotlinx.android.synthetic.main.activity_pickup_done.*
import kotlinx.android.synthetic.main.top_title.*

class PickupAddScanActivity : AppCompatActivity() {

    val tag = "PickupAddScanActivity"
    private val context = MyApplication.getContext()

    private val userId = MyApplication.preferences.userId
    private val officeCode = MyApplication.preferences.officeCode
    private val deviceId = MyApplication.preferences.deviceUUID

    lateinit var pickupNo: String
    private lateinit var scannedList: String
    private lateinit var scannedQty: String

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = GPSTrackerManager(context)

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_done)


        text_top_title.text = intent.getStringExtra("title")

        pickupNo = intent.getStringExtra("pickupNo")
        scannedList = intent.getStringExtra("scannedList")
        scannedQty = intent.getStringExtra("scannedQty")

        val scannedItems = scannedList.split(",")
        val qtyFormat = String.format(context.resources.getString(R.string.text_total_qty_count), scannedItems.size)
        text_sign_p_tracking_no.text = qtyFormat
        text_sign_p_tracking_no_more.text = scannedList
        text_sign_p_requestor.text = intent.getStringExtra("senderName")
        text_sign_p_request_qty.text = scannedQty


        layout_top_back.setOnClickListener {

            cancelUpload()
        }

        layout_sign_p_applicant_eraser.setOnClickListener {

            sign_view_sign_p_applicant_signature.clearText()
        }

        layout_sign_p_collector_eraser.setOnClickListener {

            sign_view_sign_p_collector_signature.clearText()
        }

        btn_sign_p_save.setOnClickListener {

            serverUpload()
        }


        // permission
        val checker = PermissionChecker(this)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {

            isPermissionTrue = true
        }
    }


    override fun onResume() {
        super.onResume()


        if (isPermissionTrue) {

            // Location
            val gpsEnable = gpsTrackerManager?.enableGPSSetting()

            if (gpsEnable == true) {

                gpsTrackerManager?.GPSTrackerStart()
                Log.e(tag, " onResume  Location  :  ${gpsTrackerManager?.latitude} / ${gpsTrackerManager?.longitude}")
            } else {

                DataUtil.enableLocationSettings(this, context)
            }
        }
    }


    private fun cancelUpload() {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setMessage(context.resources.getString(R.string.msg_delivered_sign_cancel))

        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { _, _ ->

            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        alertBuilder.setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialogInterface, _ ->

            dialogInterface.dismiss()
        }

        alertBuilder.show()
    }


    private fun serverUpload() {

        try {

            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {

                latitude = it.latitude
                longitude = it.longitude
            }
            Log.e(tag, "  Location $latitude / $longitude")


            if (!NetworkUtil.isNetworkAvailable(context)) {

                DisplayUtil.AlertDialog(this@PickupAddScanActivity, context.resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (!sign_view_sign_p_applicant_signature.isTouche) {

                Toast.makeText(this@PickupAddScanActivity, context.resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (!sign_view_sign_p_collector_signature.isTouche) {

                Toast.makeText(this@PickupAddScanActivity, context.resources.getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {

                DisplayUtil.AlertDialog(this@PickupAddScanActivity, context.resources.getString(R.string.msg_disk_size_error))
                return
            }


            try {

                val params = Bundle()
                params.putString("Activity", tag)
                params.putString("method", "SetPickupUploadData_AddScan")
                DataUtil.mFirebaseAnalytics.logEvent("button_click", params)
            } catch (e: java.lang.Exception) {

                Log.e("Firebase", "mFirebaseAnalytics  Exception : $e")
            }


            PickupAddScanUploadHelper.Builder(this, userId, officeCode, deviceId,
                    pickupNo, scannedList, scannedQty, sign_view_sign_p_applicant_signature, sign_view_sign_p_collector_signature,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(object : OnServerEventListener {
                        override fun onPostResult() {

                            DataUtil.inProgressListPosition = 0
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                        override fun onPostFailList() {
                        }
                    }).build().execute()
        } catch (e: Exception) {

            Log.e(tag, "   serverUpload  Exception : $e")
            Toast.makeText(this@PickupAddScanActivity, context.resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {

        cancelUpload()
    }


    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                isPermissionTrue = true
            }
        }
    }
}