package com.dezel.asbilliards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

private lateinit var statusText: TextView

private val captureRequestCode = 2001

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.statusBarColor = Color.rgb(10, 35, 30)
    window.navigationBarColor = Color.BLACK

    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(28, 32, 28, 32)
        setBackgroundColor(Color.rgb(10, 35, 30))
    }

    val title = TextView(this).apply {
        text = "AS BILLIARDS AI"
        textSize = 28f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
    }

    val subtitle = TextView(this).apply {
        text = "مساعد تحليل مسارات البلياردو"
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(Color.LTGRAY)
    }

    statusText = TextView(this).apply {
        text = "حالة النظام: جاهز"
        textSize = 16f
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(100, 255, 160))
    }

    val analyzeButton = Button(this).apply {
        text = "تحليل الطاولة"
        setOnClickListener {
            statusText.text =
                "اكتشاف الكرات وحساب مساراتها لم يُفعّل بعد"
        }
    }

    val screenButton = Button(this).apply {
        text = "بدء التقاط الشاشة"
        setOnClickListener {
            requestScreenCapture()
        }
    }

    val overlayButton = Button(this).apply {
        text = "إظهار خطوط تجريبية فوق الشاشة"
        setOnClickListener {
            openTrajectoryOverlay()
        }
    }

    root.addView(title)
    root.addView(subtitle)
    root.addView(statusText, spacedParams())
    root.addView(analyzeButton, spacedParams())
    root.addView(screenButton, spacedParams())
    root.addView(overlayButton, spacedParams())

    setContentView(root)
}

private fun openTrajectoryOverlay() {
    if (!Settings.canDrawOverlays(this)) {
        statusText.text =
            "اسمح للتطبيق بالظهور فوق التطبيقات الأخرى"

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )

        startActivity(intent)
        return
    }

    try {
        startService(
            Intent(this, TrajectoryOverlayService::class.java)
        )

        statusText.text =
            "تم طلب عرض الخطوط التجريبية فوق الشاشة"

    } catch (e: Exception) {
        statusText.text =
            "تعذر تشغيل الخطوط: ${e.message}"
    }
}

private fun requestScreenCapture() {
    val manager = getSystemService(
        Context.MEDIA_PROJECTION_SERVICE
    ) as MediaProjectionManager

    statusText.text =
        "بانتظار موافقتك على التقاط الشاشة..."

    startActivityForResult(
        manager.createScreenCaptureIntent(),
        captureRequestCode
    )
}

@Deprecated("Deprecated in Java")
override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode != captureRequestCode) return

    if (resultCode == RESULT_OK && data != null) {
        val serviceIntent = Intent(
            this,
            ScreenCaptureService::class.java
        ).apply {
            putExtra(
                ScreenCaptureService.EXTRA_RESULT_CODE,
                resultCode
            )

            putExtra(
                ScreenCaptureService.EXTRA_RESULT_DATA,
                data
            )
        }

        try {
            startForegroundService(serviceIntent)

            statusText.text =
                "تم إرسال إذن الشاشة إلى الخدمة."

        } catch (e: Exception) {
            statusText.text =
                "تعذر تشغيل خدمة الالتقاط: ${e.message}"
        }

    } else {
        statusText.text =
            "تم إلغاء إذن التقاط الشاشة."
    }
}

private fun spacedParams(): LinearLayout.LayoutParams {
    return LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, 14, 0, 14)
    }
}

}
