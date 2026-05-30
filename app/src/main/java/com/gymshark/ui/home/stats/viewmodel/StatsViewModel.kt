package com.gymshark.ui.home.stats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.training.TrainingRepository
import com.gymshark.domain.models.CategoryDayRow
import com.gymshark.domain.models.CategoryStatRow
import com.gymshark.domain.models.DailyStatsUi
import com.gymshark.domain.models.ExerciseOneRmRow
import com.gymshark.domain.models.MoodDayUi
import com.gymshark.domain.models.WeeklyVolumeRow
import com.gymshark.domain.models.toMoodUi
import com.gymshark.ui.home.stats.ChartPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StatsViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val shortDateFormatter = DateTimeFormatter.ofPattern("MM.dd")

    private val rawDailyStats = trainingRepository.observeDailyStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dailyStats = rawDailyStats.map { list ->
        list.map {
            DailyStatsUi(
                day = it.day,
                totalMinutes = it.totalDuration / 60,
                avgBpm = it.avgBpm?.toInt(),
                calories = it.totalCalories
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val moodTimeline = trainingRepository.observeMoodTimeline()
        .map { rows ->
            rows
                .groupBy { it.day }
                .map { (day, list) ->
                    val last = list.first()
                    MoodDayUi(day, last.mood.toMoodUi())
                }
                .take(7)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyMood = moodTimeline.map { list ->
        list.groupingBy { it.mood }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    val categoryStats: StateFlow<List<CategoryStatRow>> =
        trainingRepository.observeCategoryStats()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weightProgress = trainingRepository.observeBodyWeight()
        .map { list ->
            list.sortedBy { it.day }
                .mapIndexed { i, w ->
                    ChartPoint(
                        x = i.toFloat(),
                        y = w.weight,
                        label = Instant.ofEpochMilli(w.day)
                            .atZone(ZoneId.systemDefault())
                            .format(shortDateFormatter)
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val durationProgress = rawDailyStats.map { list ->
        list.reversed().mapIndexed { i, row ->
            ChartPoint(i.toFloat(), row.totalDuration / 60f, row.day.toShortLabel())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val caloriesProgress = rawDailyStats.map { list ->
        list.reversed().mapIndexed { i, row ->
            ChartPoint(i.toFloat(), row.totalCalories.toFloat(), row.day.toShortLabel())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bpmProgress = rawDailyStats.map { list ->
        list.reversed().mapIndexed { i, row ->
            row.avgBpm?.let { ChartPoint(i.toFloat(), it, row.day.toShortLabel()) }
        }.filterNotNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoryDailyStats: StateFlow<List<CategoryDayRow>> =
        trainingRepository.observeCategoryDailyStats()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyVolume: StateFlow<List<WeeklyVolumeRow>> =
        trainingRepository.observeWeeklyVolume()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Top 5 exercises by session count to avoid cluttered chart
    val topExerciseOneRM: StateFlow<List<ExerciseOneRmRow>> =
        trainingRepository.observeExerciseOneRM()
            .map { rows ->
                val top5 = rows.groupBy { it.exerciseName }
                    .entries.sortedByDescending { it.value.size }
                    .take(5)
                    .map { it.key }
                    .toSet()
                rows.filter { it.exerciseName in top5 }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun String.toShortLabel(): String =
        if (length >= 10) substring(5).replace("-", ".") else this
}
