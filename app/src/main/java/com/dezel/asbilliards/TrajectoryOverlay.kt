package com.dezel.asbilliards

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class TrajectoryOverlay(context: Context) : View(context) {

    // توهج خلفي للخطوط
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        strokeCap = Paint.Cap.ROUND
        alpha = 100
    }

    // الخط الأساسي
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    // دوائر بداية المسارات
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // حدود الدوائر
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // مسارات تجريبية فقط، وليست محسوبة من مواقع الكرات الحقيقية.

        drawTrajectory(
            canvas,
            0.15f, 0.70f,
            0.75f, 0.30f,
            Color.rgb(0, 230, 255)
        )

        drawTrajectory(
            canvas,
            0.35f, 0.55f,
            0.85f, 0.55f,
            Color.rgb(255, 235, 0)
        )

        drawTrajectory(
            canvas,
            0.60f, 0.75f,
            0.25f, 0.25f,
            Color.rgb(0, 255, 100)
        )
    }

    private fun drawTrajectory(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: Int
    ) {
        val x1 = width * startX
        val y1 = height * startY
        val x2 = width * endX
        val y2 = height * endY

        // توهج ملون خلف الخط
        glowPaint.color = color
        glowPaint.alpha = 90
        canvas.drawLine(x1, y1, x2, y2, glowPaint)

        // الخط الملون الواضح
        linePaint.color = color
        canvas.drawLine(x1, y1, x2, y2, linePaint)

        // دائرة بداية المسار
        ballPaint.color = Color.WHITE
        canvas.drawCircle(x1, y1, 12f, ballPaint)

        // إطار الدائرة
        borderPaint.color = color
        canvas.drawCircle(x1, y1, 12f, borderPaint)
    }
}
