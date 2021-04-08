package com.giosis.library

import android.graphics.Bitmap
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.main.route.Route
import com.giosis.library.main.route.RouteAdapter


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
            TODO("Not yet implemented")
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            result.value = position
            //  result.value = parent!!.getItemAtPosition(position)as String
        }
    }
}

@BindingAdapter("routeData")
fun bindRouteData(recyclerView: RecyclerView, routes: ArrayList<Route>?) {

    val adapter = recyclerView.adapter as RouteAdapter

    if (routes != null)
        adapter.submitList(routes)       // For ListAdapter
}

//@BindingAdapter("viewModel")
//fun setViewModel(view: RecyclerView, vm: TodayMyRouteViewModel) {
//    view.adapter?.run {
//        if (this is RouteAdapter) this.setViewModel(vm)
//    } ?: run {
//        RouteAdapter().apply {
//            view.adapter = this
//            this.setViewModel(vm)
//        }
//    }
//}