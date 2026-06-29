package com.gymshark.service.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class TrainingTimerService : Service() {

    companion object {
        val elapsedSeconds = MutableStateFlow(0L)
        val isRunning = MutableStateFlow(false)
        const val CHANNEL_ID = "training_timer"
    }

    private var startRealtime = 0L
    private var accumulated = 0L
    private var running = false
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()

        ServiceCompat.startForeground(
            this,
            1,
            notification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> start()
            "PAUSE" -> pause()
            "STOP" -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        if (running) return
        running = true
        isRunning.value = true
        accumulated = elapsedSeconds.value
        startRealtime = SystemClock.elapsedRealtime()

        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (running) {
                val now = SystemClock.elapsedRealtime()
                elapsedSeconds.value =
                    accumulated + (now - startRealtime) / 1000
                delay(1000)
            }
        }
    }

    private fun pause() {
        if (!running) return
        accumulated += (SystemClock.elapsedRealtime() - startRealtime) / 1000
        running = false
        isRunning.value = false
        job?.cancel()
    }

    private fun stop() {
        pause()
        isRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        isRunning.value = false
        job?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun notification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Training in progress")
            .setContentText("Timer is running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Training Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
