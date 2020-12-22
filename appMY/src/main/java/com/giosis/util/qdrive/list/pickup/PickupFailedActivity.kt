package com.giosis.util.qdrive.list.pickup

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
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
import com.giosis.library.server.data.FailedCodeResult
import com.giosis.util.qdrive.gps.GPSTrackerManager
import com.giosis.util.qdrive.international.MyApplication
import com.giosis.util.qdrive.international.OnServerEventListener
import com.giosis.util.qdrive.international.R
import com.giosis.util.qdrive.util.*
import com.giosis.util.qdrive.util.ui.CommonActivity
import kotlinx.android.synthetic.main.activity_pickup_visit_log.*
import kotlinx.android.synthetic.main.top_title.*
import java.text.SimpleDateFormat
import java.util.*

// 일반 Pickup 실패  &  CNR Pickup 실패

class PickupFailedActivity : CommonActivity(), Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {

    val tag = "PickupFailedActivity"
    private val context = MyApplication.getContext()

    private val userId = MyApplication.preferences.userId
    private val officeCode = MyApplication.preferences.officeCode
    private val deviceId = MyApplication.preferences.deviceUUID

    private lateinit var pickupType: String     // P, CNR
    private lateinit var pickupNo: String
    private lateinit var rcvType: String        // VL, RC

    // Location
    private var gpsTrackerManager: GPSTrackerManager? = GPSTrackerManager(context)

    // Camera & Gallery
    private val camera2 = Camera2APIs(this)
    private var cameraId: String? = null
    private val RESULT_LOAD_IMAGE = 2000

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA)

    //
    var arrayList: ArrayList<FailedCodeResult.FailedCode>? = null
    var failedCodeArrayList: ArrayList<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        /* setShowWhenLocked(true)
         setTurnScreenOn(true)*/

        // 어플이 사용되는 동안 화면 끄지 않기
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Lock 상태이면 보여주지 않음. (Lock 해제해야만 보임)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        // Lock 상태이면 보여줌
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        // 화면 ON
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        setContentView(R.layout.activity_pickup_visit_log)


        text_top_title.text = context.resources.getString(R.string.text_visit_log)
        pickupNo = intent.getStringExtra("pickupNo")!!
        pickupType = intent.getStringExtra("type")!!
        text_sign_p_f_pickup_no.text = pickupNo
        text_sign_p_f_applicant.text = intent.getStringExtra("applicant")
        text_sign_p_f_requested_qty.text = intent.getStringExtra("reqQty")

        if (pickupType == BarcodeType.TYPE_PICKUP) {

            text_sign_p_f_applicant_title.text = context.resources.getString(R.string.text_applicant)
            rcvType = "VL"
        } else if (pickupType == BarcodeType.TYPE_CNR) {

            text_sign_p_f_applicant_title.text = context.resources.getString(R.string.text_requestor)
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

            when {
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> {

                    Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_choose_sunday_error), Toast.LENGTH_SHORT).show()
                    text_sign_p_f_retry_date.text = context.resources.getString(R.string.text_select)
                }
                restDay.isNotEmpty() -> {

                    Toast.makeText(this@PickupFailedActivity, "$restDay " + context.resources.getString(R.string.msg_choose_another_day), Toast.LENGTH_SHORT).show()
                    text_sign_p_f_retry_date.text = context.resources.getString(R.string.text_select)
                }
                else -> {

                    val dateFormat = "yyyy-MM-dd"
                    val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
                    text_sign_p_f_retry_date.text = simpleDateFormat.format(calendar.time)
                }
            }
        }


        val datePickupDialog = DatePickerDialog(this@PickupFailedActivity,
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))

        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val maxCalendar = Calendar.getInstance()
        maxCalendar.add(Calendar.DAY_OF_YEAR, 7)

        datePickupDialog.datePicker.minDate = minCalendar.timeInMillis
        datePickupDialog.datePicker.maxDate = maxCalendar.timeInMillis


        // 2020.12  Pickup Failed Code
        setFailedCode()

        spinner_p_f_failed_reason.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {

                val reason = p0.getItemAtPosition(p2).toString()
                text_sign_p_f_failed_reason.text = reason
            }
        }

        edit_sign_p_f_memo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (99 <= edit_sign_p_f_memo.length()) {

                    Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
                }
            }
        })


        layout_top_back.setOnClickListener {

            cancelUpload()
        }

        layout_sign_p_f_retry_date.setOnClickListener {

            datePickupDialog.show()
        }

        layout_sign_p_f_failed_reason.setOnClickListener {

            spinner_p_f_failed_reason.performClick()
        }

        layout_sign_p_f_take_photo.setOnClickListener {

            if (cameraId != null) {

                camera2.takePhoto(texture_sign_p_f_preview, img_sign_p_f_visit_log)
            } else {

                Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show()
            }
        }

        layout_sign_p_f_gallery.setOnClickListener {

            getImageFromGallery()
        }

        btn_sign_p_f_save.setOnClickListener {

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


    private fun setFailedCode() {

        arrayList = DataUtil.getFailCode("P")

        if (arrayList == null) {

            DisplayUtil.AlertDialog(this@PickupFailedActivity, context.resources.getString(R.string.msg_failed_code_error))
        } else {

            failedCodeArrayList = ArrayList()

            for (i in arrayList!!.indices) {
                val failedCode: FailedCodeResult.FailedCode = arrayList!![i]
                failedCodeArrayList!!.add(failedCode.failedString)
            }

            spinner_p_f_failed_reason.prompt = context.resources.getString(R.string.text_failed_reason)
            val failedCodeArrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, failedCodeArrayList!!)
            failedCodeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner_p_f_failed_reason.adapter = failedCodeArrayAdapter
        }
    }


    override fun onResume() {
        super.onResume()


        if (isPermissionTrue) {

            if (texture_sign_p_f_preview.isAvailable) {

                openCamera()
            } else {

                texture_sign_p_f_preview.surfaceTextureListener = this
            }

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

    // Camera
    private fun openCamera() {

        val cameraManager: CameraManager = camera2.getCameraManager(this)
        cameraId = camera2.getCameraCharacteristics(cameraManager)

        if (cameraId != null) {

            camera2.setCameraDevice(cameraManager, cameraId)
        } else {

            Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Size, rotation: Int) {

        texture_sign_p_f_preview.rotation = rotation.toFloat()

        val texture = texture_sign_p_f_preview.surfaceTexture
        texture?.setDefaultBufferSize(cameraSize.width, cameraSize.height)

        val surface = Surface(texture)
        camera2.setCaptureSessionRequest(cameraDevice, surface)
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
    private fun getImageFromGallery() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE)
    }


    private fun cancelUpload() {

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setMessage(context.resources.getString(R.string.msg_delivered_sign_cancel))

        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { _, _ ->

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

                DisplayUtil.AlertDialog(this@PickupFailedActivity, context.resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (text_sign_p_f_failed_reason.text == context.resources.getString(R.string.text_select)) {

                Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_select_fail_reason), Toast.LENGTH_SHORT).show()
                return
            }

            val retryDay = text_sign_p_f_retry_date.text.toString()
            if (retryDay == context.resources.getString(R.string.text_select)) {

                Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_select_retry_date), Toast.LENGTH_SHORT).show()
                return
            }

            val driverMemo = edit_sign_p_f_memo.text.toString()
            if (driverMemo.isEmpty()) {

                Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show()
                return
            }

            if (!camera2.hasImage(img_sign_p_f_visit_log)) {

                Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.msg_visit_photo_require), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {

                DisplayUtil.AlertDialog(this@PickupFailedActivity, context.resources.getString(R.string.msg_disk_size_error))
                return
            }


            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {

                latitude = it.latitude
                longitude = it.longitude
            }
            Log.i(tag, "  Location $latitude / $longitude")


            val code: FailedCodeResult.FailedCode = arrayList!![spinner_p_f_failed_reason.selectedItemPosition]
            val failedCode: String = code.failedCode
            Log.e("krm0219", "Fail Reason Code  >  $failedCode")
            DataUtil.logEvent("button_click", tag, com.giosis.library.util.DataUtil.requestSetUploadPickupData)


            PickupFailedUploadHelper.Builder(this, userId, officeCode, deviceId,
                    rcvType, pickupNo, failedCode, retryDay, driverMemo, img_sign_p_f_visit_log,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(object : OnServerEventListener {
                        override fun onPostResult() {

                            DataUtil.inProgressListPosition = 0
                            finish()
                        }

                        override fun onPostFailList() {}
                    }).build().execute()
        } catch (e: Exception) {

            Log.e(tag, "   serverUpload  Exception : $e")
            Toast.makeText(this@PickupFailedActivity, context.resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {

        cancelUpload()
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
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {

            data?.let {
                try {

                    val imageUri = data.data
                    val selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val resizeImage = camera2.getResizeBitmap(selectedImage)
                    img_sign_p_f_visit_log.setImageBitmap(resizeImage)
                    img_sign_p_f_visit_log.scaleType = ImageView.ScaleType.CENTER_INSIDE
                } catch (e: Exception) {
                }
            }
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


        val cs = DatabaseHelper.getInstance().get("SELECT title FROM ${DatabaseHelper.DB_TABLE_REST_DAYS} WHERE rest_dt='$restDate'")

        if (cs != null && cs.moveToFirst()) {

            restDayTitle = cs.getString(cs.getColumnIndex("title"))
        }

        return restDayTitle
    }
}