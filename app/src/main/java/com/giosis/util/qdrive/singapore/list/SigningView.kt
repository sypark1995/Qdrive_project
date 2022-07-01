package com.giosis.util.qdrive.singapore.list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.giosis.util.qdrive.singapore.util.DisplayUtil

class SigningView : View {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }

    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var mPath: Path? = null
    private var mBitmapPaint: Paint? = null
    private var mPaint: Paint? = null
    private var bIsClear = false
    private var bIsDrawStart = false
    var isTouch = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {

        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.color = -0x1000000
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.strokeWidth = 8f

        val width: Int = DisplayUtil.DPFromPixel(context, 480)
        val height: Int = DisplayUtil.DPFromPixel(context, 480)

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
        mPath = Path()
        mBitmapPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        if (bIsClear) {
            val width: Int = DisplayUtil.DPFromPixel(context, 480)
            val height: Int = DisplayUtil.DPFromPixel(context, 480)
            mBitmap!!.recycle()
            mBitmap = null
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mCanvas!!.setBitmap(mBitmap)
            bIsClear = false
        }

        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
        if (bIsDrawStart) {
            if (mX != 0f && mY != 0f) {
                canvas.drawPath(mPath!!, mPaint!!)
                canvas.drawPoint(mX, mY, mPaint!!)
            }
        }
    }

    fun clearText() {
        bIsClear = true
        bIsDrawStart = false
        isTouch = false
        mPath!!.reset()
        mX = 0f
        mY = 0f
        invalidate()
    }

    private var mX = 0f
    private var mY = 0f
    private fun touch_start(x: Float, y: Float) {
        isTouch = false
        mPath!!.reset()
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
        bIsDrawStart = true
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touch_up() {
        // commit the path to our offscreen
        mPath!!.lineTo(mX, mY)
        mCanvas!!.drawPath(mPath!!, mPaint!!)
        mCanvas!!.drawPoint(mX, mY, mPaint!!)
        mPath!!.reset()
        bIsDrawStart = false
        // kill this so we don't double draw
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerCount = event.pointerCount
        isTouch = true
        for (p in 0 until pointerCount) {
            if (p == 0) {
                val x = event.x
                val y = event.y
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        this.parent.requestDisallowInterceptTouchEvent(true)
                        touch_start(x, y)
                        invalidate()
                    }
                    MotionEvent.ACTION_MOVE -> if (mX != 0f && mY != 0f) {
                        touch_move(x, y)
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> if (mX != 0f && mY != 0f) {
                        this.parent.requestDisallowInterceptTouchEvent(false)
                        touch_up()
                        invalidate()
                    }
                }
            }
        }
        return true
    }

}