package com.giosis.library.message

import com.google.gson.annotations.SerializedName

class MessageSendResult {
    @SerializedName("ResultCode")
    var resultCode = "-1"

    @SerializedName("ResultMsg")
    var resultMsg = ""
}