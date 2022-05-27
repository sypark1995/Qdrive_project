package com.giosis.util.qdrive.singapore.list.pickup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.databinding.ActivityPickupStartToScanBinding
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.gps.LocationModel
import com.giosis.util.qdrive.singapore.server.ImageUpload
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pickup done -> Bundle type scan all 로 기능개선
 * LIST > In-Progress > 'Start To Scan'
 */
class PickupDoneActivity : CommonActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
        )
    }

    var tag = "PickupDoneActivity"

    private val binding by lazy {
        ActivityPickupStartToScanBinding.inflate(layoutInflater)
    }

    lateinit var pickupNo: String
    private lateinit var mStrWaybillNo: String

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    private var locationModel = LocationModel()

    var isPermissionTrue = false

    val progressBar by lazy {
        ProgressDialog(this@PickupDoneActivity)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseEvent.createEvent(this, tag)

        pickupNo = intent.getStringExtra("pickupNo").toString()
        val applicant = intent.getStringExtra("applicant")
        mStrWaybillNo = intent.getStringExtra("scannedList").toString()
        val strReqQty = intent.getStringExtra("scannedQty")

        binding.layoutTopTitle.textTopTitle.text = resources.getString(R.string.text_start_to_scan)
        binding.textPickupNo.text = pickupNo
        binding.textApplicant.text = applicant
        binding.imgStartScanCheck.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
        binding.imgZeroQtyCheck.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
        binding.textTotalQty.text = strReqQty


        // 위, 경도
        val cs =
            DatabaseHelper.getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + pickupNo + "'"]
        if (cs.moveToFirst()) {

            val parcelLat = cs.getDouble(cs.getColumnIndex("lat"))
            val parcelLng = cs.getDouble(cs.getColumnIndex("lng"))
            val zipCode = cs.getString(cs.getColumnIndex("zip_code"))
            val state = cs.getString(cs.getColumnIndex("state"))
            val city = cs.getString(cs.getColumnIndex("city"))
            val street = cs.getString(cs.getColumnIndex("street"))
            Log.e(
                "GPSUpdate",
                "Parcel $pickupNo // $parcelLat, $parcelLng // $zipCode - $state - $city - $street"
            )

            locationModel.setParcelLocation(parcelLat, parcelLng, zipCode, state, city, street)
        }


        // Memo 입력제한
        binding.editMemo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (99 <= binding.editMemo.length()) {

                    Toast.makeText(
                        this@PickupDoneActivity,
                        resources.getText(R.string.msg_memo_too_long),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })



        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            cancelSigning()
        }

        binding.layoutApplicantEraser.setOnClickListener {

            binding.signApplicantSignature.clearText()
        }

        binding.layoutCollectorEraser.setOnClickListener {

            binding.signCollectorSignature.clearText()
        }

        binding.btnSave.setOnClickListener {
            saveServerUploadSign()
        }


        // permission
        val checker = PermissionChecker(this@PickupDoneActivity)

        if (checker.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this@PickupDoneActivity,
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

            gpsTrackerManager = GPSTrackerManager(this@PickupDoneActivity)
            gpsTrackerManager?.let {
                gpsEnable = it.enableGPSSetting()
            }

            if (gpsEnable && gpsTrackerManager != null) {
                gpsTrackerManager!!.gpsTrackerStart()
            } else {
                DataUtil.enableLocationSettings(this@PickupDoneActivity)
            }
            progressBar.setCancelable(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                isPermissionTrue = true
            }
        }
    }

    override fun onBackPressed() {
        cancelSigning()
    }

    fun cancelSigning() {
        AlertDialog.Builder(this@PickupDoneActivity)
            .setMessage(R.string.msg_delivered_sign_cancel)
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int -> finish() }
            .setNegativeButton(R.string.button_cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveServerUploadSign() {
        try {
            if (!NetworkUtil.isNetworkAvailable(this@PickupDoneActivity)) {
                DisplayUtil.AlertDialog(
                    this@PickupDoneActivity,
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
            locationModel.setDriverLocation(latitude, longitude)

            //사인이미지를 그리지 않았다면
            if (!binding.signApplicantSignature.isTouch) {
                Toast.makeText(
                    this@PickupDoneActivity,
                    resources.getString(R.string.msg_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            //사인이미지를 그리지 않았다면
            if (!binding.signCollectorSignature.isTouch) {
                Toast.makeText(
                    this@PickupDoneActivity,
                    resources.getString(R.string.msg_collector_signature_require),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val driverMemo = binding.editMemo.text.toString().trim { it <= ' ' }
            if (driverMemo == "") {
                Toast.makeText(
                    this@PickupDoneActivity,
                    resources.getString(R.string.msg_must_enter_memo),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (MemoryStatus.availableInternalMemorySize != MemoryStatus.ERROR.toLong()
                && MemoryStatus.availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
            ) {
                DisplayUtil.AlertDialog(
                    this@PickupDoneActivity,
                    resources.getString(R.string.msg_disk_size_error)
                )
                return
            }

            progressBar.visibility = View.VISIBLE

            FirebaseEvent.clickEvent(this, tag, "SetPickupUploadData_ScanAll ")

            lifecycleScope.launch {

                DataUtil.captureSign("/QdrivePickup", pickupNo, binding.signApplicantSignature)
                DataUtil.captureSign("/QdriveCollector", pickupNo, binding.signCollectorSignature)

                val contentVal = ContentValues()
                contentVal.put("stat", BarcodeType.PICKUP_DONE)
                contentVal.put("real_qty", binding.textTotalQty.text.toString())
                contentVal.put("fail_reason", "")
                contentVal.put("driver_memo", binding.editMemo.text.toString().trim { it <= ' ' })
                contentVal.put("retry_dt", "")

                DatabaseHelper.getInstance().update(
                    DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                    contentVal,
                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                    arrayOf(pickupNo, Preferences.userId)
                )

                try {
                    val bitmap1 = QDataUtil.getBitmapString(
                        this@PickupDoneActivity,
                        binding.signApplicantSignature,
                        ImageUpload.QXPOP,
                        "qdriver/sign",
                        pickupNo
                    )

                    val bitmap2 = QDataUtil.getBitmapString(
                        this@PickupDoneActivity,
                        binding.signCollectorSignature,
                        ImageUpload.QXPOP,
                        "qdriver/sign",
                        pickupNo
                    )

                    if (bitmap1 == "" || bitmap2 == "") {
                        alertShow(resources.getString(R.string.msg_upload_fail_image))
                        return@launch
                    }

                    val response =
                        RetrofitClient.instanceDynamic().requestSetPickupUpLoadDataScanAll(
                            pickupNo,
                            latitude,
                            longitude,
                            binding.textTotalQty.text.toString(),
                            bitmap1,
                            bitmap2,
                            mStrWaybillNo,
                            NetworkUtil.getNetworkType(this@PickupDoneActivity),
                            binding.editMemo.text.toString().trim { it <= ' ' }
                        )

                    if (response.resultCode == 0) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val date = Date()

                        val changeDataString: String = dateFormat.format(date)

                        val contentVal2 = ContentValues()
                        contentVal2.put("punchOut_stat", "S")
                        contentVal2.put("chg_dt", changeDataString)

                        DatabaseHelper.getInstance().update(
                            DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                            contentVal2,
                            "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                            arrayOf(pickupNo, Preferences.userId)
                        )

                        // CnR, Lazada Data Scan시 함께 Done 처리
                        val scannedList: Array<String> = mStrWaybillNo.split(",").toTypedArray()
                        for (item in scannedList) {
                            val cursor =
                                DatabaseHelper.getInstance()["SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + item + "' COLLATE NOCASE"]

                            if (cursor.moveToFirst()) {
                                val contentVal3 = ContentValues()
                                contentVal3.put("stat", BarcodeType.PICKUP_DONE)
                                contentVal3.put("real_qty", "1")
                                contentVal3.put("chg_dt", dateFormat.format(date))
                                contentVal3.put("fail_reason", "")
                                contentVal3.put("retry_dt", "")
                                contentVal3.put(
                                    "driver_memo",
                                    binding.editMemo.text.toString().trim { it <= ' ' })
                                contentVal3.put("reg_id", Preferences.userId)
                                contentVal3.put("punchOut_stat", "S")

                                DatabaseHelper.getInstance().update(
                                    DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                    contentVal3,
                                    "invoice_no=? COLLATE NOCASE " + "and reg_id = ?",
                                    arrayOf(item, Preferences.userId)
                                )
                            }
                            cursor.close()
                        }

                        val msg = String.format(
                            resources.getString(R.string.text_upload_success_count),
                            1
                        )
                        resultDialog(msg)
                        return@launch

                    } else {
                        resultDialog("api error" + response.resultMsg)
                    }


                } catch (e: java.lang.Exception) {

                    resultDialog("api Exception error $e")
                }

                progressBar.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@PickupDoneActivity,
                resources.getString(R.string.text_error) + " - " + e.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resultDialog(msg: String) {

        if (!isFinishing) {
            val builder = AlertDialog.Builder(this@PickupDoneActivity)
            builder.setCancelable(false)
            builder.setTitle(resources.getString(R.string.text_upload_result))
            builder.setMessage(msg)
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog, _ ->

                dialog.dismiss()
                finish()
            }
            builder.show()
        }
    }

    private fun alertShow(msg: String) {
        AlertDialog.Builder(this)
            .setTitle(
                resources.getString(R.string.text_upload_failed)
            )
            .setMessage(msg)
            .setPositiveButton(
                resources
                    .getString(R.string.button_close)
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss() // 닫기
            }
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

}
