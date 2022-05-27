package com.giosis.util.qdrive.singapore

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("onClick")
fun View.onClick(listener: (() -> Unit)?) {
    setOnClickListener {
        listener?.invoke()
    }
}

@BindingAdapter("set_text_resource", "set_text_string")
fun TextView.setText(resource: Int, string: String) {
    if (resource == 0) {
        text = string
    } else {
        setText(resource)
    }
}


@BindingAdapter("imageBitmap")
fun loadImage(iv: ImageView, bitmap: Bitmap?) {
    iv.setImageBitmap(bitmap)
}
