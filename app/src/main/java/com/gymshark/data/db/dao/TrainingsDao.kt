package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymshark.domain.models.DailyStatsRow
import com.gymshark.data.db.entity.TrainingExerciseEntity
import com.gymshark.data.db.entity.TrainingSetEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.domain.models.MoodRow
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

}
