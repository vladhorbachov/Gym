package com.gymshark.ui.home.profile.modal

import java.time.DayOfWeek

data class TrainingPlanDraft(
    val days: List<DayOfWeek> = emptyList(),
    val cycle: List<String> = emptyList()
)
