package com.gymshark.domain.repository.pulse

import androidx.lifecycle.LiveData
import com.gymshark.data.db.entity.PulseEntity

interface PulseRepository {

    suspend fun insert(pulse: PulseEntity)

    suspend fun getPulseByActivityType(id: Long): PulseEntity

    fun getAllPulses(): LiveData<List<PulseEntity>>

    suspend fun getLatestPulse(): PulseEntity?
}