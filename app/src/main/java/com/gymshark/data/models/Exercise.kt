package com.gymshark.data.models

@kotlinx.parcelize.Parcelize
data class Exercise(
    val id: Long,
    val title: String,
    val setsReps: String,
    val weight: String
) : android.os.Parcelable


