package com.gymshark.data.models

import java.time.LocalDate

data class TrainingCalendarDay(
    val date: LocalDate,
    val train: Train?
)