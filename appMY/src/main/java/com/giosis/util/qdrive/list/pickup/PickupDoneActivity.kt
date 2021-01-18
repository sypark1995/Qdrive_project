package com.giosis.util.qdrive.list.pickup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.OnServerEventListener
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.util.*
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_signing_start_to_scan_pickup.*
import kotlinx.android.synthetic.main.top_title.*

class PickupDoneActivity : CommonActivity() {

    val tag = "PickupDoneActivity"
    private val context = MyApplication.getContext()

    private val userId = MyApplication.preferences.userId
    private val officeCode = MyApplication.preferences.officeCode
    private val deviceId = MyApplication.preferences.deviceUUID
    lateinit var pickupNo: String
    private lateinit var scannedList: String

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = GPSTrackerManager(context)

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signing_start_to_scan_pickup)


        text_top_title.text = intent.getStringExtra("title")
        pickupNo = intent.getStringExtra("pickupNo")!!
        scannedList = intent.getStringExtra("scannedList")!!
        text_sign_p_pickup_no.text = pickupNo
        text_sign_p_applicant.text = intent.getStringExtra("applicant")
        img_sign_p_start_scan.background = context.resources.getDrawable(R.drawable.qdrive_btn_icon_check_on)
        img_sign_p_zero_qty.background = context.resources.getDrawable(R.drawable.qdrive_btn_icon_check_off)
        text_sign_p_total_qty.text = intent.getStringExtra("scannedQty")


        edit_sign_p_memo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (99 <= edit_sign_p_memo.length()) {

                    Toast.makeText(this@PickupDoneActivity, context.resources.getString(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
                }
            }
        })


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

            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }

        alertBuilder.setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialogInterface, _ ->

            dialogInterface.dismiss()
        }

        alertBuilder.show()
    }


    private fun serverUpload() {

        try {

            if (!NetworkUtil.isNetworkAvailable(context)) {

                DisplayUtil.AlertDialog(this@PickupDoneActivity, context.resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (!sign_view_sign_p_applicant_signature.isTouche) {

                Toast.makeText(this@PickupDoneActivity, context.resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (!sign_view_sign_p_collector_signature.isTouche) {

                Toast.makeText(this@PickupDoneActivity, context.resources.getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            val driverMemo = edit_sign_p_memo.text.toString()
            if (driverMemo.isEmpty()) {

                Toast.makeText(this@PickupDoneActivity, context.resources.getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {

                DisplayUtil.AlertDialog(this@PickupDoneActivity, context.resources.getString(R.string.msg_disk_size_error))
                return
            }


            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {

                latitude = it.latitude
                longitude = it.longitude
            }
            Log.e(tag, "  Location $latitude / $longitude")



            com.giosis.library.util.DataUtil.logEvent("button_click", tag, "SetPickupUploadData_ScanAll")

            PickupDoneUploadHelper.Builder(this, userId, officeCode, deviceId,
                    pickupNo, scannedList, text_sign_p_total_qty.text.toString(),
                    sign_view_sign_p_applicant_signature, sign_view_sign_p_collector_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(object : OnServerEventListener {
                        override fun onPostResult() {

                            DataUtil.inProgressListPosition = 0
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }

                        override fun onPostFailList() {
                        }
                    }).build().execute()
        } catch (e: Exception) {

            Log.e(tag, "   serverUpload  Exception : $e")
            Toast.makeText(this@PickupDoneActivity, context.resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
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