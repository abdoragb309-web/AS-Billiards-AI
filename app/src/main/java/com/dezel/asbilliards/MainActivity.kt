package com.dezel.asbilliards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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

private val green = Color.rgb(0, 220, 130)
private val background = Color.rgb(10, 35, 30)

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.statusBarColor = background
    window.navigationBarColor = Color.BLACK

    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(28, 24, 28, 24)
        setBackgroundColor(background)
    }

    val title = TextView(this).apply {
        text = "AS BILLIARDS AI"
        textSize = 28f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
    }

    val subtitle = TextView(this).apply {
        text = "مساعد مسارات البلياردو 🎱"
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(Color.LTGRAY)
    }

    statusText = TextView(this).apply {
        text = "حالة النظام: جاهز"
        textSize = 16f
        gravity = Gravity.CENTER
        setTextColor(green)
        setPadding(8, 20, 8, 20)
    }

    val showButton = makeButton(
        "🟢 إظهار مسارات الكرات",
        green
    ) {
        openTrajectoryOverlay()
    }

    val hideButton = makeButton(
        "🔴 إخفاء مسارات الكرات",
        Color.rgb(255, 90, 90)
    ) {
        sendOverlayAction(
            TrajectoryOverlayService.ACTION_HIDE,
            "تم إخفاء المسارات"
        )
    }

    val screenButton = makeButton(
        "📱 بدء التقاط الشاشة",
        Color.rgb(60, 150, 255)
    ) {
        requestScreenCapture()
    }

    val analyzeButton = makeButton(
        "🎱 تحليل الطاولة",
        Color.rgb(255, 190, 40)
    ) {
        statusText.text =
            "تحليل مواقع الكرات لم يُفعّل بعد"
    }

    val note = TextView(this).apply {
        text = "الإصدار التجريبي\nالمسارات الحالية تجريبية وليست محسوبة من الكرات الحقيقية."
        textSize = 13f
        gravity = Gravity.CENTER
        setTextColor(Color.LTGRAY)
        setPadding(8, 20, 8, 8)
    }

    root.addView(title, spacedParams())
    root.addView(subtitle, spacedParams())
    root.addView(statusText, spacedParams())
    root.addView(showButton, spacedParams())
    root.addView(hideButton, spacedParams())
    root.addView(screenButton, spacedParams())
    root.addView(analyzeButton, spacedParams())
    root.addView(note, spacedParams())

    setContentView(root)
}

private fun makeButton(
    label: String,
    color: Int,
    action: () -> Unit
): Button {
    return Button(this).apply {
        text = label
        textSize = 16f
        setTextColor(Color.WHITE)
        setBackgroundColor(color)
        isAllCaps = false
        setOnClickListener { action() }
    }
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

    sendOverlayAction(
        TrajectoryOverlayService.ACTION_SHOW,
        "تم طلب إظهار المسارات التجريبية"
    )
}

private fun sendOverlayAction(
    action: String,
    message: String
) {
    try {
        val intent = Intent(
            this,
            TrajectoryOverlayService::class.java
        ).apply {
            this.action = action
        }

        startService(intent)
        statusText.text = message

    } catch (e: Exception) {
        statusText.text =
            "تعذر تنفيذ الأمر: ${e.message}"
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
                "تم إرسال إذن التقاط الشاشة إلى الخدمة"

        } catch (e: Exception) {
            statusText.text =
                "تعذر تشغيل خدمة الالتقاط: ${e.message}"
        }

    } else {
        statusText.text =
            "تم إلغاء إذن التقاط الشاشة"
    }
}

private fun spacedParams(): LinearLayout.LayoutParams {
    return LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, 6, 0, 6)
    }
}

}
