package com.giosis.library.main.route

import com.google.gson.annotations.SerializedName
import java.util.*


// xRoute
data class RouteData(
        @SerializedName("duration") val duration: String,
        @SerializedName("distance") val distance: String,
        @SerializedName("trip_cnt") val tripCount: String,
        @SerializedName("maps") val maps: ArrayList<String>,
        @SerializedName("trip_items") val routeList: ArrayList<RouteModel>
) {

    fun getTotalHour(): String {

        val totalTime = (duration.toDouble() / 60).toInt()
        return totalTime.div(60).toString()
    }

    fun getTotalMin(): String {

        val totalTime = (duration.toDouble() / 60).toInt()
        return String.format("%02d", totalTime.rem(60))
    }

    fun getDistanceKm(): String {

        return String.format("%.1f", distance.toDouble() / 1000)
    }
}

data class RouteModel(
        @SerializedName("next_trip_duration") val next_trip_duration: String,
        @SerializedName("next_trip_distance") val next_trip_distance: String,
        @SerializedName("name") val name: String,
        @SerializedName("zip_code") val zip_code: String,
        @SerializedName("address") val address: String,
        @SerializedName("contents") val contents: ArrayList<String>
) {

    var nextTripDate = ""       // 다음 trip 까지의 날짜 시간 yyyy-MM-dd HH:mm
    var nextTripTime = ""       // 다음 trip 까지의 시간 HH:mm
    var estimatedTime = ""

    fun getItemDistanceKm(): String {

        return String.format("%.1f", next_trip_distance.toDouble() / 1000) + "Km"
    }

    fun getContent(): String {

        return contents.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "\n")
    }


}