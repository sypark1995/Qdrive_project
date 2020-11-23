package com.giosis.library

import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<VH : RecyclerView.ViewHolder, T>
    : RecyclerView.Adapter<VH>() {

    abstract fun getListModel(): ListViewModel<T>

    fun getItem(position: Int): T {
        return getListModel().getItem(position)
    }

    override fun getItemCount(): Int {
        return getListModel().getItemCount()
    }


}