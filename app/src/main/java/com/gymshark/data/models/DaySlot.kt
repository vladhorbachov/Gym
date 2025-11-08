package com.gymshark.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DaySlot(
    val id: Long,
    val day: Int,
    val types: List<String> = emptyList()
) : Parcelable

