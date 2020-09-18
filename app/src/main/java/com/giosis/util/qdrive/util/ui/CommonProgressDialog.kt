package com.giosis.util.qdrive.util.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import com.giosis.util.qdrive.singapore.R
import java.util.*


class CommonProgressDialog(context: Context) : Dialog(context) {


    init {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Objects.requireNonNull(window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)))
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_progress)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        /*  Glide.with(context)
                  .asGif()
                  .load(R.drawable.android_loading)
                  .into(img_progress)*/
    }


    fun showProgress(): CommonProgressDialog {

        show()
        return this
    }

    override fun onBackPressed() {
        //   super.onBackPressed()
    }
}