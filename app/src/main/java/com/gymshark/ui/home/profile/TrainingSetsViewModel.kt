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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import normalizeToCalendarListDistinct
import java.util.Calendar

class TrainingSetsViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
    private val catalog: ExercisesCatalog
) : ViewModel() {

    companion object {
        private const val KEY_SLOTS = "displayed_slots"           // List<DaySlot>
        private const val KEY_CURSOR = "cursor_index"             // Int
        private const val KEY_DAY_DEFAULTS =
            "day_defaults"       // Map<Int, Set<String>>  (для пре-вибору)
    }

    // Лічильник id для слотів (у поточній сесії)
    private var nextId = 1L

    /** Категорії з JSON (assets/raw) */
    val categories: StateFlow<List<String>> = flow {
        emit(catalog.loadCategories())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Базові дні користувача (з БД профілю) */
    val baseDays: StateFlow<List<Int>> = userRepository.currentUserIdFlow
        .flatMapLatest { id -> if (id == null) flowOf(null) else userRepository.observeById(id) }
        .map { entity -> entity?.trainingSlots?.getListDays() ?: emptyList() }
        .map { it.normalizeToCalendarListDistinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Слоти, які відображаємо у UI */
    val displayedSlots: StateFlow<List<DaySlot>> =
        savedStateHandle.getStateFlow(KEY_SLOTS, emptyList())

    /** Дефолти вибору типів по дню (для пре-філа у нових слотах) */
    @Suppress("UNCHECKED_CAST")
    val dayDefaults: StateFlow<Map<Int, Set<String>>> =
        savedStateHandle.getStateFlow(KEY_DAY_DEFAULTS, emptyMap())

    private var cursor: Int
        get() = savedStateHandle[KEY_CURSOR] ?: 0
        set(value) {
            savedStateHandle[KEY_CURSOR] = value
        }

    init {
        // 1) Якщо у користувача в БД ще порожні базові дні — підкинемо мінімум
        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            val user = userRepository.getById(uid) ?: return@launch
            if (user.trainingSlots.getListDays().isEmpty()) {
                user.trainingSlots.getDefaultListDays()
                userRepository.upsert(user)
            }

        }
        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            val fromDb = userRepository.getTrainingSlots(uid)

            // <-- ВАЖЛИВО: використовуємо поточне значення слотового стейту
            if (fromDb.isNotEmpty() && displayedSlots.value.isEmpty()) {
                savedStateHandle[KEY_SLOTS] = fromDb
            }

            // Виставляємо nextId після можливої ініціалізації з БД
            nextId = ( (savedStateHandle[KEY_SLOTS] as? List<DaySlot>)
                ?.maxOfOrNull { it.id } ?: 0L) + 1L
        }


        // 2) Підтягнемо дефолтні типи з БД (для пре-філа у нових слотах)
        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            val entity = userRepository.getById(uid) ?: return@launch
            val defaultsFromDb = userRepository.getTrainingDayTypes(entity)
            if (defaultsFromDb.isNotEmpty() && dayDefaults.value.isEmpty()) {
                savedStateHandle[KEY_DAY_DEFAULTS] = defaultsFromDb
            }
        }
    }

    /** Додає новий слот, циклиться по baseDays; якщо їх нема — по стандартному порядку тижня */
    fun addNext() {
        val weekOrder = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        val src = baseDays.value
            .sortedBy { weekOrder.indexOf(it) }
            .ifEmpty { weekOrder }

        val idx = (savedStateHandle[KEY_CURSOR] ?: 0) % src.size
        val day = src[idx]
        savedStateHandle[KEY_CURSOR] = (idx + 1) % src.size

        val prefill = dayDefaults.value[day]?.toList().orEmpty()
        val newSlot = DaySlot(id = nextId++, day = day, types = prefill)


        val updated = displayedSlots.value + newSlot
        savedStateHandle[KEY_SLOTS] = updated
        persistSlots(updated)        // ← ЗБЕРЕГТИ
    }


    /** Змінити типи для конкретного слота */
    fun setSlotTypes(slotId: Long, types: List<String>) {
        val valid = categories.value.toSet()
        val normalized = types.filter { it in valid }
        val updated = displayedSlots.value.map { s ->
            if (s.id == slotId) s.copy(types = normalized) else s
        }
        savedStateHandle[KEY_SLOTS] = updated
        persistSlots(updated)
    }


    /** Видалити слот (якщо потрібно) */
    fun removeSlot(slotId: Long) {
        val updated = displayedSlots.value.filterNot { it.id == slotId }
        savedStateHandle[KEY_SLOTS] = updated
        persistSlots(updated)        // ← ЗБЕРЕГТИ
    }

    private fun persistSlots(slots: List<DaySlot>) {
        viewModelScope.launch {
            val uid = userRepository.currentUserId() ?: return@launch
            userRepository.setTrainingSlots(uid, slots)
        }
    }


    /** Задати дефолтні типи для конкретного ДНЯ (впливатиме на нові слоти) + зберегти в БД */
    fun setDayDefaults(dayCalendarValue: Int, types: List<String>) {
        val valid = categories.value.toSet()
        val normalized = types.filter { it in valid }.toSet()
        val newMap = dayDefaults.value.toMutableMap().apply { put(dayCalendarValue, normalized) }
        savedStateHandle[KEY_DAY_DEFAULTS] = newMap
        persistDayDefaults()
    }

    /** Повертає дефолт для дня (для префіла діалогу) */
    fun defaultsFor(dayCalendarValue: Int): List<String> =
        dayDefaults.value[dayCalendarValue]?.toList().orEmpty()

    /** Синхронізувати з baseDays: прибрати слоти з днями, яких більше немає у профілі */
    fun syncWithBase() {
        val allowed = baseDays.value.toSet()
        if (allowed.isEmpty()) return
        val filtered = displayedSlots.value.filter { it.day in allowed }
        if (filtered.size != displayedSlots.value.size) {
            savedStateHandle[KEY_SLOTS] = filtered
            persistSlots(filtered)   // ← зберегти очищений список
        }
        val cur = savedStateHandle[KEY_CURSOR] ?: 0
        savedStateHandle[KEY_CURSOR] =
            if (baseDays.value.isNotEmpty()) cur % baseDays.value.size else 0

    }

    private fun persistDayDefaults() {
        viewModelScope.launch {
            val userId = userRepository.currentUserId() ?: return@launch
            userRepository.setTrainingDayTypes(userId, dayDefaults.value)
        }
    }
}
