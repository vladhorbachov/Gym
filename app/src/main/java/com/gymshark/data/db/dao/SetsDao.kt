package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymshark.data.db.entity.SetsEntity

@Dao
interface SetsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: SetsEntity)

    @Update
    suspend fun update(exercise: SetsEntity)

    @Delete
    suspend fun delete(exercise: SetsEntity)

    @Query("SELECT * FROM Sets WHERE id = :id")
    suspend fun getById(id: Int): SetsEntity?

    @Query("SELECT * FROM Sets")
    suspend fun getAll(): List<SetsEntity>
}
