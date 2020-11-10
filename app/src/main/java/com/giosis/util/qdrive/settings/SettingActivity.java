package com.giosis.util.qdrive.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;

import com.giosis.util.qdrive.singapore.LoginActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;

/**
 * @author krm0219
 **/
public class SettingActivity extends AppCompatActivity {
    String TAG = "SettingActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;
    FrameLayout layout_top_signOut;

    ImageView img_setting_profile;
    TextView text_setting_driver_name;
    ImageView img_setting_modify_info;
    TextView text_setting_driver_id;
    TextView text_setting_driver_email;
    TextView text_setting_driver_branch;
    TextView text_setting_change_password;

    TextView text_setting_delete_data;
    RelativeLayout layout_setting_notice;

    RelativeLayout layout_setting_printer_setting;
    RelativeLayout layout_setting_scan_setting;
    LinearLayout layout_setting_locker;
    TextView text_setting_app_version;
    Button btn_setting_developer_mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);
        layout_top_signOut = findViewById(R.id.layout_top_signout);

        img_setting_profile = findViewById(R.id.img_setting_profile);
        text_setting_driver_name = findViewById(R.id.text_setting_driver_name);
        img_setting_modify_info = findViewById(R.id.img_setting_modify_info);
        text_setting_driver_id = findViewById(R.id.text_setting_driver_id);
        text_setting_driver_email = findViewById(R.id.text_setting_driver_email);
        text_setting_driver_branch = findViewById(R.id.text_setting_driver_branch);
        text_setting_change_password = findViewById(R.id.text_setting_change_password);

        text_setting_delete_data = findViewById(R.id.text_setting_delete_data);
        layout_setting_notice = findViewById(R.id.layout_setting_notice);

        layout_setting_printer_setting = findViewById(R.id.layout_setting_printer_setting);
        layout_setting_scan_setting = findViewById(R.id.layout_setting_scan_setting);
        layout_setting_locker = findViewById(R.id.layout_setting_locker);

        text_setting_app_version = findViewById(R.id.text_setting_app_version);
        btn_setting_developer_mode = findViewById(R.id.btn_setting_developer_mode);


        //
        text_top_title.setText(R.string.navi_setting);
        layout_top_signOut.setVisibility(View.VISIBLE);

        text_top_title.setOnClickListener(clickListener);
        layout_top_back.setOnClickListener(clickListener);
        layout_top_signOut.setOnClickListener(clickListener);
        img_setting_modify_info.setOnClickListener(clickListener);
        text_setting_change_password.setOnClickListener(clickListener);
        text_setting_delete_data.setOnClickListener(clickListener);
        layout_setting_notice.setOnClickListener(clickListener);
        layout_setting_printer_setting.setOnClickListener(clickListener);
        layout_setting_scan_setting.setOnClickListener(clickListener);
        layout_setting_locker.setOnClickListener(clickListener);
        btn_setting_developer_mode.setOnClickListener(clickListener);

        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qdrive_img_default);
        img_setting_profile.setImageBitmap(mBitmap);
        RoundedBitmapDrawable roundedImageDrawable = DisplayUtil.createRoundedBitmapImageDrawableWithBorder(this, mBitmap);
        img_setting_profile.setImageDrawable(roundedImageDrawable);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        initDeveloperMode();
//
//        String opId = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//        String driverName = SharedPreferencesHelper.getSigninOpName(getApplicationContext());
//        String driverEmail = SharedPreferencesHelper.getSigninOpEmail(getApplicationContext());
//        String officeName = SharedPreferencesHelper.getSigninOfficeName(getApplicationContext());
//        String outletDriverYN = SharedPreferencesHelper.getPrefSignInOutletDriver(getApplicationContext());
//        String lockerStatus = SharedPreferencesHelper.getPrefSignInLockerStatus(getApplicationContext());
//        String version = SharedPreferencesHelper.getPrefSignInVersion(getApplicationContext());
//
        String opId = MyApplication.preferences.getUserId();
        String driverName = MyApplication.preferences.getUserName();
        String driverEmail = MyApplication.preferences.getUserEmail();
        String officeName = MyApplication.preferences.getOfficeName();
        String outletDriverYN = MyApplication.preferences.getOutletDriver();
        String lockerStatus = MyApplication.preferences.getLockerStatus();
        String version = MyApplication.preferences.getAppVersion();


        text_setting_driver_name.setText(driverName);
        text_setting_driver_id.setText(opId);
        text_setting_driver_email.setText(driverEmail);
        text_setting_driver_branch.setText(officeName);


        if (MyApplication.preferences.getServerURL().contains("test")) {

            text_setting_app_version.setText(version + "_ test");
        } else if (MyApplication.preferences.getServerURL().contains("staging")) {

            text_setting_app_version.setText(version + " _ staging");
        } else {

            text_setting_app_version.setText(version);
        }


        if (outletDriverYN.equals("Y")) {
            if (lockerStatus.contains("no pin") || lockerStatus.contains("active") || lockerStatus.contains("expired")) {

                layout_setting_locker.setVisibility(View.VISIBLE);
            }
        }

//        if (opId.equalsIgnoreCase("karam.kim")) {
//
//            layout_setting_locker.setVisibility(View.VISIBLE);
//        }
    }

    void initDeveloperMode() {
        if (MyApplication.preferences.getDeveloperMode()) {

            btn_setting_developer_mode.setVisibility(View.VISIBLE);
        } else {

            btn_setting_developer_mode.setVisibility(View.GONE);
        }
    }


    int showDeveloperModeClickCount = 0;

    View.OnClickListener clickListener = v -> {

        switch (v.getId()) {

            case R.id.text_top_title: {

                if (showDeveloperModeClickCount == 10) {

                    if (MyApplication.preferences.getDeveloperMode()) {
                        // true > false

                        MyApplication.preferences.setDeveloperMode(false);
                    } else {
                        // false > true

                        MyApplication.preferences.setDeveloperMode(true);
                        Toast.makeText(SettingActivity.this, getResources().getString(R.string.text_developer_mode), Toast.LENGTH_SHORT).show();
                    }

                    showDeveloperModeClickCount = 0;
                    initDeveloperMode();
                } else {

                    showDeveloperModeClickCount++;
                }
            }
            break;

            case R.id.layout_top_back: {

                finish();
            }
            break;

            case R.id.layout_top_signout: {

                signOut();
            }
            break;

            case R.id.img_setting_modify_info: {

                Intent intent = new Intent(SettingActivity.this, ModifyUserInfoActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.text_setting_change_password: {

                Intent intent = new Intent(SettingActivity.this, ChangePwdActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.text_setting_delete_data: {

                deleteData();
            }
            break;

            case R.id.layout_setting_notice: {

                Intent intent = new Intent(SettingActivity.this, NoticeActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.layout_setting_printer_setting: {

                Intent intent = new Intent(SettingActivity.this, PrinterSettingActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.layout_setting_scan_setting: {

                Intent intent = new Intent(SettingActivity.this, ScanSettingActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.layout_setting_locker: {

                Intent intent = new Intent(SettingActivity.this, LockerUserInfoActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.btn_setting_developer_mode: {

                Intent intent = new Intent(SettingActivity.this, DeveloperModeActivity.class);
                startActivity(intent);
            }
            break;
        }
    };

    void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.button_confirm));
        builder.setMessage(getResources().getString(R.string.msg_want_sign_out));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> {

            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.putExtra("method", "signOut");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        builder.setNeutralButton(getResources().getString(R.string.button_cancel), (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void deleteData() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.button_confirm));
        builder.setMessage(getResources().getString(R.string.msg_want_to_delete_data));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> {

            try {

                int delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");
                Log.i("krm0219", "Delete Count : " + delete);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingActivity.this);
                builder1.setTitle(getResources().getString(R.string.text_alert));
                builder1.setMessage(getResources().getString(R.string.msg_deleted_data));
                builder1.setPositiveButton(getResources().getString(R.string.button_ok), (dialog1, which1) -> dialog1.cancel());

                AlertDialog alertDialog = builder1.create();
                alertDialog.show();
            } catch (Exception ignored) {
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.button_cancel), (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}