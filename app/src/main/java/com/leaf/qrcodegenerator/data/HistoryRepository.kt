package com.leaf.qrcodegenerator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.IOException

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "history",
)

interface HistoryRepository {
    val history: Flow<List<String>>

    suspend fun save(content: String)

    suspend fun delete(contents: Set<String>)
}

class DataStoreHistoryRepository(
    private val dataStore: DataStore<Preferences>,
) : HistoryRepository {
    constructor(context: Context) : this(context.applicationContext.historyDataStore)

    private val historyKey = stringPreferencesKey("history")

    override val history: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            decodeHistory(preferences[historyKey].orEmpty())
        }

    override suspend fun save(content: String) {
        if (content.isBlank()) return
        dataStore.edit { preferences ->
            val updated = decodeHistory(preferences[historyKey].orEmpty()).toMutableList()
            updated.remove(content)
            updated.add(0, content)
            preferences[historyKey] = encodeHistory(updated)
        }
    }

    override suspend fun delete(contents: Set<String>) {
        if (contents.isEmpty()) return
        dataStore.edit { preferences ->
            val updated = decodeHistory(preferences[historyKey].orEmpty())
                .filterNot(contents::contains)
            preferences[historyKey] = encodeHistory(updated)
        }
    }

    private fun decodeHistory(raw: String): List<String> {
        if (raw.isEmpty()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.getString(index) }
        }.getOrDefault(emptyList())
    }

    private fun encodeHistory(history: List<String>): String {
        val array = JSONArray()
        for (item in history) {
            array.put(item)
        }
        return array.toString()
    }
}
