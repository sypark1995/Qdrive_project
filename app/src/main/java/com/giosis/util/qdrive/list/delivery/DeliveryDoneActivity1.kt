package com.giosis.util.qdrive.list.delivery

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.util.qdrive.barcodescanner.CaptureActivity.BarcodeListData
import com.giosis.util.qdrive.list.BarcodeData
import com.giosis.util.qdrive.list.OutletInfo
import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.util.*
import com.giosis.util.qdrive.util.ui.CommonActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_delivered.*
import kotlinx.android.synthetic.main.top_title.*
import org.json.JSONObject
import java.util.*

class DeliveryDoneActivity1 : CommonActivity(), Camera2APIs.Camera2Interface, SurfaceTextureListener {
    var TAG = "DeliveryDoneActivity1"

    //
    var context: Context? = null
    var opID = ""
    var officeCode = ""
    var deviceID = ""
    var mStrWaybillNo: String? = ""
    var mReceiveType = "RC"
    var mType = BarcodeType.TYPE_DELIVERY
    var routeNumber: String? = null
    var songjanglist: ArrayList<BarcodeData>? = null
    var senderName: String? = null
    var receiverName: String? = null


    // Camera & Gallery
    var camera2: Camera2APIs? = null
    var cameraId: String? = null
    var isGalleryActivate = false

    // GPS
    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false
    var latitude = 0.0
    var longitude = 0.0

    // Outlet
    var outletInfo: OutletInfo? = null
    var jobID: String? = null
    var vendorCode: String? = null
    var showQRCode = false
    var outletDeliveryDoneListItemArrayList: ArrayList<OutletDeliveryDoneListItem>? = null
    var outletTrackingNoAdapter: OutletTrackingNoAdapter? = null
    var isPermissionTrue = false


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered)

        context = applicationContext
        camera2 = Camera2APIs(this)

        opID = MyApplication.preferences.userId
        officeCode = MyApplication.preferences.officeCode
        deviceID = MyApplication.preferences.deviceUUID

        mStrWaybillNo = intent.getStringExtra("waybillNo")
        val barcodeList = intent.getSerializableExtra("data") as ArrayList<BarcodeListData>?
        routeNumber = try {
            val routeType = intent.getStringExtra("route")
            val routeSplit = routeType!!.split(" ").toTypedArray()
            routeSplit[0] + " " + routeSplit[1]
        } catch (e: Exception) {
            null
        }

        // 단건 다수건 바코드정보에 대한 바코드정보 리스트 재정의 songjanglist
        songjanglist = ArrayList()
        if (barcodeList == null) {

            val songData = BarcodeData()
            songData.barcode = mStrWaybillNo!!.toUpperCase()
            songData.state = mType
            songjanglist!!.add(songData)
        } else {

            for (i in 0 until barcodeList.size) {

                val songData = BarcodeData()
                songData.barcode = barcodeList[i].barcode.toUpperCase()
                songData.state = barcodeList[i].state
                songjanglist!!.add(songData)
            }
        }


        var barcodeMsg = ""
        val songJangListSize = songjanglist!!.size
        for (i in 0 until songJangListSize) {
            barcodeMsg += songjanglist!![i].barcode.toUpperCase() + "  "
        }

        text_sign_d_tracking_no_title.setText(R.string.text_tracking_no)

        if (songJangListSize > 1) {  //다수건

            val qtyFormat = String.format(resources.getString(R.string.text_total_qty_count), songJangListSize)
            text_sign_d_tracking_no.text = qtyFormat
            text_sign_d_tracking_no_more.visibility = View.VISIBLE
            text_sign_d_tracking_no_more.text = barcodeMsg
            layout_sign_d_sender.visibility = View.GONE
        } else {  //1건

            text_sign_d_tracking_no.text = barcodeMsg.trim { it <= ' ' }
            text_sign_d_tracking_no_more.visibility = View.GONE
        }


        getDeliveryInfo(songjanglist!![0].barcode)
        outletInfo = getOutletInfo(songjanglist!![0].barcode)

        text_top_title.setText(R.string.text_delivered)
        text_sign_d_receiver.text = receiverName
        text_sign_d_sender.text = senderName
        DisplayUtil.setPreviewCamera(img_sign_d_preview_bg)


        Log.e("krm0219", TAG + "  Outlet info Route : " + OutletInfo.route.substring(0, 2) + " / " + OutletInfo.route)
        // NOTIFICATION.  Outlet Delivery
        if (OutletInfo.route.substring(0, 2).contains("7E") || OutletInfo.route.substring(0, 2).contains("FL")) {

            layout_sign_d_outlet_address.visibility = View.VISIBLE
            text_sign_d_outlet_address.text = "(" + OutletInfo.zip_code + ") " + OutletInfo.address

            // 2019.04
            var outletAddress = OutletInfo.address.toUpperCase()
            var operationHour: String? = null
            Log.e("krm0219", "Operation Address : " + OutletInfo.address)

            if (outletAddress.contains(context!!.resources.getString(R.string.text_operation_hours).toUpperCase())) {

                val indexString = "(" + context!!.resources.getString(R.string.text_operation_hours).toUpperCase() + ":"
                val operationHourIndex = outletAddress.indexOf(indexString)

                operationHour = OutletInfo.address.substring(operationHourIndex + indexString.length, outletAddress.length - 1)
                outletAddress = OutletInfo.address.substring(0, operationHourIndex)
                Log.e("krm0219", "Operation Hour : $operationHour")
            } else if (outletAddress.contains(context!!.resources.getString(R.string.text_operation_hour).toUpperCase())) {

                val indexString = "(" + context!!.resources.getString(R.string.text_operation_hour).toUpperCase() + ":"
                val operationHourIndex = outletAddress.indexOf(indexString)

                operationHour = OutletInfo.address.substring(operationHourIndex + indexString.length, outletAddress.length - 1)
                outletAddress = OutletInfo.address.substring(0, operationHourIndex)
                Log.e("krm0219", "Operation Hour : $operationHour")
            }

            if (operationHour != null) {

                layout_sign_d_outlet_operation_hour.visibility = View.VISIBLE
                text_sign_d_outlet_operation_time.text = operationHour
            }

            text_sign_d_outlet_address.text = "(" + OutletInfo.zip_code + ") " + outletAddress


            text_sign_d_tracking_no_more.visibility = View.GONE
            layout_sign_d_receiver.visibility = View.GONE
            list_sign_d_outlet_list.visibility = View.VISIBLE


            outletDeliveryDoneListItemArrayList = ArrayList()

            val dbHelper = DatabaseHelper.getInstance()

            if (routeNumber == null) {      // SCAN > Delivery Done
                for (i in songjanglist!!.indices) {

                    val cs = dbHelper["SELECT rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and invoice_no='" + songjanglist!![i].barcode + "'"]

                    if (cs.moveToFirst()) {
                        do {

                            val receiverName = cs.getString(cs.getColumnIndex("rcv_nm"))

                            val outletDeliveryDoneListItem = OutletDeliveryDoneListItem()
                            outletDeliveryDoneListItem.setTrackingNo(songjanglist!![i].barcode)
                            outletDeliveryDoneListItem.setReceiverName(receiverName)
                            outletDeliveryDoneListItemArrayList!!.add(outletDeliveryDoneListItem)
                        } while (cs.moveToNext())
                    }
                }
            } else {    // LIST > In Progress

                songjanglist = ArrayList()
                var barcodeData: BarcodeData

                val cs = dbHelper["SELECT invoice_no, rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and route LIKE '%" + routeNumber + "%'"]

                if (cs.moveToFirst()) {
                    do {

                        val invoiceNo = cs.getString(cs.getColumnIndex("invoice_no"))
                        val receiverName = cs.getString(cs.getColumnIndex("rcv_nm"))

                        barcodeData = BarcodeData()
                        barcodeData.barcode = invoiceNo
                        barcodeData.state = mType
                        songjanglist!!.add(barcodeData)

                        val outletDeliveryDoneListItem = OutletDeliveryDoneListItem()
                        outletDeliveryDoneListItem.setTrackingNo(invoiceNo)
                        outletDeliveryDoneListItem.setReceiverName(receiverName)
                        outletDeliveryDoneListItemArrayList!!.add(outletDeliveryDoneListItem)
                    } while (cs.moveToNext())
                }

                if (outletDeliveryDoneListItemArrayList!!.size > 1) {

                    val qtyFormat = String.format(context!!.resources.getString(R.string.text_total_qty_count), outletDeliveryDoneListItemArrayList!!.size)
                    text_sign_d_tracking_no_title.setText(R.string.text_parcel_qty1)
                    text_sign_d_tracking_no.text = qtyFormat
                    layout_sign_d_sender.visibility = View.GONE
                }
            }


            if (OutletInfo.route.substring(0, 2).contains("7E")) {

                text_top_title.setText(R.string.text_title_7e_store_delivery)
                text_sign_d_outlet_address_title.setText(R.string.text_7e_store_address)
                layout_sign_d_sign_memo.visibility = View.VISIBLE
                layout_sign_d_visit_log.visibility = View.VISIBLE

                if (!NetworkUtil.isNetworkAvailable(context)) {
                    AlertShow(context!!.resources.getString(R.string.text_warning), context!!.resources.getString(R.string.msg_network_connect_error), context!!.resources.getString(R.string.button_close))
                    return
                } else {

                    val qrCodeAsyncTask = QRCodeAsyncTask(getString(R.string.text_outlet_7e), outletDeliveryDoneListItemArrayList!!)
                    qrCodeAsyncTask.execute()
                }
            } else {

                text_top_title.setText(R.string.text_title_fl_delivery)
                text_sign_d_outlet_address_title.setText(R.string.text_federated_locker_address)
                layout_sign_d_sign_memo.visibility = View.GONE
                layout_sign_d_visit_log.visibility = View.GONE

                outletTrackingNoAdapter = OutletTrackingNoAdapter(this@DeliveryDoneActivity1, outletDeliveryDoneListItemArrayList, "FL")
                list_sign_d_outlet_list.adapter = outletTrackingNoAdapter
                setListViewHeightBasedOnChildren(list_sign_d_outlet_list)
            }
        } else {

            layout_sign_d_outlet_address.visibility = View.GONE
            layout_sign_d_outlet_operation_hour.visibility = View.GONE
            layout_sign_d_receiver.visibility = View.VISIBLE
            list_sign_d_outlet_list.visibility = View.GONE
            layout_sign_d_sign_memo.visibility = View.VISIBLE
            layout_sign_d_visit_log.visibility = View.VISIBLE
        }


        // Memo 입력제한
        edit_sign_d_memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (99 <= edit_sign_d_memo.length()) {
                    Toast.makeText(context, context!!.resources.getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
                }
            }
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


        // Button Click
        layout_top_back.setOnClickListener(clickListener)
        img_sign_d_receiver_self.setOnClickListener(clickListener)
        text_sign_d_receiver_self.setOnClickListener(clickListener)
        img_sign_d_receiver_substitute.setOnClickListener(clickListener)
        text_sign_d_receiver_substitute.setOnClickListener(clickListener)
        img_sign_d_receiver_other.setOnClickListener(clickListener)
        text_sign_d_receiver_other.setOnClickListener(clickListener)
        layout_sign_d_sign_eraser.setOnClickListener(clickListener)
        layout_sign_d_take_photo.setOnClickListener(clickListener)
        layout_sign_d_gallery.setOnClickListener(clickListener)
        btn_sign_d_save.setOnClickListener(clickListener)
    }


    override fun onResume() {
        super.onResume()
        if (isPermissionTrue) {
            // Camera
            camera2 = Camera2APIs(this)
            if (texture_sign_d_preview!!.isAvailable) {

                openCamera()
            } else {

                texture_sign_d_preview!!.surfaceTextureListener = this
            }

            // Location
            gpsTrackerManager = GPSTrackerManager(context)
            gpsEnable = gpsTrackerManager!!.enableGPSSetting()

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager!!.GPSTrackerStart()
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
                Log.e("Location", "$TAG GPSTrackerManager onResume : $latitude  $longitude  ")
            } else {

                DataUtil.enableLocationSettings(this@DeliveryDoneActivity1, context)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isGalleryActivate = false

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            try {

                val selectedImageUri = data.data


                val selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                val resizeBitmap = camera2!!.getResizeBitmap(selectedImage)
                img_sign_d_visit_log!!.setImageBitmap(resizeBitmap)
                img_sign_d_visit_log!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                onResume()
            } catch (e: Exception) {
                Log.e("eylee", e.toString())
                e.printStackTrace()
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
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

    fun cancelSigning() {
        AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }.show()
    }


    fun confirmSigning() {
        if (OutletInfo.route.contains("7E")) {
            if (showQRCode) {        // QR Code Show
                saveOutletDeliveryDone()
            } else {                // QR Code Not Show... > 진행 불가능
                Toast.makeText(this@DeliveryDoneActivity1, context!!.resources.getString(R.string.msg_outlet_qrcode_require), Toast.LENGTH_SHORT).show()
            }
        } else if (OutletInfo.route.contains("FL")) {
            saveOutletDeliveryDone()
        } else {
            saveServerUploadSign()
        }
    }


    fun getDeliveryInfo(barcodeNo: String) {
        val cursor = DatabaseHelper.getInstance()["SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE"]
        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"))
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"))
        }
        cursor.close()
    }

    fun getOutletInfo(barcodeNo: String): OutletInfo {
        val cursor = DatabaseHelper.getInstance()["SELECT route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE"]
        val outletInfo = OutletInfo()
        if (cursor.moveToFirst()) {
            OutletInfo.route = cursor.getString(cursor.getColumnIndexOrThrow("route"))
            OutletInfo.zip_code = cursor.getString(cursor.getColumnIndexOrThrow("zip_code"))
            OutletInfo.address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
        }
        cursor.close()
        return outletInfo
    }


    private fun AlertShow(title: String, msg: String, btnText: String) {

        val alert_internet_status = AlertDialog.Builder(this)
        alert_internet_status.setTitle(title)
        alert_internet_status.setMessage(msg)
        alert_internet_status.setPositiveButton(btnText) { dialog: DialogInterface, _: Int ->
            if (title.contains("Result")) {
            } else {
                dialog.dismiss() // 닫기
                finish()
            }
        }
        alert_internet_status.show()
    }


    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-01-22
     */
    fun saveServerUploadSign() {
        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {
                AlertShow(context!!.resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (gpsTrackerManager != null) {
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
                Log.e("Location", "$TAG saveServerUploadSign  GPSTrackerManager : $latitude  $longitude  ")
            }

            val driverMemo = edit_sign_d_memo!!.text.toString()

            // NOTIFICATION. 2020.06  visit log 추가
            // 사인 or 사진 둘 중 하나는 있어야 함
            val hasSignImage = sign_view_sign_d_signature!!.isTouche
            val hasVisitImage = camera2!!.hasImage(img_sign_d_visit_log)
            Log.e("krm0219", "$TAG  has DATA : $hasSignImage / $hasVisitImage")

            if (!hasSignImage && !hasVisitImage) {

                val msg = "${context!!.resources.getString(R.string.msg_signature_require)} or${context!!.resources.getString(R.string.msg_visit_photo_require)}"
                Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
                return
            }

            //서버에 올리기전 용량체크  내장메모리가 100Kbyte 안남은경우
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context!!.resources.getString(R.string.text_warning), context!!.resources.getString(R.string.msg_disk_size_error), context!!.resources.getString(R.string.button_close))
                return
            }

//            DataUtil.captureSign("/Qdrive", songjanglist.get(0).getBarcode(), sign_view_sign_d_signature);
//            DataUtil.captureSign("/Qdrive", songjanglist.get(0).getBarcode() + "_1", img_sign_d_visit_log);
            DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadDeliveryData)

//            sign_view_sign_d_signature.buildDrawingCache()
//            val signBitmap: Bitmap = sign_view_sign_d_signature.drawingCache
//            val bitmapString = DataUtil.bitmapToString(signBitmap)

            DeliveryDoneUploadHelper.Builder(this, opID, officeCode, deviceID,
                    songjanglist, mReceiveType, driverMemo,
                    sign_view_sign_d_signature, hasSignImage, img_sign_d_visit_log, hasVisitImage,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerUploadEventListener {
                        DataUtil.inProgressListPosition = 0
                        setResult(Activity.RESULT_OK)
                        finish()
                    }.build().execute()
        } catch (e: Exception) {

            Log.e("krm0219", "$TAG  Exception : $e")
            Toast.makeText(this.applicationContext, context!!.resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    private fun AlertShow(msg: String) {
        val alert_internet_status = AlertDialog.Builder(this)
        alert_internet_status.setTitle(context!!.resources.getString(R.string.text_warning))
        alert_internet_status.setMessage(msg)
        alert_internet_status.setPositiveButton(context!!.resources.getString(R.string.button_close)) { dialog, _ ->
            dialog.dismiss() // 닫기
            finish()
        }
        alert_internet_status.show()
    }

    fun saveOutletDeliveryDone() {
        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {
                AlertShow(context!!.resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (gpsTrackerManager != null) {
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
                Log.e("Location", "$TAG saveOutletDeliveryDone  GPSTrackerManager : $latitude  $longitude  ")
            }

            if (OutletInfo.route.substring(0, 2).contains("7E")) {
                if (!sign_view_sign_d_signature!!.isTouche) {
                    Toast.makeText(this.applicationContext, context!!.resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                    return
                }
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context!!.resources.getString(R.string.text_warning), context!!.resources.getString(R.string.msg_disk_size_error), context!!.resources.getString(R.string.button_close))
                return
            }

            val driverMemo = edit_sign_d_memo!!.text.toString()

            DataUtil.logEvent("button_click", TAG + "_OUTLET", "SetOutletDeliveryUploadData")
            // 2019.02 - stat : D3 로..   서버에서 outlet stat 변경
            OutletDeliveryDoneHelper.Builder(this, opID, officeCode, deviceID,
                    songjanglist, OutletInfo.route.substring(0, 2), mReceiveType, sign_view_sign_d_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnOutletDataUploadEventListener {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }.build().execute()
        } catch (e: Exception) {

            Log.e("krm0219", "Exception ; $e")
            Toast.makeText(this.applicationContext, context!!.resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    var clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.layout_top_back -> {
                cancelSigning()
            }
            R.id.img_sign_d_receiver_self, R.id.text_sign_d_receiver_self -> {
                img_sign_d_receiver_self!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
                img_sign_d_receiver_substitute!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
                img_sign_d_receiver_other!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
                mReceiveType = "RC"
            }
            R.id.img_sign_d_receiver_substitute, R.id.text_sign_d_receiver_substitute -> {
                img_sign_d_receiver_self!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
                img_sign_d_receiver_substitute!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
                img_sign_d_receiver_other!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
                mReceiveType = "AG"
            }
            R.id.img_sign_d_receiver_other, R.id.text_sign_d_receiver_other -> {
                img_sign_d_receiver_self!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
                img_sign_d_receiver_substitute!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
                img_sign_d_receiver_other!!.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
                mReceiveType = "ET"
            }
            R.id.layout_sign_d_sign_eraser -> {
                sign_view_sign_d_signature!!.clearText()
            }
            R.id.layout_sign_d_take_photo -> {
                if (cameraId != null) {
                    camera2!!.takePhoto(texture_sign_d_preview, img_sign_d_visit_log)
                } else {
                    Toast.makeText(this@DeliveryDoneActivity1, context!!.resources.getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.layout_sign_d_gallery -> {
                imageFromAlbum
            }
            R.id.btn_sign_d_save -> {
                confirmSigning()
            }
        }
    }


    // Gallery
    private val imageFromAlbum: Unit
        get() {
            try {
                if (!isGalleryActivate) {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    isGalleryActivate = true
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE)
                }
            } catch (ex: Exception) {
                isGalleryActivate = false
                Log.i("eylee", ex.toString())
            }
        }

    // CAMERA
    private fun openCamera() {
        val cameraManager = camera2!!.getCameraManager(this)
        cameraId = camera2!!.getCameraCharacteristics(cameraManager)
        Log.e("krm0219", "$TAG  openCamera $cameraId")
        if (cameraId != null) {
            camera2!!.setCameraDevice(cameraManager, cameraId)
        } else {
            Toast.makeText(this@DeliveryDoneActivity1, context!!.resources.getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeCamera() {
        camera2!!.closeCamera()
    }

    override fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Size, rotation: Int) {
        texture_sign_d_preview!!.rotation = rotation.toFloat()
        val texture = texture_sign_d_preview!!.surfaceTexture
        texture!!.setDefaultBufferSize(cameraSize.width, cameraSize.height)
        val surface = Surface(texture)
        camera2!!.setCaptureSessionRequest(cameraDevice, surface)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}


    inner class QRCodeAsyncTask(var outlet_type: String, var outletDeliveryDoneListItemArrayList: ArrayList<OutletDeliveryDoneListItem>) : AsyncTask<Void?, Void?, String?>() {

        var imgUrl: String? = null
        var progressDialog = ProgressDialog(this@DeliveryDoneActivity1)

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setMessage(context!!.resources.getString(R.string.text_please_wait))
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): String? {
            return try {
                for (i in outletDeliveryDoneListItemArrayList.indices) {
                    val result = getQRCodeData(outletDeliveryDoneListItemArrayList[i].getTrackingNo())
                    if (result != null) {

                        /* //    TODO 7E TEST
                            //   ServerDownloadHelper.java 에서 테스트 데이터 넣고  테스트 가능
                            //   테스트 데이터를 넣은 만큼 데이터 셋팅
                            if (i == 0) {
                                result.setQrcode_data("{\"Q\":\"D\",\"J\":\"CR20181022001\",\"V\":\"QT\",\"S\":\"\",\"C\":1}");
                            } else if (i == 1) {
                                result.setQrcode_data("{\"Q\":\"D\",\"J\":\"CR20181107001\",\"V\":\"QT\",\"S\":\"\",\"C\":1}");
                            } else if (i == 2) {
                                result.setQrcode_data("{\"Q\":\"D\",\"J\":\"CR20181022001\",\"V\":\"QT\",\"S\":\"\",\"C\":1}");
                            }*/
                        val jsonObject = JSONObject(result.getQrcode_data())
                        val type = jsonObject.getString("Q")
                        if (type != "D") {
                            return null
                        }
                        jobID = jsonObject.getString("J")
                        if (jobID == null || jobID.equals("", ignoreCase = true)) {
                            return null
                        } else {
                            outletDeliveryDoneListItemArrayList[i].setJobID(jobID)
                        }
                        vendorCode = jsonObject.getString("V")
                        outletDeliveryDoneListItemArrayList[i].vendorCode = vendorCode
                        imgUrl = DataUtil.qrcode_url + result.getQrcode_data()
                        // test Data. https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code={"Q":"D","J":"CR20190313001","V":"QT","S":"472","C":1}
                        Log.e("krm0219", "QR Code URL > $imgUrl")
                        outletDeliveryDoneListItemArrayList[i].qrCode = imgUrl
                    }
                }
                "SUCCESS"
            } catch (e: Exception) {
                Log.e("krm0219", "QRCodeAsyncTask Exception : $e")
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            DisplayUtil.dismissProgressDialog(progressDialog)
            if (result != null) {
                showQRCode = true
                outletTrackingNoAdapter = OutletTrackingNoAdapter(this@DeliveryDoneActivity1, outletDeliveryDoneListItemArrayList, "7E")
                list_sign_d_outlet_list!!.adapter = outletTrackingNoAdapter
                setListViewHeightBasedOnChildren(list_sign_d_outlet_list)
            } else {
                showQRCode = false
                Toast.makeText(context, context!!.resources.getString(R.string.msg_outlet_qrcode_data_error), Toast.LENGTH_LONG).show()
            }
        }

        private fun getQRCodeData(tracking_no: String): QRCodeResult? {
            Log.e("krm0219", "$TAG  getQRCodeData  $outlet_type / $tracking_no")
            val resultObj: QRCodeResult?
            val gson = Gson()

            resultObj = try {
                val job = JSONObject()
                job.accumulate("qstation_type", outlet_type)
                job.accumulate("tracking_id", tracking_no)
                job.accumulate("app_id", DataUtil.appID)
                job.accumulate("nation_cd", DataUtil.nationCode)
                val methodName = "QRCodeForQStationDelivery"
                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
                gson.fromJson(jsonString, QRCodeResult::class.java)
            } catch (e: Exception) {
                Log.e("Exception", "$TAG  QRCodeForQStationDelivery Json Exception : $e")
                null
            }
            return resultObj
        }
    }


    companion object {
        private const val RESULT_LOAD_IMAGE = 3
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
                PermissionChecker.ACCESS_COARSE_LOCATION, PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.CAMERA)

        fun setListViewHeightBasedOnChildren(listView: ListView?) {
            val listAdapter = listView!!.adapter ?: return
            var totalHeight = 0
            val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)
            for (i in 0 until listAdapter.count) {
                val listItem = listAdapter.getView(i, null, listView)
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                totalHeight += listItem.measuredHeight
            }
            val params = listView.layoutParams
            params.height = totalHeight
            listView.layoutParams = params
            listView.requestLayout()
        }
    }
}