package com.giosis.library.server

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class APIModel : JsonModel(), Serializable {

    @SerializedName("ResultCode")
    var resultCode: Int = -1

    @SerializedName("ResultMsg")
    var resultMsg: String? = null

    @SerializedName("ResultObject")
    var resultObject: JsonElement? = null


    //
    @SerializedName("err_code")
    var errCode: String? = null

    @SerializedName("desc")
    var desc: String? = null

    @SerializedName("data")
    var data: JsonElement? = null
}