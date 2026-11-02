package com.dezel.asbilliards

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager

import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val handler = Handler(Looper.getMainLooper())

    private var lastAnalysisTime = 0L

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"

        private const val CHANNEL_ID = "as_billiards_capture"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        if (!OpenCVLoader.initDebug()) {
            stopSelf()
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification("جاري تشغيل تحليل الشاشة")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }

        val resultCode = intent.getIntExtra(
            EXTRA_RESULT_CODE,
            -1
        )

        val resultData: Intent? =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(
                    EXTRA_RESULT_DATA,
                    Intent::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_RESULT_DATA)
            }

        if (resultCode == -1 || resultData == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val manager = getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

            mediaProjection = manager.getMediaProjection(
                resultCode,
                resultData
            )

            mediaProjection?.registerCallback(
                object : MediaProjection.Callback() {
                    override fun onStop() {
                        stopSelf()
                    }
                },
                handler
            )

            startCapture()

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startCapture() {

        val metrics = resources.displayMetrics

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(
            width,
            height,
            PixelFormat.RGBA_8888,
            2
        )

        imageReader?.setOnImageAvailableListener(
            { reader ->

                val image = reader.acquireLatestImage()

                if (image != null) {

                    try {
                        val now = System.currentTimeMillis()

                        if (now - lastAnalysisTime >= 500) {

                            lastAnalysisTime = now

                            val count = analyzeImage(image)

                            sendAnalysisResult(count)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        image.close()
                    }
                }

            },
            handler
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "AS_BILLIARDS_CAPTURE",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )
    }

    private fun analyzeImage(image: Image): Int {

        val plane = image.planes[0]
        val buffer = plane.buffer.duplicate()

        val width = image.width
        val height = image.height

        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val bufferLimit = buffer.limit()

        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {

                val index =
                    y * rowStride + x * pixelStride

                if (index + 3 >= bufferLimit) continue

                val r = buffer.get(index).toInt() and 0xFF
                val g = buffer.get(index + 1).toInt() and 0
