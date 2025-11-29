package com.gymshark.data.auth

interface SettingsRepository {
    fun getVersion(): Int
    fun setVersion(version: Int)
    fun getLastTrainingId(): Int
}