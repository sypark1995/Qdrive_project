package com.giosis.library.main.route

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.giosis.library.BR
import com.google.gson.annotations.SerializedName

data class RouteData(
        @SerializedName("duration") val duration: String,
        @SerializedName("distance") val distance: String,
        @SerializedName("trip_cnt") val trip_cnt: String,
        @SerializedName("maps") val maps: String,
        @SerializedName("trip_items") val routeList: ArrayList<Route>
)

data class Route(
        @SerializedName("next_trip_duration") val next_trip_duration: String,
        @SerializedName("next_trip_distance") val next_trip_distance: String,
        @SerializedName("order_type") val order_type: String,
        @SerializedName("name") val name: String,
        @SerializedName("zip_code") val zip_code: String,
        @SerializedName("address") val address: String,
        @SerializedName("contents") val contents: ArrayList<String>,
        @SerializedName("clickable") val clickable: String
)