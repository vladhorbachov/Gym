package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymshark.data.db.entity.BodyWeightEntity

@Dao
interface BodyWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BodyWeightEntity)

    @Query("SELECT * FROM body_weight ORDER BY day DESC")
    suspend fun getAll(): List<BodyWeightEntity>

    @Query("SELECT * FROM body_weight WHERE day = :day LIMIT 1")
    suspend fun getByDay(day: Long): BodyWeightEntity?

    @Query("SELECT * FROM body_weight ORDER BY day ASC")
    suspend fun getForChart(): List<BodyWeightEntity>

    @Query("DELETE FROM body_weight")
    suspend fun clear()
}
