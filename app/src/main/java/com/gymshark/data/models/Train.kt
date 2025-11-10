package com.gymshark.data.models

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Train(
    val id: Long,
    val date: Long, // timestamp (millis)
    val title: String
){
    val localDate: LocalDate
        get() = Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
}