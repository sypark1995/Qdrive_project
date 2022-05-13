package com.giosis.util.qdrive.singapore

import android.graphics.Bitmap
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.main.route.RouteAdapter
import com.giosis.util.qdrive.singapore.main.route.RouteModel


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


@BindingAdapter("spinnerSelect")
fun bindSpinnerSelect(spinner: Spinner, result: MutableLiveData<Int>) {

    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            result.value = position
        }
    }
}

@BindingAdapter("routeData")
fun bindRouteData(recyclerView: RecyclerView, routes: List<RouteModel>?) {

    val adapter = recyclerView.adapter as RouteAdapter

    if (routes != null)
        adapter.submitList(routes)       // For ListAdapter
}