package com.giosis.library.main.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.databinding.ItemMyRouteBinding

class RouteAdapter(private val viewModel: TodayMyRouteViewModel) : ListAdapter<Route, RouteAdapter.ViewHolder>(
        RouteDiffUtil
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemMyRouteBinding>(layoutInflater, viewType, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_my_route
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = getItem(position)
        holder.bind(route)
    }


    inner class ViewHolder(private val binding: ItemMyRouteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: Route) {

            binding.route = route
            binding.executePendingBindings()        // 데이터가 수정되면 즉각 바인딩
        }
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