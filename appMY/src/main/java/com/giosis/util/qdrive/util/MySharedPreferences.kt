package com.giosis.util.qdrive.util

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class MySharedPreferences(context: Context) {

    private val PREFS_FILENAME = "com.giosis.util.qdrive_preferences"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

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


    var userId: String
        get() = prefs.getString(PREF_KEY_USER_ID, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_USER_ID, value).apply()

    var userPw: String
        get() = prefs.getString(PREF_KEY_USER_PW, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_USER_PW, value).apply()

    var userNation: String
        get() = prefs.getString(PREF_KEY_USER_NATION, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_USER_NATION, value).apply()

    var deviceUUID: String
        get() = prefs.getString(PREF_KEY_DEVICE_UUID, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_DEVICE_UUID, value).apply()

    var appVersion: String
        get() = prefs.getString(PREF_KEY_APP_VERSION, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_APP_VERSION, value).apply()


    var userName: String
        get() = prefs.getString(PREF_KEY_USER_NAME, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(PREF_KEY_USER_EMAIL, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_USER_EMAIL, value).apply()

    var officeCode: String
        get() = prefs.getString(PREF_KEY_OFFICE_CODE, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_OFFICE_CODE, value).apply()

    var officeName: String
        get() = prefs.getString(PREF_KEY_OFFICE_NAME, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_OFFICE_NAME, value).apply()


    var pickupDriver: String
        get() = prefs.getString(PREF_KEY_IS_PICKUP_DRIVER, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_IS_PICKUP_DRIVER, value).apply()

    var outletDriver: String
        get() = prefs.getString(PREF_KEY_IS_OUTLET_DRIVER, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_IS_OUTLET_DRIVER, value).apply()

    var lockerStatus: String
        get() = prefs.getString(PREF_KEY_OUTLET_LOCKER_STATUS, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_OUTLET_LOCKER_STATUS, value).apply()


    var sortIndex: Int
        get() = prefs.getInt(PREF_KEY_LIST_SORT_INDEX, 0)
        set(value) = prefs.edit().putInt(PREF_KEY_LIST_SORT_INDEX, value).apply()

    var scanVibration: String
        get() = prefs.getString(PREF_KEY_SCAN_VIBRATION, "OFF").toString()
        set(value) = prefs.edit().putString(PREF_KEY_SCAN_VIBRATION, value).apply()


    var default: String
        get() = prefs.getString(PREF_KEY_DEFAULT_YN, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_DEFAULT_YN, value).apply()

    var authNo: String
        get() = prefs.getString(PREF_KEY_AUTH_NO, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_AUTH_NO, value).apply()

    // 2020.08.  Auto Logout
    private val PREF_KEY_AUTO_LOGOUT = "autoLogout"
    private val PREF_KEY_AUTO_LOGOUT_TIME = "autoLogoutTime"
    private val PREF_KEY_AUTO_LOGOUT_SETTING = "autoLogoutSetting"

    var autoLogout: Boolean
        get() = prefs.getBoolean(PREF_KEY_AUTO_LOGOUT, false)
        set(value) = prefs.edit().putBoolean(PREF_KEY_AUTO_LOGOUT, value).apply()

    var autoLogoutTime: String
        get() = prefs.getString(PREF_KEY_AUTO_LOGOUT_TIME, "23:59").toString()
        set(value) = prefs.edit().putString(PREF_KEY_AUTO_LOGOUT_TIME, value).apply()

    var autoLogoutSetting: Boolean
        get() = prefs.getBoolean(PREF_KEY_AUTO_LOGOUT_SETTING, false)
        set(value) = prefs.edit().putBoolean(PREF_KEY_AUTO_LOGOUT_SETTING, value).apply()

    // 2020.09. Developer Mode
    private val PREF_KEY_DEVELOPER_MODE = "developerMode"
    private val PREF_KEY_SERVER_URL = "serverURL"
    private val PREF_KEY_LOCALE = "localeLanguage"

    var serverURL: String
        get() = prefs.getString(PREF_KEY_SERVER_URL, DataUtil.SERVER_REAL).toString()
        set(value) = prefs.edit().putString(PREF_KEY_SERVER_URL, value).apply()

    var developerMode: Boolean
        get() = prefs.getBoolean(PREF_KEY_DEVELOPER_MODE, false)
        set(value) = prefs.edit().putBoolean(PREF_KEY_DEVELOPER_MODE, value).apply()


    var localeLanguage: String
        get() = prefs.getString(PREF_KEY_LOCALE, Locale.getDefault().language).toString()
        set(value) = prefs.edit().putString(PREF_KEY_LOCALE, value).apply()


    // 202012. Failed Code
    private val PREF_KEY_DELIVERY_FAILED_CODE = "dFailedCode"
    private val PREF_KEY_PICKUP_FAILED_CODE = "pFailedCode"

    var dFailedCode: String
        get() = prefs.getString(PREF_KEY_DELIVERY_FAILED_CODE, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_DELIVERY_FAILED_CODE, value).apply()

    var pFailedCode: String
        get() = prefs.getString(PREF_KEY_PICKUP_FAILED_CODE, "").toString()
        set(value) = prefs.edit().putString(PREF_KEY_PICKUP_FAILED_CODE, value).apply()
}