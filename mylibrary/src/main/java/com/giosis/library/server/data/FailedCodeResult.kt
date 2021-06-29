package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FailedCodeResult : Serializable {

    @SerializedName("ResultCode")
    var resultCode = -1

    @SerializedName("ResultMsg")
    var resultMsg = ""

    @SerializedName("ResultObject")
    var resultObject: ArrayList<FailedCode>? = null

    class FailedCode {

        @SerializedName("cd_nm")
        var failedString = ""

        @SerializedName("cd")
        var failedCode = ""
    }
}