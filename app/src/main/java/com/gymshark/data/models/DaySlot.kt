package com.gymshark.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import kotlin.collections.addAll

@Parcelize
data class DaySlot(
    val id: Long,
    val day: Int,
    val types: List<String> = emptyList()
) : Parcelable

fun List<DaySlot>.getListDays(): List<Int> {
    val l = mutableListOf<Int>()
    this.forEach {
        l.add(it.day)
    }
    return l
}
fun List<DaySlot>.getDefaultListDays(): List<Int> {
    val l = mutableListOf<Int>()
    l.addAll(
        listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
    )
    return l
}

fun List<Int>.toDaysSlot(): List<DaySlot> {
    val l = mutableListOf<DaySlot>()
    this.forEach {
        l.add(DaySlot(it.toLong(), it))
    }
    return l
}
