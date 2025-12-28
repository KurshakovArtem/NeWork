package ru.netology.nework.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.UserPreview

class Converter {

    val gson = Gson()

    // Конвертируем List<Long> в JSON-строку
    @TypeConverter
    fun fromLongList(list: List<Long>?): String? {
        return gson.toJson(list)
    }

    // Конвертируем JSON-строку в List<Long>
    @TypeConverter
    fun toLongList(json: String?): List<Long>? {
        if (json == null) return null
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(json, type)
    }

    // Конвертируем List<UserPreview> в JSON-строку
    @TypeConverter
    fun fromUserPreviewList(list: List<UserPreview>?): String? {
        return gson.toJson(list)
    }

    // Конвертируем JSON-строку в List<UserPreview>
    @TypeConverter
    fun toUserPreviewList(json: String?): List<UserPreview>? {
        if (json == null) return null
        val type = object : TypeToken<List<UserPreview>>() {}.type
        return gson.fromJson(json, type)
    }


}