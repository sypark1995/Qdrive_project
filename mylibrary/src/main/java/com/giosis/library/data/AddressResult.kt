package com.giosis.library.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AddressResult : Serializable {

    @SerializedName("ResultCode")
    var resultCode = "-1"

    @SerializedName("ResultMsg")
    var resultMsg = ""

    @SerializedName("ResultObject")
    var resultObject: AddressResultObject? = null

    class AddressResultObject {

        @SerializedName("rows")
        var resultRows: java.util.ArrayList<AddressItem>? = null

        class AddressItem {

            @SerializedName("zipcode")
            var zipCode = ""

            @SerializedName("front_address")
            var frontAddress = ""
//
//        @SerializedName("i_addr_no")
//        var addressNo = ""
//
//        @SerializedName("number")
//        var number = ""

        }
    }
}