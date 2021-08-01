package com.giosis.library.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.giosis.library.R;


public class DisplayUtil {

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

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }


    public static void dismissProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {

                Log.e("DisplayUtil", "dismissProgressDialog");
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
        } finally {
            dialog = null;
        }
    }


    public static void hideKeyboard(Activity activity) {

        View view = activity.getCurrentFocus();

        if (view != null) {

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static void AlertDialog(final Activity activity, String msg) {

        if (!activity.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.text_warning));
            builder.setMessage(msg);
            builder.setPositiveButton(activity.getResources().getString(R.string.button_close),
                    (dialog, which) -> {

                        dialog.dismiss();
                        activity.finish();
                    });
            builder.show();
        }
    }


    public static void setPreviewCamera(ImageView imageview) {
        // ViewTree의 뷰가 그려질 때마다
        imageview.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //뷰의 생성된 후 크기와 위치 구하기
                        int width = imageview.getWidth();

                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, width);
                        imageview.setLayoutParams(layoutParams);

                        //리스너 해제
                        imageview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }
}