package com.example.retrodialer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RetroDialer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_DIMENSION = 600
        private const val TEXT_INSET_FROM_BORDER = 40f
        private const val HOLE_RADIUS = 40f
    }


    private var finalWidth: Int = 0
    private var finalHeight: Int = 0

    private val xCenterOfParentCircle: Float get() = finalWidth/2f
    private val yCenterOfParentCircle: Float get() = finalHeight/2f
    private val mainCircleRadius: Float get() = min(finalHeight, finalWidth)/2f

    private val mainCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
    }

    private val internalBitmap: Bitmap by lazy {
        Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
    }

    private val internalCanvas: Canvas by lazy {
        Canvas(internalBitmap)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        finalWidth = when(MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> parentWidth
            else -> DEFAULT_DIMENSION
        }

        finalHeight = when(MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> parentHeight
            else -> DEFAULT_DIMENSION
        }

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {

            canvas.drawCircle(xCenterOfParentCircle, yCenterOfParentCircle, mainCircleRadius, mainCirclePaint)
            printTextOnDialer(it, 180.toDouble(), textPaint, 1)

            mainCirclePaint.color = Color.RED
            internalCanvas.drawCircle(xCenterOfParentCircle, yCenterOfParentCircle, mainCircleRadius, mainCirclePaint)

            val angleRotated = 0
            nextHoleInCircle(internalCanvas, (180 + angleRotated).toDouble(), holePaint, 1)

            canvas.drawBitmap(internalBitmap, 0f, 0f, null)

        }
    }

    private fun nextHoleInCircle(holeCanvas: Canvas, angle: Double, paint: Paint, count: Int) {
        if (count == 11) return

        val radius = mainCircleRadius - (TEXT_INSET_FROM_BORDER + HOLE_RADIUS)
        val newX = (radius * sin(Math.toRadians(angle))).toFloat() + xCenterOfParentCircle
        val newY = (radius * cos(Math.toRadians(angle))).toFloat() + yCenterOfParentCircle

        holeCanvas.drawCircle(newX, newY, HOLE_RADIUS, paint)

        nextHoleInCircle(holeCanvas, angle-30, paint, count+1)
    }

    private fun printTextOnDialer(textCanvas: Canvas, angle: Double, paint: Paint, count: Int) {
        if (count == 11) return

        val radius = mainCircleRadius - (TEXT_INSET_FROM_BORDER + HOLE_RADIUS)
        val newX = (radius * sin(Math.toRadians(angle))).toFloat() + xCenterOfParentCircle
        val newY = (radius * cos(Math.toRadians(angle))).toFloat() + yCenterOfParentCircle

        textCanvas.drawText((count-1).toString(), newX-10, newY+5, paint)

        printTextOnDialer(textCanvas, angle-30, paint, count+1)
    }

}