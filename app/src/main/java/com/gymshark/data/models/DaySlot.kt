package com.gymshark.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.util.Calendar

@Parcelize
data class DaySlot(
    val id: Long,
    val day: DayOfWeek,
    val types: List<String> = emptyList()
) : Parcelable

fun List<DaySlot>.getListDays(): List<DayOfWeek> =
    this.map { it.day }


fun getDefaultListDays(): List<DayOfWeek> =
    listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.FRIDAY
    )

fun List<DayOfWeek>.toDaysSlot(): List<DaySlot> =
    this.map { d -> DaySlot(id = d.value.toLong(), day = d) }