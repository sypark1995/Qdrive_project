package com.giosis.util.qdrive.singapore.message

import com.google.gson.annotations.SerializedName

class MessageSendResult {
    @SerializedName("ResultCode")
    var resultCode = "-1"

    @SerializedName("ResultMsg")
    var resultMsg = ""
}