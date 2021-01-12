package com.giosis.util.qdrive.settings

import com.giosis.util.qdrive.util.ui.CommonActivity

class SettingActivity : CommonActivity() {

//    val tag = "SettingActivity"
//    val context: Context = MyApplication.getContext()
//
//    var showDeveloperModeClickCount = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_setting)
//
//
//        text_top_title.text = context.resources.getString(R.string.text_setting)
//        layout_top_signout.visibility = View.VISIBLE
//        //   initBottomMenu()
//
//
//        val profileBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.qdrive_img_default)
//        img_setting_profile.setImageBitmap(profileBitmap)
//        val roundedImageDrawable = createRoundedBitmapImageDrawableWithBorder(profileBitmap)
//        img_setting_profile.setImageDrawable(roundedImageDrawable)
//
//
//        //
//        layout_top_back.setOnClickListener {
//
//            finish()
//        }
//
//        text_top_title.setOnClickListener {
//
//            if (showDeveloperModeClickCount == 10) {
//
//                if (MyApplication.preferences.developerMode) {
//
//                    MyApplication.preferences.developerMode = false
//                } else {
//
//                    MyApplication.preferences.developerMode = true
//                    Toast.makeText(this, resources.getString(R.string.text_developer_mode), Toast.LENGTH_SHORT).show()
//                }
//
//                showDeveloperModeClickCount = 0;
//                initDeveloperMode()
//            } else {
//
//                showDeveloperModeClickCount++;
//            }
//        }
//
//        layout_top_signout.setOnClickListener {
//
//            signOut()
//        }
//
//        img_setting_modify_info.setOnClickListener {
//
//            val intent = Intent(this, ModifyUserInfoActivity::class.java)
//            startActivity(intent)
//        }
//
//        text_setting_change_password.setOnClickListener {
//
//            val intent = Intent(this, ChangePwdActivity::class.java)
//            startActivity(intent)
//        }
//
//        text_setting_delete_data.setOnClickListener {
//
//            deleteData()
//        }
//
//        layout_setting_notice.setOnClickListener {
//
//            val intent = Intent(this, NoticeActivity::class.java)
//            startActivity(intent)
//        }
//
//        layout_setting_printer_setting.setOnClickListener {
//
//            val intent = Intent(this, PrinterSettingActivity::class.java)
//            startActivity(intent)
//        }
//
//        layout_setting_scan_setting.setOnClickListener {
//
//            val intent = Intent(this, ScanSettingActivity::class.java)
//            startActivity(intent)
//        }
//
//        layout_setting_language.setOnClickListener {
//
//            val intent = Intent(this, LanguageSettingActivity::class.java)
//            startActivity(intent)
//        }
//
//        layout_setting_locker.setOnClickListener {
//
//            val intent = Intent(this, LockerUserInfoActivity::class.java)
//            startActivity(intent)
//        }
//
//        btn_setting_developer_mode.setOnClickListener {
//
//            val intent = Intent(this, DeveloperModeActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//
//    @SuppressLint("SetTextI18n")
//    override fun onResume() {
//        super.onResume()
//
//        Log.e("krm0219", "MY  Scan Vibration ${MyApplication.preferences.scanVibration}")
//        initDeveloperMode()
//
//        text_setting_driver_name.text = MyApplication.preferences.userName
//        text_setting_driver_id.text = MyApplication.preferences.userId
//        text_setting_driver_email.text = MyApplication.preferences.userEmail
//        text_setting_driver_branch.text = MyApplication.preferences.officeName
//
//
//        when {
//
//            MyApplication.preferences.serverURL.contains("test") -> {
//
//                text_setting_app_version.text = "${MyApplication.preferences.appVersion} _ test"
//            }
//            MyApplication.preferences.serverURL.contains("staging") -> {
//
//                text_setting_app_version.text = "${MyApplication.preferences.appVersion} _ staging"
//            }
//            else -> {
//
//                text_setting_app_version.text = MyApplication.preferences.appVersion
//            }
//        }
//
//
//        val outletDriverYN = MyApplication.preferences.outletDriver
//        val lockerStatus = MyApplication.preferences.lockerStatus
//
//        if (outletDriverYN == "Y") {
//            if (lockerStatus.contains("no pin") || lockerStatus.contains("active") || lockerStatus.contains("expired")) {
//
//                layout_setting_locker.visibility = View.VISIBLE
//            }
//        }
//    }
//
//
//    private fun initDeveloperMode() {
//
//        if (MyApplication.preferences.developerMode) {
//
//            btn_setting_developer_mode.visibility = View.VISIBLE
//        } else {
//
//            btn_setting_developer_mode.visibility = View.GONE
//        }
//    }
//
//
//    private fun signOut() {
//
//        Log.e(tag, " clicked signOut")
//
//
//        val alertBuilder = AlertDialog.Builder(this)
//        alertBuilder.setTitle(context.resources.getString(R.string.button_confirm))
//        alertBuilder.setMessage(context.resources.getString(R.string.msg_want_sign_out))
//        alertBuilder.setCancelable(true)
//
//        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { _, _ ->
//
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.putExtra("method", "signOut")
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            startActivity(intent)
//        }
//
//        alertBuilder.setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
//
//            dialogInterface.cancel()
//        }
//
//        alertBuilder.show()
//    }
//
//
//    private fun deleteData() {
//
//        Log.e(tag, "  clicked  deleteData")
//
//        val alertBuilder = AlertDialog.Builder(this)
//        alertBuilder.setTitle(context.resources.getString(R.string.button_confirm))
//        alertBuilder.setMessage(context.resources.getString(R.string.msg_want_to_delete_data))
//        alertBuilder.setCancelable(true)
//
//        alertBuilder.setPositiveButton(context.resources.getString(R.string.button_ok)) { _, _ ->
//
//            DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "")
//
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle(context.resources.getString(R.string.text_alert))
//            builder.setMessage(context.resources.getString(R.string.msg_deleted_data))
//            builder.setPositiveButton(context.resources.getString(R.string.button_ok)) { dialogInterface, _ ->
//
//                dialogInterface.cancel()
//            }
//
//            builder.show()
//        }
//
//        alertBuilder.setNegativeButton(context.resources.getString(R.string.button_cancel)) { dialogInterface, _ ->
//
//            dialogInterface.cancel()
//        }
//
//        alertBuilder.show()
//    }
//
//
//    private fun createRoundedBitmapImageDrawableWithBorder(bitmap: Bitmap): RoundedBitmapDrawable? {
//
//        val bitmapWidthImage = bitmap.width
//        val bitmapHeightImage = bitmap.height
//        val borderWidthHalfImage = 4
//        val bitmapRadiusImage = Math.min(bitmapWidthImage, bitmapHeightImage) / 2
//        val bitmapSquareWidthImage = Math.min(bitmapWidthImage, bitmapHeightImage)
//        val newBitmapSquareWidthImage = bitmapSquareWidthImage + borderWidthHalfImage
//        val roundedImageBitmap = Bitmap.createBitmap(newBitmapSquareWidthImage, newBitmapSquareWidthImage, Bitmap.Config.ARGB_8888)
//        val mcanvas = Canvas(roundedImageBitmap)
//        mcanvas.drawColor(Color.RED)
//        val i = borderWidthHalfImage + bitmapSquareWidthImage - bitmapWidthImage
//        val j = borderWidthHalfImage + bitmapSquareWidthImage - bitmapHeightImage
//        mcanvas.drawBitmap(bitmap, i.toFloat(), j.toFloat(), null)
//        val borderImagePaint = Paint()
//        borderImagePaint.style = Paint.Style.STROKE
//        borderImagePaint.strokeWidth = borderWidthHalfImage * 2.toFloat()
//        borderImagePaint.color = context.resources.getColor(R.color.color_ebebeb)
//        mcanvas.drawCircle(mcanvas.width / 2.toFloat(), mcanvas.width / 2.toFloat(), newBitmapSquareWidthImage / 2.toFloat(), borderImagePaint)
//        val roundedImageBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, roundedImageBitmap)
//        roundedImageBitmapDrawable.cornerRadius = bitmapRadiusImage.toFloat()
//        roundedImageBitmapDrawable.setAntiAlias(true)
//        return roundedImageBitmapDrawable
//    }
//
//
//    private fun initBottomMenu() {
//
//        btn_bottom_bar_home.setBackgroundResource(R.drawable.tab_icon_home_selector)
//        btn_bottom_bar_scan.setBackgroundResource(R.drawable.tab_icon_scan_selector)
//        btn_bottom_bar_list.setBackgroundResource(R.drawable.tab_icon_list_selector)
//        btn_bottom_bar_setting.setBackgroundResource(R.drawable.qdrive_tab_setting_h)
//
//
//        layout_bottom_bar_home.setOnClickListener {
//
//            finish()
//        }
//
//        layout_bottom_bar_scan.setOnClickListener {
//
//            val intent = Intent(this, ScanActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        layout_bottom_bar_list.setOnClickListener {
//
//            val intent = Intent(this, ListActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
}