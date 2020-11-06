package com.giosis.util.qdrive.capture.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Preconditions;

import com.google.android.gms.common.images.Size;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

/**
 * Utility class to retrieve shared preferences.
 */
public class PreferenceUtils {

    static void saveString(Context context, @StringRes int prefKeyId, @Nullable String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(prefKeyId), value)
                .apply();
    }

    @SuppressLint("RestrictedApi")
    public static CameraSource.SizePair getCameraPreviewSizePair(Context context, int cameraId) {
        Preconditions.checkArgument(
                cameraId == CameraSource.CAMERA_FACING_BACK
                        || cameraId == CameraSource.CAMERA_FACING_FRONT);
        String previewSizePrefKey;
        String pictureSizePrefKey;
        if (cameraId == CameraSource.CAMERA_FACING_BACK) {
            previewSizePrefKey = "rcpvs";
            pictureSizePrefKey = "rcpts";
        } else {
            previewSizePrefKey = "fcpvs";
            pictureSizePrefKey = "fcpts";
        }

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return new CameraSource.SizePair(
                    Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
                    Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null)));
        } catch (Exception e) {
            return null;
        }
    }

    public static FirebaseVisionObjectDetectorOptions getObjectDetectorOptionsForStillImage(
            Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean enableMultipleObjects =
                sharedPreferences.getBoolean("siodemo", false);

        boolean enableClassification =
                sharedPreferences.getBoolean("siodec", true);

        FirebaseVisionObjectDetectorOptions.Builder builder =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE);
        if (enableMultipleObjects) {
            builder.enableMultipleObjects();
        }
        if (enableClassification) {
            builder.enableClassification();
        }
        return builder.build();
    }

    public static FirebaseVisionObjectDetectorOptions getObjectDetectorOptionsForLivePreview(
            Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enableMultipleObjects =
                sharedPreferences.getBoolean("lpodemo", false);
        boolean enableClassification =
                sharedPreferences.getBoolean("lpodec", true);
        FirebaseVisionObjectDetectorOptions.Builder builder =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE);
        if (enableMultipleObjects) {
            builder.enableMultipleObjects();
        }
        if (enableClassification) {
            builder.enableClassification();
        }
        return builder.build();
    }


    public static String getAutoMLRemoteModelName(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String modelNamePrefKey = "lparmn";
        String defaultModelName = "mlkit_flowers";
        String remoteModelName = sharedPreferences.getString(modelNamePrefKey, defaultModelName);
        if (remoteModelName.isEmpty()) {
            remoteModelName = defaultModelName;
        }
        return remoteModelName;
    }

    public static String getAutoMLRemoteModelChoice(Context context) {
        String modelChoicePrefKey = "lparmc";
        String defaultModelChoice = "Local";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(modelChoicePrefKey, defaultModelChoice);
    }

    /**
     * Mode type preference is backed by {@link android.preference.ListPreference} which only support
     * storing its entry value as string type, so we need to retrieve as string and then convert to
     * integer.
     */
    private static int getModeTypePreferenceValue(
            Context context, @StringRes int prefKeyResId, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(prefKeyResId);
        return Integer.parseInt(sharedPreferences.getString(prefKey, String.valueOf(defaultValue)));
    }

    public static boolean isCameraLiveViewportEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = "clv";
        return sharedPreferences.getBoolean(prefKey, false);
    }
}
