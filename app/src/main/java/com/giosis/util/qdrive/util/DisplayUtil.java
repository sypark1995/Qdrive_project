package com.giosis.util.qdrive.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.giosis.util.qdrive.singapore.R;
import com.google.firebase.analytics.FirebaseAnalytics;

public class DisplayUtil {
    static String TAG = "DisplayUtil";
    private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

    /**
     * 픽셀단위를 현재 디스플레이 화면에 비례한 크기로 반환합니다.
     *
     * @param pixel 픽셀
     * @return 변환된 값 (DP)
     */
    public static int DPFromPixel(Context context, int pixel) {
        float scale = context.getResources().getDisplayMetrics().density;

        return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
    }

    /**
     * 현재 디스플레이 화면에 비례한 DP단위를 픽셀 크기로 반환합니다.
     *
     * @param DP 픽셀
     * @return 변환된 값 (pixel)
     */
    public static int PixelFromDP(Context context, int DP) {
        float scale = context.getResources().getDisplayMetrics().density;

        return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
    }

    public static int dpTopx(Context context, float dp) {

        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return pixel;
    }


    public static void dismissProgressDialog(ProgressDialog progressDialog) {
        Log.e(TAG, "dismissProgressDialog");

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {

                //get the Context object that was used to create the dialog
                Context context = ((ContextWrapper) progressDialog.getContext()).getBaseContext();

                // if the Context used here was an activity AND it hasn't been finished or destroyed
                // then dismiss it
                if (context instanceof Activity) {

                    if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                        dismissWithExceptionHandling(progressDialog);
                    }
                } else
                    // if the Context used wasn't an Activity, then dismiss it too
                    dismissWithExceptionHandling(progressDialog);
            }
        }
    }

    private static void dismissWithExceptionHandling(ProgressDialog dialog) {
        try {
            dialog.dismiss();
        } catch (final IllegalArgumentException e) {
            // Do nothing.
        } catch (final Exception e) {
            // Do nothing.
        }
    }


    public static void AlertDialog(final Activity activity, String msg) {

        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(activity);
        alert_internet_status.setTitle(activity.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(activity.getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        activity.finish();
                    }
                });
        alert_internet_status.show();
    }


    public static void FirebaseSelectEvents(String type, String id) {

        try {

            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
            params.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            com.giosis.library.util.DataUtil.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
        } catch (Exception e) {

            Log.e("Firebase", "FirebaseSelectEvents error : " + e.toString());
        }
    }


    public static RoundedBitmapDrawable createRoundedBitmapImageDrawableWithBorder(Context context, Bitmap bitmap) {
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
        borderImagePaint.setColor(context.getResources().getColor(R.color.color_ebebeb));
        mcanvas.drawCircle(mcanvas.getWidth() / 2, mcanvas.getWidth() / 2, newBitmapSquareWidthImage / 2, borderImagePaint);

        RoundedBitmapDrawable roundedImageBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), roundedImageBitmap);
        roundedImageBitmapDrawable.setCornerRadius(bitmapRadiusImage);
        roundedImageBitmapDrawable.setAntiAlias(true);
        return roundedImageBitmapDrawable;
    }


    public static void setPreviewCamera(ImageView imageview) {
        // ViewTree의 뷰가 그려질 때마다
        imageview.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //뷰의 생성된 후 크기와 위치 구하기
                        int a = imageview.getWidth();
                        int b = imageview.getHeight();

                        Log.e("krm0219", "Size : " + a + " / " + b);

                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(a, a);
                        imageview.setLayoutParams(layoutParams);

                        //리스너 해제
                        imageview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

}