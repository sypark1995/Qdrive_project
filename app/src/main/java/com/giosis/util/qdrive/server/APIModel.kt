package com.giosis.util.qdrive.server

import android.text.TextUtils
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class APIModel : JsonModel(), Serializable {

    var resultStr: String? = null  // 서버데이터 전문

    @SerializedName("ResultCode")
    var resultCode: Int = -1

    @SerializedName("ResultMsg")
    var resultMsg: String? = null

    @SerializedName("ResultObject")
    var resultObject: JsonElement? = null        // return data 오는 곳


    //////////////////////////////////////
    //
    @SerializedName("data")
    var data: JsonElement? = null

    @SerializedName("err_code")
    var errCode: String? = null

    @SerializedName("desc")
    var desc: String? = null

    @SerializedName("name")
    var name: String? = null

    ///////////////////////////////////////////


    fun isSuccess(): Boolean {
        return !TextUtils.isEmpty(resultStr) && resultCode == 0 && resultObject != null
    }


    fun isSuccessful(): Boolean {
        return !TextUtils.isEmpty(resultMsg) && resultCode == 0
    }

}