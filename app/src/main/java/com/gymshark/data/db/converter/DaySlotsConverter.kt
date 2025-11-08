package com.gymshark.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymshark.data.models.DaySlot

class DaySlotsConverter {
    private val gson = Gson()
    private val listType = object : TypeToken<List<DaySlot>>() {}.type

    @TypeConverter
    fun toJson(value: List<DaySlot>?): String =
        gson.toJson(value ?: emptyList<DaySlot>())

    @TypeConverter
    fun fromJson(json: String?): List<DaySlot> {
        if (json.isNullOrBlank()) return emptyList()
        val parsed: List<DaySlot>? = gson.fromJson(json, listType)
        return parsed ?: emptyList()
    }
}
