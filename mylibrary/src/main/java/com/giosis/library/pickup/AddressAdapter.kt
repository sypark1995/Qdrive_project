package com.giosis.library.pickup

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.BR
import com.giosis.library.BaseRecyclerAdapter
import com.giosis.library.ListViewModel
import com.giosis.library.R
import com.giosis.library.server.data.AddressResult

class AddressAdapter(private val viewModel: AddressDialogViewModel)
    : BaseRecyclerAdapter<AddressAdapter.ViewHolder, AddressResult.AddressResultObject.AddressItem>() {

    override fun getListModel(): ListViewModel<AddressResult.AddressResultObject.AddressItem> {
        return viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressAdapter.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout.item_search_addr, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: AddressAdapter.ViewHolder, position: Int) {
        holder.bind(viewModel, position)
    }


    inner class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: AddressDialogViewModel, position: Int) {

            Log.e("krm0219", "Adapter  $position //  ${viewModel.items.value!![position].zipCode}")


            binding.setVariable(BR.viewModel, viewModel)
            binding.setVariable(BR.item, viewModel.items.value!![position])
            binding.setVariable(BR.position, position)

            binding.executePendingBindings()
        }
    }
}