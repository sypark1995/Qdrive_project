package com.giosis.library.list.pickup

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.giosis.library.MemoryStatus
import com.giosis.library.R
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.util.*
import kotlinx.android.synthetic.main.activity_pickup_start_to_scan.*
import kotlinx.android.synthetic.main.top_title.*

class PickupZeroQtyActivity : CommonActivity() {

    val tag = "PickupZeroQtyActivity"

    lateinit var pickupNo: String

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = null

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_start_to_scan)

        pickupNo = intent.getStringExtra("pickupNo").toString()

        text_top_title.text = resources.getString(R.string.text_zero_qty)
        text_sign_p_pickup_no.text = pickupNo
        text_sign_p_applicant.text = intent.getStringExtra("applicant")
        img_sign_p_start_scan.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
        img_sign_p_zero_qty.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
        text_sign_p_total_qty.text = "0"


        edit_sign_p_memo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (99 <= edit_sign_p_memo.length()) {

                    Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
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
        val checker = PermissionChecker(this@PickupZeroQtyActivity)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(this@PickupZeroQtyActivity, PERMISSION_REQUEST_CODE, *PERMISSIONS)
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
            val gpsEnable = gpsTrackerManager?.enableGPSSetting()

            if (gpsEnable == true) {

                gpsTrackerManager?.GPSTrackerStart()
                Log.e(tag, " onResume  Location  :  ${gpsTrackerManager?.latitude} / ${gpsTrackerManager?.longitude}")
            } else {

                DataUtil.enableLocationSettings(this@PickupZeroQtyActivity)
            }
        }
    }


    private fun cancelUpload() {

        val alertBuilder = AlertDialog.Builder(this@PickupZeroQtyActivity)
        alertBuilder.setMessage(resources.getString(R.string.msg_delivered_sign_cancel))

        alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { _, _ ->

            finish()
        }

        alertBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { dialogInterface, _ ->

            dialogInterface.dismiss()
        }

        alertBuilder.show()
    }


    private fun serverUpload() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this@PickupZeroQtyActivity)) {

                DisplayUtil.AlertDialog(this@PickupZeroQtyActivity, resources.getString(R.string.msg_network_connect_error))
                return
            }

            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {

                latitude = it.latitude
                longitude = it.longitude
            }
            Log.e(tag, "  Location $latitude / $longitude")


            if (!sign_view_sign_p_applicant_signature.isTouch) {

                Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (!sign_view_sign_p_collector_signature.isTouch) {

                Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            val driverMemo = edit_sign_p_memo.text.toString()
            if (driverMemo.isEmpty()) {

                Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {

                DisplayUtil.AlertDialog(this@PickupZeroQtyActivity,
                        resources.getString(R.string.msg_disk_size_error))
                return
            }


            DataUtil.logEvent("button_click", tag, DataUtil.requestSetUploadPickupData)

            PickupZeroQtyUploadHelper.Builder(this@PickupZeroQtyActivity, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
                    pickupNo, sign_view_sign_p_applicant_signature, sign_view_sign_p_collector_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(object : OnServerEventListener {
                        override fun onPostResult() {

                            DataUtil.inProgressListPosition = 0
                            finish()
                        }

                        override fun onPostFailList() {
                        }
                    }).build().execute()
        } catch (e: Exception) {

            Log.e("Exception", "$tag   serverUpload  Exception : $e")
            Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
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