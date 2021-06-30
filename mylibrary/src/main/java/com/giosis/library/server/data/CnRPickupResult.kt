package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName

class CnRPickupResult {
    @SerializedName("contr_no")
    var contrNo = ""

    @SerializedName("partner_ref_no")
    var partnerRefNo = ""

    @SerializedName("invoice_no")
    var invoiceNo = ""

    @SerializedName("stat")
    var stat = ""

    @SerializedName("req_nm")
    var reqName = ""

    @SerializedName("tel_no")
    var telNo = ""

    @SerializedName("hp_no")
    var hpNo = ""

    @SerializedName("zip_code")
    var zipCode = ""

    @SerializedName("address")
    var address = ""

    @SerializedName("pickup_hopeday")
    var pickupHopeDay = ""

    @SerializedName("del_memo")
    var delMemo = ""

    @SerializedName("driver_memo")
    var driverMemo = ""

    @SerializedName("fail_reason")
    var failReason = ""

    @SerializedName("qty")
    var qty = ""

    @SerializedName("route")
    var route = ""

    @SerializedName("cust_no")
    var cust_no = ""
}