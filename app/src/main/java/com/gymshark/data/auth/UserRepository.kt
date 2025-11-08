package com.gymshark.data.auth

import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.models.DaySlot
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun upsert(user: UserEntity)
    suspend fun getById(id: String): UserEntity?
    fun observeById(id: String): Flow<UserEntity?>
    suspend fun deleteById(id: String)

    val currentUserIdFlow: Flow<String?>
    suspend fun setCurrentUserId(id: String)
    suspend fun currentUserId(): String?

    suspend fun setTrainingDayTypes(userId: String, dayToTypes: Map<Int, Set<String>>)
    fun getTrainingDayTypes(entity: UserEntity?): Map<Int, Set<String>>
    suspend fun getTrainingSlots(userId: String): List<DaySlot>
    suspend fun setTrainingSlots(userId: String, slots: List<DaySlot>)

}