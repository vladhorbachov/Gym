package com.gymshark.ui.home.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.UserRepository
import kotlinx.coroutines.flow.*
import normalizeToCalendarListDistinct

class TrainingViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val trainingDaysFlow: Flow<Set<Int>> =
        userRepository.currentUserIdFlow
            .filterNotNull()
            .flatMapLatest { id -> userRepository.observeById(id) }
            .map { user ->
                (user?.trainingDay?.days ?: emptyList())
                    .normalizeToCalendarListDistinct()
                    .toSet()
            }
            .distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}


