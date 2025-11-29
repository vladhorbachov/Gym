package com.gymshark.data.auth

import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.models.DaySlot
import com.gymshark.data.models.Train
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class TrainingRepository(
    private val userRepository: UserRepository,
    private val exercisesDao: ExercisesDao
) {

    fun getCompletedTrains(): Flow<List<Train>> {
        val now = LocalDate.now()
        val twoDaysAgo = now.minusDays(2)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val yesterday = now.minusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val t1 = Train(id = 1L, date = yesterday, title = "Leg Day")
        val t2 = Train(id = 2L, date = twoDaysAgo, title = "Cardio")

        return kotlinx.coroutines.flow.flowOf(listOf(t1, t2))
    }

    fun observePlannedDays(): Flow<Set<DayOfWeek>> =
        userRepository.observePlannedDays()

    fun observeDaySlots(): Flow<List<DaySlot>> =
        userRepository.observeDaySlots()

    suspend fun getTrainByTypes(types: List<String>): Train? {
        if (types.isEmpty()) return null

        val allExercises = types.flatMap { category ->
            exercisesDao.getByCategory(category)
        }

        if (allExercises.isEmpty()) {
            return null
        }

        val nowMillis = System.currentTimeMillis()

        val title = types.joinToString(" + ")

        return Train(
            id = nowMillis,
            date = nowMillis,
            title = title
        )
    }
}
