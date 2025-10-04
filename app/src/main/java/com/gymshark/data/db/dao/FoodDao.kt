package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymshark.data.db.entity.FoodEntity

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: FoodEntity)

    @Update
    suspend fun update(exercise: FoodEntity)

    @Delete
    suspend fun delete(exercise: FoodEntity)

    @Query("SELECT * FROM Food WHERE id = :id")
    suspend fun getById(id: Int): FoodEntity?

    @Query("SELECT * FROM Food")
    suspend fun getAll(): List<FoodEntity>
}
