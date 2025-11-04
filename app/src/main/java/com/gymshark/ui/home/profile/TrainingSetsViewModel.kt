package com.gymshark.ui.home.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import normalizeToCalendarListDistinct

class TrainingSetsViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_DISPLAYED = "displayed_days"
        private const val KEY_CURSOR = "cursor_index"
    }

    val baseDays: StateFlow<List<Int>> = userRepository.currentUserIdFlow
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else userRepository.observeById(id).map { it?.trainingDay?.days ?: emptyList() }
        }
        .map { it.normalizeToCalendarListDistinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val displayedDays: StateFlow<List<Int>> =
        savedStateHandle.getStateFlow(KEY_DISPLAYED, emptyList())

    private var cursor: Int
        get() = savedStateHandle[KEY_CURSOR] ?: 0
        set(value) {
            savedStateHandle[KEY_CURSOR] = value
        }

    fun addNext() {
        val src = baseDays.value
        if (src.isEmpty()) return
        val idx = cursor % src.size
        val next = src[idx]
        cursor = (idx + 1) % src.size
        savedStateHandle[KEY_DISPLAYED] = displayedDays.value + next
    }

    fun syncWithBase() {
        val allowed = baseDays.value.toSet()
        val filtered = displayedDays.value.filter { it in allowed }
        if (filtered != displayedDays.value) {
            savedStateHandle[KEY_DISPLAYED] = filtered
        }
        if (baseDays.value.isNotEmpty()) {
            cursor %= baseDays.value.size
        } else {
            cursor = 0
        }
    }

}
