package com.giosis.library

import android.view.View
import androidx.databinding.BindingAdapter


@BindingAdapter("onClick")
fun View.onClick(listener: (() -> Unit)?) {
    setOnClickListener {
        listener?.invoke()
    }
}
