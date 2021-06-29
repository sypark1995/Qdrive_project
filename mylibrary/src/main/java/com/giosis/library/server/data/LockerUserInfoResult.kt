package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*


class LockerUserInfoResult : Serializable {

    @SerializedName("ResultCode")
    var resultCode = "-1"

    @SerializedName("ResultMsg")
    var resultMsg = ""

    @SerializedName("ResultObject")
    var resultObject: LockerResultObject? = null

    class LockerResultObject {

        @SerializedName("rows")
        var resultRows: ArrayList<LockerResultRow>? = null


        class LockerResultRow {
            @SerializedName("lsp_user_key")
            var user_key: String? = null

            @SerializedName("lsp_user_status")
            var user_status: String? = null

            @SerializedName("hp_no")
            var user_mobile: String? = null

            @SerializedName("lsp_user_expired_date")
            var user_expiry_date: String? = null

            @SerializedName("lst_user_id")
            var user_id: String? = null
        }
    }
}