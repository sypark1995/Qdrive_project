package com.giosis.util.qdrive.singapore.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DefaultResult : Serializable {

    @SerializedName("ResultCode")
    var resultCode = "-1"

    @SerializedName("ResultMsg")
    var resultMsg = ""
}