package com.gymshark.domain.models

import java.time.LocalDate

data class TrainingCalendarDay(
    val date: LocalDate,
    val train: Train?
)