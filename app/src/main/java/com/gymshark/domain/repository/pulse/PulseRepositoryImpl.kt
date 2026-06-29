package com.gymshark.domain.repository.pulse

import androidx.lifecycle.LiveData
import com.gymshark.data.db.dao.PulseDao
import com.gymshark.data.db.entity.PulseEntity

class PulseRepositoryImpl(private val pulseDao: PulseDao) : PulseRepository {
    override suspend fun insert(pulse: PulseEntity) {
        pulseDao.insert(pulse)
    }

    override suspend fun getPulseByActivityType(id: Long): PulseEntity {
        return pulseDao.getPulseByActivityType(id)
    }

    override fun getAllPulses(): LiveData<List<PulseEntity>> {
        return pulseDao.getAllPulses()
    }

    override suspend fun getLatestPulse(): PulseEntity? {
        return pulseDao.getLatestPulse()
    }
}