package com.giosis.util.qdrive.settings

//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.DialogInterface
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import com.giosis.library.setting.*
//import com.giosis.util.qdrive.singapore.LoginActivity
//import com.giosis.util.qdrive.singapore.MyApplication
//import com.giosis.util.qdrive.singapore.R
//import com.giosis.util.qdrive.util.DatabaseHelper
//import com.giosis.util.qdrive.util.DisplayUtil
import com.giosis.util.qdrive.util.ui.CommonActivity
//import kotlinx.android.synthetic.main.activity_setting.*
//import kotlinx.android.synthetic.main.top_title.*

/**
 * @author krm0219
 */
class SettingActivity : CommonActivity() {

//    var TAG = "SettingActivity"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_setting)
//
//        text_top_title.setText(R.string.navi_setting)
//        layout_top_signout.visibility = View.VISIBLE
//
//        val mBitmap = BitmapFactory.decodeResource(resources, R.drawable.qdrive_img_default)
//        img_setting_profile.setImageBitmap(mBitmap)
//        val roundedImageDrawable = DisplayUtil.createRoundedBitmapImageDrawableWithBorder(this, mBitmap)
//        img_setting_profile.setImageDrawable(roundedImageDrawable)
//
//
//
//        text_top_title.setOnClickListener(clickListener)
//        layout_top_back.setOnClickListener(clickListener)
//        layout_top_signout.setOnClickListener(clickListener)
//        img_setting_modify_info.setOnClickListener(clickListener)
//        text_setting_change_password.setOnClickListener(clickListener)
//        text_setting_delete_data.setOnClickListener(clickListener)
//        layout_setting_notice.setOnClickListener(clickListener)
//        layout_setting_printer_setting.setOnClickListener(clickListener)
//        layout_setting_scan_setting.setOnClickListener(clickListener)
//        layout_setting_locker.setOnClickListener(clickListener)
//        btn_setting_developer_mode.setOnClickListener(clickListener)
//        layout_setting_language.setOnClickListener {
//
//            val intent = Intent(this, LanguageSettingActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    override fun onResume() {
//        super.onResume()
//        Log.e("krm0219", "SG  Scan Vibration  " + MyApplication.preferences.scanVibration)
//        initDeveloperMode()
//
//        val opId = MyApplication.preferences.userId
//        val outletDriverYN = MyApplication.preferences.outletDriver
//        val lockerStatus = MyApplication.preferences.lockerStatus
//        val version = MyApplication.preferences.appVersion
//
//        text_setting_driver_name.text = MyApplication.preferences.userName
//        text_setting_driver_id.text = opId
//        text_setting_driver_email.text = MyApplication.preferences.userEmail
//        text_setting_driver_branch.text = MyApplication.preferences.officeName
//
//
//        when {
//            MyApplication.preferences.serverURL.contains("test") -> {
//                text_setting_app_version.text = "$version _ test"
//            }
//            MyApplication.preferences.serverURL.contains("staging") -> {
//                text_setting_app_version.text = "$version _ staging"
//            }
//            else -> {
//                text_setting_app_version.text = version
//            }
//        }
//
//        if (outletDriverYN == "Y") {
//            if (lockerStatus.contains("no pin") || lockerStatus.contains("active") || lockerStatus.contains("expired")) {
//                layout_setting_locker.visibility = View.VISIBLE
//            }
//        }
//        // TEST
//        if (opId.equals("karam.kim", ignoreCase = true)) {
//            layout_setting_locker.visibility = View.VISIBLE
//        }
//    }
//
//
//    private fun initDeveloperMode() {
//        if (MyApplication.preferences.developerMode) {
//            btn_setting_developer_mode.visibility = View.VISIBLE
//        } else {
//            btn_setting_developer_mode.visibility = View.GONE
//        }
//    }
//
//    var showDeveloperModeClickCount = 0
//    var clickListener = View.OnClickListener { v: View ->
//        when (v.id) {
//            R.id.text_top_title -> {
//                if (showDeveloperModeClickCount == 10) {
//                    if (MyApplication.preferences.developerMode) {
//                        // true > false
//                        MyApplication.preferences.developerMode = false
//                    } else {
//                        // false > true
//                        MyApplication.preferences.developerMode = true
//                        Toast.makeText(this@SettingActivity, resources.getString(R.string.text_developer_mode), Toast.LENGTH_SHORT).show()
//                    }
//                    showDeveloperModeClickCount = 0
//                    initDeveloperMode()
//                } else {
//                    showDeveloperModeClickCount++
//                }
//            }
//            R.id.layout_top_back -> {
//                finish()
//            }
//            R.id.layout_top_signout -> {
//                signOut()
//            }
//            R.id.img_setting_modify_info -> {
//                val intent = Intent(this@SettingActivity, ModifyUserInfoActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.text_setting_change_password -> {
//                val intent = Intent(this@SettingActivity, ChangePwdActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.text_setting_delete_data -> {
//                deleteData()
//            }
//            R.id.layout_setting_notice -> {
//                val intent = Intent(this@SettingActivity, NoticeActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.layout_setting_printer_setting -> {
//                val intent = Intent(this@SettingActivity, PrinterSettingActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.layout_setting_scan_setting -> {
//                val intent = Intent(this@SettingActivity, ScanSettingActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.layout_setting_locker -> {
//                val intent = Intent(this@SettingActivity, LockerUserInfoActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.btn_setting_developer_mode -> {
//                val intent = Intent(this@SettingActivity, DeveloperModeActivity::class.java)
//                startActivity(intent)
//            }
//        }
//    }
//
//
//    private fun signOut() {
//
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(resources.getString(R.string.button_confirm))
//        builder.setMessage(resources.getString(R.string.msg_want_sign_out))
//        builder.setCancelable(true)
//        builder.setPositiveButton(resources.getString(R.string.button_ok)) { _: DialogInterface?, _: Int ->
//
//            val intent = Intent(this@SettingActivity, LoginActivity::class.java)
//            intent.putExtra("method", "signOut")
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//            finish()
//        }
//
//        builder.setNeutralButton(resources.getString(R.string.button_cancel)) { dialog: DialogInterface, id: Int -> dialog.cancel() }
//
//        val alertDialog = builder.create()
//        alertDialog.show()
//    }
//
//
//    private fun deleteData() {
//
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(resources.getString(R.string.button_confirm))
//        builder.setMessage(resources.getString(R.string.msg_want_to_delete_data))
//        builder.setCancelable(true)
//        builder.setPositiveButton(resources.getString(R.string.button_ok)) { _: DialogInterface?, _: Int ->
//            try {
//                val delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "")
//                Log.i("krm0219", "Delete Count : $delete")
//
//                val builder1 = AlertDialog.Builder(this@SettingActivity)
//                builder1.setTitle(resources.getString(R.string.text_alert))
//                builder1.setMessage(resources.getString(R.string.msg_deleted_data))
//                builder1.setPositiveButton(resources.getString(R.string.button_ok)) { dialog1: DialogInterface, _: Int -> dialog1.cancel() }
//                val alertDialog = builder1.create()
//                alertDialog.show()
//            } catch (ignored: Exception) {
//            }
//        }
//
//        builder.setNeutralButton(resources.getString(R.string.button_cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
//        val alertDialog = builder.create()
//        alertDialog.show()
//    }
}