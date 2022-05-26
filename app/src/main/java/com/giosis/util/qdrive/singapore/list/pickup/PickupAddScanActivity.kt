package com.giosis.util.qdrive.singapore.list.pickup

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.barcodescanner.StdResult
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.server.ImageUpload
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import kotlinx.android.synthetic.main.activity_pickup_done.*
import kotlinx.android.synthetic.main.top_title.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PickupAddScanActivity : CommonActivity() {

    val tag = "PickupAddScanActivity"

    lateinit var pickupNo: String
    private lateinit var scannedList: String
    private lateinit var scannedQty: String

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(
        PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
        PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
    )

    val progressBar by lazy {
        ProgressDialog(this@PickupAddScanActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_done)

        pickupNo = intent.getStringExtra("pickupNo").toString()
        scannedList = intent.getStringExtra("scannedList").toString()
        scannedQty = intent.getStringExtra("scannedQty").toString()

        val scannedItems = scannedList.split(",")
        val qtyFormat =
            String.format(resources.getString(R.string.text_total_qty_count), scannedItems.size)

        text_top_title.text = resources.getString(R.string.text_title_add_pickup)
        text_sign_p_tracking_no.text = qtyFormat
        text_sign_p_tracking_no_more.text = scannedList
        text_sign_p_requester.text = intent.getStringExtra("applicant")
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
        val checker = PermissionChecker(this@PickupAddScanActivity)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this@PickupAddScanActivity,
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
            // Location
            gpsTrackerManager = GPSTrackerManager(this@PickupAddScanActivity)
            gpsTrackerManager?.let {
                gpsEnable = it.enableGPSSetting()
            }

            if (gpsEnable && gpsTrackerManager != null) {
                gpsTrackerManager!!.gpsTrackerStart()
            } else {
                DataUtil.enableLocationSettings(this@PickupAddScanActivity)
            }
            progressBar.setCancelable(false)
        }
    }

    private fun cancelUpload() {
        AlertDialog.Builder(this@PickupAddScanActivity)
            .setMessage(resources.getString(R.string.msg_delivered_sign_cancel))
            .setPositiveButton(resources.getString(R.string.button_ok)) { _, _ ->
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            .setNegativeButton(resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }


    private fun serverUpload() {

        try {
            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }

            if (!NetworkUtil.isNetworkAvailable(this@PickupAddScanActivity)) {
                DisplayUtil.AlertDialog(
                    this@PickupAddScanActivity,
                    resources.getString(R.string.msg_network_connect_error)
                )
                return
            }

            if (!sign_view_sign_p_applicant_signature.isTouch) {
                Toast.makeText(
                    this@PickupAddScanActivity,
                    resources.getString(R.string.msg_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (!sign_view_sign_p_collector_signature.isTouch) {
                Toast.makeText(
                    this@PickupAddScanActivity,
                    resources.getString(R.string.msg_collector_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (MemoryStatus.availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && MemoryStatus.availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {
                DisplayUtil.AlertDialog(
                    this@PickupAddScanActivity,
                    resources.getString(R.string.msg_disk_size_error)
                )
                return
            }

            DataUtil.logEvent("button_click", tag, "SetPickupUploadData_AddScan")

            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {

                try {
                    val scannedItems: Array<String> = scannedList.split(",").toTypedArray()

                    for (item in scannedItems) {
                        DataUtil.captureSign(
                            "/QdrivePickup",
                            item,
                            sign_view_sign_p_applicant_signature
                        )

                        DataUtil.captureSign(
                            "/QdriveCollector",
                            item,
                            sign_view_sign_p_collector_signature
                        )
                    }

                    val bitmap1 = QDataUtil.getBitmapString(
                        this@PickupAddScanActivity,
                        sign_view_sign_p_applicant_signature,
                        ImageUpload.QXPOP,
                        "qdriver/sign",
                        pickupNo
                    )

                    val bitmap2 = QDataUtil.getBitmapString(
                        this@PickupAddScanActivity,
                        sign_view_sign_p_collector_signature,
                        ImageUpload.QXPOP,
                        "qdriver/sign",
                        pickupNo
                    )

                    if (bitmap1 == "" || bitmap2 == "") {
                        resultDialog(resources.getString(R.string.msg_upload_fail_image))
                        return@launch
                    }

                    val response =
                        RetrofitClient.instanceDynamic().requestSetPickupUploadDataAddScan(
                            pickupNo,
                            bitmap1,
                            bitmap2,
                            scannedList,
                            NetworkUtil.getNetworkType(this@PickupAddScanActivity),
                            latitude,
                            longitude,
                            scannedQty
                        )

                    if (response.resultCode == 0) {
                        resultDialog(
                            String.format(
                                resources.getString(R.string.text_upload_success_count),
                                1
                            )
                        )
                    } else {
                        alertShow("AddScan api error ${response.resultCode} ${response.resultMsg} ")
                    }

                    return@launch

                } catch (e: Exception) {
                    resultDialog("AddScan api exception $e")
                }

                progressBar.visibility = View.GONE
            }

        } catch (e: Exception) {
            Toast.makeText(
                this@PickupAddScanActivity,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onBackPressed() {
        cancelUpload()
    }

    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

    private fun alertShow(msg: String) {
        AlertDialog.Builder(this)
            .setTitle(
                resources.getString(R.string.text_upload_failed)
            )
            .setMessage(msg)
            .setPositiveButton(
                resources
                    .getString(R.string.button_close)
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss() // 닫기
            }
            .show()
    }

    private fun resultDialog(msg: String) {

        if (!isFinishing) {

            val builder = AlertDialog.Builder(this@PickupAddScanActivity)
            builder.setCancelable(false)
            builder.setTitle(resources.getString(R.string.text_upload_result))
            builder.setMessage(msg)
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog, _ ->

                dialog.dismiss()

                DataUtil.inProgressListPosition = 0
                setResult(Activity.RESULT_OK)
                finish()
            }
            builder.show()
        }
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