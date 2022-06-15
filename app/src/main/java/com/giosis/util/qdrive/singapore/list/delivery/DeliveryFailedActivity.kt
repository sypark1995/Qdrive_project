package com.giosis.util.qdrive.singapore.list.delivery


import android.app.Activity
import android.app.AlertDialog
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
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.data.FailedCodeData
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.databinding.ActivityDeliveryVisitLogBinding
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.server.ImageUpload
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class DeliveryFailedActivity : CommonActivity(), Camera2APIs.Camera2Interface,
    SurfaceTextureListener {

    val tag = "DeliveryFailedActivity"

    private val binding by lazy {
        ActivityDeliveryVisitLogBinding.inflate(layoutInflater)
    }

    private val progressBar by lazy {
        ProgressDialog(this@DeliveryFailedActivity)
    }

    lateinit var trackingNo: String

    // Location
    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    // Camera & Gallery
    private val camera2 = Camera2APIs(this@DeliveryFailedActivity)
    private var cameraId: String? = null
    var isClickedPhoto = false

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS = arrayOf(
        PermissionChecker.ACCESS_FINE_LOCATION,
        PermissionChecker.ACCESS_COARSE_LOCATION,
        PermissionChecker.READ_EXTERNAL_STORAGE,
        PermissionChecker.WRITE_EXTERNAL_STORAGE,
        PermissionChecker.CAMERA
    )

    //
    var arrayList: ArrayList<FailedCodeData>? = null
    var failedCodeArrayList: ArrayList<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)     // 어플이 사용되는 동안 화면 끄지 않기
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)   // Lock 상태이면 보여주지 않음. (Lock 해제해야만 보임)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)   // Lock 상태이면 보여줌
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)     // 화면 ON
        setContentView(binding.root)

        FirebaseEvent.createEvent(this, tag)

        binding.layoutTopTitle.textTopTitle.text = resources.getString(R.string.text_visit_log)
        trackingNo = intent.getStringExtra("trackingNo").toString()
        binding.textTrackingNo.text = trackingNo
        binding.textReceiver.text = intent.getStringExtra("receiverName")
        binding.textSender.text = intent.getStringExtra("senderName")

        DisplayUtil.setPreviewCamera(binding.imgPreviewBg)

        // 2020.12  Delivery Failed Code
        setFailedCode()

        binding.layoutFailedReason.setOnClickListener {
            binding.spinnerFailedReason.performClick()
        }

        binding.spinnerFailedReason.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    arg1: View?,
                    position: Int,
                    arg3: Long
                ) {

                    val reason = parentView.getItemAtPosition(position).toString()

                    if (reason.toUpperCase()
                            .contains(resources.getString(R.string.text_other).toUpperCase())
                    ) {

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
                    Toast.makeText(
                        this@DeliveryFailedActivity,
                        resources.getString(R.string.msg_memo_too_long),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })


        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            cancelUpload()
        }

        binding.layoutTakePhoto.setOnClickListener {

            if (cameraId != null) {
                if (!isClickedPhoto) {

                    isClickedPhoto = true
                    camera2.takePhoto(binding.texturePreview, binding.imgVisitLog)
                }
            } else {

                Toast.makeText(
                    this@DeliveryFailedActivity,
                    resources.getString(R.string.msg_back_camera_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.layoutGallery.setOnClickListener {
            getGalleryImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            serverUpload()
        }

        // permission
        val checker = PermissionChecker(this@DeliveryFailedActivity)

        if (checker.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this@DeliveryFailedActivity,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
            overridePendingTransition(0, 0)
        } else {
            isPermissionTrue = true
        }
    }


    private fun setFailedCode() {

        arrayList = DataUtil.getFailCode("D")

        if (arrayList == null) {

            DisplayUtil.AlertDialog(
                this@DeliveryFailedActivity,
                resources.getString(R.string.msg_failed_code_error)
            )
        } else {

            failedCodeArrayList = ArrayList()

            for (i in arrayList!!.indices) {
                failedCodeArrayList!!.add(arrayList!![i].failedString)
            }

            binding.spinnerFailedReason.prompt = resources.getString(R.string.text_failed_reason)
            val failedCodeArrayAdapter = ArrayAdapter(
                this@DeliveryFailedActivity,
                android.R.layout.simple_spinner_item,
                failedCodeArrayList!!
            )
            failedCodeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFailedReason.adapter = failedCodeArrayAdapter
        }
    }


    override fun onResume() {
        super.onResume()

        if (isPermissionTrue) {
            // Camera
            if (binding.texturePreview.isAvailable) {
                openCamera()
            } else {
                binding.texturePreview.surfaceTextureListener = this@DeliveryFailedActivity
            }

            // Location
            gpsTrackerManager = GPSTrackerManager(this@DeliveryFailedActivity)
            gpsTrackerManager?.let {

                gpsEnable = it.enableGPSSetting()
            }

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager!!.gpsTrackerStart()
            } else {

                DataUtil.enableLocationSettings(this@DeliveryFailedActivity)
            }
        }
    }


    // Camera
    private fun openCamera() {

        val cameraManager: CameraManager = camera2.getCameraManager(this@DeliveryFailedActivity)
        cameraId = camera2.getCameraCharacteristics(cameraManager)

        if (cameraId != null) {
            camera2.setCameraDevice(cameraManager, cameraId)
        } else {
            Toast.makeText(
                this@DeliveryFailedActivity,
                resources.getString(R.string.msg_back_camera_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCameraDeviceOpened(
        cameraDevice: CameraDevice,
        cameraSize: Size,
        rotation: Int,
        it: String
    ) {
        Log.e("Camera", "onCameraDeviceOpened  $it")
        binding.texturePreview.rotation = rotation.toFloat()

        val texture = binding.texturePreview.surfaceTexture

        if (texture != null) {
            texture.setDefaultBufferSize(cameraSize.width, cameraSize.height)

            val surface = Surface(texture)
            camera2.setCaptureSessionRequest(cameraDevice, surface)
        }
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
    private val getGalleryImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

            try {
                val selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val resizeImage = camera2.getResizeBitmap(selectedImage)
                binding.imgVisitLog.setImageBitmap(resizeImage)
                binding.imgVisitLog.scaleType = ImageView.ScaleType.CENTER_INSIDE
            } catch (e: Exception) {

            }
        }

    private fun cancelUpload() {

        val alertBuilder = AlertDialog.Builder(this@DeliveryFailedActivity)
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

            if (!NetworkUtil.isNetworkAvailable(this@DeliveryFailedActivity)) {
                DisplayUtil.AlertDialog(
                    this@DeliveryFailedActivity,
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
            Log.e(tag, "  Location $latitude / $longitude")

            // other 선택시에만 메모 필수
            val code = arrayList!![binding.spinnerFailedReason.selectedItemPosition]
            val failedCode: String = code.failedCode
            Log.e(tag, "Fail Reason Code  >  $failedCode")

            var driverMemo = ""
            if (code.failedString.toUpperCase()
                    .contains(resources.getString(R.string.text_other).toUpperCase())
            ) {
                driverMemo = binding.editMemo.text.toString()

                if (driverMemo.isEmpty()) {
                    Toast.makeText(
                        this@DeliveryFailedActivity,
                        resources.getString(R.string.msg_must_enter_memo1),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }

            if (!camera2.hasImage(binding.imgVisitLog)) {
                Toast.makeText(
                    this@DeliveryFailedActivity,
                    resources.getString(R.string.msg_visit_photo_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (MemoryStatus.availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && MemoryStatus.availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {
                DisplayUtil.AlertDialog(
                    this@DeliveryFailedActivity,
                    resources.getString(R.string.msg_disk_size_error)
                )
                return
            }

            FirebaseEvent.clickEvent(this, tag, "DeliveryFailedUploadHelper ")
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                val bitmapString = QDataUtil.getBitmapString(
                    this@DeliveryFailedActivity,
                    binding.imgVisitLog,
                    ImageUpload.QXPOD,
                    "qdriver/sign",
                    trackingNo
                )

                if (bitmapString == "") {
                    resultDialog(resources.getString(R.string.msg_upload_fail_image))
                    return@launch
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = Date()

                val contentVal = ContentValues()
                contentVal.put("stat", StatueType.DELIVERY_FAIL)
                contentVal.put("chg_dt", dateFormat.format(date))
                contentVal.put("fail_reason", failedCode)
                contentVal.put("driver_memo", driverMemo)

                DatabaseHelper.getInstance().update(
                    DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                    contentVal,
                    "invoice_no=? COLLATE NOCASE and reg_id = ?",
                    arrayOf(trackingNo, Preferences.userId)
                )

                try {
                    val response = RetrofitClient.instanceDynamic().setDeliveryUploadData(
                        StatueType.DELIVERY_FAIL,
                        "RC",
                        NetworkUtil.getNetworkType(this@DeliveryFailedActivity),
                        trackingNo,
                        bitmapString,
                        "",
                        driverMemo,
                        latitude,
                        longitude,
                        "QDRIVE",
                        failedCode
                    )

                    when (response.resultCode) {
                        0 -> {
                            val contentVal2 = ContentValues()
                            contentVal2.put("punchOut_stat", "S")

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal2,
                                "invoice_no=? COLLATE NOCASE and reg_id = ?",
                                arrayOf(trackingNo, Preferences.userId)
                            )

                            resultDialog(
                                String.format(
                                    resources.getString(R.string.text_upload_success_count),
                                    1
                                )
                            )
                        }

                        -25 -> {
                            DatabaseHelper.getInstance().delete(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                "invoice_no= '" + trackingNo + "' COLLATE NOCASE"
                            )

                            resultDialog(
                                String.format(
                                    resources.getString(R.string.text_upload_fail_count1),
                                    1,
                                    response.resultMsg
                                )
                            )
                        }

                        else -> {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val date = Date()

                            val contentVal = ContentValues()
                            contentVal.put("stat", StatueType.DELIVERY_FAIL)
                            contentVal.put("chg_dt", dateFormat.format(date))
                            contentVal.put("real_qty", "0") // 업로드시 값 Parse 시 에러나서 0 넘김

                            contentVal.put("fail_reason", failedCode)
                            contentVal.put("driver_memo", driverMemo)
                            contentVal.put("retry_dt", "")
                            contentVal.put("rev_type", "VL")
                            contentVal.put("punchOut_stat", "S")

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal,
                                "invoice_no=? COLLATE NOCASE and punchOut_stat <> 'S' " + "and reg_id = ?",
                                arrayOf(trackingNo, Preferences.userId)
                            )

                            resultDialog(
                                String.format(
                                    resources.getString(R.string.text_upload_fail_count1),
                                    1,
                                    response.resultMsg
                                )
                            )
                        }
                    }

                    progressBar.visibility = View.GONE

                } catch (e: Exception) {
                    progressBar.visibility = View.GONE

                    resultDialog(resources.getString(R.string.msg_upload_fail_15))
                }

            }

        } catch (e: Exception) {

            Log.e(tag, "   serverUpload  Exception : $e")
            Toast.makeText(
                this@DeliveryFailedActivity,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
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
        }
    }

    private fun resultDialog(msg: String) {
        if (!isFinishing) {
            AlertDialog.Builder(this)
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