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
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

private lateinit var statusText: TextView

private val captureRequestCode = 2001

private val backgroundColor = Color.rgb(10, 35, 30)
private val neonGreenColor = Color.rgb(0, 180, 100)

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.statusBarColor = backgroundColor
    window.navigationBarColor = Color.BLACK

    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(24, 24, 24, 24)
        setBackgroundColor(backgroundColor)
    }

    val title = TextView(this).apply {
        text = "AS BILLIARDS AI"
        textSize = 28f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
    }

    val subtitle = TextView(this).apply {
        text = "مساعد تحليل مسارات البلياردو 🎱"
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(Color.LTGRAY)
    }

    statusText = TextView(this).apply {
        text = "حالة النظام: جاهز"
        textSize = 16f
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(100, 255, 160))
        setPadding(8, 16, 8, 16)
    }

    val showButton = makeButton(
        "إظهار مسارات الكرات",
        neonGreenColor
    ) {
        openTrajectoryOverlay()
    }

    val hideButton = makeButton(
        "إخفاء مسارات الكرات",
        Color.rgb(190, 45, 45)
    ) {
        sendOverlayAction(
            TrajectoryOverlayService.ACTION_HIDE,
            "تم طلب إخفاء المسارات"
        )
    }

    val screenButton = makeButton(
        "بدء التقاط الشاشة",
        Color.rgb(40, 100, 190)
    ) {
        requestScreenCapture()
    }

    val analyzeButton = makeButton(
        "تحليل الطاولة",
        Color.rgb(170, 120, 20)
    ) {
        statusText.text =
            "تحليل مواقع الكرات الحقيقي لم يُفعّل بعد"
    }

    val note = TextView(this).apply {
        text = "الإصدار التجريبي\nالمسارات الحالية تجريبية، ولا تتبع مواقع الكرات الحقيقية بعد."
        textSize = 13f
        gravity = Gravity.CENTER
        setTextColor(Color.LTGRAY)
        setPadding(8, 16, 8, 8)
    }

    root.addView(title, spacedParams())
    root.addView(subtitle, spacedParams())
    root.addView(statusText, spacedParams())
    root.addView(showButton, spacedParams())
    root.addView(hideButton, spacedParams())
    root.addView(screenButton, spacedParams())
    root.addView(analyzeButton, spacedParams())
    root.addView(note, spacedParams())

    val scrollView = ScrollView(this).apply {
        fillViewport = true
        setBackgroundColor(backgroundColor)
        addView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    setContentView(scrollView)
}

private fun makeButton(
    label: String,
    buttonColor: Int,
    action: () -> Unit
): Button {
    return Button(this).apply {
        text = label
        textSize = 16f
        setTextColor(Color.WHITE)
        setBackgroundColor(buttonColor)
        isAllCaps = false

        setOnClickListener {
            action()
        }
    }
}

private fun openTrajectoryOverlay() {
    if (!Settings.canDrawOverlays(this)) {
        statusText.text =
            "اسمح للتطبيق بالظهور فوق التطبيقات الأخرى"

        val settingsIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )

        startActivity(settingsIntent)
        return
    }

    sendOverlayAction(
        TrajectoryOverlayService.ACTION_SHOW,
        "تم إرسال طلب إظهار المسارات"
    )
}

private fun sendOverlayAction(
    action: String,
    message: String
) {
    try {
        val serviceIntent = Intent(
            this,
            TrajectoryOverlayService::class.java
        ).apply {
            this.action = action
        }

        startService(serviceIntent)
        statusText.text = message

    } catch (e: Exception) {
        statusText.text =
            "تعذر تنفيذ الأمر: ${e.message ?: "خطأ غير معروف"}"
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

    if (requestCode != captureRequestCode) {
        return
    }

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
                "تم إرسال إذن الشاشة إلى خدمة الالتقاط"

        } catch (e: Exception) {
            statusText.text =
                "تعذر تشغيل خدمة الالتقاط: ${e.message ?: "خطأ غير معروف"}"
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
