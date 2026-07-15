package com.leaf.qrcodegenerator.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

fun createQrCode(
    content: String,
    size: Int,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.TRANSPARENT,
): Bitmap {
    val matrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size,
        mapOf(
            EncodeHintType.CHARACTER_SET to Charsets.UTF_8.name(),
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
        ),
    )
    val pixels = IntArray(size * size) { index ->
        if (matrix[index % size, index / size]) foregroundColor else backgroundColor
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}
