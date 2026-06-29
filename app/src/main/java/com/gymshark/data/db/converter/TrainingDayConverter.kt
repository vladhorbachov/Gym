package com.gymshark.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.gymshark.domain.models.TrainingDay

class TrainingDayConverter {
    private val gson = Gson()

    @TypeConverter
    fun toJson(value: TrainingDay?): String = gson.toJson(value ?: TrainingDay(mutableListOf()))

    @TypeConverter
    fun fromJson(json: String?): TrainingDay =
        if (json.isNullOrBlank()) TrainingDay(mutableListOf())
        else runCatching { gson.fromJson(json, TrainingDay::class.java) }.getOrElse {
            TrainingDay(
                mutableListOf()
            )
        }
}
