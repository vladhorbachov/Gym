package com.gymshark.ui.home.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.TrainingRepository
import com.gymshark.data.models.Train
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek

class TrainingViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    val trainsFlow: StateFlow<List<Train>> =
        trainingRepository.getCompletedTrains()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val trainingDaysFlow: StateFlow<Set<DayOfWeek>> =
        trainingRepository.observePlannedDays()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
}
