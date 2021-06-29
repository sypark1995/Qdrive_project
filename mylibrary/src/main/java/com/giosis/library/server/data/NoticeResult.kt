package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class NoticeResult : Serializable {

    @SerializedName("ResultCode")
    var resultCode = "-1"

    @SerializedName("ResultMsg")
    var resultMsg = ""

    @SerializedName("ResultObject")
    var resultObject: ArrayList<NoticeItem>? = null

    class NoticeItem {

        @SerializedName("nid")
        var seqNo = ""

        @SerializedName("title")
        var title = ""

        @SerializedName("reg_dt_long")
        var date = ""

        @SerializedName("reg_dt_short")
        var shortDate = ""

        @SerializedName("prevnid")
        var prevNo = ""

        @SerializedName("nextnid")
        var nextNo = ""
    }
}