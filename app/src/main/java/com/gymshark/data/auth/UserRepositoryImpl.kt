package com.gymshark.data.auth

import com.gymshark.data.db.dao.UserDao
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.models.DaySlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val currentUserStore: CurrentUserStore
) : UserRepository {

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
        currentUserIdFlow
            .filterNotNull()
            .flatMapLatest { id -> observeById(id) }
            .map { user ->
                user?.trainingSlots?.map { it.day }?.toSet() ?: emptySet()

            }
            .distinctUntilChanged()

}
