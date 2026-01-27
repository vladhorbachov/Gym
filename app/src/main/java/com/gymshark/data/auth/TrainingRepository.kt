package com.gymshark.data.auth

import android.content.Context
import androidx.room.withTransaction
import com.gymshark.R
import com.gymshark.data.db.DataBase
import com.gymshark.data.db.dao.BodyWeightDao
import com.gymshark.data.db.dao.ExercisePrDao
import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.db.dao.TrainingsDao
import com.gymshark.data.db.entity.BodyWeightEntity
import com.gymshark.data.models.DailyStatsRow
import com.gymshark.data.db.entity.ExercisePrEntity
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.data.models.PrUpdate
import com.gymshark.data.db.entity.TrainingExerciseEntity
import com.gymshark.data.db.entity.TrainingSetEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.models.DaySlot
import com.gymshark.data.models.MoodRow
import com.gymshark.data.models.Train
import com.gymshark.ui.home.training.drafts.TrainingDraft
import com.gymshark.ui.home.training.drafts.isPerformed
import com.gymshark.ui.home.training.drafts.normalizedWeightOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class TrainingRepository(
    private val db: DataBase,
    private val trainingsDao: TrainingsDao,
    private val exercisePrDao: ExercisePrDao,
    private val userRepository: UserRepository,
    private val exercisesDao: ExercisesDao,
    private val bodyWeightDao: BodyWeightDao,
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

    fun observeBodyWeight(): Flow<List<BodyWeightEntity>> = flow{
            emit(bodyWeightDao.getAll())
        }

    suspend fun saveWeight(weight: Float){
        val today = LocalDate.now()
        bodyWeightDao.upsert(
            BodyWeightEntity(
                day = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                weight = weight,
                updatedAt = System.currentTimeMillis()
            )
        )

    }
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
        val details = buildDetails(draft)
        val prUpdates = buildPrUpdates(draft)

        return db.withTransaction {
            val trainingId = trainingsDao.insertTrainingWithDetails(training, details).toInt()

            prUpdates.forEach { up ->
                val old = exercisePrDao.get(up.exerciseId)
                val addedSets = up.addedSets
                val sessionMax = up.sessionMaxWeight

                val newTotal = (old?.totalSetsLifetime ?: 0) + addedSets

                val oldMax = old?.maxWeight ?: 0f
                val isNewPr = sessionMax != null && sessionMax > oldMax

                val newEntity = ExercisePrEntity(
                    exerciseId = up.exerciseId,
                    exerciseName = up.exerciseName,
                    maxWeight = if (isNewPr) sessionMax!! else oldMax,
                    trainingIdWherePR = if (isNewPr) trainingId else (old?.trainingIdWherePR ?: trainingId),
                    totalSetsLifetime = newTotal,
                    updatedAt = System.currentTimeMillis()
                )

                exercisePrDao.upsert(newEntity)
            }

            trainingId.toLong()
        }
    }

    private fun buildDetails(
        draft: TrainingDraft
    ): List<Pair<TrainingExerciseEntity, List<TrainingSetEntity>>> {
        return draft.exercises.mapIndexed { index, ex ->
            val exEntity = TrainingExerciseEntity(
                trainingId = 0,
                exerciseId = ex.exerciseId.toInt(),
                orderIndex = index
            )

            val sets = ex.sets
                .filter { it.isPerformed() }
                .map { s ->
                    TrainingSetEntity(
                        trainingExerciseId = 0,
                        reps = s.reps, // >0
                        weight = s.normalizedWeightOrNull()
                    )
                }

            exEntity to sets
        }
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
    private fun buildPrUpdates(draft: TrainingDraft): List<PrUpdate> {
        return draft.exercises.map { ex ->
            val performed = ex.sets.filter { it.isPerformed() }
            val addedSets = performed.size

            val sessionMaxWeight: Float? = performed
                .mapNotNull { it.normalizedWeightOrNull() }
                .maxOrNull()

            PrUpdate(
                exerciseId = ex.exerciseId.toInt(),
                exerciseName = ex.title,
                addedSets = addedSets,
                sessionMaxWeight = sessionMaxWeight
            )
        }.filter { it.addedSets > 0 }
    }


    suspend fun seedExercisesIfEmpty() {
        val current = exercisesDao.getAll()
        if (current.isNotEmpty()) return

        val fromJson = parseExercisesFromJson()
        exercisesDao.insert(fromJson)
    }

    fun observeDailyStats(): Flow<List<DailyStatsRow>> =
        trainingsDao.observeDailyStats()

    fun observeMoodTimeline(): Flow<List<MoodRow>> =
        trainingsDao.observeMoodTimeline()

}
