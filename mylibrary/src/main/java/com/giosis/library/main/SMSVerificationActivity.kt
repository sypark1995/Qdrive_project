package com.giosis.library.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.giosis.library.R
import com.giosis.library.databinding.ActivitySmsVerificationBinding
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.regex.Pattern

class SMSVerificationActivity : CommonActivity() {

    var isPermissionTrue = false

    private val binding by lazy {
        ActivitySmsVerificationBinding.inflate(layoutInflater)
    }

    private var phoneNo = ""
    private var authCode = ""
    var name = ""
    var email = ""

    private var focusItem = ""
    private var mPhoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.layoutTopTitle.textTopTitle.setText(R.string.text_title_sms_verification)

        binding.editPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (Preferences.userNation == "SG") {
                    if (8 <= charSequence.length) {
                        binding.btnRequest.setBackgroundResource(R.drawable.bg_rect_929292)
                    } else {
                        binding.btnRequest.setBackgroundResource(R.drawable.bg_rect_e2e2e2)
                    }
                } else {
                    if (10 <= charSequence.length) {
                        binding.btnRequest.setBackgroundResource(R.drawable.bg_rect_929292)
                    } else {
                        binding.btnRequest.setBackgroundResource(R.drawable.bg_rect_e2e2e2)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (charSequence.isNotEmpty()) {
                    binding.btnSubmit.setBackgroundResource(R.drawable.bg_round_20_4fb648)
                } else {
                    binding.btnSubmit.setBackgroundResource(R.drawable.bg_round_20_cccccc)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })


        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            finish()
        }

        binding.btnRequest.setOnClickListener {
            requestAuthNoClick()
        }

        binding.btnSubmit.setOnClickListener {
            submitAuthNoClick()
        }


        // Permission
        val checker = PermissionChecker(this)

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

        mPhoneNumber = ""
        if (isPermissionTrue) {
            mPhoneNumber = try {
                myPhoneNumber
            } catch (e: Exception) {
                ""
            }
        }

        binding.editPhoneNumber.setText(mPhoneNumber)
    }

    private val myPhoneNumber: String
        @SuppressLint("HardwareIds")
        get() {
            var tempPhoneNo = ""
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return tempPhoneNo
            }
            val mTelephonyMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (mTelephonyMgr.line1Number != "") {
                tempPhoneNo = mTelephonyMgr.line1Number
            }
            return tempPhoneNo
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


    // SG, MY, ID 구분
    private fun requestAuthNoClick() {
        phoneNo = binding.editPhoneNumber.text.toString()
        val builder = AlertDialog.Builder(this)
        var isvalid = true

        /*
          MY 형식
          맨 앞자리 0을 포함한 10자리	// 018 367 4700
          맨 앞자리 0을 포함한 11자리 // 018 367X 4700

          ID 형식
          맨 앞자리 0을 포함한 11자리  // 0813 111 8569
          맨 앞자리 0을 포함한 12자리	 // 0813 1111 8569
          맨 앞자리 0을 포함한 13자리	 // 0813 11111 8569
          */

        if (Preferences.userNation.equals("SG", ignoreCase = true)) {
            if (phoneNo.length != 8) {
                if (phoneNo.indexOf("+65") == 0 && phoneNo.length == 11) {
                    val pattern = "\\+[0-9]+"
                    val matches = Pattern.matches(pattern, phoneNo)
                    if (!matches) {
                        isvalid = false
                        builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                        builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                        builder.setCancelable(true)
                    } else {
                        builder.setTitle(resources.getString(R.string.text_verification))
                        builder.setMessage(
                            resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                                R.string.msg_is_this_ok
                            )
                        )
                        builder.setCancelable(true)
                    }
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_please_enter_right_number))
                    builder.setCancelable(true)
                }
            } else {
                val pattern2 = "[0-9]+"
                val matches2 = Pattern.matches(pattern2, phoneNo)
                if (matches2) {
                    builder.setTitle(resources.getString(R.string.text_verification))
                    builder.setMessage(
                        resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                            R.string.msg_is_this_ok
                        )
                    )
                    builder.setCancelable(true)
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                    builder.setCancelable(true)
                }
            }
        } else if (Preferences.userNation == "MY") {
            if (phoneNo.length == 10) {       // 018 367 4700
                Log.e("Verification", "  MY - length 10 $phoneNo")
                val pattern = "^0[0-9]*$"
                val matches = Pattern.matches(pattern, phoneNo)
                if (matches) {
                    builder.setTitle(resources.getString(R.string.text_verification))
                    builder.setMessage(
                        resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                            R.string.msg_is_this_ok
                        )
                    )
                    builder.setCancelable(true)
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                    builder.setCancelable(true)
                }
            } else if (phoneNo.length == 11) {        // 018 367X 4700
                Log.e("Verification", "  MY - length 11  $phoneNo")
                val pattern = "^0[0-9]*$"
                val matches = Pattern.matches(pattern, phoneNo)
                if (matches) {
                    builder.setTitle(resources.getString(R.string.text_verification))
                    builder.setMessage(
                        resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                            R.string.msg_is_this_ok
                        )
                    )
                    builder.setCancelable(true)
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                    builder.setCancelable(true)
                }
            } else {
                isvalid = false
                builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                builder.setMessage(resources.getString(R.string.msg_please_enter_right_number))
                builder.setCancelable(true)
            }
        } else if (Preferences.userNation == "ID") {
            if (phoneNo.length == 11) {       // 0813 111 8569
                Log.e("Verification", "  ID length 11 $phoneNo")
                val pattern = "^0[0-9]*$"
                val matches = Pattern.matches(pattern, phoneNo)
                if (matches) {
                    builder.setTitle(resources.getString(R.string.text_verification))
                    builder.setMessage(
                        resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                            R.string.msg_is_this_ok
                        )
                    )
                    builder.setCancelable(true)
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                    builder.setCancelable(true)
                }
            } else if (phoneNo.length == 12) {        // 0813 1111 8569
                Log.e("Verification", "  ID  length 12  $phoneNo")
                val pattern = "^0[0-9]*$"
                val matches = Pattern.matches(pattern, phoneNo)
                if (matches) {
                    builder.setTitle(resources.getString(R.string.text_verification))
                    builder.setMessage(
                        resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                            R.string.msg_is_this_ok
                        )
                    )
                    builder.setCancelable(true)
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                    builder.setCancelable(true)
                }
            } else if (phoneNo.length == 13) {        // 0813 11111 8569
                Log.e("Verification", "  ID  length 13  $phoneNo")
                val pattern = "^0[0-9]*$"
                val matches = Pattern.matches(pattern, phoneNo)
                if (matches) {
                    builder.setTitle(resources.getString(R.string.text_verification))
                    builder.setMessage(
                        resources.getString(R.string.msg_verify_phone_number) + " (" + phoneNo + "). " + resources.getString(
                            R.string.msg_is_this_ok
                        )
                    )
                    builder.setCancelable(true)
                } else {
                    isvalid = false
                    builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                    builder.setMessage(resources.getString(R.string.msg_enter_right_format_number))
                    builder.setCancelable(true)
                }
            } else {
                isvalid = false
                builder.setTitle(resources.getString(R.string.text_invalidation_alert))
                builder.setMessage(resources.getString(R.string.msg_please_enter_right_number))
                builder.setCancelable(true)
            }
        }

        if (isvalid) {
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int ->

                dialog.cancel()

                RetrofitClient.instanceDynamic().requestGetAuthCodeRequest(phoneNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (it.resultCode != 0) {

                            val alertBuilder = AlertDialog.Builder(this@SMSVerificationActivity)
                            alertBuilder.setTitle(resources.getString(R.string.text_alert))
                            alertBuilder.setMessage(resources.getString(R.string.msg_sms_request_failed) + " " + it.resultMsg)
                            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                            try {
                                alertBuilder.show()
                            } catch (ignored: Exception) {
                            }
                        } else {

                            binding.edit4Digit.requestFocus()
                        }
                    }, {

                        Toast.makeText(
                            this,
                            "AuthCodeRequest Exception : ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            }
            builder.setNeutralButton(resources.getString(R.string.button_cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        } else {

            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun submitAuthNoClick() {

        authCode = binding.edit4Digit.text.toString()
        name = binding.editName.text.toString()
        email = binding.editEmail.text.toString()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.text_invalidation_alert))

        if (authCode.length == 4) {

            var isValidate = true
            if (name == "") {
                isValidate = false
                focusItem = "name"
                builder.setMessage(resources.getString(R.string.msg_please_enter_name))
                builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int ->
                    if (focusItem == "name") {
                        binding.editName.requestFocus()
                    }
                    dialog.cancel()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }

            if (isValidate) {

                RetrofitClient.instanceDynamic()
                    .requestSetAuthCodeCheck(phoneNo, authCode, name, email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        if (it.resultCode != 0) {

                            val alertBuilder = AlertDialog.Builder(this@SMSVerificationActivity)
                            alertBuilder.setTitle(resources.getString(R.string.text_alert))
                            alertBuilder.setMessage(
                                "${resources.getString(R.string.msg_sms_verification_failed)} ${it.resultMsg}\n${
                                    resources.getString(
                                        R.string.msg_verification_not_use
                                    )
                                }"
                            )
                            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                            if (!this@SMSVerificationActivity.isFinishing) {
                                val alertDialog = alertBuilder.create()
                                alertDialog.show()
                            }
                        } else {

                            val alertBuilder = AlertDialog.Builder(this@SMSVerificationActivity)
                            alertBuilder.setTitle(resources.getString(R.string.text_success))
                            alertBuilder.setMessage(resources.getString(R.string.msg_sms_verification_success))
                            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int ->

                                dialog.cancel()

                                Preferences.userName = name

                                val intent =
                                    Intent(this@SMSVerificationActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                startActivity(intent)
                                overridePendingTransition(
                                    android.R.anim.fade_in,
                                    android.R.anim.fade_out
                                )
                                finish()
                            }

                            if (!this@SMSVerificationActivity.isFinishing) {
                                val alertDialog = alertBuilder.create()
                                alertDialog.show()
                            }
                        }
                    }, {

                        Toast.makeText(
                            this,
                            "SetAuthCodeCheck Exception : ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            }
        } else {

            builder.setMessage(resources.getString(R.string.msg_please_enter_right_number))
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int ->
                binding.edit4Digit.requestFocus()
                dialog.cancel()
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    companion object {
        private const val TAG = "SMSVerificationActivity"
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(PermissionChecker.READ_PHONE_STATE)
    }
}