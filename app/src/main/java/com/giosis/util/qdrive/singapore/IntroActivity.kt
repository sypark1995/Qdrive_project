package com.giosis.util.qdrive.singapore;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

public class IntroActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

       /* // TEST.  VM
        Intent intent = new Intent();
        intent.setClass(IntroActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();*/

        // Live10 설치 여부 확인
        Intent intent = getPackageManager().getLaunchIntentForPackage("net.giosis.qpost");
        if (intent == null && !BuildConfig.DEBUG) {

            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.msg_live10_installed))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.button_ok),
                            (dialog, which) -> {

                                // 구글 마켓으로 이동
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setData(Uri.parse("market://details?id=net.giosis.qpost"));
                                startActivity(intent1);
                                finish();
                            }).show();
        } else {

//            try {
//                BluetoothDeviceData.connectedPrinterAddress = null;
//
//                if (BluetoothDeviceData.socket != null) {
//
//                    BluetoothDeviceData.socket.close();
//                    BluetoothDeviceData.socket = null;
//                }
//
//                Log.e("print", "IntroActivity  init()");
//            } catch (Exception e) {
//
//                Log.e("Exception", "IntroActivity  Bluetooth socket Exception : " + e.toString());
//            }


            new Handler().postDelayed(() -> {

                Intent intent12 = new Intent();
                intent12.setClass(IntroActivity.this, LoginActivity.class);
                intent12.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent12);
                finish();

            }, 500);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}