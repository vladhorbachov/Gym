package com.gymshark.data.models

@kotlinx.parcelize.Parcelize
data class Exercise(
    val id: Long,
    val title: String
) : android.os.Parcelable
