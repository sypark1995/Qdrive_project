package com.giosis.library.main.route

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.databinding.ItemMyRouteBinding

class RouteAdapter(private val viewModel: TodayMyRouteViewModel) :
    ListAdapter<RouteModel, RouteAdapter.ViewHolder>(
        RouteDiffUtil
    ) {

    var routes: List<RouteModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ItemMyRouteBinding>(layoutInflater, viewType, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_my_route
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(viewModel, position)
    }


    inner class ViewHolder(private val binding: ItemMyRouteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: TodayMyRouteViewModel, position: Int) {

            val route = getItem(position)

            Log.e("route", "Pos $position  // data ${route.next_trip_distance}")
            binding.position = position
            binding.route = route
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }


    companion object RouteDiffUtil : DiffUtil.ItemCallback<RouteModel>() {

        override fun areItemsTheSame(oldItem: RouteModel, newItem: RouteModel): Boolean {
            // 각 아이템들의 고유한 값을 비교해야 한다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RouteModel, newItem: RouteModel): Boolean {
            return oldItem == newItem
        }
    }

    fun setRouteList(routes: List<RouteModel>) {

        this.routes = routes
        notifyDataSetChanged()
    }
}
