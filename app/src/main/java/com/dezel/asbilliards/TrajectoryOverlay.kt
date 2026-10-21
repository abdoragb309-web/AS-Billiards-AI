package com.dezel.asbilliards

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class TrajectoryOverlay(context: Context) : View(context) {

private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 5f
    strokeCap = Paint.Cap.ROUND
}

private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
}

override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // مسارات تجريبية للتأكد من الرسم فقط.
    // لاحقًا سنستبدلها بمسارات محسوبة من مواقع الكرات الفعلية.

    drawTrajectory(
        canvas,
        0.15f, 0.70f,
        0.75f, 0.30f,
        Color.CYAN
    )

    drawTrajectory(
        canvas,
        0.35f, 0.55f,
        0.85f, 0.55f,
        Color.YELLOW
    )

    drawTrajectory(
        canvas,
        0.60f, 0.75f,
        0.25f, 0.25f,
        Color.GREEN
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

    linePaint.color = color
    canvas.drawLine(x1, y1, x2, y2, linePaint)

    ballPaint.color = Color.WHITE
    canvas.drawCircle(x1, y1, 8f, ballPaint)
}

}
