package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymshark.data.db.entity.TrainingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: TrainingsEntity)

    @Update
    suspend fun update(exercise: TrainingsEntity)

    @Delete
    suspend fun delete(exercise: TrainingsEntity)

    @Query("SELECT * FROM Trainings WHERE id = :id")
    suspend fun getById(id: Int): TrainingsEntity?

    @Query("SELECT * FROM Trainings")
    suspend fun getAll(): List<TrainingsEntity>

    @Query("SELECT * FROM Trainings ORDER BY time DESC")
    fun observeAll(): Flow<List<TrainingsEntity>>
}
