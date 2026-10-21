package com.dezel.asbilliards

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class TrajectoryOverlayService : Service() {

private lateinit var windowManager: WindowManager
private var overlayView: TrajectoryOverlay? = null

override fun onCreate() {
    super.onCreate()

    if (!Settings.canDrawOverlays(this)) {
        stopSelf()
        return
    }

    windowManager =
        getSystemService(WINDOW_SERVICE) as WindowManager

    overlayView = TrajectoryOverlay(this).apply {
        setBackgroundColor(Color.TRANSPARENT)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    val windowType =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        windowType,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    try {
        windowManager.addView(overlayView, params)
    } catch (e: Exception) {
        overlayView = null
        stopSelf()
    }
}

override fun onDestroy() {
    overlayView?.let { view ->
        try {
            windowManager.removeView(view)
        } catch (_: Exception) {
        }
    }

    overlayView = null
    super.onDestroy()
}

override fun onBind(intent: Intent?): IBinder? = null

}
