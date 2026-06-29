package com.gymshark.data.settings

import android.content.SharedPreferences

const val VERSION = "version"
class SettingsRepositoryImpl(
    private val prefs: SharedPreferences
): SettingsRepository {
    private val editor: SharedPreferences.Editor=prefs.edit()
    override fun getVersion(): Int {
        return prefs.getInt(VERSION, 0)
    }

    override fun setVersion(version: Int) {
        editor.putInt(VERSION,version).apply()
    }
    override fun getLastTrainingId(): Int {
        return 0
    }

}