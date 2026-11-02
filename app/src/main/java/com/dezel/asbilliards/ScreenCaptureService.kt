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

    val rgba = Mat()
    val gray = Mat()
    val small = Mat()
    val circles = Mat()

    try {
        bitmap.setPixels(
            pixels, 0, width, 0, 0, width, height
        )

        Utils.bitmapToMat(bitmap, rgba)

        Imgproc.cvtColor(
            rgba,
            gray,
            Imgproc.COLOR_RGBA2GRAY
        )

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
