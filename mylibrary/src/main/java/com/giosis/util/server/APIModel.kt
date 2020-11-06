package com.giosis.util.server

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class APIModel : JsonModel(), Serializable {

    @SerializedName("ResultCode")
    var resultCode: Int = -1

    @SerializedName("ResultObject")
    var resultObject: JsonElement? = null


}