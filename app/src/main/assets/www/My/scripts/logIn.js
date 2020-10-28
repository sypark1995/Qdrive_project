var serverURL;
var appVersion;

function init() {

//	serverURL = localStorage.getItem('serverURL');
//	appVersion = localStorage.getItem('appVersion');
//	console.log("★★★★★★★ " + serverURL + " / " + appVersion);

    //serverURL = "http://staging-qxapi.qxpress.net/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi";
    serverURL = "https://qxapi.qxpress.net/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi"
    appVersion = "3.4.3"

    $("#loginID").val(localStorage.getItem('opId'));
    $("#pw").val(localStorage.getItem('opPasswd'));
    $("#versionCode").text(appVersion);

     if(serverURL.includes("test")) {
         $("#environment").text("test - ");
     } else if(serverURL.includes("staging")) {
         $("#environment").text("staging -");
     }

    document.addEventListener("deviceready", onDeviceReady, false);

    localStorage.clear();

    setButton("btnLogin", getLoginLocation);
    setButton("qdriveLogo", goDeveloper);
}



	var clicked = 0;

function goDeveloper() {

	clicked++;

//	if(clicked == 10) {
//
//    console.log("★★★★★★★ ::  goDeveloper ");
//	 cordova.require("com.giosis.util.qdrive.util.MainActivityStarter").goDeveloper();
//	 clicked = 0;
//	}
}


function onDeviceReady() {

    document.addEventListener("backbutton", onBackKeyDown, false);

    createTable(db, 'SCAN_DELIVERY', 'contr_no, partner_ref_no, invoice_no, stat, rcv_nm, sender_nm, tel_no, hp_no, zip_code, address, img_path, fail_reason, del_memo, rcv_type, driver_memo, punchOut_stat, reg_dt, chg_dt, reg_id, chg_id, delivery_dt, delivery_cnt', function() {
        createTable(db, 'INTEGRATION_LIST', 'contr_no unique, seq_orderby, partner_ref_no, invoice_no, stat, tel_no, hp_no, zip_code, address, self_memo, type, route, sender_nm, rcv_nm, rcv_request,  desired_date, req_qty, req_nm, failed_count, delivery_dt, delivery_cnt, chg_dt, chg_id, reg_dt, reg_id, real_qty, retry_dt, driver_memo, img_path, stat2, fail_reason, desired_time, rcv_type, punchOut_stat, partner_id, cust_no, secret_no_type, secret_no, lat, lng , secure_delivery_yn, parcel_amount, currency, order_type_etc', function() {
            createTable(db, 'REST_DAYS', 'rest_dt, title', function() {
                createTable(db, 'USER_INFO', 'opId unique, last_login_dt, last_logout_dt, auto_upld_yn, auth_1, auth_2, auth_3, auth_4, sort_idx, use_custom', successCallback, failCB_Common);
            }, failCB_Common);
        }, failCB_Common);
    }, failCB_Common);

    // stat2 사용x / desired_time (route=rpc 일때만 사용)
}

var isDBCreate = false;

function successCallback(tx, results) {

    isDBCreate = true;
    console.log("★★★★★★★ ::  DB Success ");
}


function getLoginLocation() {

    console.log("★★★★★★★ ::  getLoginLocation ");

    var Latitude = 0;
    var Longitude = 0;

    try {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(onLocationSuccess, onLocationError, {
                enableHighAccuracy: true,
                timeout: 2000,
                maximumAge: 300
            });
        } else {
            checkAuth(Latitude, Longitude);
        }
    } catch (e) {

        console.log('★★★★★★★ :: getLoginLocation error' + e.message);
        checkAuth(Latitude, Longitude);
    }
}

function onLocationSuccess(position) {

    var Latitude = position.coords.latitude;
    var Longitude = position.coords.longitude;

    // console.log("★★★★★★★ :: getLoginLocation Success  Latitude : " + Latitude + " / " + "Longitude : " + Longitude);
    checkAuth(Latitude, Longitude);
}

function onLocationError(error) {

    var Latitude = 0;
    var Longitude = 0;

    // console.log("★★★★★★★ :: getLoginLocation failed  > " + error.code + '  message : ' + error.message);
    checkAuth(Latitude, Longitude);
}





function checkAuth(Latitude, Longitude) {

    var loginId = $("#loginID").val();
    var password = $("#pw").val();

    if (loginId == "") {
        alert("Please input ID.");
        return;
    }

    if (password == "") {
        alert("Please input Password.");
        return;
    }

    if (!isDBCreate) {

        alert("Sorry, DB encountered a problem. You may need to restart the device.");
        return;
    }


    var result;
    var uuid = device.uuid;
    console.log(isDBCreate + "  ◆◆◆◆ Login > " + loginId + " " + password + " " + uuid);

    var params = new RMSParam();
    params.add("login_id", loginId.trim());
    params.add("password", password);
    params.add("chanel", "QDRIVE"); //QSIGN, QS는 접속 불가 (서비스 변경)
    params.add("ip", "");
    params.add("referer", uuid);
    params.add("vehicle", "");
    params.add("latitude", Latitude);
    params.add("longitude", Longitude);
    params.add("app_id", "QDRIVE");
    params.add("nation_cd", "SG");

    var methodName = "LoginQDRIVE";
    cordova.require("com.giosis.util.qdrive.util.LoadingDialog").show();
    result = RMSHelper.callWebMethod(serverURL, methodName, params.toJson());


    if (result == -9999) { // Network Error
        return;
    }

    console.log("◆◆◆◆ Login Result > " + JSON.stringify(result.ResultObject));

    if (result == null || result.ResultCode != 0) {

        cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();

        if (result.ResultCode == -10) {
            alert("Your Qdrive account has been deactivated.");
        } else if (result.ResultMsg != "") {
            alert(result.ResultMsg);
        } else {
            alert("Sorry, your sign-in information is not valid. Please try again with your correct ID and password.");
        }

        $("#pw").val("");

        cordova.require("com.giosis.util.qdrive.util.SharedPreferencesHelper").setSigninState({
                signInState: false,
                opId: ''
            }, null,
            function(error) {
                cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();
            });
        return;
    }


    // Login Success
    var serverVersion = result.ResultObject.Version;
    console.log('★★★★★★★   version : ' + appVersion + ' / ' + serverVersion)

    if (appVersion < serverVersion) {

        cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();
        alert("Qdrive is updated to v" + serverVersion + " \nYour Qdrive version is v" + appVersion + " \nYour version will be upgraded.");
        cordova.require("com.giosis.util.qdrive.util.MainActivityStarter").goMarket();
        return;
    }


    localStorage.setItem('opId', result.ResultObject.OpId);
    localStorage.setItem('opNm', result.ResultObject.OpNm);
    localStorage.setItem('officeCd', result.ResultObject.OfficeCode);
    localStorage.setItem('officeNm', result.ResultObject.OfficeName);
    localStorage.setItem('DefaultYn', result.ResultObject.DefaultYn);
    localStorage.setItem('opType', result.ResultObject.OpType);
    localStorage.setItem('authNo', result.ResultObject.AuthNo);
    localStorage.setItem('opEmail', result.ResultObject.EpEmail);
    localStorage.setItem('pickupDriverYN', result.ResultObject.PickupDriverYN);
    localStorage.setItem('shuttle_driver_yn', result.ResultObject.shuttle_driver_yn);
    localStorage.setItem('locker_driver_status', result.ResultObject.locker_driver_status);
    localStorage.setItem('version', serverVersion);

    localStorage.setItem('opPasswd', password);
    localStorage.setItem('groupCd', result.ResultObject.GroupNo);
    localStorage.setItem('SmsYn', result.ResultObject.SmsYn);
    localStorage.setItem('DeviceYn', result.ResultObject.DeviceYn);
    localStorage.setItem('login', "1");


    cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();
    deleteTable(db, "USER_INFO", '', setUserId, failCB_Common);
}


function setUserId() {

    console.log("★★★★★★★ ::  setUserId ");

    var columns = 'opId, sort_idx';
    var data = '"' + localStorage.getItem('opId') + '", "0"';
    insertTableData(db, "USER_INFO", columns, data, setRestDays, failCB_Common);
}

function setRestDays() {

    console.log("★★★★★★★ ::  setRestDays  ");

    cordova.require("com.giosis.util.qdrive.util.SharedPreferencesHelper").setSigninState({
            signInState: true,
            opId: localStorage.getItem('opId'),
            opNm: localStorage.getItem('opNm'),
            officeCd: localStorage.getItem('officeCd'),
            officeNm: localStorage.getItem('officeNm'),
            DefaultYn: localStorage.getItem('DefaultYn'),
            opType: localStorage.getItem('opType'),
            authNo: localStorage.getItem('authNo'),
            device_id: device.uuid,
            service_type: 'DLV', //localStorage.getItem('service_type') 사용안함
            opEmail: localStorage.getItem('opEmail'),
            pickupDriverYN: localStorage.getItem('pickupDriverYN'),
            outletDriverYN: localStorage.getItem('shuttle_driver_yn'),
            lockerStatus: localStorage.getItem('locker_driver_status'),
            version: localStorage.getItem('version')
        },
        closeDatabase,
        function(error) {
            cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();
        });
}


function closeDatabase() {

    closeDataBase(db, '"' + localStorage.getItem('opId') + '"', startQdrive);
}

function startQdrive() {

    console.log("★★★★★★★ ::  startQdrive");


        cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();
        console.log("★★★★★★★ ::  startQdrive  setTimeout");

        var sms_yn = localStorage.getItem('SmsYn');
        var device_yn = localStorage.getItem('DeviceYn');

        if (sms_yn == "Y" && device_yn == "Y") {

            cordova.require("com.giosis.util.qdrive.util.MainActivityStarter").start();
            cordova.require("cordova/plugin/android/app").exitApp();
        } else {
            if (device_yn == "N") {

                alert("You have attempted to login from unauthorized mobile phone.\nIf your mobile phone was changed, you have to pass the SMS verification.");
            }
            cordova.require("com.giosis.util.qdrive.util.MainActivityStarter").verify();
            cordova.require("cordova/plugin/android/app").exitApp();

        }
}