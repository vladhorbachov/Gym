package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymshark.data.db.entity.ExercisePrEntity

@Dao
interface ExercisePrDao {
    @Query("SELECT * FROM ExercisePR WHERE exerciseId = :id LIMIT 1")
    suspend fun get(id: Int): ExercisePrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExercisePrEntity)


}
