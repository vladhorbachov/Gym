package com.gymshark.data.models

@kotlinx.parcelize.Parcelize
data class Exercise(
    val id: Long,
    val title: String,
    val setsReps: List<Test>,
    val weight: String
) : android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class Test(
    val count: Int,
    val weight: Int
) : android.os.Parcelable


