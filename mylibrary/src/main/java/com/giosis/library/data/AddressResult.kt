package com.giosis.library.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AddressResult : Serializable {

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