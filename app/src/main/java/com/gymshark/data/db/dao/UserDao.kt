package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM user WHERE userId = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM user WHERE userId = :id LIMIT 1")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("DELETE FROM user WHERE userId = :id")
    suspend fun deleteById(id: String)
}