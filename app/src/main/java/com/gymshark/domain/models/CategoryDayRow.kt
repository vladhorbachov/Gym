package com.gymshark.domain.models

data class CategoryDayRow(
    val day: String,
    val category: String,
    val maxWeight: Float,
    val totalSets: Int
)
