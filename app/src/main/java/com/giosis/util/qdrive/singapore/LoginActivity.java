package com.giosis.util.qdrive.singapore;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.giosis.util.qdrive.util.DataUtil;

import org.apache.cordova.CordovaActivity;

public class LoginActivity extends CordovaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String url = MyApplication.preferences.getServerURL() + DataUtil.API_ADDRESS;
        String versionName = BuildConfig.VERSION_NAME;


        Log.e("console", "console LoginActivity");
        super.loadUrl("file:///android_asset/www/My/index.html");

        String method = getIntent().getStringExtra("method");

//        // SignOut 하면 id, pw 값 초기화 필요
//        if (method != null && method.equals("signOut")) {
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    executeJS("initData('signout')");
//                }
//            }, 200);
//        }


     //   new Handler().postDelayed(() -> executeJS("intiServerURL('"+url+"', '"+versionName+"')"), 100);

    }

    public void executeJS(String js) {
        super.loadUrl("javascript:" + js);
    }
}