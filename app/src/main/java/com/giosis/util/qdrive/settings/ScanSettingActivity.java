package com.giosis.util.qdrive.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

public class ScanSettingActivity extends AppCompatActivity {
    String TAG = "ScanSettingActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    Button btn_scan_setting_vibration_on;
    Button btn_scan_setting_vibration_off;


    Context context;
    String vibrationString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_setting);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        btn_scan_setting_vibration_on = findViewById(R.id.btn_scan_setting_vibration_on);
        btn_scan_setting_vibration_off = findViewById(R.id.btn_scan_setting_vibration_off);


        //
        context = getApplicationContext();

        text_top_title.setText(R.string.text_title_scan_setting);


        layout_top_back.setOnClickListener(clickListener);
        btn_scan_setting_vibration_on.setOnClickListener(clickListener);
        btn_scan_setting_vibration_off.setOnClickListener(clickListener);

        //
        SharedPreferences sharedPreferences = getSharedPreferences("PREF_SCAN_SETTING", Activity.MODE_PRIVATE);
        vibrationString = sharedPreferences.getString("vibration", "0");

        if (vibrationString.equals("0")) {
            // MainActivity 에서 초기화, 혹시 안됐을 경우..

            vibrationString = "OFF";

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("vibration", vibrationString);
            editor.apply();

            btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.custom_button_normal_30);
            btn_scan_setting_vibration_on.setTextColor(context.getResources().getColor(R.color.color_4fb648));
            btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.custom_button_pressed_30);
            btn_scan_setting_vibration_off.setTextColor(context.getResources().getColor(R.color.white));
        } else {

            if (vibrationString.equals("ON")) {

                btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.custom_button_pressed_30);
                btn_scan_setting_vibration_on.setTextColor(context.getResources().getColor(R.color.white));
                btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.custom_button_normal_30);
                btn_scan_setting_vibration_off.setTextColor(context.getResources().getColor(R.color.color_4fb648));
            } else if (vibrationString.equals("OFF")) {

                btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.custom_button_normal_30);
                btn_scan_setting_vibration_on.setTextColor(context.getResources().getColor(R.color.color_4fb648));
                btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.custom_button_pressed_30);
                btn_scan_setting_vibration_off.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.btn_scan_setting_vibration_on: {

                    if (vibrationString.equals("OFF")) {        // OFF > ON

                        vibrationString = "ON";

                        btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.custom_button_pressed_30);
                        btn_scan_setting_vibration_on.setTextColor(context.getResources().getColor(R.color.white));
                        btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.custom_button_normal_30);
                        btn_scan_setting_vibration_off.setTextColor(context.getResources().getColor(R.color.color_4fb648));

                        SharedPreferences sharedPreferences = getSharedPreferences("PREF_SCAN_SETTING", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("vibration", vibrationString);
                        editor.apply();
                    }
                }
                break;
                case R.id.btn_scan_setting_vibration_off: {

                    if (vibrationString.equals("ON")) {         // ON > OFF

                        vibrationString = "OFF";

                        btn_scan_setting_vibration_on.setBackgroundResource(R.drawable.custom_button_normal_30);
                        btn_scan_setting_vibration_on.setTextColor(context.getResources().getColor(R.color.color_4fb648));
                        btn_scan_setting_vibration_off.setBackgroundResource(R.drawable.custom_button_pressed_30);
                        btn_scan_setting_vibration_off.setTextColor(context.getResources().getColor(R.color.white));

                        SharedPreferences sharedPreferences = getSharedPreferences("PREF_SCAN_SETTING", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("vibration", vibrationString);
                        editor.apply();
                    }
                }
                break;
            }
        }
    };
}