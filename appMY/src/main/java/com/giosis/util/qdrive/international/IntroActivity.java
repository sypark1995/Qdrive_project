package com.giosis.util.qdrive.international;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.giosis.util.qdrive.settings.BluetoothDeviceData;

public class IntroActivity extends Activity {

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        context = getApplicationContext();


        /*//TODO
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();*/


        // Live10 설치 여부 확인
        Intent intent = getPackageManager().getLaunchIntentForPackage("net.giosis.qpost");
        if (intent == null && !BuildConfig.DEBUG) {
            new AlertDialog.Builder(this)
                    .setMessage(context.getResources().getString(R.string.msg_live10_installed))
                    .setCancelable(false).setPositiveButton(context.getResources().getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent qtalkIntent = new Intent(Intent.ACTION_VIEW);
                            qtalkIntent.setData(Uri.parse("market://details?id=net.giosis.qpost"));
                            startActivity(qtalkIntent);
                            finish();
                        }
                    }).show();
        } else {

            // Bluetooth Print 연결 초기화 (앱 종료시 못했을 경우를 대비  : MainActivity onDestroy 호출 안되는 경우 있음 !)
            BluetoothDeviceData.connectedPrinterAddress = null;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            }, 500);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(MyApplication.localeManager.setLocale(base));
    }
}