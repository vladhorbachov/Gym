package com.gymshark.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DayTypesConverter {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<Int, Set<String>>>() {}.type

    @TypeConverter
    fun toJson(value: Map<Int, Set<String>>?): String =
        gson.toJson(value ?: emptyMap<Int, Set<String>>())

    @TypeConverter
    fun fromJson(json: String?): Map<Int, Set<String>> {
        if (json.isNullOrBlank()) return emptyMap()
        val parsed: Map<Int, Set<String>>? = gson.fromJson(json, mapType)
        return parsed ?: emptyMap()
    }
}
