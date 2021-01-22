package com.giosis.library.list.pickup

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.giosis.library.MemoryStatus
import com.giosis.library.R
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.util.*
import kotlinx.android.synthetic.main.activity_pickup_start_to_scan.*
import kotlinx.android.synthetic.main.top_title.*

/**
 * @author eylee
 * @date 2016-09-28
 * Pickup done -> Bundle type scan all 로 기능개선
 * @editor krm0219
 * LIST > In-Progress > 'Start To Scan'
 */
class PickupDoneActivity : CommonActivity() {
    var tag = "PickupDoneActivity"

    //
    lateinit var pickupNo: String
    private lateinit var mStrWaybillNo: String

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false
    var latitude = 0.0
    var longitude = 0.0

    var isPermissionTrue = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_start_to_scan)

        layout_top_back.setOnClickListener(clickListener)
        layout_sign_p_applicant_eraser.setOnClickListener(clickListener)
        layout_sign_p_collector_eraser.setOnClickListener(clickListener)
        btn_sign_p_save.setOnClickListener(clickListener)

        //
        pickupNo = intent.getStringExtra("pickupNo").toString()
        val applicant = intent.getStringExtra("applicant")
        mStrWaybillNo = intent.getStringExtra("scannedList").toString()
        val strReqQty = intent.getStringExtra("scannedQty")

        text_top_title.text = resources.getString(R.string.text_start_to_scan)
        text_sign_p_pickup_no.text = pickupNo
        text_sign_p_applicant.text = applicant
        img_sign_p_start_scan.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on)
        img_sign_p_zero_qty.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off)
        text_sign_p_total_qty.text = strReqQty


        // Memo 입력제한
        edit_sign_p_memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (99 <= edit_sign_p_memo.length()) {

                    Toast.makeText(this@PickupDoneActivity, resources.getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        //
        val checker = PermissionChecker(this)

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(*PERMISSIONS)) {
            isPermissionTrue = false
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {
            isPermissionTrue = true
        }
    }

    override fun onResume() {
        super.onResume()

        if (isPermissionTrue) {

            gpsTrackerManager = GPSTrackerManager(this@PickupDoneActivity)
            gpsEnable = gpsTrackerManager!!.enableGPSSetting()

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager!!.GPSTrackerStart()
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
                Log.e("Location", "$tag GPSTrackerManager onResume : $latitude  $longitude  ")
            } else {

                DataUtil.enableLocationSettings(this)
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
        AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int -> finish() }
                .setNegativeButton(R.string.button_cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }.show()
    }

    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-07-15
     */
    fun saveServerUploadSign() {
        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                DisplayUtil.AlertDialog(this, resources.getString(R.string.msg_network_connect_error))
                return
            }

            if (gpsTrackerManager != null) {
                latitude = gpsTrackerManager!!.latitude
                longitude = gpsTrackerManager!!.longitude
                Log.e("Location", "$tag saveServerUploadSign  GPSTrackerManager : $latitude  $longitude  ")
            }


            val realQty = text_sign_p_total_qty.text.toString()

            //사인이미지를 그리지 않았다면
            if (!sign_view_sign_p_applicant_signature.isTouch) {
                Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show()
                return
            }
            //사인이미지를 그리지 않았다면
            if (!sign_view_sign_p_collector_signature.isTouch) {
                Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.msg_collector_signature_require), Toast.LENGTH_SHORT).show()
                return
            }

            val driverMemo = edit_sign_p_memo.text.toString().trim { it <= ' ' }
            if (driverMemo == "") {
                Toast.makeText(this@PickupDoneActivity, resources.getString(R.string.msg_must_enter_memo), Toast.LENGTH_SHORT).show()
                return
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR.toLong() && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {

                DisplayUtil.AlertDialog(this, resources.getString(R.string.msg_disk_size_error))
                return
            }


            DataUtil.logEvent("button_click", tag, "SetPickupUploadData_ScanAll")

            PickupDoneUploadHelper.Builder(this, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
                    pickupNo, mStrWaybillNo, realQty, sign_view_sign_p_applicant_signature, sign_view_sign_p_collector_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
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

    var clickListener = View.OnClickListener { view ->

        when (view.id) {
            R.id.layout_top_back -> {

                cancelSigning()
            }
            R.id.layout_sign_p_applicant_eraser -> {

                sign_view_sign_p_applicant_signature.clearText()
            }
            R.id.layout_sign_p_collector_eraser -> {

                sign_view_sign_p_collector_signature.clearText()
            }
            R.id.btn_sign_p_save -> {

                saveServerUploadSign()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
                PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE)
    }
}