package com.giosis.util.qdrive.international

class LoginResult {

    var resultCode = "-1"
    var resultMsg = ""
    lateinit var resultObject: LoginData

    class LoginData {

        var userId = ""
        var nationCode = ""
        var serverVersion = ""

        var userName = ""
        var userEmail = ""
        var officeCode = ""
        var officeName = ""

        var pickupDriver = ""
        var outletDriver = ""
        var lockerStatus = ""

        var smsYn = ""
        var deviceYn = ""

        var defaultYn = ""
        var authNo = ""
    }
}