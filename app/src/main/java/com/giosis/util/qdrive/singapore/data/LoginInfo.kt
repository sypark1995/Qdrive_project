package com.giosis.util.qdrive.singapore.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LoginInfo : Serializable {

    @SerializedName("OpNo")
    var opNo: String? = null

    @SerializedName("OpId")
    var opId: String? = null

    @SerializedName("OpNm")
    var opNm: String? = null

    @SerializedName("OpType")
    var opType: String? = null

    @SerializedName("OfficeCode")
    var officeCode: String? = null

    @SerializedName("OfficeName")
    var officeName: String? = null

    @SerializedName("AgentCode")
    var agentCode: String? = null

    @SerializedName("AdminAuthYn")
    var adminAuthYn: String? = null

    @SerializedName("DsmAuthYn")
    var dsmAuthYn: String? = null

    @SerializedName("CustNo")
    var custNo: String? = null

    @SerializedName("Email")
    var email: String? = null

    @SerializedName("QLPSCustNo")
    var QLPSCustNo: String? = null

    @SerializedName("DefaultYn")
    var defaultYn: String? = null

    @SerializedName("GroupNo")
    var groupNo: String? = null

    @SerializedName("AuthNo")
    var authNo: String? = null

    @SerializedName("Version")
    var version: String? = null

    @SerializedName("SmsYn")
    var smsYn: String? = null

    @SerializedName("EpEmail")
    var epEmail: String? = null

    @SerializedName("PickupDriverYN")
    var pickupDriverYN: String? = null

    @SerializedName("shuttle_driver_yn")
    var shuttle_driver_yn: String? = null

    @SerializedName("locker_driver_status")
    var locker_driver_status: String? = null

    @SerializedName("country_code")
    var country_code: String? = null

    @SerializedName("DeviceYn")
    var deviceYn: String? = null
}