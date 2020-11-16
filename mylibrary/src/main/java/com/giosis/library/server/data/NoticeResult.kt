package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class NoticeResults : Serializable {

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

        @SerializedName("prevnid")
        var prevNo = ""

        @SerializedName("nextnid")
        var nextNo = ""
    }
}