package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymshark.data.models.Exercise

@Entity(tableName = "Exercises")
data class ExercisesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val baseCategory: String,
    val subCategory: String,
    val difficulty: Int
)
