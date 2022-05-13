package com.giosis.util.qdrive.singapore

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<VH : RecyclerView.ViewHolder, T>(diffCallback: DiffUtil.ItemCallback<T>?)
    : RecyclerView.Adapter<VH>() {

    abstract fun getListModel(): ListViewModel<T>

    fun getItem(position: Int): T {
        return getListModel().getItem(position)
    }

    override fun getItemCount(): Int {
        return getListModel().getItemCount()
    }
}