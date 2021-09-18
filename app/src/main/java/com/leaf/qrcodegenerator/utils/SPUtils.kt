package com.leaf.qrcodegenerator.utils

import com.tencent.mmkv.MMKV

object SPUtils {
    private var kv: MMKV
    private const val HISTORY_KEY = "history"

    init {
        kv = MMKV.mmkvWithID(HISTORY_KEY, MMKV.MULTI_PROCESS_MODE)
    }

    //保存历史记录
    fun saveHistory(content: String) {
        if (content.isEmpty()) return
        val historyData = kv.decodeString(HISTORY_KEY)
        if (!historyData.isNullOrEmpty()) {
            val historyList = GsonUtils.fromJsonList<String>(historyData) as ArrayList
            for (str in historyList) {
                if (str == content) {
                    historyList.remove(str)
                    break
                }
            }
            historyList.add(0, content)
            kv.encode(HISTORY_KEY, historyList.toJson())
        } else {
            clearHistory()
        }
    }

    // 获取历史记录
    fun getHistory(): List<String> {
        val longHistory = kv.decodeString(HISTORY_KEY)
        return if (!longHistory.isNullOrEmpty()) {
            val historyList = GsonUtils.fromJsonList<String>(longHistory) as ArrayList
            historyList
        } else {
            emptyList()
        }
    }

    // 清空历史记录
    fun clearHistory() {
        kv.encode(HISTORY_KEY, emptyList<String>().toJson())
    }
}