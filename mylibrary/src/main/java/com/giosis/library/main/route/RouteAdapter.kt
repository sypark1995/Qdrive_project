package com.giosis.library.main.route

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.BR
import com.giosis.library.BaseRecyclerAdapter
import com.giosis.library.ListViewModel
import com.giosis.library.R
import com.giosis.library.databinding.ItemMyRouteBinding
import com.giosis.library.pickup.AddressDialogViewModel

class RouteAdapter(private val viewModel: TodayMyRouteViewModel) : BaseRecyclerAdapter<RouteAdapter.ViewHolder, Route>(RouteDiffUtil) {

    /*
    class RouteAdapter(private val viewModel: TodayMyRouteViewModel) : ListAdapter<Route, RouteAdapter.ViewHolder>(
        RouteDiffUtil
) {*/

    var routes: List<Route> = listOf()

    override fun getListModel(): ListViewModel<Route> {
       return viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemMyRouteBinding>(layoutInflater, viewType, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_my_route
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(viewModel, position)
    }


    inner class ViewHolder(private val binding: ItemMyRouteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: TodayMyRouteViewModel, position: Int) {

            if(position == 0) {

                binding.btnItemRouteNavigator.visibility = View.VISIBLE
                binding.textItemRouteJobCount.visibility = View.GONE
                binding.layoutItemRouteTypeInfoContainer.visibility = View.GONE
            } else {

                binding.btnItemRouteNavigator.visibility = View.GONE
                binding.textItemRouteJobCount.visibility = View.VISIBLE
                binding.layoutItemRouteTypeInfoContainer.visibility = View.VISIBLE
            }


            binding.setVariable(BR.viewModel, viewModel)
            binding.setVariable(BR.item, viewModel.items.value!![position])
            binding.setVariable(BR.position, position)


            binding.executePendingBindings()
        }
    }


    fun setData(routes: List<Route>) {

        this.routes = routes
        notifyDataSetChanged()
    }


    companion object RouteDiffUtil : DiffUtil.ItemCallback<Route>() {

        override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
            // 각 아이템들의 고유한 값을 비교해야 한다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem == newItem
        }
    }
}