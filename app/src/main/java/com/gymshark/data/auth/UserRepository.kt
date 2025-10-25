package com.gymshark.data.auth

import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun upsert(user: UserEntity)
    suspend fun getById(id: String): UserEntity?
    fun observeById(id: String): Flow<UserEntity?>
    suspend fun deleteById(id: String)

    val currentUserIdFlow: Flow<String?>
    suspend fun setCurrentUserId(id: String)
    suspend fun currentUserId(): String?
}