package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymshark.data.db.entity.ExercisesEntity

@Dao
interface ExercisesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExercisesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: List<ExercisesEntity>)

    @Update
    suspend fun update(exercise: ExercisesEntity)

    @Delete
    suspend fun delete(exercise: ExercisesEntity)

    @Query("SELECT * FROM Exercises WHERE id = :id")
    suspend fun getById(id: Int): ExercisesEntity?

    @Query("SELECT * FROM Exercises")
    suspend fun getAll(): List<ExercisesEntity>

    @Query("SELECT * FROM Exercises WHERE baseCategory = :category")
    suspend fun getByCategory(category: String): List<ExercisesEntity>

    @Query("DELETE FROM Exercises")
    suspend fun delete()
}
