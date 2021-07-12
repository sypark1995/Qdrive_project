package com.giosis.library.list.pickup

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.giosis.library.MemoryStatus
import com.giosis.library.R
import com.giosis.library.databinding.ActivityPickupStartToScanBinding
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.gps.LocationModel
import com.giosis.library.util.*

/**
 * @author eylee
 * @date 2016-09-28
 * Pickup done -> Bundle type scan all 로 기능개선
 * @editor krm0219
 * LIST > In-Progress > 'Start To Scan'
 */
class PickupDoneActivity : CommonActivity() {
    var tag = "PickupDoneActivity"

    private val binding by lazy {
        ActivityPickupStartToScanBinding.inflate(layoutInflater)
    }

    //
    lateinit var pickupNo: String
    private lateinit var mStrWaybillNo: String

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    private var locationModel = LocationModel()


    var isPermissionTrue = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
        val cs = DatabaseHelper.getInstance()["SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + pickupNo + "'"]
        if (cs.moveToFirst()) {

            val parcelLat = cs.getDouble(cs.getColumnIndex("lat"))
            val parcelLng = cs.getDouble(cs.getColumnIndex("lng"))
            val zipCode = cs.getString(cs.getColumnIndex("zip_code"))
            val state = cs.getString(cs.getColumnIndex("state"))
            val city = cs.getString(cs.getColumnIndex("city"))
            val street = cs.getString(cs.getColumnIndex("street"))
            Log.e("GPSUpdate", "Parcel $pickupNo // $parcelLat, $parcelLng // $zipCode - $state - $city - $street")

            locationModel.setParcelLocation(parcelLat, parcelLng, zipCode, state, city, street)
        }


        // Memo 입력제한
        binding.editMemo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (99 <= binding.editMemo.length()) {

                    Toast.makeText(this@PickupDoneActivity, resources.getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
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
            PermissionActivity.startActivityForResult(this@PickupDoneActivity, PERMISSION_REQUEST_CODE, *PERMISSIONS)
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

                gpsTrackerManager!!.GPSTrackerStart()
                Log.e("Location", "$tag GPSTrackerManager onResume : ${gpsTrackerManager!!.latitude}  ${gpsTrackerManager!!.longitude}  ")
            } else {

                DataUtil.enableLocationSettings(this@PickupDoneActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", "$tag   onActivityResult  PERMISSIONS_GRANTED")
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
                .setNegativeButton(R.string.button_cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }.show()
    }

    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-07-15
     */
    private fun saveServerUploadSign() {
        try {

            if (!NetworkUtil.isNetworkAvailable(this@PickupDoneActivity)) {

                DisplayUtil.AlertDialog(this@PickupDoneActivity, resources.getString(R.string.msg_network_connect_error))
                return
            }

            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
            locationModel.setDriverLocation(latitude, longitude)
            Log.e("Location", "$tag saveServerUploadSign  GPSTrackerManager : $latitude  $longitude  - ${locationModel.driverLat}, ${locationModel.driverLng}")


            val realQty = binding.textTotalQty.text.toString()

            //사인이미지를 그리지 않았다면
            if (!binding.signApplicantSignature.isTouch) {
                Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                return
            }
            //사인이미지를 그리지 않았다면
            if (!binding.signCollectorSignature.isTouch) {
                Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            val driverMemo = binding.editMemo.text.toString().trim { it <= ' ' }
            if (driverMemo == "") {
                Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.msg_must_enter_memo), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {

                DisplayUtil.AlertDialog(this@PickupDoneActivity, resources.getString(R.string.msg_disk_size_error))
                return
            }


            DataUtil.logEvent("button_click", tag, "SetPickupUploadData_ScanAll")

            PickupDoneUploadHelper.Builder(this@PickupDoneActivity, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
                    pickupNo, mStrWaybillNo, realQty, binding.signApplicantSignature, binding.signCollectorSignature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), locationModel)
                    .setOnServerEventListener(object : OnServerEventListener {
                        override fun onPostResult() {

                            DataUtil.inProgressListPosition = 0
                            finish()
                        }

                        override fun onPostFailList() {}
                    }).build().execute()
        } catch (e: Exception) {

            Log.e("Exception", "$tag  Exception : $e")
            Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
                PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)
    }
}