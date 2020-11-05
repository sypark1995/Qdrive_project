package com.giosis.util.qdrive.singapore

import android.os.Bundle
import android.util.Log
import com.giosis.util.qdrive.util.DataUtil
import org.apache.cordova.CordovaActivity

class LoginActivity2 : CordovaActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = MyApplication.preferences.serverURL + DataUtil.API_ADDRESS
        val versionName = BuildConfig.VERSION_NAME
        Log.e("console", "console LoginActivity")
        super.loadUrl("file:///android_asset/www/My/index.html")
        val method = intent.getStringExtra("method")

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

    fun executeJS(js: String) {
        super.loadUrl("javascript:$js")
    }
}
