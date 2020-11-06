package com.giosis.util.qdrive.barcodescanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.giosis.util.qdrive.barcodescanner.camera.CameraManager;
import com.giosis.util.qdrive.international.R;
import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
    String TAG = "ViewfinderView";

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;

    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int frameColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {

            Rect frame = CameraManager.get().getFramingRect();
            if (frame == null) {
                return;
            }

            int width = getWidth();
            int height = getHeight();

            // Draw the exterior (i.e. outside the framing rect) darkened
            paint.setColor(resultBitmap != null ? resultColor : maskColor);
            canvas.drawRect(0, 0, width, frame.top, paint);
            canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
            canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
            canvas.drawRect(0, frame.bottom + 1, width, height, paint);

            if (resultBitmap != null) {
                // Draw the opaque result bitmap over the scanning rectangle
                paint.setAlpha(CURRENT_POINT_OPACITY);
                canvas.drawBitmap(resultBitmap, null, frame, paint);
            } else {

                // Draw a two pixel solid black border inside the framing rect
                paint.setColor(frameColor);

                // Draw a red "laser scanner" line through the middle to show decoding is active
                paint.setColor(laserColor);
                paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
                int middle = frame.height() / 2 + frame.top;
                int middleX = frame.width() / 2 + frame.left;
                paint.setStyle(Paint.Style.STROKE);
                float oldWidth = paint.getStrokeWidth();
                paint.setStrokeWidth(2);
                canvas.drawCircle(middleX, middle, 15, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(oldWidth);

                Rect previewFrame = CameraManager.get().getFramingRectInPreview();
                float scaleX = frame.width() / (float) previewFrame.width();
                float scaleY = frame.height() / (float) previewFrame.height();

                List<ResultPoint> currentPossible = possibleResultPoints;
                List<ResultPoint> currentLast = lastPossibleResultPoints;
                if (currentPossible.isEmpty()) {
                    lastPossibleResultPoints = null;
                } else {
                    possibleResultPoints = new ArrayList<>(5);
                    lastPossibleResultPoints = currentPossible;
                    paint.setAlpha(CURRENT_POINT_OPACITY);
                    paint.setColor(resultPointColor);
                    synchronized (currentPossible) {
                        for (ResultPoint point : currentPossible) {
                            canvas.drawCircle(frame.left + (int) (point.getX() * scaleX),
                                    frame.top + (int) (point.getY() * scaleY), 6.0f, paint);
                        }
                    }
                }
                if (currentLast != null) {
                    paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                    paint.setColor(resultPointColor);
                    synchronized (currentLast) {
                        for (ResultPoint point : currentLast) {
                            canvas.drawCircle(frame.left + (int) (point.getX() * scaleX),
                                    frame.top + (int) (point.getY() * scaleY), 3.0f, paint);
                        }
                    }
                }

                // Request another update at the animation interval, but only repaint the laser line,
                // not the entire viewfinder mask.
                postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
            }
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onDraw  Exception : " + e.toString());
        }
    }

    public void drawViewfinder() {

        resultBitmap = null;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (point) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }
}