package com.gymshark.ui.home.training

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.exercises.ExerciseRepository
import com.gymshark.data.training.TrainingRepository
import com.gymshark.data.user.UserRepository
import com.gymshark.domain.models.DaySlot
import com.gymshark.domain.models.Train
import com.gymshark.ui.home.training.drafts.ExerciseDraft
import com.gymshark.ui.home.training.drafts.SetEntry
import com.gymshark.ui.home.training.drafts.TrainingDraft
import com.gymshark.ui.home.training.drafts.isPerformed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class TrainingViewModel(
    private val trainingRepository: TrainingRepository,
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    companion object {
        private const val KEY_ORDER_PREFIX = "training_exercise_order_"
    }

    init {
        viewModelScope.launch {
            trainingRepository.seedExercisesIfEmpty()
        }
    }

    private val _draft = MutableStateFlow(TrainingDraft())
    val draft: StateFlow<TrainingDraft> = _draft.asStateFlow()

    val completedExerciseIdsFlow: StateFlow<Set<Int>> =
        trainingRepository
            .observeCompletedExerciseIdsForToday()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())


    private val _suggestedExercisesFlow =
        MutableStateFlow<List<ExercisesEntity>>(emptyList())
    val suggestedExercisesFlow: StateFlow<List<ExercisesEntity>> =
        _suggestedExercisesFlow.asStateFlow()

    private var currentOrderKey: String? = null

    val trainsFlow: StateFlow<List<Train>> =
        trainingRepository.getCompletedTrains()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val trainingDaysFlow: StateFlow<Set<DayOfWeek>> =
        userRepository.observePlannedDays()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val trainingSlotsFlow: StateFlow<List<DaySlot>> =
        userRepository.observeDaySlots()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val selectionDataFlow = combine(
        trainsFlow,
        trainingSlotsFlow,
        trainingDaysFlow
    ) { completedTrains, daySlots, plannedDays ->
        Triple(completedTrains, daySlots, plannedDays)
    }

    fun reorderExercises(from: Int, to: Int) {
        _draft.update { cur ->
            val list = cur.exercises.toMutableList()
            if (from !in list.indices || to !in list.indices) return@update cur
            val item = list.removeAt(from)
            list.add(to, item)
            cur.copy(exercises = list).also {
                persistCurrentExerciseOrder(list)
            }
        }
    }

    fun removeExerciseAt(index: Int) {
        _draft.update { cur ->
            if (index !in cur.exercises.indices) return@update cur
            val list = cur.exercises.toMutableList()
            list.removeAt(index)
            cur.copy(exercises = list).also {
                persistCurrentExerciseOrder(list)
            }
        }
    }

    fun setExercisesFromSuggested(list: List<ExercisesEntity>) {
        val orderKey = buildOrderKey(list)
        currentOrderKey = orderKey
        val savedOrder = readExerciseOrder(orderKey)

        _draft.update { cur ->
            val oldById = cur.exercises.associateBy { it.exerciseId }

            val newExercises = applySavedExerciseOrder(list, savedOrder).map { e ->
                val id = e.id.toLong()
                val old = oldById[id]
                old?.copy(title = e.name)
                    ?: ExerciseDraft(
                        exerciseId = id,
                        title = e.name,
                        baseCategory = e.baseCategory,
                        sets = listOf(SetEntry())
                    )
            }

            cur.copy(exercises = newExercises)
        }
    }

    private fun buildOrderKey(list: List<ExercisesEntity>): String {
        val signature = list
            .map { it.baseCategory }
            .distinct()
            .sorted()
            .joinToString(separator = "|")
            .ifBlank { "empty" }
        return KEY_ORDER_PREFIX + signature
    }

    private fun applySavedExerciseOrder(
        list: List<ExercisesEntity>,
        savedOrder: List<Int>?
    ): List<ExercisesEntity> {
        if (savedOrder == null) return list
        val byId = list.associateBy { it.id }
        return savedOrder.mapNotNull { byId[it] }
    }

    private fun persistCurrentExerciseOrder(list: List<ExerciseDraft>) {
        val key = currentOrderKey ?: return
        val value = list.joinToString(separator = ",") { it.exerciseId.toString() }
        prefs.edit().putString(key, value).apply()
    }

    private fun readExerciseOrder(key: String): List<Int>? {
        if (!prefs.contains(key)) return null
        return prefs.getString(key, "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.toIntOrNull() }
    }

    fun updateExerciseSets(exerciseId: Long, sets: List<SetEntry>) {
        _draft.update { cur ->
            cur.copy(
                exercises = cur.exercises.map { ex ->
                    if (ex.exerciseId == exerciseId) ex.copy(sets = sets) else ex
                }
            )
        }
    }

    fun getExerciseDraft(exerciseId: Long): ExerciseDraft? =
        _draft.value.exercises.firstOrNull { it.exerciseId == exerciseId }

    fun finishTraining(
        title: String,
        finishTime: Long,
        durationSec: Long,
        mood: String,
        minBpm: Int?,
        maxBpm: Int?,
        avgBpm: Int?
    ) = viewModelScope.launch {

        val cur = _draft.value
        val startTime = cur.startTime

        val firstExerciseId = cur.exercises.firstOrNull()?.exerciseId?.toInt() ?: 0
        val performedSets = cur.exercises.sumOf { ex ->
            ex.sets.count { it.isPerformed() }
        }

        val entity = TrainingsEntity(
            name = title,
            exerciseId = firstExerciseId,
            time = finishTime,
            setsCount = performedSets,
            startTime = startTime,
            finishTime = finishTime,
            fullDuration = durationSec,
            activeDuration = durationSec,
            minBPM = minBpm,
            maxBPM = maxBpm,
            avgBPM = avgBpm,
            calories = 0,
            mood = mood
        )

        trainingRepository.saveTrainingDraft(entity, cur)
        _draft.value = TrainingDraft()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayRecommendedTrainFlow: StateFlow<Train?> =
        selectionDataFlow
            .flatMapLatest { (completedTrains, daySlots, plannedDays) ->
                flow {
                    val today = LocalDate.now().dayOfWeek

                    if (!plannedDays.contains(today)) {
                        emit(null)
                        return@flow
                    }

                    val todaySlots = daySlots
                        .filter { it.day == today }
                        .sortedBy { it.id }

                    if (todaySlots.isEmpty()) {
                        emit(null)
                        return@flow
                    }

                    val lastTrainingId =
                        completedTrains.maxOfOrNull { it.id.toLong() } ?: 0L

                    val nextSlot =
                        todaySlots.firstOrNull { it.id > lastTrainingId }
                            ?: todaySlots.first()

                    emit(trainingRepository.getTrainByTypes(nextSlot.types))
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun loadSuggestedExercises(category: List<String>) {
        viewModelScope.launch {
            runCatching {
                exerciseRepository.getByCategory(category)
            }.onSuccess {
                _suggestedExercisesFlow.value = it
            }.onFailure {
                _suggestedExercisesFlow.value = emptyList()
            }
        }
    }

    fun moveTrainingSlot(sourceDate: LocalDate, targetDate: LocalDate) {
        if (sourceDate == targetDate) return

        viewModelScope.launch {
            val userId = userRepository.currentUserId() ?: return@launch
            val slots = userRepository.getTrainingSlots(userId)
            val sourceSlot = slotForDate(sourceDate, slots)?.takeIf { it.types.isNotEmpty() }
                ?: return@launch

            val targetSlot = slotForDate(targetDate, slots)
            if (targetSlot?.types?.isNotEmpty() == true) return@launch

            val updated = if (targetSlot != null) {
                slots.map { slot ->
                    when (slot.id) {
                        sourceSlot.id -> slot.copy(types = emptyList())
                        targetSlot.id -> slot.copy(types = sourceSlot.types)
                        else -> slot
                    }
                }
            } else {
                slots.map { slot ->
                    if (slot.id == sourceSlot.id) {
                        slot.copy(day = targetDate.dayOfWeek)
                    } else {
                        slot
                    }
                }
            }

            userRepository.setTrainingSlots(userId, updated)
        }
    }

    private fun slotForDate(date: LocalDate, slots: List<DaySlot>): DaySlot? {
        val slotsForDay = slots
            .filter { it.day == date.dayOfWeek }
            .sortedBy { it.id }

        if (slotsForDay.isEmpty()) return null

        val firstPlannedDate = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(date.dayOfWeek))

        if (date.isBefore(firstPlannedDate)) return null

        val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(firstPlannedDate, date)
        val index = (weeksBetween % slotsForDay.size).toInt()

        return slotsForDay[index]
    }
}
