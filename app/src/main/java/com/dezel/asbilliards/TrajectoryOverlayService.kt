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

    private var windowManager: WindowManager? = null
    private var overlayView: TrajectoryOverlay? = null

    companion object {
        const val ACTION_SHOW =
            "com.dezel.asbilliards.SHOW_OVERLAY"

        const val ACTION_HIDE =
            "com.dezel.asbilliards.HIDE_OVERLAY"
    }

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {
            ACTION_HIDE -> hideOverlay()
            ACTION_SHOW, null -> showOverlay()
        }

        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        if (overlayView != null) return

        val wm = windowManager ?: return

        val view = TrajectoryOverlay(this).apply {
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
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            wm.addView(view, params)
            overlayView = view
        } catch (_: Exception) {
            overlayView = null
            stopSelf()
        }
    }

    private fun hideOverlay() {
        val view = overlayView ?: return

        try {
            windowManager?.removeView(view)
        } catch (_: Exception) {
        }

        overlayView = null
    }

    override fun onDestroy() {
        hideOverlay()
        windowManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
