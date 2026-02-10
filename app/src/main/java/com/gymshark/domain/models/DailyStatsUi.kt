package com.gymshark.domain.models

data class DailyStatsUi(
    val day: String,
    val totalMinutes: Long,
    val avgBpm: Int?,
    val calories: Int
)