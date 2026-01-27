package com.gymshark.data.training

import com.gymshark.data.db.entity.BodyWeightEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.models.DailyStatsRow
import com.gymshark.data.models.MoodRow
import com.gymshark.data.models.Train
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

    suspend fun seedExercisesIfEmpty()
}
