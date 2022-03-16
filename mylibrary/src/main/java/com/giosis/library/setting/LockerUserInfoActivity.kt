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


class LockerUserInfoActivity :
    BaseActivity<ActivityLockerUserInfoBinding, LockerUserInfoViewModel>() {

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
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            .toInt()
    }
}
