package com.gymshark.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gymshark.data.db.entity.PulseEntity

@Dao
interface PulseDao {
    @Insert
    suspend fun insert(pulse: PulseEntity)

    @Query("SELECT * FROM pulse WHERE activityType = :id")
    suspend fun getPulseByActivityType(id: Long): PulseEntity

    @Query("SELECT * FROM pulse ORDER BY date ASC")
    fun getAllPulses(): LiveData<List<PulseEntity>>

    @Query("SELECT * FROM pulse ORDER BY date DESC LIMIT 1")
    suspend fun getLatestPulse(): PulseEntity?

}