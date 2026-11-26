package com.leaf.qrcodegenerator.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * 将文本编码为二维码位图；内容为空、超长或编码失败时返回 null。
 */
fun createQrCode(
    content: String,
    size: Int,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.TRANSPARENT,
    margin: Int = 1,
): Bitmap? {
    if (content.isEmpty() || size <= 0) return null
    val matrix = try {
        QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(
                EncodeHintType.CHARACTER_SET to Charsets.UTF_8.name(),
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to margin,
            ),
        )
    } catch (_: WriterException) {
        return null
    } catch (_: IllegalArgumentException) {
        return null
    }

    val pixels = IntArray(size * size) { index ->
        if (matrix[index % size, index / size]) foregroundColor else backgroundColor
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}
