package com.gymshark.data.settings

interface SettingsRepository {
    fun getVersion(): Int
    fun setVersion(version: Int)
    fun getLastTrainingId(): Int
}