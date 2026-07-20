package com.leaf.qrcodegenerator.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtils {
    fun copyToClipboard(context: Context, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Label", content))
    }

    fun getClipboardContent(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return try {
            val clipData = clipboard.primaryClip
            if (clipData == null || clipData.itemCount == 0) {
                ""
            } else {
                clipData.getItemAt(0).coerceToText(context)?.toString().orEmpty()
            }
        } catch (_: SecurityException) {
            ""
        }
    }
}
