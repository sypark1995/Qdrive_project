package com.giosis.util.qdrive.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.giosis.util.qdrive.singapore.MyApplication;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class SharedPreferencesHelper extends CordovaPlugin {

    public static final String SHARED_PREF_FILE = "net.giosis.util.qdrive_preferences";

    private static final String PREF_SIGN_IN_STATE = "sigin_state";
    private static final String PREF_SIGN_IN_OP_ID = "signin_opId";
    public static final String PREF_SIGN_IN_OP_NM = "signin_opNm";
    private static final String PREF_SIGN_IN_OP_DEFAULT = "signin_opDefault";
    private static final String PREF_SIGN_IN_OFFICE_CD = "office_code";
    private static final String PREF_SIGN_IN_OFFICE_NM = "office_nm";
    private static final String PREF_SIGN_IN_DEVICE_ID = "device_id";
    private static final String PREF_SIGN_IN_AUTH_NO = "signin_authNo";
    public static final String PREF_SIGN_IN_OP_EMAIL = "signin_opEmail";
    private static final String PREF_SIGN_IN_OP_PICKUP_DRIVER_YN = "signin_pickupDriverYN";

    // krm0219 Outlet
    private static final String PREF_SIGN_IN_OUTLET_DRIVER = "outlet_driver";
    private static final String PREF_SIGN_IN_LOCKER_STATUS = "locker_status";
    private static final String PREF_SIGN_IN_VERSION = "version";
    private static final String PREF_SIGN_IN_DRIVER_PW = "password";


    private CallbackContext context;

    /**
     * Executes the Request 및 PlugingResult Returns
     *
     * @param action          The action to execute
     * @param args            JSONArray of arguments for the plugin
     * @param callbackContext The Callback id when calling back into Javascript
     * @return A PluginResult object with a status and Message
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

        this.context = callbackContext;//추가

        if (action.equals("setSigninState")) {

            setSigninState(args);
            this.context.sendPluginResult(new PluginResult(PluginResult.Status.NO_RESULT));

        } else {

            this.context.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
        }

        return true;
    }

    /**
     * Set SignIn State
     */
    private void setSigninState(final JSONArray data) {

        boolean isSignIn;
        String opID;
        String opNm;
        String officeCd;
        String officeNm;
        String deviceID;
        String DefaultYn;
        String authNo;

        //2016.02.16 add eylee
        String opEmail;
        //2017-08-29 eylee
        String pickupDriverYN;

        // krm0219
        String outletDriverYN;
        String lockerStatus;
        String version;

        Context context = cordova.getActivity();

        try {
            JSONObject jObj = data.getJSONObject(0);
            isSignIn = jObj.getBoolean("signInState");
            opID = jObj.getString("opId");
            opNm = jObj.getString("opNm");
            officeCd = jObj.getString("officeCd");
            officeNm = jObj.getString("officeNm");
            deviceID = jObj.getString("device_id");
            DefaultYn = jObj.getString("DefaultYn");
            authNo = jObj.getString("authNo");
            opEmail = jObj.getString("opEmail");
            pickupDriverYN = jObj.getString("pickupDriverYN");
            String password = jObj.getString("password");

            // krm0219 Outlet
            outletDriverYN = jObj.getString("outletDriverYN");
            lockerStatus = jObj.getString("lockerStatus");
            version = jObj.getString("version");


            SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);

            Editor edit = settings.edit();
            edit.putBoolean(SharedPreferencesHelper.PREF_SIGN_IN_STATE, isSignIn);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_ID, opID);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_NM, opNm);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_DEFAULT, DefaultYn);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OFFICE_CD, officeCd);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OFFICE_NM, officeNm);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_DEVICE_ID, deviceID);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_AUTH_NO, authNo);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_EMAIL, opEmail);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_PICKUP_DRIVER_YN, pickupDriverYN);

            // krm0219
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OUTLET_DRIVER, outletDriverYN);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_LOCKER_STATUS, lockerStatus);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_VERSION, version);

            edit.apply();


            MyApplication.preferences.setUserId(opID);
            MyApplication.preferences.setDeviceUUID(deviceID);
            MyApplication.preferences.setAppVersion(version);

            MyApplication.preferences.setUserName(opNm);
            MyApplication.preferences.setUserEmail(opEmail);
            MyApplication.preferences.setOfficeCode(officeCd);
            MyApplication.preferences.setOfficeName(officeNm);

            MyApplication.preferences.setPickupDriver(pickupDriverYN);
            MyApplication.preferences.setOutletDriver(outletDriverYN);
            MyApplication.preferences.setLockerStatus(lockerStatus);

            MyApplication.preferences.setDefault(DefaultYn);
            MyApplication.preferences.setAuthNo(authNo);
            MyApplication.preferences.setUserPw(password);


            this.context.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));

        } catch (Exception e) {

            e.printStackTrace();
            this.context.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        }
    }

//
//    // Getter
//    public static boolean getSigninState(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getBoolean(SharedPreferencesHelper.PREF_SIGN_IN_STATE, false);
//    }
//
//    public static String getSigninOpID(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OP_ID, "");
//    }
//
//    public static String getSigninOpName(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OP_NM, "");
//    }
//
//    public static String getSigninAuthNo(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_AUTH_NO, "");
//    }
//
//    public static String getSigninOpDefaultYN(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OP_DEFAULT, "");
//    }
//
//    public static String getSigninOfficeCode(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OFFICE_CD, "");
//    }
//
//    public static String getSigninOfficeName(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OFFICE_NM, "");
//    }
//
//    public static String getSigninDeviceID(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_DEVICE_ID, "");
//    }
//
//    public static String getSigninOpEmail(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OP_EMAIL, "");
//    }
//
//    public static String getSigninPickupDriverYN(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OP_PICKUP_DRIVER_YN, "");
//    }
//
//    public static String getPrefSignInOutletDriver(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_OUTLET_DRIVER, "");
//    }
//
//    public static String getPrefSignInLockerStatus(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_LOCKER_STATUS, "");
//    }
//
//    public static String getPrefSignInVersion(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_VERSION, "");
//    }
//
//    public static String getPrefSignInDriverPw(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//        return settings.getString(SharedPreferencesHelper.PREF_SIGN_IN_DRIVER_PW, "");
//    }
}