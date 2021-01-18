package com.giosis.library.list.delivery

import com.google.gson.annotations.SerializedName

class QRCodeResult {
    @SerializedName("ResultCode")
    var result_code: String? = null

    @SerializedName("ResultMsg")
    var result_msg: String? = null

    @SerializedName("ResultObject")
    var qrcode_data: String? = null
}