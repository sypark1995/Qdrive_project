package com.giosis.util.qdrive.util;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

import gmkt.inc.android.common.ui.GMKT_ProgressDialog;

public class LoadingDialog extends CordovaPlugin {

    private GMKT_ProgressDialog progressDialog;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

        if (action.equals("show")) {

            progressDialog = GMKT_ProgressDialog.show(cordova.getActivity(), "", "Please Wait....", true, false);
        } else if (action.equals("hide")) {

            progressDialog.hide();
        }

        return true;
    }
}