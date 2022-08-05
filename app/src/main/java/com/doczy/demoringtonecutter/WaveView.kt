package com.doczy.demoringtonecutter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.roundToInt

interface WaveProgressListener {
    fun onProgressChange(start: Int, end: Int)
}

class WaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mCanvasWidth = 0        // Chiều rộng view
    private var mCanvasHeight = 0       // Chiều cao view
    private var mTouchDownX = 0f        // Tọa độ điểm chạm khi vừa nhấn ACTION_DOWN


    private var mWaveWidth = 10f        // Chiều rộng 1 line
    private var mWaveGap = 5f           // Khoảng cách giữa 2 line
    private var mWaveCorner = 10f       // Bo góc 1 line
    private val step = 20               // Bước nhảy, cứ sau `step` mới vẽ 1 line

    private var visibleStart = 0
    private var visibleEnd = 0

    var listener: WaveProgressListener? = null


    private var rootX = 0f      // Tọa độ X phần tử đầu tiên
    private var oldRootX = 0f   // Tọa độ cũ của rootX khi vừa ACTION_DOWN

    // Mảng dữ liệu chiều cao từng line
    var data: IntArray? = null
        set(value) {
            field = value
            setMaxValue()
            invalidate()
        }

    // Giá trị line lớn nhất
    private var mMaxValue = 0


    private fun setMaxValue() {
        mMaxValue = data?.maxOrNull() ?: 0
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mWavePaint.color = Color.parseColor("#ff00ff")

        var newVisibleStart = visibleStart

        data?.let {
            // Lấy vị trị index vẽ đầu tiên
            var index = max((-rootX / (mWaveWidth + mWaveGap)).roundToInt(), 0)
            newVisibleStart = index

            while (index * step < it.size) {

                var waveHeight = it[index * step].toFloat() / mMaxValue * mCanvasHeight
                waveHeight = max(20f, waveHeight)

                val rect = RectF(
                    index * (mWaveWidth + mWaveGap) + rootX,
                    mCanvasHeight / 2f - waveHeight / 2f,
                    index * (mWaveWidth + mWaveGap) + mWaveWidth + rootX,
                    mCanvasHeight / 2f + waveHeight / 2f
                )
                canvas.drawRoundRect(
                    rect, mWaveCorner, mWaveCorner, mWavePaint
                )

                index++

                // Nếu index vượt quá màn hình thì không cần vẽ nữa
                if (index * (mWaveWidth + mWaveGap) + rootX > mCanvasWidth) {
                    break
                }
            }
        }

        if (newVisibleStart != visibleStart) {
            visibleStart = newVisibleStart
            listener?.onProgressChange(visibleStart, visibleEnd)
        }
    }

    private fun getTotalWidth(): Int {
        data?.let {
            return (it.size.toFloat() / step).roundToInt() * (mWaveWidth + mWaveGap).toInt()
        }
        return 0
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchDownX = event.x
                oldRootX = rootX
            }
            MotionEvent.ACTION_MOVE -> {
                val distance = event.x - mTouchDownX
                rootX = (oldRootX + distance)

                if (rootX > 0) rootX = 0f
                if (rootX < -getTotalWidth() + mCanvasWidth) rootX =
                    -getTotalWidth().toFloat() + mCanvasWidth
            }
        }

        // vẽ lại UI
        invalidate()
        return true
    }
}