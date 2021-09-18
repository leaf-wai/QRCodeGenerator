package com.leaf.qrcodegenerator.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

object GsonUtils {
    @JvmStatic
    val gson: Gson = Gson()
    private val gsonThatIgnoreNull: Gson = GsonBuilder().serializeNulls().create()

    @JvmStatic
    fun <T> fromJson(json: String, t: Class<T>): T {
        return gson.fromJson(json, t)
    }

    @JvmStatic
    fun <T> fromJsonList(json: JSONArray): List<T> {
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            return gson.fromJson(json.toString(), type)
        } catch (e: Exception) {
            listOf()
        }
    }

    @JvmStatic
    fun <T> fromJsonList(json: String): List<T> {
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            listOf()
        }
    }

    @JvmStatic
    fun toJson(any: Any?): String {
        return gson.toJson(any)
    }

    @JvmStatic
    fun toJsonIgnoreNull(any: Any?): String {
        return gsonThatIgnoreNull.toJson(any)
    }
}

inline fun <reified T : Any> String.toObject(): T? {
    return try {
        GsonUtils.gson.fromJson(this, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

inline fun <reified T : Any> T.toJson(): String {
    return GsonUtils.toJson(this)
}
