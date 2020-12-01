package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

class CustomSellerInfo : Serializable {

    @SerializedName("rows")
    var resultRows: ArrayList<SellerInfo>? = null

    class SellerInfo {

        @SerializedName("cust_no")
        var cust_no = "-1"

        @SerializedName("cust_nm")
        var cust_nm = ""

        @SerializedName("tel_no")
        var tel_no = ""

        @SerializedName("hp_no")
        var hp_no = ""

        @SerializedName("login_id")
        var login_id = ""

        @SerializedName("email")
        var email = ""

        @SerializedName("seller_id")
        var seller_id = ""

        @SerializedName("seller_no")
        var seller_no = ""

        @SerializedName("svc_nation_cd")
        var svc_nation_cd = ""

        @SerializedName("cust_type")
        var cust_type = ""

        @SerializedName("payment_type")
        var payment_type = ""

        @SerializedName("nation_cd")
        var nation_cd = ""

        @SerializedName("zip_code")
        var zip_code = ""

        @SerializedName("addr_front")
        var addr_front = ""

        @SerializedName("addr_last")
        var addr_last = ""

        @SerializedName("addr_no")
        var addr_no = ""

        @SerializedName("addr_nm")
        var addr_nm = ""

        @SerializedName("recv_nm")
        var recv_nm = ""

        @SerializedName("cust_hub_id")
        var cust_hub_id = ""

        @SerializedName("cust_gst_no")
        var cust_gst_no = ""

        @SerializedName("member_kind")
        var member_kind = ""
    }
}