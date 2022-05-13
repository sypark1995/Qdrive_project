package com.giosis.util.qdrive.singapore.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PermissionChecker {

    public static String CAMERA = Manifest.permission.CAMERA;
    public static String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;


    private final Context context;

    public PermissionChecker(Context context) {

        this.context = context;
    }

    public boolean lacksPermissions(String... permissions) {

        for (String permission : permissions) {
            if (lacksPermission(permission)) {

                return true;
            }
        }

        return false;
    }

    private boolean lacksPermission(String permission) {

        // 권한 승인 (X) true // (O) false
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

    public String[] setlacksPermissions(String... permissions) {

        ArrayList<String> lacks_permission_array = new ArrayList<>();

        for (String permission : permissions) {
            if (lacksPermission(permission)) {

                lacks_permission_array.add(permission);
            }
        }

        String[] lacks_permission = lacks_permission_array.toArray(new String[lacks_permission_array.size()]);

        return lacks_permission;
    }

    /*
    public boolean lacksPermissions(String... permissions) {

        for (String permission : permissions) {
            if (lacksPermission(permission)) {

                return true;
            }
        }

        return false;
    }

    private boolean lacksPermission(String permission) {

        // 권한 승인 (X) true // (O) false
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }
     */


}
