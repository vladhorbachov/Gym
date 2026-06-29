package com.gymshark.ui.home.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.domain.repository.pulse.PulseRepository
import com.gymshark.data.db.entity.PulseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PulseViewModel(
    private val pulse: PulseRepository
) : ViewModel() {

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring = _isMeasuring.asStateFlow()

    private val _samples = mutableListOf<Int>()

    private val _minBpm = MutableStateFlow<Int?>(null)
    val minBpm = _minBpm.asStateFlow()

    private val _maxBpm = MutableStateFlow<Int?>(null)
    val maxBpm = _maxBpm.asStateFlow()

    private val _avgBpm = MutableStateFlow<Int?>(null)
    val avgBpm = _avgBpm.asStateFlow()

    fun startMockMeasurement() {
        _isMeasuring.value = true
        _samples.clear()

        repeat(20) {
            val bpm = Random.nextInt(85, 150)
            _samples.add(bpm)
        }

        _minBpm.value = _samples.minOrNull()
        _maxBpm.value = _samples.maxOrNull()
        _avgBpm.value = _samples.average().toInt()

        _isMeasuring.value = false
    }

    fun addMeasurement(bpm: Int) {
        _samples.add(bpm)
        _minBpm.value = _samples.minOrNull()
        _maxBpm.value = _samples.maxOrNull()
        _avgBpm.value = _samples.average().toInt()
    }

    fun setPulse(pulseEntity: PulseEntity){
        viewModelScope.launch {
            runCatching {
                pulse.insert(pulseEntity)
            }
        }
    }
}
