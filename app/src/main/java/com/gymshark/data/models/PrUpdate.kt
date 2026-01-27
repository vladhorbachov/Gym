package com.gymshark.data.models

data class PrUpdate(
    val exerciseId: Int,
    val exerciseName: String,
    val addedSets: Int,
    val sessionMaxWeight: Float?
)