package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TrainingSets",
    indices = [Index(value = ["trainingExerciseId"])]
)
data class TrainingSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val trainingExerciseId: Int,
    val reps: Int?,
    val weight: Float?
)
