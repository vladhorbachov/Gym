package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Trainings")
data class TrainingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val exerciseId: Int,
    val time: Long,
    val setsCount: Int,
    val startTime: Long,
    val finishTime: Long,
    val fullDuration: Long,
    val activeDuration: Long,
    val minBPM: Int?,
    val maxBPM: Int?,
    val avgBPM: Int?,
    val calories: Int,
    val mood: String
)
