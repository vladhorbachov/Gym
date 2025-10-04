package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Sets")
data class SetsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val trainingId: Int,
    val weight: Float,
    val count: Int
)
