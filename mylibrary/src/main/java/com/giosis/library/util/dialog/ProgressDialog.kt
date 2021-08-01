package com.giosis.library.util.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.giosis.library.R

class ProgressDialog @JvmOverloads constructor(
        context: Context
) : View(context) {

    private lateinit var dialog: Dialog

    private lateinit var binding: ViewDataBinding

    private var dialogVisibility: Int = GONE
        set(value) {
            field = value
            if (value == VISIBLE) {
                dialog.show()
            } else {
                dialog.dismiss()
            }
        }

    init {
        createDialog(context)
    }

    private fun createDialog(context: Context) {
        val frameLayout = FrameLayout(context)

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.layout_progress,
                frameLayout,
                true
        )

        dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar).apply {
            setContentView(frameLayout)
            setCancelable(true)
        }
    }

    fun setCancelable(boolean: Boolean) {

        dialog.setCancelable(boolean)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(0, 0)
    }

    override fun setVisibility(visibility: Int) {
        dialogVisibility = visibility
    }

    override fun getVisibility() = dialogVisibility

    /**
     * Sometimes while showing the dialog we need to replace its holder fragment or activity. In this case we
     * need to dismiss dialog.
     */
    override fun onDetachedFromWindow() {
        dialog.dismiss()
        super.onDetachedFromWindow()
    }
}