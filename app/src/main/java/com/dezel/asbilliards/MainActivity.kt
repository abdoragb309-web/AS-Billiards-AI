package com.dezel.asbilliards

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var statusText: TextView

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
                    "التحليل البصري لم يُفعّل بعد"
            }
        }

        val screenButton = Button(this).apply {
            text = "إعداد التقاط الشاشة"
            setOnClickListener {
                statusText.text =
                    "سنضيف خدمة التقاط الشاشة في الخطوة التالية"
            }
        }

        root.addView(title)
        root.addView(subtitle)

        root.addView(statusText, spacedParams())

        root.addView(analyzeButton, spacedParams())
        root.addView(screenButton, spacedParams())

        setContentView(root)
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
