package com.gymshark.data.auth

import android.content.Context
import com.gymshark.R
import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.db.dao.TrainingsDao
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.data.db.entity.TrainingExerciseEntity
import com.gymshark.data.db.entity.TrainingSetEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.models.DaySlot
import com.gymshark.data.models.Train
import com.gymshark.ui.home.training.drafts.TrainingDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class TrainingRepository(
    private val trainingsDao: TrainingsDao,
    private val userRepository: UserRepository,
    private val exercisesDao: ExercisesDao,
    private val context: Context
) {

    fun getCompletedTrains(): Flow<List<Train>> {
        val now = LocalDate.now()
        val twoDaysAgo = now.minusDays(2)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterday = now.minusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val t1 = Train(id = 1L, date = yesterday, title = "Leg Day", duration = 45L)
        val t2 = Train(id = 2L, date = twoDaysAgo, title = "Cardio", duration = 30L)

        return flowOf(listOf(t1, t2))
    }


    fun observePlannedDays(): Flow<Set<DayOfWeek>> =
        userRepository.observePlannedDays()

    fun observeDaySlots(): Flow<List<DaySlot>> =
        userRepository.observeDaySlots()

    suspend fun getTrainByTypes(types: List<String>): Train? {
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


    suspend fun getExercisesByCategory(category: List<String>): List<ExercisesEntity> {
        return exercisesDao.getByCategory(category)
    }
    suspend fun saveTrainingDraft(training: TrainingsEntity, draft: TrainingDraft): Long {
        val details: List<Pair<TrainingExerciseEntity, List<TrainingSetEntity>>> =
            draft.exercises.mapIndexed { index, ex ->
                val exEntity = TrainingExerciseEntity(
                    trainingId = 0,
                    exerciseId = ex.exerciseId.toInt(),
                    orderIndex = index
                )

                val sets = ex.sets
                    .filter { it.reps != null || it.weight != null }
                    .map { s ->
                        TrainingSetEntity(
                            trainingExerciseId = 0,
                            reps = s.reps,
                            weight = s.weight
                        )
                    }

                exEntity to sets
            }

        return trainingsDao.insertTrainingWithDetails(training, details)
    }


    private fun parseExercisesFromJson(): List<ExercisesEntity> {
        val inputStream = context.resources.openRawResource(R.raw.exercises)
        val json = inputStream.bufferedReader().use { it.readText() }

        val root = JSONObject(json)
        val listArray = root.getJSONArray("list")

        val result = mutableListOf<ExercisesEntity>()

        for (i in 0 until listArray.length()) {
            val obj = listArray.getJSONObject(i)

            val name = obj.getString("name")
            val baseCategory = obj.getString("baseCategory")
            val subCategory = obj.getString("subCategory")
            val difficulty = obj.getInt("difficulty")

            result.add(
                ExercisesEntity(
                    id = 0,
                    name = name,
                    baseCategory = baseCategory,
                    subCategory = subCategory,
                    difficulty = difficulty
                )
            )
        }

        return result
    }

    suspend fun seedExercisesIfEmpty() {
        val current = exercisesDao.getAll()
        if (current.isNotEmpty()) return

        val fromJson = parseExercisesFromJson()
        exercisesDao.insert(fromJson)
    }
}
