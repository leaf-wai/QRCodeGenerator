package com.leaf.qrcodegenerator.data

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.leaf.qrcodegenerator.utils.createQrCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface QrCodeImageStore {
    suspend fun save(content: String): Boolean
}

class MediaStoreQrCodeImageStore(
    private val contentResolver: ContentResolver,
) : QrCodeImageStore {
    override suspend fun save(content: String): Boolean = withContext(Dispatchers.IO) {
        // 保存到相册时使用白底黑码并保留白边，避免深色相册背景下无法识别
        val bitmap = createQrCode(
            content = content,
            size = 800,
            foregroundColor = Color.BLACK,
            backgroundColor = Color.WHITE,
            margin = 2,
        ) ?: return@withContext false

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/QRCodeGenerator",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        var saveUri: Uri? = null
        try {
            saveUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext false
            val saved = contentResolver.openOutputStream(saveUri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: false
            if (!saved) {
                contentResolver.delete(saveUri, null, null)
                return@withContext false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.update(
                    saveUri,
                    ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                    null,
                    null,
                )
            }
            true
        } catch (_: Exception) {
            saveUri?.let { contentResolver.delete(it, null, null) }
            false
        } finally {
            bitmap.recycle()
        }
    }
}
