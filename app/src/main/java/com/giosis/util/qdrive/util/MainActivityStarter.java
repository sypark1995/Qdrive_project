package com.giosis.util.qdrive.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.settings.DeveloperModeActivity;
import com.giosis.util.qdrive.singapore.LoginActivity;
import com.giosis.util.qdrive.singapore.SMSVerificationActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

public class MainActivityStarter extends CordovaPlugin {

    /**
     * Executes the Request 및 PlugingResult Returns
     *
     * @param action          The action to execute
     * @param args            JSONArray of arguments for the plugin
     * @param callbackContext The Callback id when calling back into Javascript
     * @return A PluginResult object with a status and Message
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {  //추가
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        //추가
        result.setKeepCallback(true);

        if (action.equals("start")) {

            start();
        } else if (action.equals("LoginStart")) {

            LoginStart();
        } else if (action.equals("goMarket")) {

            goMarket();
        } else if (action.equals("verify")) {

            verify();
        } else if(action.equals("goDeveloper")) {

            goDeveloper();
        }




        if (action.equals("goHome")) {

            goHome();
        } else {

            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));  //추가
        }

        return true;
    }

    /**
     * Starts an intent to MainActivity
     */
    public void start() {

        Log.d("eylee", "MainActivityStarter start!!!");
        Activity context = cordova.getActivity();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private void LoginStart() {

        Activity context = cordova.getActivity();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    private void goMarket() {

        // Live10
        Activity context = cordova.getActivity();
        Uri uri = Uri.parse("market://details?id=com.giosis.util.qdrive.singapore");
        Intent itt = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(itt);
    }

    public void verify() {

        Activity context = cordova.getActivity();
        Intent intent = new Intent(context, SMSVerificationActivity.class);
        context.startActivity(intent);
    }

    private void goHome() {

        Activity context = cordova.getActivity();
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.putExtra("callstack", "goHomeBtn");
        context.startActivity(mainIntent);
    }



    private void goDeveloper() {

        Activity context = cordova.getActivity();
        Intent intent = new Intent(context, DeveloperModeActivity.class);
        intent.putExtra("called", "login");
        context.startActivity(intent);
        context.finish();
    }
}