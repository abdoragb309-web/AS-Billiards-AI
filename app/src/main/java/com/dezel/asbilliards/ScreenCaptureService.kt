package com.dezel.asbilliards

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.content.Context
import android.content.pm.ServiceInfo

class ScreenCaptureService : Service() {

companion object {
    const val EXTRA_RESULT_CODE = "result_code"
    const val EXTRA_RESULT_DATA = "result_data"
    private const val CHANNEL_ID = "as_billiards_capture"
    private const val NOTIFICATION_ID = 1001
}

private var projection: MediaProjection? = null

override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
}

override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int
): Int {

    if (Build.VERSION.SDK_INT >= 29) {
        startForeground(
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
    } else {
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )
    }

    val resultCode = intent?.getIntExtra(
        EXTRA_RESULT_CODE, 0
    ) ?: 0

    val resultData = if (Build.VERSION.SDK_INT >= 33) {
        intent?.getParcelableExtra(
            EXTRA_RESULT_DATA,
            Intent::class.java
        )
    } else {
        @Suppress("DEPRECATION")
        intent?.getParcelableExtra(EXTRA_RESULT_DATA)
    }

    if (resultData == null) {
        stopSelf()
        return START_NOT_STICKY
    }

    val manager = getSystemService(
        Context.MEDIA_PROJECTION_SERVICE
    ) as MediaProjectionManager

    projection = manager.getMediaProjection(
        resultCode,
        resultData
    )

    if (projection == null) {
        stopSelf()
        return START_NOT_STICKY
    }

    // تم بدء جلسة MediaProjection.
    // سنضيف التقاط الصور وتحليل الطاولة في الخطوة التالية.

    return START_NOT_STICKY
}

private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AS Billiards Screen Capture",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }
}

private fun createNotification(): Notification {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AS BILLIARDS AI")
            .setContentText("جلسة التقاط الشاشة نشطة")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    } else {
        @Suppress("DEPRECATION")
        Notification.Builder(this)
            .setContentTitle("AS BILLIARDS AI")
            .setContentText("جلسة التقاط الشاشة نشطة")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }
}

override fun onDestroy() {
    projection?.stop()
    projection = null
    super.onDestroy()
}

override fun onBind(intent: Intent?): IBinder? = null

}
