package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymshark.domain.models.CategoryDayRow
import com.gymshark.domain.models.CategoryStatRow
import com.gymshark.domain.models.DailyStatsRow
import com.gymshark.data.db.entity.TrainingExerciseEntity
import com.gymshark.data.db.entity.TrainingSetEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.domain.models.ExerciseOneRmRow
import com.gymshark.domain.models.MoodRow
import com.gymshark.domain.models.WeeklyVolumeRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: TrainingsEntity): Long

    @Update
    suspend fun update(exercise: TrainingsEntity)

    @Delete
    suspend fun delete(exercise: TrainingsEntity)

    @Query("SELECT * FROM Trainings WHERE id = :id")
    suspend fun getById(id: Int): TrainingsEntity?

    @Query("SELECT * FROM Trainings")
    suspend fun getAll(): List<TrainingsEntity>

    @Query("SELECT * FROM Trainings ORDER BY time DESC")
    fun observeAll(): Flow<List<TrainingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingExercise(entity: TrainingExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingSets(entities: List<TrainingSetEntity>)

    @Transaction
    suspend fun insertTrainingWithDetails(
        training: TrainingsEntity,
        exercises: List<Pair<TrainingExerciseEntity, List<TrainingSetEntity>>>
    ): Long {
        val trainingId = insert(training).toInt()

        exercises.forEach { (exerciseEntity, sets) ->
            val trainingExerciseId = insertTrainingExercise(
                exerciseEntity.copy(trainingId = trainingId)
            ).toInt()

            if (sets.isNotEmpty()) {
                val setsWithFk = sets.map {
                    it.copy(trainingExerciseId = trainingExerciseId)
                }
                insertTrainingSets(setsWithFk)
            }
        }

        return trainingId.toLong()
    }

    @Query(
        """
    SELECT 
        date(time / 1000, 'unixepoch') as day,
        SUM(activeDuration) as totalDuration,
        AVG(avgBPM) as avgBpm,
        SUM(calories) as totalCalories
    FROM Trainings
    GROUP BY day
    ORDER BY day DESC
"""
    )
    fun observeDailyStats(): Flow<List<DailyStatsRow>>

    @Query("""
    SELECT 
        date(time / 1000, 'unixepoch') as day,
        mood
    FROM Trainings
    ORDER BY time DESC
""")
    fun observeMoodTimeline(): Flow<List<MoodRow>>
    @Query("""
    SELECT DISTINCT te.exerciseId
    FROM TrainingExercises te
    INNER JOIN Trainings t ON t.id = te.trainingId
    WHERE date(t.time / 1000, 'unixepoch') = date(:todayMillis / 1000, 'unixepoch')
""")
    fun observeCompletedExerciseIdsForDay(todayMillis: Long): Flow<List<Int>>

    @Query("""
        SELECT
            e.baseCategory        AS category,
            MAX(IFNULL(ts.weight, 0.0)) AS maxWeight,
            COUNT(ts.id)          AS totalSets
        FROM Exercises e
        INNER JOIN TrainingExercises te ON te.exerciseId = e.id
        INNER JOIN TrainingSets ts      ON ts.trainingExerciseId = te.id
        GROUP BY e.baseCategory
        ORDER BY totalSets DESC
    """)
    fun observeCategoryStats(): Flow<List<CategoryStatRow>>

    @Query("""
        SELECT
            date(t.time / 1000, 'unixepoch')    AS day,
            e.baseCategory                       AS category,
            MAX(IFNULL(ts.weight, 0.0))          AS maxWeight,
            COUNT(ts.id)                         AS totalSets
        FROM Trainings t
        INNER JOIN TrainingExercises te ON te.trainingId = t.id
        INNER JOIN Exercises e          ON e.id = te.exerciseId
        INNER JOIN TrainingSets ts      ON ts.trainingExerciseId = te.id
        GROUP BY day, e.baseCategory
        ORDER BY day ASC
    """)
    fun observeCategoryDailyStats(): Flow<List<CategoryDayRow>>

    @Query("""
        SELECT
            strftime('%Y-W%W', t.time / 1000, 'unixepoch') AS week,
            SUM(IFNULL(ts.weight, 0.0) * IFNULL(ts.reps, 0)) AS totalVolume
        FROM Trainings t
        INNER JOIN TrainingExercises te ON te.trainingId = t.id
        INNER JOIN TrainingSets ts      ON ts.trainingExerciseId = te.id
        GROUP BY week
        ORDER BY week ASC
    """)
    fun observeWeeklyVolume(): Flow<List<WeeklyVolumeRow>>

    @Query("""
        SELECT
            date(t.time / 1000, 'unixepoch')                                       AS day,
            e.name                                                                  AS exerciseName,
            MAX(IFNULL(ts.weight, 0.0) * (1.0 + IFNULL(ts.reps, 0) / 30.0))      AS estimated1RM
        FROM Trainings t
        INNER JOIN TrainingExercises te ON te.trainingId = t.id
        INNER JOIN Exercises e          ON e.id = te.exerciseId
        INNER JOIN TrainingSets ts      ON ts.trainingExerciseId = te.id
        GROUP BY day, e.id
        ORDER BY day ASC
    """)
    fun observeExerciseOneRM(): Flow<List<ExerciseOneRmRow>>
}
