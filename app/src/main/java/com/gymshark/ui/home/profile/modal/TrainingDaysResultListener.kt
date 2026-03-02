package com.gymshark.ui.home.profile.modal

import java.time.DayOfWeek

interface TrainingDaysResultListener {
    fun onTrainingDaysSelected(days: Set<DayOfWeek>)
}
