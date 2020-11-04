package com.giosis.util.qdrive.singapore;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.giosis.util.qdrive.settings.BluetoothDeviceData;

public class IntroActivity extends Activity {


    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        context = getApplicationContext();

       /* // TEST.  VM
        Intent intent = new Intent();
        intent.setClass(IntroActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();*/

        // Live10 설치 여부 확인
//        Intent intent = getPackageManager().getLaunchIntentForPackage("net.giosis.qpost");
//        if (intent == null) {
//            new AlertDialog.Builder(this)
//                    .setMessage(context.getResources().getString(R.string.msg_live10_installed))
//                    .setCancelable(false).setPositiveButton(context.getResources().getString(R.string.button_ok),
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            // 구글 마켓으로 이동
//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            intent.setData(Uri.parse("market://details?id=net.giosis.qpost"));
//                            startActivity(intent);
//                            finish();
//                        }
//
//                    }).show();
//        } else
            {

            try {

                BluetoothDeviceData.connectedPrinterAddress = null;

                if (BluetoothDeviceData.socket != null) {

                    BluetoothDeviceData.socket.close();
                    BluetoothDeviceData.socket = null;
                }

                Log.e("print", "IntroActivity  init()");
            } catch (Exception e) {

                Log.e("Exception", "IntroActivity  Bluetooth socket Exception : " + e.toString());
            }


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent();
                    intent.setClass(IntroActivity.this, LoginActivity2.class);
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
}