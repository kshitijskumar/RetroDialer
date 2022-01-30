package com.example.retrodialer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

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

    private var onCodeCompleteListener: ((String) -> Unit)? = null

    private var finalWidth: Int = 0
    private var finalHeight: Int = 0

    private var codeGeneratedTillNow: String = ""

    private val xCenterOfParentCircle: Float get() = finalWidth/2f
    private val yCenterOfParentCircle: Float get() = finalHeight/2f
    private val mainCircleRadius: Float get() = min(finalHeight, finalWidth)/2f

    private var dialerRotatedAngle = 0

    private var maxCodeLength: Int = 4

    private val angleToNumberMap = mutableMapOf<Int, Int>()
    init {
        setupAngleToNumberMap()
    }

    private val mainCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
    }

    private val internalBitmap: Bitmap by lazy {
        Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
    }

    private val internalCanvas: Canvas by lazy {
        Canvas(internalBitmap)
    }

    private val dialerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
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

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 14f
        isDither = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {

            drawBaseCircle(it)
            printTextOnDialer(it, 150.toDouble(), textPaint, 1)

            drawBorderAndDecoratorCircle(it)

            nextHoleInCircle(internalCanvas, (150 + dialerRotatedAngle).toDouble(), holePaint, 1)
            internalCanvas.drawRoundRect((finalWidth - mainCircleRadius/2), (finalHeight/2 - 8f), finalWidth.toFloat(), (finalHeight/2 + 8f), 2f, 2f, dialerLinePaint)

            canvas.drawBitmap(internalBitmap, 0f, 0f, null)

        }
    }

    private fun drawBaseCircle(canvas: Canvas) {
        mainCirclePaint.color = Color.BLACK
        canvas.drawCircle(xCenterOfParentCircle, yCenterOfParentCircle, mainCircleRadius, mainCirclePaint)
    }

    private fun drawBorderAndDecoratorCircle(canvas: Canvas) {
        mainCirclePaint.color = Color.WHITE
        internalCanvas.drawCircle(xCenterOfParentCircle, yCenterOfParentCircle, mainCircleRadius, mainCirclePaint)
        borderPaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 14f
            isDither = true
        }
        internalCanvas.drawCircle(xCenterOfParentCircle, yCenterOfParentCircle, mainCircleRadius-7, borderPaint)
        borderPaint.apply {
            style = Paint.Style.FILL_AND_STROKE
        }
        val innerCircleRadius = mainCircleRadius - 2* TEXT_INSET_FROM_BORDER - 2* HOLE_RADIUS
        internalCanvas.drawCircle(xCenterOfParentCircle, yCenterOfParentCircle, innerCircleRadius, borderPaint)
    }

    private fun nextHoleInCircle(holeCanvas: Canvas, angle: Double, paint: Paint, count: Int) {
        if (count == 11) return

        val radius = mainCircleRadius - (TEXT_INSET_FROM_BORDER + HOLE_RADIUS)
        val newX = (radius * sin(Math.toRadians(angle))).toFloat() + xCenterOfParentCircle
        val newY = (radius * cos(Math.toRadians(angle))).toFloat() + yCenterOfParentCircle

        holeCanvas.drawCircle(newX, newY, HOLE_RADIUS, paint)

        nextHoleInCircle(holeCanvas, angle+30, paint, count+1)
    }

    private fun printTextOnDialer(textCanvas: Canvas, angle: Double, paint: Paint, count: Int) {
        if (count == 11) return

        val radius = mainCircleRadius - (TEXT_INSET_FROM_BORDER + HOLE_RADIUS)
        val newX = (radius * sin(Math.toRadians(angle))).toFloat() + xCenterOfParentCircle
        val newY = (radius * cos(Math.toRadians(angle))).toFloat() + yCenterOfParentCircle

        textCanvas.drawText((count-1).toString(), newX-10, newY+5, paint)

        printTextOnDialer(textCanvas, angle+30, paint, count+1)
    }

    private fun angleBetweenTwoLines(x1: Float, y1: Float, x2: Float, y2: Float) {
        val m1 = (y2 - yCenterOfParentCircle) / (x2 - xCenterOfParentCircle)
        val m2 = (y1 - yCenterOfParentCircle) / (x1 - xCenterOfParentCircle)

        val tanTheta = (m2 - m1) / (1 + m1*m2)

        val angle = atan(tanTheta.toDouble())

        val pureRotation = Math.toDegrees(angle).toInt()
        if ((abs(dialerRotatedAngle + pureRotation) >= 335) || dialerRotatedAngle + pureRotation >= 0) {
            return
        }
        dialerRotatedAngle += pureRotation
        dx = x2
        dy = y2
        Log.d("RotatingAngle", "ang: $dialerRotatedAngle")
        invalidate()
    }

    private var dx = 0f
    private var dy = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dx = event.x
                dy = event.y
                true
            }
            MotionEvent.ACTION_MOVE -> {
                angleBetweenTwoLines(dx, dy, event.x, event.y)
                true
            }
            MotionEvent.ACTION_UP -> {
                addLastNumberToCodeTillNow()
                reverse()
                true
            }
            else -> false
        }
    }

    private fun reverse() {
        ValueAnimator.ofInt(dialerRotatedAngle, 0).apply {
            addUpdateListener {
                val currentValue = it.animatedValue.toString().toInt()
                dialerRotatedAngle = currentValue
                invalidate()
            }
            start()
        }
    }

    private fun setupAngleToNumberMap() {
        (0..9).forEach { number ->
            val idealAngle = 60 + number * 30
            angleToNumberMap[idealAngle] = number
        }
    }

    private fun addLastNumberToCodeTillNow() {
        val totalAngleRevolved = abs(dialerRotatedAngle)
        for (idealAngle in angleToNumberMap.keys) {
            if (totalAngleRevolved <= idealAngle+10 && totalAngleRevolved >= idealAngle-10) {
                codeGeneratedTillNow += (angleToNumberMap[idealAngle]).toString()
                Log.d("CodeGenerated", "code: $codeGeneratedTillNow")
                break
            }
        }
        if (codeGeneratedTillNow.length == maxCodeLength) {
            onCodeCompleteListener?.invoke(codeGeneratedTillNow)
            codeGeneratedTillNow = ""
        }
    }

    fun setOnCodeCompleteListener(listener: (String) -> Unit) {
        onCodeCompleteListener = listener
    }

}