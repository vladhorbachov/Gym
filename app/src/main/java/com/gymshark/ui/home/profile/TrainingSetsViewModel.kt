package com.gymshark.ui.home.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.UserRepository
import com.gymshark.data.exercises.ExercisesCatalog
import com.gymshark.data.models.DaySlot
import com.gymshark.data.models.getDefaultListDays
import com.gymshark.data.models.getListDays
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class TrainingSetsViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
    private val catalog: ExercisesCatalog
) : ViewModel() {

    companion object {
        private const val KEY_SLOTS = "displayed_slots"
        private const val KEY_CURSOR = "cursor_index"
        private const val KEY_DAY_DEFAULTS = "day_defaults"
    }

    private var nextId = 1L

    val categories: StateFlow<List<String>> = flow {
        emit(catalog.loadCategories())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val baseDays: StateFlow<List<DayOfWeek>> =
        userRepository.currentUserIdFlow
            .flatMapLatest { id -> if (id == null) flowOf(null) else userRepository.observeById(id) }
            .map { entity ->
                val list = entity?.trainingSlots?.getListDays().orEmpty()
                val weekOrder = weekOrder()
                list.distinct().sortedBy { d -> weekOrder.indexOf(d) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val displayedSlots: StateFlow<List<DaySlot>> =
        savedStateHandle.getStateFlow(KEY_SLOTS, emptyList())

    val dayDefaults: StateFlow<Map<DayOfWeek, Set<String>>> =
        savedStateHandle.getStateFlow(KEY_DAY_DEFAULTS, emptyMap())

    private var cursor: Int
        get() = savedStateHandle[KEY_CURSOR] ?: 0
        set(value) { savedStateHandle[KEY_CURSOR] = value }

    init {
        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            val user = userRepository.getById(uid) ?: return@launch
            if (user.trainingSlots.getListDays().isEmpty()) {
                val defaults = getDefaultListDays()
                userRepository.setTrainingSlots(
                    uid,
                    defaults.map { d -> DaySlot(id = d.value.toLong(), day = d, types = emptyList()) }
                )
            }
        }

        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            val fromDb = userRepository.getTrainingSlots(uid)
            if (fromDb.isNotEmpty() && displayedSlots.value.isEmpty()) {
                savedStateHandle[KEY_SLOTS] = fromDb
            }
            nextId = (displayedSlots.value.maxOfOrNull { it.id } ?: 0L) + 1L
            restoreCursor()
        }

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(baseDays, displayedSlots) { _, _ -> }
                .collectLatest { restoreCursor() }
        }

        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            val entity = userRepository.getById(uid) ?: return@launch
            val raw = userRepository.getTrainingDayTypes(entity)
            if (raw.isNotEmpty() && dayDefaults.value.isEmpty()) {
                val mapped: Map<DayOfWeek, Set<String>> = raw.mapKeys { (k, _) ->
                    when (k) {
                        java.util.Calendar.MONDAY -> DayOfWeek.MONDAY
                        java.util.Calendar.TUESDAY -> DayOfWeek.TUESDAY
                        java.util.Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
                        java.util.Calendar.THURSDAY -> DayOfWeek.THURSDAY
                        java.util.Calendar.FRIDAY -> DayOfWeek.FRIDAY
                        java.util.Calendar.SATURDAY -> DayOfWeek.SATURDAY
                        java.util.Calendar.SUNDAY -> DayOfWeek.SUNDAY
                        else -> DayOfWeek.MONDAY
                    }
                }
                savedStateHandle[KEY_DAY_DEFAULTS] = mapped
            }
        }
    }

    fun addNext() {
        val order = weekOrder()
        val src = baseDays.value
            .distinct()
            .sortedBy { d -> order.indexOf(d) }
            .ifEmpty { order }

        val idx = cursor % src.size
        val day = src[idx]
        cursor = (idx + 1) % src.size

        val prefill = dayDefaults.value[day]?.toList().orEmpty()
        val newSlot = DaySlot(id = nextId++, day = day, types = prefill)

        val updated = displayedSlots.value + newSlot
        savedStateHandle[KEY_SLOTS] = updated
        persistSlots(updated)
    }

    fun setSlotTypes(slotId: Long, types: List<String>) {
        val valid = categories.value.toSet()
        val normalized = types.filter { it in valid }
        val updated = displayedSlots.value.map { s ->
            if (s.id == slotId) s.copy(types = normalized) else s
        }
        savedStateHandle[KEY_SLOTS] = updated
        persistSlots(updated)
    }

    fun removeSlot(slotId: Long) {
        val updated = displayedSlots.value.filterNot { it.id == slotId }
        savedStateHandle[KEY_SLOTS] = updated
        persistSlots(updated)
    }

    private fun persistSlots(slots: List<DaySlot>) {
        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            userRepository.setTrainingSlots(uid, slots)
        }
    }

    fun setDayDefaults(day: DayOfWeek, types: List<String>) {
        val valid = categories.value.toSet()
        val normalized = types.filter { it in valid }.toSet()
        val newMap = dayDefaults.value.toMutableMap().apply { put(day, normalized) }
        savedStateHandle[KEY_DAY_DEFAULTS] = newMap
        persistDayDefaults()
    }

    fun defaultsFor(day: DayOfWeek): List<String> =
        dayDefaults.value[day]?.toList().orEmpty()

    fun syncWithBase() {
        val allowed = baseDays.value.toSet()
        if (allowed.isEmpty()) return
        val filtered = displayedSlots.value.filter { it.day in allowed }
        if (filtered.size != displayedSlots.value.size) {
            savedStateHandle[KEY_SLOTS] = filtered
            persistSlots(filtered)
        }
        cursor = if (baseDays.value.isNotEmpty()) cursor % baseDays.value.size else 0
    }

    private fun persistDayDefaults() {
        viewModelScope.launch {
            val userId = userRepository.currentUserId() ?: return@launch
            val legacy: Map<Int, Set<String>> = dayDefaults.value.mapKeys { (k, _) ->
                when (k) {
                    DayOfWeek.MONDAY -> java.util.Calendar.MONDAY
                    DayOfWeek.TUESDAY -> java.util.Calendar.TUESDAY
                    DayOfWeek.WEDNESDAY -> java.util.Calendar.WEDNESDAY
                    DayOfWeek.THURSDAY -> java.util.Calendar.THURSDAY
                    DayOfWeek.FRIDAY -> java.util.Calendar.FRIDAY
                    DayOfWeek.SATURDAY -> java.util.Calendar.SATURDAY
                    DayOfWeek.SUNDAY -> java.util.Calendar.SUNDAY
                }
            }
            userRepository.setTrainingDayTypes(userId, legacy)
        }
    }

    private fun weekOrder(): List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

    private fun restoreCursor() {
        val order = weekOrder()
        val src = baseDays.value
            .distinct()
            .sortedBy { d -> order.indexOf(d) }
            .ifEmpty { order }

        val lastDay = displayedSlots.value.lastOrNull()?.day
        cursor = if (lastDay != null) {
            val idx = src.indexOf(lastDay)
            if (idx == -1) 0 else (idx + 1) % src.size
        } else 0
    }
}
