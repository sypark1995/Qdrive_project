package com.giosis.util.qdrive.list;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.giosis.util.qdrive.util.DisplayUtil;

public class SigningView extends View {

    public SigningView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;

    private boolean bIsClear = false;
    private boolean bIsDrawStart = false;
    private boolean bIsTouche = false;

    private Context mContext;

    public SigningView(Context context) {
        super(context);
        init(context);
    }

    public void setIsTouche(boolean status) {
        this.bIsTouche = status;
    }

    public boolean getIsTouche() {
        return this.bIsTouche;
    }


    public SigningView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Log.e("krm0219", "SingingView init");
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);

        int width = DisplayUtil.DPFromPixel(context, 480);
        int height = DisplayUtil.DPFromPixel(context, 480);

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (bIsClear) {
            int width = DisplayUtil.DPFromPixel(mContext, 480);
            int height = DisplayUtil.DPFromPixel(mContext, 480);

            mBitmap.recycle();
            mBitmap = null;
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas.setBitmap(mBitmap);
            bIsClear = false;
        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (bIsDrawStart) {
            if (mX != 0 && mY != 0) {
                canvas.drawPath(mPath, mPaint);
                canvas.drawPoint(mX, mY, mPaint);
            }
        }
    }

    public void clearText() {
        bIsClear = true;
        bIsDrawStart = false;
        bIsTouche = false;
        mPath.reset();
        mX = 0;
        mY = 0;

        invalidate();
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        setIsTouche(false);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        bIsDrawStart = true;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        // commit the path to our offscreen

        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mCanvas.drawPoint(mX, mY, mPaint);
        mPath.reset();
        bIsDrawStart = false;

        // kill this so we don't double draw
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int pointerCount = event.getPointerCount();
        setIsTouche(true);

        for (int p = 0; p < pointerCount; p++) {
            if (p == 0) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        this.getParent().requestDisallowInterceptTouchEvent(true);
                        touch_start(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mX != 0 && mY != 0) {
                            touch_move(x, y);
                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mX != 0 && mY != 0) {
                            this.getParent().requestDisallowInterceptTouchEvent(false);
                            touch_up();
                            invalidate();
                        }
                        break;
                }
            }
        }

        return true;
    }
}