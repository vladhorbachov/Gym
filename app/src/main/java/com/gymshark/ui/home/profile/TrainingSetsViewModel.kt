package com.gymshark.ui.home.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.exercises.ExercisesCatalog
import com.gymshark.data.user.UserRepository
import com.gymshark.domain.models.DaySlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class TrainingSetsViewModel(
    private val userRepository: UserRepository,
    @Suppress("UNUSED_PARAMETER") private val savedStateHandle: SavedStateHandle,
    private val catalog: ExercisesCatalog
) : ViewModel() {

    private val weekOrder = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    private val fallbackCategories = listOf(
        "Chest",
        "Back",
        "Legs",
        "Shoulders",
        "Arms",
        "Core",
        "Cardio",
        "Push",
        "Pull",
        "Full Body",
        "Rest",
        "Custom / Other"
    )

    val categories: StateFlow<List<String>> = flow {
        emit(catalog.loadCategories().ifEmpty { fallbackCategories })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, fallbackCategories)

    val baseDays: StateFlow<List<DayOfWeek>> =
        userRepository.observePlannedDays()
            .map { days -> weekOrder.filter { it in days } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _displayedSlots = MutableStateFlow<List<DaySlot>>(emptyList())
    val displayedSlots: StateFlow<List<DaySlot>> = _displayedSlots

    fun resetDraft() {
        viewModelScope.launch {
            val userId = userRepository.currentUserId() ?: return@launch
            val savedSlots = userRepository.getTrainingSlots(userId)
            val selectedDays = baseDays.value.ifEmpty {
                weekOrder.filter { day -> savedSlots.any { it.day == day } }
            }
            _displayedSlots.value = normalizeSlots(savedSlots, selectedDays)
        }
    }

    fun setSlotType(slotId: Long, type: String) {
        val validTypes = categories.value.toSet()
        if (type !in validTypes) return

        _displayedSlots.value = displayedSlots.value.map { slot ->
            if (slot.id == slotId) {
                val updatedTypes = (slot.types + type)
                    .filter { it in validTypes }
                    .distinct()
                slot.copy(types = updatedTypes)
            } else {
                slot
            }
        }
    }

    fun setSlotTypes(slotId: Long, types: List<String>) {
        val validTypes = categories.value.toSet()
        val normalized = types.filter { it in validTypes }.distinct()
        _displayedSlots.value = displayedSlots.value.map { slot ->
            if (slot.id == slotId) slot.copy(types = normalized) else slot
        }
    }

    fun clearSlot(slotId: Long) {
        _displayedSlots.value = displayedSlots.value.map { slot ->
            if (slot.id == slotId) slot.copy(types = emptyList()) else slot
        }
    }

    fun removeSlotType(slotId: Long, type: String) {
        _displayedSlots.value = displayedSlots.value.map { slot ->
            if (slot.id == slotId) {
                slot.copy(types = slot.types.filterNot { it == type })
            } else {
                slot
            }
        }
    }

    fun commitChanges() {
        viewModelScope.launch {
            val userId = userRepository.currentUserId() ?: return@launch
            userRepository.setTrainingSlots(
                userId,
                normalizeSlots(displayedSlots.value, baseDays.value)
            )
        }
    }

    fun syncWithBase() {
        val selectedDays = baseDays.value
        if (selectedDays.isEmpty() && displayedSlots.value.isNotEmpty()) return
        _displayedSlots.value = normalizeSlots(displayedSlots.value, selectedDays)
    }

    fun addNext() {
        syncWithBase()
    }

    private fun normalizeSlots(
        slots: List<DaySlot>,
        selectedDays: List<DayOfWeek>
    ): List<DaySlot> {
        val byDay = slots.associateBy { it.day }
        return selectedDays.map { day ->
            byDay[day]?.copy(id = day.value.toLong(), day = day)
                ?: DaySlot(id = day.value.toLong(), day = day, types = emptyList())
        }
    }
}
