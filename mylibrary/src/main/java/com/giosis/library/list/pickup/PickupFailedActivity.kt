package com.giosis.library.list.pickup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.contract.ActivityResultContracts
import com.giosis.library.MemoryStatus
import com.giosis.library.R
import com.giosis.library.barcodescanner.StdResult
import com.giosis.library.databinding.ActivityPickupVisitLogBinding
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.server.ImageUpload
import com.giosis.library.server.RetrofitClient
import com.giosis.library.server.data.FailedCodeResult
import com.giosis.library.util.*
import com.giosis.library.util.dialog.ProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// 일반 Pickup 실패  &  CNR Pickup 실패
class PickupFailedActivity : CommonActivity(), Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {
    val tag = "PickupFailedActivity"

    private val binding by lazy {
        ActivityPickupVisitLogBinding.inflate(layoutInflater)
    }
    val progressBar by lazy {
        ProgressDialog(this@PickupFailedActivity)
    }

    val pickupNo by lazy {
        intent.getStringExtra("pickupNo")
    }
    private var rcvType = ""       // VL, RC
    private var arrayList: ArrayList<FailedCodeResult.FailedCode>? = null
    private var failedCodeArrayList: ArrayList<String>? = null
    var driverMemo = ""
    private var failedCode = ""
    private var retryDay = ""

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false
    var latitude = 0.0
    var longitude = 0.0

    // Camera & Gallery
    private val camera2 = Camera2APIs(this@PickupFailedActivity)
    private var cameraId: String? = null
    private var isClickedPhoto = false

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)     // 어플이 사용되는 동안 화면 끄지 않기
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)   // Lock 상태이면 보여주지 않음. (Lock 해제해야만 보임)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)   // Lock 상태이면 보여줌
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)     // 화면 ON
        setContentView(binding.root)


        val pickupType = intent.getStringExtra("type")  // P, CNR

        binding.layoutTopTitle.textTopTitle.text = resources.getString(R.string.text_visit_log)
        binding.textPickupNo.text = pickupNo
        binding.textApplicant.text = intent.getStringExtra("applicant")
        binding.textRequestedQty.text = intent.getStringExtra("reqQty")
        if (pickupType == BarcodeType.TYPE_PICKUP) {

            binding.textApplicantTitle.text = resources.getString(R.string.text_applicant)
            rcvType = "VL"
        } else if (pickupType == BarcodeType.TYPE_CNR) {

            binding.textApplicantTitle.text = resources.getString(R.string.text_requestor)
            rcvType = "RC"
        }

        //
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)

        val dateListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->

            Log.i(tag, "DATE : $year / ${monthOfYear + 1} / $dayOfMonth")
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val restDay = getRestDay(year, monthOfYear + 1, dayOfMonth)

            if (Preferences.userNation.contentEquals("SG")) {
                when {
                    restDay.isNotEmpty() -> {
                        Toast.makeText(this@PickupFailedActivity, "$restDay " + resources.getString(R.string.msg_choose_another_day), Toast.LENGTH_SHORT).show()
                        binding.textRetryDate.text = resources.getString(R.string.text_select)
                    }
                    else -> {
                        val dateFormat = "yyyy-MM-dd"
                        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
                        binding.textRetryDate.text = simpleDateFormat.format(calendar.time)
                    }
                }
            } else {

                when {
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> {
                        Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_choose_sunday_error), Toast.LENGTH_SHORT).show()
                        binding.textRetryDate.text = resources.getString(R.string.text_select)
                    }
                    restDay.isNotEmpty() -> {
                        Toast.makeText(this@PickupFailedActivity, "$restDay " + resources.getString(R.string.msg_choose_another_day), Toast.LENGTH_SHORT).show()
                        binding.textRetryDate.text = resources.getString(R.string.text_select)
                    }
                    else -> {
                        val dateFormat = "yyyy-MM-dd"
                        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
                        binding.textRetryDate.text = simpleDateFormat.format(calendar.time)
                    }
                }
            }
        }

        val datePickupDialog = DatePickerDialog(this@PickupFailedActivity,
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))

        if (Preferences.userNation.contentEquals("SG")) {

            val minCalendar = Calendar.getInstance()
            minCalendar.add(Calendar.DAY_OF_YEAR, 1)
            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.DAY_OF_YEAR, 3)

            datePickupDialog.datePicker.minDate = minCalendar.timeInMillis
            datePickupDialog.datePicker.maxDate = maxCalendar.timeInMillis
        } else {

            val minCalendar = Calendar.getInstance()
            minCalendar.add(Calendar.DAY_OF_YEAR, 1)
            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.DAY_OF_YEAR, 7)

            datePickupDialog.datePicker.minDate = minCalendar.timeInMillis
            datePickupDialog.datePicker.maxDate = maxCalendar.timeInMillis
        }


        // 2020.12  Pickup Failed Code
        setFailedCode()

        binding.spinnerFailedReason.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>, arg1: View?, position: Int, arg3: Long) {

                val reason = parentView.getItemAtPosition(position).toString()

                if (reason.toUpperCase(Locale.ROOT).contains(resources.getString(R.string.text_other).toUpperCase(Locale.ROOT))) {
                    binding.layoutMemo.visibility = View.VISIBLE
                } else {
                    binding.layoutMemo.visibility = View.GONE
                }

                binding.textFailedReason.text = reason
            }
        }

        binding.editMemo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (99 <= binding.editMemo.length()) {

                    Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
                }
            }
        })


        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            cancelUpload()
        }

        binding.layoutRetryDate.setOnClickListener {
            datePickupDialog.show()
        }

        binding.layoutFailedReason.setOnClickListener {
            binding.spinnerFailedReason.performClick()
        }

        binding.layoutTakePhoto.setOnClickListener {
            if (cameraId != null) {
                if (!isClickedPhoto) {

                    isClickedPhoto = true
                    camera2.takePhoto(binding.texturePreview, binding.imgVisitLog)
                }
            } else {

                Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutGallery.setOnClickListener {
            getGalleryImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            serverUpload()
        }


        // permission
        val checker = PermissionChecker(this@PickupFailedActivity)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(this@PickupFailedActivity, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {

            isPermissionTrue = true
        }
    }


    override fun onResume() {
        super.onResume()

        if (isPermissionTrue) {

            if (binding.texturePreview.isAvailable) {

                openCamera()
            } else {

                binding.texturePreview.surfaceTextureListener = this@PickupFailedActivity
            }

            // Location
            gpsTrackerManager = GPSTrackerManager(this@PickupFailedActivity)
            gpsTrackerManager?.let {

                gpsEnable = it.enableGPSSetting()
            }

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager!!.gpsTrackerStart()
                Log.e(tag, " onResume  Location  :  ${gpsTrackerManager!!.latitude} / ${gpsTrackerManager!!.longitude}")
            } else {

                DataUtil.enableLocationSettings(this@PickupFailedActivity)
            }

            progressBar.setCancelable(false)
        }
    }

    // Camera
    private fun openCamera() {

        val cameraManager: CameraManager = camera2.getCameraManager(this@PickupFailedActivity)
        cameraId = camera2.getCameraCharacteristics(cameraManager)

        if (cameraId != null) {

            camera2.setCameraDevice(cameraManager, cameraId)
        } else {

            Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Size, rotation: Int, it: String) {
        Log.e("Camera", "onCameraDeviceOpened  $it")

        binding.texturePreview.rotation = rotation.toFloat()

        val texture = binding.texturePreview.surfaceTexture
        texture?.setDefaultBufferSize(cameraSize.width, cameraSize.height)

        val surface = Surface(texture)
        camera2.setCaptureSessionRequest(cameraDevice, surface)
    }

    override fun onCaptureCompleted() {

        isClickedPhoto = false
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        return true
    }

    private fun closeCamera() {

        camera2.closeCamera()
    }

    // Gallery
    private val getGalleryImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

        try {
            val selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val resizeImage = camera2.getResizeBitmap(selectedImage)
            binding.imgVisitLog.setImageBitmap(resizeImage)
            binding.imgVisitLog.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } catch (e: Exception) {

        }
    }


    private fun serverUpload() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this@PickupFailedActivity)) {
                DisplayUtil.AlertDialog(this@PickupFailedActivity, resources.getString(R.string.msg_network_connect_error))
                return
            }

            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
            Log.i(tag, "  Location $latitude / $longitude")

            val code: FailedCodeResult.FailedCode = arrayList!![binding.spinnerFailedReason.selectedItemPosition]
            failedCode = code.failedCode

            if (code.failedString.toUpperCase(Locale.ROOT).contains(resources.getString(R.string.text_other).toUpperCase(Locale.ROOT))) {

                driverMemo = binding.editMemo.text.toString()
                if (driverMemo.isEmpty()) {
                    Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show()
                    return
                }
            }

            retryDay = binding.textRetryDate.text.toString()
            if (retryDay == resources.getString(R.string.text_select) || retryDay == "") {
                Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_select_retry_date), Toast.LENGTH_SHORT).show()
                return
            }

            if (!camera2.hasImage(binding.imgVisitLog)) {
                Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.msg_visit_photo_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                DisplayUtil.AlertDialog(this@PickupFailedActivity, resources.getString(R.string.msg_disk_size_error))
                return
            }


            DataUtil.logEvent("button_click", tag, "SetPickupUploadData")

            progressBar.visibility = View.VISIBLE
            CoroutineScope(QDataUtil.mainDispatcher).launch {
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

                    val msg = String.format(resources.getString(R.string.text_upload_fail_count1), 1, failReason)
                    resultDialog(msg)
                } else {

                    val msg = String.format(resources.getString(R.string.text_upload_success_count), 1)
                    resultDialog(msg)
                }
            }

//            PickupFailedUploadHelper.Builder(this@PickupFailedActivity, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
//                    rcvType, pickupNo, failedCode, retryDay, driverMemo, binding.imgVisitLog,
//                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
//                    .setOnServerEventListener(object : OnServerEventListener {
//                        override fun onPostResult() {
//
//                            DataUtil.inProgressListPosition = 0
//                            finish()
//                        }
//
//                        override fun onPostFailList() {}
//                    }).build().execute()
        } catch (e: Exception) {

            Log.e("Exception", "$tag   serverUpload  Exception : $e")
            Toast.makeText(this@PickupFailedActivity, resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("SimpleDateFormat")
    private suspend fun requestPickupUpload(pickupNo: String): StdResult =
            withContext(QDataUtil.ioDispatcher) {
                val stdResult = StdResult()

                DataUtil.captureSign("/QdrivePickup", pickupNo, binding.imgVisitLog)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val contentVal = ContentValues()
                contentVal.put("stat", "PF")
                contentVal.put("real_qty", "0")
                contentVal.put("rcv_type", rcvType)
                contentVal.put("chg_dt", dateFormat.format(Date()))
                contentVal.put("driver_memo", driverMemo)
                contentVal.put("fail_reason", failedCode)
                contentVal.put("retry_dt", retryDay)
                DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal,
                        "invoice_no=? COLLATE NOCASE and reg_id = ?", arrayOf(pickupNo, Preferences.userId))

                val bitmapString1 = QDataUtil.getBitmapString(this@PickupFailedActivity, binding.imgVisitLog,
                        ImageUpload.QXPOP, "qdriver/sign", pickupNo)
                Log.e("Image", "  $bitmapString1")

                if (bitmapString1 == "") {

                    stdResult.resultCode = -100
                    stdResult.resultMsg = resources.getString(R.string.msg_upload_fail_image)
                    return@withContext stdResult
                }


                try {

                    val model = RetrofitClient.instanceDynamic().requestSetPickupUploadData(rcvType, "PF", NetworkUtil.getNetworkType(this@PickupFailedActivity),
                            pickupNo, bitmapString1, "", driverMemo, latitude, longitude, "0", failedCode, retryDay)
                    Log.e("Server", "requestSetPickupUploadData $pickupNo  result  ${model.resultCode}/${model.resultMsg}")

                    stdResult.resultCode = model.resultCode
                    stdResult.resultMsg = model.resultMsg

                    if (model.resultCode == 0) {

                        val contentVal2 = ContentValues()
                        contentVal2.put("punchOut_stat", "S")
                        DatabaseHelper.getInstance().update(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal2,
                                "invoice_no=? COLLATE NOCASE and reg_id = ?", arrayOf(pickupNo, Preferences.userId))
                    }
                } catch (e: Exception) {

                    stdResult.resultCode = -15
                    stdResult.resultMsg = e.message
                }

                return@withContext stdResult
            }

    private fun resultDialog(msg: String) {

        if (!this@PickupFailedActivity.isFinishing) {

            val builder = AlertDialog.Builder(this@PickupFailedActivity)
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

        if (!this@PickupFailedActivity.isFinishing) {
            AlertDialog.Builder(this@PickupFailedActivity)
                    .setMessage(R.string.msg_delivered_sign_cancel)
                    .setPositiveButton(R.string.button_ok) { _, _ -> finish() }
                    .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }
                    .show()
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
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

    private fun setFailedCode() {

        arrayList = DataUtil.getFailCode("P")

        if (arrayList == null) {

            DisplayUtil.AlertDialog(this@PickupFailedActivity, resources.getString(R.string.msg_failed_code_error))
        } else {

            failedCodeArrayList = ArrayList()

            for (i in arrayList!!.indices) {
                val failedCode: FailedCodeResult.FailedCode = arrayList!![i]
                failedCodeArrayList!!.add(failedCode.failedString)
            }

            binding.spinnerFailedReason.prompt = resources.getString(R.string.text_failed_reason)
            val failedCodeArrayAdapter = ArrayAdapter(this@PickupFailedActivity, android.R.layout.simple_spinner_item, failedCodeArrayList!!)
            failedCodeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFailedReason.adapter = failedCodeArrayAdapter
        }
    }

    private fun getRestDay(year: Int, month: Int, day: Int): String {

        val yearDate = year.toString()
        var monthDate = month.toString()
        var dayDate = day.toString()

        if (monthDate.length == 1) {

            monthDate = "0$month"
        }

        if (dayDate.length == 1) {

            dayDate = "0$day"
        }

        val restDate = "$yearDate-$monthDate-$dayDate"
        var restDayTitle = ""


        val cs = DatabaseHelper.getInstance()["SELECT title FROM ${DatabaseHelper.DB_TABLE_REST_DAYS} WHERE rest_dt='$restDate'"]
        if (cs.moveToFirst()) {

            restDayTitle = cs.getString(cs.getColumnIndex("title"))
        }

        return restDayTitle
    }
}