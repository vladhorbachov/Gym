package com.gymshark.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.user.UserRepository
import com.gymshark.domain.models.Series
import kotlinx.coroutines.flow.Flow
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

    suspend fun loadUser(): UserEntity? {
        return userRepository.getById()
    }

    fun observeUser(): Flow<UserEntity?> {
        return userRepository.observeById("1")
    }

    fun saveTrainingDays(days: Set<DayOfWeek>) = viewModelScope.launch {
        userRepository.savePlannedDays(days)
    }

    fun updateWeight(weight: Float) = viewModelScope.launch {
        val user = userRepository.getById("1") ?: return@launch
        userRepository.upsert(user.copy(weight = weight))
    }

    fun registerActivity(currentTime: Long) {
        viewModelScope.launch {
            userRepository.registerActivity(currentTime)
        }
    }
    fun refreshSeriesState() {
        viewModelScope.launch {
            userRepository.refreshSeriesState(System.currentTimeMillis())
        }
    }
    fun debugSetSeries(series: Series) {
        viewModelScope.launch {
            userRepository.debugSetSeries(series)
        }
    }
}