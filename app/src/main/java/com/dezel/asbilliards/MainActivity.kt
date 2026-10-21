package com.dezel.asbilliards

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(12, 35, 32)
        window.navigationBarColor = Color.BLACK

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(28, 28, 28, 28)
        layout.setBackgroundColor(Color.rgb(12, 35, 32))

        val title = TextView(this)
        title.text = "AS BILLIARDS AI"
        title.textSize = 28f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "مساعد تحليل مسارات البلياردو"
        subtitle.textSize = 18f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        val startButton = Button(this)
        startButton.text = "بدء تحليل الطاولة"

        startButton.setOnClickListener {
            subtitle.text =
                "جاهز للتحليل — سنضيف التعرف على الكرات في الخطوة التالية"
        }

        layout.addView(title)
        layout.addView(subtitle)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 35, 0, 0)

        layout.addView(startButton, params)

        setContentView(layout)
    }
}
