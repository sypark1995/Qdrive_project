package com.giosis.library.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.*
import com.giosis.library.server.data.NoticeResult

class NoticeAdapter(private val viewModel: NoticeViewModel) : BaseRecyclerAdapter<NoticeAdapter.ViewHolder, NoticeResult.NoticeItem>(null) {

    override fun getListModel(): ListViewModel<NoticeResult.NoticeItem> {
        return viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeAdapter.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout.item_notice, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: NoticeAdapter.ViewHolder, position: Int) {
        holder.bind(viewModel, position)
    }

    inner class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: NoticeViewModel, position: Int) {
            binding.setVariable(BR.viewModel, viewModel)
            binding.setVariable(BR.item, viewModel.items.value!![position])
            binding.setVariable(BR.position, position)

            binding.executePendingBindings()
        }
    }



}