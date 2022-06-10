package com.giosis.util.qdrive.singapore.data

import com.google.gson.annotations.SerializedName

data class CaptureData(
    @SerializedName("contr_no")
    val contr_no: String,

    @SerializedName("partner_ref_no")
    val partner_ref_no: String,

    @SerializedName("invoice_no")
    val invoice_no: String,

    @SerializedName("stat")
    val stat: String,

    @SerializedName("rcv_nm")
    val rcv_nm: String,

    @SerializedName("tel_no")
    val tel_no: String,

    @SerializedName("hp_no")
    val hp_no: String,

    @SerializedName("zip_code")
    val zip_code: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("sender_nm")
    val sender_nm: String,

    @SerializedName("del_memo")
    val del_memo: String,

    @SerializedName("driver_memo")
    val driver_memo: String,

    @SerializedName("fail_reason")
    val fail_reason: String,

    @SerializedName("delivery_first_date")
    val delivery_first_date: String,

    @SerializedName("route")
    val route: String,

    @SerializedName("secret_no_type")
    val secret_no_type: String,

    @SerializedName("secret_no")
    val secret_no: String,

    @SerializedName("secure_delivery_yn")
    val secure_delivery_yn: String,

    @SerializedName("parcel_amount")
    val parcel_amount: String,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("order_type_etc")
    val order_type_etc: String,

    @SerializedName("lat_lng")
    val lat_lng: String,

    @SerializedName("high_amount_yn")
    val high_amount_yn: String,

    @SerializedName("receive_state")
    val receive_state: String,

    @SerializedName("receive_city")
    val receive_city: String,

    @SerializedName("receive_street")
    val receive_street: String,

    @SerializedName("order_type")
    val order_type: String,
)