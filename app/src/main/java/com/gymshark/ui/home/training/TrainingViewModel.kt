package com.gymshark.ui.home.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.UserRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

class TrainingViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private fun toCalendarSet(indices: List<Int>): Set<Int> = indices.map {
        when (it) {
            0 -> Calendar.MONDAY
            1 -> Calendar.TUESDAY
            2 -> Calendar.WEDNESDAY
            3 -> Calendar.THURSDAY
            4 -> Calendar.FRIDAY
            5 -> Calendar.SATURDAY
            6 -> Calendar.SUNDAY
            else -> error("Invalid day index: $it")
        }
    }.toSet()

    val trainingDaysFlow: Flow<Set<Int>> =
        userRepository.currentUserIdFlow
            .filterNotNull()
            .flatMapLatest { id -> userRepository.observeById(id) }
            .map { user -> toCalendarSet(user?.trainingDay?.days ?: emptyList()) }
            .distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}
