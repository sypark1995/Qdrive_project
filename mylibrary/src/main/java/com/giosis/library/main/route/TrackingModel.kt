package com.giosis.library.main.route

import com.google.gson.annotations.SerializedName


data class TrackingModel(
        @SerializedName("stat") val stat: String,
        @SerializedName("invoice_no") val trackingNo: String,
        @SerializedName("contr_no") val contrNo: String
)