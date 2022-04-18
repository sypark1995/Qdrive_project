package com.giosis.library.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object Preferences {

    private const val FILENAME = "com.giosis.util.qdrive_preferences"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(FILENAME, Activity.MODE_PRIVATE)
    }

    private val APP_INFO = "app_info"
    private val USER_AGENT = "user_agent"
    private val PREF_KEY_USER_ID = "userId"
    private val PREF_KEY_USER_PW = "userPw"
    private val PREF_KEY_USER_NATION = "userNation"
    private val PREF_KEY_DEVICE_UUID = "deviceUUID"
    private val PREF_KEY_APP_VERSION = "appVersion"

    private val PREF_KEY_USER_NAME = "userName"
    private val PREF_KEY_USER_EMAIL = "userEmail"
    private val PREF_KEY_OFFICE_CODE = "officeCode"
    private val PREF_KEY_OFFICE_NAME = "officeName"

    private val PREF_KEY_IS_PICKUP_DRIVER = "pickupDriver"
    private val PREF_KEY_IS_OUTLET_DRIVER = "outletDriver"
    private val PREF_KEY_OUTLET_LOCKER_STATUS = "lockerStatus"
    private val PREF_KEY_DEFAULT_YN = "default"
    private val PREF_KEY_AUTH_NO = "authNo"

    private val PREF_KEY_LIST_SORT_INDEX = "sortIndex"
    private val PREF_KEY_SCAN_VIBRATION = "scanVibration"
    private val PREF_KEY_LANGUAGE = "PREF_LANGUAGE_SETTING"

    // SG , MY 둘중 하나 만 넣고 빼기로....
    var appInfo: String
        get() = preferences.getString(APP_INFO, "SG").toString()
        set(value) = preferences.edit().putString(APP_INFO, value).apply()

    var userAgent: String
        get() = preferences.getString(USER_AGENT, "").toString()
        set(value) = preferences.edit().putString(USER_AGENT, value).apply()

    var userId: String
        get() = preferences.getString(PREF_KEY_USER_ID, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_USER_ID, value).apply()

    var userPw: String
        get() = preferences.getString(PREF_KEY_USER_PW, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_USER_PW, value).apply()

    // SG / MY / ID
    var userNation: String
        get() = preferences.getString(PREF_KEY_USER_NATION, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_USER_NATION, value).apply()

    var deviceUUID: String
        get() = preferences.getString(PREF_KEY_DEVICE_UUID, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_DEVICE_UUID, value).apply()

    var userName: String
        get() = preferences.getString(PREF_KEY_USER_NAME, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = preferences.getString(PREF_KEY_USER_EMAIL, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_USER_EMAIL, value).apply()

    var officeCode: String
        get() = preferences.getString(PREF_KEY_OFFICE_CODE, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_OFFICE_CODE, value).apply()

    var officeName: String
        get() = preferences.getString(PREF_KEY_OFFICE_NAME, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_OFFICE_NAME, value).apply()

    var pickupDriver: String
        get() = preferences.getString(PREF_KEY_IS_PICKUP_DRIVER, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_IS_PICKUP_DRIVER, value).apply()

    var outletDriver: String
        get() = preferences.getString(PREF_KEY_IS_OUTLET_DRIVER, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_IS_OUTLET_DRIVER, value).apply()

    var lockerStatus: String
        get() = preferences.getString(PREF_KEY_OUTLET_LOCKER_STATUS, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_OUTLET_LOCKER_STATUS, value).apply()

    var sortIndex: Int
        get() = preferences.getInt(PREF_KEY_LIST_SORT_INDEX, 0)
        set(value) = preferences.edit().putInt(PREF_KEY_LIST_SORT_INDEX, value).apply()

    var scanVibration: String
        get() = preferences.getString(PREF_KEY_SCAN_VIBRATION, "OFF").toString()
        set(value) = preferences.edit().putString(PREF_KEY_SCAN_VIBRATION, value).apply()

    var default: String
        get() = preferences.getString(PREF_KEY_DEFAULT_YN, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_DEFAULT_YN, value).apply()

    var authNo: String
        get() = preferences.getString(PREF_KEY_AUTH_NO, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_AUTH_NO, value).apply()


    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_MALAY = "ms"
    const val LANGUAGE_INDONESIA = "in"

    var language: String
        get() = preferences.getString(PREF_KEY_LANGUAGE, LANGUAGE_ENGLISH).toString()
        set(value) {
            // commit 으로 처리 해야 함.!!!!! 바로 리스타트 되면서 적용안되는 케이스 있음
            preferences.edit().putString(PREF_KEY_LANGUAGE, value).commit()
        }

    // 202008.  Auto Logout
    private val PREF_KEY_AUTO_LOGOUT = "autoLogout"
    private val PREF_KEY_AUTO_LOGOUT_TIME = "autoLogoutTime"
    private val PREF_KEY_AUTO_LOGOUT_SETTING = "autoLogoutSetting"

    var autoLogout: Boolean
        get() = preferences.getBoolean(PREF_KEY_AUTO_LOGOUT, false)
        set(value) = preferences.edit().putBoolean(PREF_KEY_AUTO_LOGOUT, value).apply()

    var autoLogoutTime: String
        get() = preferences.getString(PREF_KEY_AUTO_LOGOUT_TIME, "23:59").toString()
        set(value) = preferences.edit().putString(PREF_KEY_AUTO_LOGOUT_TIME, value).apply()

    var autoLogoutSetting: Boolean
        get() = preferences.getBoolean(PREF_KEY_AUTO_LOGOUT_SETTING, false)
        set(value) = preferences.edit().putBoolean(PREF_KEY_AUTO_LOGOUT_SETTING, value).apply()


    // 202009.  Developer Mode, change ServerURL
    private val PREF_KEY_DEVELOPER_MODE = "developerMode"
    private val PREF_KEY_SERVER_URL = "serverURL"

    var serverURL: String
        get() = preferences.getString(PREF_KEY_SERVER_URL, DataUtil.SERVER_REAL).toString()
        set(value) = preferences.edit().putString(PREF_KEY_SERVER_URL, value).apply()


    var developerMode: Boolean
        get() = preferences.getBoolean(PREF_KEY_DEVELOPER_MODE, false)
        set(value) = preferences.edit().putBoolean(PREF_KEY_DEVELOPER_MODE, value).apply()

    // 202012. Failed Code
    private val PREF_KEY_DELIVERY_FAILED_CODE = "dFailedCode"
    private val PREF_KEY_PICKUP_FAILED_CODE = "pFailedCode"

    var dFailedCode: String
        get() = preferences.getString(PREF_KEY_DELIVERY_FAILED_CODE, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_DELIVERY_FAILED_CODE, value).apply()

    var pFailedCode: String
        get() = preferences.getString(PREF_KEY_PICKUP_FAILED_CODE, "").toString()
        set(value) = preferences.edit().putString(PREF_KEY_PICKUP_FAILED_CODE, value).apply()

    // 202104. xRoute Server
    private val PREF_KEY_XROUTE_SERVER_URL = "xRouteServerURL"

    var xRouteServerURL: String
        get() = preferences.getString(PREF_KEY_XROUTE_SERVER_URL, DataUtil.XROUTE_SERVER_REAL).toString()
        set(value) = preferences.edit().putString(PREF_KEY_XROUTE_SERVER_URL, value).apply()

    private val PREF_KEY_GPS_MODE = "gpsMode"
    var gpsMode: String
        get() = preferences.getString(PREF_KEY_GPS_MODE, "REAL").toString()
        set(value) = preferences.edit().putString(PREF_KEY_GPS_MODE, value).apply()

    private val PREF_KEY_GPS_TEST_VALUE = "gpsTestValue"
    var gpsTestValue: String
        get() = preferences.getString(PREF_KEY_GPS_TEST_VALUE, "0, 0").toString()
        set(value) = preferences.edit().putString(PREF_KEY_GPS_TEST_VALUE, value).apply()
}
