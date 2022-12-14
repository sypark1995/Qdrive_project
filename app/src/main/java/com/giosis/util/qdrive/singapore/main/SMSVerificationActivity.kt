package com.giosis.util.qdrive.singapore.main

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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ActivitySmsVerificationBinding
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.CommonActivity
import com.giosis.util.qdrive.singapore.util.PermissionActivity
import com.giosis.util.qdrive.singapore.util.PermissionChecker
import com.giosis.util.qdrive.singapore.util.Preferences
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SMSVerificationActivity : CommonActivity() {

    companion object {
        private const val TAG = "SMSVerificationActivity"
        private const val PERMISSION_REQUEST_CODE = 1000
        private val PERMISSIONS = arrayOf(PermissionChecker.READ_PHONE_STATE)
    }

    var isPermissionTrue = false

    private val binding by lazy {
        ActivitySmsVerificationBinding.inflate(layoutInflater)
    }

    private var phoneNo = ""
    var name = ""
    var email = ""

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

        if (isPermissionTrue) {
            val mPhoneNumber = try {
                myPhoneNumber
            } catch (e: Exception) {
                ""
            }
            binding.editPhoneNumber.setText(mPhoneNumber)
        }
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

    // SG, MY, ID ??????
    private fun requestAuthNoClick() {
        phoneNo = binding.editPhoneNumber.text.toString()
        val builder = AlertDialog.Builder(this)
        var isvalid = true

        /*
          MY ??????
          ??? ????????? 0??? ????????? 10??????	// 018 367 4700
          ??? ????????? 0??? ????????? 11?????? // 018 367X 4700

          ID ??????
          ??? ????????? 0??? ????????? 11??????  // 0813 111 8569
          ??? ????????? 0??? ????????? 12??????	 // 0813 1111 8569
          ??? ????????? 0??? ????????? 13??????	 // 0813 11111 8569
          */

        if (Preferences.userNation == ("SG")) {
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

                lifecycleScope.launch {
                    try {
                        val result =
                            RetrofitClient.instanceDynamic().requestGetAuthCodeRequest(phoneNo)

                        if (result.resultCode != 0) {

                            val alertBuilder = AlertDialog.Builder(this@SMSVerificationActivity)
                            alertBuilder.setTitle(resources.getString(R.string.text_alert))
                            alertBuilder.setMessage(resources.getString(R.string.msg_sms_request_failed) + " " + result.resultMsg)
                            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                            alertBuilder.show()

                        } else {
                            binding.edit4Digit.requestFocus()
                        }

                    } catch (e: java.lang.Exception) {

                    }
                }
            }
            builder.setNeutralButton(resources.getString(R.string.button_cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }

        } else {

            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun submitAuthNoClick() {

        val authCode = binding.edit4Digit.text.toString()
        name = binding.editName.text.toString()
        email = binding.editEmail.text.toString()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.text_invalidation_alert))

        if (name == "") {
            builder.setMessage(resources.getString(R.string.msg_please_enter_name))
            builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int ->
                binding.editName.requestFocus()
                dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()

        } else {

            if (authCode.length != 4) {

                builder.setMessage(resources.getString(R.string.msg_please_enter_right_number))
                builder.setPositiveButton(resources.getString(R.string.button_ok)) { dialog: DialogInterface, _: Int ->
                    binding.edit4Digit.requestFocus()
                    dialog.cancel()
                }

                val alertDialog = builder.create()
                alertDialog.show()

            } else {

                lifecycleScope.launch {
                    try {

                        val result = RetrofitClient.instanceDynamic()
                            .requestSetAuthCodeCheck(phoneNo, authCode, name, email)

                        if (result.resultCode != 0) {

                            val alertBuilder = AlertDialog.Builder(this@SMSVerificationActivity)
                            alertBuilder.setTitle(resources.getString(R.string.text_alert))
                            alertBuilder.setMessage(
                                "${resources.getString(R.string.msg_sms_verification_failed)} ${result.resultMsg}" +
                                        "\n${resources.getString(R.string.msg_verification_not_use)}"
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

                    } catch (e: java.lang.Exception) {

                    }
                }
            }
        }
    }
}