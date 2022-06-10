package com.giosis.util.qdrive.singapore.barcodescanner


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.giosis.util.qdrive.singapore.BuildConfig
import com.giosis.util.qdrive.singapore.LoginActivity
import com.giosis.util.qdrive.singapore.MemoryStatus
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.barcodescanner.bluetooth.BluetoothChatService
import com.giosis.util.qdrive.singapore.barcodescanner.bluetooth.KScan
import com.giosis.util.qdrive.singapore.barcodescanner.bluetooth.KTSyncData
import com.giosis.util.qdrive.singapore.data.CaptureData
import com.giosis.util.qdrive.singapore.data.CnRPickupResult
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.databinding.ActivityCaptureBinding
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.list.BarcodeData
import com.giosis.util.qdrive.singapore.list.delivery.DeliveryDoneActivity2
import com.giosis.util.qdrive.singapore.list.pickup.*
import com.giosis.util.qdrive.singapore.main.submenu.SelfCollectionDoneActivity
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView.TorchListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class CaptureActivity1 : CommonActivity(), TorchListener, OnTouchListener, TextWatcher,
    View.OnKeyListener {

    companion object {

        private const val TAG = "CaptureActivity1"
        private const val bluetoothTAG = "Capture_Bluetooth"

        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(
            PermissionChecker.ACCESS_FINE_LOCATION,
            PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE,
            PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.CAMERA
        )

        /*
         * Qxpress송장번호 규칙(범용)
         * 운송장번호 규칙이 맞는지 체크
         * 10문자 안넘으면 false, 맨앞두글자가 KR,SG,QX,JP,CN이 아닐경우 false, 5,6번째가 숫자가 아닐경우 false, 영문숫자조합
          SELF_COLLECTION */

        fun isInvoiceCodeRule(invoiceNo: String?): Boolean {

            if (invoiceNo!!.length < 10) return false

            val bln = Pattern.matches("^[a-zA-Z0-9]*$", invoiceNo)
            if (!bln) {
                return false
            }

            if (10 <= invoiceNo.length) {    // self collection c2c 아닐 때

                val subInvoice = invoiceNo.substring(4, 6)
                return isStringDouble(subInvoice)
            }
            return true
        }

        private fun isStringDouble(s: String): Boolean {
            return try {
                s.toDouble()
                true
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private val binding by lazy {
        ActivityCaptureBinding.inflate(layoutInflater)
    }

    // intent
    val title: String by lazy {
        intent.getStringExtra("title") ?: ""
    }
    val titleResource: Int by lazy {
        intent.getIntExtra("title_resource", 0)
    }
    private val mScanType: String by lazy {
        intent.getStringExtra("type") ?: ""
    }
    val pickupNo: String by lazy {
        intent.getStringExtra("pickup_no") ?: ""
    }
    private val applicant: String by lazy {
        intent.getStringExtra("applicant") ?: ""
    }
    private val qty: String by lazy {
        intent.getStringExtra("qty") ?: ""
    }
    private val route: String by lazy {
        intent.getStringExtra("route") ?: ""
    }
    private var resultData: OutletPickupDoneResult.OutletPickupDoneItem? = null

    private val cameraManager: CaptureManager by lazy {
        CaptureManager(this, binding.barcodeScanner)
    }
    private var connectedDevice: BluetoothDevice? = null
    private val mBluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var mIsScanDeviceListActivityRun = false
    private var connectedDeviceName: String? = null

    private val mUpdateTimeTask = Runnable {
        if (KTSyncData.AutoConnect && KTSyncData.bIsRunning) {
            KTSyncData.mChatService.connect(connectedDevice)
        }
    }

    //
    private val deleteDrawable by lazy {
        ContextCompat.getDrawable(this, R.drawable.btn_delete)
    }

    val adapter: ScannedBarcodeAdapter by lazy {
        ScannedBarcodeAdapter(scanBarcodeArrayList, mScanType)
    }
    private var scanBarcodeArrayList = ArrayList<BarcodeData>()

    //2016-09-03 pickup cnr Requester
    private var pickupCNRRequester: String? = null
    private var isNonQ10QFSOrder = false

    private val progressBar by lazy {
        ProgressDialog(this)
    }
    private val inputMethodManager: InputMethodManager by lazy {
        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private val beepManager: BeepManager by lazy {
        BeepManager(this, BeepManager.BELL_SOUNDS_SUCCESS)
    }
    private val beepManagerError: BeepManager by lazy {
        BeepManager(this, BeepManager.BELL_SOUNDS_ERROR)
    }
    private val beepManagerDuple: BeepManager by lazy {
        BeepManager(this, BeepManager.BELL_SOUNDS_DUPLE)
    }

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false
    var isPermissionTrue = false

    var clickListener = View.OnClickListener { v ->

        when (v.id) {
            R.id.layout_top_back -> {
                onResetButtonClick()
                finish()
            }
            R.id.layout_camera -> {

                binding.layoutCamera.isSelected = true
                binding.textCamera.isSelected = true
                binding.layoutScanner.isSelected = false
                binding.textScanner.isSelected = false
                binding.layoutBluetooth.isSelected = false
                binding.textBluetooth.isSelected = false

                binding.layoutScannerMode.visibility = View.GONE
                binding.layoutBluetoothMode.visibility = View.GONE

                // bluetooth
                if (KTSyncData.mChatService != null) KTSyncData.mChatService.stop()
                KTSyncData.bIsRunning = false
                onResume()
            }
            R.id.layout_scanner -> {

                binding.layoutCamera.isSelected = false
                binding.textCamera.isSelected = false
                binding.layoutScanner.isSelected = true
                binding.textScanner.isSelected = true
                binding.layoutBluetooth.isSelected = false
                binding.textBluetooth.isSelected = false

                binding.layoutScannerMode.visibility = View.VISIBLE
                binding.layoutBluetoothMode.visibility = View.GONE

                // Camera
                cameraManager.onPause()
                // bluetooth
                if (KTSyncData.mChatService != null) KTSyncData.mChatService.stop()
                KTSyncData.bIsRunning = false
            }
            R.id.layout_bluetooth -> {

                binding.layoutCamera.isSelected = false
                binding.textCamera.isSelected = false
                binding.layoutScanner.isSelected = false
                binding.textScanner.isSelected = false
                binding.layoutBluetooth.isSelected = true
                binding.textBluetooth.isSelected = true

                binding.layoutScannerMode.visibility = View.GONE
                binding.layoutBluetoothMode.visibility = View.VISIBLE

                // Camera
                cameraManager.onPause()

                // Bluetooth 지원 && 비활성화 상태
                if (mBluetoothAdapter != null) {
                    if (!mBluetoothAdapter!!.isEnabled) {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothLauncher.launch(intent)
                    }
                }
                KTSyncData.bIsRunning = true
            }
            R.id.btn_bluetooth_device_find -> {
                mIsScanDeviceListActivityRun = true

                val intent = Intent(this@CaptureActivity1, DeviceListActivity1::class.java)
                deviceLauncher.launch(intent)
            }
            R.id.btn_add -> {
                onAddButtonClick()
            }
            R.id.btn_reset -> {
                onResetButtonClick()
            }
            R.id.btn_confirm -> {
                when (mScanType) {
                    CaptureType.CONFIRM_MY_DELIVERY_ORDER,
                    CaptureType.CHANGE_DELIVERY_DRIVER -> {
                        onUpdateButtonClick()
                    }
                    CaptureType.PICKUP_CNR,
                    CaptureType.PICKUP_SCAN_ALL,
                    CaptureType.PICKUP_ADD_SCAN,
                    CaptureType.PICKUP_TAKE_BACK,
                    CaptureType.OUTLET_PICKUP_SCAN -> {
                        onNextButtonClick()
                    }
                    CaptureType.DELIVERY_DONE -> {
                        onConfirmButtonClick()
                    }
                    CaptureType.SELF_COLLECTION -> {
                        onCaptureConfirmButtonClick()
                    }
                }
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(binding.root)

        FirebaseEvent.createEvent(this, TAG)

        if (title.isNotEmpty()) {
            binding.layoutTopTitle.textTopTitle.text = title
        } else {
            binding.layoutTopTitle.textTopTitle.text = resources.getString(titleResource)
        }

        binding.layoutCamera.isSelected = true
        binding.layoutScanner.isSelected = false
        binding.layoutBluetooth.isSelected = false

        binding.recyclerScannedBarcode.adapter = adapter

        if (mScanType == CaptureType.OUTLET_PICKUP_SCAN) {

            resultData =
                intent.getSerializableExtra("tracking_data") as OutletPickupDoneResult.OutletPickupDoneItem

            if (route == "FL") {
                binding.layoutTopTitle.textTopTitle.setText(R.string.text_title_fl_pickup)
            }

            val listItem = resultData!!.trackingNoList
            var i = 0
            while (i < listItem.size) {
                val data = BarcodeData()
                data.state = "FAIL"
                data.barcode = listItem[i].trackingNo
                scanBarcodeArrayList.add(i, data)
                i++
            }

            // TEST_
//                for(index in listItem.indices) {
//                    val data = BarcodeData()
//                    data.state = "FAIL"
//                    data.barcode = listItem[index].trackingNo
//                    scanBarcodeArrayList.add(index, data)
//                }
        }

        if (scanBarcodeArrayList.isNotEmpty()) {
            binding.recyclerScannedBarcode.scrollToPosition(scanBarcodeArrayList.size - 1)
        }

        binding.layoutTopTitle.layoutTopBack.setOnClickListener(clickListener)
        binding.layoutCamera.setOnClickListener(clickListener)
        binding.layoutScanner.setOnClickListener(clickListener)
        binding.layoutBluetooth.setOnClickListener(clickListener)
        binding.btnBluetoothDeviceFind.setOnClickListener(clickListener)
        binding.editTrackingNumber.setOnClickListener(clickListener)
        binding.btnAdd.setOnClickListener(clickListener)
        binding.btnReset.setOnClickListener(clickListener)
        binding.btnConfirm.setOnClickListener(clickListener)

        binding.editTrackingNumber.setOnTouchListener(this)
        binding.editTrackingNumber.addTextChangedListener(this)
        binding.editTrackingNumber.setOnKeyListener(this)
        binding.editTrackingNumber.isLongClickable = false
        binding.editTrackingNumber.setTextIsSelectable(false)
        deleteDrawable?.setBounds(
            0,
            0,
            deleteDrawable!!.intrinsicWidth,
            deleteDrawable!!.intrinsicHeight
        )

        // 블루투스 초기화
        initBluetoothDevice()
        // 초기화
        initManualScanViews(mScanType)

        binding.barcodeScanner.setTorchListener(this)
        cameraManager.initializeFromIntent(intent, savedInstanceState)

        binding.barcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {

                checkValidation(result.toString().uppercase())
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        })

        binding.toggleCameraFlash.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                binding.barcodeScanner.setTorchOn()
            } else {
                binding.barcodeScanner.setTorchOff()
            }
        }

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

    private fun initManualScanViews(scanType: String) {

        binding.layoutScannedCount.visibility = View.VISIBLE
        when (scanType) {

            CaptureType.CONFIRM_MY_DELIVERY_ORDER -> {
                //onUpdateButtonClick
                binding.btnConfirm.text = resources.getString(R.string.button_update)
            }
            CaptureType.CHANGE_DELIVERY_DRIVER -> {
                //onUpdateButtonClick
                binding.btnConfirm.text = resources.getString(R.string.button_done)
            }
            CaptureType.DELIVERY_DONE -> {
                //onConfirmButtonClick
                binding.layoutScannedCount.visibility = View.GONE
                binding.btnConfirm.text = resources.getString(R.string.button_confirm)
            }
            CaptureType.PICKUP_CNR,
            CaptureType.PICKUP_SCAN_ALL,
            CaptureType.PICKUP_ADD_SCAN,
            CaptureType.PICKUP_TAKE_BACK,
            CaptureType.OUTLET_PICKUP_SCAN -> {
                //onNextButtonClick
                binding.btnConfirm.text = resources.getString(R.string.button_next)
            }
            CaptureType.SELF_COLLECTION -> {
                // onCaptureConfirmButtonClick
                binding.btnConfirm.text = resources.getString(R.string.button_confirm)
            }
        }
    }

    @Synchronized
    public override fun onResume() {
        super.onResume()

        if (Preferences.userId == "") {
            Toast.makeText(
                this@CaptureActivity1,
                resources.getString(R.string.msg_qdrive_auto_logout),
                Toast.LENGTH_SHORT
            ).show()

            try {
                val intent = Intent(this@CaptureActivity1, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } catch (ignored: Exception) {
            }
        }

        try {
            binding.editTrackingNumber.requestFocus()
        } catch (e: Exception) {
            //
        }

        progressBar.setCancelable(false)

        beepManager.updatePrefs()
        beepManagerError.updatePrefs()
        beepManagerDuple.updatePrefs()

        // Bluetooth
        if (KTSyncData.bIsRunning) return
        if (KTSyncData.mChatService != null) {
            if (KTSyncData.mChatService.state == BluetoothChatService.STATE_NONE) {
                KTSyncData.mChatService.start()
            }
        }
        if (KTSyncData.bIsConnected && KTSyncData.LockUnlock) {
            KTSyncData.mKScan.LockUnlockScanButton(true)
        }
        KTSyncData.mKScan.mHandler = bluetoothHandler

        if (isPermissionTrue) {

            // Camera
            cameraManager.onResume()

            // Location
            if (mScanType == CaptureType.CHANGE_DELIVERY_DRIVER) {

                gpsTrackerManager = GPSTrackerManager(this@CaptureActivity1)
                gpsTrackerManager?.let {
                    gpsEnable = it.enableGPSSetting()
                }

                if (gpsEnable && gpsTrackerManager != null) {
                    gpsTrackerManager!!.gpsTrackerStart()
                } else {
                    DataUtil.enableLocationSettings(this@CaptureActivity1)
                }
            }
        }

    }

    private fun warningDialog(msg: String) {
        if (!isFinishing) {
            val builder = AlertDialog.Builder(this@CaptureActivity1)
            builder.setTitle(resources.getString(R.string.text_warning))
            builder.setMessage(msg)
            builder.setPositiveButton(resources.getString(R.string.button_close)) { dialog: DialogInterface, _ ->
                dialog.dismiss()
                finish()
            }
            builder.show()
        }
    }

    private fun resultDialog(title: String, msg: String?) {
        if (!isFinishing) {
            val builder = AlertDialog.Builder(this@CaptureActivity1)
            builder.setCancelable(false)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _ ->
                dialog.cancel()
            }
            builder.show()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (mScanType == CaptureType.PICKUP_CNR
            || mScanType == CaptureType.PICKUP_SCAN_ALL
            || mScanType == CaptureType.PICKUP_ADD_SCAN
            || mScanType == CaptureType.OUTLET_PICKUP_SCAN
            || mScanType == CaptureType.PICKUP_TAKE_BACK
        ) {
            //
        } else {
            inputMethodManager.showSoftInput(
                binding.editTrackingNumber,
                InputMethodManager.SHOW_IMPLICIT
            )
        }

        if (event.action != MotionEvent.ACTION_UP) {
            return false
        }

        val editText = v as EditText
        if (event.x > editText.width - editText.paddingRight - deleteDrawable!!.intrinsicWidth) {
            editText.setText("")
            editText.setCompoundDrawables(null, null, null, null)
        }
        return true
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        if (binding.editTrackingNumber.text.toString().isEmpty()) {
            binding.editTrackingNumber.setCompoundDrawables(null, null, null, null)
        } else {
            binding.editTrackingNumber.setCompoundDrawables(null, null, deleteDrawable, null)
        }

        binding.editTrackingNumber.compoundDrawablePadding = 28
    }

    override fun afterTextChanged(s: Editable) {}


    // Scanner
    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER) {

                val tempScanNo = binding.editTrackingNumber.text.toString().trim()

                if (tempScanNo.isNotEmpty()) {
                    checkValidation(tempScanNo.uppercase())
                }
                return true
            }
        }
        return true
    }

    // Bluetooth
    private fun onBluetoothBarcodeAdd(strBarcodeNo: String) {

        // bluetooth "\n"이 포함되어서 다른번호로 인식 > trim 으로 공백 없애기
        val tempScanNo = strBarcodeNo.trim()

        if (tempScanNo.isNotEmpty()) {
            Log.i(TAG, "  onBluetoothBarcodeAdd > $tempScanNo ")

            checkValidation(tempScanNo.uppercase())
        }
    }

    // EditText
    private fun onAddButtonClick() {

        val tempScanNo = binding.editTrackingNumber.text.toString().trim()

        if (tempScanNo.isNotEmpty()) {
            checkValidation(tempScanNo.uppercase())
        }
    }

    // 중복해서 들어가는것 확인용....
    var checkedBarcodeList = ArrayList<String>()

    // Add Barcode  (Validation Check / Add List)
    // NOTIFICATION.  Barcode Validation Check
    private fun checkValidation(barcode: String) {

        if (!NetworkUtil.isNetworkAvailable(this@CaptureActivity1)) {
            warningDialog(resources.getString(R.string.msg_network_connect_error))
            return
        }

        val isDuplicate = checkedBarcodeList.contains(barcode.uppercase())

        if (isDuplicate) {
            beepManagerDuple.playBeepSoundAndVibrate()
            val toast = Toast.makeText(
                this@CaptureActivity1,
                R.string.msg_tracking_number_already_entered,
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER, 0, 20)
            toast.show()

            binding.editTrackingNumber.setText("")
            inputMethodManager.hideSoftInputFromWindow(binding.editTrackingNumber.windowToken, 0)
            return
        } else {
            checkedBarcodeList.add(barcode.uppercase())
        }

        val strBarcodeNo = barcode.replace("\\r\\n|\\r|\\n".toRegex(), "")

        when (mScanType) {
            CaptureType.CONFIRM_MY_DELIVERY_ORDER -> {

                val type = if (Preferences.outletDriver == "Y") {
                    "OL"
                } else {
                    "STD"
                }

                RetrofitClient.instanceDynamic().requestValidationCheckDpc3Out(strBarcodeNo, type)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (it.resultCode < 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            beepManager.playBeepSoundAndVibrate()
                            addScannedBarcode(strBarcodeNo)

                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.CHANGE_DELIVERY_DRIVER -> {

                RetrofitClient.instanceDynamic().requestValidationCheckChangeDriver(strBarcodeNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (it.resultCode < 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            beepManager.playBeepSoundAndVibrate()

                            val result = Gson().fromJson(
                                it.resultObject,
                                ChangeDriverResult.Data::class.java
                            )

                            if (result != null) {
                                addScannedBarcode(
                                    result.trackingNo,
                                    result.status,
                                    result.currentDriver,
                                    result.contrNo
                                )
                            }

                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.PICKUP_CNR -> {
                // Edit.  2020.03  배포 (기존 CNR 중복 허용됨 > 중복 허용X 수정)
                RetrofitClient.instanceDynamic().requestValidationCheckCnR(strBarcodeNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        Log.e("Server", "requestValidationCheckCnR  result  " + it.resultCode)
                        if (it.resultCode != 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            val cnRPickupData =
                                Gson().fromJson(it.resultObject, CnRPickupResult::class.java)
                            val isDBDuplicate =
                                checkDBDuplicate(cnRPickupData.contrNo, cnRPickupData.invoiceNo)

                            if (isDBDuplicate) {
                                getCnrRequester(cnRPickupData.invoiceNo)
                            } else {
                                insertCnRData(cnRPickupData)
                            }

                            beepManager.playBeepSoundAndVibrate()
                            pickupCNRRequester = cnRPickupData.reqName
                            addScannedBarcode(strBarcodeNo)
                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.PICKUP_SCAN_ALL -> {

                RetrofitClient.instanceDynamic()
                    .requestValidationCheckPickup(pickupNo, strBarcodeNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        Log.e("Server", "requestValidationCheckPickup  result  " + it.resultCode)
                        if (it.resultCode < 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            beepManager.playBeepSoundAndVibrate()
                            addScannedBarcode(strBarcodeNo)
                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.PICKUP_ADD_SCAN -> {

                RetrofitClient.instanceDynamic()
                    .requestValidationCheckPickup(pickupNo, strBarcodeNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        Log.e("Server", "requestValidationCheckPickup  result  " + it.resultCode)
                        if (it.resultCode < 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            beepManager.playBeepSoundAndVibrate()
                            addScannedBarcode(strBarcodeNo)
                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.PICKUP_TAKE_BACK -> {

                RetrofitClient.instanceDynamic()
                    .requestValidationCheckTakeBack(pickupNo, strBarcodeNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        Log.e("Server", "requestValidationCheckTakeBack  result  " + it.resultCode)
                        if (it.resultCode < 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            beepManager.playBeepSoundAndVibrate()
                            addScannedBarcode(strBarcodeNo)
                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.OUTLET_PICKUP_SCAN -> {

                RetrofitClient.instanceDynamic()
                    .requestValidationCheckPickup(pickupNo, strBarcodeNo, route)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        Log.e("Server", "requestValidationCheckPickup  result  " + it.resultCode)
                        if (it.resultCode < 0) {

                            beepManagerError.playBeepSoundAndVibrate()
                            binding.editTrackingNumber.setText("")
                            inputMethodManager.hideSoftInputFromWindow(
                                binding.editTrackingNumber.windowToken,
                                0
                            )
                            resultDialog(
                                resources.getString(R.string.text_scanned_failed),
                                it.resultMsg
                            )
                        } else {

                            beepManager.playBeepSoundAndVibrate()
                            addScannedBarcode(strBarcodeNo)
                        }
                    }) {
                        Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_error_check_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            CaptureType.SELF_COLLECTION -> {

                if (!isInvoiceCodeRule(strBarcodeNo)) {

                    beepManagerError.playBeepSoundAndVibrate()
                    val toast = Toast.makeText(
                        this@CaptureActivity1,
                        resources.getString(R.string.msg_invalid_scan),
                        Toast.LENGTH_SHORT
                    )
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                    return
                }

                beepManager.playBeepSoundAndVibrate()

                //2016-09-12 eylee nq 끼리만 self collector 가능하게 수정하기
                if (scanBarcodeArrayList.isNotEmpty()) {
                    val tempIsNonQ10QFSOrder = isNonQ10QFSOrder
                    val tempValidation = isNonQ10QFSOrderForSelfCollection(strBarcodeNo)

                    if (tempIsNonQ10QFSOrder != tempValidation) {
                        // alert 띄워줘야 함 type 이 다르다는 - 기존 Self - Collection 과 새로 NQ 가
                        val toast = Toast.makeText(
                            this@CaptureActivity1,
                            resources.getString(R.string.msg_different_order_type),
                            Toast.LENGTH_SHORT
                        )
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                        toast.show()
                    } else {
                        isNonQ10QFSOrder = tempValidation
                    }
                } else {
                    isNonQ10QFSOrder = isNonQ10QFSOrderForSelfCollection(strBarcodeNo)
                }

                addScannedBarcode(strBarcodeNo)
            }
            else -> {

                beepManager.playBeepSoundAndVibrate()
                addScannedBarcode(strBarcodeNo)
            }
        }
    }


    // NOTIFICATION.  Add Barcode List
    private fun addScannedBarcode(
        barcodeNo: String,
        status: String = "",
        currentDriver: String = "",
        contrNo: String = ""
    ) {
        Log.e(TAG, "  addScannedBarcode   // $barcodeNo")

        val data = BarcodeData()
        data.barcode = barcodeNo.uppercase()
        data.state = "NONE"

        data.status = status
        data.currentDriver = currentDriver
        data.contrNo = contrNo

        when (mScanType) {
            CaptureType.CHANGE_DELIVERY_DRIVER,
            CaptureType.CONFIRM_MY_DELIVERY_ORDER,
            CaptureType.PICKUP_CNR,
            CaptureType.PICKUP_SCAN_ALL,
            CaptureType.PICKUP_ADD_SCAN,
            CaptureType.PICKUP_TAKE_BACK -> {

                // 스캔 시 최근 스캔한 바코드가 제일 위로 셋팅됨.
                data.state = "SUCCESS"

                scanBarcodeArrayList.add(0, data)

                adapter.notifyDataSetChanged()
                binding.recyclerScannedBarcode.smoothScrollToPosition(0)
            }

            CaptureType.OUTLET_PICKUP_SCAN -> {

                val listItem = resultData!!.trackingNoList
                var position = -400
                var i = 0

                while (i < listItem.size) {

                    val trackingNo = listItem[i].trackingNo
                    if (trackingNo.equals(barcodeNo, ignoreCase = true)) {
                        position = i
                        data.state = "SUCCESS"
                        listItem[i].isScanned = true
                    }
                    i++
                }

                if (0 <= position) {

                    scanBarcodeArrayList[position] = data
                    adapter.notifyDataSetChanged()
                    binding.recyclerScannedBarcode.smoothScrollToPosition(0)

                } else {

                    adapter.notifyDataSetChanged()

                    if (!isFinishing) {
                        val alertDialog = AlertDialog.Builder(this@CaptureActivity1)
                        alertDialog.setTitle(resources.getString(R.string.text_warning))
                        alertDialog.setMessage(resources.getString(R.string.msg_no_outlet_parcels))
                        alertDialog.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _ -> dialog.dismiss() }
                        alertDialog.show()
                    }
                }
            }
            else -> {
                //스캔 시 최근 스캔한 바코드가 아래로 추가됨.
                // maybe.. DELIVERY DONE, SELF COLLECTION
                scanBarcodeArrayList.add(data)
                adapter.notifyDataSetChanged()
            }
        }

        if (mScanType != CaptureType.CONFIRM_MY_DELIVERY_ORDER
            && mScanType != CaptureType.CHANGE_DELIVERY_DRIVER
            && mScanType != CaptureType.PICKUP_CNR
        ) {
            updateInvoiceNO(barcodeNo)
        }

        binding.textScannedCount.text = scanBarcodeArrayList.size.toString()

        binding.editTrackingNumber.setText("")
        inputMethodManager.hideSoftInputFromWindow(binding.editTrackingNumber.windowToken, 0)
    }


    private fun checkDBDuplicate(contrNo: String, invoiceNo: String): Boolean {
        val selectQuery = ("SELECT  partner_ref_no, invoice_no, stat, rcv_nm, sender_nm "
                + " FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no= '"
                + invoiceNo + "'" + " and contr_no= '" + contrNo + "'")
        val cs = DatabaseHelper.getInstance()[selectQuery]
        return 0 < cs.count
    }

    private fun getCnrRequester(invoiceNo: String): String {
        var requester = ""
        val barcodeNo = invoiceNo.trim().uppercase()
        val selectQuery =
            "SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no = '" + barcodeNo + "'"
        val cursor = DatabaseHelper.getInstance()[selectQuery]
        if (0 < cursor.count) {
            if (cursor.moveToFirst()) {
                do {
                    requester = cursor.getString(cursor.getColumnIndex("req_nm"))
                } while (cursor.moveToNext())
            }
        }
        return requester
    }

    @SuppressLint("SimpleDateFormat")
    private fun insertCnRData(data: CnRPickupResult): String {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val regDataString = dateFormat.format(Date())

        try {

            val contentVal = ContentValues()
            contentVal.put("contr_no", data.contrNo)
            contentVal.put("partner_ref_no", data.partnerRefNo)
            contentVal.put("invoice_no", data.partnerRefNo)
            contentVal.put("stat", data.stat)
            contentVal.put("tel_no", data.telNo)
            contentVal.put("hp_no", data.hpNo)
            contentVal.put("zip_code", data.zipCode)
            contentVal.put("address", data.address)
            contentVal.put("route", data.route)
            contentVal.put("type", StatueType.TYPE_PICKUP)
            contentVal.put("desired_date", data.pickupHopeDay)
            contentVal.put("req_qty", data.qty)
            contentVal.put("req_nm", data.reqName)
            contentVal.put("rcv_request", data.delMemo)
            contentVal.put("sender_nm", "")
            contentVal.put("punchOut_stat", "N")
            contentVal.put("reg_id", "")
            contentVal.put("reg_dt", regDataString)
            contentVal.put("fail_reason", data.failReason)
            contentVal.put("secret_no_type", "")
            contentVal.put("secret_no", "")
            contentVal.put("lat", "0")
            contentVal.put("lng", "0")
            contentVal.put("state", "")
            contentVal.put("city", "")
            contentVal.put("street", "")
            DatabaseHelper.getInstance()
                .insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal)
        } catch (ignored: Exception) {
        }

        return data.reqName
    }

    // 하단 버튼 클릭 이벤트
    // NOTIFICATION.  Confirm my delivery order / Change Delivery Driver
    fun onUpdateButtonClick() {

        if (scanBarcodeArrayList.isEmpty()) {
            val toast = Toast.makeText(
                this@CaptureActivity1,
                R.string.msg_tracking_number_manually,
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
            return
        }

        if (!NetworkUtil.isNetworkAvailable(this@CaptureActivity1)) {
            warningDialog(resources.getString(R.string.msg_network_connect_error))
            return
        }

        if (MemoryStatus.availableInternalMemorySize != MemoryStatus.ERROR.toLong()
            && MemoryStatus.availableInternalMemorySize < MemoryStatus.PRESENT_BYTE
        ) {
            warningDialog(resources.getString(R.string.msg_disk_size_error))
            return
        }

        if (mScanType == CaptureType.CONFIRM_MY_DELIVERY_ORDER) {
            FirebaseEvent.clickEvent(this, TAG, "SetShippingStatDpc3out api call")

            progressBar.visibility = View.VISIBLE

            var stringBuilder = StringBuilder()
            for (item in scanBarcodeArrayList) {
                if (!TextUtils.isEmpty(item.barcode)) {
                    if (stringBuilder.isEmpty()) {
                        stringBuilder = stringBuilder.append(item.barcode)
                    } else {
                        stringBuilder.append(",").append(item.barcode)
                    }
                }
            }

            RetrofitClient.instanceDynamic().requestSetShippingStatDpc3out(stringBuilder.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    progressBar.visibility = View.GONE
                    if (it.resultCode == 0) {

                        onResetButtonClick()

                        val list: ArrayList<CaptureData> = Gson().fromJson(
                            it.resultObject,
                            object :
                                TypeToken<ArrayList<CaptureData>>() {}.type
                        )

                        for (item in list) {
                            if (!TextUtils.isEmpty(item.partner_ref_no.trim())) {
                                DataUtil.insertDriverAssignInfo(item)
                            }
                        }

                        resultDialog(
                            resources.getString(R.string.text_driver_assign_result),
                            it.resultMsg
                        )
                    } else {

                        resultDialog(
                            resources.getString(R.string.text_driver_assign_result),
                            resources.getString(R.string.text_fail_update)
                        )
                    }
                }) {

                    progressBar.visibility = View.GONE
                    resultDialog(
                        resources.getString(R.string.text_driver_assign_result),
                        resources.getString(R.string.text_fail_update)
                    )
                }

        } else if (mScanType == CaptureType.CHANGE_DELIVERY_DRIVER) {
            FirebaseEvent.clickEvent(this, TAG, "SetChangeDeliveryDriver api call")

            progressBar.visibility = View.VISIBLE

            var latitude = 0.0
            var longitude = 0.0
            gpsTrackerManager?.let {
                latitude = it.latitude
                longitude = it.longitude
            }

            var stringBuilder = StringBuilder()
            for (item in scanBarcodeArrayList) {
                if (!TextUtils.isEmpty(item.contrNo)) {
                    if (stringBuilder.isEmpty()) {
                        stringBuilder = stringBuilder.append(item.contrNo)
                    } else {
                        stringBuilder.append(",").append(item.contrNo)
                    }
                }
            }

            val network = NetworkUtil.getNetworkType(this@CaptureActivity1)

            RetrofitClient.instanceDynamic().requestSetChangeDeliveryDriver(
                stringBuilder.toString(),
                network,
                latitude.toString(),
                longitude.toString()
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    progressBar.visibility = View.GONE

                    if (it.resultCode == 0) {

                        onResetButtonClick()
                        val list: ArrayList<CaptureData> = Gson().fromJson(
                            it.resultObject,
                            object :
                                TypeToken<ArrayList<CaptureData>>() {}.type
                        )
                        for (item in list) {
                            if (!TextUtils.isEmpty(item.partner_ref_no)) {
                                val successInsert = DataUtil.insertDriverAssignInfo(item)

                                if (successInsert) {

                                    RetrofitClient.instanceDynamic()
                                        .requestSetQdriverMessageChangeQdriver(item.invoice_no)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                        }) {
                                        }
                                }
                            }
                        }

                        resultDialog(
                            resources.getString(R.string.text_driver_assign_result),
                            it.resultMsg
                        )
                    } else {
                        resultDialog(
                            resources.getString(R.string.text_driver_assign_result),
                            resources.getString(R.string.text_fail_update)
                        )
                    }
                }) {
                    progressBar.visibility = View.GONE
                    resultDialog(
                        resources.getString(R.string.text_driver_assign_result),
                        resources.getString(R.string.text_fail_update)
                    )
                }
        }
    }

    // NOTIFICATION.  Scan - Delivery Done
    private fun onConfirmButtonClick() {
        if (scanBarcodeArrayList.isEmpty()) {
            val toast = Toast.makeText(
                this@CaptureActivity1,
                R.string.msg_tracking_number_manually,
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
            return
        }

        var receiverName = ""
        var diffReceiverName = false
        val deliveryBarcodeList = ArrayList<BarcodeData>()

        for (barcodeData in scanBarcodeArrayList) {

            if (barcodeData.state == "SUCCESS") {
                val name = getDeliveryReceiver(barcodeData.barcode)
                try {
                    // 수취인성명이 틀린경우
                    if (receiverName.isNotEmpty()) {
                        if (receiverName.toUpperCase(Locale.ROOT) != name.toUpperCase(Locale.ROOT)) {
                            diffReceiverName = true
                        }
                    }

                } catch (e: Exception) {
                    diffReceiverName = true
                }

                receiverName = name
                deliveryBarcodeList.add(barcodeData)
            }
        }

        // 받는사람이 틀리다면 에러 메세지
        if (diffReceiverName) {
            val toast = Toast.makeText(
                this@CaptureActivity1,
                R.string.msg_different_recipient_address,
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
            return
        }

        if (deliveryBarcodeList.isNotEmpty()) {

            val intent = Intent(this, DeliveryDoneActivity2::class.java)
            intent.putExtra("data", deliveryBarcodeList)
            finishLauncher.launch(intent)

        } else {
            val toast = Toast.makeText(
                this@CaptureActivity1,
                R.string.msg_tracking_number_manually,
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
        }
    }

    // NOTIFICATION.  Pickup (CnR / Scan All / Add Scan / Take Back / Outlet)
    private fun onNextButtonClick() {

        if (mScanType == CaptureType.OUTLET_PICKUP_SCAN) {
            var isScanned = false
            for (i in resultData!!.trackingNoList.indices) {
                if (resultData!!.trackingNoList[i].isScanned) {
                    isScanned = true
                }
            }

            if (!isScanned) {
                val toast = Toast.makeText(
                    this@CaptureActivity1,
                    R.string.msg_tracking_number_manually,
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
                return
            }
        } else {

            if (scanBarcodeArrayList.isEmpty()) {
                val toast = Toast.makeText(
                    this@CaptureActivity1,
                    R.string.msg_tracking_number_manually,
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
                return
            }
        }

        val scannedQty = scanBarcodeArrayList.size.toString()
        var scannedList = StringBuilder()

        for (item in scanBarcodeArrayList) {
            if (!TextUtils.isEmpty(item.barcode)) {
                if (scannedList.isEmpty()) {
                    scannedList = scannedList.append(item.barcode)
                } else {
                    scannedList.append(",").append(item.barcode)
                }
            }
        }

        when (mScanType) {
            CaptureType.PICKUP_CNR -> {
                val intent = Intent(this, CnRPickupDoneActivity::class.java)
                intent.putExtra("senderName", pickupCNRRequester)
                intent.putExtra("scannedList", scannedList.toString())
                intent.putExtra("scannedQty", scannedQty)
                resetFinishLauncher.launch(intent)
            }

            CaptureType.PICKUP_SCAN_ALL -> {
                val intent = Intent(this, PickupDoneActivity::class.java)
                intent.putExtra("pickupNo", pickupNo)
                intent.putExtra("applicant", applicant)
                intent.putExtra("scannedList", scannedList.toString())
                intent.putExtra("scannedQty", scannedQty)
                startActivity(intent)
                finish()
            }

            CaptureType.PICKUP_ADD_SCAN -> {
                val intent = Intent(this, PickupAddScanActivity::class.java)
                intent.putExtra("pickupNo", pickupNo)
                intent.putExtra("applicant", applicant)
                intent.putExtra("scannedList", scannedList.toString())
                intent.putExtra("scannedQty", scannedQty)
                resetResultFinishLauncher.launch(intent)
            }

            CaptureType.PICKUP_TAKE_BACK -> {
                val intent = Intent(this, PickupTakeBackActivity::class.java)
                intent.putExtra("pickupNo", pickupNo)
                intent.putExtra("applicant", applicant)
                intent.putExtra("scannedList", scannedList.toString())
                intent.putExtra("totalQty", qty)
                intent.putExtra("takeBackQty", scannedQty)
                resetResultFinishLauncher.launch(intent)
            }

            CaptureType.OUTLET_PICKUP_SCAN -> {

                var outletScannedQty = 0

                var i = 0
                while (i < resultData!!.trackingNoList.size) {
                    if (resultData!!.trackingNoList[i].isScanned) {
                        outletScannedQty++
                    }
                    i++
                }

                val outletScannedList = StringBuilder()
                var j = 0
                while (j < resultData!!.trackingNoList.size) {
                    if (resultData!!.trackingNoList[j].isScanned) {
                        if (outletScannedList.toString() != "") {
                            outletScannedList.append(",")
                        }
                        outletScannedList.append(resultData!!.trackingNoList[j].trackingNo)
                    }
                    j++
                }
                Log.e(TAG, "Outlet Pickup Scanned List : $outletScannedList  $scannedQty")

                val intent = Intent(this, OutletPickupStep3Activity::class.java)
                intent.putExtra("title", title)
                intent.putExtra("pickupNo", pickupNo)
                intent.putExtra("applicant", applicant)
                intent.putExtra("qty", qty)
                intent.putExtra("route", route)
                intent.putExtra("scannedQty", outletScannedQty)
                intent.putExtra("tracking_data", resultData)
                intent.putExtra("scannedList", outletScannedList.toString())
                startActivity(intent)
                finish()
            }
        }
    }

    // NOTIFICATION.  Reset
    private fun onResetButtonClick() {

        if (scanBarcodeArrayList.isNotEmpty()) {

            scanBarcodeArrayList.clear()
            adapter.notifyDataSetChanged()

            binding.textScannedCount.text = "0"

            if (mScanType == CaptureType.OUTLET_PICKUP_SCAN) {

                val listItem = resultData!!.trackingNoList
                for (i in listItem.indices) {
                    val data = BarcodeData()
                    data.barcode = listItem[i].trackingNo
                    data.state = "FAIL"
                    scanBarcodeArrayList.add(i, data)
                }
                adapter.notifyDataSetChanged()
            }
        }

        checkedBarcodeList.clear()


    }

    private fun getDeliveryReceiver(barcodeNo: String?): String {
        var name = ""
        val cursor =
            DatabaseHelper.getInstance()["SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                    " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE"]
        if (cursor.moveToFirst()) {
            try {
                name = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"))
            } catch (e: java.lang.Exception) {

            }
        }
        cursor.close()
        return name
    }

    @Synchronized
    public override fun onPause() {
        super.onPause()
        cameraManager.onPause()
        if (mIsScanDeviceListActivityRun || KTSyncData.bIsRunning) {
            mIsScanDeviceListActivityRun = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cameraManager.onSaveInstanceState(outState)
    }

    public override fun onDestroy() {
        super.onDestroy()

        cameraManager.onDestroy()
        onResetButtonClick()
        DataUtil.stopGPSManager(gpsTrackerManager)

        beepManager.destroy()
        beepManagerError.destroy()
        beepManagerDuple.destroy()

        // Stop the Bluetooth chat services
        if (KTSyncData.mChatService != null) KTSyncData.mChatService.stop()
        KTSyncData.mChatService = null
        KTSyncData.bIsRunning = false
    }

    //2016-09-12 eylee  self-collection nq 인지 아닌지 판단하는
    private fun isNonQ10QFSOrderForSelfCollection(barcodeNo: String): Boolean {
        var isNQ = false
        val len = barcodeNo.length
        val scanNoLast = barcodeNo.substring(len - 2).toUpperCase(Locale.ROOT)
        if (scanNoLast == "NQ") {
            isNQ = true
        }
        // return 해서 isNonQ10QFSOrder 여기에 setting 하기
        return isNQ
    }

    /*
     * update delivery set stat = @stat , chg_id = localStorage.getItem('opId')
     * , chg_dt = datetime('now') where invoice_no = @invoice_no COLLATE NOCASE
     * and punchOut_stat <> 'S' and reg_id = localStorage.getItem('opId')
     */
    private fun updateInvoiceNO(invoiceNo: String) {
        var updateCount = 0
        if (mScanType == CaptureType.PICKUP_SCAN_ALL
            || mScanType == CaptureType.PICKUP_ADD_SCAN
            || mScanType == CaptureType.PICKUP_TAKE_BACK
            || mScanType == CaptureType.OUTLET_PICKUP_SCAN
        ) {
            updateCount = 1

        } else if (mScanType == CaptureType.DELIVERY_DONE) {
            // 복수건 배달완료 시점에서는 아무것도 안함 사인전 jmkang 2013-05-08
            val contentVal = ContentValues()
            contentVal.put("reg_id", Preferences.userId) // 해당 배송번호를 가지고 자신의아이디만 없데이트
            updateCount = DatabaseHelper.getInstance().update(
                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                contentVal,
                "invoice_no=? COLLATE NOCASE " + "and punchOut_stat <> 'S' " + "and reg_id = ?",
                arrayOf(invoiceNo, Preferences.userId)
            )
        } else if (mScanType == CaptureType.SELF_COLLECTION) {
            if (isInvoiceCodeRule(invoiceNo)) {
                updateCount = 1
            }
        }

        var message = String.format(" [ %s ] ", title)
        val result: String
        val inputBarcode = scanBarcodeArrayList[scanBarcodeArrayList.size - 1].barcode

        if (updateCount < 1) {
            message += resources.getString(R.string.text_not_assigned)
            result = "FAIL"
        } else {
            message += resources.getString(R.string.text_success)
            result = "SUCCESS"
        }

        if (mScanType != CaptureType.OUTLET_PICKUP_SCAN) {
            val data = BarcodeData()
            data.barcode = inputBarcode
            data.state = result
            scanBarcodeArrayList[scanBarcodeArrayList.size - 1] = data
            adapter.notifyDataSetChanged()
        }

        // 교체 후 Adapter.notifyDataSetChanged() 메서드로 listview  변경 add comment by eylee 2016-09-08
        if (updateCount < 1) { // 실패일때만 보여준다.

            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {

                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        200L,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

            } else {
                vibrator.vibrate(200L)
            }

            val toast = Toast.makeText(this@CaptureActivity1, message, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 10)
            toast.show()
        }
    }

    // NOTIFICATION.  SELF_COLLECTION
    /*
     * 송장번호 규칙에 맞는지 체크한후 프리뷰영역에 이미지를 보여준다.
     * modified : 2016-09-09 eylee self-collection 복수 건 처리 add
     */
    private fun onCaptureConfirmButtonClick() {
        if (scanBarcodeArrayList.isEmpty()) {
            val toast =
                Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
            return
        }

        if (!isInvoiceCodeRule(scanBarcodeArrayList[0].barcode)) {
            val toast = Toast.makeText(
                this@CaptureActivity1,
                resources.getString(R.string.msg_invalid_scan),
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
            return
        }

        // SELF_COLLECTION            //복수건 가져다가 self-collection by 2016-09-09
        // 넘기는 데이터 재정의 스캔성공된 것들만 보낸다.
        val newBarcodeNoList = ArrayList<BarcodeData>()

        for (data in scanBarcodeArrayList) {
            if (data.state == "FAIL") {
                val toast = Toast.makeText(this, R.string.msg_invalid_scan, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
                return
            } else {
                newBarcodeNoList.add(data)
            }
        }

        if (0 < newBarcodeNoList.size) {
            val intent = Intent(this, SelfCollectionDoneActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("data", newBarcodeNoList)
            intent.putExtra("nonq10qfs", isNonQ10QFSOrder.toString()) //09-12 add isNonQ10QFSOrder
            resetFinishLauncher.launch(intent)
        } else {
            val toast =
                Toast.makeText(this, R.string.msg_tracking_number_manually, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
        }
    }

    override fun onTorchOn() {}
    override fun onTorchOff() {}


    private val bluetoothLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.e(TAG, "bluetoothLauncher ${it.resultCode} ")

        if (it.resultCode == RESULT_OK) {
            setupChat()
        } else {
            Toast.makeText(
                this@CaptureActivity1,
                R.string.msg_bluetooth_enabled,
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private val deviceLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        //  Bluetooth Device 연결
        if (it.resultCode == RESULT_OK) {

            val address = it.data?.extras?.getString(DeviceListActivity1.EXTRA_DEVICE_ADDRESS)
            connectedDevice = mBluetoothAdapter!!.getRemoteDevice(address)
            KTSyncData.mChatService.connect(connectedDevice)
        }
    }

    private val finishLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.e(TAG, "finishLauncher ${it.resultCode} ")

        if (it.resultCode == RESULT_OK) {
            finish()
        }
    }

    private val resetFinishLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.e(TAG, "resetFinishLauncher ${it.resultCode} ")

        onResetButtonClick()
        if (it.resultCode == RESULT_OK) {
            finish()
        }
    }

    private val resetResultFinishLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.e(TAG, "resetResultFinishLauncher ${it.resultCode} ")

            onResetButtonClick()
            if (it.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "onActivityResult $requestCode / $resultCode")

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                    Log.e(TAG, "onActivityResult  PERMISSIONS_GRANTED")
                    isPermissionTrue = true
                }
            }
        }
    }

    private fun initBluetoothDevice() {

        // If the adapter is null, then Bluetooth is not supported          // Bluetooth 지원하지 않음
        if (mBluetoothAdapter == null && !BuildConfig.DEBUG) {
            Toast.makeText(
                this@CaptureActivity1,
                resources.getString(R.string.msg_bluetooth_not_supported),
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        KTSyncData.mKScan = KScan(this, bluetoothHandler)
        for (i in 0..9) {
            KTSyncData.SerialNumber[i] = '0'.toByte()
            KTSyncData.FWVersion[i] = '0'.toByte()
        }

        var temp: ByteArray
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        temp = preferenceManager.getString("Data Delimiter", "4")!!.toByteArray()
        KTSyncData.DataDelimiter = temp[0] - '0'.toByte()
        temp = preferenceManager.getString("Record Delimiter", "1")!!.toByteArray()
        KTSyncData.RecordDelimiter = temp[0] - '0'.toByte()
    }

    public override fun onStart() {
        super.onStart()
        if (mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled) {
            if (KTSyncData.mChatService == null) setupChat()
        }
    }

    private fun setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        KTSyncData.mChatService = BluetoothChatService(this, bluetoothHandler)
    }

    // Bluetooth
    @SuppressLint("HandlerLeak")
    private val bluetoothHandler: Handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            Log.i(bluetoothTAG, "handleMessage " + msg.what)
            when (msg.what) {
                BluetoothChatService.MESSAGE_STATE_CHANGE -> {
                    Log.i(bluetoothTAG, "MESSAGE_STATE_CHANGE: " + msg.arg1)
                    when (msg.arg1) {
                        BluetoothChatService.STATE_CONNECTED -> {
                            binding.textBluetoothConnectState.text =
                                resources.getString(R.string.text_connected)
                            binding.textBluetoothDeviceName.visibility = View.VISIBLE
                            binding.textBluetoothDeviceName.text = "($connectedDeviceName)"
                            binding.btnBluetoothDeviceFind.visibility = View.GONE
                            removeCallbacks(mUpdateTimeTask)
                            KTSyncData.mKScan.DeviceConnected(true)
                        }
                        BluetoothChatService.STATE_CONNECTING -> {
                            binding.textBluetoothConnectState.text =
                                resources.getString(R.string.text_connecting)
                            binding.textBluetoothDeviceName.visibility = View.GONE
                            binding.btnBluetoothDeviceFind.visibility = View.GONE
                        }
                        BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                            binding.textBluetoothConnectState.text =
                                resources.getString(R.string.text_disconnected)
                            binding.textBluetoothDeviceName.visibility = View.GONE
                            binding.btnBluetoothDeviceFind.visibility = View.VISIBLE
                        }
                        BluetoothChatService.STATE_LOST -> {
                            binding.textBluetoothConnectState.text =
                                resources.getString(R.string.text_disconnected)
                            binding.textBluetoothDeviceName.visibility = View.GONE
                            binding.btnBluetoothDeviceFind.visibility = View.VISIBLE
                            KTSyncData.bIsConnected = false
                            postDelayed(mUpdateTimeTask, 2000)
                        }
                        BluetoothChatService.STATE_FAILED -> {
                            binding.textBluetoothConnectState.text =
                                resources.getString(R.string.text_disconnected)
                            binding.textBluetoothDeviceName.visibility = View.GONE
                            binding.btnBluetoothDeviceFind.visibility = View.VISIBLE
                            postDelayed(mUpdateTimeTask, 5000)
                        }
                    }
                }
                BluetoothChatService.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    var i = 0
                    while (i < msg.arg1) {
                        KTSyncData.mKScan.HandleInputData(readBuf[i])
                        i++
                    }
                }
                BluetoothChatService.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    connectedDeviceName = msg.data.getString(BluetoothChatService.DEVICE_NAME)
                    Toast.makeText(
                        this@CaptureActivity1,
                        resources.getString(R.string.text_connected_to) + connectedDeviceName,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                BluetoothChatService.MESSAGE_TOAST -> {
                    Toast.makeText(
                        this@CaptureActivity1,
                        msg.data.getString(BluetoothChatService.TOAST),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                KScan.MESSAGE_DISPLAY -> {
                    val displayBuf = msg.obj as ByteArray
                    val displayMessage = String(displayBuf, 0, msg.arg1)
                    onBluetoothBarcodeAdd(displayMessage)
                    KTSyncData.bIsSyncFinished = true
                }
                KScan.MESSAGE_SEND -> {
                    val sendBuf = msg.obj as ByteArray
                    KTSyncData.mChatService.write(sendBuf)
                }
            }
        }
    }
}