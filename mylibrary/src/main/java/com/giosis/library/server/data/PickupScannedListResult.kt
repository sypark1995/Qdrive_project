package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName

class PickupScannedListResult {

    @SerializedName("ResultCode")
    var resultCode = -1

    @SerializedName("ResultMsg")
    var resultMsg = ""

    @SerializedName("ResultObject")
    var resultObject: List<ScanPackingList>? = null

    class ScanPackingList {

        @SerializedName("packing_no")
        val packingNo = ""

        @SerializedName("pickup_no")
        val pickupNo = ""

        @SerializedName("reg_dt")
        val regDt = ""

        @SerializedName("op_id")
        val opID = ""
    }
}