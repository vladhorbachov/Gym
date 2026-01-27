package com.gymshark.data.models

data class DailyStatsRow(
    val day: String,
    val totalDuration: Long,
    val avgBpm: Float?,
    val totalCalories: Int
)