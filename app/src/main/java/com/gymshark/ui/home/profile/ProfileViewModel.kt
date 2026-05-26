package com.gymshark.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.user.UserRepository
import com.gymshark.domain.models.Series
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val currentUserFlow = userRepository.currentUserIdFlow
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(null) else userRepository.observeById(id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val plannedDaysFlow =
        userRepository.observePlannedDays()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val daySlotsFlow =
        userRepository.observeDaySlots()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveUser(user: UserEntity) = viewModelScope.launch {
        val userId = user.userId.ifBlank { userRepository.currentUserId() ?: "1" }
        userRepository.upsert(user.copy(userId = userId))
        userRepository.setCurrentUserId(userId)
    }

    fun setCurrentUserId() = viewModelScope.launch {
        loadUser()?.userId?.let { userRepository.setCurrentUserId(it) }
    }

    suspend fun loadUser(): UserEntity? {
        return currentUserFlow.value
    }

    fun observeUser(): Flow<UserEntity?> {
        return currentUserFlow
    }

    fun saveTrainingDays(days: Set<DayOfWeek>) = viewModelScope.launch {
        userRepository.savePlannedDays(days)
    }

    fun updateWeight(weight: Float) = viewModelScope.launch {
        val user = loadUser() ?: return@launch
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
