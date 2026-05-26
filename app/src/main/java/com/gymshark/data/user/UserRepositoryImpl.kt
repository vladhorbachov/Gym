package com.gymshark.data.user

import com.gymshark.data.db.dao.UserDao
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.prefs.UserPrefs
import com.gymshark.domain.models.DaySlot
import com.gymshark.domain.models.Series
import com.gymshark.domain.models.toDaysSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId


class UserRepositoryImpl(
    private val userDao: UserDao,
    private val currentUserStore: CurrentUserStore,
    private val prefs: UserPrefs
) : UserRepository {
    private fun currentUserIdOrFallback(): String =
        currentUserStore.currentUserIdOrNull() ?: prefs.getCurrentUserId() ?: "1"

    override suspend fun upsert(user: UserEntity) = userDao.upsert(user)
    override suspend fun getById(id: String) = userDao.getById(id)
    override fun observeById(id: String): Flow<UserEntity?> = userDao.observeById(id)
    override suspend fun deleteById(id: String) = userDao.deleteById(id)

    override val currentUserIdFlow: Flow<String?> = currentUserStore.currentUserIdFlow
    override suspend fun setCurrentUserId(id: String) = currentUserStore.setCurrentUserId(id)
    override suspend fun currentUserId(): String? = currentUserStore.currentUserIdOrNull()
    override suspend fun setTrainingDayTypes(userId: String, dayToTypes: Map<Int, Set<String>>) {
        val user = userDao.getById(userId) ?: return
        userDao.upsert(user.copy(trainingDayTypes = dayToTypes))
    }

    override fun getTrainingDayTypes(entity: UserEntity?): Map<Int, Set<String>> =
        entity?.trainingDayTypes ?: emptyMap()

    override suspend fun getTrainingSlots(userId: String): List<DaySlot> {
        val u = userDao.getById(userId) ?: return emptyList()
        return u.trainingSlots
    }

    override suspend fun setTrainingSlots(userId: String, slots: List<DaySlot>) {
        val u = userDao.getById(userId) ?: return
        userDao.upsert(u.copy(trainingSlots = slots))
    }

    override fun observePlannedDays(): Flow<Set<DayOfWeek>> =
        observeById(currentUserIdOrFallback())
            .map { user ->
                user?.trainingSlots?.map { it.day }?.toSet() ?: emptySet()
            }
            .distinctUntilChanged()

    override fun observeDaySlots(): Flow<List<DaySlot>> =
        observeById(currentUserIdOrFallback())
            .map { user ->
                user?.trainingSlots ?: emptyList()
            }
            .distinctUntilChanged()

    override suspend fun registerActivity(currentTimeMillis: Long) {
        val user = userDao.getById(currentUserIdOrFallback()) ?: return

        if (user.series.lastSession == 0L) {
            userDao.upsert(
                user.copy(
                    series = user.series.copy(
                        current = 1,
                        maxSeries = maxOf(user.series.maxSeries, 1),
                        lastSession = currentTimeMillis
                    )
                )
            )
            return
        }

        val zoneId = ZoneId.systemDefault()

        val today = Instant.ofEpochMilli(currentTimeMillis)
            .atZone(zoneId)
            .toLocalDate()

        val lastDay = Instant.ofEpochMilli(user.series.lastSession)
            .atZone(zoneId)
            .toLocalDate()

        val diffDays = today.toEpochDay() - lastDay.toEpochDay()

        when {
            diffDays == 0L -> {
                userDao.upsert(
                    user.copy(
                        series = user.series.copy(
                            lastSession = currentTimeMillis
                        )
                    )
                )
            }

            diffDays == 1L -> {
                val newCurrent = user.series.current + 1
                val newMax = maxOf(user.series.maxSeries, newCurrent)

                userDao.upsert(
                    user.copy(
                        series = user.series.copy(
                            current = newCurrent,
                            maxSeries = newMax,
                            lastSession = currentTimeMillis
                        )
                    )
                )
            }

            else -> {
                userDao.upsert(
                    user.copy(
                        series = user.series.copy(
                            current = 1,
                            maxSeries = user.series.maxSeries,
                            lastSession = currentTimeMillis
                        )
                    )
                )
            }
        }
    }

    override suspend fun refreshSeriesState(currentTimeMillis: Long) {
        val userId = currentUserIdOrFallback()
        val user = userDao.getById(userId) ?: return

        val lastSession = user.series.lastSession
        if (lastSession == 0L) return

        val zoneId = ZoneId.systemDefault()

        val today = Instant.ofEpochMilli(currentTimeMillis)
            .atZone(zoneId)
            .toLocalDate()

        val lastDay = Instant.ofEpochMilli(lastSession)
            .atZone(zoneId)
            .toLocalDate()

        val diffDays = today.toEpochDay() - lastDay.toEpochDay()

        when {
            diffDays <= 1L -> {
                return
            }

            else -> {
                if (user.series.current != 0) {
                    userDao.upsert(
                        user.copy(
                            series = user.series.copy(
                                current = 0
                            )
                        )
                    )
                }
            }
        }
    }

    override suspend fun savePlannedDays(days: Set<DayOfWeek>) {
        val slots = days.toList().toDaysSlot()
        setTrainingSlots(currentUserIdOrFallback(), slots)
    }

    override suspend fun debugSetSeries(series: Series) {
        val user = userDao.getById(currentUserIdOrFallback()) ?: return
        userDao.upsert(user.copy(series = series))
    }
}
