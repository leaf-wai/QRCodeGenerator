package com.leaf.qrcodegenerator.utils

import com.tencent.mmkv.MMKV
import org.json.JSONArray

object SPUtils {
    private const val HISTORY_KEY = "history"
    private val kv: MMKV = MMKV.mmkvWithID(HISTORY_KEY, MMKV.MULTI_PROCESS_MODE)

    fun saveHistory(content: String) {
        if (content.isEmpty()) return
        val history = readHistory()
        history.remove(content)
        history.add(0, content)
        writeHistory(history)
    }

    fun getHistory(): List<String> = readHistory()

    fun clearHistory() = writeHistory(emptyList())

    private fun readHistory(): MutableList<String> {
        val raw = kv.decodeString(HISTORY_KEY).orEmpty()
        if (raw.isEmpty()) return mutableListOf()

        return runCatching {
            val array = JSONArray(raw)
            MutableList(array.length()) { index -> array.getString(index) }
        }.getOrDefault(mutableListOf())
    }

    private fun writeHistory(history: List<String>) {
        val array = JSONArray()
        for (item in history) {
            array.put(item)
        }
        kv.encode(HISTORY_KEY, array.toString())
    }
}
