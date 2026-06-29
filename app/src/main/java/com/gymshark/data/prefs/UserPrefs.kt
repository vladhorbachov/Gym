package com.gymshark.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCurrentUserId(userId: String) {
        prefs.edit { putString(KEY_CURRENT_USER_ID, userId) }
    }

    fun getCurrentUserId(): String? {
        return prefs.getString(KEY_CURRENT_USER_ID, null)
    }

    fun clearCurrentUserId() {
        prefs.edit { remove(KEY_CURRENT_USER_ID) }
    }

    fun clearAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}
