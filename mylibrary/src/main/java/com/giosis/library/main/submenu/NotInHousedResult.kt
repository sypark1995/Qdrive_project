package com.giosis.library.main.submenu

import com.google.gson.annotations.SerializedName

/**
 * @author krm0219  2018.07.26
 * @editor krm0219 2020.09
 */
class NotInHousedResult {

    @SerializedName("ResultCode")
    var resultCode = -1

    @SerializedName("ResultMsg")
    var resultMsg = ""

    @SerializedName("ResultObject")
    var resultObject: List<NotInHousedList>? = null

    class NotInHousedList {

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
}