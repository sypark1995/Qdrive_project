package com.giosis.library.list.pickup

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.giosis.library.MemoryStatus
import com.giosis.library.R
import com.giosis.library.databinding.ActivityPickupStartToScanBinding
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.server.ImageUpload
import com.giosis.library.util.*
import com.giosis.library.util.dialog.ProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PickupZeroQtyActivity : CommonActivity() {

    val tag = "PickupZeroQtyActivity"

    private val binding by lazy {
        ActivityPickupStartToScanBinding.inflate(layoutInflater)
    }

    val progressBar by lazy {
        ProgressDialog(this@PickupZeroQtyActivity)
    }

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
        setContentView(binding.root)

        pickupNo = intent.getStringExtra("pickupNo").toString()

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

                    Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
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


            if (!binding.signApplicantSignature.isTouch) {

                Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (!binding.signCollectorSignature.isTouch) {

                Toast.makeText(this@PickupZeroQtyActivity, resources.getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            val driverMemo = binding.editMemo.text.toString()
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


            // TODO_ ImageUpload 테스트
//            progressBar.visibility = View.VISIBLE
//            lifecycleScope.launch(Dispatchers.IO) {
//
//                binding.signApplicantSignature.buildDrawingCache()
//                binding.signCollectorSignature.buildDrawingCache()
//                val captureView: Bitmap =  binding.signApplicantSignature.drawingCache
//                val captureView2: Bitmap =  binding.signCollectorSignature.drawingCache
//                val bitmapString = DataUtil.bitmapToString(this@PickupZeroQtyActivity, captureView, ImageUpload.QXPOP, "qdriver/sign", pickupNo)
//                val bitmapString2 = DataUtil.bitmapToString(this@PickupZeroQtyActivity, captureView2, ImageUpload.QXPOP, "qdriver/sign", pickupNo)
//
//                withContext(Dispatchers.Main) {
//
//                    progressBar.visibility = View.GONE
//                }
//            }

            PickupZeroQtyUploadHelper.Builder(this@PickupZeroQtyActivity, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
                    pickupNo, binding.signApplicantSignature,  binding.signCollectorSignature, driverMemo,
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