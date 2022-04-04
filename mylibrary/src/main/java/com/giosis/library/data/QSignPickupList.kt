package com.giosis.library.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class QSignPickupList : Serializable {

    @SerializedName("contr_no")
    val contrNo = ""

    @SerializedName("partner_ref_no")
    val partnerRefNo = ""

    @SerializedName("invoice_no")
    val invoiceNo = ""

    @SerializedName("stat")
    val stat = ""

    @SerializedName("req_nm")
    val reqName = ""

    @SerializedName("partner_id")
    val partnerID = ""

    @SerializedName("req_dt")
    val reqDate = ""

    @SerializedName("tel_no")
    val telNo = ""

    @SerializedName("hp_no")
    val hpNo = ""

    @SerializedName("zip_code")
    val zipCode = ""

    @SerializedName("address")
    val address = ""

    @SerializedName("del_memo")
    val delMemo = ""

    @SerializedName("driver_memo")
    val driverMemo = ""

    @SerializedName("fail_reason")
    val failReason = ""

    @SerializedName("pickup_hopeday")
    val pickupHopeDay = ""

    @SerializedName("qty")
    val qty = ""

    @SerializedName("route")
    val route = ""

    @SerializedName("secret_no_type")
    val secretNoType = ""

    @SerializedName("secret_no")
    val secretNo = ""

    @SerializedName("cust_no")
    val custNo = ""

    @SerializedName("ref_pickup_no")
    val ref_pickup_no = ""

    @SerializedName("lat_lng")
    val lat_lng = ""

    @SerializedName("sender_state")
    val state = ""

    @SerializedName("sender_city")
    val city = ""

    @SerializedName("sender_street")
    val street = ""

    val pickupHopeTime: String
        get() = reqDate.substring(10, reqDate.length)
}