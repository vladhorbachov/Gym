package com.gymshark.ui.home.stats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.TrainingRepository
import com.gymshark.data.models.DailyStatsUi
import com.gymshark.data.models.MoodDayUi
import com.gymshark.data.models.toMoodUi
import com.gymshark.ui.home.stats.ChartPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {


    val dailyStats =
        trainingRepository.observeDailyStats()
            .map { list ->
                list.map {
                    DailyStatsUi(
                        day = it.day,
                        totalMinutes = it.totalDuration / 60,
                        avgBpm = it.avgBpm?.toInt(),
                        calories = it.totalCalories
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )
    val moodTimeline =
        trainingRepository.observeMoodTimeline()
            .map { rows ->
                rows
                    .groupBy { it.day }
                    .map { (day, list) ->
                        val last = list.first()
                        MoodDayUi(day, last.mood.toMoodUi())
                    }
                    .take(7)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    val weeklyMood =
        moodTimeline.map { list ->
            list.groupingBy { it.mood }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
        }


    private val _weightProgress =
        MutableStateFlow<List<ChartPoint>>(emptyList())
    val weightProgress = _weightProgress.asStateFlow()

    fun loadWeightProgress() {
        viewModelScope.launch {
            val points = (1..30).map {
                ChartPoint(
                    x = it.toFloat(),
                    y = (70..85).random().toFloat()
                )
            }
            _weightProgress.value = points
        }
    }
}
