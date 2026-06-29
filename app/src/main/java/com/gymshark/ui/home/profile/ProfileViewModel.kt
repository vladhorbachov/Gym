package com.gymshark.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.user.UserRepository
import com.gymshark.domain.models.DaySlot
import com.gymshark.domain.models.Pentagon
import com.gymshark.domain.models.Series
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

data class ProfileState(
    val user: UserEntity? = null,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val trainingSlots: List<DaySlot> = emptyList()
)

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

    val profileState =
        combine(currentUserFlow, plannedDaysFlow, daySlotsFlow) { user, days, slots ->
            ProfileState(
                user = user,
                selectedDays = days,
                trainingSlots = slots
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileState())

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

    fun saveTrainingPlan(slots: List<DaySlot>) = viewModelScope.launch {
        val userId = userRepository.currentUserId() ?: "1"
        userRepository.setTrainingSlots(
            userId,
            slots
                .filter { it.day in plannedDaysFlow.value }
                .sortedBy { it.day.value }
                .map { it.copy(id = it.day.value.toLong()) }
        )
    }

    fun savePersonalDetails(
        name: String,
        age: Int,
        sex: Boolean,
        height: Int
    ) = viewModelScope.launch {
        val baseUser = loadUser() ?: emptyUser()
        val userId = baseUser.userId.ifBlank { userRepository.currentUserId() ?: "1" }
        userRepository.upsert(
            baseUser.copy(
                userId = userId,
                name = name,
                age = age,
                sex = sex,
                height = height
            )
        )
        userRepository.setCurrentUserId(userId)
    }

    fun updateWeight(weight: Float) = viewModelScope.launch {
        val user = loadUser() ?: return@launch
        userRepository.upsert(user.copy(weight = weight))
    }

    fun saveWeight(weight: Float) = viewModelScope.launch {
        val baseUser = loadUser() ?: emptyUser()
        val userId = baseUser.userId.ifBlank { userRepository.currentUserId() ?: "1" }
        userRepository.upsert(baseUser.copy(userId = userId, weight = weight))
        userRepository.setCurrentUserId(userId)
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

    private fun emptyUser(): UserEntity =
        UserEntity(
            userId = "",
            name = "",
            age = 0,
            sex = true,
            weight = 0f,
            height = 0,
            minBPM = 0,
            maxBPM = 0,
            avgBPM = 0,
            pentagon = Pentagon(0, 0, 0, 0, 0),
            series = Series(0, 0, 0L),
            trainingDayTypes = emptyMap(),
            trainingSlots = emptyList()
        )
}
