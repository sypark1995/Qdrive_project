package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName

class NotInHousedResult {

    @SerializedName("invoice_no")
    var invoiceNo = ""

    @SerializedName("req_nm")
    var reqName = ""

    @SerializedName("partner_ref_no")
    var partner_id = ""

    @SerializedName("zip_code")
    var zipCode = ""

    @SerializedName("address")
    var address = ""

    @SerializedName("pickup_cmpl_dt")
    var pickup_date: String = ""

    @SerializedName("qty")
    var real_qty = ""

    @SerializedName("not_processed_qty")
    var not_processed_qty = ""

    @SerializedName("qdriveOutstandingInhousedPickupLists")
    var subLists: List<NotInHousedSubList>? = null

    class NotInHousedSubList {

        @SerializedName("packing_no")
        var packingNo = ""

        @SerializedName("purchased_amt")
        var purchasedAmount = ""

        @SerializedName("purchased_currency")
        var purchaseCurrency = ""
    }
}