package com.giosis.util.qdrive.singapore.barcodescanner

import com.google.gson.annotations.SerializedName
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false, name = "StdCustomResultOfChgDelDriverResult")
class ChangeDriverResult {

    @SerializedName("ResultCode")
    @Element(name = "ResultCode", required = false)
    var resultCode = -1

    @SerializedName("ResultMsg")
    @Element(name = "ResultMsg", required = false)
    var resultMsg = ""

    @Element(required = false, name = "ResultObject")
    var resultObject: Data? = null

    @Root(strict = false, name = "ResultObject")
    class Data {

        @SerializedName("contr_no")
        @Element(name = "contr_no", required = false)
        val contrNo = ""

        @SerializedName("tracking_no")
        @Element(name = "tracking_no", required = false)
        val trackingNo = ""

        @SerializedName("del_driver_id")
        @Element(name = "del_driver_id", required = false)
        val currentDriver = ""

        @SerializedName("status")
        @Element(name = "status", required = false)
        var status = ""
    }
}