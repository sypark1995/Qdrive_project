package com.giosis.util.qdrive.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.giosis.util.qdrive.barcodescanner.ManualHelper;
import com.giosis.util.qdrive.singapore.LoginActivity;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

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


        //
        text_top_title.setText(R.string.navi_setting);
        layout_top_signOut.setVisibility(View.VISIBLE);


        layout_top_back.setOnClickListener(clickListener);
        layout_top_signOut.setOnClickListener(clickListener);
        img_setting_modify_info.setOnClickListener(clickListener);
        text_setting_change_password.setOnClickListener(clickListener);
        text_setting_delete_data.setOnClickListener(clickListener);
        layout_setting_notice.setOnClickListener(clickListener);
        layout_setting_printer_setting.setOnClickListener(clickListener);
        layout_setting_scan_setting.setOnClickListener(clickListener);
        layout_setting_locker.setOnClickListener(clickListener);

        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qdrive_img_default);
        img_setting_profile.setImageBitmap(mBitmap);
        RoundedBitmapDrawable roundedImageDrawable = createRoundedBitmapImageDrawableWithBorder(mBitmap);
        img_setting_profile.setImageDrawable(roundedImageDrawable);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        String opId = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        String driverName = SharedPreferencesHelper.getSigninOpName(getApplicationContext());
        String driverEmail = SharedPreferencesHelper.getSigninOpEmail(getApplicationContext());
        String officeName = SharedPreferencesHelper.getSigninOfficeName(getApplicationContext());
        String outletDriverYN = SharedPreferencesHelper.getPrefSignInOutletDriver(getApplicationContext());
        String lockerStatus = SharedPreferencesHelper.getPrefSignInLockerStatus(getApplicationContext());
        String version = SharedPreferencesHelper.getPrefSignInVersion(getApplicationContext());


        text_setting_driver_name.setText(driverName);
        text_setting_driver_id.setText(opId);
        text_setting_driver_email.setText(driverEmail);
        text_setting_driver_branch.setText(officeName);


        if (ManualHelper.MOBILE_SERVER_URL.contains("test")) {

            text_setting_app_version.setText(version + "_ test");
        } else if (ManualHelper.MOBILE_SERVER_URL.contains("staging")) {

            text_setting_app_version.setText(version + " _ staging");
        } else {

            text_setting_app_version.setText(version);
        }


        if (outletDriverYN.equals("Y")) {
            if (lockerStatus.contains("no pin") || lockerStatus.contains("active") || lockerStatus.contains("expired")) {

                layout_setting_locker.setVisibility(View.VISIBLE);
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
            }
        }
    };

    void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.button_confirm));
        builder.setMessage(getResources().getString(R.string.msg_want_sign_out));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                intent.putExtra("method", "signOut");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void deleteData() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.button_confirm));
        builder.setMessage(getResources().getString(R.string.msg_want_to_delete_data));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {

                    int delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");

                    Log.e("krm0219", "Delete : " + delete);

                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                    builder.setTitle(getResources().getString(R.string.text_alert));
                    builder.setMessage(getResources().getString(R.string.msg_deleted_data));
                    builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } catch (Exception e) {

                }
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private RoundedBitmapDrawable createRoundedBitmapImageDrawableWithBorder(Bitmap bitmap) {
        int bitmapWidthImage = bitmap.getWidth();
        int bitmapHeightImage = bitmap.getHeight();
        int borderWidthHalfImage = 4;

        int bitmapRadiusImage = Math.min(bitmapWidthImage, bitmapHeightImage) / 2;
        int bitmapSquareWidthImage = Math.min(bitmapWidthImage, bitmapHeightImage);
        int newBitmapSquareWidthImage = bitmapSquareWidthImage + borderWidthHalfImage;

        Bitmap roundedImageBitmap = Bitmap.createBitmap(newBitmapSquareWidthImage, newBitmapSquareWidthImage, Bitmap.Config.ARGB_8888);
        Canvas mcanvas = new Canvas(roundedImageBitmap);
        mcanvas.drawColor(Color.RED);
        int i = borderWidthHalfImage + bitmapSquareWidthImage - bitmapWidthImage;
        int j = borderWidthHalfImage + bitmapSquareWidthImage - bitmapHeightImage;

        mcanvas.drawBitmap(bitmap, i, j, null);

        Paint borderImagePaint = new Paint();
        borderImagePaint.setStyle(Paint.Style.STROKE);
        borderImagePaint.setStrokeWidth(borderWidthHalfImage * 2);
        borderImagePaint.setColor(getResources().getColor(R.color.color_ebebeb));
        mcanvas.drawCircle(mcanvas.getWidth() / 2, mcanvas.getWidth() / 2, newBitmapSquareWidthImage / 2, borderImagePaint);

        RoundedBitmapDrawable roundedImageBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), roundedImageBitmap);
        roundedImageBitmapDrawable.setCornerRadius(bitmapRadiusImage);
        roundedImageBitmapDrawable.setAntiAlias(true);
        return roundedImageBitmapDrawable;
    }
}