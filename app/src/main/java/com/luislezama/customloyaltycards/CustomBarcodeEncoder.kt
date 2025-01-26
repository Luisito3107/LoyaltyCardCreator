package com.luislezama.customloyaltycards

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder


class CustomBarcodeEncoder: BarcodeEncoder() {
    private var bgColor: Int = Color.parseColor("#ffffff")
    private var fgColor: Int = Color.parseColor("#000000")

    fun setBackgroundColor(bgColor: Int) {
        this.bgColor = bgColor
    }

    fun setForegroundColor(fgColor: Int) {
        this.fgColor = fgColor
    }

    override fun createBitmap(matrix: BitMatrix): Bitmap? {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = (if (matrix[x, y]) fgColor else bgColor).toInt()
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}