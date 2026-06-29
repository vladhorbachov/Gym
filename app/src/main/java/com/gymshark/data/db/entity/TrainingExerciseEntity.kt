package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TrainingExercises",
    indices = [
        Index(value = ["trainingId"]),
        Index(value = ["exerciseId"])
    ]
)
data class TrainingExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val trainingId: Int,
    val exerciseId: Int,
    val orderIndex: Int
)
