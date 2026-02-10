package com.gymshark.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.gymshark.domain.models.Pentagon

class PentagonConverter {
    private companion object {
        val gson = Gson()
        val default = Pentagon()
    }

    @TypeConverter
    fun fromPentagon(pentagon: Pentagon?): String =
        gson.toJson(pentagon ?: default)

    @TypeConverter
    fun toPentagon(json: String?): Pentagon =
        try {
            if (json.isNullOrBlank()) default else gson.fromJson(json, Pentagon::class.java) ?: default
        } catch (_: Exception) {
            default
        }
}