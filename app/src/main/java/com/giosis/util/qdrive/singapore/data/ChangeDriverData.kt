package com.giosis.util.qdrive.singapore.data

import com.google.gson.annotations.SerializedName

data class ChangeDriverData(
    @SerializedName("contr_no")
    val contr_no: String,

    @SerializedName("tracking_no")
    val tracking_no: String,

    @SerializedName("del_driver_id")
    val del_driver_id: String,

    @SerializedName("status")
    val status: String
)