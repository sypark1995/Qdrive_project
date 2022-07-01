package com.giosis.util.qdrive.singapore.pickup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.BR
import com.giosis.util.qdrive.singapore.BaseRecyclerAdapter
import com.giosis.util.qdrive.singapore.ListViewModel
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.data.AddressResult

class AddressAdapter(private val viewModel: AddressDialogViewModel) :
    BaseRecyclerAdapter<AddressAdapter.ViewHolder, AddressResult.AddressItem>(
        null
    ) {

    override fun getListModel(): ListViewModel<AddressResult.AddressItem> {
        return viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_search_address,
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

        fun bind(viewModel: AddressDialogViewModel, position: Int) {

            binding.setVariable(BR.viewModel, viewModel)
            binding.setVariable(BR.item, viewModel.items.value!![position])
            binding.setVariable(BR.position, position)

            binding.executePendingBindings()
        }
    }
}