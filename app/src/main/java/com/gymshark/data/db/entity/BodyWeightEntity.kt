package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight")
data class BodyWeightEntity(

    @PrimaryKey
    val day: Long,
    val weight: Float,
    val updatedAt: Long
)