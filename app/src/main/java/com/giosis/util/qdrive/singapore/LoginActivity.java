package com.giosis.util.qdrive.singapore;

import android.os.Bundle;
import android.os.Handler;

import org.apache.cordova.CordovaActivity;

public class LoginActivity extends CordovaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //WebView.setWebContentsDebuggingEnabled(true);

        super.loadUrl("file:///android_asset/www/My/index.html");

        String method = getIntent().getStringExtra("method");

        // SignOut 하면 id, pw 값 초기화 필요
        if (method != null && method.equals("signOut")) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    executeJS("initData('signout')");
                }
            }, 200);
        }
    }

    public void executeJS(String js) {
        super.loadUrl("javascript:" + js);
    }
}