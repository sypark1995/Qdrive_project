package com.giosis.library.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.giosis.library.R;


public class PermissionActivity extends CommonActivity {

    public static final int PERMISSIONS_GRANTED = 200;
    public static final int PERMISSIONS_DENIED = 400;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String EXTRA_PERMISSIONS = "EXTRA_PERMISSIONS";
    private static final String PACKAGE_URL_SCHEME = "package:";

    private PermissionChecker checker;
    private boolean requiresCheck;


    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {

        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        setContentView(R.layout.activity_permission);

        checker = new PermissionChecker(this);
        requiresCheck = true;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            super.setRequestedOrientation(requestedOrientation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (requiresCheck) {
            String[] permissions = getPermissions();

            if (checker.lacksPermissions(permissions)) {        // 권한이 없는 경우 권한 요청!

                requestPermissions(permissions);
            } else {    // 모든 권한이 있는 경우

                allPermissionsGranted();
            }
        } else {
            requiresCheck = true;
        }
    }


    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    private void allPermissionsGranted() {

        setResult(PERMISSIONS_GRANTED);
        finish();
        overridePendingTransition(0, 0);
    }

    // requestPermissions()의 result
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(permissions, grantResults)) {

            requiresCheck = true;
            allPermissionsGranted();
        } else {

            requiresCheck = false;
            boolean check_dont_again = false;
            String[] lacks_permission = checker.setlacksPermissions(permissions);

            for (int i = 0; i < lacks_permission.length; i++) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, lacks_permission[i])) {
                    // Don't ask again check!
                    Log.e("permission", "  Permission  " + lacks_permission[i]);
                    check_dont_again = true;
                }
            }

            if (check_dont_again) {

                showDontAgainMissingPermissionDialog();
            } else {

                showMissingPermissionDialog();
            }
        }
    }

    // 모든 권한이 허용되었는지 체크
    private boolean hasAllPermissionsGranted(@NonNull String[] permissions, @NonNull int[] grantResults) {

        for (int grantResult : grantResults) {

            if (grantResult == PackageManager.PERMISSION_DENIED) {

                return false;
            }
        }
        return true;
    }


    // 권한을 하나라도 거부 하였을 때 나타나는 Dialog
    private void showMissingPermissionDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PermissionActivity.this);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(getString(R.string.permission_title));
        dialogBuilder.setMessage(getString(R.string.permission_content));

        dialogBuilder.setNegativeButton(getString(R.string.permission_re_try), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // 허용할 때 까지 요청
                String[] permissions = getPermissions();
                requestPermissions(permissions);

              /*  종료
                setResult(PERMISSIONS_DENIED);
                finish();
                overridePendingTransition(0, 0);*/
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.permission_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        dialogBuilder.show();
    }

    // 권한을 하나라도 거부 하였을 때 나타나는 Dialog  (Don't ask again check!!)
    private void showDontAgainMissingPermissionDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PermissionActivity.this);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(getString(R.string.permission_title));
        dialogBuilder.setMessage(getString(R.string.permission_dont_again_content));

        dialogBuilder.setPositiveButton(getString(R.string.permission_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        dialogBuilder.show();
    }

    private void startAppSettings() {       // 설정 화면으로 이동

        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
}

