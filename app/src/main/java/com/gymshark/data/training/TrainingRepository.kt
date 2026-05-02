package com.gymshark.data.training

import com.gymshark.data.db.entity.BodyWeightEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.domain.models.CategoryDayRow
import com.gymshark.domain.models.CategoryStatRow
import com.gymshark.domain.models.DailyStatsRow
import com.gymshark.domain.models.ExerciseOneRmRow
import com.gymshark.domain.models.MoodRow
import com.gymshark.domain.models.Train
import com.gymshark.domain.models.WeeklyVolumeRow
import com.gymshark.ui.home.training.drafts.TrainingDraft
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {

    fun getCompletedTrains(): Flow<List<Train>>

    suspend fun getTrainByTypes(types: List<String>): Train?

    fun observeBodyWeight(): Flow<List<BodyWeightEntity>>

    suspend fun saveWeight(weight: Float)

    suspend fun saveTrainingDraft(
        training: TrainingsEntity,
        draft: TrainingDraft
    ): Long

    fun observeDailyStats(): Flow<List<DailyStatsRow>>

    fun observeMoodTimeline(): Flow<List<MoodRow>>

    fun observeCategoryStats(): Flow<List<CategoryStatRow>>

    fun observeCategoryDailyStats(): Flow<List<CategoryDayRow>>
    fun observeWeeklyVolume(): Flow<List<WeeklyVolumeRow>>
    fun observeExerciseOneRM(): Flow<List<ExerciseOneRmRow>>

    suspend fun seedExercisesIfEmpty()
    fun observeCompletedExerciseIdsForToday(): Flow<Set<Int>>
}
