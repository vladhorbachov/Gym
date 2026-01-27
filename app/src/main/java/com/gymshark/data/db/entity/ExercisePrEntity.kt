package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ExercisePR")
data class ExercisePrEntity(
    @PrimaryKey val exerciseId: Int,
    val exerciseName: String,
    val maxWeight: Float,
    val trainingIdWherePR: Int,
    val totalSetsLifetime: Int,
    val updatedAt: Long
)