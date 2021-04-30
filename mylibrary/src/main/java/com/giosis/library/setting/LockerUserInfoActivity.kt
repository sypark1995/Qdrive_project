package com.giosis.library.setting


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityLockerUserInfoBinding
import com.giosis.library.util.DataUtil
import kotlinx.android.synthetic.main.activity_locker_user_info.*
import kotlinx.android.synthetic.main.top_title.*


class LockerUserInfoActivity : BaseActivity<ActivityLockerUserInfoBinding, LockerUserInfoViewModel>() {

    val tag = "LockerUserInfoActivity"


    override fun getLayoutId(): Int {
        return R.layout.activity_locker_user_info
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): LockerUserInfoViewModel {
        return ViewModelProvider(this).get(LockerUserInfoViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        text_top_title.text = resources.getString(R.string.text_title_locker_user_info)

        layout_top_back.setOnClickListener {
            finish()
        }

        btn_locker_user_go.setOnClickListener {

            val webUri: Uri = Uri.parse(DataUtil.locker_pin_url)
            val intent = Intent(Intent.ACTION_VIEW, webUri)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }


        getViewModel().userKey.observe(this, Observer {

            DataUtil.copyClipBoard(this, it)

            text_locker_user_user_key.text = it
            text_locker_user_user_key_1.text = it
            text_locker_user_user_key_1.visibility = View.GONE
        })

        getViewModel().status.observe(this, Observer {

            text_locker_user_status.text = it
        })

        getViewModel().mobile.observe(this, Observer {

            text_locker_user_mobile_no.text = it
        })

        getViewModel().expiryDate.observe(this, Observer {

            text_locker_user_expiry_pin_date.text = it
        })

        getViewModel().barcodeImg.observe(this, Observer {

            if (it != null) {

                layout_locker_user_barcode.visibility = View.VISIBLE
                text_locker_user_barcode_error.visibility = View.GONE

                val resizeBitmap = Bitmap.createScaledBitmap(it, dpTopx(260F), dpTopx(100F), true)
                val ob = BitmapDrawable(resources, resizeBitmap)
                img_locker_user_barcode.background = ob
                text_locker_user_user_key_1.visibility = View.VISIBLE
            } else {

                layout_locker_user_barcode.visibility = View.GONE
                text_locker_user_barcode_error.visibility = View.VISIBLE
            }
        })


        getViewModel().errorAlert.observe(this, Observer {

            Toast.makeText(this, resources.getString(it), Toast.LENGTH_SHORT).show()
        })
    }


    override fun onResume() {
        super.onResume()

        getViewModel().callServer()
    }


    private fun dpTopx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
    }
}
//    }
//
//
//    var TAG = "LockerUserInfoActivity"
//    var gson = Gson()
//    var layout_top_back: FrameLayout? = null
//    var text_top_title: TextView? = null
//    var text_locker_user_user_key: TextView? = null
//    var text_locker_user_status: TextView? = null
//    var text_locker_user_mobile_no: TextView? = null
//    var text_locker_user_expiry_pin_date: TextView? = null
//    var layout_locker_user_barcode: LinearLayout? = null
//    var img_locker_user_barcode: ImageView? = null
//    var text_locker_user_user_key_1: TextView? = null
//    var text_locker_user_barcode_error: TextView? = null
//    var btn_locker_user_go: Button? = null
//    var context: Context? = null
//    var op_id: String? = null
//    var progressDialog: ProgressDialog? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_locker_user_info)
//        layout_top_back = findViewById(R.id.layout_top_back)
//        text_top_title = findViewById(R.id.text_top_title)
//        text_locker_user_user_key = findViewById(R.id.text_locker_user_user_key)
//        text_locker_user_status = findViewById(R.id.text_locker_user_status)
//        text_locker_user_mobile_no = findViewById(R.id.text_locker_user_mobile_no)
//        text_locker_user_expiry_pin_date = findViewById(R.id.text_locker_user_expiry_pin_date)
//        layout_locker_user_barcode = findViewById(R.id.layout_locker_user_barcode)
//        img_locker_user_barcode = findViewById(R.id.img_locker_user_barcode)
//        text_locker_user_user_key_1 = findViewById(R.id.text_locker_user_user_key_1)
//        text_locker_user_barcode_error = findViewById(R.id.text_locker_user_barcode_error)
//        btn_locker_user_go = findViewById(R.id.btn_locker_user_go)
//
//
//        //
//        context = applicationContext
//        //op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//        op_id = com.giosis.util.qdrive.singapore.MyApplication.preferences.userId
//        progressDialog = ProgressDialog(this@LockerUserInfoActivity)
//        text_top_title.setText(R.string.text_title_locker_user_info)
//        layout_top_back.setOnClickListener(clickListener)
//        text_locker_user_barcode_error.setOnClickListener(clickListener)
//        btn_locker_user_go.setOnClickListener(clickListener)
//        val content = SpannableString(resources.getString(R.string.text_error_retry))
//        content.setSpan(UnderlineSpan(), 0, content.length, 0)
//        text_locker_user_barcode_error.setText(content)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (com.giosis.util.qdrive.util.NetworkUtil.isNetworkAvailable(this@LockerUserInfoActivity)) {
//            val lockerUserInfoAsyncTask = LockerUserInfoAsyncTask(op_id)
//            lockerUserInfoAsyncTask.execute()
//        } else {
//            Toast.makeText(this@LockerUserInfoActivity, getString(R.string.wifi_connect_failed), Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    var clickListener = View.OnClickListener { v ->
//        when (v.id) {
//            R.id.layout_top_back -> {
//                finish()
//            }
//            R.id.text_locker_user_barcode_error -> {
//
//
//                //op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//                op_id = com.giosis.util.qdrive.singapore.MyApplication.preferences.userId
//                if (com.giosis.util.qdrive.util.NetworkUtil.isNetworkAvailable(this@LockerUserInfoActivity)) {
//                    val lockerUserInfoAsyncTask = LockerUserInfoAsyncTask(op_id)
//                    lockerUserInfoAsyncTask.execute()
//                } else {
//                    Toast.makeText(this@LockerUserInfoActivity, getString(R.string.wifi_connect_failed), Toast.LENGTH_SHORT).show()
//                }
//            }
//            R.id.btn_locker_user_go -> {
//                val webpage = Uri.parse(com.giosis.util.qdrive.util.DataUtil.locker_pin_url)
//                val intent = Intent(Intent.ACTION_VIEW, webpage)
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivity(intent)
//                }
//            }
//        }
//    }
//
//    private inner class LockerUserInfoAsyncTask // TEST
//    // this.op_id = "7Eleven.Ajib";
//    internal constructor(var op_id: String?) : AsyncTask<Void?, Void?, LockerUserInfoResult?>() {
//        override fun onPreExecute() {
//            super.onPreExecute()
//            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//            progressDialog!!.setMessage(resources.getString(R.string.text_please_wait))
//            progressDialog!!.setCancelable(false)
//            progressDialog!!.show()
//        }
//
//        protected override fun doInBackground(vararg params: Void): LockerUserInfoResult? {
//            var resultObj: LockerUserInfoResult?
//            try {
//                val job = JSONObject()
//                job.accumulate("op_id", op_id)
//                job.accumulate("app_id", com.giosis.util.qdrive.util.DataUtil.appID)
//                job.accumulate("nation_cd", com.giosis.util.qdrive.util.DataUtil.nationCode)
//                val methodName = "GetShuttleDriverForFederatedlockerInfo"
//                val jsonString: String = com.giosis.util.qdrive.util.Custom_JsonParser.requestServerDataReturnJSON(methodName, job)
//                resultObj = gson.fromJson(jsonString, LockerUserInfoResult::class.java)
//            } catch (e: Exception) {
//                Log.e("Exception", "$TAG  GetShuttleDriverForFederatedlockerInfo Json Exception : $e")
//                resultObj = null
//            }
//            return resultObj
//        }
//
//        override fun onPostExecute(result: LockerUserInfoResult?) {
//            try {
//                if (result != null) {
//                    if (result.getResult_code().equals("0")) {
//                        val resultRow: LockerResultRow = result.getResultObject().getResultRows().get(0)
//                        com.giosis.util.qdrive.util.DataUtil.copyClipBoard(context, resultRow.getUser_key())
//                        text_locker_user_user_key.setText(resultRow.getUser_key())
//                        text_locker_user_status.setText(resultRow.getUser_status())
//                        text_locker_user_mobile_no.setText(resultRow.getUser_mobile())
//                        try {
//                            val result_date: String = resultRow.getUser_expiry_date()
//                            val old_format: DateFormat = SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.KOREA)
//                            val new_format: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
//                            val old_date = old_format.parse(result_date)
//                            text_locker_user_expiry_pin_date!!.text = new_format.format(old_date)
//                        } catch (e: ParseException) {
//                            Log.e("krm0219", "Error : $e")
//                            text_locker_user_expiry_pin_date.setText(resultRow.getUser_expiry_date())
//                        }
//                        val barcodeAsyncTask = BarcodeAsyncTask(resultRow)
//                        barcodeAsyncTask.execute()
//                    } else {
//                        com.giosis.util.qdrive.util.DisplayUtil.dismissProgressDialog(progressDialog)
//                        Toast.makeText(this@LockerUserInfoActivity, resources.getString(R.string.msg_download_locker_info_error) + " - " + result.getResult_msg(), Toast.LENGTH_SHORT).show()
//                        Log.e("krm0219", "LockerUserInfoAsyncTask  ResultCode : " + result.getResult_code().toString() + " / " + result.getResult_msg())
//                    }
//                } else {
//                    com.giosis.util.qdrive.util.DisplayUtil.dismissProgressDialog(progressDialog)
//                    Toast.makeText(this@LockerUserInfoActivity, """${resources.getString(R.string.msg_download_locker_info_error)}
// ${resources.getString(R.string.msg_please_try_again)}""", Toast.LENGTH_SHORT).show()
//                    Log.e("krm0219", "LockerUserInfoAsyncTask  result null")
//                }
//            } catch (e: Exception) {
//                com.giosis.util.qdrive.util.DisplayUtil.dismissProgressDialog(progressDialog)
//                Toast.makeText(this@LockerUserInfoActivity, """
//     ${resources.getString(R.string.msg_download_locker_info_error)}
//     $e
//     """.trimIndent(), Toast.LENGTH_SHORT).show()
//                Log.e("krm0219", "$TAG  LockerUserInfoAsyncTask Exception : $e")
//            }
//        }
//
//    }
//
//    inner class BarcodeAsyncTask(data: LockerResultRow) : AsyncTask<Void?, Void?, Bitmap?>() {
//        var result: LockerResultRow
//        var barcode_data: String
//        var imgUrl: String? = null
//        protected override fun doInBackground(vararg params: Void): Bitmap? {
//            return try {
//                imgUrl = com.giosis.util.qdrive.util.DataUtil.barcode_url + barcode_data
//                Log.e("krm0219", "$TAG Barcode URL = $imgUrl")
//                val url = URL(imgUrl)
//                trustAllHosts()
//                val connection = url.openConnection() as HttpURLConnection
//                connection.doInput = true
//                connection.connect()
//                val input = connection.inputStream
//                BitmapFactory.decodeStream(input)
//            } catch (e: Exception) {
//                Log.e("krm0219", "QRCodeAsyncTask Exception : $e")
//                e.printStackTrace()
//                null
//            }
//        }
//
//        override fun onPostExecute(bitmap: Bitmap?) {
//            super.onPostExecute(bitmap)
//            com.giosis.util.qdrive.util.DisplayUtil.dismissProgressDialog(progressDialog)
//            if (bitmap != null) {
//                layout_locker_user_barcode!!.visibility = View.VISIBLE
//                text_locker_user_barcode_error!!.visibility = View.GONE
//                text_locker_user_user_key_1.setText(result.getUser_key())
//                val resizeBitmap = Bitmap.createScaledBitmap(bitmap, dpTopx(260f), dpTopx(100f), true)
//                val ob = BitmapDrawable(resources, resizeBitmap)
//                img_locker_user_barcode!!.background = ob
//            } else {
//                layout_locker_user_barcode!!.visibility = View.GONE
//                text_locker_user_barcode_error!!.visibility = View.VISIBLE
//            }
//        }
//
//        private fun trustAllHosts() {
//            // Create a trust manager that does not validate certificate chains
//            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
//                override fun getAcceptedIssuers(): Array<X509Certificate> {
//                    return arrayOf()
//                }
//
//                @Throws(CertificateException::class)
//                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
//                }
//
//                @Throws(CertificateException::class)
//                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
//                }
//            })
//
//            // Install the all-trusting trust manager
//            try {
//                val sc = SSLContext.getInstance("TLS")
//                sc.init(null, trustAllCerts, SecureRandom())
//                HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        init {
//            result = data
//            barcode_data = data.getUser_key()
//        }
//    }
//
//    private fun dpTopx(dp: Float): Int {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context!!.resources.displayMetrics).toInt()
//    }
//}