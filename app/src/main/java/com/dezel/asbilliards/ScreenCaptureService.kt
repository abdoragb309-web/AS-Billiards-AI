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

import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ScreenCaptureService : Service() {

companion object {  
    const val EXTRA_RESULT_CODE = "result_code"  
    const val EXTRA_RESULT_DATA = "result_data"  

    private const val CHANNEL_ID = "as_billiards_capture"  
    private const val NOTIFICATION_ID = 1001  
}  

private var projection: MediaProjection? = null  
private var virtualDisplay: VirtualDisplay? = null  
private var imageReader: ImageReader? = null  

private val handler = Handler(Looper.getMainLooper())  

private var frameCount = 0  
private var lastNotificationUpdate = 0L  
private var lastAnalysisTime = 0L  
private var detectedCircles = 0  

private var openCvReady = false  

private val projectionCallback =  
    object : MediaProjection.Callback() {  
        override fun onStop() {  
            stopSelf()  
        }  
    }  

override fun onCreate() {  
    super.onCreate()  
    createNotificationChannel()  
    openCvReady = OpenCVLoader.initLocal()  
}  

override fun onStartCommand(  
    intent: Intent?,  
    flags: Int,  
    startId: Int  
): Int {  

    val resultCode = intent?.getIntExtra(  
        EXTRA_RESULT_CODE, 0  
    ) ?: 0  

    val resultData: Intent? =  
        if (Build.VERSION.SDK_INT >= 33) {  
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

    try {  
        if (Build.VERSION.SDK_INT >= 29) {  
            startForeground(  
                NOTIFICATION_ID,  
                createNotification("جاري تجهيز الالتقاط"),  
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION  
            )  
        } else {  
            startForeground(  
                NOTIFICATION_ID,  
                createNotification("جاري تجهيز الالتقاط")  
            )  
        }  

        val manager = getSystemService(  
            Context.MEDIA_PROJECTION_SERVICE  
        ) as MediaProjectionManager  

        projection = manager.getMediaProjection(  
            resultCode, resultData  
        )  

        val activeProjection = projection  

        if (activeProjection == null) {  
            stopSelf()  
            return START_NOT_STICKY  
        }  

        activeProjection.registerCallback(  
            projectionCallback, handler  
        )  

        startCapture()  

    } catch (e: Exception) {  
        updateNotification("حدث خطأ أثناء بدء الالتقاط")  
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
        3  
    )  

    imageReader?.setOnImageAvailableListener(  
        { reader ->  
            val image = try {  
                reader.acquireLatestImage()  
            } catch (e: Exception) {  
                null  
            }  

            if (image != null) {  
                try {  
                    frameCount++  

                    val now = System.currentTimeMillis()  

                    // تحليل إطار واحد كل نصف ثانية  
                    if (openCvReady &&  
                        now - lastAnalysisTime >= 500  
                    ) {  
                        lastAnalysisTime = now  
                        detectedCircles = analyzeImage(image)  
                    }  

                    if (now - lastNotificationUpdate >= 3000) {  
                        lastNotificationUpdate = now  

                        updateNotification(  
                            if (openCvReady) {  
                                "الالتقاط يعمل — أجسام دائرية مبدئياً: $detectedCircles"  
                            } else {  
                                "OpenCV غير جاهز — الإطارات: $frameCount"  
                            }  
                        )  
                    }  

                } catch (e: Exception) {  
                    updateNotification("تعذر تحليل إطار الشاشة")  
                } finally {  
                    image.close()  
                }  
            }  
        },  
        handler  
    )  

    virtualDisplay = projection?.createVirtualDisplay(  
        "ASBilliardsScreen",  
        width,  
        height,  
        density,  
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,  
        imageReader?.surface,  
        null,  
        handler  
    )  

    if (virtualDisplay == null) {  
        updateNotification("تعذر إنشاء شاشة الالتقاط")  
    } else {  
        updateNotification("تم تجهيز الالتقاط وربط OpenCV")  
    }  
}  

private fun analyzeImage(image: Image): Int {  

    val plane = image.planes[0]  
    val buffer = plane.buffer  

    val width = image.width  
    val height = image.height  

    val rowStride = plane.rowStride  
    val pixelStride = plane.pixelStride  

    val pixels = IntArray(width * height)  

    for (y in 0 until height) {  
        for (x in 0 until width) {  

            val index =  
                y * rowStride + x * pixelStride  

            val r = buffer.get(index).toInt() and 0xFF  
            val g = buffer.get(index + 1).toInt() and 0xFF  
            val b = buffer.get(index + 2).toInt() and 0xFF  
            val a = buffer.get(index + 3).toInt() and 0xFF  

            pixels[y * width + x] =  
                Color.argb(a, r, g, b)  
        }  
    }  

    val bitmap = Bitmap.createBitmap(  
        width,  
        height,  
        Bitmap.Config.ARGB_8888  
    )  

    bitmap.setPixels(  
        pixels, 0, width, 0, 0, width, height  
    )  

    val rgba = Mat()  
    val gray = Mat()  
    val small = Mat()  
    val circles = Mat()  

    try {  
        Utils.bitmapToMat(bitmap, rgba)  

        Imgproc.cvtColor(  
            rgba,  
            gray,  
            Imgproc.COLOR_RGBA2GRAY  
        )  

        // تصغير الصورة لتخفيف الحمل  
        Imgproc.resize(  
            gray,  
            small,  
            Size(width / 2.0, height / 2.0)  
        )  

        Imgproc.GaussianBlur(  
            small,  
            small,  
            Size(5.0, 5.0),  
            1.5  
        )  

        // رصد الدوائر مبدئياً  
        Imgproc.HoughCircles(  
            small,  
            circles,  
            Imgproc.HOUGH_GRADIENT,  
            1.0,  
            12.0,  
            100.0,  
            18.0,  
            4,  
            18  
        )  

        return circles.cols()  

    } finally {  
        rgba.release()  
        gray.release()  
        small.release()  
        circles.release()  
        bitmap.recycle()  
    }  
}  

private fun createNotificationChannel() {  
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  
        val channel = NotificationChannel(  
            CHANNEL_ID,  
            "AS BILLIARDS Screen Capture",  
            NotificationManager.IMPORTANCE_LOW  
        )  

        getSystemService(  
            NotificationManager::class.java  
        ).createNotificationChannel(channel)  
    }  
}  

private fun createNotification(  
    message: String  
): Notification {  

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  
        Notification.Builder(this, CHANNEL_ID)  
            .setContentTitle("AS BILLIARDS AI")  
            .setContentText(message)  
            .setSmallIcon(android.R.drawable.ic_menu_view)  
            .setOnlyAlertOnce(true)  
            .build()  
    } else {  
        @Suppress("DEPRECATION")  
        Notification.Builder(this)  
            .setContentTitle("AS BILLIARDS AI")  
            .setContentText(message)  
            .setSmallIcon(android.R.drawable.ic_menu_view)  
            .setOnlyAlertOnce(true)  
            .build()  
    }  
}  

private fun updateNotification(message: String) {  
    val manager = getSystemService(  
        Context.NOTIFICATION_SERVICE  
    ) as NotificationManager  

    manager.notify(  
        NOTIFICATION_ID,  
        createNotification(message)  
    )  
}  

override fun onDestroy() {  
    virtualDisplay?.release()  
    virtualDisplay = null  

    imageReader?.setOnImageAvailableListener(null, null)  
    imageReader?.close()  
    imageReader = null  

    projection?.unregisterCallback(projectionCallback)  
    projection?.stop()  
    projection = null  

    super.onDestroy()  
}  

override fun onBind(intent: Intent?): IBinder? = null

}
