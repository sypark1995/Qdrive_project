//package com.giosis.library.list.delivery
//
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.app.ProgressDialog
//import android.content.DialogInterface
//import android.content.Intent
//import android.graphics.SurfaceTexture
//import android.hardware.camera2.CameraDevice
//import android.os.AsyncTask
//import android.os.Bundle
//import android.provider.MediaStore
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.util.Size
//import android.view.Surface
//import android.view.TextureView
//import android.view.TextureView.SurfaceTextureListener
//import android.view.View
//import android.widget.*
//import com.giosis.util.qdrive.singapore.MemoryStatus
//import com.giosis.util.qdrive.singapore.MemoryStatus.availableInternalMemorySize
//import com.giosis.util.qdrive.singapore.R
//import com.giosis.util.qdrive.singapore.DatabaseHelper
//import com.giosis.util.qdrive.singapore.DatabaseHelper.Companion.getInstance
//import com.giosis.util.qdrive.singapore.GPSTrackerManager
//import com.giosis.util.qdrive.singapore.LocationModel
//import com.giosis.util.qdrive.singapore.BarcodeData
//import com.giosis.util.qdrive.singapore.OutletInfo
//import com.giosis.util.qdrive.singapore.RowItem
//import com.giosis.util.qdrive.singapore.SigningView
//import com.giosis.library.server.Custom_JsonParser
//import com.giosis.library.util.*
//import com.giosis.library.util.Camera2APIs.Camera2Interface
//import com.giosis.util.qdrive.singapore.Preferences.deviceUUID
//import com.giosis.util.qdrive.singapore.Preferences.userId
//import com.giosis.util.qdrive.singapore.Preferences.userNation
//import com.google.gson.Gson
//import org.json.JSONObject
//import java.util.*
//import kotlin.collections.ArrayList
//
///***************
// * LIST, In Progress > 'Delivered'  // SCAN > Delivery Done
// * 2020.06 사진 추가
// */
//class DeliveryDoneActivity2 : CommonActivity(), Camera2Interface,
//    SurfaceTextureListener {
//    var TAG = "DeliveryDoneActivity"
//
//
//    //
//
//
//    lateinit var edit_sign_d_memo: EditText
//    lateinit var list_sign_d_outlet_list: ListView
//    var sign_view_sign_d_signature: SigningView? = null
//
//
//    var texture_sign_d_preview: TextureView? = null
//    var img_sign_d_preview_bg: ImageView? = null
//    var img_sign_d_visit_log: ImageView? = null
//
//
//    // Outlet
//
//
//    //
//    var opID = ""
//    var officeCode = ""
//    var deviceID = ""
//    var mStrWaybillNo = ""
//    var mReceiveType = "RC"
//    var mType = BarcodeType.TYPE_DELIVERY
//    var routeNumber: String? = null
//    var barcodeList: ArrayList<BarcodeData>? = null
//    var senderName: String? = null
//    var receiverName: String? = null
//    var highAmountYn: String? = "N"
//
//    // Camera & Gallery
//    var camera2 = Camera2APIs(this)
//    var cameraId: String? = null
//    var isClickedPhoto = false
//    var isGalleryActivate = false
//
//    // GPS
//    var gpsTrackerManager: GPSTrackerManager? = null
//    var gpsEnable = false
//    var latitude = 0.0
//    var longitude = 0.0
//    var locationModel = LocationModel()
//
//    // Outlet
//    var outletInfo: OutletInfo? = null
//    var jobID: String? = null
//    var vendorCode: String? = null
//    var showQRCode = false
//    var outletDeliveryDoneListItemArrayList: ArrayList<OutletDeliveryDoneListItem>? = null
//    var outletTrackingNoAdapter: OutletTrackingNoAdapter? = null
//    var isPermissionTrue = false
//
//    @SuppressLint("SetTextI18n")
//    public override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_delivered)
//        val layout_top_back: FrameLayout = findViewById(R.id.layout_top_back)
//        val text_top_title: TextView = findViewById(R.id.text_top_title)
//        val text_sign_d_tracking_no_title: TextView =
//            findViewById(R.id.text_sign_d_tracking_no_title)
//
//        val text_sign_d_tracking_no: TextView = findViewById(R.id.text_sign_d_tracking_no)
//        val text_sign_d_tracking_no_more: TextView = findViewById(R.id.text_sign_d_tracking_no_more)
//        val layout_sign_d_receiver: LinearLayout = findViewById(R.id.layout_sign_d_receiver)
//        val text_sign_d_receiver: TextView = findViewById(R.id.text_sign_d_receiver)
//        val img_sign_d_receiver_self: ImageView = findViewById(R.id.img_sign_d_receiver_self)
//        val text_sign_d_receiver_self: TextView = findViewById(R.id.text_sign_d_receiver_self)
//        val img_sign_d_receiver_substitute: ImageView =
//            findViewById(R.id.img_sign_d_receiver_substitute)
//        val text_sign_d_receiver_substitute: TextView =
//            findViewById(R.id.text_sign_d_receiver_substitute)
//        val img_sign_d_receiver_other: ImageView = findViewById(R.id.img_sign_d_receiver_other)
//        val text_sign_d_receiver_other: TextView = findViewById(R.id.text_sign_d_receiver_other)
//        val layout_sign_d_sender: LinearLayout = findViewById(R.id.layout_sign_d_sender)
//        val text_sign_d_sender: TextView = findViewById(R.id.text_sign_d_sender)
//        val layout_sign_d_sign_memo: LinearLayout = findViewById(R.id.layout_sign_d_sign_memo)
//        edit_sign_d_memo = findViewById(R.id.edit_sign_d_memo)
//        val layout_sign_d_sign_eraser: LinearLayout = findViewById(R.id.layout_sign_d_sign_eraser)
//        sign_view_sign_d_signature = findViewById(R.id.sign_view_sign_d_signature)
//        val layout_sign_d_visit_log: LinearLayout = findViewById(R.id.layout_sign_d_visit_log)
//        val layout_sign_d_take_photo: LinearLayout = findViewById(R.id.layout_sign_d_take_photo)
//        val layout_sign_d_gallery: LinearLayout = findViewById(R.id.layout_sign_d_gallery)
//        texture_sign_d_preview = findViewById(R.id.texture_sign_d_preview)
//        img_sign_d_preview_bg = findViewById(R.id.img_sign_d_preview_bg)
//        img_sign_d_visit_log = findViewById(R.id.img_sign_d_visit_log)
//        val btn_sign_d_save: Button = findViewById(R.id.btn_sign_d_save)
//
//        // Outlet
//        val layout_sign_d_outlet_address: LinearLayout =
//            findViewById(R.id.layout_sign_d_outlet_address)
//        val text_sign_d_outlet_address_title: TextView =
//            findViewById(R.id.text_sign_d_outlet_address_title)
//        val text_sign_d_outlet_address: TextView = findViewById(R.id.text_sign_d_outlet_address)
//        val layout_sign_d_outlet_operation_hour: LinearLayout =
//            findViewById(R.id.layout_sign_d_outlet_operation_hour)
//        val text_sign_d_outlet_operation_time: TextView =
//            findViewById(R.id.text_sign_d_outlet_operation_time)
//        list_sign_d_outlet_list = findViewById(R.id.list_sign_d_outlet_list)
//
//
//        layout_top_back.setOnClickListener {
//            cancelSigning()
//        }
//
//        img_sign_d_receiver_self.setOnClickListener {
//            img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
//            img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            mReceiveType = "RC"
//        }
//        text_sign_d_receiver_self.setOnClickListener {
//            img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
//            img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            mReceiveType = "RC"
//        }
//
//        img_sign_d_receiver_substitute.setOnClickListener {
//            img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
//            img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            mReceiveType = "AG"
//        }
//
//        text_sign_d_receiver_substitute.setOnClickListener {
//            img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
//            img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            mReceiveType = "AG"
//        }
//
//        img_sign_d_receiver_other.setOnClickListener {
//            img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
//            mReceiveType = "ET"
//        }
//
//        text_sign_d_receiver_other.setOnClickListener {
//            img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
//            img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
//            mReceiveType = "ET"
//        }
//
//        layout_sign_d_sign_eraser.setOnClickListener {
//            sign_view_sign_d_signature!!.clearText()
//        }
//
//        layout_sign_d_take_photo.setOnClickListener {
//            if (cameraId != null) {
//                if (!isClickedPhoto) {  // Camera CaptureSession 완료되면 다시 클릭할 수 있도록 수정
//                    isClickedPhoto = true
//                    camera2.takePhoto(texture_sign_d_preview, img_sign_d_visit_log)
//                }
//            } else {
//                Toast.makeText(
//                    this@DeliveryDoneActivity2,
//                    resources.getString(R.string.msg_back_camera_required),
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//        layout_sign_d_gallery.setOnClickListener {
//            getImageFromAlbum()
//        }
//
//        btn_sign_d_save.setOnClickListener {
//            confirmSigning()
//        }
//
//        //
//        camera2 = Camera2APIs(this)
//        opID = userId
//        officeCode = Preferences.officeCode
//        deviceID = deviceUUID
//        barcodeList = ArrayList()
//
//        // in List (단건)
//        try {
//            val parcel = intent.getSerializableExtra("parcel") as RowItem?
//            val songData = BarcodeData()
//            songData.barcode = parcel!!.shipping.uppercase(Locale.getDefault())
//            songData.state = BarcodeType.TYPE_DELIVERY
//            barcodeList!!.add(songData)
//            highAmountYn = parcel.high_amount_yn
//            mStrWaybillNo = parcel.shipping
//            if (userNation != "SG") {
//                locationModel.setParcelLocation(
//                    parcel.lat, parcel.lng,
//                    parcel.zip_code!!, parcel.state, parcel.city, parcel.street
//                )
//                Log.e(
//                    "GPSUpdate",
//                    "Parcel " + parcel.shipping + " // " + parcel.lat + ", " + parcel.lng + " // "
//                            + parcel.zip_code + " - " + parcel.state + " - " + parcel.city + " - " + parcel.street
//                )
//            }
//        } catch (e: Exception) {
//            Log.e("Exception", "Exception $e")
//        }
//        routeNumber = try {
//            val routeType = intent.getStringExtra("route")
//            val routeSplit = routeType!!.split(" ").toTypedArray()
//            routeSplit[0] + " " + routeSplit[1]
//        } catch (e: Exception) {
//            null
//        }
//
//
//        // in Capture (bulk)
//        try {
//            val list = intent.getSerializableExtra("data") as ArrayList<BarcodeData>?
//            for (i in list!!.indices) {
//                val trackingNo = list[i].barcode!!.uppercase(Locale.getDefault())
//                val songData = BarcodeData()
//                songData.barcode = trackingNo
//                songData.state = BarcodeType.TYPE_DELIVERY
//                barcodeList!!.add(songData)
//
//                // 위, 경도 & high amount
//                val cs =
//                    getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + trackingNo + "'"]
//                if (cs.moveToFirst()) {
//                    try {
//                        val value = cs.getString(cs.getColumnIndex("high_amount_yn"))
//                        if (value.equals("Y", ignoreCase = true)) {
//                            highAmountYn = value
//                        }
//                    } catch (ignore: Exception) {
//                    }
//                    if (userNation != "SG") {
//                        if (barcodeList!!.size == 1) {
//                            val parcelLat = cs.getDouble(cs.getColumnIndex("lat"))
//                            val parcelLng = cs.getDouble(cs.getColumnIndex("lng"))
//                            val zipCode = cs.getString(cs.getColumnIndex("zip_code"))
//                            val state = cs.getString(cs.getColumnIndex("state"))
//                            val city = cs.getString(cs.getColumnIndex("city"))
//                            val street = cs.getString(cs.getColumnIndex("street"))
//                            Log.e(
//                                "GPSUpdate",
//                                "Parcel " + trackingNo + " // " + parcelLat + ", " + parcelLng + " // "
//                                        + zipCode + " - " + state + " - " + city + " - " + street
//                            )
//                            locationModel.setParcelLocation(
//                                parcelLat,
//                                parcelLng,
//                                zipCode,
//                                state,
//                                city,
//                                street
//                            )
//                        }
//                    }
//                }
//            }
//        } catch (ignored: Exception) {
//        }
//        val barcodeMsg = StringBuilder()
//        val size = barcodeList!!.size
//        for (i in 0 until size) {
//            barcodeMsg.append(barcodeList!![i].barcode).append("  ")
//        }
//        text_sign_d_tracking_no_title.setText(R.string.text_tracking_no)
//        if (1 < size) {  // 다수건
//            val qtyFormat = String.format(resources.getString(R.string.text_total_qty_count), size)
//            text_sign_d_tracking_no.text = qtyFormat
//            text_sign_d_tracking_no_more.visibility = View.VISIBLE
//            text_sign_d_tracking_no_more.text = barcodeMsg.toString()
//            layout_sign_d_sender.visibility = View.GONE
//        } else {  //1건
//            text_sign_d_tracking_no.text = barcodeMsg.toString().trim { it <= ' ' }
//            text_sign_d_tracking_no_more.visibility = View.GONE
//        }
//        getDeliveryInfo(barcodeList!![0].barcode)
//        outletInfo = getOutletInfo(barcodeList!![0].barcode)
//        text_top_title.setText(R.string.text_delivered)
//        text_sign_d_receiver.text = receiverName
//        text_sign_d_sender.text = senderName
//        DisplayUtil.setPreviewCamera(img_sign_d_preview_bg)
//
//        // NOTIFICATION.  Outlet Delivery
//        if (outletInfo!!.route != null) {
//            if (outletInfo!!.route!!.substring(0, 2)
//                    .contains("7E") || outletInfo!!.route!!.substring(0, 2).contains("FL")
//            ) {
//                layout_sign_d_outlet_address.visibility = View.VISIBLE
//                text_sign_d_outlet_address.text =
//                    "(" + outletInfo!!.zip_code + ") " + outletInfo!!.address
//
//                // 2019.04
//                var outletAddress = outletInfo!!.address!!.uppercase(Locale.getDefault())
//                var operationHour: String? = null
//                Log.e(TAG, "Operation Address : " + outletInfo!!.address)
//                if (outletAddress.contains(
//                        resources.getString(R.string.text_operation_hours)
//                            .uppercase(Locale.getDefault())
//                    )
//                ) {
//                    val indexString =
//                        "(" + resources.getString(R.string.text_operation_hours)
//                            .uppercase(Locale.getDefault()) + ":"
//                    val operationHourIndex = outletAddress.indexOf(indexString)
//                    operationHour = outletInfo!!.address!!.substring(
//                        operationHourIndex + indexString.length,
//                        outletAddress.length - 1
//                    )
//                    outletAddress = outletInfo!!.address!!.substring(0, operationHourIndex)
//                    Log.e(TAG, "Operation Hour : $operationHour")
//                } else if (outletAddress.contains(
//                        resources.getString(R.string.text_operation_hour)
//                            .uppercase(Locale.getDefault())
//                    )
//                ) {
//                    val indexString =
//                        "(" + resources.getString(R.string.text_operation_hour)
//                            .uppercase(Locale.getDefault()) + ":"
//                    val operationHourIndex = outletAddress.indexOf(indexString)
//                    operationHour = outletInfo!!.address!!.substring(
//                        operationHourIndex + indexString.length,
//                        outletAddress.length - 1
//                    )
//                    outletAddress = outletInfo!!.address!!.substring(0, operationHourIndex)
//                    Log.e(TAG, "Operation Hour : $operationHour")
//                }
//                if (operationHour != null) {
//                    layout_sign_d_outlet_operation_hour.visibility = View.VISIBLE
//                    text_sign_d_outlet_operation_time.text = operationHour
//                }
//                text_sign_d_outlet_address.text = "(" + outletInfo!!.zip_code + ") " + outletAddress
//                text_sign_d_tracking_no_more.visibility = View.GONE
//                layout_sign_d_receiver.visibility = View.GONE
//                list_sign_d_outlet_list.visibility = View.VISIBLE
//                outletDeliveryDoneListItemArrayList = ArrayList()
//                val dbHelper = getInstance()
//                if (routeNumber == null) {      // SCAN > Delivery Done
//                    for (i in barcodeList!!.indices) {
//                        val cs =
//                            dbHelper["SELECT rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
//                                    + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and invoice_no='" + barcodeList!![i].barcode + "'"]
//                        if (cs.moveToFirst()) {
//                            do {
//                                val receiver_name = cs.getString(cs.getColumnIndex("rcv_nm"))
//                                val outletDeliveryDoneListItem = OutletDeliveryDoneListItem()
//                                outletDeliveryDoneListItem.trackingNo = barcodeList!![i].barcode
//                                outletDeliveryDoneListItem.receiverName = receiver_name
//                                outletDeliveryDoneListItemArrayList!!.add(outletDeliveryDoneListItem)
//                            } while (cs.moveToNext())
//                        }
//                    }
//                } else {    // LIST > In Progress
//                    barcodeList = ArrayList()
//                    var barcodeData: BarcodeData
//                    val cs =
//                        dbHelper["SELECT invoice_no, rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
//                                + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and route LIKE '%" + routeNumber + "%'"]
//                    if (cs.moveToFirst()) {
//                        do {
//                            val invoice_no = cs.getString(cs.getColumnIndex("invoice_no"))
//                            val receiver_name = cs.getString(cs.getColumnIndex("rcv_nm"))
//                            barcodeData = BarcodeData()
//                            barcodeData.barcode = invoice_no
//                            barcodeData.state = mType
//                            barcodeList!!.add(barcodeData)
//                            val outletDeliveryDoneListItem = OutletDeliveryDoneListItem()
//                            outletDeliveryDoneListItem.trackingNo = invoice_no
//                            outletDeliveryDoneListItem.receiverName = receiver_name
//                            outletDeliveryDoneListItemArrayList!!.add(outletDeliveryDoneListItem)
//                        } while (cs.moveToNext())
//                    }
//                    if (outletDeliveryDoneListItemArrayList!!.size > 1) {
//                        val qtyFormat = String.format(
//                            resources.getString(R.string.text_total_qty_count),
//                            outletDeliveryDoneListItemArrayList!!.size
//                        )
//                        text_sign_d_tracking_no_title.setText(R.string.text_parcel_qty1)
//                        text_sign_d_tracking_no.text = qtyFormat
//                        layout_sign_d_sender.visibility = View.GONE
//                    }
//                }
//                if (outletInfo!!.route!!.substring(0, 2).contains("7E")) {
//                    text_top_title.setText(R.string.text_title_7e_store_delivery)
//                    text_sign_d_outlet_address_title.setText(R.string.text_7e_store_address)
//                    layout_sign_d_sign_memo.visibility = View.VISIBLE
//                    layout_sign_d_visit_log.visibility = View.VISIBLE
//                    if (!NetworkUtil.isNetworkAvailable(this)) {
//                        alertShow(resources.getString(R.string.msg_network_connect_error))
//                        return
//                    } else {
//                        closeCamera()
//                        val qrCodeAsyncTask = QRCodeAsyncTask(
//                            getString(R.string.text_outlet_7e),
//                            outletDeliveryDoneListItemArrayList!!
//                        )
//                        qrCodeAsyncTask.execute()
//                    }
//                } else {
//                    text_top_title.setText(R.string.text_title_fl_delivery)
//                    text_sign_d_outlet_address_title.setText(R.string.text_federated_locker_address)
//                    layout_sign_d_sign_memo.visibility = View.GONE
//                    layout_sign_d_visit_log.visibility = View.GONE
//                    outletTrackingNoAdapter = OutletTrackingNoAdapter(
//                        this@DeliveryDoneActivity2,
//                        outletDeliveryDoneListItemArrayList,
//                        "FL"
//                    )
//                    list_sign_d_outlet_list.adapter = outletTrackingNoAdapter
//                    setListViewHeightBasedOnChildren(list_sign_d_outlet_list)
//                }
//            } else {
//                layout_sign_d_outlet_address.visibility = View.GONE
//                layout_sign_d_outlet_operation_hour.visibility = View.GONE
//                layout_sign_d_receiver.visibility = View.VISIBLE
//                list_sign_d_outlet_list.visibility = View.GONE
//                layout_sign_d_sign_memo.visibility = View.VISIBLE
//                layout_sign_d_visit_log.visibility = View.VISIBLE
//            }
//        } else {
//            layout_sign_d_outlet_address.visibility = View.GONE
//            layout_sign_d_outlet_operation_hour.visibility = View.GONE
//            layout_sign_d_receiver.visibility = View.VISIBLE
//            list_sign_d_outlet_list.visibility = View.GONE
//            layout_sign_d_sign_memo.visibility = View.VISIBLE
//            layout_sign_d_visit_log.visibility = View.VISIBLE
//        }
//
//
//        // Memo 입력제한
//        edit_sign_d_memo.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (99 <= edit_sign_d_memo.length()) {
//                    Toast.makeText(
//                        this@DeliveryDoneActivity2,
//                        resources.getText(R.string.msg_memo_too_long),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//
//            override fun afterTextChanged(editable: Editable) {}
//        })
//
//
//        // 권한 여부 체크 (없으면 true, 있으면 false)
//        val checker = PermissionChecker(this)
//        if (checker.lacksPermissions(*PERMISSIONS)) {
//            isPermissionTrue = false
//            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
//            overridePendingTransition(0, 0)
//        } else {
//            isPermissionTrue = true
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        isGalleryActivate = false
//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
//            try {
//                val selectedImageUri = data.data
//                val selectedImage =
//                    MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
//                val resizeBitmap = camera2.getResizeBitmap(selectedImage)
//                img_sign_d_visit_log!!.setImageBitmap(resizeBitmap)
//                img_sign_d_visit_log!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
//                onResume()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
//            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
//                Log.e("Permission", "$TAG   onActivityResult  PERMISSIONS_GRANTED")
//                isPermissionTrue = true
//            }
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        closeCamera()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        DataUtil.stopGPSManager(gpsTrackerManager)
//    }
//
//    override fun onBackPressed() {
//        cancelSigning()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (isPermissionTrue) {
//            // Camera
//            if (!outletInfo!!.route!!.substring(0, 2).contains("7E")) {
//
//                // When the screen is turned off and turned back on, the SurfaceTexture is already available.
//                if (texture_sign_d_preview!!.isAvailable) {
//                    openCamera("onResume")
//                } else {
//                    texture_sign_d_preview!!.surfaceTextureListener = this
//                }
//            }
//
//            // Location
//            gpsTrackerManager = GPSTrackerManager(this)
//            gpsEnable = gpsTrackerManager!!.enableGPSSetting()
//            if (gpsEnable && gpsTrackerManager != null) {
//                gpsTrackerManager!!.gpsTrackerStart()
//                latitude = gpsTrackerManager!!.latitude
//                longitude = gpsTrackerManager!!.longitude
//            } else {
//                DataUtil.enableLocationSettings(this@DeliveryDoneActivity2)
//            }
//        }
//    }
//
//    fun confirmSigning() {
//        if (outletInfo!!.route!!.contains("7E")) {
//            if (showQRCode) {        // QR Code Show
//                saveOutletDeliveryDone()
//            } else {                // QR Code Not Show... > 진행 불가능
//                Toast.makeText(
//                    this@DeliveryDoneActivity2,
//                    resources.getString(R.string.msg_outlet_qrcode_require),
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        } else if (outletInfo!!.route!!.contains("FL")) {
//            saveOutletDeliveryDone()
//        } else {
//            saveServerUploadSign()
//        }
//    }
//
//    fun getDeliveryInfo(barcodeNo: String?) {
//        val cursor =
//            getInstance()["SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE"]
//        if (cursor.moveToFirst()) {
//            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"))
//            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"))
//        }
//        cursor.close()
//    }
//
//    fun cancelSigning() {
//        AlertDialog.Builder(this)
//            .setMessage(R.string.msg_delivered_sign_cancel)
//            .setPositiveButton(R.string.button_ok) { _, _ ->
//                setResult(RESULT_CANCELED)
//                finish()
//            }
//            .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }.show()
//    }
//
//    fun getOutletInfo(barcodeNo: String?): OutletInfo {
//        val cursor =
//            getInstance()["SELECT route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE"]
//        val outletInfo = OutletInfo()
//        if (cursor.moveToFirst()) {
//            outletInfo.route = cursor.getString(cursor.getColumnIndexOrThrow("route"))
//            outletInfo.zip_code = cursor.getString(cursor.getColumnIndexOrThrow("zip_code"))
//            outletInfo.address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
//        }
//        cursor.close()
//        return outletInfo
//    }
//
//    /*
//     * 실시간 Upload 처리
//     * add by jmkang 2014-01-22
//     */
//    private fun saveServerUploadSign() {
//        try {
//            if (!NetworkUtil.isNetworkAvailable(this)) {
//                alertShow(resources.getString(R.string.msg_network_connect_error))
//                return
//            }
//            if (gpsTrackerManager != null) {
//                latitude = gpsTrackerManager!!.latitude
//                longitude = gpsTrackerManager!!.longitude
//                Log.e(
//                    "Location",
//                    "$TAG saveServerUploadSign  GPSTrackerManager : $latitude  $longitude  "
//                )
//                locationModel.setDriverLocation(latitude, longitude)
//            }
//            val driverMemo = edit_sign_d_memo.text.toString()
//
//            // NOTIFICATION. 2020.06  visit log 추가
//            // 사인 or 사진 둘 중 하나는 있어야 함
//            val hasSignImage = sign_view_sign_d_signature!!.isTouch
//            val hasVisitImage = camera2.hasImage(img_sign_d_visit_log)
//            //   Log.e(TAG, TAG + "  has DATA : " + hasSignImage + " / " + hasVisitImage);
//            if (highAmountYn == "Y") {
//                if (!hasSignImage || !hasVisitImage) {
//                    val msg = resources.getString(R.string.msg_high_amount_sign_photo)
//                    Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
//                    return
//                }
//            } else {
//                if (!hasSignImage && !hasVisitImage) {
//                    val msg = """${resources.getString(R.string.msg_signature_require)} or ${
//                        resources.getString(R.string.msg_visit_photo_require)
//                    }"""
//                    Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
//                    return
//                }
//            }
//
//            //서버에 올리기전 용량체크  내장메모리가 100Kbyte 안남은경우
//            if (availableInternalMemorySize != MemoryStatus.ERROR.toLong() && availableInternalMemorySize < MemoryStatus.PRESENT_BYTE) {
//                alertShow(resources.getString(R.string.msg_disk_size_error))
//                return
//            }
//            DataUtil.logEvent("button_click", TAG, "SetDeliveryUploadData")
//            DeliveryDoneUploadHelper.Builder(
//                this, opID, officeCode, deviceID,
//                barcodeList, mReceiveType, driverMemo,
//                sign_view_sign_d_signature, hasSignImage, img_sign_d_visit_log, hasVisitImage,
//                availableInternalMemorySize, locationModel
//            )
//                .setOnServerUploadEventListener(object : OnServerEventListener {
//                    override fun onPostResult() {
//                        DataUtil.inProgressListPosition = 0
//                        setResult(RESULT_OK)
//                        finish()
//                    }
//
//                    override fun onPostFailList() {}
//                }).build().execute()
//        } catch (e: Exception) {
//            Log.e("Exception", "saveServerUploadSign  Exception : $e")
//            Toast.makeText(
//                this,
//                resources.getString(R.string.text_error) + " - " + e.toString(),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun alertShow(msg: String) {
//        val alertInternetStatus = AlertDialog.Builder(this)
//        alertInternetStatus.setTitle(resources.getString(R.string.text_warning))
//        alertInternetStatus.setMessage(msg)
//        alertInternetStatus.setPositiveButton(
//            resources.getString(R.string.button_close)
//        ) { dialog: DialogInterface, _: Int ->
//            dialog.dismiss()
//            finish()
//        }
//        alertInternetStatus.show()
//    }
//
//    private fun saveOutletDeliveryDone() {
//        try {
//            if (!NetworkUtil.isNetworkAvailable(this)) {
//                alertShow(resources.getString(R.string.msg_network_connect_error))
//                return
//            }
//            if (gpsTrackerManager != null) {
//                latitude = gpsTrackerManager!!.latitude
//                longitude = gpsTrackerManager!!.longitude
//                Log.e(
//                    "Location",
//                    "$TAG saveOutletDeliveryDone  GPSTrackerManager : $latitude  $longitude  "
//                )
//            }
//            if (outletInfo!!.route!!.substring(0, 2).contains("7E")) {
//                if (!sign_view_sign_d_signature!!.isTouch) {
//                    Toast.makeText(
//                        this,
//                        resources.getString(R.string.msg_signature_require),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return
//                }
//            }
//            if (availableInternalMemorySize != MemoryStatus.ERROR.toLong() && availableInternalMemorySize < MemoryStatus.PRESENT_BYTE) {
//                alertShow(resources.getString(R.string.msg_disk_size_error))
//                return
//            }
//            val driverMemo = edit_sign_d_memo.text.toString()
//            DataUtil.logEvent("button_click", TAG + "_OUTLET", "SetOutletDeliveryUploadData")
//            // 2019.02 - stat : D3 로..   서버에서 outlet stat 변경
//            OutletDeliveryDoneHelper.Builder(
//                this,
//                opID,
//                officeCode,
//                deviceID,
//                barcodeList,
//                outletInfo!!.route!!.substring(0, 2),
//                mReceiveType,
//                sign_view_sign_d_signature,
//                driverMemo,
//                availableInternalMemorySize,
//                latitude,
//                longitude
//            )
//                .setOnOutletDataUploadEventListener {
//                    setResult(RESULT_OK)
//                    finish()
//                }.build().execute()
//        } catch (e: Exception) {
//            Log.e("Exception", "saveOutletDeliveryDone   Exception ; $e")
//            Toast.makeText(
//                this,
//                resources.getString(R.string.text_error) + " - " + e.toString(),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    // Gallery
//    private fun getImageFromAlbum() {
//        try {
//            if (!isGalleryActivate) {
//                val intent = Intent()
//                intent.type = "image/*"
//                intent.action = Intent.ACTION_GET_CONTENT
//                isGalleryActivate = true
//                startActivityForResult(
//                    Intent.createChooser(intent, "Select Picture"),
//                    RESULT_LOAD_IMAGE
//                )
//            }
//        } catch (ex: Exception) {
//            isGalleryActivate = false
//            Log.i("eylee", ex.toString())
//        }
//    }
//
//    // CAMERA
//    private fun openCamera(it: String) {
//        val cameraManager = camera2.getCameraManager(this)
//        cameraId = camera2.getCameraCharacteristics(cameraManager)
//        Log.e("Camera", "$TAG  openCamera $cameraId   >>> $it")
//        if (cameraId != null) {
//            camera2.setCameraDevice(cameraManager, cameraId)
//        } else {
//            Toast.makeText(
//                this@DeliveryDoneActivity2,
//                resources.getString(R.string.msg_back_camera_required),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun closeCamera() {
//        camera2.closeCamera()
//    }
//
//    override fun onCameraDeviceOpened(
//        cameraDevice: CameraDevice,
//        cameraSize: Size,
//        rotation: Int,
//        it: String
//    ) {
//        Log.e("Camera", "onCameraDeviceOpened  $it")
//        texture_sign_d_preview!!.rotation = rotation.toFloat()
//        try {
//            val texture = texture_sign_d_preview!!.surfaceTexture
//            texture!!.setDefaultBufferSize(cameraSize.width, cameraSize.height)
//            val surface = Surface(texture)
//            camera2.setCaptureSessionRequest(cameraDevice, surface)
//        } catch (e: Exception) {
//            Log.e("Exception", "onCameraDeviceOpened  Exception : $e")
//        }
//    }
//
//    override fun onCaptureCompleted() {
//        isClickedPhoto = false
//    }
//
//    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
//        openCamera("onSurfaceTextureAvailable")
//    }
//
//    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
//    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//        return true
//    }
//
//    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
//
//    private fun callAPi() {
//
//    }
//    inner class QRCodeAsyncTask(
//        var outlet_type: String,
//        var outletDeliveryDoneListItemArrayList: ArrayList<OutletDeliveryDoneListItem>
//    ) :
//        AsyncTask<Void?, Void?, String?>() {
//        var qrCodeResultArrayList: ArrayList<QRCodeResult>
//        var imgUrl: String? = null
//        var progressDialog = ProgressDialog(this@DeliveryDoneActivity2)
//        override fun onPreExecute() {
//            super.onPreExecute()
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//            progressDialog.setMessage(resources.getString(R.string.text_please_wait))
//            progressDialog.setCancelable(false)
//            progressDialog.show()
//        }
//
//        protected override fun doInBackground(vararg params: Void): String? {
//            return try {
//                for (i in outletDeliveryDoneListItemArrayList.indices) {
//                    val result = getQRCodeData(outletDeliveryDoneListItemArrayList[i].trackingNo)
//                    if (result != null) {
//                        val jsonObject = JSONObject(result.qrcode_data)
//                        val type = jsonObject.getString("Q")
//                        if (type != "D") {
//                            return null
//                        }
//                        jobID = jsonObject.getString("J")
//                        if (jobID == null || jobID.equals("", ignoreCase = true)) {
//                            return null
//                        } else {
//                            outletDeliveryDoneListItemArrayList[i].jobID = jobID
//                        }
//                        vendorCode = jsonObject.getString("V")
//                        outletDeliveryDoneListItemArrayList[i].vendorCode = vendorCode
//                        imgUrl = DataUtil.qrcode_url + result.qrcode_data
//                        // https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code={"Q":"D","J":"CR20190313001","V":"QT","S":"472","C":1}
//                        Log.e(TAG, "QR Code URL > $imgUrl")
//                        outletDeliveryDoneListItemArrayList[i].qrCode = imgUrl
//                    }
//                }
//                "SUCCESS"
//            } catch (e: Exception) {
//                Log.e("Exception", "QRCodeForQStationDelivery Exception : $e")
//                e.printStackTrace()
//                null
//            }
//        }
//
//        override fun onPostExecute(result: String?) {
//            super.onPostExecute(result)
//            DisplayUtil.dismissProgressDialog(progressDialog)
//            if (result != null) {
//                showQRCode = true
//                outletTrackingNoAdapter = OutletTrackingNoAdapter(
//                    this@DeliveryDoneActivity2,
//                    outletDeliveryDoneListItemArrayList, "7E"
//                )
//                list_sign_d_outlet_list.adapter = outletTrackingNoAdapter
//                setListViewHeightBasedOnChildren(list_sign_d_outlet_list)
//                if (texture_sign_d_preview!!.isAvailable) {
//                    openCamera("Outlet")
//                } else {
//                    texture_sign_d_preview!!.surfaceTextureListener = this@DeliveryDoneActivity2
//                }
//            } else {
//                showQRCode = false
//                Toast.makeText(
//                    this@DeliveryDoneActivity2,
//                    resources.getString(R.string.msg_outlet_qrcode_data_error),
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//
//        private fun getQRCodeData(tracking_no: String?): QRCodeResult? {
//            Log.e(TAG, "$TAG  getQRCodeData  $outlet_type / $tracking_no")
//            val resultObj: QRCodeResult? = try {
//                val job = JSONObject()
//                job.accumulate("qstation_type", outlet_type)
//                job.accumulate("tracking_id", tracking_no)
//                job.accumulate("app_id", DataUtil.appID)
//                job.accumulate("nation_cd", userNation)
//                val methodName = "QRCodeForQStationDelivery"
//                val jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
//                Gson().fromJson(jsonString, QRCodeResult::class.java)
//            } catch (e: Exception) {
//                Log.e("Exception", "  QRCodeForQStationDelivery Json Exception : $e")
//                null
//            }
//            return resultObj
//        }
//
//        init {
//            qrCodeResultArrayList = ArrayList()
//        }
//    }
//
//    fun setListViewHeightBasedOnChildren(listView: ListView?) {
//        val listAdapter = listView!!.adapter ?: return
//        var totalHeight = 0
//        val desiredWidth =
//            View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)
//        for (i in 0 until listAdapter.count) {
//            val listItem = listAdapter.getView(i, null, listView)
//            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
//            totalHeight += listItem.measuredHeight
//        }
//        val params = listView.layoutParams
//        params.height = totalHeight
//        listView.layoutParams = params
//        listView.requestLayout()
//    }
//
//    companion object {
//        private const val RESULT_LOAD_IMAGE = 3
//        private const val PERMISSION_REQUEST_CODE = 1000
//        private val PERMISSIONS = arrayOf(
//            PermissionChecker.READ_EXTERNAL_STORAGE,
//            PermissionChecker.WRITE_EXTERNAL_STORAGE,
//            PermissionChecker.ACCESS_COARSE_LOCATION,
//            PermissionChecker.ACCESS_FINE_LOCATION,
//            PermissionChecker.CAMERA
//        )
//    }
//}