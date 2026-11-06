package com.example.ecommerceapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val data = mutableMapOf<String, Float>()

    private val colors = listOf(
        0xFF7E57C2.toInt(),
        0xFF42A5F5.toInt(),
        0xFF66BB6A.toInt(),
        0xFFFFA726.toInt(),
        0xFFEF5350.toInt(),
        0xFF26A69A.toInt()
    )

    fun setData(newData: Map<String, Float>) {
        data.clear()
        data.putAll(newData)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) {
            paint.color = 0xFFCCCCCC.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 35f

            val size = min(width, height) * 0.55f
            val left = (width - size) / 2f
            val top = (height - size) / 2f
            val right = left + size
            val bottom = top + size

            canvas.drawArc(
                RectF(left, top, right, bottom),
                0f,
                360f,
                false,
                paint
            )

            paint.style = Paint.Style.FILL
            return
        }

        val total = data.values.sum()

        if (total <= 0f) {
            return
        }

        val size = min(width, height) * 0.65f
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val right = left + size
        val bottom = top + size

        val rect = RectF(left, top, right, bottom)

        var startAngle = -90f

        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f

            paint.style = Paint.Style.FILL
            paint.color = colors[index % colors.size]

            canvas.drawArc(
                rect,
                startAngle,
                sweepAngle,
                true,
                paint
            )

            startAngle += sweepAngle
        }

        paint.color = 0xFFF5F5F5.toInt()

        val centerX = width / 2f
        val centerY = height / 2f
        val holeRadius = size * 0.22f

        canvas.drawCircle(
            centerX,
            centerY,
            holeRadius,
            paint
        )
    }
}