package com.gymshark.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymshark.data.models.Pentagon
import com.gymshark.data.models.Series
import com.gymshark.data.models.TrainingDay

@Entity(tableName = "User")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    var userId: String,
    var name: String,
    var age: Int,
    var sex: Boolean,
    var weight: Float,
    var height: Int,
    var minBPM: Int,
    var maxBPM: Int,
    var avgBPM: Int,
    var pentagon: Pentagon,
    var trainingDay: TrainingDay,
    @Embedded(prefix = "series_")
    var series: Series
)