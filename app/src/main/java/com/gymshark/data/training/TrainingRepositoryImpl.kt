package com.gymshark.data.training

import android.content.Context
import androidx.room.withTransaction
import com.gymshark.data.db.DataBase
import com.gymshark.data.db.dao.BodyWeightDao
import com.gymshark.data.db.dao.ExercisePrDao
import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.db.dao.TrainingsDao
import com.gymshark.data.db.entity.BodyWeightEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.domain.models.CategoryDayRow
import com.gymshark.domain.models.CategoryStatRow
import com.gymshark.domain.models.DailyStatsRow
import com.gymshark.domain.models.ExerciseOneRmRow
import com.gymshark.domain.models.MoodRow
import com.gymshark.domain.models.Train
import com.gymshark.domain.models.WeeklyVolumeRow
import com.gymshark.data.training.mapper.PrUpdateMapper
import com.gymshark.data.training.mapper.TrainingDraftMapper
import com.gymshark.data.training.seed.ExercisesSeedDataSource
import com.gymshark.ui.home.training.drafts.TrainingDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class TrainingRepositoryImpl(
    private val db: DataBase,
    private val trainingsDao: TrainingsDao,
    private val exercisePrDao: ExercisePrDao,
    private val exercisesDao: ExercisesDao,
    private val bodyWeightDao: BodyWeightDao,
    private val context: Context,
    private val draftMapper: TrainingDraftMapper,
    private val prUpdateMapper: PrUpdateMapper,
    private val seedDataSource: ExercisesSeedDataSource
) : TrainingRepository {

    override fun getCompletedTrains(): Flow<List<Train>> {
        val now = LocalDate.now()
        val twoDaysAgo = now.minusDays(2)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterday = now.minusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val t1 = Train(id = 1L, date = yesterday, title = "Leg Day", duration = 45L)
        val t2 = Train(id = 2L, date = twoDaysAgo, title = "Cardio", duration = 30L)

        return flowOf(listOf(t1, t2))
    }


    override fun observeBodyWeight(): Flow<List<BodyWeightEntity>> = flow {
        emit(bodyWeightDao.getAll())
    }

    override suspend fun saveWeight(weight: Float) {
        val today = LocalDate.now()
        bodyWeightDao.upsert(
            BodyWeightEntity(
                day = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                weight = weight,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun saveTrainingDraft(
        training: TrainingsEntity,
        draft: TrainingDraft
    ): Long {
        val details = draftMapper.buildDetails(draft)
        val prUpdates = prUpdateMapper.buildPrUpdates(draft)

        return db.withTransaction {
            val trainingId =
                trainingsDao.insertTrainingWithDetails(training, details).toInt()

            prUpdates.forEach { up ->
                val old = exercisePrDao.get(up.exerciseId)

                val newTotal = (old?.totalSetsLifetime ?: 0) + up.addedSets
                val oldMax = old?.maxWeight ?: 0f
                val isNewPr =
                    up.sessionMaxWeight != null && up.sessionMaxWeight > oldMax

                exercisePrDao.upsert(
                    com.gymshark.data.db.entity.ExercisePrEntity(
                        exerciseId = up.exerciseId,
                        exerciseName = up.exerciseName,
                        maxWeight = if (isNewPr) up.sessionMaxWeight!! else oldMax,
                        trainingIdWherePR = if (isNewPr) trainingId
                        else old?.trainingIdWherePR ?: trainingId,
                        totalSetsLifetime = newTotal,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            trainingId.toLong()
        }
    }

    override fun observeDailyStats(): Flow<List<DailyStatsRow>> =
        trainingsDao.observeDailyStats()

    override fun observeMoodTimeline(): Flow<List<MoodRow>> =
        trainingsDao.observeMoodTimeline()

    override fun observeCategoryStats(): Flow<List<CategoryStatRow>> =
        trainingsDao.observeCategoryStats()

    override fun observeCategoryDailyStats(): Flow<List<CategoryDayRow>> =
        trainingsDao.observeCategoryDailyStats()

    override fun observeWeeklyVolume(): Flow<List<WeeklyVolumeRow>> =
        trainingsDao.observeWeeklyVolume()

    override fun observeExerciseOneRM(): Flow<List<ExerciseOneRmRow>> =
        trainingsDao.observeExerciseOneRM()

    override suspend fun seedExercisesIfEmpty() {
        if (exercisesDao.getAll().isNotEmpty()) return
        exercisesDao.insert(seedDataSource.load(context))
    }

    override suspend fun getTrainByTypes(types: List<String>): Train? {
        if (types.isEmpty()) return null

        val allExercises = types.flatMap { category ->
            exercisesDao.getByCategory(listOf(category))
        }

        if (allExercises.isEmpty()) return null

        val nowMillis = System.currentTimeMillis()
        val title = types.joinToString(" + ")

        return Train(
            id = nowMillis,
            date = nowMillis,
            title = title,
            duration = 0L
        )
    }
    override fun observeCompletedExerciseIdsForToday(): Flow<Set<Int>> {
        val todayMillis = System.currentTimeMillis()
        return trainingsDao.observeCompletedExerciseIdsForDay(todayMillis)
            .map { it.toSet() }
    }

}
