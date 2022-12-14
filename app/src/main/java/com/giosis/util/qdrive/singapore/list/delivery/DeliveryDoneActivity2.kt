package com.giosis.util.qdrive.singapore.list.delivery

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.MemoryStatus.availableInternalMemorySize
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.databinding.ActivityDeliveredBinding
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.list.BarcodeData
import com.giosis.util.qdrive.singapore.list.OutletInfo
import com.giosis.util.qdrive.singapore.list.RowItem
import com.giosis.util.qdrive.singapore.listener.setOnSingleClickListener
import com.giosis.util.qdrive.singapore.server.ImageUpload
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.giosis.util.qdrive.singapore.util.Camera2APIs.Camera2Interface
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/***************
 * LIST, In Progress > 'Delivered'  // SCAN > Delivery Done
 * 2020.06 사진 추가
 */
class DeliveryDoneActivity2 : CommonActivity(), Camera2Interface,
    SurfaceTextureListener {

    companion object {
        private const val RESULT_LOAD_IMAGE = 3
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            PermissionChecker.READ_EXTERNAL_STORAGE,
            PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.ACCESS_FINE_LOCATION,
            PermissionChecker.CAMERA
        )
    }

    var TAG = "DeliveryDoneActivity"


    var mReceiveType = "RC"
    var routeNumber: String? = null
    var barcodeList = ArrayList<String>()// 바코드 리스트만 가지고 있으면 된다..

    // Camera & Gallery
    val camera2 by lazy {
        Camera2APIs(this)
    }

    var cameraId: String? = null
    var isClickedPhoto = false

    // GPS
    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false
    var latitude = 0.0
    var longitude = 0.0

    // Outlet
    var outletInfo = OutletInfo()
    var showQRCode = false
    var isPermissionTrue = false

    private val progressBar by lazy {
        ProgressDialog(this@DeliveryDoneActivity2)
    }
    val binding by lazy {
        ActivityDeliveredBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.appBar.textTopTitle.setText(R.string.text_delivered)

        binding.appBar.layoutTopBack.setOnClickListener {
            cancelSigning()
        }

        binding.receiverSelfLayout.setOnClickListener {
            binding.imgSignDReceiverSelf.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
            binding.imgSignDReceiverSubstitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
            binding.imgSignDReceiverOther.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
            mReceiveType = "RC"
        }

        binding.receiverSubstituteLayout.setOnClickListener {
            binding.imgSignDReceiverSelf.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
            binding.imgSignDReceiverSubstitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
            binding.imgSignDReceiverOther.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
            mReceiveType = "AG"
        }

        binding.receiverOtherLayout.setOnClickListener {
            binding.imgSignDReceiverSelf.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
            binding.imgSignDReceiverSubstitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
            binding.imgSignDReceiverOther.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
            mReceiveType = "ET"
        }

        binding.layoutSignDSignEraser.setOnClickListener {
            binding.signViewSignDSignature.clearText()
        }

        binding.layoutSignDTakePhoto.setOnClickListener {
            if (cameraId != null) {
                if (!isClickedPhoto) {  // Camera CaptureSession 완료되면 다시 클릭할 수 있도록 수정
                    isClickedPhoto = true
                    camera2.takePhoto(binding.textureSignDPreview, binding.imgSignDVisitLog)
                }
            } else {
                Toast.makeText(
                    this@DeliveryDoneActivity2,
                    resources.getString(R.string.msg_back_camera_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.layoutSignDGallery.setOnSingleClickListener {
            getImageFromAlbum()
        }

        binding.btnSignDSave.setOnClickListener {
            confirmSigning()
        }

        if (intent.hasExtra("parcel")) {
            // in List (단건)
            val parcel = intent.getSerializableExtra("parcel") as RowItem

            barcodeList.add(parcel.shipping.uppercase(Locale.getDefault()))

            if (parcel.route.contains("7E") || parcel.route.contains("FL")) {
                routeNumber = try {
                    val routeSplit = parcel.route.split(" ").toTypedArray()
                    routeSplit[0] + " " + routeSplit[1]
                } catch (e: Exception) {
                    null
                }
            }

        } else if (intent.hasExtra("data")) {
            // in Capture (bulk)
            val list = intent.getSerializableExtra("data") as ArrayList<BarcodeData>

            for (dataItem in list) {
                val trackingNo = dataItem.barcode!!.uppercase(Locale.getDefault())

                barcodeList.add(trackingNo)
            }
        }

        val barcodeMsg = TextUtils.join(",", barcodeList)
        binding.textSignDTrackingNoTitle.setText(R.string.text_tracking_no)

        if (barcodeList.size == 1) {
            binding.textSignDTrackingNo.text = barcodeMsg.toString().trim()
            binding.textSignDTrackingNoMore.visibility = View.GONE
        } else {
            // 다수건
            val qtyFormat = String.format(
                resources.getString(R.string.text_total_qty_count),
                barcodeList.size
            )
            binding.textSignDTrackingNo.text = qtyFormat
            binding.textSignDTrackingNoMore.visibility = View.VISIBLE
            binding.textSignDTrackingNoMore.text = barcodeMsg.toString()
            binding.layoutSignDSender.visibility = View.GONE
        }

        getDeliveryInfo(barcodeList[0])

        DisplayUtil.setPreviewCamera(binding.imgSignDPreviewBg)

        // NOTIFICATION.  Outlet Delivery
        if (outletInfo.route != null) {
            if (outletInfo.route!!.substring(0, 2).contains("7E")
                || outletInfo.route!!.substring(0, 2).contains("FL")
            ) {
                binding.layoutSignDOutletAddress.visibility = View.VISIBLE
                binding.textSignDOutletAddress.text =
                    "(" + outletInfo.zip_code + ") " + outletInfo.address

                try {

                    Log.e(TAG, "Operation Address : " + outletInfo.address)
                    // ex: CLEMENTI MRT STATION 3150 COMMONWEALTH AVENUE WEST #02-01 (Operation hours: 24 hours)

                    val splitAddress = outletInfo.address!!.split(":")
                    Log.e(
                        TAG,
                        "hours =>>> ${splitAddress[1].subSequence(0, splitAddress[1].length - 1)}"
                    )

                    val operationHour =
                        splitAddress[1].subSequence(0, splitAddress[1].length - 1).toString()

                    if (operationHour.isNotEmpty()) {
                        binding.layoutSignDOutletOperationHour.visibility = View.VISIBLE
                        binding.textSignDOutletOperationTime.text = operationHour
                    }

                    val splitAddress2 = outletInfo.address!!.split("(")
                    Log.e(TAG, "address ==>>> ${splitAddress2[0].trim()}")

                    binding.textSignDOutletAddress.text =
                        "(" + outletInfo.zip_code + ") " + splitAddress2[0].trim()
                } catch (e: Exception) {

                }

                binding.textSignDTrackingNoMore.visibility = View.GONE
                binding.layoutSignDReceiver.visibility = View.GONE
                binding.outletRecycler.visibility = View.VISIBLE

                val outletList = ArrayList<OutletDeliveryItem>()

                if (routeNumber == null) {
                    for (i in barcodeList.indices) {
                        val cs =
                            DatabaseHelper.getInstance()["SELECT rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                                    + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + Preferences.userId + "' and invoice_no='" + barcodeList[i] + "'"]

                        if (cs.moveToFirst()) {
                            do {
                                val receiver = cs.getString(cs.getColumnIndex("rcv_nm"))

                                val item = OutletDeliveryItem().apply {
                                    this.trackingNo = barcodeList[i]
                                    this.receiverName = receiver
                                }

                                outletList.add(item)

                            } while (cs.moveToNext())
                        }
                    }
                } else {

                    val cs =
                        DatabaseHelper.getInstance()["SELECT invoice_no, rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                                + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + Preferences.userId + "' and route LIKE '%" + routeNumber + "%'"]

                    if (cs.moveToFirst()) {
                        do {
                            val invoiceNo = cs.getString(cs.getColumnIndex("invoice_no"))
                            val receiver = cs.getString(cs.getColumnIndex("rcv_nm"))

                            val item = OutletDeliveryItem().apply {
                                this.trackingNo = invoiceNo
                                this.receiverName = receiver
                            }

                            outletList.add(item)

                        } while (cs.moveToNext())
                    }

                    if (outletList.size > 1) {
                        val qtyFormat = String.format(
                            resources.getString(R.string.text_total_qty_count),
                            outletList.size
                        )
                        binding.textSignDTrackingNoTitle.setText(R.string.text_parcel_qty1)
                        binding.textSignDTrackingNo.text = qtyFormat
                        binding.layoutSignDSender.visibility = View.GONE
                    }
                }

                if (outletInfo.route!!.substring(0, 2).contains("7E")) {

                    binding.appBar.textTopTitle.setText(R.string.text_title_7e_store_delivery)
                    binding.textSignDOutletAddressTitle.setText(R.string.text_7e_store_address)
                    binding.layoutSignDSignMemo.visibility = View.VISIBLE
                    binding.layoutSignDVisitLog.visibility = View.GONE

                    val progressDialog = android.app.ProgressDialog(this)
                    progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
                    progressDialog.setMessage(resources.getString(R.string.text_downloading))
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    progressDialog.max = outletList.size

                    lifecycleScope.launch {

                        var resultCode = -1
                        try {
                            for ((index,item) in outletList.withIndex()) {
                                progressDialog.progress = index
                                val response =
                                    RetrofitClient.instanceDynamic().qrCodeForQStationDelivery(
                                        item.trackingNo!!
                                    )

                                if (response.result_code == "0" && response.qrcode_data != null) {
                                    val result = Gson().fromJson(
                                        response.qrcode_data,
                                        QRCodeData::class.java
                                    )

                                    if (result.q == "D" && !(result.jobID.isNullOrEmpty())) {

                                        item.jobID = result.jobID
                                        item.vendorCode = result.vendorCode
                                        item.qrCode = DataUtil.qrcode_url + response.qrcode_data

                                    } else {
                                        resultCode = response.result_code!!.toInt()
                                        break
                                    }
                                }

                                resultCode = response.result_code!!.toInt()
                            }

                        } catch (e: java.lang.Exception) {
                            resultCode = -1
                        }

                        progressDialog.hide()

                        if (resultCode == 0) {

                            showQRCode = true

                            val adapter = Outlet7ETrackingNoAdapter(outletList,"7E")
                            binding.outletRecycler.adapter = adapter

                        } else {
                            showQRCode = false
                            Toast.makeText(
                                this@DeliveryDoneActivity2,
                                resources.getString(R.string.msg_outlet_qrcode_data_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } else {

                    binding.appBar.textTopTitle.setText(R.string.text_title_fl_delivery)
                    binding.textSignDOutletAddressTitle.setText(R.string.text_federated_locker_address)
                    binding.layoutSignDSignMemo.visibility = View.GONE
                    binding.layoutSignDVisitLog.visibility = View.GONE

                    val adapter = Outlet7ETrackingNoAdapter(outletList,"FL")
                    binding.outletRecycler.adapter = adapter

                }

            } else {

                binding.layoutSignDOutletAddress.visibility = View.GONE
                binding.layoutSignDOutletOperationHour.visibility = View.GONE
                binding.layoutSignDReceiver.visibility = View.VISIBLE
                binding.outletRecycler.visibility = View.GONE
                binding.layoutSignDSignMemo.visibility = View.VISIBLE
                binding.layoutSignDVisitLog.visibility = View.VISIBLE

            }
        } else {
            binding.layoutSignDOutletAddress.visibility = View.GONE
            binding.layoutSignDOutletOperationHour.visibility = View.GONE
            binding.layoutSignDReceiver.visibility = View.VISIBLE
            binding.outletRecycler.visibility = View.GONE
            binding.layoutSignDSignMemo.visibility = View.VISIBLE
            binding.layoutSignDVisitLog.visibility = View.VISIBLE
        }

        // Memo 입력제한
        binding.editSignDMemo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (99 <= binding.editSignDMemo.length()) {
                    Toast.makeText(
                        this@DeliveryDoneActivity2,
                        resources.getText(R.string.msg_memo_too_long),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        // 권한 여부 체크 (없으면 true, 있으면 false)
        val checker = PermissionChecker(this)
        if (checker.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {
            isPermissionTrue = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", "$TAG   onActivityResult  PERMISSIONS_GRANTED")
                isPermissionTrue = true
            }
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

    override fun onBackPressed() {
        cancelSigning()
    }

    override fun onResume() {
        super.onResume()
        if (isPermissionTrue) {
            // Camera
            if (!outletInfo.route!!.substring(0, 2).contains("7E")) {

                // When the screen is turned off and turned back on, the SurfaceTexture is already available.
                if (binding.textureSignDPreview.isAvailable) {

                    openCamera("onResume")
                } else {
                    binding.textureSignDPreview.surfaceTextureListener = this
                }
            }

            // Location
            gpsTrackerManager = GPSTrackerManager(this)
            gpsEnable = gpsTrackerManager!!.enableGPSSetting()
            if (gpsEnable && gpsTrackerManager != null) {
                gpsTrackerManager!!.gpsTrackerStart()
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
            } else {
                DataUtil.enableLocationSettings(this@DeliveryDoneActivity2)
            }
        }
    }

    private fun confirmSigning() {
        if (outletInfo.route!!.contains("7E")) {
            if (showQRCode) {        // QR Code Show
                saveOutletDeliveryDone()
            } else {                // QR Code Not Show... > 진행 불가능
                Toast.makeText(
                    this@DeliveryDoneActivity2,
                    resources.getString(R.string.msg_outlet_qrcode_require),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (outletInfo.route!!.contains("FL")) {
            saveOutletDeliveryDone()
        } else {
            saveServerUploadSign()
        }
    }

    private fun getDeliveryInfo(barcodeNo: String?) {
        val cursor =
            DatabaseHelper.getInstance()["SELECT rcv_nm, sender_nm, route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE"]

        if (cursor.moveToFirst()) {
            try {
                binding.textSignDSender.text =
                    cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"))

                binding.textSignDReceiver.text =
                    cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"))

                outletInfo.route = cursor.getString(cursor.getColumnIndexOrThrow("route"))
                outletInfo.zip_code = cursor.getString(cursor.getColumnIndexOrThrow("zip_code"))
                outletInfo.address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            } catch (e: Exception) {

            }
        }

        cursor.close()
    }

    private fun cancelSigning() {
        AlertDialog.Builder(this)
            .setMessage(R.string.msg_delivered_sign_cancel)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }
            .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }.show()
    }


    private fun saveServerUploadSign() {
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setMessage(resources.getString(R.string.text_downloading))
        progressDialog.setCancelable(false)

        try {
            if (!NetworkUtil.isNetworkAvailable(this)) {
                alertShow(resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (gpsTrackerManager != null) {
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
            }

            val driverMemo = binding.editSignDMemo.text.toString()

            // 사인 or 사진 둘 중 하나는 있어야 함
            val hasSignImage = binding.signViewSignDSignature.isTouch
            val hasVisitImage = camera2.hasImage(binding.imgSignDVisitLog)

            val reformatBarcodeList = ArrayList<String>()

            for (item in barcodeList) {
                reformatBarcodeList.add("'$item'")
            }

            val highAmountCount =
                DatabaseHelper.getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                        + " WHERE high_amount_yn = 'Y' AND invoice_no IN ("
                        + TextUtils.join(",", reformatBarcodeList) + ")"].count

            if (highAmountCount > 0) {
                if (!hasSignImage || !hasVisitImage) {
                    val msg = resources.getString(R.string.msg_high_amount_sign_photo)
                    Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                if (!hasSignImage && !hasVisitImage) {
                    val msg = "${resources.getString(R.string.msg_signature_require)} or${
                        resources.getString(R.string.msg_visit_photo_require)
                    }"
                    Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
                    return
                }
            }

            //서버에 올리기전 용량체크  내장메모리가 100Kbyte 안남은경우
            if (availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {
                alertShow(resources.getString(R.string.msg_disk_size_error))
                return
            }

            FirebaseEvent.clickEvent(this, TAG, "SetDeliveryUploadData")


            progressDialog.show()
            progressDialog.max = barcodeList.size

            lifecycleScope.launch {

                var resultMsg = ""
                try {
                    for ((index,item) in barcodeList.withIndex()) {

                        progressDialog.progress = index
                        var bitmapString = ""
                        if (binding.signViewSignDSignature.isTouch) {
                            DataUtil.capture(
                                "/Qdrive",
                                item,
                                binding.signViewSignDSignature
                            )

                            bitmapString = QDataUtil.getBitmapString(
                                this@DeliveryDoneActivity2,
                                binding.signViewSignDSignature,
                                ImageUpload.QXPOD,
                                "qdriver/sign",
                                item
                            )
                        }

                        var bitmapString2 = ""
                        if (camera2.hasImage(binding.imgSignDVisitLog)) {
                            DataUtil.capture(
                                "/Qdrive",
                                item + "_1",
                                binding.imgSignDVisitLog
                            )

                            bitmapString2 = QDataUtil.getBitmapString(
                                this@DeliveryDoneActivity2,
                                binding.imgSignDVisitLog,
                                ImageUpload.QXPOD,
                                "qdriver/delivery",
                                item
                            )
                        }

                        if (bitmapString.isEmpty() && bitmapString2.isEmpty()) {
                            resultDialog(resources.getString(R.string.msg_upload_fail_image))
                            return@launch
                        }

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val date = Date()

                        val contentVal = ContentValues()
                        contentVal.put("stat", StatueType.DELIVERY_DONE)
                        contentVal.put("rcv_type", mReceiveType)
                        contentVal.put("driver_memo", driverMemo)
                        contentVal.put("chg_dt", dateFormat.format(date))
                        contentVal.put("fail_reason", "")

                        DatabaseHelper.getInstance().update(
                            DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                            contentVal,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                            arrayOf(item, Preferences.userId)
                        )

                        val response = RetrofitClient.instanceDynamic().setDeliveryUploadData(
                            StatueType.DELIVERY_DONE,
                            mReceiveType,
                            NetworkUtil.getNetworkType(this@DeliveryDoneActivity2),
                            item,
                            bitmapString,
                            bitmapString2,
                            driverMemo,
                            latitude,
                            longitude,
                            "QR",
                            ""
                        )

                        if (response.resultCode == 0) {
                            val contentVal2 = ContentValues()
                            contentVal2.put("punchOut_stat", "S")

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal2,
                                "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                                arrayOf(item, Preferences.userId)
                            )
                        } else if (response.resultCode == -25) {
                            // A 드라이버 한테 화물이 할당되어 있는데 B 드라이버 (나) 가 처리할려고 하는 경우
                            DatabaseHelper.getInstance().delete(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                "invoice_no= '$item' COLLATE NOCASE"
                            )
                        }
                        resultMsg = response.resultMsg!!
                    }

                } catch (e: Exception) {
                    resultMsg = e.toString()
                }

                progressDialog.hide()
                resultDialog(resultMsg)
            }

        } catch (e: Exception) {
            progressDialog.hide()

            Toast.makeText(
                this,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun alertShow(msg: String) {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.text_warning))
            .setMessage(msg).setPositiveButton(
                resources.getString(R.string.button_close)
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                finish()
            }.show()
    }

    private fun saveOutletDeliveryDone() {
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setMessage(resources.getString(R.string.text_downloading))
        progressDialog.setCancelable(false)

        try {
            if (!NetworkUtil.isNetworkAvailable(this)) {
                alertShow(resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (gpsTrackerManager != null) {
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
            }

            if (outletInfo.route!!.substring(0, 2).contains("7E")) {
                if (!binding.signViewSignDSignature.isTouch) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.msg_signature_require),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }

            if (availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {
                alertShow(resources.getString(R.string.msg_disk_size_error))
                return
            }

            val driverMemo = binding.editSignDMemo.text.toString()

            FirebaseEvent.clickEvent(this, TAG + "_OUTLET", "SetOutletDeliveryUploadData")

            progressDialog.show()
            progressDialog.max = barcodeList.size

            lifecycleScope.launch {

                var resultMsg = ""

                for ((index,item) in barcodeList.withIndex()) {
                    progressDialog.progress = index
                    var bitmap1 = ""

                    if (outletInfo.route!!.substring(0, 2) == "7E") {
                        DataUtil.capture("/Qdrive", item, binding.signViewSignDSignature)

                        bitmap1 = QDataUtil.getBitmapString(
                            this@DeliveryDoneActivity2,
                            binding.signViewSignDSignature,
                            ImageUpload.QXPOD,
                            "qdriver/sign",
                            item
                        )

                        if (bitmap1 == "") {
                            resultDialog(resources.getString(R.string.msg_upload_fail_image))
                            return@launch
                        }
                    }

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val date = Date()

                    val contentVal = ContentValues()
                    contentVal.put("stat", StatueType.DELIVERY_START)
                    contentVal.put("rcv_type", mReceiveType)
                    contentVal.put("driver_memo", driverMemo)
                    contentVal.put("chg_dt", dateFormat.format(date))
                    contentVal.put("fail_reason", "")

                    DatabaseHelper.getInstance().update(
                        DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                        contentVal,
                        "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                        arrayOf(item, Preferences.userId)
                    )

                    try {
                        val response = RetrofitClient.instanceDynamic().outletDeliveryUploadData(
                            mReceiveType,
                            bitmap1,
                            NetworkUtil.getNetworkType(this@DeliveryDoneActivity2),
                            item,
                            driverMemo,
                            latitude,
                            longitude,
                            outletInfo.route!!.substring(0, 2)
                        )

                        if (response.resultCode == 0) {
                            val contentVal2 = ContentValues()
                            contentVal2.put("punchOut_stat", "S")

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal2,
                                "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                                arrayOf(item, Preferences.userId)
                            )
                        } else if (response.resultCode == -25) {
                            // A 드라이버 한테 화물이 할당되어 있는데 B 드라이버 (나) 가 처리할려고 하는 경우
                            DatabaseHelper.getInstance().delete(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                "invoice_no= '$item' COLLATE NOCASE"
                            )
                        }
                        resultMsg = response.resultMsg!!
                    } catch (e: java.lang.Exception) {
                        resultMsg = resources.getString(R.string.msg_upload_fail_15)
                    }
                }

                progressDialog.hide()
                resultDialog(resultMsg)
            }

        } catch (e: Exception) {
            progressDialog.hide()

            Log.e("Exception", "saveOutletDeliveryDone   Exception ; $e")
            Toast.makeText(
                this,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gallery
    private fun getImageFromAlbum() {
        try {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(
                Intent.createChooser(intent,"Select Picture")
            )
        } catch (ex: java.lang.Exception) {
        }
    }

    // CAMERA
    private fun openCamera(it: String) {
        val cameraManager = camera2.getCameraManager(this)
        cameraId = camera2.getCameraCharacteristics(cameraManager)
        Log.e("Camera", "$TAG  openCamera $cameraId   >>> $it")
        if (cameraId != null) {
            camera2.setCameraDevice(cameraManager, cameraId)
        } else {
            Toast.makeText(
                this@DeliveryDoneActivity2,
                resources.getString(R.string.msg_back_camera_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            try {
                val selectedImageUri = it.data!!.data
                val selectedImage =
                    MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                val resizeBitmap = camera2.getResizeBitmap(selectedImage)
                binding.imgSignDVisitLog.setImageBitmap(resizeBitmap)
                binding.imgSignDVisitLog.scaleType = ImageView.ScaleType.CENTER_INSIDE

                onResume()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun closeCamera() {
        camera2.closeCamera()
    }

    override fun onCameraDeviceOpened(
        cameraDevice: CameraDevice,
        cameraSize: Size,
        rotation: Int,
        it: String
    ) {
        Log.e("Camera", "onCameraDeviceOpened  $it")
        binding.textureSignDPreview.rotation = rotation.toFloat()
        try {
            val texture = binding.textureSignDPreview.surfaceTexture
            texture!!.setDefaultBufferSize(cameraSize.width, cameraSize.height)
            val surface = Surface(texture)
            camera2.setCaptureSessionRequest(cameraDevice, surface)
        } catch (e: Exception) {
            Log.e("Exception", "onCameraDeviceOpened  Exception : $e")
        }
    }

    override fun onCaptureCompleted() {
        isClickedPhoto = false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        openCamera("onSurfaceTextureAvailable")
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    private fun resultDialog(msg: String) {
        if (!isFinishing) {
            AlertDialog.Builder(this@DeliveryDoneActivity2)
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