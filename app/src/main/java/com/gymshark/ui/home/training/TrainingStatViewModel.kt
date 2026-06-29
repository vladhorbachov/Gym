package com.gymshark.ui.home.training

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.gymshark.service.timer.TrainingTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrainingStatViewModel(app: Application) : AndroidViewModel(app) {

    val elapsedSeconds = TrainingTimerService.elapsedSeconds
    val isRunning = TrainingTimerService.isRunning
    private val _startTime = MutableStateFlow<Long?>(null)
    val startTime = _startTime.asStateFlow()
    fun start() {
        if (_startTime.value == null) {
            _startTime.value = System.currentTimeMillis()
        }
        send("START")
    }
    fun pause() = send("PAUSE")
    fun stop() = send("STOP")

    private fun send(action: String) {
        val ctx = getApplication<Application>()
        val intent = Intent(ctx, TrainingTimerService::class.java).apply {
            this.action = action
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent)
        } else {
            ctx.startService(intent)
        }
    }
    fun stopAndReset(): Pair<Long?, Long> {
        val st = _startTime.value
        val durationSec = elapsedSeconds.value

        stop()
        _startTime.value = null
        TrainingTimerService.elapsedSeconds.value = 0L
        TrainingTimerService.isRunning.value = false

        return st to durationSec
    }

}
