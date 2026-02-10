package com.gymshark.domain.models

import com.gymshark.data.db.entity.TrainingsEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

data class Train(
    val id: Long,
    val date: Long, // timestamp (millis)
    val title: String,
    val duration: Long
) {
    val localDate: LocalDate
        get() = Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    val durationMin: Long
        get() = duration / 60L
}

fun Train.toTrainingsEntity(
    exerciseId: Int = 0,
    setsCount: Int = 0,
    startTime: Long,
    finishTime: Long,
    activeDuration: Long = duration,
    minBPM: Int = 70,
    maxBPM: Int = 140,
    avgBPM: Int = 105,
    calories: Int = estimateCalories(duration),
    mood: String = "neutral"
): TrainingsEntity {
    return TrainingsEntity(
        name = title,
        exerciseId = exerciseId,
        time = date,
        setsCount = setsCount,
        startTime = startTime,
        finishTime = finishTime,
        fullDuration = (finishTime - startTime),
        activeDuration = activeDuration * 1000L,
        minBPM = minBPM,
        maxBPM = maxBPM,
        avgBPM = avgBPM,
        calories = calories,
        mood = mood
    )
}

private fun estimateCalories(durationSec: Long): Int {
    val minutes = durationSec / 60.0
    return (minutes * 6.0).roundToInt()
}