package com.gymshark.data.user

import com.gymshark.data.db.entity.UserEntity
import com.gymshark.domain.models.DaySlot
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
private const val KEY_USER_ID = "1"
interface UserRepository {


    suspend fun upsert(user: UserEntity)
    suspend fun getById(id: String = KEY_USER_ID): UserEntity?
    fun observeById(id: String): Flow<UserEntity?>
    suspend fun deleteById(id: String)

    val currentUserIdFlow: Flow<String?>
    suspend fun setCurrentUserId(id: String = KEY_USER_ID)
    suspend fun currentUserId(): String?

    suspend fun setTrainingDayTypes(userId: String, dayToTypes: Map<Int, Set<String>>)
    fun getTrainingDayTypes(entity: UserEntity?): Map<Int, Set<String>>
    suspend fun getTrainingSlots(userId: String): List<DaySlot>
    suspend fun setTrainingSlots(userId: String, slots: List<DaySlot>)

    fun observePlannedDays(): Flow<Set<DayOfWeek>>
    suspend fun savePlannedDays(days: Set<DayOfWeek>)
    fun observeDaySlots(): Flow<List<DaySlot>>


}