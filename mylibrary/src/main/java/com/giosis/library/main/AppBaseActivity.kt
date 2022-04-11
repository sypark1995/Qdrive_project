package com.giosis.library.main

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import com.giosis.library.R
import com.giosis.library.UploadData
import com.giosis.library.barcodescanner.CaptureActivity1
import com.giosis.library.database.DatabaseHelper
import com.giosis.library.databinding.ActivityMainBinding
import com.giosis.library.databinding.ViewNavListHeaderBinding
import com.giosis.library.gps.FusedProviderService
import com.giosis.library.gps.GPSTrackerManager
import com.giosis.library.gps.LocationManagerService
import com.giosis.library.list.ListActivity
import com.giosis.library.main.route.TodayMyRouteActivity
import com.giosis.library.main.submenu.OutletOrderStatusActivity
import com.giosis.library.main.submenu.RpcListActivity
import com.giosis.library.pickup.CreatePickupOrderActivity
import com.giosis.library.server.APIModel
import com.giosis.library.server.RetrofitClient
import com.giosis.library.setting.SettingActivity
import com.giosis.library.setting.bluetooth.BluetoothDeviceData
import com.giosis.library.util.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

open class AppBaseActivity : CommonActivity() {
    var TAG = "AppBaseActivity"

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val headerBinding by lazy {
        ViewNavListHeaderBinding.inflate(LayoutInflater.from(this), null, false)
    }

    private var titleString: String = ""

    var isPermissionTrue = false

    private val PERMISSIONS = arrayOf(
        PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
        PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE
    )

    private val PERMISSION_REQUEST_CODE = 1000

    // Only SG
    var customerMessageCount = 0
    var adminMessageCount = 0

    var gpsTrackerManager: GPSTrackerManager? = null
    var gpsEnable = false

    var latitude = 0.0
    var longitude = 0.0

    var uploadFailedCount = "0"

    fun setTopTitle(title: String) {
        titleString = title
        binding.appBar.textTopTitle.text = titleString
    }

    fun leftMenuGone() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        binding.drawerLayout.setScrimColor(Color.parseColor("#4D000000"))
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBar.toolbar,
            R.string.button_open,
            R.string.button_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

//        binding.navList.addHeaderView(headerBinding.root)
//        val adapter2 = NavListViewAdapter2()
//        binding.navList.setAdapter(adapter2)

        //
        binding.layoutBottomBarHome.setOnClickListener {

            if (!titleString.contains(getString(R.string.navi_home))) {
                val intent = Intent(it.context, AppBaseActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.layoutBottomBarScan.setOnClickListener {
            val intent = Intent(it.context, ScanActivity::class.java)
            startActivity(intent)
        }

        binding.layoutBottomBarList.setOnClickListener {
            val intent = Intent(it.context, ListActivity::class.java)
            startActivity(intent)
        }

        binding.layoutBottomBarSetting.setOnClickListener {
            val intent = Intent(it.context, SettingActivity::class.java)
            startActivity(intent)
        }

        val leftViewAdapter = LeftViewAdapter()
        binding.navList.adapter = leftViewAdapter

        val leftSubList: ArrayList<String>
        val leftSubList2: ArrayList<String>

        if (Preferences.outletDriver == "Y") {
            leftSubList = ArrayList(
                listOf(
                    getString(R.string.text_start_delivery_for_outlet),
                    getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup),
                    getString(R.string.navi_sub_self)
                )
            )
            leftSubList2 = ArrayList(
                listOf(
                    getString(R.string.navi_sub_in_progress),
                    getString(R.string.navi_sub_upload_fail),
                    getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed),
                    getString(R.string.text_outlet_order_status)
                )
            )
        } else {
            leftSubList = ArrayList(
                listOf(
                    getString(R.string.navi_sub_confirm_delivery),
                    getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup),
                    getString(R.string.navi_sub_self)
                )
            )
            leftSubList2 = ArrayList(
                listOf(
                    getString(R.string.navi_sub_in_progress),
                    getString(R.string.navi_sub_upload_fail),
                    getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed)
                )
            )
        }

        leftViewAdapter.addItem(
            R.drawable.side_icon_home_selector,
            getString(R.string.navi_home),
            null,
        )

        leftViewAdapter.addItem(
            R.drawable.side_icon_home_selector,
            getString(R.string.navi_home),
            null,
        )

        leftViewAdapter.addItem(
            R.drawable.memu_scan_selector,
            getString(R.string.navi_scan),
            leftSubList,
        )

        leftViewAdapter.addItem(
            R.drawable.menu_list_selector,
            getString(R.string.navi_list),
            leftSubList2,
        )

        leftViewAdapter.addItem(
            R.drawable.side_icon_statistics_selector,
            getString(R.string.navi_statistics),
            null,
        )

        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y") {
            leftViewAdapter.addItem(
                R.drawable.icon_pickup_order,
                getString(R.string.text_create_pickup_order),
                null,
            )
        }

        leftViewAdapter.addItem(
            R.drawable.side_icon_settings_selector,
            getString(R.string.navi_setting),
            null,
        )
        //

        QDataUtil.setCustomUserAgent(this@AppBaseActivity)
        DatabaseHelper.getInstance().dbPath

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )


        // FCM token
        saveServerFCMToken()

        setTopTitle(resources.getString(R.string.navi_home))

        // 7ETB, FLTB push 받고 온 경우 확인
        var downloadApi = intent.getStringExtra("DOWNLOAD")
        if (downloadApi == null) {
            downloadApi = "N"
        }

        if (DownloadCheck()!! || downloadApi == "Y") {
            download()
        }

        binding.mainView.layoutHomeListCount.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
        binding.mainView.layoutHomeDownload.setOnClickListener {
            download()
        }
        binding.mainView.layoutHomeUpload.setOnClickListener {
            Upload()
        }

//        binding.mainView.textHomeDriverName.text = Preferences.userName
//        binding.mainView.textHomeDriverOffice.text = Preferences.officeName
        binding.mainView.imgHomeDriverProfile.setImageBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.qdrive_img_default
            )
        )
        binding.mainView.imgHomeDriverProfile.background = ShapeDrawable(OvalShape())
        binding.mainView.imgHomeDriverProfile.clipToOutline = true

        if (Preferences.pickupDriver == "Y" && Preferences.userNation == "SG") {
            binding.mainView.btnHomeCreatePickupOrder.visibility = View.VISIBLE
        } else {
            binding.mainView.btnHomeCreatePickupOrder.visibility = View.GONE
        }

        if (Preferences.outletDriver == "Y") {
            binding.mainView.btnHomeConfirmMyDeliveryOrder.text =
                resources.getString(R.string.text_start_delivery_for_outlet)
            binding.mainView.btnHomeOutletOrderStatus.visibility = View.VISIBLE

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, DisplayUtil.dpTopx(this, 15f), 0, 0)
            binding.mainView.btnHomeChangeDeliveryDriver.layoutParams = lp
        } else {
            binding.mainView.btnHomeConfirmMyDeliveryOrder.text =
                resources.getString(R.string.button_confirm_my_delivery_order)
            binding.mainView.btnHomeOutletOrderStatus.visibility = View.GONE

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, DisplayUtil.dpTopx(this, 15f), 0, DisplayUtil.dpTopx(this, 30f))

            if (binding.mainView.btnHomeCreatePickupOrder.visibility == View.VISIBLE) {

                if (binding.mainView.btnHomeTodayMyRoute.visibility == View.VISIBLE) {

                    binding.mainView.btnHomeTodayMyRoute.layoutParams = lp
                } else {

                    binding.mainView.btnHomeCreatePickupOrder.layoutParams = lp
                }
            } else {

                if (binding.mainView.btnHomeTodayMyRoute.visibility == View.VISIBLE) {

                    binding.mainView.btnHomeTodayMyRoute.layoutParams = lp
                } else {

                    binding.mainView.btnHomeChangeDeliveryDriver.layoutParams = lp
                }
            }
        }

        binding.mainView.btnHomeConfirmMyDeliveryOrder.setOnClickListener {

            val intent = Intent(this, CaptureActivity1::class.java)
            intent.putExtra("title", resources.getString(R.string.text_title_driver_assign))
            intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
            startActivity(intent)
        }
        binding.mainView.btnHomeAssignPickupDriver.setOnClickListener {

            val intent = Intent(this, RpcListActivity::class.java)
            startActivity(intent)
        }
        binding.mainView.btnHomeChangeDeliveryDriver.setOnClickListener {

            if (gpsEnable && gpsTrackerManager != null) {

                val intent = Intent(this, CaptureActivity1::class.java)
                intent.putExtra(
                    "title",
                    resources.getString(R.string.button_change_delivery_driver)
                )
                intent.putExtra("type", BarcodeType.CHANGE_DELIVERY_DRIVER)
                startActivity(intent)
            } else {

                DataUtil.enableLocationSettings(this)
            }
        }
        binding.mainView.btnHomeOutletOrderStatus.setOnClickListener {

            val intent = Intent(this, OutletOrderStatusActivity::class.java)
            startActivity(intent)
        }
        binding.mainView.btnHomeCreatePickupOrder.setOnClickListener {

            val intent = Intent(this, CreatePickupOrderActivity::class.java)
            startActivity(intent)
        }
        binding.mainView.btnHomeTodayMyRoute.setOnClickListener {

            val intent = Intent(this, TodayMyRouteActivity::class.java)
            startActivity(intent)
        }

        val checker = PermissionChecker(this)

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(*PERMISSIONS)) {

            isPermissionTrue = false
            PermissionActivity.startActivityForResult(
                this,
                PERMISSION_REQUEST_CODE,
                *PERMISSIONS
            )
            overridePendingTransition(0, 0)
        } else {

            isPermissionTrue = true
            gpsTrackerServiceStart()
        }
    }

    companion object {
        enum class MENU_LIST {

        }


    }

    fun setMessageCount(customer_count: Int, admin_count: Int) {
        customerMessageCount = customer_count
        adminMessageCount = admin_count
        val count = customer_count + admin_count

        headerBinding.textMessageCount.visibility = View.VISIBLE
        headerBinding.textMessageCount.text = count.toString()
    }

    fun goneMessageCount() {
        headerBinding.textMessageCount.visibility = View.GONE
    }

    fun setNaviHeader(name: String?, office: String?) {

        headerBinding.textNavHeaderDriverName.text = name
        headerBinding.textNavHeaderDriverOffice.text = office
    }

    override fun onResume() {
        super.onResume()

        setNaviHeader(Preferences.userName, Preferences.officeName)
        binding.mainView.textHomeDriverName.text = Preferences.userName
        binding.mainView.textHomeDriverOffice.text = Preferences.officeName

        DataUtil.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (isPermissionTrue) {

            gpsTrackerManager = GPSTrackerManager(this)
            gpsEnable = gpsTrackerManager!!.enableGPSSetting()

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager!!.gpsTrackerStart()
            } else {

                DataUtil.enableLocationSettings(this)
            }
        }

        initMessageCount()


        // TODO_TEST  badge
//        MyApplication myApp = (MyApplication) getApplicationContext();
//        myApp.setBadgeCnt(0);
        setBadge(applicationContext, 0)

        if (Preferences.userId == "") {
            Toast.makeText(
                this,
                resources.getString(R.string.msg_qdrive_auto_logout),
                Toast.LENGTH_SHORT
            ).show()

            try {
                if (Preferences.userNation.equals("SG")) {

                    val intent = Intent(
                        this,
                        Class.forName("com.giosis.util.qdrive.singapore.LoginActivity")
                    )

                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                } else {

                    val intent = Intent(
                        this,
                        Class.forName("com.giosis.util.qdrive.international.LoginActivity")
                    )

                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            } catch (e: Exception) {

                Log.e("Exception", "  Exception : $e")
                Toast.makeText(this, "Exception : $e", Toast.LENGTH_SHORT).show()
            }
        }

        MainActivityServer.getLocalCount1(this)
    }

    private fun setBadge(context: Context, count: Int) {
        val launcherClassName = getLauncherClassName(context) ?: return

        val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
        intent.putExtra("badge_count", count)
        intent.putExtra("badge_count_package_name", context.packageName)
        intent.putExtra("badge_count_class_name", launcherClassName)
        context.sendBroadcast(intent)
    }

    private fun getLauncherClassName(context: Context): String? {
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfos) {
            val pkgName = resolveInfo.activityInfo.applicationInfo.packageName
            if (pkgName.equals(context.packageName, ignoreCase = true)) {
                return resolveInfo.activityInfo.name
            }
        }
        return null
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                isPermissionTrue = true
                gpsTrackerServiceStart()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun DownloadCheck(): Boolean? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val today = dateFormat.format(Date())

        // 날짜기준 UTC
        val selectQuery = (" select  datetime(max(reg_dt), 'localtime') as PI_Time, "
                + " strftime('%Y-%m-%d', max(reg_dt)) as PI_Date "
                + " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                + " where reg_id= '" + Preferences.userId + "'"
                + " and strftime('%Y-%m-%d', reg_dt) = date('now')")

        try {

            val cs: Cursor = DatabaseHelper.getInstance()[selectQuery]
            if (cs.moveToFirst()) {

                val PunchInDate = cs.getString(cs.getColumnIndex("PI_Date"))
                if (PunchInDate != null && PunchInDate != "") {
                    //오늘 날짜가 있으면 다운로드 하지 않음
                    if (today == PunchInDate) {
                        return false
                    }
                }
            }
        } catch (e: Exception) {

            Log.e("Exception", "$TAG - DownloadCheck Exception : $e")
        }
        return true
    }

    private fun Upload() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT)
                .show()
            return
        }

        // TODO_kjyoo MainActivityServer.INSTANCE.upload() 로 변경중....
        val songjanglist = ArrayList<UploadData>()
        // 업로드 대상건 로컬 DB 조회
        val selectQuery = "select invoice_no" +
                " , stat " +
                " , ifnull(rcv_type, '')  as rcv_type" +
                " , ifnull(fail_reason, '')  as fail_reason" +
                " , ifnull(driver_memo, '') as driver_memo" +
                " , ifnull(real_qty, '') as real_qty" +
                " , ifnull(retry_dt , '') as retry_dt" +
                " , type " +
                " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                " where reg_id= '" + Preferences.userId + "'" +
                " and punchOut_stat <> 'S' and chg_dt is not null"
        val cs: Cursor = DatabaseHelper.getInstance().get(selectQuery)

        if (cs.moveToFirst()) {
            do {
                val data = UploadData()
                data.noSongjang = cs.getString(cs.getColumnIndex("invoice_no"))
                data.stat = cs.getString(cs.getColumnIndex("stat"))
                data.receiveType = cs.getString(cs.getColumnIndex("rcv_type"))
                data.failReason = cs.getString(cs.getColumnIndex("fail_reason"))
                data.driverMemo = cs.getString(cs.getColumnIndex("driver_memo"))
                data.realQty = cs.getString(cs.getColumnIndex("real_qty"))
                data.retryDay = cs.getString(cs.getColumnIndex("retry_dt"))
                data.type = cs.getString(cs.getColumnIndex("type"))
                songjanglist.add(data)
            } while (cs.moveToNext())
        }

        if (gpsEnable && gpsTrackerManager != null) {
            latitude = gpsTrackerManager!!.latitude
            longitude = gpsTrackerManager!!.longitude
            Log.e("Location", "$TAG - Upload() > $latitude, $longitude")
        }

        if (songjanglist.size > 0) {

            DataUtil.logEvent("button_click", TAG, "SetDeliveryUploadData / SetPickupUploadData")

            DeviceDataUploadHelper.Builder(
                this, Preferences.userId, Preferences.officeCode, Preferences.deviceUUID,
                songjanglist, "QH", latitude, longitude
            ).setOnServerEventListener(object : OnServerEventListener {
                override fun onPostResult() {
                    MainActivityServer.getLocalCount1(this@AppBaseActivity)
                }

                override fun onPostFailList() {
                    MainActivityServer.getLocalCount1(this@AppBaseActivity)
                }
            }).build().execute()
        } else {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.msg_no_data_to_upload))
                .setTitle(resources.getString(R.string.button_upload))
                .setCancelable(false).setPositiveButton(
                    resources.getString(R.string.button_ok)
                ) { dialog: DialogInterface?, which: Int -> }.show()
        }
    }

    private fun download() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT)
                .show()
            return
        } else {
            MainActivityServer.download(this)
        }

        if (0 < uploadFailedCount.toInt()) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.msg_download_not_supported))
                .setTitle(resources.getString(R.string.text_alert))
                .setCancelable(false).setPositiveButton(
                    resources.getString(R.string.button_ok)
                ) { dialog: DialogInterface?, which: Int -> }.show()
        }
    }

    private fun gpsTrackerServiceStart() {
        if (Preferences.pickupDriver == "Y") {

            // gps 켜는 alert 창  -> 객체 하나에만 호출해도 GPS 설정창 문제 없음(기기는 하나)
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            enableGPSSetting(locationManager)

            val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            val isGooglePlayService = ConnectionResult.SUCCESS == status

            if (isGooglePlayService) { // Fused Provider Service start (Google play 에 클라이언트 객체 얻어 서비스)

                if (!isServiceRunning(FusedProviderService::class.java)) {
                    val intent = Intent(this, FusedProviderService::class.java)
                    ContextCompat.startForegroundService(this, intent)
                }

            } else { // location manager Service start (샤오미 등 구글 플레이 가 작동하지 않는 폰)

                if (!isServiceRunning(LocationManagerService::class.java)) {
                    val intent = Intent(this, LocationManagerService::class.java)
                    ContextCompat.startForegroundService(this, intent)
                }

            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, " ** onDestroy()")
        MainActivityServer.setDestroyUserInfo(this)

        //Bluetooth Setting 화면 connection 없애기
        BluetoothDeviceData.connectedPrinterAddress = null

        try {
            if (isServiceRunning(FusedProviderService::class.java)) {
                val intent = Intent(this, FusedProviderService::class.java)
                stopService(intent)
            }
        } catch (e: Exception) {
        }

        try {
            if (isServiceRunning(LocationManagerService::class.java)) {
                val intent = Intent(this, LocationManagerService::class.java)
                stopService(intent)
            }
        } catch (e: Exception) {
        }

        DatabaseHelper.getInstance().close()
    }

    private fun enableGPSSetting(locationManager: LocationManager?) {
        if (locationManager != null) {

            val gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnable) {

                DataUtil.enableLocationSettings(this)
            }
        }
    }

    private fun saveServerFCMToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (task.isSuccessful) {
                // Get new FCM registration token
                val fcmToken = task.result!!

                RetrofitClient.instanceDynamic().requestSetFCMToken(
                    fcmToken,
                    Preferences.userId,
                    "01",
                    Preferences.deviceUUID,
                    DataUtil.appID,
                    Preferences.userNation
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { it: APIModel? -> }
                    ) { it: Throwable? -> }
            }
        }
    }

    private fun initMessageCount() {

        if (NetworkUtil.isNetworkAvailable(this)) {

            try {

                val cal = Calendar.getInstance()
                cal.time = Date()
                cal.add(Calendar.DATE, -1) //minus number would decrement the days
                val yDate = cal.time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val yesterday = dateFormat.format(yDate) + " 00:00:00"
                val date = URLEncoder.encode(yesterday, "UTF-8")

                RetrofitClient.instanceDynamic().requestGetNewMessageCount(
                    date, Preferences.userId,
                    DataUtil.appID, Preferences.userNation
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe({ it: APIModel ->
                        if (it.resultObject != null) {
                            val count = Gson().fromJson<Int>(
                                it.resultObject,
                                object : TypeToken<Int?>() {}.type
                            )
                            RetrofitClient.instanceDynamic().requestGetNewMessageCountFromQxSystem(
                                Preferences.userId,
                                DataUtil.appID, Preferences.userNation
                            )
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ it1: APIModel ->
                                    if (it1.resultObject != null) {
                                        val adminCount = Gson().fromJson<Int>(
                                            it1.resultObject,
                                            object : TypeToken<Int?>() {}.type
                                        )
                                        Log.e(
                                            "Message",
                                            "count >>>> $count / $adminCount"
                                        )
                                        if (0 < count || 0 < adminCount) {
                                            setMessageCount(count, adminCount)
                                        } else {
                                            goneMessageCount()
                                        }
                                    }
                                }
                                ) { it1: Throwable ->
                                    Log.e(
                                        RetrofitClient.errorTag,
                                        "$TAG - $it1"
                                    )
                                }
                        }
                    }
                    ) { it: Throwable ->
                        Log.e(
                            RetrofitClient.errorTag,
                            "$TAG - $it"
                        )
                    }
            } catch (ignore: Exception) {
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.msg_network_connect_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
