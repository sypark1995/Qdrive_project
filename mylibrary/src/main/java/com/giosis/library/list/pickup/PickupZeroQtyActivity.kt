package com.giosis.library.list.pickup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.giosis.library.MemoryStatus
import com.giosis.library.R
import com.giosis.library.barcodescanner.StdResult
import com.giosis.library.databinding.ActivityPickupStartToScanBinding
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.server.ImageUpload
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.*
import com.giosis.library.util.dialog.ProgressDialog
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class PickupZeroQtyActivity : CommonActivity() {
    val tag = "PickupZeroQtyActivity"

    private val binding by lazy {
        ActivityPickupStartToScanBinding.inflate(layoutInflater)
    }
    val progressBar by lazy {
        ProgressDialog(this@PickupZeroQtyActivity)
    }

    val pickupNo by lazy {
        intent.getStringExtra("pickupNo")
    }
    var driverMemo = ""

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false
    var latitude = 0.0
    var longitude = 0.0

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000

    val PERMISSIONS = arrayOf(
        PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
        PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.layoutTopTitle.textTopTitle.text = resources.getString(R.string.text_zero_qty)
        binding.textPickupNo.text = pickupNo
        binding.textApplicant.text = intent.getStringExtra("applicant")
        binding.imgStartScanCheck.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
        binding.imgZeroQtyCheck.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
        binding.textTotalQty.text = "0"


        binding.editMemo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (99 <= binding.editMemo.length()) {

                    Toast.makeText(
                        this@PickupZeroQtyActivity,
                        resources.getString(R.string.msg_memo_too_long),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })


        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            cancelUpload()
        }

        binding.layoutApplicantEraser.setOnClickListener {
            binding.signApplicantSignature.clearText()
        }

        binding.layoutCollectorEraser.setOnClickListener {
            binding.signCollectorSignature.clearText()
        }

        binding.btnSave.setOnClickListener {
            serverUpload()
        }


        // permission
        val checker = PermissionChecker(this@PickupZeroQtyActivity)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this@PickupZeroQtyActivity,
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
            gpsTrackerManager = GPSTrackerManager(this@PickupZeroQtyActivity)
            gpsTrackerManager?.let {
                gpsEnable = it.enableGPSSetting()
            }

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager!!.gpsTrackerStart()
                Log.e(
                    tag,
                    " onResume  Location  :  ${gpsTrackerManager!!.latitude} / ${gpsTrackerManager!!.longitude}"
                )
            } else {

                DataUtil.enableLocationSettings(this@PickupZeroQtyActivity)
            }

            progressBar.setCancelable(false)
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun serverUpload() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this@PickupZeroQtyActivity)) {

                DisplayUtil.AlertDialog(
                    this@PickupZeroQtyActivity,
                    resources.getString(R.string.msg_network_connect_error)
                )
                return
            }

            gpsTrackerManager?.let {

                latitude = it.latitude
                longitude = it.longitude
            }
            Log.e(tag, "  Location $latitude / $longitude")


            if (!binding.signApplicantSignature.isTouch) {

                Toast.makeText(
                    this@PickupZeroQtyActivity,
                    resources.getString(R.string.msg_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (!binding.signCollectorSignature.isTouch) {

                Toast.makeText(
                    this@PickupZeroQtyActivity,
                    resources.getString(R.string.msg_collector_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            driverMemo = binding.editMemo.text.toString()
            if (driverMemo.isEmpty()) {

                Toast.makeText(
                    this@PickupZeroQtyActivity,
                    resources.getString(R.string.msg_must_enter_memo1),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (MemoryStatus.availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && MemoryStatus.availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {

                DisplayUtil.AlertDialog(
                    this@PickupZeroQtyActivity,
                    resources.getString(R.string.msg_disk_size_error)
                )
                return
            }


            DataUtil.logEvent("button_click", tag, "SetPickupUploadData")

            progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                // doInBackground
                val result = requestPickupUpload(pickupNo!!)

                // onPostExecute
                progressBar.visibility = View.GONE

                if (result.resultCode < 0) {

                    val failReason = when (result.resultCode) {
                        -14 -> {
                            resources.getString(R.string.msg_upload_fail_14)
                        }
                        -15 -> {
                            resources.getString(R.string.msg_upload_fail_15)
                        }
                        -16 -> {
                            resources.getString(R.string.msg_upload_fail_16)
                        }
                        else -> {
                            result.resultMsg
                        }
                    }

                    val msg = String.format(
                        resources.getString(R.string.text_upload_fail_count),
                        0,
                        1,
                        failReason
                    )
                    resultDialog(msg)
                } else {

                    val msg =
                        String.format(resources.getString(R.string.text_upload_success_count), 1)
                    resultDialog(msg)
                }
            }

//            PickupZeroQtyUploadHelper.Builder(this@PickupZeroQtyActivity, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
//                    pickupNo, binding.signApplicantSignature, binding.signCollectorSignature, driverMemo,
//                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
//                    .setOnServerEventListener(object : OnServerEventListener {
//                        override fun onPostResult() {
//
//                            DataUtil.inProgressListPosition = 0
//                            finish()
//                        }
//
//                        override fun onPostFailList() {
//                        }
//                    }).build().execute()
        } catch (e: Exception) {

            Log.e("Exception", "$tag   serverUpload  Exception : $e")
            Toast.makeText(
                this@PickupZeroQtyActivity,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private suspend fun requestPickupUpload(pickupNo: String): StdResult =
        withContext(Dispatchers.IO) {
            val stdResult = StdResult()

            DataUtil.captureSign("/QdrivePickup", pickupNo, binding.signApplicantSignature)
            DataUtil.captureSign("/QdriveCollector", pickupNo, binding.signCollectorSignature)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val contentVal = ContentValues()
            contentVal.put("stat", BarcodeType.PICKUP_DONE)
            contentVal.put("real_qty", "0")
            contentVal.put("rcv_type", "ZQ")
            contentVal.put("chg_dt", dateFormat.format(Date()))
            contentVal.put("driver_memo", driverMemo)
            contentVal.put("fail_reason", "")
            contentVal.put("retry_dt", "")
            DatabaseHelper.getInstance().update(
                DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                "invoice_no=? COLLATE NOCASE and reg_id = ?", arrayOf(pickupNo, Preferences.userId)
            )

            val bitmapString1 = QDataUtil.getBitmapString(
                this@PickupZeroQtyActivity, binding.signApplicantSignature,
                ImageUpload.QXPOP, "qdriver/sign", pickupNo
            )
            val bitmapString2 = QDataUtil.getBitmapString(
                this@PickupZeroQtyActivity, binding.signCollectorSignature,
                ImageUpload.QXPOP, "qdriver/sign", pickupNo
            )
            Log.e("Image", "  $bitmapString1 / $bitmapString2")

            if (bitmapString1 == "" || bitmapString2 == "") {

                stdResult.resultCode = -100
                stdResult.resultMsg = resources.getString(R.string.msg_upload_fail_image)
                return@withContext stdResult
            }


            try {

                val model = RetrofitClient.instanceDynamic().requestSetPickupUploadData(
                    "ZQ",
                    BarcodeType.PICKUP_DONE,
                    NetworkUtil.getNetworkType(this@PickupZeroQtyActivity),
                    pickupNo,
                    bitmapString1,
                    bitmapString2,
                    driverMemo,
                    latitude,
                    longitude,
                    "0",
                    "",
                    ""
                )
                Log.e(
                    "Server",
                    "requestSetPickupUploadData $pickupNo  result  ${model.resultCode}/${model.resultMsg}"
                )

                stdResult.resultCode = model.resultCode
                stdResult.resultMsg = model.resultMsg

                if (model.resultCode == 0) {

                    val contentVal2 = ContentValues()
                    contentVal2.put("punchOut_stat", "S")
                    DatabaseHelper.getInstance().update(
                        DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                        contentVal2,
                        "invoice_no=? COLLATE NOCASE and reg_id = ?",
                        arrayOf(pickupNo, Preferences.userId)
                    )
                }
            } catch (e: Exception) {

                stdResult.resultCode = -15
                stdResult.resultMsg = e.message
            }

            return@withContext stdResult
        }

    private fun resultDialog(msg: String) {

        if (!this@PickupZeroQtyActivity.isFinishing) {

            val builder = AlertDialog.Builder(this@PickupZeroQtyActivity)
            builder.setCancelable(false)
            builder.setTitle(resources.getString(R.string.text_upload_result))
            builder.setMessage(msg)
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog, _ ->

                dialog.dismiss()

                DataUtil.inProgressListPosition = 0
                finish()
            }
            builder.show()
        }
    }

    override fun onBackPressed() {

        cancelUpload()
    }

    private fun cancelUpload() {

        if (!this@PickupZeroQtyActivity.isFinishing) {
            AlertDialog.Builder(this@PickupZeroQtyActivity)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok) { _, _ -> finish() }
                .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }
                .show()
        }
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