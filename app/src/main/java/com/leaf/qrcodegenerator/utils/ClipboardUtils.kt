package com.leaf.qrcodegenerator.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardUtils {
    fun copyToClipboard(context: Context, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Label", content))
        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    fun getClipboardContent(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData == null || clipData.itemCount <= 0) {
            return ""
        }
        val item = clipData.getItemAt(0)
        return if (item == null || item.text == null) {
            ""
        } else item.text.toString()
    }
}