package com.leaf.qrcodegenerator.data

import com.leaf.qrcodegenerator.utils.SPUtils

interface HistoryRepository {
    fun getHistory(): List<String>

    fun save(content: String)

    fun delete(contents: Set<String>)
}

object MmkvHistoryRepository : HistoryRepository {
    override fun getHistory(): List<String> = SPUtils.getHistory()

    override fun save(content: String) = SPUtils.saveHistory(content)

    override fun delete(contents: Set<String>) = SPUtils.deleteHistory(contents)
}
