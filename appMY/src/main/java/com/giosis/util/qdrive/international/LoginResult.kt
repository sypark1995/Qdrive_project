package com.giosis.util.qdrive.international

import com.google.gson.annotations.SerializedName

class LoginResult {

    var resultCode = "-1"
    var resultMsg = ""
    lateinit var resultObject: LoginData

    class LoginData {

        @SerializedName("OpId")
        var userId = ""

        @SerializedName("country_code")
        var nationCode = ""

        @SerializedName("Version")
        var serverVersion = ""

        @SerializedName("OpNm")
        var userName = ""

        @SerializedName("EpEmail")
        var userEmail = ""

        @SerializedName("OfficeCode")
        var officeCode = ""

        @SerializedName("OfficeName")
        var officeName = ""

        @SerializedName("PickupDriverYN")
        var pickupDriver = ""

        @SerializedName("shuttle_driver_yn")
        var outletDriver = ""

        @SerializedName("locker_driver_status")
        var lockerStatus = ""

        @SerializedName("SmsYn")
        var smsYn = ""

        @SerializedName("DeviceYn")
        var deviceYn = ""

        @SerializedName("DefaultYn")
        var defaultYn = ""

        @SerializedName("AuthNo")
        var authNo = ""
    }
}