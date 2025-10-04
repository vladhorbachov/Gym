package com.gymshark.data.auth

import com.gymshark.data.db.entity.UserEntity

interface UserRepository {
    suspend fun save(user: UserEntity)
    suspend fun getById(id: String): UserEntity?
}