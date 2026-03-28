package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pulse")
data class PulseEntity(
    @PrimaryKey(autoGenerate = true)
    val idKey: Long = 0,
    val activityType: Int,
    val date: Long,
    val value: Int

)