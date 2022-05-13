package com.giosis.util.qdrive.singapore.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.data.NoticeResult
import com.giosis.util.qdrive.singapore.BaseRecyclerAdapter
import com.giosis.util.qdrive.singapore.ListViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.BR

class NoticeAdapter(private val viewModel: NoticeViewModel) :
    BaseRecyclerAdapter<NoticeAdapter.ViewHolder, NoticeResult.NoticeItem>(null) {

    override fun getListModel(): ListViewModel<NoticeResult.NoticeItem> {
        return viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_notice,
            parent,
            false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(viewModel, position)
    }

    inner class ViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: NoticeViewModel, position: Int) {
            binding.setVariable(BR.viewModel, viewModel)
            binding.setVariable(BR.item, viewModel.items.value!![position])
            binding.setVariable(BR.position, position)

            binding.executePendingBindings()
        }
    }


}