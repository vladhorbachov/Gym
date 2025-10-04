package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymshark.data.db.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(user: UserEntity)

    @Update
    suspend fun updateRoom(user: UserEntity)

    @Delete
    suspend fun deleteRoom(user: UserEntity)

    @Query("SELECT * FROM User WHERE userId=:userId")
    suspend fun getUserByIdRoom(userId: String): UserEntity?
}