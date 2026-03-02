package com.gymshark.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.user.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val plannedDaysFlow =
        userRepository.observePlannedDays()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val daySlotsFlow =
        userRepository.observeDaySlots()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveUser(user: UserEntity) = viewModelScope.launch {
        userRepository.upsert(user)
        userRepository.setCurrentUserId()
    }

    fun setCurrentUserId() = viewModelScope.launch {
        userRepository.setCurrentUserId()
    }

    suspend fun loadUser() =
        userRepository.getById()

    fun saveTrainingDays(days: Set<DayOfWeek>) = viewModelScope.launch {
        userRepository.savePlannedDays(days)
    }

    fun updateWeight(weight: Float) = viewModelScope.launch {
        val id = userRepository.currentUserId() ?: return@launch
        val user = userRepository.getById(id) ?: return@launch
        userRepository.upsert(user.copy(weight = weight))
    }
}
