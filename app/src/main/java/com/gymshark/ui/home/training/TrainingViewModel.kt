package com.gymshark.ui.home.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.TrainingRepository
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.models.DaySlot
import com.gymshark.data.models.Train
import com.gymshark.data.models.toTrainingsEntity
import com.gymshark.ui.home.training.drafts.ExerciseDraft
import com.gymshark.ui.home.training.drafts.SetEntry
import com.gymshark.ui.home.training.drafts.TrainingDraft
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
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            trainingRepository.seedExercisesIfEmpty()
        }
    }
    private val _draft = MutableStateFlow(TrainingDraft())
    val draft: StateFlow<TrainingDraft> = _draft.asStateFlow()

    private val _suggestedExercisesFlow =
        MutableStateFlow<List<ExercisesEntity>>(emptyList())
    val suggestedExercisesFlow: StateFlow<List<ExercisesEntity>> =
        _suggestedExercisesFlow.asStateFlow()

    val trainsFlow: StateFlow<List<Train>> =
        trainingRepository.getCompletedTrains()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val trainingDaysFlow: StateFlow<Set<DayOfWeek>> =
        trainingRepository.observePlannedDays()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val trainingSlotsFlow: StateFlow<List<DaySlot>> =
        trainingRepository.observeDaySlots()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val selectionDataFlow = combine(
        trainsFlow,
        trainingSlotsFlow,
        trainingDaysFlow
    ) { completedTrains, daySlots, plannedDays ->
        Triple(completedTrains, daySlots, plannedDays)
    }

    fun setExercisesFromSuggested(list: List<ExercisesEntity>) {
        _draft.update { cur ->
            val oldById = cur.exercises.associateBy { it.exerciseId }

            val newExercises = list.map { e ->
                val id = e.id.toLong()
                val old = oldById[id]
                old?.copy(title = e.name)
                    ?: ExerciseDraft(
                        exerciseId = id,
                        title = e.name,
                        sets = listOf(SetEntry())
                    )
            }

            cur.copy(exercises = newExercises)
        }
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
        durationSec: Long
    ) = viewModelScope.launch {

        val cur = _draft.value
        val startTime = cur.startTime

        val firstExerciseId = cur.exercises.firstOrNull()?.exerciseId?.toInt() ?: 0
        val performedSets = cur.exercises.sumOf { ex ->
            ex.sets.count { s -> s.reps != null || s.weight != null }
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
            minBPM = 70,
            maxBPM = 140,
            avgBPM = 105,
            calories = 0,
            mood = "neutral"
        )

        trainingRepository.saveTrainingDraft(entity, cur)


        _draft.value = TrainingDraft()
    }



    val todayRecommendedTrainFlow: StateFlow<Train?> =
        selectionDataFlow
            .flatMapLatest { (completedTrains, daySlots, plannedDays) ->
                flow {
                    val today = LocalDate.now().dayOfWeek

                    if (!plannedDays.contains(today)) {
                        emit(null)
                        return@flow
                    }

                    val todaySlots: List<DaySlot> = daySlots
                        .filter { it.day == today }
                        .sortedBy { it.id }

                    if (todaySlots.isEmpty()) {
                        emit(null)
                        return@flow
                    }

                    val lastTrainingId: Long = completedTrains
                        .maxOfOrNull { it.id.toLong() } ?: 0L

                    val nextSlot: DaySlot =
                        todaySlots.firstOrNull { it.id > lastTrainingId }
                            ?: todaySlots.first()

                    val train = trainingRepository.getTrainByTypes(nextSlot.types)
                    emit(train)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun loadSuggestedExercises(category: List<String>) {
        viewModelScope.launch {
            runCatching {
                trainingRepository.getExercisesByCategory(category)
            }.onSuccess { list ->
                _suggestedExercisesFlow.value = list
            }.onFailure {
                _suggestedExercisesFlow.value = emptyList()
            }
        }
    }

}
